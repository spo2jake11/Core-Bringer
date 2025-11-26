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
    public int gold;
    // New: stage progression (controls GameScreen background and map regeneration)
    public int stageLevel = 1;

    // Question tracking data for metrics
    public java.util.Map<String, QuestionLevelData> questionData = new java.util.HashMap<>();
    // Objective progress per level (e.g., "level1" -> count)
    public java.util.Map<String, Integer> objectives = new java.util.HashMap<>();

    public static class QuestionLevelData {
        public String title;
        public int correct;
        public int wrong;

        public QuestionLevelData() {
            this.correct = 0;
            this.wrong = 0;
        }

        public QuestionLevelData(String title) {
            this.title = title;
            this.correct = 0;
            this.wrong = 0;
        }

        public void addResult(boolean isCorrect) {
            if (isCorrect) {
                correct++;
            } else {
                wrong++;
            }
        }
    }

    public static class TotalsData {
        public int correct;
        public int wrong;

        public TotalsData() {
            this.correct = 0;
            this.wrong = 0;
        }

        public void addResult(boolean isCorrect) {
            if (isCorrect) {
                correct++;
            } else {
                wrong++;
            }
        }
    }

    public TotalsData totals = new TotalsData();

    public SaveData() {}


    public SaveData(int currentHp, int maxHp, int energy, int maxEnergy, String[] cards, int battleWon) {
        this.currentHp = currentHp;
        this.maxHp = maxHp;
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.cards = cards;
        this.battleWon = battleWon;
        this.stageLevel = 1;
    }
}
