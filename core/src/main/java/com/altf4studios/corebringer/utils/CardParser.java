package com.altf4studios.corebringer.utils;

import com.altf4studios.corebringer.screens.gamescreen.SampleCardHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing and managing card data from JSON files.
 * This class provides reusable methods for loading cards and finding specific cards.
 */
public class CardParser {
    
    private static final String CARDS_FILE_PATH = "cards.json";
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
            Json json = new Json();
            JsonValue root = new JsonReader().parse(Gdx.files.internal(CARDS_FILE_PATH));
            
            allCards.clear();
            for (JsonValue cardJson : root.get("cards")) {
                SampleCardHandler cardHandler = json.readValue(SampleCardHandler.class, cardJson);
                allCards.add(cardHandler);
            }
            
            Gdx.app.log("CardParser", "Successfully loaded " + allCards.size + " cards");
            return allCards;
            
        } catch (Exception e) {
            Gdx.app.error("CardParser", "Error loading cards: " + e.getMessage());
            return new Array<>();
        }
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