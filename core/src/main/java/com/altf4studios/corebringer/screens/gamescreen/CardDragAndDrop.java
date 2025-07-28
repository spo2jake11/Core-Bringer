package com.altf4studios.corebringer.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

public class CardDragAndDrop {
    public final DragAndDrop dragAndDrop;

    public CardDragAndDrop(Label[] cardLabels, Label[] slotLabels, Skin skin) {
        dragAndDrop = new DragAndDrop();
        for (Label cardLabel : cardLabels) {
            dragAndDrop.addSource(makeCardSource(cardLabel, skin));
        }
        for (Label slotLabel : slotLabels) {
            dragAndDrop.addTarget(makeSlotTarget(slotLabel));
        }
    }

    private DragAndDrop.Source makeCardSource(final Label cardLabel, final Skin skin) {
        return new DragAndDrop.Source(cardLabel) {
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                DragAndDrop.Payload payload = new DragAndDrop.Payload();
                payload.setObject(cardLabel.getText().toString());
                payload.setDragActor(new Label(cardLabel.getText(), skin));
                return payload;
            }
        };
    }

    private DragAndDrop.Target makeSlotTarget(final Label slotLabel) {
        return new DragAndDrop.Target(slotLabel) {
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                slotLabel.setColor(Color.LIGHT_GRAY);
                return true;
            }
            public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload) {
                slotLabel.setColor(Color.WHITE);
            }
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                String cardName = (String) payload.getObject();
                String slotName = slotLabel.getText().toString();
                Gdx.app.log("DragDrop", "Card '" + cardName + "' dropped on slot '" + slotName + "'");
                System.out.println("Card '" + cardName + "' dropped on slot '" + slotName + "'");
            }
        };
    }
} 