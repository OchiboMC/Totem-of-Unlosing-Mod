package net.ochibo.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record ZippedItemData(
        Holder<Item> item,
        int count,
        int slot,
        DataComponentPatch component
) {
    public static ZippedItemData of(ItemStack stack, int slotIndex) {
        return new ZippedItemData(
                stack.getItemHolder(),
                stack.getCount(),
                slotIndex,
                stack.getComponentsPatch()
        );
    }

    public ItemStack toStack() {
        return new ItemStack(this.item, this.count, this.component);
    }

    public static byte[] encodeList(List<ZippedItemData> list, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(list.size());
        for (ZippedItemData data : list) {
            buf.writeVarInt(BuiltInRegistries.ITEM.getId(data.item().value()));
            buf.writeVarInt(data.count());
            buf.writeVarInt(data.slot());
            DataComponentPatch.STREAM_CODEC.encode(buf, data.component());
        }
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    public static List<ZippedItemData> decodeList(RegistryFriendlyByteBuf buf) {
        if (!buf.isReadable()) {
            return new ArrayList<>();
        }
        int size = buf.readVarInt();
        List<ZippedItemData> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Item item = Item.byId(buf.readVarInt());
            Holder<Item> itemEntry = item.builtInRegistryHolder();
            int count = buf.readVarInt();
            int slot = buf.readVarInt();
            DataComponentPatch component = DataComponentPatch.STREAM_CODEC.decode(buf);
            list.add(new ZippedItemData(itemEntry, count, slot, component));
        }
        return list;
    }
}