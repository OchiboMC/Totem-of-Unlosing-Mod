package net.ochibo.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.ochibo.TotemOfUnlosing;
import net.ochibo.custom.ModItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Shadow
    @Final
    private Random random;

    @Inject(method = "onEntityStatus",at=@At("TAIL"))
    public void onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci){
        ClientPlayNetworkHandler self = (ClientPlayNetworkHandler) (Object) this;
        final MinecraftClient client = MinecraftClient.getInstance();
        Entity entity = packet.getEntity(self.getWorld());

        if (entity != null && client.world != null) {
            switch (packet.getStatus()) {
                case TotemOfUnlosing.PROTECTED_BY_TOTEM_OF_UNLOSING:
                    for (int i = 0; i < 400; i++) {
                        client.world.addParticle(ParticleTypes.PORTAL,entity.getX(),entity.getY()+1,entity.getZ(),random.nextDouble() * 3 - 1.5,random.nextDouble() * 4 - 2,random.nextDouble() * 3 - 1.5);
                    }
                    client.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_PLAYER_HURT_FREEZE, entity.getSoundCategory(), 1.0F, 0.72F, false);
                    client.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, entity.getSoundCategory(), 1.0F, 0.72F, false);
                    client.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BLOCK_TRIAL_SPAWNER_AMBIENT, entity.getSoundCategory(), 1.0F, 1F, false);
                    client.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS, entity.getSoundCategory(), 1.0F, 1F, false);
                    client.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EVENT_MOB_EFFECT_BAD_OMEN, entity.getSoundCategory(), 0.9F, 0.6F, false);
                    if (entity == client.player) {
                        client.gameRenderer.showFloatingItem(getActiveTotemOfUnlosing(client.player));
                    }
                    break;
                default:
                    entity.handleStatus(packet.getStatus());
            }
        }
    }


    @Unique
    private static ItemStack getActiveTotemOfUnlosing(PlayerEntity player) {
        for (Hand hand : Hand.values()) {
            ItemStack itemStack = player.getStackInHand(hand);
            if (itemStack.isOf(ModItem.TOTEM_OF_UNLOSING)) {
                return itemStack;
            }
        }

        return new ItemStack(ModItem.TOTEM_OF_UNLOSING);
    }
}
