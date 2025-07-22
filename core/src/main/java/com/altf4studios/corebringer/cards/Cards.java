package com.altf4studios.corebringer.cards;

import com.altf4studios.corebringer.effects.CardEffectType;
import com.altf4studios.corebringer.slots.SlotType;

import java.util.List;

public class Cards {
    public String id;
    public String name;
    public String description;
    public int baseDamage;
    public int baseBlock;
    public int baseHeal;
    public String statusName;
    public int statusAmount;
    public int energyAmount;
    public SlotType type;
    public Tags tags;
    public TargetType targetType;
    public int cost;
    public String codeEffect;
    public String suggestion;
    public List<CardEffectType> effectTypes;

    public enum TargetType{
        SELF, ENEMY;
    }

    public enum Tags{
        BUFF, DEBUFF, ATTACK, DEFENSE;
    }

    // TODO: This CardUI seems to be using an older implementation of effects.
    // public List<CardEffects.CardEffectType> Effects;
}

    //class CardUI {
    //public void renderCard(Cards card) {
    //    if (card.Effects.contains(CardEffects.CardEffectType.INCREASE_ATK)) {
    //        drawEffectIcon("");
    //    }
    //    if (card.Effects.contains(CardEffects.CardEffectType.APPLY_STATUS)){
    //
    //    }
    //    if (card.Effects.contains(CardEffects.CardEffectType.MULTIHIT)){
    //
    //    }
    //    if (card.Effects.contains(CardEffects.CardEffectType.ATTACK)){
    //
    //    }
    //    if (card.Effects.contains(CardEffects.CardEffectType.DEFENSE)){
    //
    //    }
    //    // ... Cost inclusion shall be added through development
    //}
    //
    //private void drawEffectIcon(String iconName) {
    //    // Drawing logic here
    //}
//}

