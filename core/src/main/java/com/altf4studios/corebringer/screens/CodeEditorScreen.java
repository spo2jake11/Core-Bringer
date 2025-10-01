package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.Utils;
import com.altf4studios.corebringer.compiler.CodePolicyValidator;
import com.altf4studios.corebringer.compiler.JavaExternalRunner;
import com.altf4studios.corebringer.quiz.CodeEvaluationService;
import com.altf4studios.corebringer.quiz.QuestionnaireManager;
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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

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
    private Label keyPointsLabel;
    private boolean showHints = true;

    private QuestionnaireManager.Question currentQ;
    private final CodeEvaluationService evaluator = new CodeEvaluationService();

    private int energy;
    private final int MAX_ENERGY = 3;
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
        try {
            QuestionnaireManager.get().initDefault();
            if (!QuestionnaireManager.get().isReady()) {
                Gdx.app.error("CodeEditorScreen", "questionnaire.json not ready or empty");
            }
        } catch (Exception e) {
            Gdx.app.error("CodeEditorScreen", "Failed to initialize questionnaire: " + e.getMessage());
        }
    }

    private void pickRandomQuestion() {
        if (!QuestionnaireManager.get().isReady()) {
            currentQ = null;
            if (questionLabel != null) questionLabel.setText("No questions available.");
            if (keyPointsLabel != null) keyPointsLabel.setText("");
            return;
        }
        currentQ = QuestionnaireManager.get().getRandomUnsolved();
        if (questionLabel != null && currentQ != null) {
            questionLabel.setText("Q" + currentQ.id + ": " + currentQ.questions);
        }
        if (keyPointsLabel != null && currentQ != null) {
            keyPointsLabel.setText(showHints ? formatKeyPoints(currentQ.keyPoints) : "");
        }
    }

    private void buildUI() {
        // Question panel (top-left half)
        Table questionTable = new Table();
        questionTable.top().left();
        questionLabel = new Label("Loading questions...", skin);
        questionLabel.setAlignment(Align.topLeft);
        questionLabel.setWrap(true);
        keyPointsLabel = new Label("", skin);
        keyPointsLabel.setWrap(true);
        keyPointsLabel.setAlignment(Align.topLeft);

        // Container for question + hints
        Table qContainer = new Table();
        qContainer.add(questionLabel).expandX().fillX().padBottom(6).row();
        qContainer.add(keyPointsLabel).expandX().fillX();

        ScrollPane questionPane = new ScrollPane(qContainer, skin);
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
        TextButton btnToggleHints = new TextButton("Hide Hints", skin);
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
        btnToggleHints.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showHints = !showHints;
                btnToggleHints.setText(showHints ? "Hide Hints" : "Show Hints");
                if (currentQ != null && keyPointsLabel != null) {
                    keyPointsLabel.setText(showHints ? formatKeyPoints(currentQ.keyPoints) : "");
                }
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
        right.add(btnToggleHints).padTop(10).left().row();
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

    private String formatKeyPoints(com.badlogic.gdx.utils.Array<String> keyPoints) {
        if (keyPoints == null || keyPoints.size == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("Hints:\n");
        for (String kp : keyPoints) {
            String h = humanizeKeyPoint(kp);
            if (!h.isEmpty()) sb.append("• ").append(h).append('\n');
        }
        return sb.toString();
    }

    private String humanizeKeyPoint(String kp) {
        if (kp == null) return "";
        String k = kp.trim();
        if (k.startsWith("needs:")) {
            String need = k.substring(6);
            if (need.contains("|")) {
                return "Use one of: " + need.replace('|', '/');
            }
            String[] parts = need.split(":");
            String type = parts[0];
            int n = 1;
            if (parts.length > 1) try { n = Integer.parseInt(parts[1]); } catch (Exception ignored) {}
            switch (type) {
                case "print": return "Print something using System.out";
                case "int": return n == 1 ? "Use at least 1 int variable" : ("Use at least " + n + " int variables");
                case "string": return n == 1 ? "Use a String" : ("Use at least " + n + " Strings");
                case "char": return "Use a char";
                case "double": return "Use a double";
                case "float": return "Use a float";
                default: return k;
            }
        }
        if (k.startsWith("op:")) {
            String op = k.substring(3);
            switch (op) {
                case "add": return "Perform addition";
                case "sub": return "Perform subtraction";
                case "mul": return "Perform multiplication";
                case "div": return "Perform division";
                default: return k;
            }
        }
        return k;
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
                // Extract actual program output (optional)
                String actual = result;
                int idx = result.indexOf("Program output:\n");
                if (idx >= 0) {
                    actual = result.substring(idx + "Program output:\n".length()).trim();
                }

                QuestionnaireManager.Question q = currentQ;
                CodeEvaluationService.EvaluationResult ev = evaluator.evaluate(q, code, actual, null);

                if (ev.passed && q != null) {
                    QuestionnaireManager.get().markSolved(q.id);
                }

                StringBuilder fb = new StringBuilder();
                for (String f : ev.feedback) fb.append("- ").append(f).append('\n');

                String judgement = ev.passed ? "\n\n=== Judgement: ✅ Correct ===" : "\n\n=== Judgement: ❌ Incorrect ===";
                String scoreLine = "\nScore: " + ev.score + "/" + ev.total + "\n\nFeedback:\n" + fb;
                final String finalText = result + judgement + scoreLine;

                Gdx.app.postRunnable(() -> {
                    showResult("Code Execution Result", finalText);
                    outputLabel.setText(ev.passed ? "Correct" : "Incorrect");
                });
            } catch (Exception e) {
                Gdx.app.postRunnable(() -> {
                    showResult("Error", "❌ ERROR: " + e.getMessage());
                    outputLabel.setText("Error");
                });
            }
        }).start();
    }

    // Old variable_quiz-based evaluation removed; now handled by CodeEvaluationService

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

    // Removed legacy QuizQuestion structure in favor of QuestionnaireManager.Question
}

