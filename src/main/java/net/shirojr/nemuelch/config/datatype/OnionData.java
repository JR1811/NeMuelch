package net.shirojr.nemuelch.config.datatype;

@SuppressWarnings({"FieldMayBeFinal"})
public class OnionData {
    private OnionEntityData entity;
    private OnionToolData tool;

    public OnionData(boolean environmentalDamage, double maxHealth, double movSpeed, double followRange,
                     int explosionRadius, int effectRadius, int durability, int minAmountSpawn, int maxAmountSpawn) {
        this.entity = new OnionEntityData(environmentalDamage, maxHealth, movSpeed, followRange, explosionRadius, effectRadius);
        this.tool = new OnionToolData(durability, minAmountSpawn, maxAmountSpawn);
    }

    public OnionEntityData getEntityData() {
        return this.entity;
    }
    public OnionToolData getToolData() {
        return this.tool;
    }

    public static class OnionEntityData {
        private boolean environmentalDamage;
        private double maxHealth, movSpeed, followRange;
        private int explosionRadius, effectRadius;

        public OnionEntityData(boolean environmentalDamage, double maxHealth, double movSpeed, double followRange,
                               int explosionRadius, int effectRadius) {
            this.environmentalDamage = environmentalDamage;
            this.maxHealth = maxHealth;
            this.movSpeed = movSpeed;
            this.followRange = followRange;
            this.explosionRadius = explosionRadius;
            this.effectRadius = effectRadius;
        }

        public boolean getEnvironmentalDamage() {
            return environmentalDamage;
        }
        public double getMaxHealth() {
            return maxHealth;
        }
        public double getMovSpeed() {
            return movSpeed;
        }
        public double getFollowRange() {
            return followRange;
        }
        public int getExplosionRadius() {
            return explosionRadius;
        }
        public int getEffectRadius() {
            return effectRadius;
        }
    }

    public static class OnionToolData {
        private int durability, minAmountSpawn, maxAmountSpawn;
        public OnionToolData(int durability, int minAmountSpawn, int maxAmountSpawn) {
            this.durability = durability;
            this.minAmountSpawn = minAmountSpawn;
            this.maxAmountSpawn = maxAmountSpawn;
        }

        public int getDurability() {
            return durability;
        }
        public int getMinAmountSpawn() {
            return minAmountSpawn;
        }
        public int getMaxAmountSpawn() {
            return maxAmountSpawn;
        }
    }

}

