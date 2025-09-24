package com.altf4studios.corebringer.screens.gamescreen;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

public class CardHandTable {
    public final Table cardTable;
    public final Table[] cardTables; // Each card's container
    public final ImageButton[] cardImageButtons;
    public final Label[] cardCostLabels;
    private TextureAtlas cardAtlas;

    public CardHandTable(Skin skin, float cardWidth, float cardHeight, String[] cardNames, int[] cardCosts) {
        // Load card atlas
        cardAtlas = new TextureAtlas("assets/cards/cards_atlas.atlas");

        cardTable = new Table();
        cardTable.bottom();
        cardTable.defaults().space(15).pad(5).fill().uniform();
        cardTables = new Table[5];
        cardImageButtons = new ImageButton[5];
        cardCostLabels = new Label[5];

        for (int i = 0; i < 5; i++) {
            cardTables[i] = new Table();

            // Create image button with card texture
            TextureRegionDrawable cardDrawable = getDrawableForCardName(cardNames[i]);

            ImageButton.ImageButtonStyle cardStyle = new ImageButton.ImageButtonStyle();
            cardStyle.up = cardDrawable;
            cardStyle.down = cardDrawable;
            cardStyle.over = cardDrawable;

            cardImageButtons[i] = new ImageButton(cardStyle);
            cardImageButtons[i].setSize(100, 200); // Set size as requested

            // Create cost label
            //cardCostLabels[i] = new Label("Cost: " + cardCosts[i], skin);
            //cardCostLabels[i].setAlignment(Align.center);
            //cardCostLabels[i].setFontScale(1.1f);

            cardTables[i].add(cardImageButtons[i]).size(200, 250).row();
            //cardTables[i].add(cardCostLabels[i]).padTop(10f);
            cardTable.add(cardTables[i]).height(cardHeight).width(cardWidth);
        }
        cardTable.padTop(20);
    }

    private String getAtlasBaseName(String cardNameLower) {
        if (cardNameLower.contains("shield")) {
            return "Final_Shield";
        } else if (cardNameLower.contains("heal")) {
            return "Heal_Package";
        } else if (cardNameLower.contains("poison") || cardNameLower.contains("bite")) {
            return "Looping_Bite";
        } else if (cardNameLower.contains("variable") || cardNameLower.contains("slash")) {
            return "Variable_Slash";
        } else if (cardNameLower.contains("function") || cardNameLower.contains("logic") || cardNameLower.contains("break")) {
            return "Logic_Break";
        } else if (cardNameLower.contains("memory") || cardNameLower.contains("leak")) {
            return "Memory_Leak";
        }
        return null;
    }

    private int extractLevelSuffix(String cardNameLower) {
        // Try to find a trailing number in the name; default to 1
        int level = 1;
        for (int i = cardNameLower.length() - 1; i >= 0; i--) {
            char ch = cardNameLower.charAt(i);
            if (Character.isDigit(ch)) {
                level = Character.digit(ch, 10);
                break;
            }
            if (Character.isLetter(ch)) {
                // stop if letters after digits not found
                break;
            }
        }
        if (level < 1 || level > 5) level = 1;
        return level;
    }

    public TextureRegionDrawable getDrawableForCardName(String cardName) {
        if (cardAtlas == null) {
            return new TextureRegionDrawable(cardAtlas.findRegion("bck_card"));
        }
        String lower = cardName == null ? "" : cardName.toLowerCase();
        String base = getAtlasBaseName(lower);
        int level = extractLevelSuffix(lower);
        if (base != null) {
            String candidate = base + level; // e.g., Variable_Slash1
            if (cardAtlas.findRegion(candidate) != null) {
                return new TextureRegionDrawable(cardAtlas.findRegion(candidate));
            }
            // Fallback to level 1 for this base
            String fallback = base + "1";
            if (cardAtlas.findRegion(fallback) != null) {
                return new TextureRegionDrawable(cardAtlas.findRegion(fallback));
            }
        }
        // Final fallback
        return new TextureRegionDrawable(cardAtlas.findRegion("bck_card"));
    }

    public void dispose() {
        if (cardAtlas != null) {
            cardAtlas.dispose();
        }
    }
}
