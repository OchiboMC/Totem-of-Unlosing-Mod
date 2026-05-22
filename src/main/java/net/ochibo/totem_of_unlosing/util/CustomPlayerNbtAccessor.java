package net.ochibo.totem_of_unlosing.util;

import net.minecraft.world.item.ItemStack;

public interface CustomPlayerNbtAccessor {
    ItemStack getUnlosingTotem();
    void setUnlosingTotem(ItemStack value);
}