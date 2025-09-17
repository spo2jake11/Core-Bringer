package com.altf4studios.corebringer.status;

import com.altf4studios.corebringer.entities.Entity;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.badlogic.gdx.Gdx;

public class StatusManager {
    private static final StatusManager INSTANCE = new StatusManager();

    public static StatusManager getInstance() {
        return INSTANCE;
    }

    private Map<Entity, Map<String, StatusEffect>> entityStatusMap = new HashMap<>();

    /**
     * Apply a status effect to an entity
     * @param entity The entity to apply the status to
     * @param effect The status effect to apply
     */
    public void applyStatus(Entity entity, StatusEffect effect) {
        if (entity == null || effect == null) return;
        
        Map<String, StatusEffect> statusMap = entityStatusMap.computeIfAbsent(entity, k -> new HashMap<>());
        String statusName = effect.getName();
        
        // Check if entity already has this status
        if (statusMap.containsKey(statusName)) {
            // Stack the status effect
            StatusEffect existing = statusMap.get(statusName);
            existing.increasePower(effect.getPower());
            existing.extendDuration(effect.getDuration());
            Gdx.app.log("StatusManager", "Stacked " + statusName + " on " + entity.getName());
        } else {
            // Apply new status
            statusMap.put(statusName, effect);
            effect.onApply();
            Gdx.app.log("StatusManager", "Applied " + statusName + " to " + entity.getName());
        }
    }

    /**
     * Remove a specific status effect from an entity
     * @param entity The entity to remove the status from
     * @param statusName The name of the status to remove
     */
    public void removeStatus(Entity entity, String statusName) {
        Map<String, StatusEffect> statusMap = entityStatusMap.get(entity);
        if (statusMap != null && statusMap.containsKey(statusName)) {
            StatusEffect effect = statusMap.remove(statusName);
            effect.onRemove(entity);
            Gdx.app.log("StatusManager", "Removed " + statusName + " from " + entity.getName());
        }
    }

    /**
     * Remove all status effects from an entity
     * @param entity The entity to clear statuses from
     */
    public void clearAllStatuses(Entity entity) {
        Map<String, StatusEffect> statusMap = entityStatusMap.get(entity);
        if (statusMap != null) {
            for (StatusEffect effect : statusMap.values()) {
                effect.onRemove(entity);
            }
            statusMap.clear();
            Gdx.app.log("StatusManager", "Cleared all statuses from " + entity.getName());
        }
    }

    /**
     * Process turn start for all status effects on an entity
     * @param entity The entity to process turn start for
     */
    public void processTurnStart(Entity entity) {
        Map<String, StatusEffect> statusMap = entityStatusMap.get(entity);
        if (statusMap == null) return;

        List<String> expiredStatuses = new ArrayList<>();
        int totalDotDamage = 0; // combined Poison + Bleed damage

        for (Map.Entry<String, StatusEffect> entry : statusMap.entrySet()) {
            String statusName = entry.getKey();
            StatusEffect effect = entry.getValue();

            switch (statusName) {
                case "Poison":
                    // Deal damage equal to current power, then power-- and duration--
                    if (effect.getPower() > 0) {
                        totalDotDamage += effect.getPower();
                        effect.decreasePower(1);
                    }
                    effect.tick();
                    break;
                case "Bleed":
                    // Deal damage equal to current power; only duration--
                    if (effect.getPower() > 0) {
                        totalDotDamage += effect.getPower();
                    }
                    effect.tick();
                    break;
                default:
                    // Keep default behavior for other statuses
                    effect.onTurnStart(entity);
                    break;
            }

            if (effect.isExpired()) {
                expiredStatuses.add(statusName);
            }
        }

        // Apply combined DOT in a single hit so it's simultaneous
        if (totalDotDamage > 0 && entity.isAlive()) {
            entity.takeDamage(totalDotDamage);
        }

        // Remove expired statuses
        for (String statusName : expiredStatuses) {
            StatusEffect effect = statusMap.remove(statusName);
            effect.onExpire(entity);
        }
    }

    /**
     * Process turn end for all status effects on an entity
     * @param entity The entity to process turn end for
     */
    public void processTurnEnd(Entity entity) {
        Map<String, StatusEffect> statusMap = entityStatusMap.get(entity);
        if (statusMap != null) {
            for (StatusEffect effect : statusMap.values()) {
                effect.onTurnEnd(entity);
            }
        }
    }

    /**
     * Modify incoming damage based on active status effects
     * @param entity The entity receiving damage
     * @param baseDamage The original damage amount
     * @return The modified damage amount
     */
    public int modifyIncomingDamage(Entity entity, int baseDamage) {
        Map<String, StatusEffect> statusMap = entityStatusMap.get(entity);
        if (statusMap != null) {
            int modifiedDamage = baseDamage;
            for (StatusEffect effect : statusMap.values()) {
                modifiedDamage = effect.modifyIncomingDamage(modifiedDamage, entity);
            }
            return modifiedDamage;
        }
        return baseDamage;
    }

