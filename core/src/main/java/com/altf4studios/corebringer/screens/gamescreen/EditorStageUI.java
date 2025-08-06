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
    private TextButton btnRunCode;
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
        setupEditorUI();
        Gdx.app.log("EditorStageUI", "Editor stage UI initialized successfully");
    }

    private void setupEditorUI() {
        float worldHeight = editorStage.getViewport().getWorldHeight();
        float worldWidth = editorStage.getViewport().getWorldWidth();

        editorTable = new Table();
        Texture editorBG = new Texture(Gdx.files.internal("ui/UI_v3.png"));
        Drawable editorTableDrawable = new TextureRegionDrawable(new TextureRegion(editorBG));

        // Create code input area and run button
        codeInputArea = new TextArea("// Write your code here\n", skin);
        codeInputArea.setPrefRows(4);
        codeInputArea.setScale(0.8f);
        btnRunCode = new TextButton("Run", skin);

        // Table for code input and run button
        Table codeInputTable = new Table();
        codeInputTable.left().top();
        codeInputTable.add(codeInputArea).growX().padRight(10).padLeft(15);
        codeInputTable.add(btnRunCode).width(80).height(40).top();

        // When Run is clicked, execute code and show result
        btnRunCode.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String code = codeInputArea.getText();
                String result = codeSimulator.simulate(code);
                System.out.println(result);
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
        leftEditorTable.add(codeInputTable).growX().row();

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
}
