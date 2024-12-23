package net.shirojr.nemuelch.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.fluid.NeMuelchFluid;
import net.shirojr.nemuelch.init.ConfigInit;
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
import net.shirojr.nemuelch.item.materials.NeMuelchArmorMaterials;
import net.shirojr.nemuelch.util.helper.WateringCanHelper;

@SuppressWarnings("unused")
public class NeMuelchItems {
    //region muelch
    public static final Item GREEN_MUELCH = registerItem("green_muelch",
            new NeMuelchGreenItem(new FabricItemSettings().group(NeMuelchItemGroup.NEMUELCH).food(NeMuelchDrinkComponents.GREEN_MILK).maxCount(1)));

    public static final Item BROWN_MUELCH = registerItem("brown_muelch",
            new NeMuelchBrownItem(new FabricItemSettings().group(NeMuelchItemGroup.NEMUELCH).food(NeMuelchDrinkComponents.BROWN_MILK).maxCount(1)));

    public static final Item BLUE_MUELCH = registerItem("blue_muelch",
            new NeMuelchBlueItem(new FabricItemSettings().group(NeMuelchItemGroup.NEMUELCH).food(NeMuelchDrinkComponents.BLUE_MILK).maxCount(1)));

    public static final Item PINK_MUELCH = registerItem("pink_muelch",
            new NeMuelchPinkItem(new FabricItemSettings().group(NeMuelchItemGroup.NEMUELCH).food(NeMuelchDrinkComponents.PINK_MILK).maxCount(1)));

    public static final Item YELLOW_MUELCH = registerItem("yellow_muelch",
            new NeMuelchYellowItem(new FabricItemSettings().group(NeMuelchItemGroup.NEMUELCH).food(NeMuelchDrinkComponents.YELLOW_MILK).maxCount(1)));

    public static final Item PURPLE_MUELCH = registerItem("purple_muelch",
            new NeMuelchPurpleItem(new FabricItemSettings().group(NeMuelchItemGroup.NEMUELCH).food(NeMuelchDrinkComponents.PURPLE_MILK).maxCount(1)));
    //endregion
    //region canes
    public static final Item PEST_CANE = registerItem("pestcane",
            new PestcaneItem(new FabricItemSettings().group(NeMuelchItemGroup.WARFARE).maxCount(1)));

    public static final Item ARKADUS_CANE = registerItem("arkaduscane",
            new ArkaduscaneItem(new FabricItemSettings().group(NeMuelchItemGroup.WARFARE).maxCount(1)));

    public static final Item ARKADUSCANE_ENTITY_PROJECTILE_ITEM = registerItem("arkaduscane_projectile",
            new ArkaduscaneProjectileEntityItem(new FabricItemSettings()));

    public static final Item GLADIUS_CANE = registerItem("gladiuscane",
            new GladiuscaneItem(new FabricItemSettings().group(NeMuelchItemGroup.WARFARE).maxCount(1)));

    public static final Item GLADIUS_BLADE = registerItem("gladiusblade",
            new GladiusBladeItem(ToolMaterials.IRON, ConfigInit.CONFIG.gladiusBladeAttackDamage, ConfigInit.CONFIG.gladiusBladeAttackSpeed,
                    new FabricItemSettings().group(NeMuelchItemGroup.WARFARE).maxCount(1)));

    public static final Item RADIATUM_CANE = registerItem("radiatumcane",
            new RadiatumCaneItem(new FabricItemSettings().group(NeMuelchItemGroup.WARFARE).maxCount(1)));
    //endregion
    //region admin tools
    public static final Item REFILLER_TOOL = registerItem("refiller_tool",
            new RefillToolItem(new FabricItemSettings().group(NeMuelchItemGroup.HELPERTOOLS).maxCount(1)));

    public static final Item ENTITYTRANSPORT_TOOL = registerItem("entity_transport_tool",
            new EntityTransportToolItem(new FabricItemSettings().group(NeMuelchItemGroup.HELPERTOOLS).maxCount(1)));
    //endregion

    public static final Item GLOVE_ITEM = registerItem("training_glove",
            new TrainingGloveItem(new FabricItemSettings().group(NeMuelchItemGroup.WARFARE).maxCount(1)));