    /**
     * Modify outgoing damage based on active status effects
     * @param entity The entity dealing damage
     * @param baseDamage The original damage amount
     * @return The modified damage amount
     */
    public int modifyOutgoingDamage(Entity entity, int baseDamage) {
        Map<String, StatusEffect> statusMap = entityStatusMap.get(entity);
        if (statusMap != null) {
            int modifiedDamage = baseDamage;
            for (StatusEffect effect : statusMap.values()) {
                modifiedDamage = effect.modifyOutgoingDamage(modifiedDamage, entity);
            }
            return modifiedDamage;
        }
        return baseDamage;
    }

    /**
     * Modify block amount based on active status effects
     * @param entity The entity with block
     * @param baseBlock The original block amount
     * @return The modified block amount
     */
    public int modifyBlock(Entity entity, int baseBlock) {
        Map<String, StatusEffect> statusMap = entityStatusMap.get(entity);
        if (statusMap != null) {
            int modifiedBlock = baseBlock;
            for (StatusEffect effect : statusMap.values()) {
                modifiedBlock = effect.modifyBlock(modifiedBlock, entity);
            }
            return modifiedBlock;
        }
        return baseBlock;
    }

    /**
     * Modify healing amount based on active status effects
     * @param entity The entity receiving healing
     * @param baseHealing The original healing amount
     * @return The modified healing amount
     */
    public int modifyHealing(Entity entity, int baseHealing) {
        Map<String, StatusEffect> statusMap = entityStatusMap.get(entity);
        if (statusMap != null) {
            int modifiedHealing = baseHealing;
            for (StatusEffect effect : statusMap.values()) {
                modifiedHealing = effect.modifyHealing(modifiedHealing, entity);
            }
            return modifiedHealing;
        }
        return baseHealing;
    }

    /**
     * Get all status effects for an entity
     * @param entity The entity to get statuses for
     * @return Map of status names to status effects
     */
    public Map<String, StatusEffect> getStatuses(Entity entity) {
        return entityStatusMap.getOrDefault(entity, new HashMap<>());
    }

    /**
     * Get a specific status effect for an entity
     * @param entity The entity to check
     * @param statusName The name of the status effect
     * @return The status effect, or null if not found
     */
    public StatusEffect getStatus(Entity entity, String statusName) {
        Map<String, StatusEffect> statusMap = entityStatusMap.get(entity);
        return statusMap != null ? statusMap.get(statusName) : null;
    }

    /**
     * Convenience: Get the power (stacks) of a status on an entity
     */
    public int getPower(Entity entity, String statusName) {
        StatusEffect se = getStatus(entity, statusName);
        return se != null ? se.getPower() : 0;
    }

    /**
     * Convenience: Get remaining duration of a status on an entity
     */
    public int getDuration(Entity entity, String statusName) {
        StatusEffect se = getStatus(entity, statusName);
        return se != null ? se.getDuration() : 0;
    }

    /**
     * Check if an entity has a specific status effect
     * @param entity The entity to check
     * @param statusName The name of the status effect
     * @return true if the entity has the status effect
     */
    public boolean hasStatus(Entity entity, String statusName) {
        Map<String, StatusEffect> statusMap = entityStatusMap.get(entity);
        return statusMap != null && statusMap.containsKey(statusName) && !statusMap.get(statusName).isExpired();
    }

    /**
     * Check if an entity has any status effects
     * @param entity The entity to check
     * @return true if the entity has any active status effects
     */
    public boolean hasAnyStatus(Entity entity) {
        Map<String, StatusEffect> statusMap = entityStatusMap.get(entity);
        return statusMap != null && !statusMap.isEmpty();
    }

    /**
     * Get the count of active status effects for an entity
     * @param entity The entity to count statuses for
     * @return The number of active status effects
     */
    public int getStatusCount(Entity entity) {
        Map<String, StatusEffect> statusMap = entityStatusMap.get(entity);
        return statusMap != null ? statusMap.size() : 0;
    }

    /**
     * Update and clean expired status effects for an entity
     * @param entity The entity to update statuses for
     */
    public void updateStatuses(Entity entity) {
        Map<String, StatusEffect> statusMap = entityStatusMap.get(entity);
        if (statusMap != null) {
            List<String> expiredStatuses = new ArrayList<>();
            
            for (Map.Entry<String, StatusEffect> entry : statusMap.entrySet()) {
                if (entry.getValue().isExpired()) {
                    expiredStatuses.add(entry.getKey());
                }
            }
            
            // Remove expired statuses
            for (String statusName : expiredStatuses) {
                StatusEffect effect = statusMap.remove(statusName);
                effect.onExpire(entity);
            }
        }
    }

    /**
     * Remove entity from status tracking (useful when entity dies)
     * @param entity The entity to remove
     */
    public void removeEntity(Entity entity) {
        if (entityStatusMap.containsKey(entity)) {
            clearAllStatuses(entity);
            entityStatusMap.remove(entity);
        }
    }
}
