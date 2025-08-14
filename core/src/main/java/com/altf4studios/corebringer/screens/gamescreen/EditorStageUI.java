package com.altf4studios.corebringer.screens.gamescreen;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.entities.Enemy;
import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.interpreter.CodeSimulator;
import com.altf4studios.corebringer.utils.LoggingUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class EditorStageUI {
    private Stage editorStage;
    private Skin skin;
    private Main corebringer;
    private CodeSimulator codeSimulator;

    // UI Components
    private Table editorTable;
    private Table submenuTable;
    private TextArea codeInputArea;
    private Window outputWindow;
    private TextArea outputArea;
    private TextButton btnRunCode;
    private TextButton btnRunClass;
    private TextButton btnOptions;
    private TextButton btnLog;
    private TextButton btnCheckDeck;
    private TextButton btnCharacter;
    private Player player;
    private Enemy enemy;
    private List<String> listofcards;
    private ScrollPane scrolllistofcards;
    private Array<String> carddescription;
    private SampleCardHandler selectedcard;

    public EditorStageUI(Stage editorStage, Skin skin, Main corebringer, CodeSimulator codeSimulator, Player player, Enemy enemy) {
        this.editorStage = editorStage;
        this.skin = skin;
        this.corebringer = corebringer;
        this.codeSimulator = codeSimulator;
        this.editorStage = editorStage;
        this.skin = skin;
        this.corebringer = corebringer;
        this.codeSimulator = codeSimulator;
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
        btnRunClass = new TextButton("Run Class", skin);

        // Table for code input and run button
        Table codeInputTable = new Table();
        codeInputTable.left().top();
        codeInputTable.add(codeInputArea).growX().padRight(10).padLeft(15);

        // Button table
        Table buttonTable = new Table();
        buttonTable.add(btnRunCode).width(100).height(40).padRight(5).row();
        buttonTable.add(btnRunClass).width(100).height(40);

        codeInputTable.add(buttonTable).top();

        // When Run Code is clicked, execute code snippet and show result
        btnRunCode.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String code = codeInputArea.getText();
                System.out.println("Executing code: " + code);
                String result = codeSimulator.simulate(code);
                System.out.println("Raw result: '" + result + "'");

                // Filter out debug messages and show only actual output
                String cleanResult = filterOutput(result);
                outputArea.setText(cleanResult.isEmpty() ? "// No output" : cleanResult);
                showOutputWindow("Code Execution Result", cleanResult);
                System.out.println("Code Result: " + result);
            }
        });

        // When Run Class is clicked, compile and execute class code
        btnRunClass.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String code = codeInputArea.getText();
                System.out.println("Executing class code: " + code);
                String result = codeSimulator.compileAndExecute(code);
                System.out.println("Raw class result: '" + result + "'");

                // Filter out debug messages and show only actual output
                String cleanResult = filterOutput(result);
                outputArea.setText(cleanResult.isEmpty() ? "// No output" : cleanResult);
                showOutputWindow("Class Execution Result", cleanResult);
                System.out.println("Class Result: " + result);
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

    /**
     * Creates the output window (initially hidden)
     */
    private void createOutputWindow() {
        // Create output area
        outputArea = new TextArea("// Output will appear here\n", skin);
        outputArea.setPrefRows(4);
        outputArea.setScale(0.8f);
        outputArea.setDisabled(true); // Make it read-only

        // Create output window
        outputWindow = new Window("Output", skin);
        outputWindow.setModal(false);
        outputWindow.setMovable(true);
        outputWindow.setResizable(true);
        outputWindow.pad(20);
        outputWindow.setSize(600, 400);
        outputWindow.setPosition(
            Gdx.graphics.getWidth() / 2 - 300,
            Gdx.graphics.getHeight() / 2 - 200
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
    }

    /**
     * Hides the output window
     */
    private void hideOutputWindow() {
        outputWindow.setVisible(false);
    }

    /**
     * Filters out debug messages and returns only the actual output
     */
    private String filterOutput(String result) {
        if (result == null || result.isEmpty()) {
            return "";
        }

        // Remove debug messages that start with specific patterns
        String[] lines = result.split("\n");
        StringBuilder cleanOutput = new StringBuilder();

        for (String line : lines) {
            // Skip debug messages
            if (line.startsWith("CodeSimulator.") ||
                line.startsWith("JShellExecutor.") ||
                line.startsWith("ClassManager.") ||
                line.startsWith("Executing code:") ||
                line.startsWith("Executing class code:") ||
                line.startsWith("Raw result:") ||
                line.startsWith("Raw class result:") ||
                line.startsWith("Captured") ||
                line.startsWith("Final") ||
                line.startsWith("Code Result:") ||
                line.startsWith("Class Result:") ||
                line.startsWith("Executing:") ||
                line.contains("called with") ||
                line.contains("Captured System.out") ||
                line.contains("Captured JShell output") ||
                line.contains("Final output") ||
                line.contains("Final executeMainMethod output")) {
                continue;
            }

            // Skip empty lines and lines that are just debug info
            if (line.trim().isEmpty() || line.trim().equals("'") || line.trim().equals("''")) {
                continue;
            }

            cleanOutput.append(line).append("\n");
        }

        return cleanOutput.toString().trim();
    }
}
