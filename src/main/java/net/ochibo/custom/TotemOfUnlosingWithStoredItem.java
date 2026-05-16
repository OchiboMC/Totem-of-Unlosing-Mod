package net.ochibo.custom;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.ochibo.TotemOfUnlosing;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;

public class TotemOfUnlosingWithStoredItem extends Item {
    public TotemOfUnlosingWithStoredItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        TriConsumer<SoundEvent, Float, Float> play = (ev, volume, pitch) -> {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    RegistryEntry.of(ev),
                    SoundCategory.PLAYERS, volume, pitch);
        };

        play.accept(SoundEvents.ENTITY_WARDEN_HEARTBEAT,1f,0.8f);
        play.accept(SoundEvents.ENTITY_WITHER_BREAK_BLOCK,0.5f,2f);
        play.accept(SoundEvents.ITEM_OMINOUS_BOTTLE_DISPOSE,1f,0.9f);
        play.accept(SoundEvents.EVENT_MOB_EFFECT_RAID_OMEN,0.5f,1.3f);

        for (int i = 0; i < 20; i++) {
            Vec3d vec3d = new Vec3d((player.getRandom().nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
            vec3d = vec3d.rotateX(-player.getPitch() * (float) (Math.PI / 180.0));
            vec3d = vec3d.rotateY(-player.getYaw() * (float) (Math.PI / 180.0));
            double d = -player.getRandom().nextFloat() * 0.6 - 0.3;
            Vec3d vec3d2 = new Vec3d((player.getRandom().nextFloat() - 0.5) * 0.3, d, 0.6);
            vec3d2 = vec3d2.rotateX(-player.getPitch() * (float) (Math.PI / 180.0));
            vec3d2 = vec3d2.rotateY(-player.getYaw() * (float) (Math.PI / 180.0));
            vec3d2 = vec3d2.add(player.getX(), player.getEyeY(), player.getZ());
            player.getWorld().addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, (hand == Hand.MAIN_HAND) ? player.getMainHandStack(): player.getOffHandStack()), vec3d2.x, vec3d2.y, vec3d2.z, vec3d.x, vec3d.y + 0.05, vec3d.z);
        }

        for (int i = 0; i < 100; i++) {
            player.getWorld().addParticle(ParticleTypes.REVERSE_PORTAL,player.getX(),player.getY()+1,player.getZ(),player.getRandom().nextDouble() * 3 - 1.5,player.getRandom().nextDouble() * 4 - 2,player.getRandom().nextDouble() * 3 - 1.5);
            if (i % 2 == 0)player.getWorld().addParticle(ParticleTypes.SOUL,player.getX(),player.getY()+1,player.getZ(),(player.getRandom().nextDouble() * 2 - 1) / 2,(player.getRandom().nextDouble() * 2 - 1) / 2,(player.getRandom().nextDouble() * 2 - 1) / 2);
        }

        ItemStack totemStack;
        if (hand == Hand.MAIN_HAND) {
            totemStack = player.getMainHandStack().copy();
            player.getMainHandStack().decrement(1);
        } else {
            totemStack = player.getOffHandStack().copy();
            player.getOffHandStack().decrement(1);
        }

        player.getInventory().dropAll();

        byte[] compressedData = totemStack.get(ModComponent.STORED_ITEMS);
        if (compressedData != null) {
            RegistryByteBuf buf = new RegistryByteBuf(
                    Unpooled.wrappedBuffer(compressedData),
                    player.getRegistryManager()
            );
            List<ZippedItemData> recoveredList = ZippedItemData.decodeList(buf);

            for (ZippedItemData data : recoveredList) {
                if (data.slot() == 0)player.getInventory().setStack(PlayerInventory.OFF_HAND_SLOT, data.toStack());
                else if (data.slot() == PlayerInventory.OFF_HAND_SLOT)player.getInventory().setStack(0, data.toStack());
                else if (data.slot() == TotemOfUnlosing.SHOULD_DROP_SLOT) player.dropItem(data.toStack(),true,true);
                else player.getInventory().setStack(data.slot(), data.toStack());
            }
        }
        return super.use(world, player, hand);
    }
}
