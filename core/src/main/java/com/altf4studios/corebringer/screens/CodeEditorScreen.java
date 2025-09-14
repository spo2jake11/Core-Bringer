package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.Utils;
import com.altf4studios.corebringer.compiler.CodePolicyValidator;
import com.altf4studios.corebringer.compiler.JavaExternalRunner;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Random;
import java.util.regex.Pattern;

public class CodeEditorScreen implements Screen {
    private final Main corebringer;
    private final Stage stage;
    private final Skin skin;

    private final JavaExternalRunner javaRunner = new JavaExternalRunner();

    // Background
    private Texture backgroundTexture;
    private Image backgroundImage;

    private Window outputWindow;
    private TextArea outputArea;
    private TextArea codeInputArea;
    private Label outputLabel;
    private Label questionLabel;

    private Array<QuizQuestion> questions;
    private QuizQuestion current;
    private final Random random = new Random();

    private int energy;
    private final int MAX_ENERGY = 10;
    private Label energyLabel;
    private Dialog maxEnergyDialog;

    // --- Session Points for Energy Transfer ---
    private int sessionPoints = 0;
    // --- End Session Points ---

    public CodeEditorScreen(Main corebringer) {
        this.corebringer = corebringer;
        this.skin = corebringer.testskin;
        this.stage = new Stage(new ScreenViewport());

        // Background setup (add first so it renders behind other actors)
        try {
            backgroundTexture = new Texture(Utils.getInternalPath("assets/backgrounds/code_window_bg.png"));
            backgroundImage = new Image(backgroundTexture);
            backgroundImage.setFillParent(true);
            stage.addActor(backgroundImage);
        } catch (Exception e) {
            Gdx.app.error("CodeEditorScreen", "Failed to load background image: " + e.getMessage());
        }
        // Initialize energy from GameScreen if available
        if (corebringer.gameScreen != null) {
            this.energy = corebringer.gameScreen.getEnergy();
        } else {
            this.energy = 0;
        }
        loadQuestions();
        buildUI();
    }

    private void loadQuestions() {
        questions = new Array<>();
        try {
            JsonReader reader = new JsonReader();
            JsonValue root;
            if (Gdx.files.internal("variables_quiz.json").exists()) {
                root = reader.parse(Gdx.files.internal("variables_quiz.json"));
            } else if (Gdx.files.internal("assets/variables_quiz.json").exists()) {
                root = reader.parse(Gdx.files.internal("assets/variables_quiz.json"));
            } else {
                Gdx.app.error("CodeEditorScreen", "variables_quiz.json not found in assets");
                return;
            }
            for (JsonValue item : root) {
                QuizQuestion q = new QuizQuestion();
                q.id = item.getInt("id");
                q.question = item.getString("question");
                q.expectedOutputRegex = item.get("validations").getString("expectedOutputRegex");
                q.codePatterns = new Array<>();
                for (JsonValue p : item.get("validations").get("codePatterns")) {
                    q.codePatterns.add(p.asString());
                }
                JsonValue points = item.get("points");
                q.pointClass = points.getInt("class", 0);
                q.pointMain = points.getInt("mainMethod", 0);
                q.pointVarDecl = points.getInt("variableDeclaration", 0);
                q.pointAssign = points.getInt("assignment", 0);
                q.pointPrint = points.getInt("print", 0);
                q.pointCorrectOutput = points.getInt("correctOutput", 0);
                questions.add(q);
            }
        } catch (Exception e) {
            Gdx.app.error("CodeEditorScreen", "Failed to load questions: " + e.getMessage());
        }
    }

    private void pickRandomQuestion() {
        if (questions == null || questions.size == 0) {
            current = null;
            if (questionLabel != null) questionLabel.setText("No questions available.");
            return;
        }
        current = questions.get(random.nextInt(questions.size));
        if (questionLabel != null) {
            questionLabel.setText("Q" + current.id + ": " + current.question);
        }
    }

