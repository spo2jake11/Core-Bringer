package com.altf4studios.corebringer.utils;

public class SaveData {
    public int hp;
    public int energy;
    public String[] cards;
    public int battleWon;

    public SaveData() {}


    public SaveData(int hp, int energy, String[] cards, int battleWon) {
        this.hp = hp;
        this.energy = energy;
        this.cards = cards;
        this.battleWon = battleWon;
    }
}

