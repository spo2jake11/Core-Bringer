package com.altf4studios.corebringer.screens.gamescreen;

import com.altf4studios.corebringer.utils.CardParser;
import com.altf4studios.corebringer.utils.CardDataManager;
import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.entities.Enemy;
// import com.altf4studios.corebringer.status.Poison; // commented out per request
import com.altf4studios.corebringer.turns.TurnManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.altf4studios.corebringer.screens.GameScreen;

public class CardStageUI {
    private Stage cardStage;
    private Skin skin;
    private CardParser cardParser;
    private CardDataManager cardDataManager;
    private CardHandTable cardHandTable;
    private TextButton drawButton;
    private Array<String> availableCardNames;
    private Array<String> drawPoolNames;
    private Player player;
    private Enemy enemy;
    private TurnManager turnManager;
    private GameScreen gameScreen;
    private Array<String> discardPile;
    private float cardWidth, cardHeight;

    public CardStageUI(Stage cardStage, Skin skin, CardParser cardParser, Player player, Enemy enemy, TurnManager turnManager, GameScreen gameScreen) {
        this.cardStage = cardStage;
        this.skin = skin;
        this.player = player;
        this.enemy = enemy;
        this.cardParser = cardParser;
        this.cardDataManager = CardDataManager.getInstance();
        this.turnManager = turnManager;
        this.gameScreen = gameScreen;
        this.discardPile = new Array<>();
        calculateDimensions();
        setupCardUI();
    }

    private void calculateDimensions() {
        float worldWidth = Gdx.graphics.getWidth();
        float worldHeight = Gdx.graphics.getHeight();
        this.cardWidth = (worldWidth * 0.8f) / 5;
        this.cardHeight = worldHeight * 0.2f;
    }

    private int[] getCardCosts(String[] cardNames) {
        int[] costs = new int[cardNames.length];
        for (int i = 0; i < cardNames.length; i++) {
            int cost = 0;
            if (cardDataManager != null && cardDataManager.isInitialized()) {
                cost = cardDataManager.getCostByName(cardNames[i]);
            } else if (cardParser != null) {
                SampleCardHandler card = cardParser.findCardByName(cardNames[i]);
                cost = (card != null) ? card.cost : 0;
            }
            costs[i] = cost;
        }
        return costs;
    }

    private void setupCardUI() {
        initializeDrawPoolFromSave();
        createNewHand();
        createDrawButton();
        // Place the End Turn button inside the visible area (bottom-right)
        float sw = cardStage.getViewport().getScreenWidth();
        float sh = cardStage.getViewport().getScreenHeight();
        if (drawButton.getWidth() <= 0f || drawButton.getHeight() <= 0f) {
            drawButton.setSize(200f, 50f);
        }
        drawButton.setPosition(sw - drawButton.getWidth() - 40f, 40f);
        cardStage.addActor(drawButton);
        showCards();
    }

    private void createNewHand() {
        String[] cardNames = getCardNames();
        int[] cardCosts = getCardCosts(cardNames);
        cardHandTable = new CardHandTable(skin, cardWidth, cardHeight, cardNames, cardCosts);
        cardStage.addActor(cardHandTable.cardGroup);
        // Ensure initial layout uses the stage's viewport for proper centering
        cardHandTable.updateHandLayout();
        // Also schedule a layout on the next frame to account for any late viewport sizing
        cardStage.addAction(Actions.run(() -> cardHandTable.updateHandLayout()));
        setupCardListeners();
        logCounts("After createNewHand");
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
        if (savedIds != null && savedIds.length > 0) {
            for (String id : savedIds) {
                SampleCardHandler c = null;
                if (cardDataManager != null && cardDataManager.isInitialized()) {
                    c = cardDataManager.getById(id);
                }
                if (c == null && cardParser != null && cardParser.isCardsLoaded()) {
                    c = cardParser.findCardById(id);
                }
                if (c != null && c.name != null) {
                    drawPoolNames.add(c.name); // duplicates preserved
                }
            }
        }
        // Do NOT fallback to all cards; keep pool empty if player has no saved cards
        if (drawPoolNames.size == 0) {
            Gdx.app.log("CardStageUI", "Saved deck empty or invalid. No fallback to all cards.");
        }
        // Start with draw pool as the available pool; we remove as we draw
        availableCardNames.addAll(drawPoolNames);
    }

    private String[] getCardNames() {
        String[] cardNames = new String[5];


        for (int i = 0; i < 5; i++) {
            // If deck is empty, shuffle discard pile back into deck
            if (availableCardNames.size == 0 && discardPile.size > 0) {
                availableCardNames.addAll(discardPile);
                discardPile.clear();
            }

            if (availableCardNames.size > 0) {
                int randomIndex = (int) (Math.random() * availableCardNames.size);
                cardNames[i] = availableCardNames.get(randomIndex);
                availableCardNames.removeIndex(randomIndex);
            } else {
                cardNames[i] = "Corrupted Card";
            }
        }

        return cardNames;
    }

