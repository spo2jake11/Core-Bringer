package com.altf4studios.corebringer.screens.gamescreen;

import com.altf4studios.corebringer.utils.CardParser;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class CardStageUI {
    private Stage cardStage;
    private Skin skin;
    private CardParser cardParser;
    private CardSlotTable cardSlotTable;
    private CardHandTable cardHandTable;
    private CardDragAndDrop cardDragAndDrop;
    
    public CardStageUI(Stage cardStage, Skin skin, CardParser cardParser) {
        this.cardStage = cardStage;
        this.skin = skin;
        this.cardParser = cardParser;
        setupCardUI();
        Gdx.app.log("CardStageUI", "Card stage UI initialized successfully");
    }
    
    private void setupCardUI() {
        // Calculate responsive dimensions
        float worldWidth = Gdx.graphics.getWidth();
        float worldHeight = Gdx.graphics.getHeight();
        float cardHeight = worldHeight * 0.2f;
        float cardWidth = (worldWidth * 0.8f) / 5;

        // Create slot table
        cardSlotTable = new CardSlotTable(skin, cardWidth);
        
        // Get card names
        String[] cardNames = getCardNames();
        
        // Create card hand table
        cardHandTable = new CardHandTable(skin, cardWidth, cardHeight, cardNames);
        
        // Setup drag and drop
        Label[] slotLabels = {cardSlotTable.slotBasic, cardSlotTable.slotCombine, cardSlotTable.slotExtend};
        cardDragAndDrop = new CardDragAndDrop(cardHandTable.cardLabels, slotLabels, skin);
        
        // Add click listeners to cards
        setupCardClickListeners();
        
        // Create parent table
        Table parentTable = new Table();
        parentTable.setFillParent(true);
        parentTable.top();
        parentTable.add(cardSlotTable.slotTable).expandX().padTop(10).row();
        parentTable.add(cardHandTable.cardTable).expandX().padTop(10);
        
        cardStage.addActor(parentTable);
    }
    
    private String[] getCardNames() {
        Array<String> cardNames = new Array<>();
        Array<SampleCardHandler> allCards;
        
        if (cardParser.isCardsLoaded()) {
            allCards = cardParser.getAllCards();
            int cardCount = Math.min(5, allCards.size);

            Array<String> availableCardNames = new Array<>();
            for (SampleCardHandler card : allCards) {
                availableCardNames.add(card.name);
            }

            for (int i = 0; i < cardCount; i++) {
                if (availableCardNames.size > 0) {
                    int randomIndex = (int) (Math.random() * availableCardNames.size);
                    cardNames.add(availableCardNames.get(randomIndex));
                    Gdx.app.log("Card Loaded", availableCardNames.get(randomIndex));
                    availableCardNames.removeIndex(randomIndex);
                }
            }
        } else {
            cardNames.add("Card 1");
            cardNames.add("Card 2");
            cardNames.add("Card 3");
            cardNames.add("Card 4");
            cardNames.add("Card 5");
            Gdx.app.error("CardStage", "Failed to load cards, using fallback names");
        }
        
        return cardNames.toArray(String.class);
    }
    
    private void setupCardClickListeners() {
        for (Label cardLabel : cardHandTable.cardLabels) {
            cardLabel.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Gdx.app.log("Card Used: ", cardLabel.getText() + " was used");
                }
            });
        }
    }
    
    public CardSlotTable getCardSlotTable() {
        return cardSlotTable;
    }
    
    public CardHandTable getCardHandTable() {
        return cardHandTable;
    }
    
    public CardDragAndDrop getCardDragAndDrop() {
        return cardDragAndDrop;
    }
    
    public void refreshCardHand() {
        Gdx.app.log("CardStageUI", "Refreshing card hand with new random cards...");
        
        // Get new random card names
        String[] newCardNames = getCardNames();
        
        // Update card labels with new names
        for (int i = 0; i < cardHandTable.cardLabels.length && i < newCardNames.length; i++) {
            cardHandTable.cardLabels[i].setText(newCardNames[i]);
            Gdx.app.log("CardStageUI", "Card " + (i + 1) + " updated to: " + newCardNames[i]);
        }
        
        // Re-setup drag and drop with new cards
        if (cardDragAndDrop != null) {
            Label[] slotLabels = {cardSlotTable.slotBasic, cardSlotTable.slotCombine, cardSlotTable.slotExtend};
            cardDragAndDrop = new CardDragAndDrop(cardHandTable.cardLabels, slotLabels, skin);
        }
        
        // Re-setup click listeners
        setupCardClickListeners();
        
        Gdx.app.log("CardStageUI", "Card hand refreshed successfully");
    }
} 