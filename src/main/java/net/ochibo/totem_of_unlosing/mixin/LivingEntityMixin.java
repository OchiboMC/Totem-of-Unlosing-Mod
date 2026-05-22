package net.ochibo.totem_of_unlosing.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.gameevent.GameEvent;
import net.ochibo.totem_of_unlosing.TotemOfUnlosingMod;
import net.ochibo.totem_of_unlosing.custom.ModItems;
import net.ochibo.totem_of_unlosing.custom.ZippedItemData;
import net.ochibo.totem_of_unlosing.util.CustomPlayerNbtAccessor;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
    private void tryUseUnlosingTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) return;

        ItemStack itemStack = null;

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemStack2 = self.getItemInHand(hand);
            if (itemStack2.is(Items.TOTEM_OF_UNDYING)) {
                return;
            }
        }

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemStack2 = self.getItemInHand(hand);
            if (itemStack2.is(ModItems.TOTEM_OF_UNLOSING.get())) {
                itemStack = itemStack2.copy();
                itemStack2.shrink(1);
                break;
            }
        }

        if (itemStack != null) {
            CustomPlayerNbtAccessor nbt = ((CustomPlayerNbtAccessor) self);
            if (player instanceof ServerPlayer) {
                self.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                onUseTotem(player, nbt);
            }
            self.level().broadcastEntityEvent(self, TotemOfUnlosingMod.PROTECTED_BY_TOTEM_OF_UNLOSING);
        }
        cir.setReturnValue(itemStack != null);
    }

    @Unique
    private void onUseTotem(Player player, CustomPlayerNbtAccessor nbtAccessor) {
        Map<Integer, ItemStack> stored_item = new HashMap<>();
        List<ZippedItemData> stored_zipped_item = new ArrayList<>();

        NonNullList<ItemStack> main = player.getInventory().items;
        NonNullList<ItemStack> armor = player.getInventory().armor;
        Map<String, List<Integer>> itemLookup = new HashMap<>();
        List<ItemStack> waitingItem = new ArrayList<>();

        for (int i = 0; i < main.size(); i++) {
            var item = main.get(i).copy();
            if (!item.isEmpty()) {
                if (item.is(ModItems.TOTEM_OF_UNLOSING_PROTECTED.get())) {
                    waitingItem.addAll(unpackTotem(item, player));
                    continue;
                }
                String key = generateKey(item);
                itemLookup.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
                stored_item.put(i, item);
            }
        }

        for (int i = 0; i < armor.size(); i++) {
            var item = armor.get(i).copy();
            if (!item.isEmpty()) {
                stored_item.put(i + main.size(), item);
            }
        }

        ItemStack offhandItem = player.getInventory().offhand.get(0).copy();
        if (!offhandItem.isEmpty()) {
            if (offhandItem.is(ModItems.TOTEM_OF_UNLOSING_PROTECTED.get())) {
                waitingItem.addAll(unpackTotem(offhandItem, player));
            } else {
                String key = generateKey(offhandItem);
                itemLookup.computeIfAbsent(key, k -> new ArrayList<>()).add(Inventory.SLOT_OFFHAND);
                stored_item.put(Inventory.SLOT_OFFHAND, offhandItem);
            }
        }

        List<ItemStack> overflowItems = new ArrayList<>();

        for (ItemStack waitingStack : waitingItem) {
            String key = generateKey(waitingStack);

            if (itemLookup.containsKey(key)) {
                for (int slotIndex : itemLookup.get(key)) {
                    ItemStack targetInInv = stored_item.get(slotIndex);

                    int capacity = targetInInv.getMaxStackSize() - targetInInv.getCount();
                    int transferAmount = Math.min(waitingStack.getCount(), capacity);

                    targetInInv.grow(transferAmount);
                    waitingStack.shrink(transferAmount);

                    if (waitingStack.isEmpty()) break;
                }
            }

            if (!waitingStack.isEmpty()) {
                if (stored_item.size() + overflowItems.size() < 60) {
                    overflowItems.add(waitingStack);
                } else {
                    player.drop(waitingStack, true, false);
                }
            }
        }

        stored_item.forEach((slot, stack) -> {
            stored_zipped_item.add(ZippedItemData.of(stack, slot));
        });

        for (ItemStack overflow : overflowItems) {
            stored_zipped_item.add(ZippedItemData.of(overflow, TotemOfUnlosingMod.SHOULD_DROP_SLOT));
        }
        ItemStack newTotem = new ItemStack(ModItems.TOTEM_OF_UNLOSING_PROTECTED.get());
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        byte[] compressedData = ZippedItemData.encodeList(stored_zipped_item, buf);

        CompoundTag tag = newTotem.getOrCreateTag();
        tag.putByteArray("StoredItems", compressedData);

        int xp = 0;
        if (!player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !player.isSpectator()) {
            int i = player.experienceLevel * 6;
            xp = Math.min(i, 85);
        }

        tag.putInt("StoredExp", xp);

        nbtAccessor.setUnlosingTotem(newTotem);
        player.getInventory().clearContent();
    }

    @Unique
    private static String generateKey(ItemStack stack) {
        return BuiltInRegistries.ITEM.getId(stack.getItem()) + "_" + (stack.hasTag() ? stack.getTag().hashCode() : 0);
    }

    @Unique
    private static List<ItemStack> unpackTotem(ItemStack totem, Player player) {
        List<ItemStack> stackList = new ArrayList<>();
        if (!totem.hasTag() || !totem.getTag().contains("StoredItems")) return stackList;
        byte[] data = totem.getTag().getByteArray("StoredItems");
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        ZippedItemData.decodeList(buf).forEach(zippedItemData -> stackList.add(zippedItemData.toStack()));
        TotemOfUnlosingMod.LOGGER.info(stackList.toString());
        return stackList;
    }
}