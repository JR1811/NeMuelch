package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// parts have different functionality
// - sigil allows for signage
// - strip allows for viewing history and makes wrapper re-usable
// - wrapper is container, which will get destroyed after one-time use without strip

public class BookWrapperItem extends Item {
    public static final int STORABLE_ITEMS_AMOUNT = 1;

    public BookWrapperItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack stack = context.getStack();
        BlockPos blockPos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        if (player == null) return super.useOnBlock(context);
        World world = player.getWorld();
        if (!(world instanceof ServerWorld)) return super.useOnBlock(context);
        BlockState targetState = world.getBlockState(blockPos);

        if (Part.SIGIL.canEquip(stack)) {
            if (targetState.isIn(NeMuelchTags.Blocks.SIGIL_COLOR_BLOCKS)) {
                if (targetState.contains(Properties.LIT) && !targetState.get(Properties.LIT)) return ActionResult.PASS;
                world.setBlockState(blockPos, targetState.with(Properties.LIT, false));
            }
            if (world instanceof ServerWorld serverWorld) {
                Part.SIGIL.equip(serverWorld, blockPos, stack, targetState.getMapColor(world, blockPos).color);
            }
        } else {
            return ActionResult.FAIL;
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world instanceof ServerWorld serverWorld) {
            for (ItemStack storedStack : SignedWrapperInfo.getStoredStacks(stack, true)) {
                user.getInventory().offerOrDrop(storedStack);
            }
            serverWorld.playSound(null, user.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.NEUTRAL, 1f, 1f);
            serverWorld.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_LEASH_KNOT_BREAK, SoundCategory.NEUTRAL, 1f, 1f);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        NbtCompound oldNbt = oldStack.copy().getNbt();
        NbtCompound newNbt = newStack.copy().getNbt();
        if (oldNbt != null && !oldNbt.contains(NbtKeys.SIGIL)) {
            return newNbt != null && newNbt.contains(NbtKeys.SIGIL);
        }

        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return;
        tooltip.add(Text.translatable("item.nemuelch.book_wrapper.tooltip.desc1"));
        if (world == null) return;
        List<SignedWrapperInfo> content = SignedWrapperInfo.getContent(stack, false);
        for (int i = 0; i < Math.min(SignedWrapperInfo.MAX_LINES, content.size()); i++) {
            SignedWrapperInfo entry = content.get(i);
            tooltip.add(entry.getOutput(world));
        }
    }

    public static boolean destroyAfterUsage(ItemStack stack) {
        return Part.STRIP.hasPart(stack);
    }

    public static boolean showItemUsageHistory(ItemStack stack) {
        return Part.SIGIL.hasPart(stack);
    }


    public record SignedWrapperInfo(String source, ItemStack stack, String target, long timeOfAddedContent) {
        public static final int MAX_LINES = NeMuelchConfigInit.CONFIG.bookWrapperItemData.getMaxTooltipLineNumber();

        public static void toNbt(List<SignedWrapperInfo> data, NbtCompound nbt) {
            NbtList nbtList = new NbtList();
            if (nbt.contains(NbtKeys.BOOK_WRAPPER_TOOLTIP_CONTENT)) {
                nbtList.addAll(nbt.getList(NbtKeys.BOOK_WRAPPER_TOOLTIP_CONTENT, NbtElement.COMPOUND_TYPE));
            }
            for (SignedWrapperInfo entry : data) {
                NbtCompound entryNbt = new NbtCompound();
                entryNbt.putString(NbtKeys.SOURCE_NAME, entry.source);
                NbtCompound stackNbt = new NbtCompound();
                entry.stack.writeNbt(stackNbt);
                entryNbt.put(NbtKeys.ITEM, stackNbt);
                entryNbt.putString(NbtKeys.TARGET_NAME, entry.target);
                entryNbt.putLong(NbtKeys.TIME_OF_ADDED_CONTENT, entry.timeOfAddedContent);

                nbtList.add(entryNbt);
            }
            nbt.put(NbtKeys.BOOK_WRAPPER_TOOLTIP_CONTENT, nbtList);
        }

        public static List<SignedWrapperInfo> fromNbt(@Nullable NbtCompound nbt) {
            List<SignedWrapperInfo> signedWrapperInfos = new ArrayList<>();
            if (nbt == null || !nbt.contains(NbtKeys.BOOK_WRAPPER_TOOLTIP_CONTENT)) {
                return signedWrapperInfos;
            }
            for (NbtElement nbtElement : nbt.getList(NbtKeys.BOOK_WRAPPER_TOOLTIP_CONTENT, NbtElement.COMPOUND_TYPE)) {
                NbtCompound entryNbt = (NbtCompound) nbtElement;
                String playerName = entryNbt.getString(NbtKeys.SOURCE_NAME);
                ItemStack content = ItemStack.fromNbt(entryNbt.getCompound(NbtKeys.ITEM));
                String targetName = entryNbt.getString(NbtKeys.TARGET_NAME);
                long timeOfAddedContent = entryNbt.getLong(NbtKeys.TIME_OF_ADDED_CONTENT);
                signedWrapperInfos.add(new SignedWrapperInfo(playerName, content, targetName, timeOfAddedContent));
            }
            return signedWrapperInfos;
        }

        public Text getOutput(World world) {
            String formattedTime;
            long seconds = (world.getTime() - timeOfAddedContent) / 20;
            if (seconds < 60) {
                formattedTime = seconds + " seconds";
            } else {
                long minutes = seconds / 60;
                if (minutes < 60) {
                    formattedTime = minutes + " minute" + (minutes != 1 ? "s" : "");
                } else {
                    long hours = minutes / 60;
                    if (hours < 24) {
                        formattedTime = hours + " hour" + (hours != 1 ? "s" : "");
                    } else {
                        long days = hours / 24;
                        formattedTime = days + " day" + (days != 1 ? "s" : "");
                    }
                }
            }
            return Text.literal("From %s To %s - %s - %s ago".formatted(source, target, stack, formattedTime));
        }

        public static void addContent(World world, LivingEntity source, LivingEntity target, ItemStack wrapperStack, ItemStack... contentStacks) {
            List<SignedWrapperInfo> signedWrapperInfoList = new ArrayList<>();
            NbtCompound nbt = wrapperStack.getNbt();
            if (nbt != null && nbt.contains(NbtKeys.BOOK_WRAPPER_TOOLTIP_CONTENT)) {
                signedWrapperInfoList.addAll(SignedWrapperInfo.fromNbt(nbt));
            }
            for (ItemStack contentStack : contentStacks) {
                SignedWrapperInfo entry = new SignedWrapperInfo(source.getName().getString(), contentStack, target.getName().getString(), world.getTime());
                signedWrapperInfoList.add(entry);
            }
            SignedWrapperInfo.toNbt(signedWrapperInfoList, wrapperStack.getOrCreateNbt());
        }

        public static List<SignedWrapperInfo> getContent(ItemStack wrappedStack, boolean removeFromWrapper) {
            NbtCompound nbt = wrappedStack.getNbt();
            List<SignedWrapperInfo> signedWrapperInfos = SignedWrapperInfo.fromNbt(nbt);
            if (removeFromWrapper && nbt != null) {
                nbt.remove(NbtKeys.BOOK_WRAPPER_TOOLTIP_CONTENT);
            }
            return signedWrapperInfos;
        }

        public static List<ItemStack> getStoredStacks(ItemStack wrapperStack, boolean removeFromWrapper) {
            List<ItemStack> stacks = new ArrayList<>();
            List<SignedWrapperInfo> content = getContent(wrapperStack, removeFromWrapper);
            for (SignedWrapperInfo entry : content) {
                stacks.add(entry.stack.copy());
            }
            return stacks;
        }


        public static List<ItemStack> getStoredStacks(List<SignedWrapperInfo> data) {
            List<ItemStack> result = new ArrayList<>();
            for (SignedWrapperInfo entry : data) {
                result.add(entry.stack);
            }
            return result;
        }
    }

    public enum Part implements StringIdentifiable {
        WRAPPER("wrapper", NbtKeys.WRAPPER, SoundEvents.ITEM_BOOK_PAGE_TURN, 16639931, Set.of()),
        STRIP("wrapper_strip", NbtKeys.STRIP, SoundEvents.ENTITY_LEASH_KNOT_PLACE, 3847130, Set.of(WRAPPER)),
        SIGIL("wrapper_sigil", NbtKeys.SIGIL, SoundEvents.BLOCK_SLIME_BLOCK_HIT, 11546150, Set.of(WRAPPER, STRIP));

        private final String name;
        private final String nbtKey;
        private final SoundEvent sound;
        private final int defaultColor;
        private final Set<Part> requiredParts;

        Part(String name, String nbtKey, SoundEvent sound, int defaultColor, Set<Part> requiredParts) {
            this.name = name;
            this.nbtKey = nbtKey;
            this.sound = sound;
            this.defaultColor = defaultColor;
            this.requiredParts = requiredParts;
        }

        public String getNbtKey() {
            return nbtKey;
        }

        public SoundEvent getSound() {
            return sound;
        }

        public int getDefaultColor() {
            return defaultColor;
        }

        public Set<Part> getRequiredParts() {
            return requiredParts;
        }

        @Override
        public String asString() {
            return name;
        }

        public static Part fromString(String name) {
            for (Part entry : Part.values()) {
                if (entry.asString().equals(name)) return entry;
            }
            throw new IllegalStateException("Invalid Identifier for BookWrapperItem Part recipe");
        }

        @Nullable
        public Integer getColor(ItemStack stack) {
            NbtCompound nbt = stack.getNbt();
            if (nbt == null || !nbt.contains(nbtKey)) return null;
            return nbt.getInt(nbtKey);
        }

        public void setColor(ItemStack stack, @Nullable Integer color) {
            stack.getOrCreateNbt().putInt(nbtKey, color == null ? this.defaultColor : color);
        }

        public void equip(ServerWorld world, BlockPos pos, ItemStack stack, int color) {
            setColor(stack, color);
            world.playSound(null, pos, sound, SoundCategory.NEUTRAL, 1f, 1f);
        }

        public boolean canEquip(ItemStack stack) {
            for (Part requiredPart : getRequiredParts()) {
                Integer color = requiredPart.getColor(stack);
                if (color == null) return false;
            }
            return true;
        }

        public void remove(ItemStack stack) {
            NbtCompound nbt = stack.getNbt();
            if (nbt == null) return;
            nbt.remove(nbtKey);
        }

        public boolean hasPart(ItemStack stack) {
            NbtCompound nbt = stack.getNbt();
            return nbt != null && nbt.contains(nbtKey);
        }

        /**
         * Closely related to {@link DyeableItem#blendAndSetColor(ItemStack, List)}
         */
        @Nullable
        public Integer getBlendedColor(ItemStack bookWrapperItemStack, List<DyeItem> dyeItems) {
            if (!(bookWrapperItemStack.getItem() instanceof BookWrapperItem)) return null;

            int[] rgbSum = new int[3];
            int maxBrightnessSum = 0;
            int colorCount = 0;

            Integer existingColor = this.getColor(bookWrapperItemStack);
            if (existingColor != null) {
                float r = ((existingColor >> 16) & 0xFF) / 255.0F;
                float g = ((existingColor >> 8) & 0xFF) / 255.0F;
                float b = (existingColor & 0xFF) / 255.0F;
                maxBrightnessSum += (int) (Math.max(r, Math.max(g, b)) * 255.0F);
                rgbSum[0] += (int) (r * 255.0F);
                rgbSum[1] += (int) (g * 255.0F);
                rgbSum[2] += (int) (b * 255.0F);
                colorCount++;
            }

            for (DyeItem dyeItem : dyeItems) {
                float[] components = dyeItem.getColor().getColorComponents();
                int r = (int) (components[0] * 255.0F);
                int g = (int) (components[1] * 255.0F);
                int b = (int) (components[2] * 255.0F);
                maxBrightnessSum += Math.max(r, Math.max(g, b));
                rgbSum[0] += r;
                rgbSum[1] += g;
                rgbSum[2] += b;
                colorCount++;
            }

            if (colorCount == 0) return null;

            int averageR = rgbSum[0] / colorCount;
            int averageG = rgbSum[1] / colorCount;
            int averageB = rgbSum[2] / colorCount;

            float averageMaxBrightness = (float) maxBrightnessSum / colorCount;
            float currentMaxComponent = Math.max(averageR, Math.max(averageG, averageB));
            averageR = (int) (averageR * averageMaxBrightness / currentMaxComponent);
            averageG = (int) (averageG * averageMaxBrightness / currentMaxComponent);
            averageB = (int) (averageB * averageMaxBrightness / currentMaxComponent);
            return (averageR << 16) | (averageG << 8) | averageB;
        }
    }
}
