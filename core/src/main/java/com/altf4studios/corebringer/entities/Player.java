package com.altf4studios.corebringer.entities;

import java.util.ArrayList;
import java.util.List;
import com.altf4studios.corebringer.screens.gamescreen.SampleCardHandler;

public class Player extends Entity {
    private int currentCardCount;
    private final int totalCardCount = 15;
    private int energy;
    public static final int MAX_HAND_SIZE = 10;
    private List<SampleCardHandler> hand = new ArrayList<>();
    private List<SampleCardHandler> drawPile = new ArrayList<>();
    private List<SampleCardHandler> discardPile = new ArrayList<>();
    private List<SampleCardHandler> exhaustPile = new ArrayList<>();

    public Player(String name, int maxHealth, int attack, int defense, int energy) {
        super(name, maxHealth, attack, defense);
        this.energy = energy;
        this.currentCardCount = totalCardCount;
    }

    public int getCurrentCardCount() {
        return currentCardCount;
    }

    public int getTotalCardCount() {
        return totalCardCount;
    }

    public int getEnergy() {
        return energy;
    }

    public void useCard() {
        if (currentCardCount > 0) {
            currentCardCount--;
        }
    }

    public void restoreCards() {
        currentCardCount = totalCardCount;
    }

    public void useEnergy(int amount) {
        if (energy >= amount) {
            energy -= amount;
        }
    }

    public void restoreEnergy(int amount) {
        energy += amount;
    }

    // Player-specific action
    public void heal(int amount) {
        if (alive) {
            health = Math.min(maxHealth, health + amount);
        }
    }

    @Override
    public void target(Player player) {
        // Player targeting another player (if needed)
    }

    @Override
    public void target(Enemy enemy) {
        // Player targets an enemy (e.g., attack)
    }

    // Draw a card, enforcing hand size limit
    public void drawCard() {
        if (hand.size() >= MAX_HAND_SIZE) {
            // Hand is full, skip drawing
            return;
        }
        if (drawPile.isEmpty()) {
            shuffleDiscardIntoDraw();
        }
        if (!drawPile.isEmpty()) {
            hand.add(drawPile.remove(0));
        }
    }
    // Discard a specific card from hand
    public void discardCard(SampleCardHandler card) {
        if (hand.contains(card)) {
            hand.remove(card);
            discardPile.add(card);
        }
    }
    // Discard all cards from hand
    public void discardHand() {
        discardPile.addAll(hand);
        hand.clear();
    }
    // Exhaust a specific card from hand
    public void exhaustCard(SampleCardHandler card) {
        if (hand.contains(card)) {
            hand.remove(card);
            exhaustPile.add(card);
        }
    }
    // Shuffle discard pile into draw pile
    private void shuffleDiscardIntoDraw() {
        if (!discardPile.isEmpty()) {
            drawPile.addAll(discardPile);
            discardPile.clear();
            java.util.Collections.shuffle(drawPile);
        }
    }
    // Getters for the piles
    public List<SampleCardHandler> getHand() { return hand; }
    public List<SampleCardHandler> getDrawPile() { return drawPile; }
    public List<SampleCardHandler> getDiscardPile() { return discardPile; }
    public List<SampleCardHandler> getExhaustPile() { return exhaustPile; }
}
