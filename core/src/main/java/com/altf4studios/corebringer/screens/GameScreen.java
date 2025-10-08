package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
// Removed interpreter integration
import com.altf4studios.corebringer.utils.CardParser;
import com.altf4studios.corebringer.turns.TurnManager;
import com.altf4studios.corebringer.battle.BattleManager;
import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.entities.Enemy;
import com.altf4studios.corebringer.screens.gamescreen.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.altf4studios.corebringer.utils.SaveManager;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public class GameScreen implements Screen{
    /// Declaration of variables and elements here.
    private Main corebringer;

    private Stage uiStage;
    private Stage battleStage;
    private Stage cardStage;

    // CardParser instance for managing card data
    private CardParser cardParser;

    // --- Battle/Turn Management ---
    private TurnManager turnManager;
    private BattleManager battleManager;
    private Player player;
    private Enemy enemy;
    // --- End Battle/Turn Management ---

    // --- UI Components ---
    private BattleStageUI battleStageUI;
    private CardStageUI cardStageUI;
    // Top submenu HP label to sync with character image HP
    private Label topHpNumLabel;
    private Label goldLabel;
    // --- End UI Components ---

    // --- Energy System ---
    private int energy = 0;
    private final int MAX_ENERGY = 3;
    private Label energyLabel;
    private Stack energyWidget; // label + background
    private Texture energyBgTexture;
    private Window optionsWindow;
    private Window deckWindow;
    // Persisted deck ids for this run
    private String[] savedDeckIds;
    // --- End Energy System ---
    // --- Currency ---
    private int gold = 0;
    // --- End Currency ---
    // --- Deck UI helpers ---
    private TextureAtlas deckCardAtlas;
    private ObjectMap<String, String> idToAtlasName;
    // --- End Deck UI helpers ---
    // Track when we've already applied regen for the current player turn
    private boolean playerTurnEnergyApplied = false;

    // --- Death Screen ---
    private Window deathScreenWindow = null;
    private boolean deathScreenShown = false;
    // --- End Death Screen ---

    // --- Victory Screen ---
    private Window victoryScreenWindow = null;
    private boolean victoryScreenShown = false;
    // --- End Victory Screen ---

    // --- Game Lifecycle State ---
    private enum GameState { RUNNING, VICTORY, DEFEAT }
    private GameState gameState = GameState.RUNNING;
    // --- End Game Lifecycle State ---


    public GameScreen(Main corebringer) {
        this.corebringer = corebringer; /// The Master Key that holds all screens together

        // Initialize CardParser
        cardParser = CardParser.getInstance();
        // Initialize CardDataManager maps from parser data for O(1) lookups
        com.altf4studios.corebringer.utils.CardDataManager.getInstance().initFrom(cardParser);

        ///This stages are separated to lessen complications
        uiStage = new Stage(new ScreenViewport());
        battleStage = new Stage(new ScreenViewport());
        cardStage = new Stage(new ScreenViewport());


        // --- Load stats from save file (must exist at this point) ---
        int hp = 20; // Legacy fallback current HP
        int maxHpFromSave = 20; // Default fallback max HP
        int energyVal = 0;
        String[] cards = new String[]{
            "basic_variable_slash_1", "basic_variable_slash_1", "basic_variable_slash_1",
            "basic_variable_slash_1", "basic_variable_slash_1", "shield_final_shield_1",
            "shield_final_shield_1", "shield_final_shield_1", "shield_final_shield_1",
            "shield_final_shield_1", "heal_ultimate_heal_1", "heal_ultimate_heal_1",
            "heal_ultimate_heal_1", "poison_looping_bite_1", "poison_looping_bite_1"
        };
        int battleWon = 0;
        int goldFromSave = 0;
        String enemyName = "Enemy";
        int enemyHp = 20;

        // Load from save file (should exist from MainMenuScreen)
        if (SaveManager.saveExists()) {
            com.altf4studios.corebringer.utils.SaveData stats = SaveManager.loadStats();
            if (stats != null) {
                // Prefer new fields; fallback to legacy hp if needed
                hp = (stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : hp));
                maxHpFromSave = (stats.maxHp > 0 ? stats.maxHp : Math.max(hp, maxHpFromSave));
                energyVal = stats.energy;
                cards = stats.cards;
                battleWon = stats.battleWon;
                goldFromSave = stats.gold;
            }
        }

        // Load random enemy from JSON
        try {
            com.badlogic.gdx.files.FileHandle file = Gdx.files.internal("assets/enemies.json");
            String json = file.readString();
            com.badlogic.gdx.utils.JsonReader jsonReader = new com.badlogic.gdx.utils.JsonReader();
            com.badlogic.gdx.utils.JsonValue enemies = jsonReader.parse(json);
            if (enemies != null && enemies.size > 0) {
                int idx = com.badlogic.gdx.math.MathUtils.random(enemies.size - 1);
                com.badlogic.gdx.utils.JsonValue enemyData = enemies.get(idx);
                enemyName = enemyData.getString("name");
                enemyHp = enemyData.getInt("hp");
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to load enemy from JSON: " + e.getMessage());
        }

        // Expose cards to CardStageUI via getter

        // --- Battle/Turn Management ---
        // Initialize player and enemy
        // Important: do NOT set player's maxHealth to the saved current HP.
        // Create with a reasonable max, then set current HP from save so healing can increase HP.
        int playerMaxHp = Math.max(maxHpFromSave, 20); // ensure max >= 20
        player = new Player("Player", playerMaxHp, 10, 5, 3);
        player.setHp(Math.min(hp, playerMaxHp)); // clamp to max
        enemy = new Enemy("enemy1", enemyName, enemyHp, 8, 3, Enemy.enemyType.NORMAL, 0, new String[]{});
        // Temporary TurnManager for early consumers; BattleManager will hold the canonical one
        turnManager = new TurnManager(player, enemy);
        // --- End Battle/Turn Management ---


        ///Every stages provides a main method for them
        ///They also have local variables and objects for them to not interact with other methods
        battleStageUI = new BattleStageUI(battleStage, corebringer.testskin);
        // Initialize BattleManager now that UI exists and replace turnManager reference for other systems
        battleManager = new BattleManager(player, enemy, battleStageUI);
        turnManager = battleManager.getTurnManager();
        // Make saved deck available to card UI
        this.savedDeckIds = cards;
        // Initialize gold from save
        this.gold = goldFromSave;
        cardStageUI = new CardStageUI(cardStage, corebringer.testskin, cardParser, player, enemy, turnManager, this);
        createUI();

        // If battleWon == 1, reroll enemy and reset battleWon
        if (battleWon == 1) {
            battleStageUI.changeEnemy();
            battleWon = 0;
            // Persist stats with maxEnergy and current deck
            SaveManager.saveStats(player.getHp(), player.getMaxHealth(), energyVal, getMaxEnergy(), cards, battleWon, gold);
        }

        // Set energy from save
        setEnergy(energyVal);
        // Create energy label with background icon (text like 0/3)
        energyLabel = new Label(energy + "/" + MAX_ENERGY, corebringer.testskin);
        energyLabel.setAlignment(Align.center);
        // Background image from assets/icons/energy.png
        try {
            energyBgTexture = new Texture(Gdx.files.internal("assets/icons/energy.png"));
            Image bg = new Image(energyBgTexture);
            bg.setSize(100, 100);
            // Build stacked widget: background under, text over
            energyWidget = new Stack();
            energyWidget.add(bg);
            energyWidget.add(energyLabel);
            // Reasonable size for the icon + text
            energyWidget.setSize(bg.getWidth(), bg.getHeight());
            battleStage.addActor(energyWidget);
        } catch (Exception e) {
            // Fallback: add label alone if texture missing
            energyWidget = new Stack();
            energyWidget.add(energyLabel);
            battleStage.addActor(energyWidget);
        }

        // Test output to verify new UI classes are working
        Gdx.app.log("GameScreen", "Successfully initialized all UI components:");
        Gdx.app.log("GameScreen", "- BattleStageUI: " + (battleStageUI != null ? "OK" : "FAILED"));
        Gdx.app.log("GameScreen", "- CardStageUI: " + (cardStageUI != null ? "OK" : "FAILED"));

        // Test enemy atlas loading
        if (battleStageUI != null) {
            Gdx.app.log("GameScreen", "Current enemy: " + battleStageUI.getCurrentEnemyName());
            Gdx.app.log("GameScreen", "Atlas loaded: " + battleStageUI.isEnemyAtlasLoaded());
            Gdx.app.log("GameScreen", "Available enemies: " + battleStageUI.getAvailableEnemies().size);
        }

        /// Here's all the things that will initialize once the Start Button is clicked.

        /// This provides lines to be able to monitor the objects' boundaries
//        battleStage.setDebugAll(true);
//        cardStage.setDebugAll(true);
//        uiStage.setDebugAll(true);
        // Interpreter removed

    }

    public String[] getSavedDeckIds() {
        return savedDeckIds != null ? savedDeckIds : new String[]{};
    }

    public BattleManager getBattleManager() {
        return battleManager;
    }

    // Call this after any stat change (hp, energy, cards, battleWon)
    public void saveProgress(int battleWonValue) {
        String[] deck = savedDeckIds != null ? savedDeckIds : new String[]{};
        SaveManager.saveStats(player.getHp(), player.getMaxHealth(), energy, getMaxEnergy(), deck, battleWonValue, gold);
    }

    private void createUI() {
        // Main table that fills the parent
        Table tbltopUI = new Table();
        tbltopUI.setFillParent(true);
        tbltopUI.top();

        // Inner table for the gray background
        Table innerTable = new Table();
        innerTable.setBackground(corebringer.testskin.newDrawable("white", new Color(0.5f, 0.5f, 0.5f, 1)));
        innerTable.defaults().padTop(10).padBottom(10);

        // Table handler for the left side
        Table tblLeftPane = new Table();
        Label lblCharName = new Label("Player", corebringer.testskin);
        topHpNumLabel = new Label("HP: " + player.getHp() + "/" + player.getMaxHealth(), corebringer.testskin);
        // Load gold before creating UI label
        this.gold = this.gold == 0 ? 0 : this.gold; // no-op if already set
        goldLabel = new Label("Gold: " + this.gold, corebringer.testskin);

        // Table handler for the right side
        Table tblRightPane = new Table();
        Label lblMenu = new Label("Menu", corebringer.testskin);
        TextButton btnDeck = new TextButton("Deck", corebringer.testskin);
        Label lblMap = new Label("Map", corebringer.testskin);

        // Filler table
        Table tblFiller = new Table();

        // Left Pane placement
        tblLeftPane.defaults().padLeft(10).padRight(10).uniform();
        tblLeftPane.add(lblCharName);
        tblLeftPane.add(topHpNumLabel);
        tblLeftPane.add(goldLabel);

        // Right Pane placement
        tblRightPane.defaults().padRight(10).padLeft(10).uniform();
        tblRightPane.add(lblMap);
        tblRightPane.add(btnDeck);
        tblRightPane.add(lblMenu);

        // Add content to inner table
        innerTable.add(tblLeftPane).left();
        innerTable.add(tblFiller).growX();
        innerTable.add(tblRightPane).right().padRight(15);

        // Add inner table to main table with fixed height
        tbltopUI.add(innerTable).growX().height(75);

        uiStage.addActor(tbltopUI);

        // Deck button behavior: bring deck window to front and show scrollable grid
        btnDeck.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showDeckWindow();
            }
        });
    }

    private void ensureDeckResourcesLoaded() {
        if (deckCardAtlas == null) {
            deckCardAtlas = new TextureAtlas("assets/cards/cards_atlas.atlas");
        }
        if (idToAtlasName == null) {
            idToAtlasName = new ObjectMap<>();
            try {
                JsonReader reader = new JsonReader();
                JsonValue root = reader.parse(Gdx.files.internal("assets/cards.json"));
                for (JsonValue card : root.get("cards")) {
                    String id = card.getString("id", null);
                    String atlas = card.getString("atlasName", null);
                    if (id != null && atlas != null) {
                        idToAtlasName.put(id, atlas);
                    }
                }
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Failed to load cards.json for deck mapping: " + e.getMessage());
            }
        }
    }

    private void showDeckWindow() {
        ensureDeckResourcesLoaded();

        if (deckWindow != null) {
            deckWindow.toFront();
            deckWindow.setVisible(true);
            return;
        }

        float windowWidth = Gdx.graphics.getWidth() * 0.8f;
        float windowHeight = Gdx.graphics.getHeight() * 0.8f;
        float windowX = (Gdx.graphics.getWidth() - windowWidth) / 2f;
        float windowY = (Gdx.graphics.getHeight() - windowHeight) / 2f;

        deckWindow = new Window("Deck", corebringer.testskin);
        deckWindow.setModal(true);
        deckWindow.setMovable(true);
        deckWindow.setResizable(false);
        deckWindow.setSize(windowWidth, windowHeight);
        deckWindow.setPosition(windowX, windowY);

        Table grid = new Table();
        grid.defaults().pad(10);

        String[] ids = getSavedDeckIds();
        int col = 0;
        for (int i = 0; i < ids.length; i++) {
            String id = ids[i];
            String atlasName = idToAtlasName != null ? idToAtlasName.get(id) : null;
            String region = atlasName != null ? atlasName.replace(" ", "_") : "bck_card";
            TextureRegionDrawable drawable = new TextureRegionDrawable(deckCardAtlas.findRegion(region));
            Image img = new Image(drawable);
            img.setSize(150, 190);
            grid.add(img).size(150, 190);
            col++;
            if (col == 4) {
                grid.row();
                col = 0;
            }
        }

        ScrollPane scrollPane = new ScrollPane(grid, corebringer.testskin);
        scrollPane.setScrollingDisabled(true, false); // enable vertical scrolling
        scrollPane.setFadeScrollBars(false);

        // Controls row
        Table controls = new Table();
        TextButton btnClose = new TextButton("Close", corebringer.testskin);
        btnClose.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (deckWindow != null) {
                    deckWindow.remove();
                    deckWindow = null;
                }
                // Return input control back to the GameScreen's normal input processors
                show();
            }
        });
        controls.add(btnClose).right();

        Table content = new Table();
        content.setFillParent(true);
        content.add(scrollPane).expand().fill().row();
        content.add(controls).right().pad(10);

        deckWindow.add(content).expand().fill();
        uiStage.addActor(deckWindow);
        deckWindow.toFront();
        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void show() {


        // --- Energy gain from CodeEditorScreen points ---
        if (corebringer.codeEditorScreen != null) {
            int gained = corebringer.codeEditorScreen.consumeSessionPoints();
            if (gained > 0) {
                addEnergy(gained);
                Gdx.app.log("GameScreen", "Gained energy from CodeEditorScreen: " + gained);
            }
        }
        // --- End energy gain logic ---
        // Register all input processors and stages with Main's global multiplexer
        corebringer.clearInputProcessors();
        corebringer.addInputProcessor(uiStage);
        corebringer.addInputProcessor(battleStage);
        corebringer.addInputProcessor(cardStage);
        corebringer.addInputProcessor(new InputProcessor() {
            @Override public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    if (optionsWindow == null || !optionsWindow.isVisible()) {
                        showOptionsWindow();
                    } else {
                        optionsWindow.setVisible(false);
                        optionsWindow.remove();
                        // No need to reset multiplexer, it's always managed by Main
                    }
                    return true;
                }
                return false;
            }
            @Override public boolean keyUp(int keycode) { return false; }
            @Override public boolean keyTyped(char character) { return false; }
            @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
            @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
            @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
            @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
            @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
            @Override public boolean scrolled(float amountX, float amountY) { return false; }
        });
        // Add Overhack button at the bottom of the screen, below cardStageUI
        addOverhackButtonToBottom();

        // --- Ensure viewport is updated to current window size ---
        battleStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        cardStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    private void addOverhackButtonToBottom() {
        // Remove previous Overhack button if any
        for (Actor actor : battleStage.getActors()) {
            if (actor.getName() != null && actor.getName().equals("OverhackButton")) {
                actor.remove();
                break;
            }
        }
        TextButton btnRecharge = new TextButton("Overhack", corebringer.testskin);
        btnRecharge.setName("OverhackButton");
        btnRecharge.setWidth(200);
        btnRecharge.setHeight(50);
        btnRecharge.setPosition((Gdx.graphics.getWidth() / btnRecharge.getWidth()) + 50, 100);
        btnRecharge.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.codeEditorScreen);
            }
        });
        battleStage.addActor(btnRecharge);

        // Reposition energy widget to be at least 200 units above the Overhack button
        if (energyWidget != null) {
            float newX = btnRecharge.getX() + 50;
            float newY = btnRecharge.getY() + btnRecharge.getHeight() + 100f;
            energyWidget.setPosition(newX, newY);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        // Act only when game is running; always draw so UI remains visible
        if (gameState == GameState.RUNNING) {
            battleStage.act(delta);
            cardStage.act(delta);
        }
        battleStage.draw();
        cardStage.draw();
        uiStage.act(delta);
        uiStage.draw();
        // Removed: editorStage.act(delta); editorStage.draw();

        // --- BattleManager: process turn phases, enemy AI, and UI indicator ---
        if (gameState == GameState.RUNNING) {
            battleManager.update(delta);
        }
        // --- End BattleManager ---

        // --- Energy auto-regen at start of player's actionable turn ---
        // Regen once when player's turn becomes active (not during delay/poison resolution)
        if (turnManager != null) {
            boolean playerTurnActive = turnManager.isPlayerTurn() && !turnManager.isDelaying();
            if (playerTurnActive && !playerTurnEnergyApplied) {
                setEnergy(MAX_ENERGY);
                playerTurnEnergyApplied = true;
            } else if (turnManager.isEnemyTurn()) {
                // Reset flag so next player turn will regen again
                playerTurnEnergyApplied = false;
            }
        }

        /// For wiring the HP/Shield values properly
        battleStageUI.updateHpBars(player.getHp(), enemy.getHp());
        battleStageUI.updateShieldBars(player.getBlock(), enemy.getBlock());
//        battleStageUI.updateHpColors(player.hasPoison(), enemy.hasPoison());
//        battleStageUI.updateShieldColors(player.getBlock() > 0, enemy.getBlock() > 0);
//        // New: enemy HP color for bleed/stun (priority handled in UI)
//        battleStageUI.updateEnemyHpStatusColor(enemy.hasPoison(), enemy.hasStatus("Bleed"), enemy.hasStatus("Stun"));

        // Turn indicator is updated by BattleManager
        // Sync top submenu HP label with current player HP each frame
        if (topHpNumLabel != null) {
            topHpNumLabel.setText("HP: " + player.getHp() + "/" + player.getMaxHealth());
        }

        // --- Death Screen Trigger ---
        if (!deathScreenShown && player.getHp() <= 0) {
            showDeathScreen();
        }
        // --- End Death Screen Trigger ---

        // --- Victory Screen Trigger ---
        if (!victoryScreenShown && !deathScreenShown && enemy.getHp() <= 0) {
            showVictoryScreen();
        }
        // --- End Victory Screen Trigger ---
    }



    // --- Test methods for enemy changing ---
    public void testChangeEnemy() {
        if (battleStageUI != null) {
            battleStageUI.changeEnemy();
            Gdx.app.log("GameScreen", "Changed enemy to: " + battleStageUI.getCurrentEnemyName());
        }
    }

    public void testChangeToSpecificEnemy(String enemyName) {
        if (battleStageUI != null) {
            battleStageUI.changeEnemy(enemyName);
            Gdx.app.log("GameScreen", "Changed enemy to: " + battleStageUI.getCurrentEnemyName());
        }
    }

    public void testTurnSystem() {
        if (turnManager != null) {
            Gdx.app.log("GameScreen", "Testing turn system...");
            Gdx.app.log("GameScreen", "Current phase: " + turnManager.getCurrentPhase());
            Gdx.app.log("GameScreen", "Is player turn: " + turnManager.isPlayerTurn());
            Gdx.app.log("GameScreen", "Is enemy turn: " + turnManager.isEnemyTurn());

            // Test ending player turn
            if (turnManager.isPlayerTurn()) {
                turnManager.endPlayerTurn();
                Gdx.app.log("GameScreen", "Player turn ended, new phase: " + turnManager.getCurrentPhase());
            }
        }
    }

    public void triggerRandomSelection() {
        Gdx.app.log("GameScreen", "Triggering random enemy and card selection...");

        // Random enemy selection
        if (battleStageUI != null) {
            battleStageUI.changeEnemy();
            Gdx.app.log("GameScreen", "Random enemy selected: " + battleStageUI.getCurrentEnemyName());
        }

        // Random card selection (refresh card hand)
        if (cardStageUI != null) {
//            refreshCardHand();
            Gdx.app.log("GameScreen", "Random cards selected");
        }

        // Show a brief message that random selection occurred
        showRandomSelectionMessage();
    }

    private void showRandomSelectionMessage() {
        // Create a temporary label to show random selection message
        Label randomMessage = new Label("Random Selection Complete!", corebringer.testskin);
        randomMessage.setAlignment(Align.center);
        randomMessage.setPosition(
            Gdx.graphics.getWidth() / 2f - 150f,
            Gdx.graphics.getHeight() / 2f + 100f
        );

        // Add to battleStage for display (was editorStage)
        battleStage.addActor(randomMessage);

        // Remove after 2 seconds
        randomMessage.addAction(Actions.sequence(
            Actions.delay(2f),
            Actions.removeActor()
        ));
    }

    // --- End test methods ---

    @Override public void resize(int width, int height) {
        uiStage.getViewport().update(width, height,true);
        battleStage.getViewport().update(width, height, true);
        cardStage.getViewport().update(width, height, true);
    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void hide() {
    }

    @Override
    public void dispose() {
        uiStage.dispose();
        if (battleStageUI != null) {
            battleStageUI.dispose();
        }
        battleStage.dispose();
        // Removed: editorStage.dispose();
        cardStage.dispose();
        // Dispose energy background texture if created
        if (energyBgTexture != null) {
            energyBgTexture.dispose();
            energyBgTexture = null;
        }
        // Clean up victory screen
        if (victoryScreenWindow != null) {
            victoryScreenWindow.remove();
            victoryScreenWindow = null;
        }
        // Removed recursive self-dispose call
    }

    private void showOptionsWindow() {
        if (optionsWindow != null && optionsWindow.isVisible()) return;
        optionsWindow = new Window("Options", corebringer.testskin);
        optionsWindow.setModal(true);
        optionsWindow.setMovable(true);
        optionsWindow.pad(20);
        optionsWindow.setSize(600, 480);
        optionsWindow.setPosition(Gdx.graphics.getWidth() / 2f - 200f, Gdx.graphics.getHeight() / 2f - 200f);
        // Add buttons
        TextButton btnJournal = new TextButton("Journal", corebringer.testskin);
        TextButton btnTitle = new TextButton("Title", corebringer.testskin);
        TextButton btnClose = new TextButton("Close", corebringer.testskin);
        btnJournal.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                float worldWidth = Gdx.graphics.getWidth();
                float worldHeight = Gdx.graphics.getHeight();
                JournalWindow journalWindow = new JournalWindow(corebringer.testskin, worldWidth, worldHeight);
                battleStage.addActor(journalWindow);
                journalWindow.setVisible(true);
                optionsWindow.setVisible(false);
                optionsWindow.remove();
            }
        });
        btnTitle.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                // Fade out current game music, fade in main menu music
                if (corebringer.corebringergamescreenbgm.isPlaying()) {
                    corebringer.fadeOutMusic(corebringer.corebringergamescreenbgm, 1f, () -> {
                        corebringer.fadeInMusic(corebringer.corebringerstartmenubgm, 1f);
                    });
                } else if (corebringer.corebringermapstartbgm.isPlaying()) {
                    corebringer.fadeOutMusic(corebringer.corebringermapstartbgm, 1f, () -> {
                        corebringer.fadeInMusic(corebringer.corebringerstartmenubgm, 1f);
                    });
                } else {
                    corebringer.fadeInMusic(corebringer.corebringerstartmenubgm, 1f);
                }
                corebringer.setScreen(corebringer.mainMenuScreen);
                optionsWindow.setVisible(false);
                optionsWindow.remove();
                // Re-register input processors for the new screen
                if (corebringer.getScreen() != null) corebringer.getScreen().show();
            }
        });
        btnClose.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                optionsWindow.setVisible(false);
                optionsWindow.remove();
                // Re-register input processors for this screen
                show();
            }
        });
        Table table = new Table();
        table.add(btnJournal).row();
        table.add(btnTitle).row();
        table.add(btnClose).row();
        optionsWindow.add(table).expand().fill();
        uiStage.addActor(optionsWindow);
        // No need to reset multiplexer, it's always managed by Main
    }
    public void setEnergy(int value) {
        this.energy = Math.min(value, MAX_ENERGY);
        updateEnergyLabel();
    }
    public int getEnergy() {
        return energy;
    }
    public int getMaxEnergy() {
        return MAX_ENERGY;
    }
    public void addEnergy(int amount) {
        setEnergy(this.energy + amount);
    }
    private void updateEnergyLabel() {
        if (energyLabel != null) {
            energyLabel.setText(energy + "/" + MAX_ENERGY);
        }
    }

    private void showDeathScreen() {
        if (deathScreenWindow != null) return; // Already shown
        deathScreenShown = true;
        gameState = GameState.DEFEAT;
        // Load the death screen texture
        Texture deathTexture = new Texture(Gdx.files.internal("assets/nameplates/death_scrn.png"));
        Image deathImage = new Image(deathTexture);
        deathImage.getColor().a = 0f;
        deathImage.addAction(Actions.fadeIn(1.5f)); // 1.5 seconds fade in
        // Calculate window size (60% width, 90% height) and center
        float windowWidth = Gdx.graphics.getWidth() * 0.6f;
        float windowHeight = Gdx.graphics.getHeight() * 0.9f;
        float windowX = (Gdx.graphics.getWidth() - windowWidth) / 2f;
        float windowY = (Gdx.graphics.getHeight() - windowHeight) / 2f;
        // Create window to overlay everything
        deathScreenWindow = new Window("", corebringer.testskin);
        deathScreenWindow.setModal(true);
        deathScreenWindow.setMovable(false);
        deathScreenWindow.setResizable(false);
        deathScreenWindow.setSize(windowWidth, windowHeight);
        deathScreenWindow.setPosition(windowX, windowY);
        deathScreenWindow.setTouchable(Touchable.enabled);
        deathScreenWindow.setColor(1,1,1,0f);
        deathScreenWindow.addAction(Actions.fadeIn(1.5f));
        // Centered button
        TextButton btnReturn = new TextButton("Return to Title", corebringer.testskin);
        btnReturn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                // Delete save file on death
                com.altf4studios.corebringer.utils.SaveManager.deleteSave();
                // Transfer input ownership to the next screen before disposing
                corebringer.clearInputProcessors();
                // Switch screen; LibGDX will call show() on the next screen automatically
                corebringer.setScreen(corebringer.mainMenuScreen);
                // Optionally reset death screen state
                if (deathScreenWindow != null) {
                    deathScreenWindow.remove();
                    deathScreenWindow = null;
                    deathScreenShown = false;
                }
                // End this GameScreen lifecycle safely: clear stages and dispose next frame
                try { if (battleStage != null) { battleStage.clear(); } } catch (Exception ignored) {}
                try { if (cardStage != null) { cardStage.clear(); } } catch (Exception ignored) {}
                try { if (uiStage != null) { uiStage.clear(); } } catch (Exception ignored) {}
                Gdx.app.postRunnable(() -> GameScreen.this.dispose());
            }
        });
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.add(deathImage).expand().fill().row();
        overlay.add(btnReturn).center().padTop(-300f); // Adjust as needed for button position
        deathScreenWindow.add(overlay).expand().fill();
        // Add to uiStage so it renders on top of other stages
        uiStage.addActor(deathScreenWindow);
        deathScreenWindow.toFront();
        // Block all input except the modal by routing input to uiStage
        Gdx.input.setInputProcessor(uiStage);
    }

    private void showVictoryScreen() {
        if (victoryScreenWindow != null) return; // Already shown
        victoryScreenShown = true;
        gameState = GameState.VICTORY;

        // Calculate window size (60% width, 90% height)
        float windowWidth = Gdx.graphics.getWidth() * 0.6f;
        float windowHeight = Gdx.graphics.getHeight() * 0.9f;
        float windowX = (Gdx.graphics.getWidth() - windowWidth) / 2f;
        float windowY = (Gdx.graphics.getHeight() - windowHeight) / 2f;

        // Create victory window with gray background
        victoryScreenWindow = new Window("", corebringer.testskin);
        victoryScreenWindow.setModal(true);
        victoryScreenWindow.setMovable(false);
        victoryScreenWindow.setResizable(false);
        victoryScreenWindow.setSize(windowWidth, windowHeight);
        victoryScreenWindow.setPosition(windowX, windowY);
        victoryScreenWindow.setTouchable(Touchable.enabled);

        // Set gray background color
        victoryScreenWindow.setColor(0.5f, 0.5f, 0.5f, 1f);

        // Create content table
        Table contentTable = new Table();
        contentTable.setFillParent(true);

        // Victory message
        Label victoryMessage = new Label("You win!", corebringer.testskin);
        victoryMessage.setAlignment(Align.center);
        victoryMessage.setFontScale(2.0f); // Make it larger

        // Gold reward (random 50-100)
        final int goldReward = MathUtils.random(50, 100);
        Label goldGainedLabel = new Label("Gold Gained: " + goldReward, corebringer.testskin);
        goldGainedLabel.setAlignment(Align.center);
        Label totalGoldPreview = new Label("Total Gold After: " + (this.gold + goldReward), corebringer.testskin);
        totalGoldPreview.setAlignment(Align.center);

        // Proceed button
        TextButton btnProceed = new TextButton("Proceed", corebringer.testskin);
        btnProceed.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                // Apply gold reward
                GameScreen.this.gold += goldReward;
                if (goldLabel != null) {
                    goldLabel.setText("Gold: " + GameScreen.this.gold);
                }
                // Save progress with battle won = 1
                saveProgress(1);
                // Hide victory screen
                if (victoryScreenWindow != null) {
                    victoryScreenWindow.remove();
                    victoryScreenWindow = null;
                    victoryScreenShown = false;
                }
                // Return to game map and end this GameScreen's lifecycle
                corebringer.clearInputProcessors();
                // Switch to map; LibGDX will call show() on map screen
                corebringer.setScreen(corebringer.gameMapScreen);
                // Clear stages to cancel actions and detach actors before disposing
                try { if (battleStage != null) { battleStage.clear(); } } catch (Exception ignored) {}
                try { if (cardStage != null) { cardStage.clear(); } } catch (Exception ignored) {}
                try { if (uiStage != null) { uiStage.clear(); } } catch (Exception ignored) {}
                // Dispose on next frame to ensure no native resources are still in use this tick
                Gdx.app.postRunnable(() -> GameScreen.this.dispose());
            }
        });

        // Add content to table
        contentTable.add(victoryMessage).expand().center().row();
        contentTable.add(goldGainedLabel).center().padTop(20f).row();
        contentTable.add(totalGoldPreview).center().padTop(10f).row();
        contentTable.add(btnProceed).center().padTop(50f);

        victoryScreenWindow.add(contentTable).expand().fill();
        // Add to uiStage so it renders on top of other stages
        uiStage.addActor(victoryScreenWindow);
        victoryScreenWindow.toFront();

        // Block all input except the modal by routing input to uiStage
        Gdx.input.setInputProcessor(uiStage);
    }

    /**
     * Rerolls the enemy and cards for a new game session.
     * Loads enemy name and hp from enemies.json and updates Enemy and UI.
     */
    public void rerollEnemyAndCards() {
        try {
            // Load enemies.json
            com.badlogic.gdx.files.FileHandle file = Gdx.files.internal("assets/enemies.json");
            String json = file.readString();
            com.badlogic.gdx.utils.JsonReader jsonReader = new com.badlogic.gdx.utils.JsonReader();
            com.badlogic.gdx.utils.JsonValue enemies = jsonReader.parse(json);
            if (enemies != null && enemies.size > 0) {
                int idx = com.badlogic.gdx.math.MathUtils.random(enemies.size - 1);
                com.badlogic.gdx.utils.JsonValue enemyData = enemies.get(idx);
                String name = enemyData.getString("name");
                int hp = enemyData.getInt("hp");
                // Update Enemy object
                if (enemy != null) {
                    enemy.setName(name);
                    // Ensure enemy max health is updated so setHp isn't clamped to an old max
                    enemy.setMaxHealth(hp);
                    enemy.setHp(hp);
                }
                // Update UI
                if (battleStageUI != null) {
                    battleStageUI.changeEnemy(name);
                    // Update UI HP bars using current player/enemy values
                    battleStageUI.updateHpBars(player.getHp(), enemy.getHp());
                    battleStageUI.setEnemyHp(hp);
                }
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to reroll enemy from JSON: " + e.getMessage());
        }
        // Reroll cards
//        if (cardStageUI != null) {
//            cardStageUI.refreshCardHand();
//        }
        // Reset turn system to player's turn and clear pending state
        if (battleManager != null) {
            battleManager.resetTurns();
            Gdx.app.log("GameScreen", "Turn system reset after reroll. Player HP: " + player.getHp() + ", Enemy HP: " + enemy.getHp());
        }
    }
}
