package com.altf4studios.corebringer.cards;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.altf4studios.corebringer.slots.SlotType;
public class CardFactory {

    private static final Map<String, Cards> cardLibrary = new HashMap<>();

    public static void loadCards(String path){
        JsonReader reader = new JsonReader();
        Json json = new Json();
        JsonValue root = reader.parse(Gdx.files.internal(path));
        JsonValue cardArray = root.get("cards");

        for (JsonValue cardJson : cardArray) {
            String typeStr = cardJson.getString("type", "");
            Cards card = null;

            // Parse cost and baseEffect as int, even if they are strings in JSON
            int cost = 0;
            int baseEffect = 0;
            try {
                String costStr = cardJson.getString("cost", "0");
                cost = Integer.parseInt(costStr.trim());
            } catch (Exception e) {
                System.err.println("[CardFactory] Invalid cost for card: " + cardJson.getString("id", "unknown") + ", defaulting to 0");
            }
            try {
                String baseEffectStr = cardJson.getString("baseEffect", "0");
                baseEffect = Integer.parseInt(baseEffectStr.trim().split(",")[0]); // Only first value if comma-separated
            } catch (Exception e) {
                System.err.println("[CardFactory] Invalid baseEffect for card: " + cardJson.getString("id", "unknown") + ", defaulting to 0");
            }

            // Parse SlotType enum if possible
            String slotTypeStr = cardJson.getString("type", "");
            SlotType slotType = null;
            try {
                slotType = SlotType.valueOf(slotTypeStr);
            } catch (Exception e) {
                // fallback: null or handle as needed
            }

            if (typeStr.equalsIgnoreCase("ATTACK")) {
                card = json.readValue(AttackCard.class, cardJson);
            } else if (typeStr.equalsIgnoreCase("DEFENSE")) {
                card = json.readValue(UtilityCard.class, cardJson);
            } else if (typeStr.equalsIgnoreCase("BUFF")) {
                card = json.readValue(StatusCard.class, cardJson);
            } else {
                System.err.println("❌ Unknown card type: " + typeStr);
                continue;
            }

            // Set parsed values explicitly to ensure type safety
            if (card != null) {
                card.cost = cost;
                card.baseEffect = baseEffect;
                card.type = slotType;
                // Optionally handle tags if present in JSON
                if (cardJson.has("tags")) {
                    card.tags = json.readValue(ArrayList.class, String.class, cardJson.get("tags"));
                }
                if (card.id != null) {
                    cardLibrary.put(card.id, card);
                }
            }
        }
        System.out.println("✅ Loaded " + cardLibrary.size() + " cards.");
    }

    public static Cards getCard(String id) {
        Cards original = cardLibrary.get(id);
        return original != null ? original.copy() : null;
    }
}
