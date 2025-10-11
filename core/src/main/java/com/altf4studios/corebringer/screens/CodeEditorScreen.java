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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

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
    // Fallback question list if QuestionnaireManager fails to initialize
    private Array<QuestionnaireManager.Question> localQuestions;

    // --- Session Points (unrelated to battle energy) ---
    private int sessionPoints = 0;

    public CodeEditorScreen(Main corebringer) {
        this.corebringer = corebringer;
        this.skin = corebringer.testskin;
        this.stage = new Stage(new ScreenViewport());

        // Background setup (add first so it renders behind other actors)
        try {
            backgroundTexture = new Texture(Utils.getInternalPath("assets/backgrounds/code_window.png"));
            backgroundImage = new Image(backgroundTexture);
            backgroundImage.setFillParent(true);
            stage.addActor(backgroundImage);
        } catch (Exception e) {
            Gdx.app.error("CodeEditorScreen", "Failed to load background image: " + e.getMessage());
        }
        loadQuestions();
        buildUI();
    }

    private void loadQuestions() {
        localQuestions = null;
        // Attempt 1: Utils internal JSON
        try {
            com.badlogic.gdx.files.FileHandle fh = Utils.getInternalPath("assets/questionnaire.json");
            Gdx.app.log("CodeEditorScreen", "Attempting Utils JSON: " + fh.path() + ", exists=" + fh.exists());
            QuestionnaireManager.get().initFromJson(fh);
        } catch (Exception ex) {
            Gdx.app.error("CodeEditorScreen", "Utils JSON load failed: " + ex.getMessage());
        }

        // Attempt 2: Gdx.files.internal JSON
        if (!QuestionnaireManager.get().isReady()) {
            try {
                com.badlogic.gdx.files.FileHandle fh2 = com.badlogic.gdx.Gdx.files.internal("assets/questionnaire.json");
                Gdx.app.log("CodeEditorScreen", "Attempting Internal JSON: " + fh2.path() + ", exists=" + fh2.exists());
                QuestionnaireManager.get().initFromJson(fh2);
            } catch (Exception ex) {
                Gdx.app.error("CodeEditorScreen", "Internal JSON load failed: " + ex.getMessage());
            }
        }

        // Attempt 3: TXT fallback
        if (!QuestionnaireManager.get().isReady()) {
            try {
                com.badlogic.gdx.files.FileHandle txt = Utils.getInternalPath("assets/questions.txt");
                if (txt != null && txt.exists()) {
                    Gdx.app.log("CodeEditorScreen", "Attempting TXT: " + txt.path());
                    QuestionnaireManager.get().initFromTxt(txt);
                }
            } catch (Exception ex) {
                Gdx.app.error("CodeEditorScreen", "TXT load failed: " + ex.getMessage());
            }
        }

        // Mirror to localQuestions or do local JSON parse
        if (QuestionnaireManager.get().isReady()) {
            int count = QuestionnaireManager.get().getAll().size;
            localQuestions = new Array<>(QuestionnaireManager.get().getAll());
            Gdx.app.log("CodeEditorScreen", "Loaded questionnaire with " + count + " questions (mirrored locally)");
        } else {
            // Final fallback: parse JSON locally (tolerates manager state issues)
            try {
                com.badlogic.gdx.files.FileHandle src = com.badlogic.gdx.Gdx.files.internal("assets/questionnaire.json");
                Gdx.app.log("CodeEditorScreen", "Attempting local JSON parse: " + src.path() + ", exists=" + src.exists());
                JsonReader reader = new JsonReader();
                JsonValue root = reader.parse(src);
                JsonValue qArr = root.get("questions");
                if (qArr != null && qArr.isArray()) {
                    localQuestions = new Array<>();
                    for (JsonValue it = qArr.child; it != null; it = it.next) {
                        QuestionnaireManager.Question q = new QuestionnaireManager.Question();
                        q.id = it.getInt("id", 0);
                        q.isSolve = it.getInt("isSolve", 0);
                        q.questions = it.getString("questions", "");
                        q.chance = it.getFloat("chance", 0.5f);
                        JsonValue kp = it.get("keyPoints");
                        if (kp != null && kp.isArray()) {
                            for (JsonValue s = kp.child; s != null; s = s.next) {
                                q.keyPoints.add(s.asString());
                            }
                        }
                        localQuestions.add(q);
                    }
                    Gdx.app.log("CodeEditorScreen", "Loaded localQuestions fallback count=" + localQuestions.size);
                }
            } catch (Exception ex) {
                Gdx.app.error("CodeEditorScreen", "Local JSON parse failed: " + ex.getMessage());
            }

            if (localQuestions == null || localQuestions.size == 0) {
                // Last resort: inject one question
                localQuestions = new Array<>();
                QuestionnaireManager.Question q = new QuestionnaireManager.Question();
                q.id = 1;
                q.isSolve = 0;
                q.questions = "Declare an int x = 1 and print it.";
                q.keyPoints.add("needs:print");
                q.keyPoints.add("needs:int:1");
                q.chance = 0.35f;
                localQuestions.add(q);
                Gdx.app.error("CodeEditorScreen", "Failed to load questions; injected 1 fallback question");
            }
        }
    }

    private void pickRandomQuestion() {
        // Prefer localQuestions if available
        if (localQuestions != null && localQuestions.size > 0) {
            int idx = MathUtils.random(localQuestions.size - 1);
            currentQ = localQuestions.get(idx);
        } else if (!QuestionnaireManager.get().isReady()) {
            currentQ = null;
            // Try localQuestions fallback
            if (localQuestions != null && localQuestions.size > 0) {
                int idx = MathUtils.random(localQuestions.size - 1);
                currentQ = localQuestions.get(idx);
            } else {
                // Try to (re)load, then retry selection once
                Gdx.app.log("CodeEditorScreen", "No questions available; reloading and retrying selection");
                loadQuestions();
                if (localQuestions != null && localQuestions.size > 0) {
                    int idx2 = MathUtils.random(localQuestions.size - 1);
                    currentQ = localQuestions.get(idx2);
                } else if (QuestionnaireManager.get().isReady()) {
                    currentQ = QuestionnaireManager.get().getRandomUnsolved();
                } else {
                    if (questionLabel != null) questionLabel.setText("No questions available.");
                    if (keyPointsLabel != null) keyPointsLabel.setText("");
                    return;
                }
            }
        }
        if (currentQ == null && QuestionnaireManager.get().isReady()) {
            currentQ = QuestionnaireManager.get().getRandomUnsolved();
        }
        if (currentQ == null) {
            // Fallback: pick first available
            Array<QuestionnaireManager.Question> all = QuestionnaireManager.get().isReady() ? QuestionnaireManager.get().getAll() : localQuestions;
            if (all != null && all.size > 0) currentQ = all.first();
            Gdx.app.log("CodeEditorScreen", "Random pick returned null; using fallback first question");
        }
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
        // Add inner padding so the first glyph of each wrapped line is not clipped by the ScrollPane scissor
        qContainer.pad(12);
        qContainer.add(questionLabel).expandX().fillX().padBottom(6).row();
        qContainer.add(keyPointsLabel).expandX().fillX();

        ScrollPane questionPane = new ScrollPane(qContainer, skin);
        questionPane.setFadeScrollBars(false);
        questionTable.add(questionPane).expand().fill().padLeft(5);

        // Code editor panel
        codeInputArea = new TextArea("// Type your solution here\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println(\"\");\n    }\n}\n", skin);
        codeInputArea.setPrefRows(20);
        // Prefer top-left alignment for text rendering
        codeInputArea.setAlignment(Align.topLeft);
        // Increase the TextArea's own internal padding by adjusting its style background
        TextField.TextFieldStyle tfStyle = new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        // Helper to create a padded drawable (draw-less is fine since container has background)
        Drawable bg = tfStyle.background != null ? tfStyle.background : skin.getDrawable("textfield");
        BaseDrawable paddedBg = new BaseDrawable(bg);
        paddedBg.setLeftWidth(bg.getLeftWidth() + 18f);
        paddedBg.setRightWidth(bg.getRightWidth() + 10f);
        paddedBg.setTopHeight(bg.getTopHeight() + 8f);
        paddedBg.setBottomHeight(bg.getBottomHeight() + 8f);
        tfStyle.background = paddedBg;
        if (tfStyle.focusedBackground != null) {
            Drawable fbg = tfStyle.focusedBackground;
            BaseDrawable paddedFbg = new BaseDrawable(fbg);
            paddedFbg.setLeftWidth(fbg.getLeftWidth() + 18f);
            paddedFbg.setRightWidth(fbg.getRightWidth() + 10f);
            paddedFbg.setTopHeight(fbg.getTopHeight() + 8f);
            paddedFbg.setBottomHeight(fbg.getBottomHeight() + 8f);
            tfStyle.focusedBackground = paddedFbg;
        }
        if (tfStyle.disabledBackground != null) {
            Drawable dbg = tfStyle.disabledBackground;
            BaseDrawable paddedDbg = new BaseDrawable(dbg);
            paddedDbg.setLeftWidth(dbg.getLeftWidth() + 18f);
            paddedDbg.setRightWidth(dbg.getRightWidth() + 10f);
            paddedDbg.setTopHeight(dbg.getTopHeight() + 8f);
            paddedDbg.setBottomHeight(dbg.getBottomHeight() + 8f);
            tfStyle.disabledBackground = paddedDbg;
        }
        codeInputArea.setStyle(tfStyle);
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

        // Removed energy UI and controls from Code Editor screen per request
        Table right = new Table();
        // Wrap TextArea in a padded container to prevent first-character clipping inside ScrollPane
        Table codeContainer = new Table();
        // Increase left padding to offset any glyph left-bearings
        codeContainer.padTop(12).padBottom(12).padLeft(12).padRight(12);
        // Outer container white to look like a border
        codeContainer.setBackground(skin.newDrawable("white", 1f, 1f, 1f, 0.8f));
        // Inner container with light gray background that holds the TextArea
        Table codeInner = new Table();
        codeInner.setBackground(skin.newDrawable("white", Color.DARK_GRAY));
        codeInner.add(codeInputArea).expand().fill();
        // Add a fixed left gutter to guarantee space before the text render origin
        codeContainer.add().width(16f).growY();
        codeContainer.add(codeInner).expand().fill();
        ScrollPane codeScroll = new ScrollPane(codeContainer, skin);
        codeScroll.setFadeScrollBars(false);
        // Avoid content overscrolling into the left scissor region
        codeScroll.setOverscroll(false, false);
        right.add(codeScroll).padLeft(10).padBottom(40).grow().colspan(2).row();
        right.add(btnRun).padLeft(15).padTop(10).left();
        right.add(btnNextQ).padTop(10).left().row();
        right.add(btnBack).padLeft(15).padTop(10).left();
        right.add(outputLabel).padTop(10).left();
        right.add(btnToggleHints).padTop(10).left().row();
        // Energy gain button removed

        // Layout root using a SplitPane to enforce 35% (left) / 65% (right)
        Table root = new Table();
        root.setFillParent(true);
        SplitPane split = new SplitPane(questionTable, right, false, skin);
        split.setSplitAmount(0.4f); // 40% left, 60% right
        // Lock divider without disabling touch so children can receive input
        split.setMinSplitAmount(0.4f);
        split.setMaxSplitAmount(0.4f);
        // Make the divider invisible by using a fully transparent handle drawable
        SplitPane.SplitPaneStyle spStyle = new SplitPane.SplitPaneStyle();
        spStyle.handle = skin.newDrawable("white", 0f, 0f, 0f, 0f);
        split.setStyle(spStyle);

        root.add(split).expand().fill().pad(5);

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
        if (k.startsWith("var:")) {
            String name = k.substring(4).trim();
            return name.isEmpty() ? k : ("Declare variable '" + name + "'");
        }
        if (k.startsWith("literal:")) {
            String lit = k.substring(8).trim();
            return lit.isEmpty() ? k : ("Include literal: " + lit);
        }
        if (k.startsWith("format:")) {
            String fmt = k.substring(7).trim();
            switch (fmt) {
                case "equation": return "Show as an equation (e.g., a + b = c)";
                case "same_line": return "Print both values on the same line";
                case "full_name": return "Combine firstName and lastName into a full name";
                case "parentheses": return "Use parentheses in the expression";
                case "nested_parentheses": return "Use nested parentheses in the expression";
                case "average": return "Compute an average (sum then divide)";
                case "unit_convert": return "Perform a unit conversion (e.g., *60, *24, etc.)";
                case "area_rectangle": return "Compute rectangle area (length * width)";
                case "perimeter_rectangle": return "Compute rectangle perimeter";
                case "area_square": return "Compute square area (side * side)";
                case "area_triangle": return "Compute triangle area (base * height / 2)";
                case "salary_total": return "Compute total salary (rate * days)";
                case "series_sum": return "Sum a numeric series";
                case "square": return "Square a number";
                case "cube": return "Cube a number";
                case "two_expressions": return "Combine two expressions (then operate)";
                case "sentence": return "Output a sentence (words combined)";
                case "separate_prints": return "Use separate print statements for each part";
                default: return k;
            }
        }
        if (k.startsWith("constraint:")) {
            String c = k.substring(11).trim();
            switch (c) {
                case "same_value": return "Two variables or outputs should have the same value";
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

                boolean instakillProc = false;
                if (ev.passed && q != null) {
                    QuestionnaireManager.get().markSolved(q.id);
                    // Overhack proc logic: use question's chance as probability
                    float chance = MathUtils.clamp(q.chance, 0f, 1f);
                    instakillProc = MathUtils.random() < chance;
                    Gdx.app.log("CodeEditorScreen", "Overhack roll: chance=" + chance + ", proc=" + instakillProc);
                    // Apply battle effect on main thread
                    final boolean finalProc = instakillProc;
                    Gdx.app.postRunnable(() -> {
                        if (corebringer != null && corebringer.gameScreen != null) {
                            com.altf4studios.corebringer.battle.BattleManager bm = corebringer.gameScreen.getBattleManager();
                            if (bm != null) {
                                if (finalProc) {
                                    bm.instakillEnemy();
                                    // Return to game screen and start delayed victory with tag
                                    corebringer.setScreen(corebringer.gameScreen);
                                    corebringer.gameScreen.startInstakillVictory(0.5f, "Instakill!!");
                                } else {
                                    bm.endPlayerTurnNow();
                                }
                            }
                        }
                    });
                }

                StringBuilder fb = new StringBuilder();
                for (String f : ev.feedback) fb.append("- ").append(f).append('\n');

                String judgement = ev.passed ? "\n\n=== Judgement: ✅ Correct ===" : "\n\n=== Judgement: ❌ Incorrect ===";
                String feedbackBlock = "\n\nFeedback:\n" + fb;
                final String finalText = result + judgement + feedbackBlock;

                final boolean incorrect = !ev.passed;
                final boolean suppressUI = (ev.passed && instakillProc) || incorrect;
                Gdx.app.postRunnable(() -> {
                    if (incorrect) {
                        // Return to game screen, skip user's turn, and show stun message
                        if (corebringer != null && corebringer.gameScreen != null) {
                            corebringer.setScreen(corebringer.gameScreen);
                            // Mirror the manual End Turn flow (hide cards, flush, enemy turn, redraw)
                            com.altf4studios.corebringer.screens.gamescreen.CardStageUI csui = corebringer.gameScreen.getCardStageUI();
                            if (csui != null) {
                                csui.endTurnProgrammatically();
                            } else {
                                // Fallback: just end the turn if UI not ready
                                com.altf4studios.corebringer.battle.BattleManager bm = corebringer.gameScreen.getBattleManager();
                                if (bm != null) bm.endPlayerTurnNow();
                            }
                            // Show centered red message for a short duration
                            corebringer.gameScreen.showCenterMessage("Incorrect Overhack. Suffer Stun", Color.RED, 2.0f);
                        }
                        // Do not show the result window
                        return;
                    }
                    if (!suppressUI) {
                        showResult("Code Execution Result", finalText);
                        outputLabel.setText(ev.passed ? "Correct" : "Incorrect");
                    }
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

    // Removed all energy-related methods and UI

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
        // Ensure questions are reloaded when screen is shown (in case singleton was cleared elsewhere)
        loadQuestions();
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
        // No energy sync with GameScreen
    }
    @Override public void dispose() {
        stage.dispose();
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }

    // Removed legacy QuizQuestion structure in favor of QuestionnaireManager.Question
}

