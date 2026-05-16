package net.ochibo.custom;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.ochibo.util.CustomPlayerNbtAccessor;

public class ModEvents {

    public static void registerModServerEvents() {
        ServerPlayerEvents.AFTER_RESPAWN.register(ServerEvents::onPlayerRespawned);
    }

    public static class ServerEvents{
        private static void onPlayerRespawned(
                ServerPlayerEntity oldPlayer,
                ServerPlayerEntity newPlayer,
                boolean alive
        ) {
            if (!((CustomPlayerNbtAccessor)oldPlayer).getUnlosingTotem().isEmpty()) return;

            newPlayer.getInventory().setStack(0,((CustomPlayerNbtAccessor)oldPlayer).getUnlosingTotem());
        }
    }
}
