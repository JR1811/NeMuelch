package net.shirojr.nemuelch.config.datatype;
@SuppressWarnings({"FieldMayBeFinal"})
public class PullBodyFeatureData {
    private Tool tool;
    private Velocity velocity;
    public PullBodyFeatureData(int damage, int cooldown, double horizontal, double vertical) {
        this.tool = new Tool(damage, cooldown);
        this.velocity = new Velocity(horizontal, vertical);
    }

    public Tool getTool() {
        return tool;
    }
    public Velocity getVelocity() {
        return velocity;
    }

    public static class Tool {
        private int damage, cooldown;
        public Tool(int damage, int cooldown) {
            this.damage = damage;
            this.cooldown = cooldown;
        }

        public int getDamage() {
            return damage;
        }
        public int getCooldown() {
            return cooldown;
        }
    }

    public static class Velocity {
        private double horizontal, vertical;
        public Velocity(double horizontal, double vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }

        public double getHorizontal() {
            return horizontal;
        }
        public double getVertical() {
            return vertical;
        }
    }
}
