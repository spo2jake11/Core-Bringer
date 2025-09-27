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
    // --- End UI Components ---

    // --- Energy System ---
    private int energy = 0;
    private final int MAX_ENERGY = 3;
    private Label energyLabel;
    private Window optionsWindow;
    // Persisted deck ids for this run
    private String[] savedDeckIds;
    // --- End Energy System ---

    // --- Death Screen ---
    private Window deathScreenWindow = null;
    private boolean deathScreenShown = false;
    // --- End Death Screen ---

    // --- Victory Screen ---
    private Window victoryScreenWindow = null;
    private boolean victoryScreenShown = false;
    // --- End Victory Screen ---


    public GameScreen(Main corebringer) {
        this.corebringer = corebringer; /// The Master Key that holds all screens together

        // Initialize CardParser
        cardParser = CardParser.getInstance();

        ///This stages are separated to lessen complications
        uiStage = new Stage(new ScreenViewport());
        battleStage = new Stage(new ScreenViewport());
        cardStage = new Stage(new ScreenViewport());


        // --- Load stats from save file (must exist at this point) ---
        int hp = 20; // Default fallback values
        int energyVal = 0;
        String[] cards = new String[]{
            "basic_variable_slash_1", "basic_variable_slash_1", "basic_variable_slash_1",
            "basic_variable_slash_1", "basic_variable_slash_1", "shield_final_shield_1",
            "shield_final_shield_1", "shield_final_shield_1", "shield_final_shield_1",
            "shield_final_shield_1", "heal_ultimate_heal_1", "heal_ultimate_heal_1",
            "heal_ultimate_heal_1", "poison_looping_bite_1", "poison_looping_bite_1"
        };
        int battleWon = 0;
        String enemyName = "Enemy";
        int enemyHp = 20;

        // Load from save file (should exist from MainMenuScreen)
        if (SaveManager.saveExists()) {
            com.altf4studios.corebringer.utils.SaveData stats = SaveManager.loadStats();
            if (stats != null) {
                hp = stats.hp;
                energyVal = stats.energy;
                cards = stats.cards;
                battleWon = stats.battleWon;
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
        // Initialize player and enemy (example values, adjust as needed)
        player = new Player("Player", hp, 10, 5, 3); // hp loaded from save
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
        cardStageUI = new CardStageUI(cardStage, corebringer.testskin, cardParser, player, enemy, turnManager, this);
        createUI();

        // If battleWon == 1, reroll enemy and reset battleWon
        if (battleWon == 1) {
            battleStageUI.changeEnemy();
            battleWon = 0;
            SaveManager.saveStats(player.getHp(), energyVal, cards, battleWon);
        }

        // Set energy from save
        setEnergy(energyVal);
        // Add energy label to battleStage
        energyLabel = new Label("Energy: " + energy + "/10", corebringer.testskin);
        energyLabel.setAlignment(Align.topLeft);
        energyLabel.setPosition(10, Gdx.graphics.getHeight() - 30);
        battleStage.addActor(energyLabel);

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
        SaveManager.saveStats(player.getHp(), energy, new String[]{}, battleWonValue); // TODO: pass actual cards
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
        Label lblHpNum = new Label("HP: " + player.getHp() + "/" + player.getMaxHealth(), corebringer.testskin);
        Label lblGold = new Label("Gold: 0", corebringer.testskin);

        // Table handler for the right side
        Table tblRightPane = new Table();
        Label lblMenu = new Label("Menu", corebringer.testskin);
        Label lblDeck = new Label("Deck", corebringer.testskin);
        Label lblMap = new Label("Map", corebringer.testskin);

        // Filler table
        Table tblFiller = new Table();

        // Left Pane placement
        tblLeftPane.defaults().padLeft(10).padRight(10).uniform();
        tblLeftPane.add(lblCharName);
        tblLeftPane.add(lblHpNum);
        tblLeftPane.add(lblGold);

        // Right Pane placement
        tblRightPane.defaults().padRight(10).padLeft(10).uniform();
        tblRightPane.add(lblMap);
        tblRightPane.add(lblDeck);
        tblRightPane.add(lblMenu);

        // Add content to inner table
        innerTable.add(tblLeftPane).left();
        innerTable.add(tblFiller).growX();
        innerTable.add(tblRightPane).right().padRight(15);

        // Add inner table to main table with fixed height
        tbltopUI.add(innerTable).growX().height(75);

        uiStage.addActor(tbltopUI);
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
        // Add Recharge button at the bottom of the screen, below cardStageUI
        addRechargeButtonToBottom();

        // --- Ensure viewport is updated to current window size ---
        battleStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        cardStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    private void addRechargeButtonToBottom() {
        // Remove previous Recharge button if any
        for (Actor actor : battleStage.getActors()) {
            if (actor.getName() != null && actor.getName().equals("RechargeButton")) {
                actor.remove();
                break;
            }
        }
        TextButton btnRecharge = new TextButton("Recharge", corebringer.testskin);
        btnRecharge.setName("RechargeButton");
        btnRecharge.setWidth(200);
        btnRecharge.setHeight(50);
        btnRecharge.setPosition((Gdx.graphics.getWidth() / btnRecharge.getWidth()) + 50, 100);
        btnRecharge.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.codeEditorScreen);
            }
        });
        battleStage.addActor(btnRecharge);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        battleStage.act(delta);
        battleStage.draw();
        cardStage.act(delta);
        cardStage.draw();
        uiStage.act(delta);
        uiStage.draw();
        // Removed: editorStage.act(delta); editorStage.draw();

        // --- BattleManager: process turn phases, enemy AI, and UI indicator ---
        battleManager.update(delta);
        // --- End BattleManager ---

        /// For wiring the HP/Shield values properly
        battleStageUI.updateHpBars(player.getHp(), enemy.getHp());
        battleStageUI.updateShieldBars(player.getBlock(), enemy.getBlock());