    private void buildUI() {
        // Question panel (top-left half)
        Table questionTable = new Table();
        questionTable.top().left();
        questionLabel = new Label("Loading questions...", skin);
        questionLabel.setAlignment(Align.topLeft);
        questionLabel.setWrap(true);
        ScrollPane questionPane = new ScrollPane(questionLabel, skin);
        questionPane.setFadeScrollBars(false);
        questionTable.add(questionPane).expand().fill().padLeft(5);

        // Code editor panel
        codeInputArea = new TextArea("// Type your solution here\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println(\"\");\n    }\n}\n", skin);
        codeInputArea.setPrefRows(20);
        // Tab/Enter behavior (simple)
        codeInputArea.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.TAB) {
                    insertAtCursor(codeInputArea, "    ");
                    return true;
                }
                return false;
            }
        });

        // Buttons
        TextButton btnRun = new TextButton("Run Code", skin);
        TextButton btnBack = new TextButton("Back to Game", skin);
        TextButton btnNextQ = new TextButton("Next Question", skin);
        outputLabel = new Label("Ready", skin);

        btnRun.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                runAndJudge();
            }
        });
        btnBack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.gameScreen);
            }
        });
        btnNextQ.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                pickRandomQuestion();
            }
        });

        // Add energy label at top left
        energyLabel = new Label("Energy: " + energy + "/" + MAX_ENERGY, skin);
        energyLabel.setAlignment(Align.topLeft);
        energyLabel.setPosition(10, Gdx.graphics.getHeight() - 30);
        stage.addActor(energyLabel);

        // Add a button to gain energy for demonstration
        TextButton btnGainEnergy = new TextButton("Gain Energy", skin);
        btnGainEnergy.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                addEnergy(1);
            }
        });
        Table right = new Table();
        right.add(new ScrollPane(codeInputArea, skin)).grow().colspan(2).row();
        right.add(btnRun).padTop(10).left();
        right.add(btnNextQ).padTop(10).left().row();
        right.add(btnBack).padTop(10).left();
        right.add(outputLabel).padTop(10).left();
        right.add(btnGainEnergy).padTop(10).left().row();

        // Layout root
        Table root = new Table();
        root.setFillParent(true);

        // Left column is question (half width), right column is code editor
        root.add(questionTable).width(Gdx.graphics.getWidth() * 0.5f).growY().pad(10);
        root.add(right).grow().pad(10);

        stage.addActor(root);

        createOutputWindow();

        // ESC to close output window if open
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    if (outputWindow != null && outputWindow.isVisible()) {
                        outputWindow.setVisible(false);
                        return true;
                    }
                }
                return false;
            }
        });

        // Ensure a question is shown immediately
        pickRandomQuestion();
    }

    private void runAndJudge() {
        final String code = codeInputArea.getText();
        outputLabel.setText("Compiling...");
        new Thread(() -> {
            try {
                CodePolicyValidator.ValidationResult vr = CodePolicyValidator.validate(code);
                if (!vr.valid) {
                    Gdx.app.postRunnable(() -> {
                        showResult("Validation Error", "❌ VALIDATION FAILED:\n" + vr.message + "\n\n" + CodePolicyValidator.policyTemplate());
                        outputLabel.setText("Validation failed");
                    });
                    return;
                }

                String result = javaRunner.compileAndRun(code);
                boolean correct = evaluateCorrectness(code, result);
                String judgement = correct ? "\n\n=== Judgement: ✅ Correct ===" : "\n\n=== Judgement: ❌ Incorrect ===";
                int score = computeScore(code, result);
                // --- Add to session points if correct ---
                if (score > 0) sessionPoints += score;
                // --- End session points logic ---
                String scoreLine = "\nPoints: " + score + "/" + (current != null ? current.totalPoints() : 0);
                final String finalText = result + judgement + scoreLine;

                Gdx.app.postRunnable(() -> {
                    showResult("Code Execution Result", finalText);
                    outputLabel.setText(correct ? "Correct" : "Incorrect");
                });
            } catch (Exception e) {
                Gdx.app.postRunnable(() -> {
                    showResult("Error", "❌ ERROR: " + e.getMessage());
                    outputLabel.setText("Error");
                });
            }
        }).start();
    }

    private boolean evaluateCorrectness(String code, String runnerOutput) {
        if (current == null) return false;
        // Must compile and run successfully producing output
        if (runnerOutput == null) return false;
        if (!runnerOutput.startsWith("Program")) return false;
        // Extract actual output lines
        String actual = runnerOutput;
        int idx = runnerOutput.indexOf("Program output:\n");
        if (idx >= 0) {
            actual = runnerOutput.substring(idx + "Program output:\n".length()).trim();
        }
        boolean outputOk = Pattern.compile(current.expectedOutputRegex, Pattern.MULTILINE).matcher(actual).find();
        boolean codeOk = true;
        for (String pattern : current.codePatterns) {
            try {
                if (!Pattern.compile(pattern).matcher(code).find()) {
                    codeOk = false; break;
                }
            } catch (Exception ignored) { codeOk = false; break; }
        }
        return outputOk && codeOk;
    }

    private int computeScore(String code, String runnerOutput) {
        if (current == null) return 0;
        int score = 0;
        if (code.contains("public class ")) score += current.pointClass;
        if (code.contains("public static void main")) score += current.pointMain;
        // Heuristic mappings using patterns
        boolean decl = false, assign = false, prnt = false;
        for (String pattern : current.codePatterns) {
            try {
                if (pattern.contains("=") && Pattern.compile(pattern).matcher(code).find()) {
                    if (pattern.contains("println")) prnt = true;
                    else { decl = true; assign = true; }
                } else if (pattern.toLowerCase().contains("println") && Pattern.compile(pattern).matcher(code).find()) {
                    prnt = true;
                }
            } catch (Exception ignored) {}
        }
        if (decl) score += current.pointVarDecl;
        if (assign) score += current.pointAssign;
        if (prnt) score += current.pointPrint;

        // Output correctness
        String actual = runnerOutput;
        int idx = runnerOutput.indexOf("Program output:\n");
        if (idx >= 0) actual = runnerOutput.substring(idx + "Program output:\n".length()).trim();
        if (Pattern.compile(current.expectedOutputRegex, Pattern.MULTILINE).matcher(actual).find()) {
            score += current.pointCorrectOutput;
        }
        return score;
    }

    private void createOutputWindow() {
        outputArea = new TextArea("", skin);
        outputArea.setDisabled(true);
        outputWindow = new Window("Output", skin);
        outputWindow.setModal(false);
        outputWindow.setMovable(true);
        outputWindow.setResizable(false);
        outputWindow.pad(20);
        outputWindow.setSize(900, 600);
        outputWindow.setPosition(
            Gdx.graphics.getWidth() / 2f - 450f,
            Gdx.graphics.getHeight() / 2f - 300f
        );
        TextButton btnClose = new TextButton("Close", skin);
        btnClose.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                outputWindow.setVisible(false);
            }
        });
        Table bottom = new Table();
        bottom.add(btnClose).right();
        outputWindow.add(new ScrollPane(outputArea, skin)).grow().row();
        outputWindow.add(bottom).right();
        outputWindow.setVisible(false);
        stage.addActor(outputWindow);
    }

    private void showResult(String title, String content) {

        outputArea.setText(content);
        outputWindow.setVisible(true);
        outputWindow.toFront();
        float cx = Gdx.graphics.getWidth() / 2f - outputWindow.getWidth() / 2f;
        float cy = Gdx.graphics.getHeight() / 2f - outputWindow.getHeight() / 2f;
        outputWindow.setPosition(cx, cy);
    }

    private static void insertAtCursor(TextArea area, String toInsert) {
        String text = area.getText();
        int cursor = area.getCursorPosition();
        String before = text.substring(0, cursor);
        String after = text.substring(cursor);
        area.setText(before + toInsert + after);
        area.setCursorPosition(cursor + toInsert.length());
    }

    public void addEnergy(int amount) {
        if (energy >= MAX_ENERGY) {
            showMaxEnergyDialog();
            return;
        }
        energy = Math.min(energy + amount, MAX_ENERGY);
        updateEnergyLabel();
    }
    private void updateEnergyLabel() {
        if (energyLabel != null) {
            energyLabel.setText("Energy: " + energy + "/" + MAX_ENERGY);
        }
    }
    private void showMaxEnergyDialog() {
        if (maxEnergyDialog == null) {
            maxEnergyDialog = new Dialog("Max Energy", skin) {
                @Override
                protected void result(Object object) {
                    this.hide();
                }
            };
            maxEnergyDialog.text("You have reached the maximum energy (10) and cannot obtain more.");
            maxEnergyDialog.button("OK");
        }
        maxEnergyDialog.show(stage);
    }

    // --- Session Points Getter/Resetter ---
    public int consumeSessionPoints() {
        int pts = sessionPoints;
        sessionPoints = 0;
        return pts;
    }
    // --- End Session Points Getter/Resetter ---

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        pickRandomQuestion();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.12f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {
        // Sync energy back to GameScreen
        if (corebringer.gameScreen != null) {
            corebringer.gameScreen.setEnergy(energy);
        }
    }
    @Override public void dispose() {
        stage.dispose();
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }

    private static class QuizQuestion {
        int id;
        String question;
        Array<String> codePatterns;
        String expectedOutputRegex;
        int pointClass;
        int pointMain;
        int pointVarDecl;
        int pointAssign;
        int pointPrint;
        int pointCorrectOutput;
        int totalPoints() {
            return pointClass + pointMain + pointVarDecl + pointAssign + pointPrint + pointCorrectOutput;
        }
    }
}

