package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
// Removed interpreter integration
import com.altf4studios.corebringer.utils.CardParser;
import com.altf4studios.corebringer.turns.TurnManager;
import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.entities.Enemy;
import com.altf4studios.corebringer.screens.gamescreen.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
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

    private Stage battleStage;
    private Stage cardStage;

    // CardParser instance for managing card data
    private CardParser cardParser;

    // --- TurnManager Integration ---
    private TurnManager turnManager;
    private Player player;
    private Enemy enemy;
    // --- End TurnManager Integration ---

    // --- UI Components ---
    private BattleStageUI battleStageUI;
    private CardStageUI cardStageUI;
    // --- End UI Components ---

    // --- Energy System ---
    private int energy = 0;
    private final int MAX_ENERGY = 10;
    private Label energyLabel;
    private Window optionsWindow;
    // --- End Energy System ---

    // --- Death Screen ---
    private Window deathScreenWindow = null;
    private boolean deathScreenShown = false;
    // --- End Death Screen ---


    public GameScreen(Main corebringer) {
        this.corebringer = corebringer; /// The Master Key that holds all screens together

        // Initialize CardParser
        cardParser = CardParser.getInstance();

        ///This stages are separated to lessen complications
        battleStage = new Stage(new ScreenViewport());
        cardStage = new Stage(new ScreenViewport());

        // --- Load stats from save file if exists ---
        int hp = 20;
        int energyVal = 0;
        String[] cards = new String[]{};
        int battleWon = 0;
        String enemyName = "Enemy";
        int enemyHp = 20;
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
        if (SaveManager.saveExists()) {
            com.altf4studios.corebringer.utils.SaveData stats = SaveManager.loadStats();
            if (stats != null) {
                hp = stats.hp;
                energyVal = stats.energy;
                cards = stats.cards;
            }
        }

        // --- TurnManager Integration ---
        // Initialize player and enemy (example values, adjust as needed)
        player = new Player("Player", hp, 10, 5, 3); // hp loaded from save
        enemy = new Enemy("enemy1", enemyName, enemyHp, 8, 3, Enemy.enemyType.NORMAL, 0, new String[]{});
        turnManager = new TurnManager(player, enemy);
        // --- End TurnManager Integration ---

        ///Every stages provides a main method for them
        ///They also have local variables and objects for them to not interact with other methods
        battleStageUI = new BattleStageUI(battleStage, corebringer.testskin);
        cardStageUI = new CardStageUI(cardStage, corebringer.testskin, cardParser, player, enemy, turnManager, this);

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
        // Interpreter removed

    }

    // Call this after any stat change (hp, energy, cards, battleWon)
    public void saveProgress(int battleWonValue) {
        SaveManager.saveStats(player.getHp(), energy, new String[]{}, battleWonValue); // TODO: pass actual cards
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
        btnRecharge.setPosition((Gdx.graphics.getWidth() - btnRecharge.getWidth()) / 2f, 100);
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
        // Removed: editorStage.act(delta); editorStage.draw();

        // --- TurnManager Integration: process turn phases and execute enemy turns ---
        // Update turn manager (handles delays)
        turnManager.update(delta);

        // Execute enemy turn if it's enemy's turn and not delaying
        if (turnManager.isEnemyTurn() && !turnManager.isDelaying()) {
            turnManager.executeEnemyTurn();
        }

        // Check for game over (only log once)
        if (turnManager.shouldLogGameOver()) {
            String winner = turnManager.getWinner();
            Gdx.app.log("GameScreen", "Game Over! Winner: " + winner);
            // You can add game over UI here later
        }
        // --- End TurnManager Integration ---

        /// For wiring the HP/Shield values properly
        battleStageUI.updateHpBars(player.getHp(), enemy.getHp());
        battleStageUI.updateShieldBars(player.getBlock(), enemy.getBlock());
        battleStageUI.updateHpColors(player.hasPoison(), enemy.hasPoison());
        battleStageUI.updateShieldColors(player.getBlock() > 0, enemy.getBlock() > 0);

        // Update turn indicator
        if (turnManager.isPlayerTurn()) {
            battleStageUI.updateTurnIndicator("Player's Turn");
        } else {
            battleStageUI.updateTurnIndicator("Enemy's Turn");
        }

        // --- Death Screen Trigger ---
        if (!deathScreenShown && player.getHp() <= 0) {
            showDeathScreen();
        }
        // --- End Death Screen Trigger ---
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

//    private void refreshCardHand() {
//        if (cardStageUI != null) {
//            cardStageUI.refreshCardHand();
//        }
//    }
    // --- End test methods ---

    @Override public void resize(int width, int height) {
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
        if (battleStageUI != null) {
            battleStageUI.dispose();
        }
        battleStage.dispose();
        // Removed: editorStage.dispose();
        cardStage.dispose();
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
        battleStage.addActor(optionsWindow);
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
        if (cardStageUI != null) {
            cardStageUI.refreshCardHand();
        }
        // Reset turn manager phase to player's turn and clear any pending turn state
        if (turnManager != null) {
            turnManager.reset();
            Gdx.app.log("GameScreen", "TurnManager reset after reroll. Player HP: " + player.getHp() + ", Enemy HP: " + enemy.getHp());
        }
    }
}
