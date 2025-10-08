package com.altf4studios.corebringer.utils;

public class SaveData {
    // Deprecated: kept for backward compatibility with older saves
    public int hp;
    // New fields
    public int currentHp;
    public int maxHp;
    public int energy;
    public int maxEnergy;
    public String[] cards;
    public int battleWon;

    public SaveData() {}


    public SaveData(int currentHp, int maxHp, int energy, int maxEnergy, String[] cards, int battleWon) {
        this.currentHp = currentHp;
        this.maxHp = maxHp;
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.cards = cards;
        this.battleWon = battleWon;
    }
}

