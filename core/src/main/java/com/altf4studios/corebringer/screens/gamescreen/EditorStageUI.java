package com.altf4studios.corebringer.screens.gamescreen;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.entities.Enemy;
import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.compiler.JavaExternalRunner;
import com.altf4studios.corebringer.compiler.CodePolicyValidator;
import com.altf4studios.corebringer.utils.LoggingUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.StringBuilder;

public class EditorStageUI {

    private TextButton btnJournal;
    private Stage editorStage;
    private Skin skin;
    private Main corebringer;


    // UI Components
    private Table editorTable;
    private Window optionsWindow;
    private TextArea codeInputArea;
    private Window outputWindow;
    private TextArea outputArea;
    private TextButton btnRunCode;
    private TextButton btnRunClass;
    private TextButton btnCodeOnly;
    private Label outputLabel;
    private final JavaExternalRunner javaRunner = new JavaExternalRunner();
    private Player player;
    private Enemy enemy;
    private List<String> listofcards;
    private ScrollPane scrolllistofcards;
    private Array<String> carddescription;
    private SampleCardHandler selectedcard;

    public EditorStageUI(Stage editorStage, Skin skin, Main corebringer, Player player, Enemy enemy) {
        this.editorStage = editorStage;
        this.skin = skin;
        this.corebringer = corebringer;

        this.player = player;
        this.enemy = enemy;
        setupEditorUI();
        Gdx.app.log("EditorStageUI", "Editor stage UI initialized successfully");
    }

