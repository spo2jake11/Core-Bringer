package com.altf4studios.corebringer.utils;

import com.altf4studios.corebringer.screens.gamescreen.SampleCardHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

// removed unused imports

/**
 * Utility class for parsing and managing card data from JSON files.
 * This class provides reusable methods for loading cards and finding specific cards.
 */
public class CardParser {
    
    private static final String CARDS_FILE_PATH = "assets/cards.json";
    private static CardParser instance;
    private Array<SampleCardHandler> allCards;
    
    /**
     * Private constructor for singleton pattern
     */
    private CardParser() {
        allCards = new Array<>();
        loadAllCards();
    }
    
    /**
     * Get singleton instance of CardParser
     * @return CardParser instance
     */
    public static CardParser getInstance() {
        if (instance == null) {
            instance = new CardParser();
        }
        return instance;
    }
    
    /**
     * Load all cards from the JSON file
     * @return Array of SampleCardHandler objects
     */
    public Array<SampleCardHandler> loadAllCards() {
        try {
            JsonValue root = new JsonReader().parse(Gdx.files.internal(CARDS_FILE_PATH));

            allCards.clear();
            for (JsonValue cardJson : root.get("cards")) {
                // Manually map to support both old and new schemas
                SampleCardHandler card = new SampleCardHandler();

                // Common/new fields
                card.id = safeString(cardJson, "id", "");
                card.level = safeInt(cardJson, "level", 1);
                card.name = safeString(cardJson, "name", "");
                card.description = safeString(cardJson, "description", "");
                card.cost = safeInt(cardJson, "cost", 0);
                card.baseEffect = safeInt(cardJson, "baseEffect", 0);
                card.codeEffect = safeString(cardJson, "codeEffect", "");
                card.suggestion = safeString(cardJson, "suggestion", "");

                // Old schema fields
                String type = safeString(cardJson, "type", null);
                String targetType = safeString(cardJson, "targetType", null);
                Array<String> tags = readStringArray(cardJson.get("tags"));

                // New schema fields
                String singleTag = safeString(cardJson, "tag", null);
                String target = safeString(cardJson, "target", null);

                // Normalize type
                if (type == null) {
                    type = inferTypeFromTag(singleTag);
                }
                card.type = type != null ? type : "BUFF";

                // Normalize targetType
                if (targetType == null) {
                    targetType = inferTargetType(target);
                }
                card.targetType = targetType != null ? targetType : "ENEMY";

                // Normalize tags[]
                if (tags == null) {
                    tags = new Array<>();
                }
                if (singleTag != null && !containsIgnoreCase(tags, singleTag)) {
                    tags.add(singleTag);
                }
                if (!containsIgnoreCase(tags, card.type)) {
                    tags.add(card.type.toUpperCase());
                }
                card.tags = tags;

                allCards.add(card);
            }

            Gdx.app.log("CardParser", "Successfully loaded " + allCards.size + " cards");
            return allCards;

        } catch (Exception e) {
            Gdx.app.error("CardParser", "Error loading cards: " + e.getMessage());
            return new Array<>();
        }
    }

    // ---- helpers ----
    private String safeString(JsonValue obj, String key, String def) {
        return obj.has(key) && !obj.get(key).isNull() ? obj.getString(key) : def;
    }

    private int safeInt(JsonValue obj, String key, int def) {
        return obj.has(key) && !obj.get(key).isNull() ? obj.getInt(key) : def;
    }

    private Array<String> readStringArray(JsonValue arr) {
        if (arr == null || !arr.isArray()) return null;
        Array<String> out = new Array<>();
        for (JsonValue v = arr.child; v != null; v = v.next) {
            if (!v.isNull()) out.add(v.asString());
        }
        return out;
    }

    private boolean containsIgnoreCase(Array<String> arr, String val) {
        for (String s : arr) if (s != null && s.equalsIgnoreCase(val)) return true;
        return false;
    }

    private String inferTypeFromTag(String tag) {
        if (tag == null) return null;
        String t = tag.toUpperCase();
        switch (t) {
            case "BASIC": return "ATTACK";
            case "SHIELD": return "DEFENSE";
            case "HEAL": return "BUFF";
            case "POISON":
            case "BLEED":
            case "STUN": return "DEBUFF";
            default: return null;
        }
    }

    private String inferTargetType(String target) {
        if (target == null) return null;
        String t = target.toUpperCase();
        if (t.contains("SELF")) return "SELF";
        return "ENEMY";
    }
    
    /**
     * Get all card descriptions as strings for UI display
     * @return Array of card description strings
     */
    public Array<String> getCardDescriptions() {
        Array<String> descriptions = new Array<>();
        
        for (SampleCardHandler card : allCards) {
            descriptions.add(card.toString());
        }
        
        return descriptions;
    }
    
    /**
     * Find a card by its string representation
     * @param cardString The string representation of the card
     * @return SampleCardHandler if found, null otherwise
     */
    public SampleCardHandler findCardByString(String cardString) {
        if (cardString == null) {
            return null;
        }
        
        for (SampleCardHandler card : allCards) {
            if (card.toString().equals(cardString)) {
                return card;
            }
        }
        
        return null;
    }
    
    /**
     * Find a card by its name
     * @param cardName The name of the card
     * @return SampleCardHandler if found, null otherwise
     */
    public SampleCardHandler findCardByName(String cardName) {
        if (cardName == null) {
            return null;
        }
        
        for (SampleCardHandler card : allCards) {
            if (card.name.equals(cardName)) {
                return card;
            }
        }
        
        return null;
    }

    /**
     * Find a card by its id
     * @param cardId The id of the card
     * @return SampleCardHandler if found, null otherwise
     */
    public SampleCardHandler findCardById(String cardId) {
        if (cardId == null) {
            return null;
        }
        for (SampleCardHandler card : allCards) {
            if (card.id.equals(cardId)) {
                return card;
            }
        }
        return null;
    }
    
    /**
     * Find cards by type
     * @param cardType The type of cards to find
     * @return Array of SampleCardHandler objects of the specified type
     */
    public Array<SampleCardHandler> findCardsByType(String cardType) {
        Array<SampleCardHandler> typeCards = new Array<>();
        
        if (cardType == null) {
            return typeCards;
        }
        
        for (SampleCardHandler card : allCards) {
            if (card.type.toLowerCase().equals(cardType.toLowerCase())) {
                typeCards.add(card);
            }
        }
        
        return typeCards;
    }
    
    /**
     * Get all cards
     * @return Array of all SampleCardHandler objects
     */
    public Array<SampleCardHandler> getAllCards() {
        return allCards;
    }
    
    /**
     * Get the number of cards loaded
     * @return Number of cards
     */
    public int getCardCount() {
        return allCards.size;
    }
    
    /**
     * Reload cards from the JSON file
     * @return true if successful, false otherwise
     */
    public boolean reloadCards() {
        try {
            loadAllCards();
            return true;
        } catch (Exception e) {
            Gdx.app.error("CardParser", "Error reloading cards: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if cards are loaded
     * @return true if cards are loaded, false otherwise
     */
    public boolean isCardsLoaded() {
        return allCards.size > 0;
    }
    
    /**
     * Get error message if card loading failed
     * @return Error message string
     */
    public String getErrorMessage() {
        if (isCardsLoaded()) {
            return "Cards loaded successfully";
        } else {
            return "Error loading cards: No cards found";
        }
    }
} 