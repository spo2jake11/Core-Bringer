package com.altf4studios.corebringer.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.ArrayList;
import java.util.Arrays;

public class CardHandTable {
    public final Table cardTable;
    public final Table[] cardTables; // Each card's container
    public final ImageButton[] cardImageButtons;
    public final Label[] cardCostLabels;
    private final TextureAtlas cardAtlas;

    public final Group cardGroup;
    public final ArrayList<ImageButton> handCards;

    public CardHandTable(Skin skin, float cardWidth, float cardHeight, String[] cardNames, int[] cardCosts) {
        // Load card atlas
        cardGroup = new Group();
        handCards = new ArrayList<>();

        cardAtlas = new TextureAtlas("assets/cards/cards_atlas.atlas");

        cardTable = new Table();
        cardTable.bottom();
        cardTable.defaults().space(15).pad(5).fill().uniform();
        cardTables = new Table[5];
        cardImageButtons = new ImageButton[5];
        cardCostLabels = new Label[5];


        //This opens up to read the JSON file containing card data
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(Gdx.files.internal("assets/cards.json"));

        //This here creates a map of card IDs to their atlas names
        ObjectMap<String, String> idToAtlasName = new ObjectMap<>();
        for(JsonValue card : root.get("cards")){
            String id = card.getString("name");
            String atlas = card.getString("atlasName");
            idToAtlasName.put(id, atlas);
        }

        ObjectMap<String, String> nameToId = new ObjectMap<>();
        for(JsonValue card : root.get("cards")){
            String id = card.getString("name");
            String displayName = card.getString("id");
            nameToId.put(displayName, id);
        }

        Gdx.app.log("Cards Loaded", Arrays.toString(cardNames));

        for (int i = 0; i < 5; i++) {
            cardTables[i] = new Table();

            String atlasName = idToAtlasName.get(cardNames[i]);
            String newCard = atlasName.replace(" ", "_");

            Gdx.app.log("New Card", newCard);

            // Create image button with card texture
            TextureRegionDrawable cardDrawable = getDrawableForCardName(newCard);

            ImageButton.ImageButtonStyle cardStyle = new ImageButton.ImageButtonStyle();
            cardStyle.up = cardDrawable;
            cardStyle.down = cardDrawable;
            cardStyle.over = cardDrawable;

            ImageButton cardButton = new ImageButton(cardStyle);
            cardButton.setSize(200, 250);
            cardButton.setName(cardNames[i]);
            handCards.add(cardButton);
            cardGroup.addActor(cardButton);

        }
        cardTable.padTop(20);
        updateHandLayout();
    }

    public void updateHandLayout(){
        float worldWidth;
        if (cardGroup != null && cardGroup.getStage() != null && cardGroup.getStage().getViewport() != null) {
            worldWidth = cardGroup.getStage().getViewport().getWorldWidth();
        } else {
            worldWidth = Gdx.graphics.getWidth();
        }
        float spacing = 20f; // horizontal gap between cards
        float y = 50; // Fixed Y position from bottom

        // Compute total width of the hand: sum of card widths + gaps
        float totalWidth = 0f;
        for (int i = 0; i < handCards.size(); i++) {
            totalWidth += handCards.get(i).getWidth();
            if (i < handCards.size() - 1) totalWidth += spacing;
        }

        // Left-most X such that the whole hand is horizontally centered
        float startX = (worldWidth - totalWidth) * 0.5f;

        // Place each card sequentially from startX
        float cursorX = startX;
        for (int i = 0; i < handCards.size(); i++) {
            ImageButton card = handCards.get(i);
            card.setPosition(cursorX, y);
            cursorX += card.getWidth();
            if (i < handCards.size() - 1) cursorX += spacing;
        }
    }

    public void addCardToHand(ImageButton card) {
        if (handCards.size() < 5) { // Limit hand size to 5 cards
            handCards.add(card);
            cardGroup.addActor(card);
            updateHandLayout();
        }
    }

    public void removeCardFromHand(ImageButton card) {
        if (handCards.remove(card)) {
            cardGroup.removeActor(card);
            updateHandLayout();
        }
    }

    public TextureRegionDrawable getDrawableForCardName(String cardName) {
        if(cardAtlas != null && cardName != null && !cardName.isEmpty()){
            return new TextureRegionDrawable(cardAtlas.findRegion(cardName));
        }
        // Final fallback
        return new TextureRegionDrawable(cardAtlas.findRegion("bck_card"));
    }

    public void flushHand(Array<String> discardPile){
        for (ImageButton card : handCards) {
            discardPile.add(card.getName());
        }
        handCards.clear();
        cardGroup.clear();
    }

    // CRITICAL: Dispose method to prevent memory leak
    public void dispose() {
        try {
            if (cardAtlas != null) {
                cardAtlas.dispose();
                Gdx.app.log("CardHandTable", "Disposed TextureAtlas to prevent memory leak");
            }
        } catch (Exception e) {
            Gdx.app.error("CardHandTable", "Error disposing TextureAtlas: " + e.getMessage());
        }

        // Clear collections
        if (handCards != null) {
            handCards.clear();
        }
        if (cardGroup != null) {
            cardGroup.clear();
        }
    }
}