    private void setupEditorUI() {
        float worldHeight = editorStage.getViewport().getWorldHeight();
        float worldWidth = editorStage.getViewport().getWorldWidth();

        editorTable = new Table();
        Texture editorBG = new Texture(Gdx.files.internal("ui/UI_v3.png"));
        Drawable editorTableDrawable = new TextureRegionDrawable(new TextureRegion(editorBG));


        // Create code input area and run buttons
        codeInputArea = new TextArea("// Write your code here\n// Examples:\n// - Snippets: int x = 5; System.out.println(x);\n// - Classes: public class MyClass { ... }\n// - Methods: public static void main(String[] args) { ... }\n", skin);
        codeInputArea.setPrefRows(5);
        codeInputArea.setScale(0.8f);
        editorStage.setKeyboardFocus(codeInputArea);

        // Create output window (initially hidden)
        createOutputWindow();

        btnRunCode = new TextButton("Run Code", skin);
        btnRunClass = new TextButton("Run Class", skin);
        btnCodeOnly = new TextButton("Code Editor Only", skin);

        // Table for code input and run button
        Table codeInputTable = new Table();
        codeInputTable.left().top();
        codeInputTable.add(codeInputArea).growX().padRight(10).padLeft(15);

        // IDE-like behavior: Tab inserts 4 spaces; Enter auto-indents +4 spaces
        codeInputArea.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.TAB) {
                    insertAtCursor(codeInputArea, "    ");
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) {
                    String text = codeInputArea.getText();
                    int cursor = codeInputArea.getCursorPosition();
                    int lineStart = text.lastIndexOf('\n', Math.max(0, cursor - 1));
                    lineStart = (lineStart == -1) ? 0 : lineStart + 1;
                    int leading = countLeadingSpaces(text, lineStart);
                    String indent = buildSpaces(leading + 4);
                    insertAtCursor(codeInputArea, "\n" + indent);
                    return true;
                }
                return false;
            }
        });
        codeInputArea.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    toggleOptionsWindow();
                    return true;
                }
                return false;
            }
        });

        // Button table with output display
        Table buttonTable = new Table();
        buttonTable.add(btnRunCode).width(120).height(40).center().row();
        buttonTable.add(btnCodeOnly).width(120).height(40).center().row();

        // Add output label below buttons (smaller, just for status)
        outputLabel = new Label("Ready to run code", skin);
        outputLabel.setColor(Color.WHITE);
        outputLabel.setWrap(true);
        outputLabel.setAlignment(Align.center);
        outputLabel.setFontScale(0.6f);
        buttonTable.add(outputLabel).width(100).height(30).center().row();

        // Add small instruction label
        Label instructionLabel = new Label("Click Run Code to compile & execute", skin);
        instructionLabel.setColor(Color.LIGHT_GRAY);
        instructionLabel.setFontScale(0.5f);
        instructionLabel.setAlignment(Align.center);
        buttonTable.add(instructionLabel).width(100).height(20).center().row();

        // Add clear button for output
        TextButton btnClearOutput = new TextButton("Clear", skin);
        btnClearOutput.setScale(0.6f);
        btnClearOutput.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                outputLabel.setText("Ready to run code");
                outputLabel.setColor(Color.WHITE);

                // Also clear output window if it's visible
                if (outputWindow.isVisible()) {
                    hideOutputWindow();
                }
            }
        });
        buttonTable.add(btnClearOutput).width(70).height(20);

        codeInputTable.add(buttonTable).top();

        // When Run Code is clicked, execute the code using CodeSimulator
        btnRunCode.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String code = codeInputArea.getText();
                System.out.println("Executing code with external javac/java: " + code);

                // Update output label to show compiling status
                outputLabel.setText("Compiling...");
                outputLabel.setColor(Color.YELLOW);

                // Execute code in a separate thread to avoid blocking UI
                new Thread(() -> {
                    try {
                        // Validate first
                        CodePolicyValidator.ValidationResult vr = CodePolicyValidator.validate(code);
                        if (!vr.valid) {
                            String result = "❌ VALIDATION FAILED:\n" + vr.message + "\n\n" + CodePolicyValidator.policyTemplate();
                            Gdx.app.postRunnable(() -> {
                                outputArea.setText(result);
                                showOutputWindow("Validation Error", result);
                                outputLabel.setText("❌ Validation failed - See output window");
                                outputLabel.setColor(Color.RED);
                            });
                            return;
                        }

                        String result = javaRunner.compileAndRun(code);

                        // Update UI on main thread
                        Gdx.app.postRunnable(() -> {
                            // Show detailed result in output window
                            outputArea.setText(result);
                            showOutputWindow("Code Execution Result", result);

                            // Update button area with brief status
                            if (result.contains("✅")) {
                                outputLabel.setText("✅ Success - See output window");
                                outputLabel.setColor(Color.GREEN);
                            } else {
                                outputLabel.setText("❌ Failed - See output window");
                                outputLabel.setColor(Color.RED);
                            }
                        });
                    } catch (Exception e) {
                        Gdx.app.postRunnable(() -> {
                            String errorResult = "❌ Unexpected error: " + e.getMessage();

                            // Show error in output window
                            outputArea.setText(errorResult);
                            showOutputWindow("Code Execution Error", errorResult);

                            // Update button area
                            outputLabel.setText("❌ Error occurred");
                            outputLabel.setColor(Color.RED);
                        });
                    }
                }).start();
            }
        });


        editorTable.bottom();
        editorTable.setFillParent(false);
        editorTable.setSize(worldWidth, worldHeight * 0.3f);
        editorTable.background(editorTableDrawable);

        // Add code input table to the editor table (submenu removed; moved to options window)
        Table leftEditorTable = new Table();
        leftEditorTable.add(codeInputTable).growX();

        editorTable.add(leftEditorTable).grow();

        editorStage.addActor(editorTable);

        // Create options window (hidden by default)
        createOptionsWindow();

        // Input: toggle options window with ESC
        editorStage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    toggleOptionsWindow();
                    return true;
                }
                return false;
            }
        });

        // Button listeners
        btnCodeOnly.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.codeEditorScreen);
            }
        });
    }

    private void createOptionsWindow() {
        Texture optionBG = new Texture(Gdx.files.internal("ui/optionsBG.png"));
        Drawable optionBGDrawable = new TextureRegionDrawable(new TextureRegion(optionBG));
        optionsWindow = new Window("Options", skin);
        optionsWindow.setModal(true);
        optionsWindow.setMovable(false);
        optionsWindow.pad(20);
        optionsWindow.setSize(640, 480);
        optionsWindow.setPosition(
            Gdx.graphics.getWidth() / 2f - 320f,
            Gdx.graphics.getHeight() / 2f - 240f
        );
        optionsWindow.background(optionBGDrawable);
        optionsWindow.setColor(1, 1, 1, 1);

        // Moved submenu actions here
        TextButton btnJournalLocal = new TextButton("Journal", skin);
        btnJournalLocal.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showBlankJournalWindow();
            }
        });

        TextButton btnToMain = new TextButton("Title", skin);
        btnToMain.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.mainMenuScreen);
                optionsWindow.remove();
            }
        });

        TextButton btnClose = new TextButton("Close", skin);
        btnClose.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                optionsWindow.setVisible(false);
                optionsWindow.remove();
            }
        });

        TextButton btnDeck = new TextButton("Deck", skin);
        TextButton btnLogs = new TextButton("Logs", skin);
        TextButton btnCharacter = new TextButton("Character", skin);

        Table content = new Table();
        content.defaults().pad(10).growX();
        content.add(new Label("Options", skin)).colspan(2).center().row();
        content.add(btnJournalLocal).row();
        content.add(btnDeck).row();
        content.add(btnLogs).row();
        content.add(btnCharacter).row();
        content.add(btnToMain).row();

        Table bottom = new Table();
        bottom.add(btnClose).right();

        optionsWindow.clear();
        optionsWindow.add(content).grow().row();
        optionsWindow.add(bottom).right();

        optionsWindow.setVisible(false);
        editorStage.addActor(optionsWindow);
    }

    private void toggleOptionsWindow() {
        if (optionsWindow == null) {
            createOptionsWindow();
        }
        boolean show = !optionsWindow.isVisible();
        optionsWindow.setVisible(show);
        if (show) optionsWindow.toFront();
    }

    // old optionsWindowUI removed (submenu migrated to optionsWindow)

    private void showBlankJournalWindow() {
        Texture optionBG = new Texture(Gdx.files.internal("ui/optionsBG.png"));
        Drawable optionBGDrawable = new TextureRegionDrawable(new TextureRegion(optionBG));
        Window journalWindow = new Window("Journal", skin);
        journalWindow.setModal(true);
        journalWindow.setMovable(false);
        journalWindow.pad(20);
        journalWindow.setSize(1300, 800);
        journalWindow.setPosition(
            Gdx.graphics.getWidth() / 2 / 2,
            Gdx.graphics.getHeight() / 2 / 2
        );
        journalWindow.background(optionBGDrawable);
        journalWindow.setColor(1, 1, 1, 1);

        TextButton btnClose = new TextButton("Close", skin);
        btnClose.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                journalWindow.remove();
            }
        });

        // Add Java tutorial text at the top left
        Label tutorialTitle = new Label("Java Tutorials", skin);
        tutorialTitle.setFontScale(1.5f);
        tutorialTitle.setAlignment(Align.left);

        Label mainMethodLabel = new Label("public static void main(String [] args) {   } // Main Syntax", skin);
        mainMethodLabel.setFontScale(1.2f);
        mainMethodLabel.setAlignment(Align.left);

        Label ifLabel = new Label("if(condition){ //code blocks here}  // if condition syntax", skin);
        ifLabel.setFontScale(1.2f);
        ifLabel.setAlignment(Align.left);

        // Add tutorial content to the top left area
        journalWindow.add(tutorialTitle).top().left().padLeft(160).padTop(100).row();
        journalWindow.add(mainMethodLabel).top().left().padLeft(20).padTop(80).row();
        journalWindow.add(ifLabel).top().left().padLeft(20).padTop(20).row();

        // Add only a close button - content is blank for now
        journalWindow.add(btnClose).padLeft(20).padBottom(20).expandY().left().bottom();

        editorStage.addActor(journalWindow);
    }

    // --- Helpers for editor behavior ---
    private static void insertAtCursor(TextArea area, String toInsert) {
        String text = area.getText();
        int cursor = area.getCursorPosition();
        String before = text.substring(0, cursor);
        String after = text.substring(cursor);
        area.setText(before + toInsert + after);
        area.setCursorPosition(cursor + toInsert.length());
    }

    private static int countLeadingSpaces(String text, int fromIndex) {
        int i = 0;
        while (fromIndex + i < text.length() && text.charAt(fromIndex + i) == ' ') {
            i++;
        }
        return i;
    }

    private static String buildSpaces(int n) {
        if (n <= 0) return "";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(' ');
        return sb.toString();
    }

    public void resize(float screenWidth, float screenHeight, float bottomHeight) {
        editorTable.setSize(screenWidth, bottomHeight);
    }

    public TextArea getCodeInputArea() {
        return codeInputArea;
    }

    public TextButton getBtnRunCode() {
        return btnRunCode;
    }

    public TextArea getOutputArea() {
        return outputArea;
    }

    public Label getOutputLabel() {
        return outputLabel;
    }

    public void setOutput(String output, Color color) {
        if (outputLabel != null) {
            outputLabel.setText(output);
            if (color != null) {
                outputLabel.setColor(color);
            }
        }
    }

    /**
     * Creates the output window (initially hidden)
     */
    private void createOutputWindow() {
        // Create output area
        outputArea = new TextArea("// Output will appear here\n", skin);
        outputArea.setPrefRows(6);
        outputArea.setScale(0.8f);
        outputArea.setDisabled(true); // Make it read-only

        // Create output window
        outputWindow = new Window("Output", skin);
        outputWindow.setModal(false);
        outputWindow.setMovable(true);
        outputWindow.setResizable(false);
        outputWindow.pad(20);
        outputWindow.setSize(900, 600); // 30% bigger: 600*1.3=780, 400*1.3=520
        outputWindow.setPosition(
            Gdx.graphics.getWidth() / 2 - 390, // 780/2 = 390
            Gdx.graphics.getHeight() / 2 - 260  // 520/2 = 260
        );

        // Add close button
        TextButton btnClose = new TextButton("Close", skin);
        btnClose.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hideOutputWindow();
            }
        });

        // Add clear button
        TextButton btnClear = new TextButton("Clear", skin);
        btnClear.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                outputArea.setText("// Output cleared\n");
            }
        });

        // Layout the window
        Table buttonTable = new Table();
        buttonTable.add(btnClear).padRight(10);
        buttonTable.add(btnClose);

        outputWindow.add(outputArea).grow().padBottom(10).row();
        outputWindow.add(buttonTable).right();

        // Initially hide the window
        outputWindow.setVisible(false);
        editorStage.addActor(outputWindow);
    }

    /**
     * Shows the output window with the given title
     */
    private void showOutputWindow(String title, String result) {
        outputWindow.setVisible(true);
        outputWindow.toFront(); // Bring to front

        // Update the output area with the result
        if (result != null && !result.isEmpty()) {
            outputArea.setText(result);
        }

        // Show and bring to front
        outputWindow.setVisible(true);
        outputWindow.toFront();

        // Position window in center of screen
        float centerX = Gdx.graphics.getWidth() / 2 - outputWindow.getWidth() / 2;
        float centerY = Gdx.graphics.getHeight() / 2 - outputWindow.getHeight() / 2;
        outputWindow.setPosition(centerX, centerY);
    }

    /**
     * Hides the output window
     */
    private void hideOutputWindow() {
        outputWindow.setVisible(false);
    }
}
