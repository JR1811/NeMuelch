package net.shirojr.nemuelch.init;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;
import net.shirojr.nemuelch.item.custom.MuelchItem;
import net.shirojr.nemuelch.item.custom.adminToolItem.*;
import net.shirojr.nemuelch.item.custom.armorAndShieldItem.FortifiedShieldItem;
import net.shirojr.nemuelch.item.custom.armorAndShieldItem.PortableBarrelItem;
import net.shirojr.nemuelch.item.custom.block.CrateBlockItem;
import net.shirojr.nemuelch.item.custom.caneItem.*;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.ArtifactItem;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.CallOfAgonyItem;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.MiasmaItem;
import net.shirojr.nemuelch.item.custom.gloveItem.TrainingGloveItem;
import net.shirojr.nemuelch.item.custom.supportItem.*;
import net.shirojr.nemuelch.util.helper.WateringCanHelper;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public interface NeMuelchItems {
    List<Item> NEMUELCH_ITEMS = new ArrayList<>();
    List<Item> NEMUELCH_VARIATION_BLOCK_ITEMS = new ArrayList<>();
    List<Item> TOOLS = new ArrayList<>();
    List<Item> COMBAT = new ArrayList<>();
    List<Item> FOOD_AND_DRINK = new ArrayList<>();
    List<MiasmaItem> MIASMA_ITEMS = new ArrayList<>();
    List<CrateBlockItem> CRATES = new ArrayList<>();

    List<Item> NEMUELCH_DRINKS = new ArrayList<>();
    List<Item> PEST_CANES = new ArrayList<>();


    Item GREEN_MUELCH = registerFoodAndDrinks("green_muelch",
            new MuelchItem(new Item.Settings().food(NeMuelchFoodComponents.GREEN_MILK).maxCount(1),
                    List.of(), 4)
    );
    Item BROWN_MUELCH = registerFoodAndDrinks("brown_muelch",
            new MuelchItem(new Item.Settings().food(NeMuelchFoodComponents.BROWN_MILK).maxCount(1),
                    List.of(new StatusEffectInstance(StatusEffects.RESISTANCE, 500, 2)), 5)
    );
    Item BLUE_MUELCH = registerFoodAndDrinks("blue_muelch",
            new MuelchItem(new Item.Settings().food(NeMuelchFoodComponents.BLUE_MILK).maxCount(1),
                    List.of(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 2400, 0)), 5)
    );
    Item PINK_MUELCH = registerFoodAndDrinks("pink_muelch",
            new MuelchItem(new Item.Settings().food(NeMuelchFoodComponents.PINK_MILK).maxCount(1),
                    List.of(new StatusEffectInstance(StatusEffects.REGENERATION, 700, 0)), 5)
    );
    Item YELLOW_MUELCH = registerFoodAndDrinks("yellow_muelch",
            new MuelchItem(new Item.Settings().food(NeMuelchFoodComponents.YELLOW_MILK).maxCount(1),
                    List.of(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 100, 3)), 5)
    );
    Item PURPLE_MUELCH = registerFoodAndDrinks("purple_muelch",
            new MuelchItem(new Item.Settings().food(NeMuelchFoodComponents.PURPLE_MILK).maxCount(1),
                    List.of(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 2400, 0),
                            new StatusEffectInstance(StatusEffects.SPEED, 500, 1)), 5)
    );

    BlockItem ADVANCED_FOG = register(
            "advanced_fog", new AdvancedFogBlockItem(new Item.Settings().maxCount(1))
    );


    PestcaneItem PEST_CANE = registerCane("pestcane",
            new PestcaneItem(new Item.Settings().maxCount(1)));
    ArkaduscaneItem ARKADUS_CANE = registerCane("arkaduscane",
            new ArkaduscaneItem(new Item.Settings().maxCount(1)));
    GladiuscaneItem GLADIUS_CANE = registerCane("gladiuscane",
            new GladiuscaneItem(new Item.Settings().maxCount(1)));
    RadiatumCaneItem RADIATUM_CANE = registerCane("radiatumcane",
            new RadiatumCaneItem(new Item.Settings().maxCount(1)));
    GladiusBladeItem GLADIUS_BLADE = registerCane("gladiusblade",
            new GladiusBladeItem(
                    ToolMaterials.IRON,
                    NeMuelchConfigInit.CONFIG.gladiusBladeAttackDamage,
                    NeMuelchConfigInit.CONFIG.gladiusBladeAttackSpeed,
                    new Item.Settings().maxCount(1))
    );


    RefillToolItem REFILLER = register("refiller_tool",
            new RefillToolItem(new Item.Settings().maxCount(1)));
    EntityTransportToolItem ENTITY_TRANSPORTER = register("entity_transport_tool",
            new EntityTransportToolItem(new Item.Settings().maxCount(1)));
    SoundToolItem SOUND_TOOL = register("sound_tool",
            new SoundToolItem(new Item.Settings().maxCount(1)));
    CameraDisplacementToolItem DISPLACEMENT_TOOL = register("displacement_tool",
            new CameraDisplacementToolItem(new Item.Settings().maxCount(1)));


    ArkaduscaneProjectileEntityItem ARKADUSCANE_ENTITY_PROJECTILE = register("arkaduscane_projectile",
            new ArkaduscaneProjectileEntityItem(new Item.Settings())
    );

    TrainingGloveItem GLOVE = register("glove",
            new TrainingGloveItem(new Item.Settings().maxCount(1)));

    BandageItem BANDAGE = register("bandage",
            new BandageItem(new Item.Settings().maxCount(8)));

    OintmentItem OINTMENT = register("ointment",
            new OintmentItem(new Item.Settings().maxCount(8)));

    OminousHeartItem OMINOUS_HEART = register("ominous_heart",
            new OminousHeartItem(new Item.Settings().maxCount(1)));

    PortableBarrelItem PORTABLE_BARREL = register("portable_barrel",
            new PortableBarrelItem(NeMuelchArmorMaterials.BARREL_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Settings()));

    FortifiedShieldItem FORTIFIED_SHIELD = register("fortifiedshield",
            new FortifiedShieldItem(ToolMaterials.IRON));

    CallOfAgonyItem CALL_OF_AGONY = register("call_of_agony",
            new CallOfAgonyItem(new Item.Settings().maxCount(1)));

    WandOfSolPoleItem WAND_OF_SOL_POLE = register("wandofsol_pole",
            new WandOfSolPoleItem(new Item.Settings()
                    .maxCount(16).fireproof().rarity(Rarity.COMMON)));

    WandOfSolTankItem WAND_OF_SOL_TANK = register("wandofsol_tank",
            new WandOfSolTankItem(new Item.Settings()
                    .maxCount(2).fireproof().rarity(Rarity.RARE)));

    WandOfSolTabletItem WAND_OF_SOL_TABLET = register("wandofsol_tablet",
            new WandOfSolTabletItem(new Item.Settings()
                    .maxCount(1).fireproof().rarity(Rarity.RARE)));

    WandOfSolItem WAND_OF_SOL = register("wandofsol",
            new WandOfSolItem(NeMuelchBlocks.WAND_OF_SOL, new Item.Settings()
                    .maxCount(1).fireproof().rarity(Rarity.EPIC)));

    WateringCanItem WATERING_CAN_COPPER = register("watering_can_copper",
            new WateringCanItem(NeMuelchBlocks.WATERING_CAN, new Item.Settings()
                    .maxCount(1).rarity(Rarity.COMMON), WateringCanHelper.ItemMaterial.COPPER));
    WateringCanItem WATERING_CAN_IRON = register("watering_can_iron",
            new WateringCanItem(NeMuelchBlocks.WATERING_CAN, new Item.Settings()
                    .maxCount(1).rarity(Rarity.UNCOMMON), WateringCanHelper.ItemMaterial.IRON));
    WateringCanItem WATERING_CAN_GOLD = register("watering_can_gold",
            new WateringCanItem(NeMuelchBlocks.WATERING_CAN, new Item.Settings()
                    .maxCount(1).rarity(Rarity.RARE), WateringCanHelper.ItemMaterial.GOLD));
    WateringCanItem WATERING_CAN_DIAMOND = register("watering_can_diamond",
            new WateringCanItem(NeMuelchBlocks.WATERING_CAN, new Item.Settings()
                    .maxCount(1).rarity(Rarity.RARE), WateringCanHelper.ItemMaterial.DIAMOND));

    IronScaffoldingItem IRON_SCAFFOLDING = register("iron_scaffolding",
            new IronScaffoldingItem(NeMuelchBlocks.IRON_SCAFFOLDING, new Item.Settings())
    );

    ArtifactItem BLOCKED_BOOK_ARTIFACT = register("blocked_book_artifact",
            new ArtifactItem(new Item.Settings().maxCount(1).fireproof().rarity(Rarity.EPIC)));

    DropPotBlockItem DROP_POT_BLOCK = register("drop_pot", new DropPotBlockItem(NeMuelchBlocks.DROP_POT,
            new Item.Settings().maxCount(1)));
    PotLauncherItem POT_LAUNCHER = register("pot_launcher", new PotLauncherItem(
            new Item.Settings().maxCount(1)));
    Item POT_LAUNCHER_LEGS = register("pot_launcher_legs", new Item(
            new Item.Settings().maxCount(1)));
    Item POT_LAUNCHER_DEEPSLATE_BASKET = register("pot_launcher_deepslate_basket", new Item(
            new Item.Settings().maxCount(1)));
    Item POT_LAUNCHER_LOADER = register("pot_launcher_loader", new Item(
            new Item.Settings().maxCount(1)));

    BookWrapperItem BOOK_WRAPPER = register("book_wrapper", new BookWrapperItem(new Item.Settings().maxCount(1)));

    RottenMeatAppleItem ROTTEN_MEAT_APPLE = register("rotten_meat_apple", new RottenMeatAppleItem(new Item.Settings().maxCount(1)));

    TalismanItem TALISMAN_STAR = register("talisman_star", new TalismanItem(new Item.Settings().maxCount(1), 4));
    TalismanItem TALISMAN_RIBBON = register("talisman_ribbon", new TalismanItem(new Item.Settings().maxCount(1), 3));

    Item LARD = register("lard", new Item(new Item.Settings().food(NeMuelchFoodComponents.LARD)));
    SoapItem SOAP = register("soap", new SoapItem(new Item.Settings().maxCount(1).food(NeMuelchFoodComponents.SOAP), 3));
    SoapItem CREATIVE_SOAP = register("creative_soap", new SoapItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC), -1));

    MeatLumpItem MEAT_LUMP = register("meat_lump",
            new MeatLumpItem(new Item.Settings().food(NeMuelchFoodComponents.MEAT_LUMP), MeatLumpItem.State.DEFAULT)
    );
    MeatLumpItem COOKED_MEAT_LUMP = register("cooked_meat_lump",
            new MeatLumpItem(new Item.Settings().food(NeMuelchFoodComponents.COOKED_MEAT_LUMP), MeatLumpItem.State.COOKED)
    );
    MeatLumpItem ROTTEN_MEAT_LUMP = register("rotten_meat_lump",
            new MeatLumpItem(new Item.Settings().maxCount(16).food(NeMuelchFoodComponents.ROTTEN_MEAT_LUMP), MeatLumpItem.State.ROTTEN)
    );

    MiasmaItem MIASMA_BIG = registerMiasma("miasma_big", new MiasmaItem(new Item.Settings(), MiasmaItem.Type.BIG));
    MiasmaItem MIASMA_MEDIUM = registerMiasma("miasma_medium", new MiasmaItem(new Item.Settings(), MiasmaItem.Type.MEDIUM));
    MiasmaItem MIASMA_SMALL = registerMiasma("miasma_small", new MiasmaItem(new Item.Settings(), MiasmaItem.Type.SMALL));

    CrateBlockItem CRATE_OAK = registerCrate("oak", NeMuelchBlocks.CRATE_OAK);
    CrateBlockItem CRATE_SPRUCE = registerCrate("spruce", NeMuelchBlocks.CRATE_SPRUCE);
    CrateBlockItem CRATE_BIRCH = registerCrate("birch", NeMuelchBlocks.CRATE_BIRCH);
    CrateBlockItem CRATE_JUNGLE = registerCrate("jungle", NeMuelchBlocks.CRATE_JUNGLE);
    CrateBlockItem CRATE_ACACIA = registerCrate("acacia", NeMuelchBlocks.CRATE_ACACIA);
    CrateBlockItem CRATE_CHERRY = registerCrate("cherry", NeMuelchBlocks.CRATE_CHERRY);
    CrateBlockItem CRATE_DARK_OAK = registerCrate("dark_oak", NeMuelchBlocks.CRATE_DARK_OAK);
    CrateBlockItem CRATE_MANGROVE = registerCrate("mangrove", NeMuelchBlocks.CRATE_MANGROVE);


    private static <T extends Item> T register(String name, T entry) {
        T registeredEntry = Registry.register(Registries.ITEM, NeMuelch.getId(name), entry);
        NEMUELCH_ITEMS.add(registeredEntry);
        return registeredEntry;
    }

    private static <T extends Item> T registerCane(String name, T entry) {
        T registeredEntry = register(name, entry);
        PEST_CANES.add(registeredEntry);
        TOOLS.add(registeredEntry);
        COMBAT.add(registeredEntry);
        return registeredEntry;
    }

    private static <T extends Item> T registerFoodAndDrinks(String name, T entry) {
        T registeredEntry = register(name, entry);
        FOOD_AND_DRINK.add(registeredEntry);
        return registeredEntry;
    }

    private static <T extends MiasmaItem> T registerMiasma(String name, T entry) {
        T registeredEntry = register(name, entry);
        MIASMA_ITEMS.add(registeredEntry);
        return registeredEntry;
    }

    private static CrateBlockItem registerCrate(String prefix, CrateBlock block) {
        CrateBlockItem entry = register(prefix + "_crate", new CrateBlockItem(block, new Item.Settings()));
        CRATES.add(entry);
        return entry;
    }

    static void initialize() {
        // static initialisation
    }
}
