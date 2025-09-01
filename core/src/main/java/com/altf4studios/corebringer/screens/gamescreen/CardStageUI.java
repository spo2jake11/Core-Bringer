package com.altf4studios.corebringer.screens.gamescreen;

import com.altf4studios.corebringer.utils.CardParser;
import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.entities.Enemy;
import com.altf4studios.corebringer.status.Poison;
import com.altf4studios.corebringer.turns.TurnManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.altf4studios.corebringer.screens.GameScreen;

public class CardStageUI {
    private Stage cardStage;
    private Skin skin;
    private CardParser cardParser;
    private CardHandTable cardHandTable;
    private float worldHeight;
    private boolean[] discardedCards;
    private int cardsInHand;
    private Button drawButton;
    private Array<String> availableCardNames;
    private Player player;
    private Enemy enemy;
    private TurnManager turnManager;
    private GameScreen gameScreen;

    public CardStageUI(Stage cardStage, Skin skin, CardParser cardParser, Player player, Enemy enemy, TurnManager turnManager, GameScreen gameScreen) {
        this.cardStage = cardStage;
        this.skin = skin;
        this.player = player;
        this.enemy = enemy;
        this.cardParser = cardParser;
        this.turnManager = turnManager;
        this.gameScreen = gameScreen;
        this.discardedCards = new boolean[5];
        this.cardsInHand = 5;
        setupCardUI();
        worldHeight = cardStage.getViewport().getWorldHeight();
    }

    private void setupCardUI() {
        // Calculate responsive dimensions
        float worldWidth = Gdx.graphics.getWidth();
        float worldHeight = Gdx.graphics.getHeight();
        float cardHeight = worldHeight * 0.2f;
        float cardWidth = (worldWidth * 0.8f) / 5;

        // Initialize available card names
        initializeAvailableCards();

        // Get card names
        String[] cardNames = getCardNames();

        // Create card hand table
        cardHandTable = new CardHandTable(skin, cardWidth, cardHeight, cardNames);

        // Add click listeners to cards for discard functionality
        setupCardClickListeners();

        // Create draw button
        createDrawButton();

        // Add draw button to the card table in a new row
        cardHandTable.cardTable.row();
        cardHandTable.cardTable.add(drawButton).colspan(5).padTop(10);

        // Create parent table
        Table parentTable = new Table();
        parentTable.setFillParent(true);
        parentTable.bottom();
        parentTable.add(cardHandTable.cardTable).expandX().padBottom(worldHeight * 0.55f - 100f); // Move up by 100 units

        cardStage.addActor(parentTable);
    }

    private void initializeAvailableCards() {
        availableCardNames = new Array<>();
        if (cardParser.isCardsLoaded()) {
            Array<SampleCardHandler> allCards = cardParser.getAllCards();
            for (SampleCardHandler card : allCards) {
                availableCardNames.add(card.name);
            }
        } else {
            // Fallback card names if cards aren't loaded
            availableCardNames.add("Card 1");
            availableCardNames.add("Card 2");
            availableCardNames.add("Card 3");
            availableCardNames.add("Card 4");
            availableCardNames.add("Card 5");
            availableCardNames.add("Card 6");
            availableCardNames.add("Card 7");
            availableCardNames.add("Card 8");
            availableCardNames.add("Card 9");
            availableCardNames.add("Card 10");
        }
    }

    private String[] getCardNames() {
        String[] cardNames = new String[5];

        for (int i = 0; i < 5; i++) {
            if (availableCardNames.size > 0) {
                int randomIndex = (int) (Math.random() * availableCardNames.size);
                cardNames[i] = availableCardNames.get(randomIndex);
                availableCardNames.removeIndex(randomIndex);
            } else {
                cardNames[i] = "Corrupted Card";
                discardedCards[i] = true;
            }
        }

        return cardNames;
    }

