package net.shirojr.nemuelch.config.datatype;

@SuppressWarnings({"FieldMayBeFinal"})
public class EnchantmentLevelData {
    private int smite, bane, sharpness, power, knockback, unbreaking;
    private int protection, fireProtection, fallProtection, explosionProtection, projectileProtection;
    public EnchantmentLevelData(int smite, int bane, int sharpness, int power, int knockback, int unbreaking,
                                int protection, int fireProtection, int fallProtection,
                                int explosionProtection, int projectileProtection) {
        this.smite = smite;
        this.bane = bane;
        this.sharpness = sharpness;
        this.power = power;
        this.knockback = knockback;
        this.unbreaking = unbreaking;
        this.protection = protection;
        this.fireProtection = fireProtection;
        this.fallProtection = fallProtection;
        this.explosionProtection = explosionProtection;
        this.projectileProtection = projectileProtection;
    }

    public int getSmite() {
        return smite;
    }
    public int getBane() {
        return bane;
    }
    public int getSharpness() {
        return sharpness;
    }
    public int getPower() {
        return power;
    }
    public int getKnockback() {
        return knockback;
    }
    public int getUnbreaking() {
        return unbreaking;
    }
    public int getProtection() {
        return protection;
    }
    public int getFireProtection() {
        return fireProtection;
    }
    public int getFallProtection() {
        return fallProtection;
    }
    public int getExplosionProtection() {
        return explosionProtection;
    }
    public int getProjectileProtection() {
        return projectileProtection;
    }
}
