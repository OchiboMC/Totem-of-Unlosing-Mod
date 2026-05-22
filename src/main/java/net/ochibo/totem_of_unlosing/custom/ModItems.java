package net.ochibo.totem_of_unlosing.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ochibo.totem_of_unlosing.TotemOfUnlosingMod;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TotemOfUnlosingMod.MODID);

    public static final RegistryObject<Item> TOTEM_OF_UNLOSING = ITEMS.register("totem_of_unlosing",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> TOTEM_OF_UNLOSING_PROTECTED = ITEMS.register("totem_of_unlosing_protected",
            () -> new TotemOfUnlosingWithStoredItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
