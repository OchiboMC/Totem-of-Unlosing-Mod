package net.ochibo.custom;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.ochibo.TotemOfUnlosing;
import net.ochibo.util.CustomPlayerNbtAccessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModEvents {

    public static void registerModServerEvents() {
        ServerPlayerEvents.AFTER_RESPAWN.register(ServerEvents::onPlayerRespawned);
    }

    public static class ServerEvents{
        private static void onPlayerRespawned(
                ServerPlayerEntity oldPlayer,
                ServerPlayerEntity newPlayer,
                boolean alive
        ) {
            if (!((CustomPlayerNbtAccessor)oldPlayer).getWasProtectedItem()) return;
            DefaultedList<ItemStack> main = oldPlayer.getInventory().main;
            DefaultedList<ItemStack> armor = oldPlayer.getInventory().armor;
            Map<String, List<Integer>> itemLookup = new HashMap<>();
            List<ItemStack> waitingItem = new ArrayList<>();
            for (int i = 0; i < main.size(); i++) {
                var item = main.get(i);
                if (!item.isEmpty() || !(item.getCount() >= item.getMaxCount())) {
                    if (item.isOf(ModItem.TOTEM_OF_UNLOSING_PROTECTED)){
                        waitingItem.addAll(unpackTotem(item,oldPlayer));
                        continue;
                    }
                    String key = generateKey(item);
                    itemLookup.computeIfAbsent(key,k -> new ArrayList<>()).add(i);
                }
            }
            ItemStack offhandItem = oldPlayer.getInventory().offHand.getFirst();
            if (!offhandItem.isEmpty() || !(offhandItem.getCount() >= offhandItem.getMaxCount())) {
                if (offhandItem.isOf(ModItem.TOTEM_OF_UNLOSING_PROTECTED)){
                    waitingItem.addAll(unpackTotem(offhandItem,oldPlayer));
                }
                else {
                    String key = generateKey(offhandItem);
                    itemLookup.computeIfAbsent(key,k -> new ArrayList<>()).add(PlayerInventory.OFF_HAND_SLOT);
                }
            }

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

                }
            }



            ItemStack newTotem = new ItemStack(ModItem.TOTEM_OF_UNLOSING_PROTECTED);
            RegistryByteBuf buf = new RegistryByteBuf(Unpooled.buffer(), newPlayer.getRegistryManager());

            List<ZippedItemData> stored_item = new ArrayList<>();
            byte[] compressedData = ZippedItemData.encodeList(stored_item, buf);
            newTotem.set(ModComponent.STORED_ITEMS,compressedData);
            newPlayer.getInventory().main.set(0,newTotem);
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
}
