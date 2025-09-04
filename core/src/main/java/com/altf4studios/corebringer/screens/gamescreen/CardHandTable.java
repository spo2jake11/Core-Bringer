package com.altf4studios.corebringer.screens.gamescreen;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

public class CardHandTable {
    public final Table cardTable;
    public final Table[] cardTables; // Each card's container
    public final TextButton[] cardNameButtons;
    public final TextButton[] cardCostButtons;

    public CardHandTable(Skin skin, float cardWidth, float cardHeight, String[] cardNames, int[] cardCosts) {
        cardTable = new Table();
        cardTable.bottom();
        cardTable.defaults().space(15).pad(5).fill().uniform();
        cardTables = new Table[5];
        cardNameButtons = new TextButton[5];
        cardCostButtons = new TextButton[5];
        for (int i = 0; i < 5; i++) {
            cardTables[i] = new Table();
            cardNameButtons[i] = new TextButton(cardNames[i], skin);
            cardNameButtons[i].getLabel().setAlignment(Align.center);
            cardNameButtons[i].getLabel().setFontScale(1.5f);
            cardCostButtons[i] = new TextButton("Cost: " + cardCosts[i], skin, "default");
            cardCostButtons[i].getLabel().setFontScale(1.1f);
            cardTables[i].add(cardNameButtons[i]).row();
            cardTables[i].add(cardCostButtons[i]).padTop(10f);
            cardTable.add(cardTables[i]).height(cardHeight).width(cardWidth);
        }
        cardTable.padTop(20);
    }
}
