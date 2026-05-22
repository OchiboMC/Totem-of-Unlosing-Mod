package net.ochibo.custom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ochibo.TotemOfUnlosing;

import java.util.List;

public class ModItem {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TotemOfUnlosing.MOD_ID);

    public static final DeferredItem<Item> TOTEM_OF_UNLOSING = ITEMS.register("totem_of_unlosing", 
        () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> TOTEM_OF_UNLOSING_PROTECTED = ITEMS.register("totem_of_unlosing_protected", 
        () -> new TotemOfUnlosingWithStoredItem(new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.UNCOMMON)
            .component(ModComponent.STORED_ITEMS.get(), new StoredItemsComponent(new byte[0]))
            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
            .component(DataComponents.ITEM_NAME, Component.translatable("item.totem_of_unlosing.totem_of_unlosing"))
            .component(DataComponents.LORE, new ItemLore(List.of(Component.translatable("item.totem_of_unlosing.totem_of_unlosing.protected_desc")), List.of()))
    ));
}
