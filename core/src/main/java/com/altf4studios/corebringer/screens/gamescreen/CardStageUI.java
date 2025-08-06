package com.altf4studios.corebringer.screens.gamescreen;

import com.altf4studios.corebringer.utils.CardParser;
import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.entities.Enemy;
import com.altf4studios.corebringer.status.Poison;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

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

    public CardStageUI(Stage cardStage, Skin skin, CardParser cardParser, Player player, Enemy enemy) {
        this.cardStage = cardStage;
        this.skin = skin;
        this.player = player;
        this.enemy = enemy;
        this.cardParser = cardParser;
        this.discardedCards = new boolean[5];
        this.cardsInHand = 5;
        setupCardUI();
        Gdx.app.log("CardStageUI", "Card stage UI initialized successfully");
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
        parentTable.add(cardHandTable.cardTable).expandX().padBottom(worldHeight * 0.45f);

        cardStage.addActor(parentTable);
    }

    private void initializeAvailableCards() {
        availableCardNames = new Array<>();
        Gdx.app.log("CardStageUI", "Initializing available cards...");
        Gdx.app.log("CardStageUI", "CardParser isCardsLoaded: " + cardParser.isCardsLoaded());
        Gdx.app.log("CardStageUI", "CardParser card count: " + cardParser.getCardCount());

        if (cardParser.isCardsLoaded()) {
            Array<SampleCardHandler> allCards = cardParser.getAllCards();
            Gdx.app.log("CardStageUI", "Loading " + allCards.size + " cards from CardParser");
            for (SampleCardHandler card : allCards) {
                availableCardNames.add(card.name);
                Gdx.app.log("CardStageUI", "Added card: " + card.name + " (Type: " + card.type + ")");
            }
        } else {
            Gdx.app.log("CardStageUI", "Cards not loaded, using fallback names");
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

        Gdx.app.log("CardStageUI", "Total available card names: " + availableCardNames.size);
    }

    private String[] getCardNames() {
        String[] cardNames = new String[5];

        for (int i = 0; i < 5; i++) {
            if (availableCardNames.size > 0) {
                int randomIndex = (int) (Math.random() * availableCardNames.size);
                cardNames[i] = availableCardNames.get(randomIndex);
                Gdx.app.log("Card Loaded", availableCardNames.get(randomIndex));
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
                    if (!discardedCards[cardIndex] && !cardLabel.getText().toString().equals("Corrupted Card") || discardedCards[cardIndex] && cardLabel.getText().toString().equals("Corrupted Card")) {

                        /// Call the effect resolver before discarding
                        resolveCardEffect(cardLabel.getText().toString());

                        /// Discard the card (the logic now somehow works when discarding "corrupted" cards)
                        discardedCards[cardIndex] = true;
                        cardLabel.setText("Discarded");
                        cardLabel.setColor(0.5f, 0.5f, 0.5f, 0.5f); // Gray out the card
                        cardsInHand--;

                        Gdx.app.log("CardStageUI", "Card discarded: " + cardLabel.getText() + " at index " + cardIndex);
                        Gdx.app.log("CardStageUI", "Cards remaining: " + cardsInHand);

                        // Check if all cards are used
                        if (cardsInHand == 0) {
                            Gdx.app.log("CardStageUI", "All cards used, refreshing hand...");
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

                    Gdx.app.log("CardStageUI", "Drew card: " + newCardName + " at index " + i);
                    Gdx.app.log("CardStageUI", "Cards in hand: " + cardsInHand);

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
        Gdx.app.log("CardStageUI", "Refreshing card hand with new random cards...");

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
            Gdx.app.log("CardStageUI", "Card " + (i + 1) + " updated to: " + newCardNames[i]);
        }

        // Hide draw button after refresh
        hideDrawButton();

        Gdx.app.log("CardStageUI", "Card hand refreshed successfully");
    }

    private void resolveCardEffect(String cardName) {
        SampleCardHandler card = cardParser.findCardByName(cardName);

        if (card != null) {
            Gdx.app.log("CardEffect", "Processing card: " + card.name + " (Type: " + card.type + ", Effect: " + card.baseEffect + ")");

            if (card.type.equalsIgnoreCase("ATTACK")) {
                int damage = card.baseEffect;

                Gdx.app.log("CardEffect", "Card '" + card.name + "' used! Deals " + damage + " damage to enemy.");

                // Apply damage to enemy
                if (enemy != null && enemy.isAlive()) {
                    enemy.takeDamage(damage);
                    Gdx.app.log("CardEffect", "Enemy HP reduced to: " + enemy.getHp());
                }

            } else if (card.type.equalsIgnoreCase("DEFENSE")) {
                int block = card.baseEffect;

                Gdx.app.log("CardEffect", "Card '" + card.name + "' used! Gains " + block + " block.");

                // Apply block to player
                if (player != null && player.isAlive()) {
                    player.gainBlock(block);
                    Gdx.app.log("CardEffect", "Player block increased to: " + player.getBlock());
                }

            } else if (card.type.equalsIgnoreCase("BUFF")) {
                // Handle buff effects based on tags
                if (card.tags != null && card.tags.contains("INCREASE_ATTACK", false)) {
                    Gdx.app.log("CardEffect", "Card '" + card.name + "' used! Increases attack by " + card.baseEffect);
                    // For now, just log the buff - you can implement attack buffing later
                } else if (card.tags != null && card.tags.contains("APPLY_STATS", false)) {
                    Gdx.app.log("CardEffect", "Card '" + card.name + "' used! Applies stat buff: " + card.baseEffect);
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

            // Check for poison application in card description or tags
            if (card.description != null && card.description.toLowerCase().contains("poison")) {
                int poisonPower = 5; // Default poison power
                int poisonDuration = 3; // Default duration

                // Try to extract poison power from description
                if (card.description.contains("apply") && card.description.contains("poison")) {
                    // Look for numbers in the description
                    String[] words = card.description.split("\\s+");
                    for (int i = 0; i < words.length; i++) {
                        if (words[i].toLowerCase().contains("poison") && i > 0) {
                            try {
                                poisonPower = Integer.parseInt(words[i-1]);
                                break;
                            } catch (NumberFormatException e) {
                                // Use default value
                            }
                        }
                    }
                }

                Gdx.app.log("CardEffect", "Card '" + card.name + "' applies " + poisonPower + " poison for " + poisonDuration + " turns.");

                // Apply poison to enemy
                if (enemy != null && enemy.isAlive()) {
                    Poison poison = new Poison("Poison", poisonPower, poisonDuration);
                    enemy.addPoison(poison);
                    Gdx.app.log("CardEffect", "Enemy now has " + enemy.getPoisonEffects().size() + " poison effects");
                }
            }

        } else {
            Gdx.app.log("CardEffect", "No such card found: " + cardName);
        }
    }
}
