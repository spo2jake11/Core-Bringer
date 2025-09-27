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
import com.badlogic.gdx.utils.Array;
import com.altf4studios.corebringer.screens.GameScreen;

public class CardStageUI {
    private Stage cardStage;
    private Skin skin;
    private CardParser cardParser;
    private CardHandTable cardHandTable;
    // private float worldHeight; // unused
    private boolean[] discardedCards;
    private int cardsInHand;
    private TextButton drawButton;
    private Array<String> availableCardNames;
    private Array<String> drawPoolNames; // names derived from saved deck ids (with duplicates)
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
        // worldHeight = cardStage.getViewport().getWorldHeight();
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
        initializeDrawPoolFromSave();

        // Get card names and costs
        String[] cardNames = getCardNames();
        int[] cardCosts = getCardCosts(cardNames);

        // Create card hand table
        cardHandTable = new CardHandTable(skin, cardWidth, cardHeight, cardNames, cardCosts);

        cardStage.addActor(cardHandTable.cardGroup);

        // Add click and hover listeners to cards
        setupCardListeners();

        // Create draw button (TextButton)
        createDrawButton();

        drawButton.setPosition(cardStage.getViewport().getScreenX() * 0.80f, cardStage.getViewport().getScreenY() * 0.25f);
        cardStage.addActor(drawButton);

//        // Layout: cards in a row, draw button at the right
//        Table rowTable = new Table();
//        for (Table cardTable : cardHandTable.cardTables) {
//            rowTable.add(cardTable).padRight(10f).width(cardWidth).height(cardHeight);
//        }
//        rowTable.add(drawButton).padLeft(20f).width(120f).height(cardHeight).right();

//        // Parent table for positioning
//        Table parentTable = new Table();
//        parentTable.setFillParent(true);
//        parentTable.bottom();
//        parentTable.add(rowTable).expandX().padBottom(worldHeight * 0.25f - 100f);
//
//        cardStage.addActor(parentTable);

        // Ensure cards are visible by default
        showCards();
    }

    private void initializeAvailableCards() {
        // Backward-compatible random pool from all cards if saved deck is not provided
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

    private void initializeDrawPoolFromSave() {
        drawPoolNames = new Array<>();
        availableCardNames = new Array<>();
        // Load from GameScreen saved deck ids
        String[] savedIds = gameScreen != null ? gameScreen.getSavedDeckIds() : null;
        if (savedIds != null && savedIds.length > 0 && cardParser.isCardsLoaded()) {
            for (String id : savedIds) {
                SampleCardHandler c = cardParser.findCardById(id);
                if (c != null) {
                    drawPoolNames.add(c.name); // duplicates preserved
                }
            }
        }
        // Fallback to all cards if saved draw pool is empty
        if (drawPoolNames.size == 0) {
            initializeAvailableCards();
            drawPoolNames.addAll(availableCardNames);
        }
        // Start with draw pool as the available pool; we remove as we draw
        availableCardNames.addAll(drawPoolNames);
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

    private void setupCardListeners() {

        for (ImageButton card : getCardHandTable().handCards) {
            card.clearListeners();
            card.addListener(new InputListener(){
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    card.scaleBy(1.5f);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    card.setScale(1.0f);
                }
            });
            card.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    SampleCardHandler cardInfo = cardParser.findCardByName(card.getName());
                    int cost = (cardInfo != null) ? cardInfo.cost : 0;
                    if (gameScreen.getEnergy() <= cost) {
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
                    resolveCardEffect(card.getName());


                    cardHandTable.removeCardFromHand(card);

                }
            });

        }

//        for (int i = 0; i < cardHandTable.cardImageButtons.length; i++) {
//            final int cardIndex = i;
//            final ImageButton cardButton = cardHandTable.cardImageButtons[i];
//            final String cardName = cardNames[i];
//            cardButton.clearListeners();
//            cardButton.addListener(new InputListener() {
//                @Override
//                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
//                    cardButton.setScale(1.5f);
//                    cardButton.addListener(new ClickListener() {
//                        @Override
//                        public void clicked(InputEvent event, float x, float y) {
//                            SampleCardHandler card = cardParser.findCardByName(cardName);
//                            int cost = (card != null) ? card.cost : 0;
//                            if (gameScreen.getEnergy() < cost) {
//                                Dialog dialog = new Dialog("No Energy", skin) {
//                                    @Override
//                                    protected void result(Object object) {
//                                        this.hide();
//                                    }
//                                };
//                                dialog.text("No energy! Please recharge!");
//                                dialog.button("OK");
//                                dialog.show(cardStage);
//                                return;
//                            }
//                            // Deduct energy
//                            gameScreen.addEnergy(-cost);
//                            // Call the effect resolver before discarding
//                            resolveCardEffect(cardName);
//                            // Discard the card
//                            discardedCards[cardIndex] = true;
//                            cardButton.setColor(0.5f, 0.5f, 0.5f, 0.5f); // Gray out the card
//                            cardsInHand--;
//                            cardButton.clearListeners();
//                            // Check if all cards are used
//                            showDrawButton();
//                        }
//                    });
//                }
//                @Override
//                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
//                    cardButton.setScale(1f);
//                }
//            });
//
//
////            cardButton.addListener(new InputListener() {
////                @Override
////                public boolean mouseMoved(InputEvent event, float x, float y) {
////                    SampleCardHandler card = cardParser.findCardByName(cardName);
////                    if (card != null) {
////                        // Show tooltip or description - you can implement this later
////                        Gdx.app.log("CardHover", card.description != null ? card.description : "No description");
////                    }
////                    return true;
////                }
////                @Override
////                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
////                    // Hide tooltip - you can implement this later
////                }
////            });
//        }
    }

    private void createDrawButton() {
        drawButton = new TextButton("End Turn", skin);
        drawButton.setVisible(true);
        drawButton.setColor(Color.GOLD);
        drawButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Hide cards when player ends turn
                hideCards();

                // End player turn when draw button is pressed
                if (turnManager != null) {
                    turnManager.endPlayerTurn();
                }
                // Execute enemy turn immediately after player ends turn
                if (gameScreen != null && gameScreen.getBattleManager() != null) {
                    gameScreen.getBattleManager().executeEnemyTurn();
                }

                // Schedule cards to be shown after enemy turn with a delay
                scheduleCardShow();
            }
        });
    }

    private void showDrawButton() {
        drawButton.setVisible(true);

    }

    private void hideRechargeButton() {

    }