    public static final Item BANDAGE_ITEM = registerItem("bandage",
            new BandageItem(new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT).maxCount(8)));

    public static final Item OINTMENT_ITEM = registerItem("ointment",
            new OintmentItem(new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT).maxCount(8)));

    public static final Item ONION_WAND_ITEM = registerItem("onion_wand",
            new OnionWandItem(new FabricItemSettings().group(NeMuelchItemGroup.WARFARE).maxCount(1)));

    public static final Item OMINOUS_HEART = registerItem("ominous_heart",
            new OminousHeartItem(new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT).maxCount(1)));

    public static final Item ENVELOPE = registerItem("envelope",
            new EnvelopeItem(new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT).maxCount(1)));

    public static final Item PORTABLE_BARREL = registerItem("portable_barrel",
            new PortableBarrelItem(NeMuelchArmorMaterials.BARREL_MATERIAL, EquipmentSlot.CHEST,
                    new FabricItemSettings().group(NeMuelchItemGroup.WARFARE)));

    public static final Item FORTIFIED_SHIELD = registerItem("fortifiedshield",
            new FortifiedShieldItem(ToolMaterials.IRON));

    public static final Item HONEY_BUCKET = registerItem("honey_bucket",
            new BucketItem(NeMuelchFluid.HONEY_STILL, new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT).maxCount(1)));

    public static final Item SLIME_BUCKET = registerItem("slime_bucket",
            new BucketItem(NeMuelchFluid.SLIME_STILL, new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT).maxCount(1)));

    public static final Item CALL_OF_AGONY = registerItem("call_of_agony",
            new CallOfAgonyItem(new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT).maxCount(1)));

    public static final Item WAND_OF_SOL_POLE = registerItem("wandofsol_pole",
            new WandOfSolPoleItem(new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT)
                    .maxCount(16).fireproof().rarity(Rarity.COMMON)));

    public static final Item WAND_OF_SOL_TANK = registerItem("wandofsol_tank",
            new WandOfSolTankItem(new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT)
                    .maxCount(2).fireproof().rarity(Rarity.RARE)));

    public static final Item WAND_OF_SOL_TABLET = registerItem("wandofsol_tablet",
            new WandOfSolTabletItem(new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT)
                    .maxCount(1).fireproof().rarity(Rarity.RARE)));

    public static final Item WAND_OF_SOL = registerItem("wandofsol",
            new WandOfSolItem(NeMuelchBlocks.WAND_OF_SOL, new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT)
                    .maxCount(1).fireproof().rarity(Rarity.EPIC)));

    public static final Item WATERING_CAN_COPPER = registerItem("watering_can_copper",
            new WateringCanItem(NeMuelchBlocks.WATERING_CAN, new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT)
                    .maxCount(1).rarity(Rarity.COMMON), WateringCanHelper.ItemMaterial.COPPER));
    public static final Item WATERING_CAN_IRON = registerItem("watering_can_iron",
            new WateringCanItem(NeMuelchBlocks.WATERING_CAN, new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT)
                    .maxCount(1).rarity(Rarity.UNCOMMON), WateringCanHelper.ItemMaterial.IRON));
    public static final Item WATERING_CAN_GOLD = registerItem("watering_can_gold",
            new WateringCanItem(NeMuelchBlocks.WATERING_CAN, new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT)
                    .maxCount(1).rarity(Rarity.RARE), WateringCanHelper.ItemMaterial.GOLD));
    public static final Item WATERING_CAN_DIAMOND = registerItem("watering_can_diamond",
            new WateringCanItem(NeMuelchBlocks.WATERING_CAN, new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT)
                    .maxCount(1).rarity(Rarity.RARE), WateringCanHelper.ItemMaterial.DIAMOND));

    public static final Item TNT_STICK = registerItem("tnt_stick",
            new TntStickItem(new FabricItemSettings().maxCount(1).group(NeMuelchItemGroup.SUPPORT)));

    public static final Item BLOCKED_BOOK_ARTIFACT = registerItem("blocked_book_artifact",
            new ArtifactItem(new FabricItemSettings().group(NeMuelchItemGroup.WARFARE).maxCount(1).fireproof().rarity(Rarity.EPIC)));

    public static final DropPotBlockItem DROP_POT_BLOCK = registerItem("drop_pot", new DropPotBlockItem(NeMuelchBlocks.DROP_POT,
            new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT).maxCount(1)));
    public static final PotLauncherItem POT_LAUNCHER = registerItem("pot_launcher", new PotLauncherItem(
            new FabricItemSettings().group(NeMuelchItemGroup.SUPPORT).maxCount(1)));


    public static final Item PRESTINURAN_INGOT = registerItem("prestinuran_ingot",
            new Item(new FabricItemSettings()));
    public static final RoyalGuardArmorSetItem ROYAL_GUARD_ARMOR_HELMET_ITEM = registerItem("royal_guard_helmet",
            new RoyalGuardArmorSetItem(NeMuelchArmorMaterials.ROYAL_GUARD_ARMOR, EquipmentSlot.HEAD,
                    new FabricItemSettings().group(NeMuelchItemGroup.WARFARE)));
    public static final RoyalGuardArmorSetItem ROYAL_GUARD_ARMOR_CHESTPLATE_ITEM = registerItem("royal_guard_chestplate",
            new RoyalGuardArmorSetItem(NeMuelchArmorMaterials.ROYAL_GUARD_ARMOR, EquipmentSlot.CHEST,
                    new FabricItemSettings().group(NeMuelchItemGroup.WARFARE)));
    public static final RoyalGuardArmorSetItem ROYAL_GUARD_ARMOR_LEGGINGS_ITEM = registerItem("royal_guard_leggings",
            new RoyalGuardArmorSetItem(NeMuelchArmorMaterials.ROYAL_GUARD_ARMOR, EquipmentSlot.LEGS,
                    new FabricItemSettings().group(NeMuelchItemGroup.WARFARE)));
    public static final RoyalGuardArmorSetItem ROYAL_GUARD_ARMOR_BOOTS_ITEM = registerItem("royal_guard_boots",
            new RoyalGuardArmorSetItem(NeMuelchArmorMaterials.ROYAL_GUARD_ARMOR, EquipmentSlot.FEET,
                    new FabricItemSettings().group(NeMuelchItemGroup.WARFARE)));

    public static final Item VERZITRAN_INGOT = registerItem("verzitran_ingot",
            new Item(new FabricItemSettings()));
    public static final FallenGuardArmorSetItem FALLEN_GUARD_ARMOR_HELMET_ITEM = registerItem("fallen_guard_helmet",
            new FallenGuardArmorSetItem(NeMuelchArmorMaterials.FALLEN_GUARD_ARMOR, EquipmentSlot.HEAD,
                    new FabricItemSettings().group(NeMuelchItemGroup.WARFARE)));
    public static final FallenGuardArmorSetItem FALLEN_GUARD_ARMOR_CHESTPLATE_ITEM = registerItem("fallen_guard_chestplate",
            new FallenGuardArmorSetItem(NeMuelchArmorMaterials.FALLEN_GUARD_ARMOR, EquipmentSlot.CHEST,
                    new FabricItemSettings().group(NeMuelchItemGroup.WARFARE)));
    public static final FallenGuardArmorSetItem FALLEN_GUARD_ARMOR_LEGGINGS_ITEM = registerItem("fallen_guard_leggings",
            new FallenGuardArmorSetItem(NeMuelchArmorMaterials.FALLEN_GUARD_ARMOR, EquipmentSlot.LEGS,
                    new FabricItemSettings().group(NeMuelchItemGroup.WARFARE)));
    public static final FallenGuardArmorSetItem FALLEN_GUARD_ARMOR_BOOTS_ITEM = registerItem("fallen_guard_boots",
            new FallenGuardArmorSetItem(NeMuelchArmorMaterials.FALLEN_GUARD_ARMOR, EquipmentSlot.FEET,
                    new FabricItemSettings().group(NeMuelchItemGroup.WARFARE)));


    //preparing items for loading
    private static <T extends Item> T registerItem(String name, T item) {
        return Registry.register(Registry.ITEM, new Identifier(NeMuelch.MOD_ID, name), item);
    }

    public static void registerModItems() {
        NeMuelch.LOGGER.info("Registering " + NeMuelch.MOD_ID + " Mod items");
    }
}
