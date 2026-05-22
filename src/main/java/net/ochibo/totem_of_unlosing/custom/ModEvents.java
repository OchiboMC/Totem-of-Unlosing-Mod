package net.ochibo.totem_of_unlosing.custom;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ochibo.totem_of_unlosing.TotemOfUnlosingMod;
import net.ochibo.totem_of_unlosing.util.CustomPlayerNbtAccessor;

@Mod.EventBusSubscriber(modid = TotemOfUnlosingMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        CustomPlayerNbtAccessor oldNbt = (CustomPlayerNbtAccessor) oldPlayer;
        CustomPlayerNbtAccessor newNbt = (CustomPlayerNbtAccessor) newPlayer;

        ItemStack storedTotem = oldNbt.getUnlosingTotem();

        if (!storedTotem.isEmpty()) {
            newNbt.setUnlosingTotem(storedTotem.copy());
            newPlayer.getInventory().setItem(0, storedTotem.copy());
        }
    }
}