package net.ochibo.util;

import net.minecraft.item.ItemStack;

public interface CustomPlayerNbtAccessor {
    ItemStack getUnlosingTotem();
    void setUnlosingTotem(ItemStack value);
}