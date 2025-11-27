package com.altf4studios.corebringer.entities;

public class Enemy extends Entity {
    private String entityID;
    private enemyType type;
    private int skillCooldown;
    private String[] skills;

    private double attackProb = 0.6; // default pattern: 60% attack
    private double defendProb = 0.25;
    private double healProb = 0.15;

    public enum enemyType {
        NORMAL, ELITE, BOSS, SPECIAL;
    }

    public Enemy(String entityID, String name, int maxHealth, int attack, int defense, enemyType type, int skillCooldown, String[] skills) {
        super(name, maxHealth, attack, defense);
        this.entityID = entityID;
        this.type = type;
        this.skillCooldown = skillCooldown;
        this.skills = skills;
    }

    /**
     * Expose a setter for max health on Enemy to allow runtime updates when rerolling enemies.
     */
    public void setMaxHealth(int maxHealth) {
        super.setMaxHealth(maxHealth);
    }

    public String getEntityID() {
        return entityID;
    }

    public enemyType getType() {
        return type;
    }

    public int getSkillCooldown() {
        return skillCooldown;
    }

    public String[] getSkills() {
        return skills;
    }

    public void setName(String name) {
        super.setName(name);
    }

    public void setAttack(int attack) {
        // Delegate to Entity.setAttack to centralize clamping behavior
        super.setAttack(attack);
    }

    public double getAttackProb() { return attackProb; }
    public double getDefendProb() { return defendProb; }
    public double getHealProb() { return healProb; }

    public void setAttackPattern(double attackProb, double defendProb, double healProb) {
        try {
            double sum = attackProb + defendProb + healProb;
            if (sum <= 0) return; // ignore
            // normalize to sum==1
            this.attackProb = attackProb / sum;
            this.defendProb = defendProb / sum;
            this.healProb = healProb / sum;
        } catch (Exception ignored) {}
    }

    // Enemy-specific action
    public void useSkill(int index, Entity target) {
        // Implement skill logic here
    }

    // Defensive action: enemy gains shield (block) and skips attack
    public void defend() {
        if (!this.isAlive()) return;
        int blockAmount = Math.max(3, (int)Math.round(this.getDefense() * 1.5));
        this.gainBlock(blockAmount);
    }

    // Simple attack method for basic turn-based combat
    public void attack(Entity target) {
        if (target != null && target.isAlive() && this.isAlive()) {
            int damage = this.getAttack();
            target.takeDamage(damage);
        }
    }

    // Simple heal method for basic turn-based combat
    public void heal(int amount) {
        if (!this.isAlive()) return;
        if (amount <= 0) return;
        this.setHp(this.getHp() + amount);
    }

    @Override
    public void target(Player player) {
        // Enemy targets a player (e.g., attack or use skill)
    }

    @Override
    public void target(Enemy enemy) {
        // Enemy targets another enemy (if needed)
    }
}
