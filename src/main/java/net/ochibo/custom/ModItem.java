package net.ochibo.custom;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.ochibo.TotemOfUnlosing;

import java.util.List;

public class ModItem {
    public static final Item TOTEM_OF_UNLOSING = registerItem("totem_of_unlosing", new Item(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));
    public static final Item TOTEM_OF_UNLOSING_PROTECTED = registerItem("totem_of_unlosing_protected", new TotemOfUnlosingWithStoredItem(new Item.Settings()
            .maxCount(1)
            .rarity(Rarity.UNCOMMON)
            .component(ModComponent.STORED_ITEMS,null)
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,true)
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item.totem-of-unlosing.totem_of_unlosing"))
            .component(DataComponentTypes.LORE, new LoreComponent(List.of(Text.translatable("item.totem-of-unlosing.totem_of_unlosing.protected_desc"))))
    ));

    private static Item registerItem(String id, Item item) {
        return Registry.register(Registries.ITEM,RegistryKey.of(Registries.ITEM.getKey(), Identifier.of(TotemOfUnlosing.MOD_ID,id)), item);
    }

    public static void registerModItem(){
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries ->{
            entries.add(TOTEM_OF_UNLOSING);
        });
    }
}