    private void setupCardClickListeners() {
        for (int i = 0; i < cardHandTable.cardLabels.length; i++) {
            final int cardIndex = i;
            final Label cardLabel = cardHandTable.cardLabels[i];

            cardLabel.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    String cardName = cardLabel.getText().toString();
                    SampleCardHandler card = cardParser.findCardByName(cardName);
                    int cost = (card != null) ? card.cost : 0;
                    if (gameScreen.getEnergy() < cost) {
                        Dialog dialog = new Dialog("No Energy", skin) {
                            @Override
                            protected void result(Object object) {
                                this.hide();
                            }
                        };
                        dialog.text("No energy! Please recharge!");
                        dialog.button("OK");
                        dialog.show(cardStage);
                        return;
                    }
                    if (!discardedCards[cardIndex] && !cardName.equals("Corrupted Card") || discardedCards[cardIndex] && cardName.equals("Corrupted Card")) {
                        // Deduct energy
                        gameScreen.addEnergy(-cost);
                        // Call the effect resolver before discarding
                        resolveCardEffect(cardName);
                        // Discard the card
                        discardedCards[cardIndex] = true;
                        cardLabel.setText("Discarded");
                        cardLabel.setColor(0.5f, 0.5f, 0.5f, 0.5f); // Gray out the card
                        cardsInHand--;
                        // Check if all cards are used
                        if (cardsInHand == 0) {
                            refreshCardHand();
                        }
                        // Show draw button if there are cards available to draw
                        if (availableCardNames.size > 0) {
                            showDrawButton();
                        }
                    }
                }
            });
        }
    }

    private void createDrawButton() {
        drawButton = new Button(skin, "default");
        drawButton.setVisible(false);
        drawButton.setColor(Color.GOLD);

        drawButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                drawCard();
            }
        });
    }

    private void showDrawButton() {
        if (availableCardNames.size > 0) {
            drawButton.setVisible(true);
        }
    }

    private void hideDrawButton() {
        drawButton.setVisible(false);
    }

    private void drawCard() {
        if (availableCardNames.size > 0 && cardsInHand < 5) {
            // Find an empty slot
            for (int i = 0; i < cardHandTable.cardLabels.length; i++) {
                if (discardedCards[i] || cardHandTable.cardLabels[i].getText().toString().equals("Corrupted Card")) {
                    // Draw a new card
                    int randomIndex = (int) (Math.random() * availableCardNames.size);
                    String newCardName = availableCardNames.get(randomIndex);

                    cardHandTable.cardLabels[i].setText(newCardName);
                    cardHandTable.cardLabels[i].setColor(1f, 1f, 1f, 1f); // Reset color
                    discardedCards[i] = false;
                    cardsInHand++;

                    availableCardNames.removeIndex(randomIndex);

                    // Hide draw button if no more cards available or hand is full
                    if (availableCardNames.size == 0 || cardsInHand == 5) {
                        hideDrawButton();
                    }

                    break;
                }
            }
        }
    }

    public CardHandTable getCardHandTable() {
        return cardHandTable;
    }

    public void refreshCardHand() {
        // Reset discarded cards array
        for (int i = 0; i < discardedCards.length; i++) {
            discardedCards[i] = false;
        }

        // Reset cards in hand count
        cardsInHand = 5;

        // Reinitialize available cards if needed
        if (availableCardNames.size == 0) {
            initializeAvailableCards();
        }

        // Get new random card names
        String[] newCardNames = getCardNames();

        // Update card labels with new names and reset colors
        for (int i = 0; i < cardHandTable.cardLabels.length && i < newCardNames.length; i++) {
            cardHandTable.cardLabels[i].setText(newCardNames[i]);
            cardHandTable.cardLabels[i].setColor(1f, 1f, 1f, 1f); // Reset color
        }

        // Hide draw button after refresh
        hideDrawButton();
    }

    private void resolveCardEffect(String cardName) {
        SampleCardHandler card = cardParser.findCardByName(cardName);

        if (card != null) {
            // Handle cards that have both attack and defense effects (like Astral Mirage)
            boolean hasAttackEffect = card.description != null &&
                (card.description.toLowerCase().contains("deal") || card.description.toLowerCase().contains("damage"));
            boolean hasShieldEffect = card.description != null &&
                card.description.toLowerCase().contains("gain") && card.description.toLowerCase().contains("shield");

            if (card.type.equalsIgnoreCase("ATTACK")) {
                int damage = card.baseEffect;

                // Apply damage to enemy
                if (enemy != null && enemy.isAlive()) {
                    enemy.takeDamage(damage);
                }

                // If this attack card also has shield effect, apply it
                if (hasShieldEffect) {
                    int shieldFromDesc = parseShieldFromDescription(card.description);
                    if (shieldFromDesc > 0) {
                        if (player != null && player.isAlive()) {
                            player.gainBlock(shieldFromDesc);
                        }
                    }
                }

            } else if (card.type.equalsIgnoreCase("DEFENSE")) {
                int shieldFromDesc = parseShieldFromDescription(card.description);
                int block = shieldFromDesc > 0 ? shieldFromDesc : card.baseEffect;

                // Apply block (shield) to player
                if (player != null && player.isAlive()) {
                    player.gainBlock(block);
                }

                // If this defense card also has attack effect, apply it
                if (hasAttackEffect) {
                    int damage = card.baseEffect;
                    if (damage > 0) {
                        if (enemy != null && enemy.isAlive()) {
                            enemy.takeDamage(damage);
                        }
                    }
                }

            } else if (card.type.equalsIgnoreCase("BUFF")) {
                // Handle buff effects based on tags
                if (card.tags != null && card.tags.contains("INCREASE_ATTACK", false)) {
                    // For now, just log the buff - you can implement attack buffing later
                } else if (card.tags != null && card.tags.contains("APPLY_STATS", false)) {
                    // For now, just log the buff - you can implement stat buffing later
                } else {
                    Gdx.app.log("CardEffect", "Card '" + card.name + "' used! Generic buff effect: " + card.baseEffect);
                }

            } else if (card.type.equalsIgnoreCase("DEBUFF")) {
                // Handle debuff effects
                Gdx.app.log("CardEffect", "Card '" + card.name + "' used! Applies debuff: " + card.baseEffect);
                // You can implement debuff effects here

            } else {
                Gdx.app.log("CardEffect", "Unknown card type: " + card.type);
            }

            // Check for poison application in card description
            if (card.description != null && card.description.toLowerCase().contains("poison")) {
                int poisonStacks = 0;
                // Extract the number before the word 'poison'
                String[] words = card.description.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    if (words[i].toLowerCase().startsWith("poison")) {
                        if (i > 0) {
                            try {
                                poisonStacks = Integer.parseInt(words[i - 1]);
                            } catch (NumberFormatException ignored) {}
                        }
                        break;
                    }
                }
                if (poisonStacks <= 0) {
                    poisonStacks = 5; // sensible default
                }

                if (enemy != null && enemy.isAlive()) {
                    Poison poison = new Poison("Poison", poisonStacks, 0);
                    enemy.addPoison(poison);
                }
            }

            // End player turn after playing a card
            if (turnManager != null && turnManager.isPlayerTurn()) {
                turnManager.endPlayerTurn();
            }

        } else {
            Gdx.app.log("CardEffect", "No such card found: " + cardName);
        }
    }

    private int parseShieldFromDescription(String description) {
        if (description == null) return 0;
        String lower = description.toLowerCase();
        int gainIdx = lower.indexOf("gain");
        int shieldIdx = lower.indexOf("shield");
        if (gainIdx == -1 || shieldIdx == -1 || shieldIdx <= gainIdx) return 0;
        String between = lower.substring(gainIdx, shieldIdx);
        String[] parts = between.split("[^0-9]+");
        int value = 0;
        for (String p : parts) {
            if (p != null && p.length() > 0) {
                try {
                    value = Integer.parseInt(p);
                } catch (NumberFormatException ignored) {}
            }
        }
        return Math.max(0, value);
    }
}
