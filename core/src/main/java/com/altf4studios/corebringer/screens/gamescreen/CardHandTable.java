package com.altf4studios.corebringer.screens.gamescreen;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class CardHandTable {
    public final Table cardTable;
    public final Label[] cardLabels;

    public CardHandTable(Skin skin, float cardWidth, float cardHeight, String[] cardNames) {
        cardTable = new Table();
        cardTable.bottom();
        cardTable.defaults().space(15).pad(5).fill().uniform();
        cardLabels = new Label[5];
        for (int i = 0; i < 5; i++) {
            cardLabels[i] = new Label(cardNames[i], skin);
            cardLabels[i].setAlignment(Align.center);
            cardLabels[i].setFontScale(1.5f);
            cardTable.add(cardLabels[i]).height(cardHeight).width(cardWidth);
        }
        cardTable.padTop(20);
    }
} 