//        battleStageUI.updateHpColors(player.hasPoison(), enemy.hasPoison());
//        battleStageUI.updateShieldColors(player.getBlock() > 0, enemy.getBlock() > 0);
//        // New: enemy HP color for bleed/stun (priority handled in UI)
//        battleStageUI.updateEnemyHpStatusColor(enemy.hasPoison(), enemy.hasStatus("Bleed"), enemy.hasStatus("Stun"));

        // Turn indicator is updated by BattleManager

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
        // Clean up victory screen
        if (victoryScreenWindow != null) {
            victoryScreenWindow.remove();
            victoryScreenWindow = null;
        }
        this.dispose();

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
            energyLabel.setText("Energy: " + energy + "/" + MAX_ENERGY);
        }
    }

    private void showDeathScreen() {
        if (deathScreenWindow != null) return; // Already shown
        deathScreenShown = true;
        // Load the death screen texture
        Texture deathTexture = new Texture(Gdx.files.internal("assets/nameplates/death_scrn.png"));
        Image deathImage = new Image(deathTexture);
        deathImage.getColor().a = 0f;
        deathImage.addAction(Actions.fadeIn(1.5f)); // 1.5 seconds fade in
        // Create window to overlay everything
        deathScreenWindow = new Window("", corebringer.testskin);
        deathScreenWindow.setModal(true);
        deathScreenWindow.setMovable(false);
        deathScreenWindow.setResizable(false);
        deathScreenWindow.setFillParent(true);
        deathScreenWindow.setTouchable(Touchable.enabled);
        deathScreenWindow.setColor(1,1,1,0f);
        deathScreenWindow.addAction(Actions.fadeIn(1.5f));
        // Centered button
        TextButton btnReturn = new TextButton("Return to Title", corebringer.testskin);
        btnReturn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                // Delete save file on death
                com.altf4studios.corebringer.utils.SaveManager.deleteSave();
                corebringer.setScreen(corebringer.mainMenuScreen);
                // Optionally reset death screen state
                if (deathScreenWindow != null) {
                    deathScreenWindow.remove();
                    deathScreenWindow = null;
                    deathScreenShown = false;
                }
            }
        });
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.add(deathImage).expand().fill().row();
        overlay.add(btnReturn).center().padTop(-300f); // Adjust as needed for button position
        deathScreenWindow.add(overlay).expand().fill();
        battleStage.addActor(deathScreenWindow);
        // Block all input except the button
        Gdx.input.setInputProcessor(battleStage);
    }

    private void showVictoryScreen() {
        if (victoryScreenWindow != null) return; // Already shown
        victoryScreenShown = true;

        // Calculate window size (70% width, 80% height)
        float windowWidth = Gdx.graphics.getWidth() * 0.7f;
        float windowHeight = Gdx.graphics.getHeight() * 0.8f;
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

        // Proceed button
        TextButton btnProceed = new TextButton("Proceed", corebringer.testskin);
        btnProceed.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                // Save progress with battle won = 1
                saveProgress(1);
                // Reroll enemy and cards for next battle
                rerollEnemyAndCards();
                // Hide victory screen
                if (victoryScreenWindow != null) {
                    victoryScreenWindow.remove();
                    victoryScreenWindow = null;
                    victoryScreenShown = false;
                }
            }
        });

        // Add content to table
        contentTable.add(victoryMessage).expand().center().row();
        contentTable.add(btnProceed).center().padTop(50f);

        victoryScreenWindow.add(contentTable).expand().fill();
        battleStage.addActor(victoryScreenWindow);

        // Block all input except the button
        Gdx.input.setInputProcessor(battleStage);
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
