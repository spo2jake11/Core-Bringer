package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
// Removed interpreter integration
import com.altf4studios.corebringer.metrics.CodingMetricsManager;
import com.altf4studios.corebringer.utils.CardParser;
import com.altf4studios.corebringer.turns.TurnManager;
import com.altf4studios.corebringer.battle.BattleManager;
import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.entities.Enemy;
import com.altf4studios.corebringer.screens.gamescreen.*;
import com.altf4studios.corebringer.metrics.MetricsDisplayWindow;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.altf4studios.corebringer.utils.SimpleSaveManager;
import com.altf4studios.corebringer.utils.SaveData;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public class GameScreen implements Screen{
    /// Declaration of variables and elements here.
    private Main corebringer;

    private Stage uiStage;
    private Stage battleStage;
    private Stage cardStage;
    private Stage overlayStage;

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
    private int MAX_ENERGY = 3;
    private Label energyLabel;
    private Stack energyWidget; // label + background
    private Texture energyBgTexture;
    private Window optionsWindow;
    private Window deckWindow;
    private MetricsDisplayWindow metricsDisplayWindow;
    // Persisted deck ids for this run
    private String[] savedDeckIds;
    // --- End Energy System ---
    // --- Currency ---
    private int gold = 0;
    // Transition guard to avoid GL calls during screen switch
    private boolean transitioning = false;
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
    private boolean showingMetricsInDeath = false;
    // --- End Death Screen ---

    // --- Victory Screen ---
    private Window victoryScreenWindow = null;
    private boolean victoryScreenShown = false;
    private boolean showingMetricsInVictory = false;
    // --- End Victory Screen ---

    // Death/Victory screen textures owned by this screen
    private Texture deathScreenTexture = null;

    // --- Game Lifecycle State ---
    private enum GameState { RUNNING, VICTORY, DEFEAT }
    private GameState gameState = GameState.RUNNING;
    // --- End Game Lifecycle State ---

    // Guard against double-dispose and post-dispose rendering
    private boolean isDisposed = false;

    // --- One-turn Card Effect Buff ---
    // Multiplier applied to card effects for the current player turn. Defaults to 1.0f.
    private float cardEffectMultiplier = 1.0f;

    // --- Perf HUD ---
    private GLProfiler glProfiler;
    private Label perfLabel;
    private float perfTimer = 0f;
    private boolean perfHudEnabled = true; // toggle with F3

    // --- Instakill Victory Flow ---
    // When an instakill happens from CodeEditorScreen, delay victory popup by a short duration and tag it.
    private boolean instakillFlowActive = false;
    private float instakillDelay = 0f;
    private String instakillTag = null; // e.g., "Instakill!!"
    // Start the delayed victory flow (called by CodeEditorScreen)
    public void startInstakillVictory(float delaySeconds, String label) {
        this.instakillFlowActive = true;
        this.instakillDelay = Math.max(0f, delaySeconds);
        this.instakillTag = label;
    }

    // --- Card effect multiplier API ---
    public void activateOneTurnBuff(float multiplier) {
        cardEffectMultiplier = Math.max(1.0f, multiplier);
        showCenterMessage("Overhack Buff: x" + String.format("%.2f", cardEffectMultiplier) + " this turn", Color.GREEN, 2.0f);
    }
    public float getCardEffectMultiplier() {
        return cardEffectMultiplier;
    }
    public void clearCardEffectMultiplier() {
        cardEffectMultiplier = 1.0f;
    }
    // --- End Instakill Victory Flow ---


    // Whether this battle should use only boss enemies
    private boolean bossOnlyBattle = false;

    public GameScreen(Main corebringer) {
        this(corebringer, false);
    }

    public GameScreen(Main corebringer, boolean bossOnly) {
        // Load stageLevel from save file if available
        int stageFromSave = 1;
        try {
            com.altf4studios.corebringer.utils.SaveData sd = SimpleSaveManager.loadData();
            if (sd != null && sd.stageLevel > 0) stageFromSave = sd.stageLevel;
        } catch (Exception ignored) {}
        this.corebringer = corebringer;
        this.bossOnlyBattle = bossOnly;

        // Initialize CardParser
        cardParser = CardParser.getInstance();
        // Initialize CardDataManager maps from parser data for O(1) lookups
        com.altf4studios.corebringer.utils.CardDataManager.getInstance().initFrom(cardParser);

        ///This stages are separated to lessen complications
        uiStage = new Stage(new ScreenViewport());
        battleStage = new Stage(new ScreenViewport());
        cardStage = new Stage(new ScreenViewport());
        overlayStage = new Stage(new ScreenViewport());


        // --- Load stats from save file (must exist at this point) ---
        int hp = 20; // Legacy fallback current HP
        int maxHpFromSave = 20; // Default fallback max HP
        int energyVal = 0;
        String[] cards = new String[]{
            "basic_variable_slash_1", "basic_variable_slash_1", "basic_variable_slash_1",
            "basic_variable_slash_1", "basic_variable_slash_1", "shield_final_shield_1",
            "shield_final_shield_1", "shield_final_shield_1", "shield_final_shield_1",
            "shield_final_shield_1", "heal_ultimate_heal_1", "heal_ultimate_heal_1",
            "heal_ultimate_heal_1"
        };
        int battleWon = 0;
        int goldFromSave = 0;
        String enemyName = "Enemy";
        int enemyHp = 20;

        // Load from save file (should exist from MainMenuScreen)
        if (SimpleSaveManager.saveExists()) {
            com.altf4studios.corebringer.utils.SaveData stats = SimpleSaveManager.loadData();
            if (stats != null) {
                // Prefer new fields; fallback to legacy hp if needed
                hp = (stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : hp));
                maxHpFromSave = (stats.maxHp > 0 ? stats.maxHp : Math.max(hp, maxHpFromSave));
                energyVal = stats.energy;
                // Apply max energy from save with fallback to 3
                MAX_ENERGY = (stats.maxEnergy > 0 ? stats.maxEnergy : 3);
                cards = stats.cards;
                battleWon = stats.battleWon;
                goldFromSave = stats.gold;
            }
        }

        // Load enemy from new JSON format: level -> common/boss pools
        try {
            com.badlogic.gdx.files.FileHandle file = Gdx.files.internal("assets/enemies.json");
            String json = file.readString();
            com.badlogic.gdx.utils.JsonReader jsonReader = new com.badlogic.gdx.utils.JsonReader();
            com.badlogic.gdx.utils.JsonValue root = jsonReader.parse(json);
            // For now, pick from first level; can be extended to use current map level
            com.badlogic.gdx.utils.JsonValue levels = root.get("levels");
            if (levels != null && levels.size > 0) {
                com.badlogic.gdx.utils.JsonValue level = levels.get(0);
                com.badlogic.gdx.utils.JsonValue pool = bossOnlyBattle ? level.get("boss") : level.get("common");
                if (pool != null && pool.size > 0) {
                    int idx = com.badlogic.gdx.math.MathUtils.random(pool.size - 1);
                    com.badlogic.gdx.utils.JsonValue enemyData = pool.get(idx);
                    enemyName = enemyData.getString("name");
                    enemyHp = enemyData.getInt("hp");
                }
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
        battleStageUI = new BattleStageUI(battleStage, corebringer.testskin, corebringer.getAssets());
        // Apply stage-specific background (clamped 1..5)
        int bgStage = Math.max(1, Math.min(5, stageFromSave));
        battleStageUI.setBackgroundStage(bgStage);
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
            SimpleSaveManager.saveStats(player.getHp(), player.getMaxHealth(), energyVal, getMaxEnergy(), cards, battleWon, gold);
        }

        // Set energy to full at start of battle (don't use saved energy value)
        setEnergy(MAX_ENERGY);
        // Create energy label with background icon (text like 0/3)
        energyLabel = new Label(energy + "/" + MAX_ENERGY, corebringer.testskin);
        energyLabel.setAlignment(Align.center);
        // Background image from assets/icons/energy.png (via AssetManager)
        try {
            AssetManager assets = corebringer.getAssets();
            String energyPath = "assets/icons/energy_icon.png";
            if (assets != null) {
                if (!assets.isLoaded(energyPath, Texture.class)) {
                    assets.load(energyPath, Texture.class);
                    assets.finishLoadingAsset(energyPath);
                }
                energyBgTexture = assets.get(energyPath, Texture.class);
            } else {
                energyBgTexture = new Texture(Gdx.files.internal(energyPath));
            }
            Image bg = new Image(energyBgTexture);
            // Make energy widget responsive to screen size
            float screenWidth = Gdx.graphics.getWidth();
            float screenHeight = Gdx.graphics.getHeight();
            float energySize = Math.min(screenWidth * 0.08f, screenHeight * 0.12f); // 8% of width or 12% of height, whichever is smaller
            bg.setSize(energySize, energySize);
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

    public com.altf4studios.corebringer.screens.gamescreen.CardStageUI getCardStageUI() {
        return cardStageUI;
    }

    public Player getPlayer() {
        return player;
    }

    // Call this after any stat change (hp, energy, cards, battleWon)
    public void saveProgress(int battleWonValue) {
        String[] deck = savedDeckIds != null ? savedDeckIds : new String[]{};
        SimpleSaveManager.saveStats(player.getHp(), player.getMaxHealth(), energy, getMaxEnergy(), deck, battleWonValue, gold);
    }

    private void createUI() {
        // Main table that fills the parent
        Table tbltopUI = new Table();
        tbltopUI.setFillParent(true);
        tbltopUI.top();

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
        TextButton btnMenu = new TextButton("Menu", corebringer.testskin);
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
        tblRightPane.add(btnMenu);

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
        btnMenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showOptionsWindow();
            }
        });
    }

    private void ensureDeckResourcesLoaded() {
        if (deckCardAtlas == null) {
            AssetManager assets = corebringer.getAssets();
            String cardsAtlasPath = "assets/cards/cards_atlas.atlas";
            if (assets != null) {
                if (!assets.isLoaded(cardsAtlasPath, TextureAtlas.class)) {
                    assets.load(cardsAtlasPath, TextureAtlas.class);
                    assets.finishLoadingAsset(cardsAtlasPath);
                }
                deckCardAtlas = assets.get(cardsAtlasPath, TextureAtlas.class);
            } else {
                deckCardAtlas = new TextureAtlas(cardsAtlasPath);
            }
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

        float windowWidth = Math.min(Gdx.graphics.getWidth() * 0.8f, 1200f);   // 80% of screen or max 1200px
        float windowHeight = Math.min(Gdx.graphics.getHeight() * 0.7f, 800f);
        float windowX = (Gdx.graphics.getWidth() - windowWidth) / 2f;
        float windowY = (Gdx.graphics.getHeight() - windowHeight) / 2f;

        deckWindow = new Window("Deck", corebringer.testskin);
        deckWindow.setModal(true);
        deckWindow.setMovable(true);
        deckWindow.setResizable(false);
        // Make windows responsive
        // 70% of screen or max 800px
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
            // Make card size responsive
            float screenWidth = Gdx.graphics.getWidth();
            float cardWidth = screenWidth * 0.12f;  // 12% of screen width
            float cardHeight = cardWidth * 1.27f;   // Maintain aspect ratio (190/150 = 1.27)
            img.setSize(cardWidth, cardHeight);
            grid.add(img).size(cardWidth, cardHeight);
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
        corebringer.playMusic("battle");

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
        corebringer.addInputProcessor(overlayStage);
        // Ensure the global multiplexer is the active input processor
        Gdx.input.setInputProcessor(corebringer.getGlobalMultiplexer());
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
                if (keycode == Input.Keys.F3) {
                    perfHudEnabled = !perfHudEnabled;
                    if (perfLabel != null) perfLabel.setVisible(perfHudEnabled);
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
        overlayStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        // Perf HUD label (top-left)
        try {
            if (perfLabel == null) {
                perfLabel = new Label("", corebringer.testskin);
                perfLabel.setColor(Color.LIGHT_GRAY);
                perfLabel.setAlignment(Align.topLeft);
                perfLabel.setPosition(10, Gdx.graphics.getHeight() - 10);
                uiStage.addActor(perfLabel);
                perfLabel.setVisible(perfHudEnabled);
            }
        } catch (Exception ignored) {}

        // Enable profiler when screen shows
        if (glProfiler == null) {
            glProfiler = new GLProfiler(Gdx.graphics);
        }
        glProfiler.enable();
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
                corebringer.showCodeEditor();
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
        if (isDisposed || transitioning) return; // Prevent rendering during transition or after dispose
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
        overlayStage.act(delta);
        overlayStage.draw();
        // Removed: editorStage.act(delta); editorStage.draw();

        // Perf HUD update (once per second)
        perfTimer += delta;
        if (perfHudEnabled && perfLabel != null && glProfiler != null && perfTimer >= 1f) {
            int fps = Gdx.graphics.getFramesPerSecond();
            long javaHeap = Gdx.app.getJavaHeap() / (1024 * 1024);
            long nativeHeap = Gdx.app.getNativeHeap() / (1024 * 1024);
            int draws = glProfiler.getDrawCalls();
            int texBinds = glProfiler.getTextureBindings();
            int shaders = glProfiler.getShaderSwitches();
            perfLabel.setText("FPS:" + fps +
                    "  Heap(M):" + javaHeap + "/" + nativeHeap +
                    "  Draws:" + draws +
                    "  Tex:" + texBinds +
                    "  Shaders:" + shaders);
            // Reset counters for next second
            glProfiler.reset();
            perfTimer = 0f;
        }

        // --- BattleManager: process turn phases, enemy AI, and UI indicator ---
        if (gameState == GameState.RUNNING) {
            battleManager.update(delta);
        }
        // --- End BattleManager ---

        // --- Energy auto-regen at start of player's actionable turn ---
        // Regen once when player's turn becomes active (not during delay/poison resolution)
//        if (turnManager != null) {
//            if (turnManager.isPlayerTurn() && !playerTurnEnergyApplied) {
//                setEnergy(MAX_ENERGY);
//                playerTurnEnergyApplied = true;
//                Gdx.app.log("GameScreen", "Energy reset to " + MAX_ENERGY + " at start of player turn");
//            } else if (turnManager.isEnemyTurn()) {
//                // Reset flag so next player turn will regen again
//                playerTurnEnergyApplied = false;
//                // Do not auto-clear buff here; CardStageUI will clear it when End Turn is pressed
//            }
//        }

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
            if (instakillFlowActive) {
                // Defer showing victory until delay elapses
                instakillDelay -= delta;
                if (instakillDelay <= 0f) {
                    showVictoryScreen();
                    // Reset instakill flow
                    instakillFlowActive = false;
                    instakillDelay = 0f;
                }
            } else {
                showVictoryScreen();
            }
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

    // Show a temporary centered message overlay on the battle stage
    public void showCenterMessage(String text, Color color, float durationSeconds) {
        try {
            Label msg = new Label(text, corebringer.testskin);
            msg.setAlignment(Align.center);
            if (color != null) msg.setColor(color);
            msg.setPosition(
                Gdx.graphics.getWidth() / 2f - 200f,
                Gdx.graphics.getHeight() / 2f + 100f
            );
            battleStage.addActor(msg);
            msg.addAction(Actions.sequence(
                Actions.delay(Math.max(0.1f, durationSeconds)),
                Actions.removeActor()
            ));
        } catch (Exception ignored) {}
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

    /** Debug helper: log actor counts across stages to detect buildup */
    public void logStageActorCounts(String label) {
        int battleCount = (battleStage != null) ? battleStage.getActors().size : -1;
        int cardCount = (cardStage != null) ? cardStage.getActors().size : -1;
        int uiCount = (uiStage != null) ? uiStage.getActors().size : -1;
        Gdx.app.log("Actors", label +
                " | battleStage=" + battleCount +
                " cardStage=" + cardCount +
                " uiStage=" + uiCount);
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

    @Override public void hide() {
        // OPTIMIZATION: Clear stages and cancel actions when screen is hidden
        try {
            if (battleStage != null) {
                battleStage.getRoot().clearActions();
                // Don't clear actors yet - they may be needed when returning
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error in hide() clearing battleStage: " + e.getMessage());
        }

        try {
            if (cardStage != null) {
                cardStage.getRoot().clearActions();
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error in hide() clearing cardStage: " + e.getMessage());
        }

        try {
            if (uiStage != null) {
                uiStage.getRoot().clearActions();
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error in hide() clearing uiStage: " + e.getMessage());
        }
    }
    @Override public void resize(int width, int height) {
        uiStage.getViewport().update(width, height,true);
        battleStage.getViewport().update(width, height, true);
        cardStage.getViewport().update(width, height, true);
        overlayStage.getViewport().update(width, height, true);
    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }

    @Override
    public void dispose() {
        if (isDisposed) return;
        isDisposed = true;

        // Disable profiler
        try { if (glProfiler != null) glProfiler.disable(); } catch (Exception ignored) {}

        // Stop ongoing actions to minimize in-flight draws
        try { if (battleStage != null) battleStage.getRoot().clearActions(); } catch (Exception ignored) {}
        try { if (cardStage != null) cardStage.getRoot().clearActions(); } catch (Exception ignored) {}
        try { if (uiStage != null) uiStage.getRoot().clearActions(); } catch (Exception ignored) {}
        try { if (overlayStage != null) overlayStage.getRoot().clearActions(); } catch (Exception ignored) {}


        // Dispose stages first to stop any rendering using their batches/meshes
        try { if (battleStage != null) battleStage.dispose(); } catch (Exception ignored) {}
        try { if (cardStage != null) cardStage.dispose(); } catch (Exception ignored) {}
        try { if (uiStage != null) uiStage.dispose(); } catch (Exception ignored) {}
        try { if (overlayStage != null) overlayStage.dispose(); } catch (Exception ignored) {}


        // Then dispose UI helpers/atlases/textures owned by the screen
        if (battleStageUI != null) {
            try { battleStageUI.dispose(); } catch (Exception ignored) {}
        }
        // If using AssetManager, unload screen-specific assets
        try {
            AssetManager assets = corebringer.getAssets();
            if (assets != null) {
                String energyPath = "assets/icons/energy_icon.png";
                String cardsAtlasPath = "assets/cards/cards_atlas.atlas";
                String battleBgAtlas = "assets/backgrounds/backgrounds_atlas.atlas";
                String enemyAtlasPath = "basic-characters/normal_mob/normal_mobs.atlas";
                if (assets.isLoaded(energyPath)) assets.unload(energyPath);
                if (assets.isLoaded(cardsAtlasPath)) assets.unload(cardsAtlasPath);
                if (assets.isLoaded(battleBgAtlas)) assets.unload(battleBgAtlas);
                if (assets.isLoaded(enemyAtlasPath)) assets.unload(enemyAtlasPath);
            } else {
                // Fallback if not using AssetManager: dispose directly
                try { if (deckCardAtlas != null) { deckCardAtlas.dispose(); } } catch (Exception ignored) {}
                try { if (energyBgTexture != null) { energyBgTexture.dispose(); } } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        deckCardAtlas = null;
        energyBgTexture = null;
        // Dispose death screen texture if created
        if (deathScreenTexture != null) {
            try { deathScreenTexture.dispose(); } catch (Exception ignored) {}
            deathScreenTexture = null;
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
        optionsWindow = new Window("", corebringer.testskin);
        optionsWindow.setModal(false);
        optionsWindow.setMovable(false);
        optionsWindow.pad(20);
        // Make windows responsive
        float windowWidth = Math.min(Gdx.graphics.getWidth() * 0.4f, 600);   // 80% of screen or max 1200px
        float windowHeight = Math.min(Gdx.graphics.getHeight() * 0.4f, 600f);  // 70% of screen or max 800px
        optionsWindow.setSize(windowWidth, windowHeight);
        // Center the options window properly with new responsive size
        optionsWindow.setPosition(
            (Gdx.graphics.getWidth() - windowWidth) / 2f,
            (Gdx.graphics.getHeight() - windowHeight) / 2f
        );
        // Add buttons
        TextButton btnJournal = new TextButton("Journal", corebringer.testskin);
        TextButton btnTitle = new TextButton("Give up", corebringer.testskin);
        TextButton btnClose = new TextButton("Close", corebringer.testskin);
        btnJournal.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                float worldWidth = Gdx.graphics.getWidth();
                float worldHeight = Gdx.graphics.getHeight();
                JournalWindow journalWindow = new JournalWindow(corebringer.testskin, worldWidth, worldHeight);
                overlayStage.addActor(journalWindow);
                journalWindow.setVisible(true);
                journalWindow.toFront();
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
                // Delete current run save file
                SimpleSaveManager.deleteSave();
                // Dispose and clear the existing GameMapScreen so a new run starts fresh
                try {
                    if (corebringer.gameMapScreen != null) {
                        corebringer.gameMapScreen.dispose();
                        corebringer.gameMapScreen = null;
                    }
                    Gdx.app.postRunnable(GameScreen.this::dispose);
                } catch (Exception ignored) {}
                corebringer.showMainMenu();
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
        table.defaults().pad(7);
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
        deathScreenTexture = new Texture(Gdx.files.internal("assets/nameplates/death_scrn.png"));
        Image deathImage = new Image(deathScreenTexture);
        deathImage.getColor().a = 0f;
        deathImage.addAction(Actions.fadeIn(1.5f)); // 1.5 seconds fade in
        // Calculate window size (60% width, 90% height) and center
        // Make windows responsive
        float windowWidth = Math.min(Gdx.graphics.getWidth() * 0.8f, 1200f);   // 80% of screen or max 1200px
        float windowHeight = Math.min(Gdx.graphics.getHeight() * 0.7f, 800f);  // 70% of screen or max 800px

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
        TextButton btnCheckStats = new TextButton("Check Your Stats", corebringer.testskin);
        btnCheckStats.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                // Hide death screen and show metrics
                if (deathScreenWindow != null) {
                    deathScreenWindow.remove();
                    deathScreenWindow = null;
                }

                // Show metrics
                showMetricsAfterDeath();
            }
        });
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.add(deathImage).expand().fill().row();
        overlay.add(btnCheckStats).center().padTop(-300f); // Adjust as needed for button position
        deathScreenWindow.add(overlay).expand().fill();
        // Add to uiStage so it renders on top of other stages
        uiStage.addActor(deathScreenWindow);
        deathScreenWindow.toFront();
        // Block all input except the modal by routing input to uiStage
        Gdx.input.setInputProcessor(uiStage);
        // Clear the tag so subsequent victories are clean
        instakillTag = null;
    }

    private void showMetricsAfterDeath() {
        if (showingMetricsInDeath) return;
        showingMetricsInDeath = true;

        showInlineMetricsWindow(() -> {
            // Hide metrics and reset flag
            showingMetricsInDeath = false;
            // Delete save file on death
            SimpleSaveManager.deleteSave();
            // Switch screen
            corebringer.showMainMenu();
            // Transfer input ownership to the next screen before disposing
            corebringer.clearInputProcessors();
            // Clear the tag so subsequent games are clean
            instakillTag = null;
            // Dispose all screens except MainMenuScreen
            corebringer.disposeAllScreensExceptMainMenu();
        });
    }


    private void showVictoryScreen() {
        if (victoryScreenWindow != null) return; // Already shown
        victoryScreenShown = true;
        gameState = GameState.VICTORY;

        // Check if this is the final stage (stage 5) - if so, show completion screen with metrics
        int currentStage = 1;
        try {
            com.altf4studios.corebringer.utils.SaveData saveData = SimpleSaveManager.loadData();
            if (saveData != null && saveData.stageLevel > 0) {
                currentStage = saveData.stageLevel;
            }
        } catch (Exception ex) {
            Gdx.app.log("GameScreen", "Could not load stageLevel for victory screen, defaulting to 1: " + ex.getMessage());
        }

        if (currentStage >= 5) {
            // This is the final stage - show completion screen with metrics
            showGameCompletionScreen();
            return;
        }

        // Calculate window size (60% width, 90% height)
        float windowWidth = Math.min(Gdx.graphics.getWidth() * 0.5f, 800f);   // 80% of screen or max 1200px
        float windowHeight = Math.min(Gdx.graphics.getHeight() * 0.5f, 800f);
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

        // Optional instakill tag label
        Label instakillLabel = null;
        if (instakillTag != null && !instakillTag.isEmpty()) {
            instakillLabel = new Label(instakillTag, corebringer.testskin);
            instakillLabel.setAlignment(Align.center);
            instakillLabel.setColor(Color.RED);
            instakillLabel.setFontScale(1.5f);
        }

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
                transitioning = true;
                corebringer.clearInputProcessors();
                // If boss battle was won, dispose current map and recreate with next stage
                if (bossOnlyBattle) {
                    int currentStage = 1;
                    try {
                        com.altf4studios.corebringer.utils.SaveData s = SimpleSaveManager.loadData();
                        if (s != null && s.stageLevel > 0) currentStage = s.stageLevel;
                    } catch (Exception ignored) {}
                    int nextStage = Math.max(1, Math.min(5, currentStage + 1));
                    // Disable profiler before transition
                    try { if (glProfiler != null) glProfiler.disable(); } catch (Exception ignored) {}
                    // Dispose existing map (if any)
                    try { if (corebringer.gameMapScreen != null) corebringer.gameMapScreen.dispose(); } catch (Exception ignored) {}
                    // Persist the new stage level along with current stats
                    try {
                        String[] deck = savedDeckIds != null ? savedDeckIds : new String[]{};
                        SimpleSaveManager.saveStats(player.getHp(), player.getMaxHealth(), energy, getMaxEnergy(), deck, 0, gold, nextStage);
                    } catch (Exception ignored) {}
                    // Ensure a fresh map screen exists and switch to it
                    corebringer.gameMapScreen = new GameMapScreen(corebringer);
                    corebringer.setScreen(corebringer.gameMapScreen);
                } else {
                    // Non-boss: maintain current map and advance rank
                    if (corebringer.gameMapScreen != null) {
                        try { corebringer.gameMapScreen.advanceToNextRank(); } catch (Exception ignored) {}
                    } else {
                        corebringer.gameMapScreen = new GameMapScreen(corebringer);
                    }
                    // Disable profiler before transition
                    try { if (glProfiler != null) glProfiler.disable(); } catch (Exception ignored) {}
                    corebringer.setScreen(corebringer.gameMapScreen);
                }
                // Clear stages to cancel actions and detach actors before disposing
                try { if (battleStage != null) { battleStage.clear(); } } catch (Exception ignored) {}
                try { if (cardStage != null) { cardStage.clear(); } } catch (Exception ignored) {}
                try { if (uiStage != null) { uiStage.clear(); } } catch (Exception ignored) {}
                // Dispose on next frame to ensure no native resources are still in use this tick
                Gdx.app.postRunnable(GameScreen.this::dispose);
            }
        });

        // Add content to table
        if (instakillLabel != null) {
            contentTable.add(instakillLabel).expand().center().padBottom(10f).row();
        }
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

    private void showGameCompletionScreen() {
        if (showingMetricsInVictory) return;
        showingMetricsInVictory = true;

        // Create completion window
        float windowWidth = Math.min(Gdx.graphics.getWidth() * 0.8f, 1200f);
        float windowHeight = Math.min(Gdx.graphics.getHeight() * 0.7f, 800f);
        float windowX = (Gdx.graphics.getWidth() - windowWidth) / 2f;
        float windowY = (Gdx.graphics.getHeight() - windowHeight) / 2f;

        // Create completion window
        victoryScreenWindow = new Window("", corebringer.testskin);
        victoryScreenWindow.setModal(true);
        victoryScreenWindow.setMovable(false);
        victoryScreenWindow.setResizable(false);
        victoryScreenWindow.setSize(windowWidth, windowHeight);
        victoryScreenWindow.setPosition(windowX, windowY);
        victoryScreenWindow.setTouchable(Touchable.enabled);
        victoryScreenWindow.setColor(0.2f, 0.8f, 0.2f, 0.9f); // Green background for completion

        // Create content table
        Table contentTable = new Table();
        contentTable.setFillParent(true);
        contentTable.pad(20);

        // Completion message
        Label completionMessage = new Label("🎉 CONGRATULATIONS! 🎉", corebringer.testskin);
        completionMessage.setAlignment(Align.center);
        completionMessage.setFontScale(2.5f);
        completionMessage.setColor(Color.YELLOW);

        Label completionSubMessage = new Label("You have completed Core Bringer!", corebringer.testskin);
        completionSubMessage.setAlignment(Align.center);
        completionSubMessage.setFontScale(1.5f);
        completionSubMessage.setColor(Color.WHITE);

        Label completionSubMessage2 = new Label("You have defeated the final boss!", corebringer.testskin);
        completionSubMessage2.setAlignment(Align.center);
        completionSubMessage2.setFontScale(1.2f);
        completionSubMessage2.setColor(Color.LIGHT_GRAY);

        // Check final stats button
        TextButton btnCheckFinalStats = new TextButton("Check Your Final Stats", corebringer.testskin);
        btnCheckFinalStats.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Hide completion screen and show metrics
                if (victoryScreenWindow != null) {
                    victoryScreenWindow.remove();
                    victoryScreenWindow = null;
                    victoryScreenShown = false;
                }

                // Show metrics
                showMetricsAfterVictory();
            }
        });

        // Layout content
        contentTable.add(completionMessage).expandX().fillX().padBottom(20).row();
        contentTable.add(completionSubMessage).expandX().fillX().padBottom(10).row();
        contentTable.add(completionSubMessage2).expandX().fillX().padBottom(30).row();
        contentTable.add(btnCheckFinalStats).expandX().fillX().height(60).row();

        victoryScreenWindow.add(contentTable).expand().fill();

        // Add to uiStage so it renders on top of other stages
        uiStage.addActor(victoryScreenWindow);
        victoryScreenWindow.toFront();
        // Block all input except the modal by routing input to uiStage
        Gdx.input.setInputProcessor(uiStage);
        // Clear the tag so subsequent games are clean
        instakillTag = null;
    }

    private void showMetricsAfterVictory() {
        if (showingMetricsInVictory) return;
        showingMetricsInVictory = true;

        showInlineMetricsWindow(() -> {
            // Hide metrics and reset flag
            showingMetricsInVictory = false;
            // Delete save file on completion
            SimpleSaveManager.deleteSave();
            // Transfer input ownership to the next screen before disposing
            corebringer.clearInputProcessors();
            // Dispose all screens except MainMenuScreen
            corebringer.disposeAllScreensExceptMainMenu();
            // Switch screen
            corebringer.showMainMenu();
            // Clear the tag so subsequent games are clean
            instakillTag = null;
        });
    }

    // Build and show a self-contained metrics window on uiStage
    private void showInlineMetricsWindow(Runnable onReturn) {
        // Ensure UI viewport is current (do not clear battleStage to keep death/victory window flow intact)
        uiStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        float windowWidth = Math.min(Gdx.graphics.getWidth() * 0.8f, 1200f);
        float windowHeight = Math.min(Gdx.graphics.getHeight() * 0.8f, 800f);
        float windowX = (Gdx.graphics.getWidth() - windowWidth) / 2f;
        float windowY = (Gdx.graphics.getHeight() - windowHeight) / 2f;

        Window metricsWin = new Window("Coding Performance Analytics", corebringer.testskin);
        metricsWin.setModal(true);
        metricsWin.setMovable(false);
        metricsWin.setResizable(false);
        metricsWin.setTouchable(Touchable.enabled);
        metricsWin.setSize(windowWidth, windowHeight);
        metricsWin.setPosition(windowX, windowY);

        Table root = new Table();
        root.setFillParent(true);
        root.pad(10);

        // Fetch metrics data
        CodingMetricsManager metrics = CodingMetricsManager.getInstance();

        // Overall section
        Label hdrOverall = new Label("\uD83D\uDCCA Overall Performance", corebringer.testskin);
        hdrOverall.setAlignment(Align.left);
        root.add(hdrOverall).expandX().fillX().padBottom(8).row();

        Table overallTbl = new Table();
        int totalAttempted = metrics.getTotalQuestionsAttempted();
        int totalCorrect = metrics.getTotalQuestionsCorrect();
        int totalWrong = Math.max(0, totalAttempted - totalCorrect);
        float accuracy = totalAttempted > 0 ? (totalCorrect * 100f / totalAttempted) : 0f;
        overallTbl.add(new Label("Total Coding Questions: " + totalAttempted, corebringer.testskin)).left().row();
        Label corr = new Label("✅ Correct: " + totalCorrect, corebringer.testskin);
        corr.setColor(0,1,0,1);
        overallTbl.add(corr).left().row();
        Label wrong = new Label("❌ Wrong: " + totalWrong, corebringer.testskin);
        wrong.setColor(1,0,0,1);
        overallTbl.add(wrong).left().row();
        overallTbl.add(new Label(String.format("Overall Accuracy: %.1f%%", accuracy), corebringer.testskin)).left().row();
        root.add(overallTbl).expandX().fillX().padBottom(12).row();

        // Level breakdown (condensed)
        Label hdrLevels = new Label("\uD83D\uDCC8 Level-by-Level Analysis", corebringer.testskin);
        hdrLevels.setAlignment(Align.left);
        root.add(hdrLevels).expandX().fillX().padBottom(8).row();

        Table lvlTbl = new Table();
        com.badlogic.gdx.utils.Array<CodingMetricsManager.LevelMetrics> all = metrics.getAllLevelMetrics();
        if (all.size == 0) {
            lvlTbl.add(new Label("No coding attempts recorded yet.", corebringer.testskin)).left().row();
        } else {
            for (CodingMetricsManager.LevelMetrics lm : all) {
                if (lm.questionsAttempted <= 0) continue;
                lvlTbl.add(new Label(String.format("Level %d - %s", lm.level, lm.levelName), corebringer.testskin)).left().row();
                lvlTbl.add(new Label(String.format("  ✅ %d  |  ❌ %d  |  Acc: %.1f%%",
                        lm.questionsCorrect, lm.questionsIncorrect, lm.accuracyPercentage), corebringer.testskin)).left().padBottom(4).row();
            }
        }
        ScrollPane sp = new ScrollPane(lvlTbl, corebringer.testskin);
        sp.setFadeScrollBars(false);
        sp.setOverscroll(false, false);
        root.add(sp).expand().fill().row();

        // Return button row
        TextButton btnReturn = new TextButton("Return to Main Menu", corebringer.testskin);
        btnReturn.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y){
                if (onReturn != null) onReturn.run();
            }
        });
        Table btnRow = new Table();
        btnRow.add(btnReturn).height(50).pad(10);
        root.add(btnRow).expandX().fillX();

        metricsWin.add(root).expand().fill();
        uiStage.addActor(metricsWin);
        metricsWin.toFront();

        // Ensure uiStage focuses the metrics window and its scrollable content
        try {
            uiStage.setKeyboardFocus(metricsWin);
            uiStage.setScrollFocus(sp);
        } catch (Exception ignored) {}

        // Route input via global multiplexer with uiStage only
        corebringer.clearInputProcessors();
        corebringer.addInputProcessor(uiStage);
        Gdx.input.setInputProcessor(corebringer.getGlobalMultiplexer());
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
            com.badlogic.gdx.utils.JsonValue root = jsonReader.parse(json);
            // Navigate: levels -> first level -> common or boss
            com.badlogic.gdx.utils.JsonValue levels = root.get("levels");
            if (levels != null && levels.size > 0) {
                com.badlogic.gdx.utils.JsonValue level = levels.get(0);
                com.badlogic.gdx.utils.JsonValue pool = bossOnlyBattle ? level.get("boss") : level.get("common");
                if (pool != null && pool.size > 0) {
                    int idx = com.badlogic.gdx.math.MathUtils.random(pool.size - 1);
                    com.badlogic.gdx.utils.JsonValue enemyData = pool.get(idx);
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
                    }
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

    /**
     * Save game state data (HP, energy, cards, gold, stage level)
     */
    public void saveGameState() {
        SimpleSaveManager.updateData(data -> {
            if (player != null) {
                data.currentHp = player.getHp();
                data.maxHp = player.getMaxHealth();
                data.energy = player.getEnergy();
                data.maxEnergy = getMaxEnergy();
            }

            if (cardStageUI != null) {
                // Get card names from the card stage UI
                data.cards = new String[0]; // Placeholder - need to implement getCardNames method
            }

            // For now, use 0 for battles won (can be enhanced later)
            data.battleWon = 0;

            // Update stage level based on battles won
            if (data.battleWon > 0) {
                data.stageLevel = Math.min(5, (data.battleWon / 3) + 1);
            }

            Gdx.app.log("GameScreen", String.format("Saved game state: HP=%d/%d, Energy=%d/%d, Cards=%d, Battles=%d, Stage=%d",
                data.currentHp, data.maxHp, data.energy, data.maxEnergy,
                data.cards != null ? data.cards.length : 0, data.battleWon, data.stageLevel));
        });
    }
}
