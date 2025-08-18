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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;

public class EditorStageUI {
    private Stage editorStage;
    private Skin skin;
    private Main corebringer;

    // UI Components
    private Table editorTable;
    private Table submenuTable;
    private TextArea codeInputArea;
    private Window outputWindow;
    private TextArea outputArea;
    private TextButton btnRunCode;
    private TextButton btnOptions;
    private TextButton btnLog;
    private TextButton btnCheckDeck;
    private TextButton btnCharacter;
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

        // Create output window (initially hidden)
        createOutputWindow();

        btnRunCode = new TextButton("Run Code", skin);

        // Table for code input and run button
        Table codeInputTable = new Table();
        codeInputTable.left().top();
        codeInputTable.add(codeInputArea).growX().padRight(10).padLeft(15);

        // Button table with output display
        Table buttonTable = new Table();
        buttonTable.add(btnRunCode).width(100).height(40).center().row();

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

        // Submenu buttons
        submenuTable = new Table();
        submenuTable.bottom();
        submenuTable.setFillParent(false);
        submenuTable.setSize(editorTable.getWidth() * 0.2f, editorTable.getHeight());

        btnOptions = new TextButton("Options", skin);
        btnLog = new TextButton("Logs", skin);
        btnCheckDeck = new TextButton("Deck", skin);
        btnCharacter = new TextButton("Character", skin);

        submenuTable.defaults().padTop(30).padBottom(30).padRight(20).padLeft(20).fill().uniform();
        submenuTable.add(btnOptions);
        submenuTable.add(btnCheckDeck).row();
        submenuTable.add(btnLog);
        submenuTable.add(btnCharacter);

        // Add code input table and submenu to the editor table
        Table leftEditorTable = new Table();
        leftEditorTable.add(codeInputTable).growX();

        editorTable.add(leftEditorTable).grow();
        editorTable.add(submenuTable).growY().right();

        editorStage.addActor(editorTable);

        // Add click listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        btnOptions.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                optionsWindowUI();
            }
        });
    }

    private void optionsWindowUI() {
        Texture optionBG = new Texture(Gdx.files.internal("ui/optionsBG.png"));
        Drawable optionBGDrawable = new TextureRegionDrawable(new TextureRegion(optionBG));
        Window optionsWindow = new Window("", skin);
        optionsWindow.setModal(true);
        optionsWindow.setMovable(false);
        optionsWindow.pad(20);
        optionsWindow.setSize(640, 480);
        optionsWindow.setPosition(
            Gdx.graphics.getWidth() / 2 / 2,
            Gdx.graphics.getHeight() / 2 / 2
        );
        optionsWindow.background(optionBGDrawable);
        optionsWindow.setColor(1, 1, 1, 1);

        TextButton btnClose = new TextButton("Close", skin);
        btnClose.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                optionsWindow.remove();
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

        optionsWindow.add(new Label("Options go here", skin)).top().colspan(2).row();
        optionsWindow.add(btnClose).growX().padLeft(20).padTop(20).space(15).bottom();
        optionsWindow.add(btnToMain).growX().padRight(20).padTop(20).space(15).bottom();

        editorStage.addActor(optionsWindow);
    }

    public void resize(float screenWidth, float screenHeight, float bottomHeight) {
        editorTable.setSize(screenWidth, bottomHeight);
        submenuTable.setSize(screenWidth * 0.2f, bottomHeight);
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