    private void setupCardListeners() {
        for (ImageButton card : cardHandTable.handCards) {
            float originalWidth = card.getWidth();
            float originalHeight = card.getHeight();
            card.clearListeners();

            card.addListener(new InputListener(){
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    card.setSize(originalWidth * 1.2f, originalHeight * 1.2f);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    card.setSize(originalWidth, originalHeight);
                }
            });

            card.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    handleCardClick(card);
                }
            });
        }
    }

    private void handleCardClick(ImageButton card) {
        SampleCardHandler cardInfo = null;
        if (cardDataManager != null && cardDataManager.isInitialized()) {
            cardInfo = cardDataManager.getByName(card.getName());
        }
        if (cardInfo == null && cardParser != null) {
            cardInfo = cardParser.findCardByName(card.getName());
        }
        int cost = (cardInfo != null) ? cardInfo.cost : 0;

        if (gameScreen.getEnergy() < cost) {
            showNoEnergyDialog();
            return;
        }

        gameScreen.addEnergy(-cost);
        resolveCardEffect(card.getName());
        discardPile.add(card.getName());
        cardHandTable.removeCardFromHand(card);
    }

    private void showNoEnergyDialog() {
        Dialog dialog = new Dialog("No Energy", skin) {
            @Override
            protected void result(Object object) {
                this.hide();
            }
        };
        dialog.text("No energy! Please recharge!");
        dialog.button("OK");
        dialog.show(cardStage);
    }

    private void createDrawButton() {
        drawButton = new TextButton("End Turn", skin);
        drawButton.setVisible(true);
        drawButton.setColor(Color.GOLD);
        drawButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hideCards();
                cardHandTable.flushHand(discardPile);
                scheduleCardShow();
                Gdx.app.log("Discard Cards", discardPile.toString() + " Total: " + discardPile.size);
                logCounts("After end turn (flushed)");
            }
        });
    }

    // Programmatically trigger the same flow as pressing the End Turn button
    public void endTurnProgrammatically() {
        hideCards();
        if (cardHandTable != null) {
            cardHandTable.flushHand(discardPile);
        }
        scheduleCardShow();
        Gdx.app.log("Discard Cards", discardPile.toString() + " Total: " + discardPile.size);
        logCounts("After end turn (flushed)");
    }

    private void scheduleCardShow() {
        if (turnManager != null) {
            turnManager.endPlayerTurn();
        }

        // Sequence: 2s delay -> enemy acts -> 2s delay -> draw new hand & player's turn visuals
        cardStage.addAction(Actions.sequence(
            Actions.delay(2.0f),
            Actions.run(() -> {
                if (gameScreen != null && gameScreen.getBattleManager() != null) {
                    gameScreen.getBattleManager().executeEnemyTurn();
                }
            }),
            Actions.delay(2.0f),
            Actions.run(() -> {
                createNewHand();
                showCards();
                logCounts("After new hand drawn");
            })
        ));
    }

    public void hideCards() {
        if (cardHandTable != null) {
            cardHandTable.cardGroup.setVisible(false);
        }
        drawButton.setVisible(false);
    }

    public void showCards() {
        if (cardHandTable != null) {
            cardHandTable.cardGroup.setVisible(true);
        }
        drawButton.setVisible(true);
    }

    public CardHandTable getCardHandTable() {
        return cardHandTable;
    }

    private void resolveCardEffect(String cardName) {
        SampleCardHandler card = null;
        if (cardDataManager != null && cardDataManager.isInitialized()) {
            card = cardDataManager.getByName(cardName);
        }
        if (card == null && cardParser != null) {
            card = cardParser.findCardByName(cardName);
        }

        if (card == null) {
            Gdx.app.log("CardEffect", "No such card found: " + cardName);
            return;
        }

        // Primary effect by card type
        if (card.type.equalsIgnoreCase("ATTACK")) {
            handleAttack(card);
        } else if (card.type.equalsIgnoreCase("DEFENSE")) {
            handleDefense(card);
        } else if (card.type.equalsIgnoreCase("BUFF")) {
            handleBuff(card);
        } else if (card.type.equalsIgnoreCase("DEBUFF")) {
            handleDebuff(card);
        } else {
            Gdx.app.log("CardEffect", "Unknown card type: " + card.type);
        }

        // Secondary effects parsed from description (poison, heal, bleed, stun)
        applySecondaryEffects(card);

        // End player turn
        if (turnManager != null && turnManager.isPlayerTurn()) {
            turnManager.endPlayerTurn();
        }
    }

    // -- Helpers ------------------------------------------------------------

    private boolean hasKeyword(String description, String keyword) {
        return description != null && description.toLowerCase().contains(keyword.toLowerCase());
    }

    private void handleAttack(SampleCardHandler card) {
        int damage = card.baseEffect;
        if (enemy != null && enemy.isAlive()) {
            enemy.takeDamage(damage);
        }
        // Hybrid: attack + shield from description
        if (hasKeyword(card.description, "gain") && hasKeyword(card.description, "shield")) {
            int shieldFromDesc = parseShieldFromDescription(card.description);
            if (shieldFromDesc > 0 && player != null && player.isAlive()) {
                player.gainBlock(shieldFromDesc);
            }
        }
    }

    private void handleDefense(SampleCardHandler card) {
        int shieldFromDesc = parseShieldFromDescription(card.description);
        int block = shieldFromDesc > 0 ? shieldFromDesc : card.baseEffect;
        if (player != null && player.isAlive()) {
            player.gainBlock(block);
        }
        // Hybrid: defense + attack from description
        if (hasKeyword(card.description, "deal") || hasKeyword(card.description, "damage")) {
            int damage = card.baseEffect;
            if (damage > 0 && enemy != null && enemy.isAlive()) {
                enemy.takeDamage(damage);
            }
        }
    }

    private void handleBuff(SampleCardHandler card) {
        if (card.tags != null && card.tags.contains("INCREASE_ATTACK", false)) {
            // TODO: implement attack buff
        } else if (card.tags != null && card.tags.contains("APPLY_STATS", false)) {
            // TODO: implement stat buff
        } else if (hasKeyword(card.description, "heal")) {
            int healAmount = parseNumberBeforeKeyword(card.description, "heal");
            if (healAmount <= 0) healAmount = Math.max(0, card.baseEffect);
            Gdx.app.log("CardEffect", "Card '" + card.name + "' used! Heal: " + healAmount + " HP");
        } else {
            Gdx.app.log("CardEffect", "Card '" + card.name + "' used! Generic buff effect: " + card.baseEffect);
        }
    }

    private void handleDebuff(SampleCardHandler card) {
        Gdx.app.log("CardEffect", "Card '" + card.name + "' used! Applies debuff: " + card.baseEffect);
        // TODO: implement concrete debuff logic via tags when available
    }

    private void applySecondaryEffects(SampleCardHandler card) {
        /* // Poison (commented out per request)
        if (hasKeyword(card.description, "poison")) {
            int poisonStacks = 0;
            String[] words = card.description.split("\\s+");
            for (int i = 0; i < words.length; i++) {
                if (words[i].toLowerCase().startsWith("poison")) {
                    if (i > 0) {
                        try { poisonStacks = Integer.parseInt(words[i - 1]); } catch (NumberFormatException ignored) {}
                    }
                    break;
                }
            }
            if (poisonStacks <= 0) poisonStacks = Math.max(0, card.baseEffect > 0 ? card.baseEffect : 5);
            if (enemy != null && enemy.isAlive()) {
                Poison poison = new Poison("Poison", poisonStacks, 3);
                enemy.addPoison(poison);
            }
        }
        */

        // Heal (by description or by card ID as fallback)
        if (hasKeyword(card.description, "heal") || (card.id != null && card.id.toLowerCase().contains("heal"))) {
            int healAmount = parseNumberBeforeKeyword(card.description, "heal");
            if (healAmount <= 0) healAmount = Math.max(0, card.baseEffect);
            if (player != null && player.isAlive() && healAmount > 0) {
                // Instant heal on use
                player.heal(healAmount);
                Gdx.app.log("CardEffect", "Healed " + healAmount + " HP using '" + card.name + "'");
            }
        }

        /* // Bleed (commented out per request)
        if (hasKeyword(card.description, "bleed")) {
            int bleedStacks = parseNumberBeforeKeyword(card.description, "bleed");
            if (bleedStacks <= 0) bleedStacks = Math.max(0, card.baseEffect);
            if (enemy != null && enemy.isAlive() && bleedStacks > 0) {
                com.altf4studios.corebringer.status.StatusManager.getInstance()
                    .applyStatus(enemy, new com.altf4studios.corebringer.status.Bleed("Bleed", bleedStacks, 5));
            }
        }
        */

        /* // Stun (commented out per request)
        if (hasKeyword(card.description, "stun")) {
            int stunDuration = parseNumberBeforeKeyword(card.description, "stun");
            if (stunDuration <= 0) stunDuration = Math.max(0, card.baseEffect);
            if (enemy != null && enemy.isAlive() && stunDuration > 0) {
                enemy.addStatus("Stun", stunDuration);
            }
        }
        */
    }

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

    private void logCounts(String context) {
        int deck = (availableCardNames != null) ? availableCardNames.size : 0;
        int discard = (discardPile != null) ? discardPile.size : 0;
        int hand = (cardHandTable != null && cardHandTable.handCards != null) ? cardHandTable.handCards.size() : 0;
        int total = deck + hand + discard;
        Gdx.app.log("DeckCount", context + " | Deck:" + deck + " Hand:" + hand + " Discard:" + discard + " Total:" + total);
    }

    public void dispose() {
        try {
            if (cardStage != null) {
                cardStage.clear();
            }
        } catch (Exception e) {
            Gdx.app.error("CardStageUI", "Error while clearing cardStage: " + e.getMessage());
        }

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
