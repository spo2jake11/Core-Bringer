package com.altf4studios.corebringer.objects;

public class Cards {
        public String id;
        public String name;
        public String description;
        public CardType type;
        public String[] tags;
        public int cost;
        public String codeEffect;
        public String suggestion;


        public enum CardType{
            ATTACK_CLOSE, ATTACK_RANGE, DEFENSE, BUFF, DEBUFF, STATUS
        }
}
