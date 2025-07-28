package com.altf4studios.corebringer.screens.gamescreen;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class CardSlotTable {
    public final Table slotTable;
    public final Label slotBasic;
    public final Label slotCombine;
    public final Label slotExtend;

    public CardSlotTable(Skin skin, float cardWidth) {
        slotTable = new Table();
        slotTable.top();
        slotTable.setFillParent(false);
        slotTable.padTop(10);
        slotTable.defaults().space(30).pad(10).fill().uniform();
        slotBasic = new Label("Basic", skin);
        slotCombine = new Label("Combine", skin);
        slotExtend = new Label("Extend", skin);
        slotBasic.setAlignment(Align.center);
        slotCombine.setAlignment(Align.center);
        slotExtend.setAlignment(Align.center);
        slotTable.add(slotBasic).width(cardWidth).height(60);
        slotTable.add(slotCombine).width(cardWidth).height(60);
        slotTable.add(slotExtend).width(cardWidth).height(60);
    }
} 