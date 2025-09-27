package com.altf4studios.corebringer.cards;

import com.badlogic.gdx.utils.Array;

public class CardDeckService {
    public String[] drawHand(Array<String> availableNames, int handSize) {
        String[] result = new String[handSize];
        for (int i = 0; i < handSize; i++) {
            if (availableNames.size > 0) {
                int idx = (int) (Math.random() * availableNames.size);
                result[i] = availableNames.get(idx);
                availableNames.removeIndex(idx);
            } else {
                result[i] = "Corrupted Card";
            }
        }
        return result;
    }
}



