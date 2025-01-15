package net.shirojr.nemuelch.config.datatype;

@SuppressWarnings({"FieldMayBeFinal"})
public class WateringCanData {
    private int fillRate, fillChance;
    private Material copper, iron, gold, diamond;
    public WateringCanData(int fillRate, int fillChance, Material copper, Material iron, Material gold, Material diamond) {
        this.fillRate = fillRate;
        this.fillChance = fillChance;
        this.copper = copper;
        this.iron = iron;
        this.gold = gold;
        this.diamond = diamond;
    }

    public int getFillRate() {
        return fillRate;
    }
    public int getFillChance() {
        return fillChance;
    }
    public Material getCopper() {
        return copper;
    }
    public Material getIron() {
        return iron;
    }
    public Material getGold() {
        return gold;
    }
    public Material getDiamond() {
        return diamond;
    }

    public static class Material {
        private int capacity;
        public Material(int capacity) {
            this.capacity = capacity;
        }

        public int getCapacity() {
            return capacity;
        }
    }
}
