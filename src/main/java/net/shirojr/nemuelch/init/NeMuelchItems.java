package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.adminToolItem.EntityTransportToolItem;
import net.shirojr.nemuelch.item.custom.adminToolItem.RefillToolItem;
import net.shirojr.nemuelch.item.custom.armorAndShieldItem.FallenGuardArmorSetItem;
import net.shirojr.nemuelch.item.custom.armorAndShieldItem.FortifiedShieldItem;
import net.shirojr.nemuelch.item.custom.armorAndShieldItem.PortableBarrelItem;
import net.shirojr.nemuelch.item.custom.armorAndShieldItem.RoyalGuardArmorSetItem;
import net.shirojr.nemuelch.item.custom.caneItem.*;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.ArtifactItem;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.CallOfAgonyItem;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.OnionWandItem;
import net.shirojr.nemuelch.item.custom.gloveItem.TrainingGloveItem;
import net.shirojr.nemuelch.item.custom.muelchItem.*;
import net.shirojr.nemuelch.item.custom.supportItem.*;
import net.shirojr.nemuelch.util.helper.WateringCanHelper;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class NeMuelchItems {
    //region muelch
    public static final List<Item> NEMUELCH_DRINKS = new ArrayList<>();

    public static final Item GREEN_MUELCH = registerNemuelchDrink("green_muelch",
            new NeMuelchGreenItem(new FabricItemSettings().group(NeMuelchItemGroups.NEMUELCH).food(NeMuelchDrinkComponents.GREEN_MILK).maxCount(1)));

    public static final Item BROWN_MUELCH = registerNemuelchDrink("brown_muelch",
            new NeMuelchBrownItem(new FabricItemSettings().group(NeMuelchItemGroups.NEMUELCH).food(NeMuelchDrinkComponents.BROWN_MILK).maxCount(1)));

    public static final Item BLUE_MUELCH = registerNemuelchDrink("blue_muelch",
            new NeMuelchBlueItem(new FabricItemSettings().group(NeMuelchItemGroups.NEMUELCH).food(NeMuelchDrinkComponents.BLUE_MILK).maxCount(1)));

    public static final Item PINK_MUELCH = registerNemuelchDrink("pink_muelch",
            new NeMuelchPinkItem(new FabricItemSettings().group(NeMuelchItemGroups.NEMUELCH).food(NeMuelchDrinkComponents.PINK_MILK).maxCount(1)));

    public static final Item YELLOW_MUELCH = registerNemuelchDrink("yellow_muelch",
            new NeMuelchYellowItem(new FabricItemSettings().group(NeMuelchItemGroups.NEMUELCH).food(NeMuelchDrinkComponents.YELLOW_MILK).maxCount(1)));

    public static final Item PURPLE_MUELCH = registerNemuelchDrink("purple_muelch",
            new NeMuelchPurpleItem(new FabricItemSettings().group(NeMuelchItemGroups.NEMUELCH).food(NeMuelchDrinkComponents.PURPLE_MILK).maxCount(1)));
    //endregion
    //region canes
    public static final List<Item> PEST_CANES = new ArrayList<>();

    public static final Item PEST_CANE = registerPestCanes("pestcane",
            new PestcaneItem(new FabricItemSettings().group(NeMuelchItemGroups.WARFARE).maxCount(1)));

    public static final Item ARKADUS_CANE = registerPestCanes("arkaduscane",
            new ArkaduscaneItem(new FabricItemSettings().group(NeMuelchItemGroups.WARFARE).maxCount(1)));

    public static final Item ARKADUSCANE_ENTITY_PROJECTILE_ITEM = registerItem("arkaduscane_projectile",
            new ArkaduscaneProjectileEntityItem(new FabricItemSettings()));

    public static final Item GLADIUS_CANE = registerPestCanes("gladiuscane",
            new GladiuscaneItem(new FabricItemSettings().group(NeMuelchItemGroups.WARFARE).maxCount(1)));

    public static final Item GLADIUS_BLADE = registerPestCanes("gladiusblade",
            new GladiusBladeItem(ToolMaterials.IRON, NeMuelchConfigInit.CONFIG.gladiusBladeAttackDamage, NeMuelchConfigInit.CONFIG.gladiusBladeAttackSpeed,
                    new FabricItemSettings().group(NeMuelchItemGroups.WARFARE).maxCount(1)));

    public static final Item RADIATUM_CANE = registerPestCanes("radiatumcane",
            new RadiatumCaneItem(new FabricItemSettings().group(NeMuelchItemGroups.WARFARE).maxCount(1)));
    //endregion
    //region admin tools
    public static final Item REFILLER_TOOL = registerItem("refiller_tool",
            new RefillToolItem(new FabricItemSettings().group(NeMuelchItemGroups.HELPERTOOLS).maxCount(1)));

    public static final Item ENTITYTRANSPORT_TOOL = registerItem("entity_transport_tool",
            new EntityTransportToolItem(new FabricItemSettings().group(NeMuelchItemGroups.HELPERTOOLS).maxCount(1)));
    //endregion

    public static final Item GLOVE = registerItem("training_glove",
            new TrainingGloveItem(new FabricItemSettings().group(NeMuelchItemGroups.WARFARE).maxCount(1)));

    public static final Item BANDAGE = registerItem("bandage",
            new BandageItem(new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT).maxCount(8)));

    public static final Item OINTMENT = registerItem("ointment",
            new OintmentItem(new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT).maxCount(8)));

    public static final Item ONION_WAND = registerItem("onion_wand",
            new OnionWandItem(new FabricItemSettings().group(NeMuelchItemGroups.WARFARE).maxCount(1)));

    public static final Item OMINOUS_HEART = registerItem("ominous_heart",
            new OminousHeartItem(new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT).maxCount(1)));

    public static final Item ENVELOPE = registerItem("envelope",
            new EnvelopeItem(new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT).maxCount(1)));

    public static final Item PORTABLE_BARREL = registerItem("portable_barrel",
            new PortableBarrelItem(NeMuelchArmorMaterials.BARREL_MATERIAL, EquipmentSlot.CHEST,
                    new FabricItemSettings().group(NeMuelchItemGroups.WARFARE)));

    public static final Item FORTIFIED_SHIELD = registerItem("fortifiedshield",
            new FortifiedShieldItem(ToolMaterials.IRON));

    public static final Item HONEY_BUCKET = registerItem("honey_bucket",
            new BucketItem(NeMuelchFluids.HONEY_STILL, new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT).maxCount(1)));

    public static final Item SLIME_BUCKET = registerItem("slime_bucket",
            new BucketItem(NeMuelchFluids.SLIME_STILL, new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT).maxCount(1)));

    public static final Item CALL_OF_AGONY = registerItem("call_of_agony",
            new CallOfAgonyItem(new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT).maxCount(1)));

    public static final Item WAND_OF_SOL_POLE = registerItem("wandofsol_pole",
            new WandOfSolPoleItem(new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT)
                    .maxCount(16).fireproof().rarity(Rarity.COMMON)));

    public static final Item WAND_OF_SOL_TANK = registerItem("wandofsol_tank",
            new WandOfSolTankItem(new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT)
                    .maxCount(2).fireproof().rarity(Rarity.RARE)));

    public static final Item WAND_OF_SOL_TABLET = registerItem("wandofsol_tablet",
            new WandOfSolTabletItem(new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT)
                    .maxCount(1).fireproof().rarity(Rarity.RARE)));

    public static final Item WAND_OF_SOL = registerItem("wandofsol",
            new WandOfSolItem(NeMuelchBlocks.WAND_OF_SOL, new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT)
                    .maxCount(1).fireproof().rarity(Rarity.EPIC)));

    public static final Item WATERING_CAN_COPPER = registerItem("watering_can_copper",
            new WateringCanItem(NeMuelchBlocks.WATERING_CAN, new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT)
                    .maxCount(1).rarity(Rarity.COMMON), WateringCanHelper.ItemMaterial.COPPER));
    public static final Item WATERING_CAN_IRON = registerItem("watering_can_iron",
            new WateringCanItem(NeMuelchBlocks.WATERING_CAN, new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT)
                    .maxCount(1).rarity(Rarity.UNCOMMON), WateringCanHelper.ItemMaterial.IRON));
    public static final Item WATERING_CAN_GOLD = registerItem("watering_can_gold",
            new WateringCanItem(NeMuelchBlocks.WATERING_CAN, new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT)
                    .maxCount(1).rarity(Rarity.RARE), WateringCanHelper.ItemMaterial.GOLD));
    public static final Item WATERING_CAN_DIAMOND = registerItem("watering_can_diamond",
            new WateringCanItem(NeMuelchBlocks.WATERING_CAN, new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT)
                    .maxCount(1).rarity(Rarity.RARE), WateringCanHelper.ItemMaterial.DIAMOND));

    public static final Item BLOCKED_BOOK_ARTIFACT = registerItem("blocked_book_artifact",
            new ArtifactItem(new FabricItemSettings().group(NeMuelchItemGroups.WARFARE).maxCount(1).fireproof().rarity(Rarity.EPIC)));

    public static final DropPotBlockItem DROP_POT_BLOCK = registerItem("drop_pot", new DropPotBlockItem(NeMuelchBlocks.DROP_POT,
            new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT).maxCount(1)));
    public static final PotLauncherItem POT_LAUNCHER = registerItem("pot_launcher", new PotLauncherItem(
            new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT).maxCount(1)));
    public static final Item POT_LAUNCHER_LEGS = registerItem("pot_launcher_legs", new Item(
            new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT).maxCount(1)));
    public static final Item POT_LAUNCHER_DEEPSLATE_BASKET = registerItem("pot_launcher_deepslate_basket", new Item(
            new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT).maxCount(1)));
    public static final Item POT_LAUNCHER_LOADER = registerItem("pot_launcher_loader", new Item(
            new FabricItemSettings().group(NeMuelchItemGroups.SUPPORT).maxCount(1)));


    public static final Item PRESTINURAN_INGOT = registerItem("prestinuran_ingot",
            new Item(new FabricItemSettings()));
    public static final RoyalGuardArmorSetItem ROYAL_GUARD_ARMOR_HELMET_ITEM = registerItem("royal_guard_helmet",
            new RoyalGuardArmorSetItem(NeMuelchArmorMaterials.ROYAL_GUARD_ARMOR, EquipmentSlot.HEAD,
                    new FabricItemSettings().group(NeMuelchItemGroups.WARFARE)));
    public static final RoyalGuardArmorSetItem ROYAL_GUARD_ARMOR_CHESTPLATE_ITEM = registerItem("royal_guard_chestplate",
            new RoyalGuardArmorSetItem(NeMuelchArmorMaterials.ROYAL_GUARD_ARMOR, EquipmentSlot.CHEST,
                    new FabricItemSettings().group(NeMuelchItemGroups.WARFARE)));
    public static final RoyalGuardArmorSetItem ROYAL_GUARD_ARMOR_LEGGINGS_ITEM = registerItem("royal_guard_leggings",
            new RoyalGuardArmorSetItem(NeMuelchArmorMaterials.ROYAL_GUARD_ARMOR, EquipmentSlot.LEGS,
                    new FabricItemSettings().group(NeMuelchItemGroups.WARFARE)));
    public static final RoyalGuardArmorSetItem ROYAL_GUARD_ARMOR_BOOTS_ITEM = registerItem("royal_guard_boots",
            new RoyalGuardArmorSetItem(NeMuelchArmorMaterials.ROYAL_GUARD_ARMOR, EquipmentSlot.FEET,
                    new FabricItemSettings().group(NeMuelchItemGroups.WARFARE)));

    public static final Item VERZITRAN_INGOT = registerItem("verzitran_ingot",
            new Item(new FabricItemSettings()));
    public static final FallenGuardArmorSetItem FALLEN_GUARD_ARMOR_HELMET_ITEM = registerItem("fallen_guard_helmet",
            new FallenGuardArmorSetItem(NeMuelchArmorMaterials.FALLEN_GUARD_ARMOR, EquipmentSlot.HEAD,
                    new FabricItemSettings().group(NeMuelchItemGroups.WARFARE)));
    public static final FallenGuardArmorSetItem FALLEN_GUARD_ARMOR_CHESTPLATE_ITEM = registerItem("fallen_guard_chestplate",
            new FallenGuardArmorSetItem(NeMuelchArmorMaterials.FALLEN_GUARD_ARMOR, EquipmentSlot.CHEST,
                    new FabricItemSettings().group(NeMuelchItemGroups.WARFARE)));
    public static final FallenGuardArmorSetItem FALLEN_GUARD_ARMOR_LEGGINGS_ITEM = registerItem("fallen_guard_leggings",
            new FallenGuardArmorSetItem(NeMuelchArmorMaterials.FALLEN_GUARD_ARMOR, EquipmentSlot.LEGS,
                    new FabricItemSettings().group(NeMuelchItemGroups.WARFARE)));
    public static final FallenGuardArmorSetItem FALLEN_GUARD_ARMOR_BOOTS_ITEM = registerItem("fallen_guard_boots",
            new FallenGuardArmorSetItem(NeMuelchArmorMaterials.FALLEN_GUARD_ARMOR, EquipmentSlot.FEET,
                    new FabricItemSettings().group(NeMuelchItemGroups.WARFARE)));


    private static <T extends Item> T registerItem(String name, T item) {
        return Registry.register(Registry.ITEM, new Identifier(NeMuelch.MOD_ID, name), item);
    }

    private static <T extends Item> T registerNemuelchDrink(String name, T item) {
        T registeredItem = registerItem(name, item);
        NEMUELCH_DRINKS.add(registeredItem);
        return registeredItem;
    }

    private static <T extends Item> T registerPestCanes(String name, T item) {
        T registeredItem = registerItem(name, item);
        PEST_CANES.add(registeredItem);
        return registeredItem;
    }


    public static void initialize() {
        // static initialisation
    }
}
