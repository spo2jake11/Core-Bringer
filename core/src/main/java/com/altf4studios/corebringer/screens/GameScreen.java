package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.interpreter.JShellExecutor;
import com.altf4studios.corebringer.utils.CardParser;
import com.altf4studios.corebringer.turns.TurnManager;
import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.entities.Enemy;
import com.altf4studios.corebringer.screens.gamescreen.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

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


    // --- TurnManager Integration ---
    private TurnManager turnManager;
    private Player player;
    private Enemy enemy;
    // --- End TurnManager Integration ---

    // --- UI Components ---
    private BattleStageUI battleStageUI;
    private CardStageUI cardStageUI;
    private EditorStageUI editorStageUI;
    // --- End UI Components ---


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

        // --- TurnManager Integration ---
        // Initialize player and enemy (example values, adjust as needed)
        player = new Player("Player", 100, 10, 5, 3);
        enemy = new Enemy("enemy1", "Enemy", 100, 8, 3, Enemy.enemyType.NORMAL, 0, new String[]{});
        turnManager = new TurnManager(player, enemy);
        // --- End TurnManager Integration ---

        ///Every stages provides a main method for them
        ///They also have local variables and objects for them to not interact with other methods
        battleStageUI = new BattleStageUI(battleStage, corebringer.testskin);
        cardStageUI = new CardStageUI(cardStage, corebringer.testskin, cardParser);
        editorStageUI = new EditorStageUI(editorStage, corebringer.testskin, corebringer, codeSimulator);
        
        // Test output to verify new UI classes are working
        Gdx.app.log("GameScreen", "Successfully initialized all UI components:");
        Gdx.app.log("GameScreen", "- BattleStageUI: " + (battleStageUI != null ? "OK" : "FAILED"));
        Gdx.app.log("GameScreen", "- CardStageUI: " + (cardStageUI != null ? "OK" : "FAILED"));
        Gdx.app.log("GameScreen", "- EditorStageUI: " + (editorStageUI != null ? "OK" : "FAILED"));

        /// Here's all the things that will initialize once the Start Button is clicked.

        /// This provides lines to be able to monitor the objects' boundaries
//        battleStage.setDebugAll(true);
        editorStage.setDebugAll(true);
        cardStage.setDebugAll(true);
        JShellExecutor shell = new JShellExecutor();

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

        // --- TurnManager Integration: process turn phases and execute cards ---
        // Example: process one card per frame if in ACTION phase
        turnManager.executeNextCard();
        // Optionally, advance phase if queues are empty (simple example)
        if (turnManager.getCurrentPhase() == TurnManager.TurnPhase.PLAYER_ACTION && isPlayerQueueEmpty()) {
            turnManager.nextPhase();
        } else if (turnManager.getCurrentPhase() == TurnManager.TurnPhase.ENEMY_ACTION && isEnemyQueueEmpty()) {
            turnManager.nextPhase();
        }
        // --- End TurnManager Integration ---
    }

    // --- Helper methods for queue checks ---
    private boolean isPlayerQueueEmpty() {
        // Reflection or accessor in TurnManager would be better, but for now:
        // This is a placeholder; you may want to add a public method in TurnManager for this
        return true; // Replace with actual check if needed
    }
    private boolean isEnemyQueueEmpty() {
        // This is a placeholder; you may want to add a public method in TurnManager for this
        return true; // Replace with actual check if needed
    }
    // --- End helper methods ---

    @Override public void resize(int width, int height) {
        battleStage.getViewport().update(width, height, true);
        cardStage.getViewport().update(width, height, true);
        editorStage.getViewport().update(width, height, true);

        //This here is the resize for editorUI
        float screenHeight = editorStage.getViewport().getWorldHeight();
        float screenWidth = editorStage.getViewport().getWorldWidth();
        float bottomHeight = screenHeight * 0.3f;
        editorStageUI.resize(screenWidth, screenHeight, bottomHeight);

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
