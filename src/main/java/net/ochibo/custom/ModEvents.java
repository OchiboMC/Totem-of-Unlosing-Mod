package net.ochibo.custom;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.ochibo.TotemOfUnlosing;

@EventBusSubscriber(modid = TotemOfUnlosing.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        ItemStack savedTotem = oldPlayer.getData(ModAttachment.UNLOSING_TOTEM);
        if (savedTotem.isEmpty()) return;

        if (!newPlayer.getInventory().add(savedTotem.copy())) {
            newPlayer.drop(savedTotem.copy(), true);
        }
        // Clear it from the old player to prevent any persistent references
        oldPlayer.setData(ModAttachment.UNLOSING_TOTEM, ItemStack.EMPTY);
    }

    @EventBusSubscriber(modid = TotemOfUnlosing.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void buildContents(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.COMBAT) {
                event.accept(ModItem.TOTEM_OF_UNLOSING);
            }
        }
    }
}
