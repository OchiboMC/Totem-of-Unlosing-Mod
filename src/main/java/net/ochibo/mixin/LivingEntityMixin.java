package net.ochibo.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.event.GameEvent;
import net.ochibo.TotemOfUnlosing;
import net.ochibo.custom.ModComponent;
import net.ochibo.custom.ModItem;
import net.ochibo.custom.ZippedItemData;
import net.ochibo.util.CustomPlayerNbtAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "tryUseTotem",at = @At("HEAD"),cancellable = true)
    private void tryUseUnlosingTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir){
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof PlayerEntity player)) return;

        ItemStack itemStack = null;

        for (Hand hand : Hand.values()) {
            ItemStack itemStack2 = self.getStackInHand(hand);
            if (itemStack2.isOf(ModItem.TOTEM_OF_UNLOSING)) {
                itemStack = itemStack2.copy();
                itemStack2.decrement(1);
                break;
            }
        }

        if (itemStack != null) {
            // トーテムがある場合の処理
            CustomPlayerNbtAccessor nbt = ((CustomPlayerNbtAccessor) self);
            if (player instanceof ServerPlayerEntity) {
                self.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
                onUseTotem(player,nbt);
            }
            self.getWorld().sendEntityStatus(self, TotemOfUnlosing.PROTECTED_BY_TOTEM_OF_UNLOSING);
        }
        cir.setReturnValue(itemStack != null);
    }

    private void onUseTotem(PlayerEntity player,CustomPlayerNbtAccessor nbtAccessor){
        List<ZippedItemData> stored_item = new ArrayList<>();

        DefaultedList<ItemStack> main = player.getInventory().main;
        DefaultedList<ItemStack> armor = player.getInventory().armor;
        Map<String, List<Integer>> itemLookup = new HashMap<>();
        List<ItemStack> waitingItem = new ArrayList<>();
        //main
        for (int i = 0; i < main.size(); i++) {
            var item = main.get(i);
            if (!item.isEmpty() || !(item.getCount() >= item.getMaxCount())) {
                if (item.isOf(ModItem.TOTEM_OF_UNLOSING_PROTECTED)){
                    waitingItem.addAll(unpackTotem(item,player));
                    continue;
                }
                String key = generateKey(item);
                itemLookup.computeIfAbsent(key,k -> new ArrayList<>()).add(i);
            }
        }

        //armor
        for (int i = 0; i < armor.size(); i++) {
            var item = armor.get(i);
            if (!item.isEmpty()) {
                stored_item.add(ZippedItemData.of(item,i+main.size()));
            }
        }

        //offhand
        ItemStack offhandItem = player.getInventory().offHand.getFirst();
        if (!offhandItem.isEmpty() || !(offhandItem.getCount() >= offhandItem.getMaxCount())) {
            if (offhandItem.isOf(ModItem.TOTEM_OF_UNLOSING_PROTECTED)){
                waitingItem.addAll(unpackTotem(offhandItem,player));
            }
            else {
                String key = generateKey(offhandItem);
                itemLookup.computeIfAbsent(key,k -> new ArrayList<>()).add(PlayerInventory.OFF_HAND_SLOT);
            }
        }

        // store
        for (ItemStack waitingStack : waitingItem) {
            String key = generateKey(waitingStack);

            if (itemLookup.containsKey(key)) {
                for (int slotIndex : itemLookup.get(key)) {
                    ItemStack targetInInv = main.get(slotIndex);

                    int capacity = targetInInv.getMaxCount() - targetInInv.getCount();
                    int transferAmount = Math.min(waitingStack.getCount(), capacity);

                    targetInInv.increment(transferAmount);
                    waitingStack.decrement(transferAmount);

                    if (waitingStack.isEmpty()) break;
                }
            }

            if (!waitingStack.isEmpty()) {
                if (stored_item.size() < 60){
                    stored_item.add(ZippedItemData.of(waitingStack,TotemOfUnlosing.SHOULD_DROP_SLOT));
                }else {
                    player.dropItem(waitingStack,true,false);
                }
            }
        }

        ItemStack newTotem = new ItemStack(ModItem.TOTEM_OF_UNLOSING_PROTECTED);
        RegistryByteBuf buf = new RegistryByteBuf(Unpooled.buffer(), player.getRegistryManager());
        byte[] compressedData = ZippedItemData.encodeList(stored_item, buf);
        newTotem.set(ModComponent.STORED_ITEMS,compressedData);
        nbtAccessor.setUnlosingTotem(newTotem);
    }

    private static String generateKey(ItemStack stack){
        return Item.getRawId(stack.getItem()) + "_" + stack.getComponents().hashCode();
    }

    private static List<ItemStack> unpackTotem(ItemStack totem, PlayerEntity player) {
        byte[] data = totem.get(ModComponent.STORED_ITEMS);
        List<ItemStack> stackList = new ArrayList<>();
        if (data == null) return stackList;
        RegistryByteBuf buf = new RegistryByteBuf(
                Unpooled.wrappedBuffer(data),
                player.getRegistryManager()
        );
        ZippedItemData.decodeList(buf).forEach(zippedItemData -> stackList.add(zippedItemData.toStack()));
        return stackList;
    }
}
