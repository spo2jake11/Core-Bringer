package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.interpreter.JShellExecutor;
import com.altf4studios.corebringer.utils.CardParser;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen implements Screen{
    /// Declaration of variables and elements here.
    private Main corebringer;

    private Stage battleStage;
    private Stage cardStage;
    private Stage editorStage;

    // CardParser instance for managing card data
    private CardParser cardParser;

    // CodeSimulator for running user code
    private com.altf4studios.corebringer.interpreter.CodeSimulator codeSimulator;
    // TextArea for code input
    private TextArea codeInputArea;
    // Run button
    private TextButton btnRunCode;


    public GameScreen(Main corebringer) {
        this.corebringer = corebringer; /// The Master Key that holds all screens together

        // Initialize CardParser
        cardParser = CardParser.getInstance();

        // Initialize CodeSimulator
        codeSimulator = new com.altf4studios.corebringer.interpreter.CodeSimulator();

        ///This stages are separated to lessen complications
        battleStage = new Stage(new ScreenViewport());
        editorStage = new Stage(new ScreenViewport());
        cardStage = new Stage(new ScreenViewport());


        ///Every stages provides a main method for them
        ///They also have local variables and objects for them to not interact with other methods
        battleStageUI();
        cardStageUI();
        editorStageUI();

        /// Here's all the things that will initialize once the Start Button is clicked.

        /// This provides lines to be able to monitor the objects' boundaries
//        battleStage.setDebugAll(true);
        editorStage.setDebugAll(true);
        cardStage.setDebugAll(true);
        JShellExecutor shell = new JShellExecutor();

    }


/// This for editorStageUI ONLY
    private Table editorTable;
    private Table submenuTable;
    private Texture editorBG;
    private Drawable editorTableDrawable;
    private Label codeLabel;
    private TextButton btnOptions;
    private TextButton btnLog;
    private TextButton btnCheckDeck;
    private TextButton btnCharacter;

    private void editorStageUI() {
        /// This is where the code editor will be
        float worldHeight = editorStage.getViewport().getWorldHeight();
        float worldWidth = editorStage.getViewport().getWorldWidth();

        editorTable = new Table();
        editorBG = new Texture(Gdx.files.internal("ui/UI_v3.png"));
        editorTableDrawable = new TextureRegionDrawable(new TextureRegion(editorBG));
        // Create code input area and run button
        codeInputArea = new TextArea("// Write your code here\n", corebringer.testskin);
        codeInputArea.setPrefRows(6);

        btnRunCode = new TextButton("Run", corebringer.testskin);

        // Table for code input and run button
        Table codeInputTable = new Table();
        codeInputTable.left().top();
        codeInputTable.add(codeInputArea).growX().padRight(10);
        codeInputTable.add(btnRunCode).width(80).height(40).top();

        // When Run is clicked, execute code and show result in codeLabel
        btnRunCode.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String code = codeInputArea.getText();
                String result = codeSimulator.simulate(code);
                
            }
        });

        editorTable.bottom();
        editorTable.setFillParent(false);
        editorTable.setSize(worldWidth, worldHeight * 0.3f);
        editorTable.background(editorTableDrawable);

        /// These now will become the submenu buttons when created
        submenuTable = new Table();
        submenuTable.bottom();
        submenuTable.setFillParent(false);
        submenuTable.setSize(editorTable.getWidth() * 0.2f, editorTable.getHeight());

        btnOptions = new TextButton("Options", corebringer.testskin);
        btnLog = new TextButton("Logs", corebringer.testskin);
        btnCheckDeck = new TextButton("Deck", corebringer.testskin);
        btnCharacter = new TextButton("Character", corebringer.testskin);

        /// Default format for submenuTable
        submenuTable.defaults().padTop(30).padBottom(30).padRight(20).padLeft(20).fill().uniform();
        /// Row 1
        submenuTable.add(btnOptions);
        submenuTable.add(btnCheckDeck).row();
        /// Row 2
        submenuTable.add(btnLog);
        submenuTable.add(btnCharacter);
        /// Everything is now added into the editorTable


        // Add code input table and codeLabel to the left, submenu to the right
        Table leftEditorTable = new Table();
        leftEditorTable.add(codeInputTable).growX().row();

        editorTable.add(leftEditorTable).grow();
        editorTable.add(submenuTable).growY().right();

        editorStage.addActor(editorTable);

        /// This Click Listener opens the option window
        btnOptions.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                optionsWindowUI();
            }
        });
    }

    /// This is for cardStageUI only
    Table cardTable = new Table();

    private void cardStageUI() {
        /// Calculate responsive dimensions
        float worldWidth = Gdx.graphics.getWidth();
        float worldHeight = Gdx.graphics.getHeight();
        float cardHeight = worldHeight * 0.2f; // 20% of screen height
        float cardWidth = (worldWidth * 0.8f) / 5; // 80% of screen width divided by 5 cards

        /// This is where the card display will be

        cardTable.bottom();
        cardTable.padBottom((worldHeight * 0.3f) + 15);
        cardTable.setFillParent(true);

        /// Load and randomize cards using CardParser
        Array<String> cardNames = new Array<>();
        if (cardParser.isCardsLoaded()) {
            Array<SampleCardHandler> allCards = cardParser.getAllCards();
            int cardCount = Math.min(5, allCards.size); // Limit to 5 cards

            // Create a list of available card names
            Array<String> availableCardNames = new Array<>();
            for (SampleCardHandler card : allCards) {
                availableCardNames.add(card.name);
            }

            // Randomly select up to 5 cards
            for (int i = 0; i < cardCount; i++) {
                if (availableCardNames.size > 0) {
                    int randomIndex = (int) (Math.random() * availableCardNames.size);
                    cardNames.add(availableCardNames.get(randomIndex));
                    Gdx.app.log("Card Loaded", availableCardNames.get(randomIndex));
                    availableCardNames.removeIndex(randomIndex); // Remove to avoid duplicates
                }
            }

            Gdx.app.log("CardStage", "Loaded " + cardNames.size + " random cards");
        } else {
            // Fallback if cards couldn't be loaded
            cardNames.add("Card 1");
            cardNames.add("Card 2");
            cardNames.add("Card 3");
            cardNames.add("Card 4");
            cardNames.add("Card 5");
            Gdx.app.error("CardStage", "Failed to load cards, using fallback names");
        }

        /// Create 5 card placeholders with card names
        Label card1 = new Label(cardNames.get(0), corebringer.testskin);
        Label card2 = new Label(cardNames.get(1), corebringer.testskin);
        Label card3 = new Label(cardNames.get(2), corebringer.testskin);
        Label card4 = new Label(cardNames.get(3), corebringer.testskin);
        Label card5 = new Label(cardNames.get(4), corebringer.testskin);

        card1.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cardInput(card1);
            }
        });

        card2.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cardInput(card2);
            }
        });

        card3.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cardInput(card3);
            }
        });

        card4.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cardInput(card4);
            }
        });

        card5.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cardInput(card5);
            }
        });

        /// Set alignment for all card labels
        card1.setAlignment(Align.center);
        card2.setAlignment(Align.center);
        card3.setAlignment(Align.center);
        card4.setAlignment(Align.center);
        card5.setAlignment(Align.center);

        /// Configure table defaults for even spacing
        cardTable.defaults().space(15).pad(5).fill().uniform();

        /// Add all 5 cards in a single row
        cardTable.add(card1).height(cardHeight).width(cardWidth);
        cardTable.add(card2).height(cardHeight).width(cardWidth);
        cardTable.add(card3).height(cardHeight).width(cardWidth);
        cardTable.add(card4).height(cardHeight).width(cardWidth);
        cardTable.add(card5).height(cardHeight).width(cardWidth);

        /// Position the card table at the top with some padding
        cardTable.padTop(20);

        cardStage.addActor(cardTable);
    }

    private void cardInput(Label label){
        Gdx.app.log("Card Used: ", label.getText() + " was used");
    }

    private void battleStageUI() {

        Table actionTable = new Table();
        actionTable.top();
        actionTable.setFillParent(true);

        /// These labels provides placeholders for the actuals objects later on
        Label userHpLabel = new Label("100", corebringer.testskin);
        Label enemyHpLabel = new Label("100", corebringer.testskin);
        Label userTemplate = new Label("Player", corebringer.testskin);
        Label enemyTemplate = new Label("Enemy", corebringer.testskin);

        /// This edits the alignment of the labels
        userHpLabel.setAlignment(Align.center);
        userTemplate.setAlignment(Align.center);
        enemyHpLabel.setAlignment(Align.center);
        enemyHpLabel.setAlignment(Align.center);

        /// This is the table configurations to be able to locate them
        actionTable.defaults().padTop(50);
        actionTable.add(userHpLabel).height(25).width(500).padLeft(50);
        actionTable.add(enemyHpLabel).height(25).width(500).padRight(50).row();
        actionTable.defaults().reset();
        actionTable.defaults().padTop(65);
        actionTable.add(userTemplate).height(50).pad(100).center();
        actionTable.add(enemyTemplate).height(50).pad(100).center();
        battleStage.addActor(actionTable);
    }

    private void optionsWindowUI(){
        /// This creates an option window
        Texture optionBG = new Texture(Gdx.files.internal("ui/optionsBG.png"));
        Drawable optionBGDrawable = new TextureRegionDrawable(new TextureRegion(optionBG));
        Window optionsWindow = new Window("", corebringer.testskin);
        optionsWindow.setModal(true);
        optionsWindow.setMovable(false);
        optionsWindow.pad(20);
        optionsWindow.setSize(640, 480);
        optionsWindow.setPosition(
            Gdx.graphics.getWidth() / 2 /2,
            Gdx.graphics.getHeight() /2 / 2
        );
        optionsWindow.background(optionBGDrawable);
        optionsWindow.setColor(1,1,1,1);
        /// This button will close the options menu
        TextButton btnClose = new TextButton("Close", corebringer.testskin);
        btnClose.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                optionsWindow.remove();
            }
        });

        TextButton btnToMain = new TextButton("Title", corebringer.testskin);
        btnToMain.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.mainMenuScreen);
                optionsWindow.remove();
            }
        });
        /// This is where we add objects into the optionsWindow
        optionsWindow.add(new Label("Options go here", corebringer.testskin)).top().colspan(2).row();
        optionsWindow.add(btnClose).growX().padLeft(20).padTop(20).space(15).bottom();
        optionsWindow.add(btnToMain).growX().padRight(20).padTop(20).space(15).bottom();

        /// This is where it is added into the stage
        editorStage.addActor(optionsWindow);
    }

    @Override
    public void show() {
        /// This gives the button functions to become clickable
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(editorStage);
        multiplexer.addProcessor(cardStage);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        /// Since there are multiple stages, they are needed to be drawn separately
        battleStage.act(delta);
        battleStage.draw();

        cardStage.act(delta);
        cardStage.draw();

        editorStage.act(delta);
        editorStage.draw();

    }


    @Override public void resize(int width, int height) {
        battleStage.getViewport().update(width, height, true);
        cardStage.getViewport().update(width, height, true);
        editorStage.getViewport().update(width, height, true);

        //This here is the resize for editorUI
        float screenHeight = editorStage.getViewport().getWorldHeight();
        float screenWidth = editorStage.getViewport().getWorldWidth();
        float bottomHeight = screenHeight * 0.3f;
        editorTable.setSize(screenWidth, bottomHeight);
        submenuTable.setSize(screenWidth * 0.2f, bottomHeight);

        //This here is the resize for cardUI
        float worldHeight = cardStage.getViewport().getWorldHeight();
        cardTable.padBottom((worldHeight * 0.3f) + 15);

    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void hide() {
    }

    @Override
    public void dispose() {
        battleStage.dispose();
        editorStage.dispose();
        cardStage.dispose();
    }
}
