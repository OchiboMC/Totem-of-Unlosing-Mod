package net.ochibo.totem_of_unlosing.custom;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public record ZippedItemData(
        Item item,
        int count,
        int slot,
        CompoundTag tag
) {
    public static ZippedItemData of(ItemStack stack, int slotIndex) {
        return new ZippedItemData(
                stack.getItem(),
                stack.getCount(),
                slotIndex,
                stack.hasTag() ? stack.getTag().copy() : null
        );
    }

    public ItemStack toStack() {
        ItemStack stack = new ItemStack(this.item, this.count);
        if (this.tag != null) {
            stack.setTag(this.tag.copy());
        }
        return stack;
    }

    public static byte[] encodeList(List<ZippedItemData> list, FriendlyByteBuf buf) {
        buf.writeVarInt(list.size());
        for (ZippedItemData data : list) {
            buf.writeVarInt(BuiltInRegistries.ITEM.getId(data.item()));
            buf.writeByte(data.count());
            buf.writeByte(data.slot());
            buf.writeNbt(data.tag());
        }
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    public static List<ZippedItemData> decodeList(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ZippedItemData> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Item item = BuiltInRegistries.ITEM.byId(buf.readVarInt());
            int count = buf.readByte();
            int slot = buf.readByte();
            CompoundTag tag = buf.readNbt();

            list.add(new ZippedItemData(item, count, slot, tag));
        }
        return list;
    }
}