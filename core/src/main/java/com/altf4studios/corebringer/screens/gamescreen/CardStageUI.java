package com.altf4studios.corebringer.screens.gamescreen;

import com.altf4studios.corebringer.utils.CardParser;
import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.entities.Enemy;
import com.altf4studios.corebringer.status.Poison;
import com.altf4studios.corebringer.turns.TurnManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
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
    private TextButton drawButton;
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

    private int[] getCardCosts(String[] cardNames) {
        int[] costs = new int[cardNames.length];
        for (int i = 0; i < cardNames.length; i++) {
            SampleCardHandler card = cardParser.findCardByName(cardNames[i]);
            costs[i] = (card != null) ? card.cost : 0;
        }
        return costs;
    }

    private void setupCardUI() {
        // Calculate responsive dimensions
        float worldWidth = Gdx.graphics.getWidth();
        float worldHeight = Gdx.graphics.getHeight();
        float cardHeight = worldHeight * 0.2f;
        float cardWidth = (worldWidth * 0.8f) / 5;

        // Initialize available card names
        initializeAvailableCards();

        // Get card names and costs
        String[] cardNames = getCardNames();
        int[] cardCosts = getCardCosts(cardNames);

        // Create card hand table
        cardHandTable = new CardHandTable(skin, cardWidth, cardHeight, cardNames, cardCosts);

        // Add click and hover listeners to cards
        setupCardListeners(cardNames, cardCosts);

        // Create draw button (TextButton)
        createDrawButton();

        // Layout: cards in a row, draw button at the right
        Table rowTable = new Table();
        for (Table cardTable : cardHandTable.cardTables) {
            rowTable.add(cardTable).padRight(10f).width(cardWidth).height(cardHeight);
        }
        rowTable.add(drawButton).padLeft(20f).width(120f).height(cardHeight).right();

        // Parent table for positioning
        Table parentTable = new Table();
        parentTable.setFillParent(true);
        parentTable.bottom();
        parentTable.add(rowTable).expandX().padBottom(worldHeight * 0.55f - 100f);

        cardStage.addActor(parentTable);
        hideDrawButton(); // Hide by default
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

    private void setupCardListeners(String[] cardNames, int[] cardCosts) {
        for (int i = 0; i < cardHandTable.cardNameButtons.length; i++) {
            final int cardIndex = i;
            final TextButton cardButton = cardHandTable.cardNameButtons[i];
            final String cardName = cardNames[i];
            final int cardCost = cardCosts[i];
            cardButton.clearListeners();
            cardButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
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
                    // Deduct energy
                    gameScreen.addEnergy(-cost);
                    // Call the effect resolver before discarding
                    resolveCardEffect(cardName);
                    // Discard the card
                    discardedCards[cardIndex] = true;
                    cardButton.setText("Discarded");
                    cardButton.setColor(0.5f, 0.5f, 0.5f, 0.5f); // Gray out the card
                    cardsInHand--;
                    // Check if all cards are used
                    if (cardsInHand == 0) {
                        showDrawButton();
                    } else {
                        hideDrawButton();
                    }
                }
            });
            cardButton.addListener(new InputListener() {
                @Override
                public boolean mouseMoved(InputEvent event, float x, float y) {
                    SampleCardHandler card = cardParser.findCardByName(cardName);
                    if (card != null) {
                        cardButton.setText(card.description != null ? card.description : "No description");
                    }
                    return true;
                }
                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    cardButton.setText(cardName);
                }
            });
        }
    }

    private void createDrawButton() {
        drawButton = new TextButton("Draw", skin);
        drawButton.setVisible(false);
        drawButton.setColor(Color.GOLD);
        drawButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                drawCard();
                refreshCardHand();
            }
        });
    }

    private void showDrawButton() {
        drawButton.setVisible(true);

    }

    private void hideDrawButton() {
        drawButton.setVisible(false);
    }

    private void drawCard() {
        for (int i = 0; i < cardHandTable.cardNameButtons.length && cardsInHand < 5 && availableCardNames.size > 0; i++) {
            if (discardedCards[i] || cardHandTable.cardNameButtons[i].getText().toString().equals("Corrupted Card")) {
                int randomIndex = (int) (Math.random() * availableCardNames.size);
                String newCardName = availableCardNames.get(randomIndex);
                SampleCardHandler card = cardParser.findCardByName(newCardName);
                int newCost = (card != null) ? card.cost : 0;
                cardHandTable.cardNameButtons[i].setText(newCardName);
                cardHandTable.cardNameButtons[i].setColor(1f, 1f, 1f, 1f); // Reset color
                cardHandTable.cardCostButtons[i].setText("Cost: " + newCost);
                discardedCards[i] = false;
                cardsInHand++;
                availableCardNames.removeIndex(randomIndex);
            }
        }
        // Hide draw button after drawing
        hideDrawButton();
    }

    public CardHandTable getCardHandTable() {
        return cardHandTable;
    }

    public void refreshCardHand() {
        for (int i = 0; i < discardedCards.length; i++) {
            discardedCards[i] = false;
        }
        cardsInHand = 5;
        if (availableCardNames.size == 0) {
            initializeAvailableCards();
        }
        String[] newCardNames = getCardNames();
        int[] newCardCosts = getCardCosts(newCardNames);
        for (int i = 0; i < cardHandTable.cardNameButtons.length && i < newCardNames.length; i++) {
            cardHandTable.cardNameButtons[i].setText(newCardNames[i]);
            cardHandTable.cardNameButtons[i].setColor(1f, 1f, 1f, 1f); // Reset color
            cardHandTable.cardCostButtons[i].setText("Cost: " + newCardCosts[i]);
        }
        setupCardListeners(newCardNames, newCardCosts);
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

    /**
     * Clean up internal references and UI actors. Do NOT dispose the stage here because the
     * GameScreen owns and will dispose the Stage. This prevents double-dispose issues.
     */
    public void dispose() {
        try {
            if (cardStage != null) {
                cardStage.clear();
            }
        } catch (Exception e) {
            Gdx.app.error("CardStageUI", "Error while clearing cardStage: " + e.getMessage());
        }

        // Null out references to help GC
        skin = null;
        cardParser = null;
        cardHandTable = null;
        availableCardNames = null;
        player = null;
        enemy = null;
        turnManager = null;
        gameScreen = null;
        drawButton = null;
    }
}