//    private void hideDrawButton() {
//        drawButton.setVisible(false);
//    }

    public void hideCards() {
        if (cardHandTable != null) {
            for (Table cardTable : cardHandTable.cardTables) {
                cardTable.setVisible(false);
            }
        }
        drawButton.setVisible(false);
    }

    public void showCards() {
        if (cardHandTable != null) {
            for (Table cardTable : cardHandTable.cardTables) {
                cardTable.setVisible(true);
            }
        }
        drawButton.setVisible(true);
    }

    private void scheduleCardShow() {
        // Add a delayed action to show cards after enemy turn
        cardStage.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
            com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(2.0f), // 2 second delay
            com.badlogic.gdx.scenes.scene2d.actions.Actions.run(() -> {
                showCards();
//                refreshCardHand(); // Refresh the card hand when showing
            })
        ));
    }

//    private void drawCard() {
//        for (int i = 0; i < cardHandTable.cardNameButtons.length && cardsInHand < 5 && availableCardNames.size > 0; i++) {
//            if (discardedCards[i] || cardHandTable.cardNameButtons[i].getText().toString().equals("Corrupted Card")) {
//                int randomIndex = (int) (Math.random() * availableCardNames.size);
//                String newCardName = availableCardNames.get(randomIndex);
//                SampleCardHandler card = cardParser.findCardByName(newCardName);
//                int newCost = (card != null) ? card.cost : 0;
//                cardHandTable.cardNameButtons[i].setText(newCardName);
//                cardHandTable.cardNameButtons[i].setColor(1f, 1f, 1f, 1f); // Reset color
//                cardHandTable.cardCostButtons[i].setText("Cost: " + newCost);
//                discardedCards[i] = false;
//                cardsInHand++;
//                availableCardNames.removeIndex(randomIndex);
//            }
//        }
//        // Hide draw button after drawing
//
//    }

    public CardHandTable getCardHandTable() {
        return cardHandTable;
    }

//    public void refreshCardHand() {
//        for (int i = 0; i < discardedCards.length; i++) {
//            discardedCards[i] = false;
//        }
//        cardsInHand = 5;
//        if (availableCardNames.size == 0) {
//            initializeDrawPoolFromSave();
//        }
//        String[] newCardNames = getCardNames();
//        int[] newCardCosts = getCardCosts(newCardNames);
//        for (int i = 0; i < cardHandTable.cardImageButtons.length && i < newCardNames.length; i++) {
//            // Reset card button color
//            cardHandTable.cardImageButtons[i].setColor(1f, 1f, 1f, 1f);
//            // Update cost label
//            //cardHandTable.cardCostLabels[i].setText("Cost: " + newCardCosts[i]);
//        }
//        setupCardListeners();
//    }

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
                    // Apply poison with base duration 3 and additive stacks
                    Poison poison = new Poison("Poison", poisonStacks, 3);
                    enemy.addPoison(poison);
                }
            }

            // Heal handling: parse from description or fallback to baseEffect
            if (card.description != null && card.description.toLowerCase().contains("heal")) {
                int healAmount = parseNumberBeforeKeyword(card.description, "heal");
                if (healAmount <= 0) healAmount = Math.max(0, card.baseEffect);
                if (player != null && player.isAlive() && healAmount > 0) {
                    player.heal(healAmount);
                }
            }

            // Bleed handling: apply simple status using Entity.addStatus as a baseline
            if (card.description != null && card.description.toLowerCase().contains("bleed")) {
                int bleedStacks = parseNumberBeforeKeyword(card.description, "bleed");
                if (bleedStacks <= 0) bleedStacks = Math.max(0, card.baseEffect);
                if (enemy != null && enemy.isAlive() && bleedStacks > 0) {
                    // Route through StatusManager so duration stacks are honored
                    com.altf4studios.corebringer.status.StatusManager.getInstance()
                        .applyStatus(enemy, new com.altf4studios.corebringer.status.Bleed("Bleed", bleedStacks, 5));
                }
            }

            // Stun handling: apply duration as baseEffect or parsed from description
            if (card.description != null && card.description.toLowerCase().contains("stun")) {
                int stunDuration = parseNumberBeforeKeyword(card.description, "stun");
                if (stunDuration <= 0) stunDuration = Math.max(0, card.baseEffect);
                if (enemy != null && enemy.isAlive() && stunDuration > 0) {
                    enemy.addStatus("Stun", stunDuration);
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

    // Extract the integer immediately before a given keyword in a case-insensitive way
    private int parseNumberBeforeKeyword(String text, String keyword) {
        if (text == null || keyword == null) return 0;
        String lower = text.toLowerCase();
        String[] words = lower.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            if (words[i].startsWith(keyword.toLowerCase())) {
                if (i > 0) {
                    try {
                        return Integer.parseInt(words[i - 1].replaceAll("[^0-9]", ""));
                    } catch (NumberFormatException ignored) {
                        return 0;
                    }
                }
                return 0;
            }
        }
        return 0;
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
