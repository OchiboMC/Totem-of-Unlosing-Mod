package net.ochibo.custom;

import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import java.util.ArrayList;
import java.util.List;

public record ZippedItemData(
        RegistryEntry<Item> item,
        int count,
        int slot,
        ComponentChanges component
) {
    public static ZippedItemData of(ItemStack stack, int slotIndex) {
        return new ZippedItemData(
                stack.getRegistryEntry(),
                stack.getCount(),
                slotIndex,
                stack.getComponentChanges()
        );
    }

    public ItemStack toStack() {
        return new ItemStack(this.item, this.count, this.component);
    }

    public static byte[] encodeList(List<ZippedItemData> list, RegistryByteBuf buf) {
        buf.writeVarInt(list.size());
        for (ZippedItemData data : list) {
            buf.writeVarInt(Registries.ITEM.getRawId(data.item().value()));
            buf.writeByte(data.count());
            buf.writeByte(data.slot());
            ComponentChanges.PACKET_CODEC.encode(buf, data.component());
        }
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    public static List<ZippedItemData> decodeList(RegistryByteBuf buf) {
        int size = buf.readVarInt();
        List<ZippedItemData> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            RegistryEntry<Item> itemEntry = Registries.ITEM.getEntry(buf.readVarInt()).orElseThrow();
            int count = buf.readByte();
            int slot = buf.readByte();
            ComponentChanges component = ComponentChanges.PACKET_CODEC.decode(buf);
            list.add(new ZippedItemData(itemEntry, count, slot, component));
        }
        return list;
    }
}