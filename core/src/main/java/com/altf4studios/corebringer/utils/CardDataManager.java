package com.altf4studios.corebringer.utils;

import com.altf4studios.corebringer.screens.gamescreen.SampleCardHandler;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Singleton manager that builds O(1) lookup maps for cards loaded by CardParser.
 * Provides fast access by ID and by name, plus convenience getters (cost, type, etc.).
 */
public class CardDataManager {
    private static CardDataManager instance;

    private final ObjectMap<String, SampleCardHandler> byId = new ObjectMap<>();
    private final ObjectMap<String, SampleCardHandler> byName = new ObjectMap<>();
    private boolean initialized = false;

    private CardDataManager() {}

    public static CardDataManager getInstance() {
        if (instance == null) instance = new CardDataManager();
        return instance;
    }

    /**
     * Initialize maps from CardParser data. Safe to call multiple times; it rebuilds the maps.
     */
    public void initFrom(CardParser parser) {
        byId.clear();
        byName.clear();
        if (parser != null && parser.isCardsLoaded()) {
            Array<SampleCardHandler> all = parser.getAllCards();
            for (SampleCardHandler c : all) {
                if (c == null) continue;
                if (c.id != null && c.id.length() > 0) byId.put(c.id, c);
                if (c.name != null && c.name.length() > 0) byName.put(c.name, c);
            }
            initialized = true;
        } else {
            initialized = false;
        }
    }

    public boolean isInitialized() {
        return initialized && byId.size > 0;
    }

    public SampleCardHandler getById(String id) {
        return id == null ? null : byId.get(id);
    }

    public SampleCardHandler getByName(String name) {
        return name == null ? null : byName.get(name);
    }

    public int getCostByName(String name) {
        SampleCardHandler c = getByName(name);
        return c != null ? c.cost : 0;
    }

    public int getCostById(String id) {
        SampleCardHandler c = getById(id);
        return c != null ? c.cost : 0;
    }
}
