package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.Utils;
import com.altf4studios.corebringer.utils.LoggingUtils;
import com.altf4studios.corebringer.utils.SaveManager;
import com.altf4studios.corebringer.utils.CardDataManager;
import com.altf4studios.corebringer.screens.gamescreen.SampleCardHandler;
import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.utils.SaveData;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.Random;

public class RestScreen implements Screen{
    private Main corebringer;
    private Stage corerestscreenstage;
    private Table corerestscreentable;
    private Table resttable;
    private Label restlabel;
    private Label healLabel;
    private Label upgradeLabel;
    private TextButton backtomapbutton;
    private CardDataManager cardDataManager;
    private Player player;
    private Array<String> upgradeableCardIds;
    private Array<ImageButton> cardButtons;
    private TextureAtlas cardAtlas;
    private ObjectMap<String, String> idToAtlasName;
    private int cardsSelected = 0;
    private final int MAX_UPGRADES = 1;
    private Array<String> upgradedCardIds; // Track upgraded cards for saving
    private int selectedCardDeckIndex = -1; // Track the deck index of the selected card
    private Array<Integer> upgradeableCardDeckIndices; // Track deck indices of upgradeable cards

    public RestScreen(Main corebringer) {
        ///Here's all the things that will initiate upon Option button being clicked
        this.corebringer = corebringer; /// The Master Key that holds all screens together
        corerestscreenstage = new Stage(new FitViewport(1280, 720));
        corerestscreentable = new Table();
        corerestscreentable.setFillParent(true);
        corerestscreenstage.addActor(corerestscreentable);

        // Initialize card data manager
        cardDataManager = CardDataManager.getInstance();
        
        // Get player from GameScreen if available
        if (corebringer.gameScreen != null) {
            player = corebringer.gameScreen.getPlayer();
        }

        // Load card atlas and mapping
        loadCardResources();

        ///This is where items in the table are declared and initialized
        resttable = new Table();
        restlabel = new Label("Rest Area - Heal and Upgrade Cards", corebringer.testskin);
        restlabel.setAlignment(Align.center);
        
        healLabel = new Label("", corebringer.testskin);
        upgradeLabel = new Label("Select 1 card to upgrade:", corebringer.testskin);
        upgradeLabel.setAlignment(Align.center);

        backtomapbutton = new TextButton("Continue to Next Node", corebringer.testskin);

        // Initialize upgradeable cards array
        upgradeableCardIds = new Array<>();
        cardButtons = new Array<>();
        upgradedCardIds = new Array<>();
        upgradeableCardDeckIndices = new Array<>();

        // Perform healing
        performHealing();

        // Select random cards for upgrade
        selectUpgradeableCards();

        // Create card selection UI
        createCardSelectionUI();

        ///Functionality for the Back to Map button
        backtomapbutton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Save upgraded cards to player's deck
                saveUpgradedCards();
                
                if (corebringer.gameMapScreen != null) {
                    try { corebringer.gameMapScreen.advanceToNextRank(); } catch (Exception ignored) {}
                }
                corebringer.setScreen(corebringer.gameMapScreen);
            }
        });

        ///This is where the items in the First Table is called
        resttable.add(restlabel).padBottom(30f).row();
        resttable.add(healLabel).padBottom(20f).row();
        resttable.add(upgradeLabel).padBottom(20f).row();
        
        // Add card selection area
        Table cardSelectionTable = new Table();
        cardSelectionTable.defaults().pad(15).center();
        
        for (int i = 0; i < cardButtons.size; i++) {
            ImageButton cardButton = cardButtons.get(i);
            String cardId = upgradeableCardIds.get(i);
            
            // Get card info for display
            SampleCardHandler card = getCardInfo(cardId);
            String displayText = card != null ? 
                card.name + "\nLevel " + card.level + "\nEffect: " + card.baseEffect :
                "Unknown Card";
            
            // Create nested table for better layout
            Table cardContainer = new Table();
            
            // Card image (larger, proper dimensions)
            cardContainer.add(cardButton).size(180, 220).row();
            
            // Card info label (positioned below the card)
            Label cardLabel = new Label(displayText, corebringer.testskin);
            cardLabel.setAlignment(Align.center);
            cardContainer.add(cardLabel).padTop(10);
            
            cardSelectionTable.add(cardContainer);
        }
        resttable.add(cardSelectionTable).padBottom(30f).row();
        
        resttable.add(backtomapbutton).padBottom(50f).row();

        ///This is where the first table is called to the core table
        corerestscreentable.add(resttable);
    }

    private void performHealing() {
        if (player != null) {
            int healAmount = (int) (player.getMaxHealth() * 0.30f); // 30% healing
            player.heal(healAmount);
            healLabel.setText("You have been healed for " + healAmount + " HP!");
            healLabel.setColor(Color.GREEN);
        } else {
            healLabel.setText("Player data not available");
            healLabel.setColor(Color.RED);
        }
    }

    private void loadCardResources() {
        // Load card atlas
        cardAtlas = new TextureAtlas("assets/cards/cards_atlas.atlas");
        
        // Load card ID to atlas name mapping
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
            Gdx.app.error("RestScreen", "Failed to load cards.json: " + e.getMessage());
        }
    }

    private void selectUpgradeableCards() {
        // Get player's deck from GameScreen
        String[] playerDeck = corebringer.gameScreen != null ? 
            corebringer.gameScreen.getSavedDeckIds() : new String[]{};
        
        // Filter to only level 1 cards (can be upgraded) and track their deck indices
        Array<String> tempUpgradeableCardIds = new Array<>();
        Array<Integer> tempUpgradeableCardDeckIndices = new Array<>();
        
        for (int i = 0; i < playerDeck.length; i++) {
            String cardId = playerDeck[i];
            if (cardId != null && cardId.endsWith("_1")) {
                tempUpgradeableCardIds.add(cardId);
                tempUpgradeableCardDeckIndices.add(i);
            }
        }
        
        // Select 3 random cards from the player's level 1 cards
        Random random = new Random();
        int cardsToShow = Math.min(3, tempUpgradeableCardIds.size);
        
        for (int i = 0; i < cardsToShow; i++) {
            if (tempUpgradeableCardIds.size > 0) {
                int randomIndex = random.nextInt(tempUpgradeableCardIds.size);
                this.upgradeableCardIds.add(tempUpgradeableCardIds.get(randomIndex));
                this.upgradeableCardDeckIndices.add(tempUpgradeableCardDeckIndices.get(randomIndex));
                tempUpgradeableCardIds.removeIndex(randomIndex);
                tempUpgradeableCardDeckIndices.removeIndex(randomIndex);
            }
        }
    }

    private SampleCardHandler getCardInfo(String cardId) {
        if (cardDataManager != null && cardDataManager.isInitialized()) {
            return cardDataManager.getById(cardId);
        }
        return null;
    }

    private void createCardSelectionUI() {
        for (int i = 0; i < upgradeableCardIds.size; i++) {
            String cardId = upgradeableCardIds.get(i);
            
            // Get atlas name for this card
            String atlasName = idToAtlasName.get(cardId);
            if (atlasName == null) {
                atlasName = "bck_card"; // fallback
            }
            
            // Create card button using GameMapScreen's method
            ImageButton cardButton = createAtlasButton(atlasName);
            cardButton.setName(cardId);
            
            // Add click listener
            final int cardIndex = i;
            cardButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (cardsSelected < MAX_UPGRADES) {
                        upgradeCard(cardIndex);
                        cardsSelected++;
                        updateUpgradeLabel();
                        
                        // Show card back cover and disable interaction for ALL cards
                        showCardBackCover(cardButton);
                        
                        // Show back covers for all other cards
                        for (int j = 0; j < cardButtons.size; j++) {
                            if (j != cardIndex) {
                                showCardBackCover(cardButtons.get(j));
                            }
                        }
                        
                        // Update the label to show the upgrade
                        updateCardDisplay(cardIndex);
                    }
                }
            });
            
            cardButtons.add(cardButton);
        }
    }

    private ImageButton createAtlasButton(String regionName) {
        TextureRegionDrawable base = new TextureRegionDrawable(cardAtlas.findRegion(regionName));
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = base;
        style.over = base.tint(new Color(1f, 1f, 1f, 0.9f));
        style.down = base.tint(new Color(0.85f, 0.85f, 0.85f, 1f));
        style.checked = base.tint(new Color(0.9f, 0.9f, 0.9f, 1f));
        return new ImageButton(style);
    }

    private void showCardBackCover(ImageButton cardButton) {
        // Change to back cover texture
        TextureRegionDrawable backCover = new TextureRegionDrawable(cardAtlas.findRegion("bck_card"));
        ImageButton.ImageButtonStyle backStyle = new ImageButton.ImageButtonStyle();
        backStyle.up = backCover;
        backStyle.down = backCover;
        backStyle.over = backCover;
        cardButton.setStyle(backStyle);
        
        // Disable interaction
        cardButton.setTouchable(Touchable.disabled);
        cardButton.setColor(Color.GRAY);
    }

    private void updateCardDisplay(int cardIndex) {
        if (cardIndex < 0 || cardIndex >= upgradeableCardIds.size) return;
        
        String cardId = upgradeableCardIds.get(cardIndex);
        SampleCardHandler card = getCardInfo(cardId);
        
        // Log the upgrade
        if (card != null) {
            LoggingUtils.log("RestScreen", "Card " + card.name + " upgraded to level " + card.level);
        }
    }

    private void upgradeCard(int cardIndex) {
        if (cardIndex < 0 || cardIndex >= upgradeableCardIds.size) return;
        
        String currentId = upgradeableCardIds.get(cardIndex);
        
        // Store the deck index of the selected card
        selectedCardDeckIndex = upgradeableCardDeckIndices.get(cardIndex);
        
        // Upgrade the card to next level
        String nextLevelId = getNextLevelCardId(currentId);
        if (nextLevelId != null) {
            upgradeableCardIds.set(cardIndex, nextLevelId);
            upgradedCardIds.add(nextLevelId); // Track upgraded card for saving
            SampleCardHandler upgradedCard = getCardInfo(nextLevelId);
            if (upgradedCard != null) {
                LoggingUtils.log("RestScreen", "Upgraded card to " + upgradedCard.name + " at deck index " + selectedCardDeckIndex);
            }
        }
    }

    private String getNextLevelCardId(String currentId) {
        switch (currentId) {
            case "basic_variable_slash_1": return "basic_variable_slash_2";
            case "basic_variable_slash_2": return "basic_variable_slash_3";
            case "shield_final_shield_1": return "shield_final_shield_2";
            case "shield_final_shield_2": return "shield_final_shield_3";
            case "heal_heal_package_1": return "heal_heal_package_2";
            case "heal_heal_package_2": return "heal_heal_package_3";
            default: return null;
        }
    }

    private void updateUpgradeLabel() {
        int remaining = MAX_UPGRADES - cardsSelected;
        if (remaining > 0) {
            upgradeLabel.setText("Select 1 card to upgrade:");
        } else {
            upgradeLabel.setText("Card upgrade selected! Ready to continue.");
            upgradeLabel.setColor(Color.GREEN);
        }
    }

    private void saveUpgradedCards() {
        // Always save HP synchronization, even if no cards were upgraded
        
        // Get current save data
        SaveData stats = SaveManager.saveExists() ? SaveManager.loadStats() : null;
        if (stats == null) {
            LoggingUtils.log("RestScreen", "No save data found, cannot save upgrades");
            return;
        }
        
        // Get current deck
        String[] currentDeck = stats.cards != null ? stats.cards : new String[]{};
        
        // Create new deck with upgraded cards
        String[] newDeck = new String[currentDeck.length];
        System.arraycopy(currentDeck, 0, newDeck, 0, currentDeck.length);
        
        // Replace only the specific card at the selected deck index (if any cards were upgraded)
        if (selectedCardDeckIndex >= 0 && selectedCardDeckIndex < newDeck.length && upgradedCardIds.size > 0) {
            String upgradedId = upgradedCardIds.get(0); // Should only be one upgrade
            String originalId = newDeck[selectedCardDeckIndex];
            newDeck[selectedCardDeckIndex] = upgradedId;
            LoggingUtils.log("RestScreen", "Replaced " + originalId + " with " + upgradedId + " at deck index " + selectedCardDeckIndex);
        }
        
        // Save the updated deck with current player HP (after healing)
        int currentPlayerHp = player != null ? player.getHp() : (stats.currentHp > 0 ? stats.currentHp : (stats.hp > 0 ? stats.hp : 20));
        int currentPlayerMaxHp = player != null ? player.getMaxHealth() : (stats.maxHp > 0 ? stats.maxHp : 20);
        
        SaveManager.saveStats(
            currentPlayerHp,
            currentPlayerMaxHp,
            stats.energy,
            stats.maxEnergy > 0 ? stats.maxEnergy : 3,
            newDeck,
            stats.battleWon,
            stats.gold
        );
        
        if (upgradedCardIds.size > 0) {
            LoggingUtils.log("RestScreen", "Saved 1 card upgrade to player deck at index " + selectedCardDeckIndex + " and synchronized player HP: " + currentPlayerHp + "/" + currentPlayerMaxHp);
        } else {
            LoggingUtils.log("RestScreen", "Synchronized player HP after healing: " + currentPlayerHp + "/" + currentPlayerMaxHp);
        }
    }

    private String getOriginalCardId(String upgradedId) {
        switch (upgradedId) {
            case "basic_variable_slash_2": return "basic_variable_slash_1";
            case "basic_variable_slash_3": return "basic_variable_slash_2";
            case "shield_final_shield_2": return "shield_final_shield_1";
            case "shield_final_shield_3": return "shield_final_shield_2";
            case "heal_heal_package_2": return "heal_heal_package_1";
            case "heal_heal_package_3": return "heal_heal_package_2";
            default: return null;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(corerestscreenstage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        corerestscreenstage.act(delta); ////Used to call the Stage and render the elements that is inside it
        corerestscreenstage.draw();
    }

    @Override public void resize(int width, int height) {
        corerestscreenstage.getViewport().update(width, height, true);
        Gdx.input.setInputProcessor(corerestscreenstage);
    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void hide() {
    }

    @Override
    public void dispose() {
        corerestscreenstage.dispose();
        if (cardAtlas != null) {
            cardAtlas.dispose();
        }
    }
}
