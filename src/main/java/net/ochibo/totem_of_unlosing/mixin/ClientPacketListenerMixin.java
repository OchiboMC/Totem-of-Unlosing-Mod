package net.ochibo.totem_of_unlosing.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.ochibo.totem_of_unlosing.TotemOfUnlosingMod;
import net.ochibo.totem_of_unlosing.custom.ModItems;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Shadow
    @Final
    private RandomSource random;

    @Inject(method = "handleEntityEvent", at = @At("TAIL"))
    public void onEntityStatus(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        ClientPacketListener self = (ClientPacketListener) (Object) this;
        final Minecraft client = Minecraft.getInstance();
        Entity entity = packet.getEntity(client.level);

        if (entity != null && client.level != null) {
            switch (packet.getEventId()) {
                case TotemOfUnlosingMod.PROTECTED_BY_TOTEM_OF_UNLOSING:
                    for (int i = 0; i < 400; i++) {
                        client.level.addParticle(ParticleTypes.PORTAL, entity.getX(), entity.getY() + 1, entity.getZ(), random.nextDouble() * 3 - 1.5, random.nextDouble() * 4 - 2, random.nextDouble() * 3 - 1.5);
                    }
                    client.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_HURT_FREEZE, entity.getSoundSource(), 1.0F, 0.72F, false);
                    client.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.WARDEN_SONIC_CHARGE, entity.getSoundSource(), 1.0F, 0.72F, false);
                    client.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EVOKER_PREPARE_ATTACK, entity.getSoundSource(), 1.0F, 1F, false);
                    client.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.RAID_HORN.get(), entity.getSoundSource(), 1.0F, 1F, false);
                    client.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.WITHER_SPAWN, entity.getSoundSource(), 0.9F, 1.6F, false);

                    if (entity == client.player) {
                        client.gameRenderer.displayItemActivation(getActiveTotemOfUnlosing(client.player));
                    }
                    break;
                default:
                    entity.handleEntityEvent(packet.getEventId());
            }
        }
    }

    @Unique
    private static ItemStack getActiveTotemOfUnlosing(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (itemStack.is(ModItems.TOTEM_OF_UNLOSING.get())) {
                return itemStack;
            }
        }

        return new ItemStack(ModItems.TOTEM_OF_UNLOSING.get());
    }
}