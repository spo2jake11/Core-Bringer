package com.altf4studios.corebringer.cards;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.Gdx;
import java.util.HashMap;
import java.util.Map;

public class CardAtlasResolver {
    private final TextureAtlas cardAtlas;
    private final Map<String, String> cardNameMappings;
    private final String defaultCardName = "bck_card";

    public CardAtlasResolver() {
        this.cardAtlas = new TextureAtlas("assets/cards/cards_atlas.atlas");
        this.cardNameMappings = initializeCardMappings();
    }

    public TextureRegionDrawable getDrawableForCardName(String cardName) {
        if (cardAtlas == null) {
            Gdx.app.error("CardAtlasResolver", "Card atlas is null, using default card");
            return new TextureRegionDrawable(cardAtlas.findRegion(defaultCardName));
        }
        
        if (cardName == null || cardName.trim().isEmpty()) {
            Gdx.app.log("CardAtlasResolver", "Card name is null or empty, using default card");
            return new TextureRegionDrawable(cardAtlas.findRegion(defaultCardName));
        }

        String normalizedName = normalize(cardName);
        String atlasRegionName = findAtlasRegion(normalizedName);
        
        if (atlasRegionName != null && cardAtlas.findRegion(atlasRegionName) != null) {
            Gdx.app.log("CardAtlasResolver", "Found atlas region '" + atlasRegionName + "' for card '" + cardName + "'");
            return new TextureRegionDrawable(cardAtlas.findRegion(atlasRegionName));
        }
        
        Gdx.app.log("CardAtlasResolver", "No atlas region found for card '" + cardName + "', using default card");
        return new TextureRegionDrawable(cardAtlas.findRegion(defaultCardName));
    }

    private Map<String, String> initializeCardMappings() {
        Map<String, String> mappings = new HashMap<>();
        
        // Direct mappings for common card names - matching actual atlas region names
        mappings.put("shield", "Final_Shield");
        mappings.put("final shield", "Final_Shield");
        mappings.put("heal", "Heal_Package");
        mappings.put("heal package", "Heal_Package");
        
        // Variable Strash (not Slash) - matching atlas file
        mappings.put("variable strash", "Variable_Strash");
        mappings.put("variable slash", "Variable_Strash"); // Common typo
        mappings.put("variable", "Variable_Strash");
        mappings.put("strash", "Variable_Strash");
        mappings.put("slash", "Variable_Strash"); // Fallback for slash
        
        mappings.put("logic break", "Logic_Break");
        mappings.put("logic", "Logic_Break");
        mappings.put("break", "Logic_Break");
        mappings.put("function", "Logic_Break");
        
        // Add more mappings as needed
        return mappings;
    }

    private String normalize(String name) {
        if (name == null) return "";
        
        // Convert to lowercase and replace common separators
        name = name.toLowerCase().trim();
        name = name.replace('_', ' ');
        name = name.replace('-', ' ');
        name = name.replaceAll("\\s+", " "); // Replace multiple spaces with single space
        
        // Handle common typos and variations
        if (name.contains("slash")) {
            name = name.replace("slash", "strash"); // Convert slash to strash to match atlas
        }
        if (name.contains("attak")) {
            name = name.replace("attak", "attack");
        }
        if (name.contains("defens")) {
            name = name.replace("defens", "defense");
        }
        
        return name;
    }

    private String findAtlasRegion(String normalizedName) {
        // First, try exact mapping
        if (cardNameMappings.containsKey(normalizedName)) {
            String baseName = cardNameMappings.get(normalizedName);
            return findRegionWithLevel(baseName, normalizedName);
        }
        
        // Try partial matching
        for (Map.Entry<String, String> entry : cardNameMappings.entrySet()) {
            if (normalizedName.contains(entry.getKey())) {
                String baseName = entry.getValue();
                return findRegionWithLevel(baseName, normalizedName);
            }
        }
        
        // Try to find by keywords
        String baseName = getAtlasBaseNameByKeywords(normalizedName);
        if (baseName != null) {
            return findRegionWithLevel(baseName, normalizedName);
        }
        
        return null;
    }

    private String findRegionWithLevel(String baseName, String cardName) {
        int level = extractLevelSuffix(cardName);
        
        // Try with level suffix first
        String candidate = baseName + level;
        if (cardAtlas.findRegion(candidate) != null) {
            return candidate;
        }
        
        // Try level 1 as fallback
        String fallback = baseName + "1";
        if (cardAtlas.findRegion(fallback) != null) {
            return fallback;
        }
        
        // Try without level suffix
        if (cardAtlas.findRegion(baseName) != null) {
            return baseName;
        }
        
        return null;
    }

    private String getAtlasBaseNameByKeywords(String cardNameLower) {
        if (cardNameLower.contains("shield")) {
            return "Final_Shield";
        } else if (cardNameLower.contains("heal")) {
            return "Heal_Package";
        } else if (cardNameLower.contains("variable") || cardNameLower.contains("slash") || cardNameLower.contains("strash")) {
            return "Variable_Strash"; // Correct atlas name
        } else if (cardNameLower.contains("function") || cardNameLower.contains("logic") || cardNameLower.contains("break")) {
            return "Logic_Break";
        
        } else if (cardNameLower.contains("poison") || cardNameLower.contains("bite")) {
            return "Looping_Bite";
        }
        return null;
    }

    private int extractLevelSuffix(String cardNameLower) {
        int level = 1;
        
        // Look for numbers at the end of the string
        for (int i = cardNameLower.length() - 1; i >= 0; i--) {
            char ch = cardNameLower.charAt(i);
            if (Character.isDigit(ch)) {
                // Found a digit, extract the full number
                int start = i;
                while (start > 0 && Character.isDigit(cardNameLower.charAt(start - 1))) {
                    start--;
                }
                try {
                    level = Integer.parseInt(cardNameLower.substring(start, i + 1));
                    break;
                } catch (NumberFormatException e) {
                    level = 1;
                    break;
                }
            }
            if (Character.isLetter(ch)) {
                break;
            }
        }
        
        // Clamp level to valid range
        if (level < 1 || level > 5) level = 1;
        return level;
    }

    /**
     * Get all available atlas region names for debugging
     */
    public String[] getAllAtlasRegions() {
        if (cardAtlas == null) return new String[0];
        return cardAtlas.getRegions().toArray(String.class);
    }

    /**
     * Log all available atlas regions for debugging
     */
    public void logAllAtlasRegions() {
        if (cardAtlas == null) {
            Gdx.app.log("CardAtlasResolver", "Atlas is null");
            return;
        }
        String[] regions = getAllAtlasRegions();
        Gdx.app.log("CardAtlasResolver", "Available atlas regions:");
        for (String region : regions) {
            Gdx.app.log("CardAtlasResolver", "  - " + region);
        }
    }

    /**
     * Check if a specific atlas region exists
     */
    public boolean hasAtlasRegion(String regionName) {
        return cardAtlas != null && cardAtlas.findRegion(regionName) != null;
    }

    public void dispose() {
        if (cardAtlas != null) {
            cardAtlas.dispose();
        }
    }
}



