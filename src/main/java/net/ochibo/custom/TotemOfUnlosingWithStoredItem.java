package net.ochibo.custom;

import io.netty.buffer.Unpooled;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.ochibo.TotemOfUnlosing;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;

public class TotemOfUnlosingWithStoredItem extends Item {
    public TotemOfUnlosingWithStoredItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        TriConsumer<SoundEvent, Float, Float> play = (ev, volume, pitch) -> {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ev,
                    SoundSource.PLAYERS, volume, pitch);
        };

        play.accept(SoundEvents.WARDEN_HEARTBEAT, 1f, 0.8f);
        play.accept(SoundEvents.WITHER_BREAK_BLOCK, 0.5f, 2f);
        play.accept(SoundEvents.OMINOUS_BOTTLE_DISPOSE, 1f, 0.9f);
        play.accept(SoundEvents.APPLY_EFFECT_RAID_OMEN, 0.5f, 1.3f);

        for (int i = 0; i < 20; i++) {
            Vec3 vec3 = new Vec3((player.getRandom().nextFloat() - 0.5) * 0.1, level.getRandom().nextDouble() * 0.1 + 0.1, 0.0);
            vec3 = vec3.xRot(-player.getXRot() * (float) (Math.PI / 180.0));
            vec3 = vec3.yRot(-player.getYRot() * (float) (Math.PI / 180.0));
            double d = -player.getRandom().nextFloat() * 0.6 - 0.3;
            Vec3 vec3d2 = new Vec3((player.getRandom().nextFloat() - 0.5) * 0.3, d, 0.6);
            vec3d2 = vec3d2.xRot(-player.getXRot() * (float) (Math.PI / 180.0));
            vec3d2 = vec3d2.yRot(-player.getYRot() * (float) (Math.PI / 180.0));
            vec3d2 = vec3d2.add(player.getX(), player.getEyeY(), player.getZ());
            player.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, (hand == InteractionHand.MAIN_HAND) ? player.getMainHandItem() : player.getOffhandItem()), vec3d2.x, vec3d2.y, vec3d2.z, vec3.x, vec3.y + 0.05, vec3.z);
        }

        for (int i = 0; i < 100; i++) {
            player.level().addParticle(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1, player.getZ(), player.getRandom().nextDouble() * 3 - 1.5, player.getRandom().nextDouble() * 4 - 2, player.getRandom().nextDouble() * 3 - 1.5);
            if (i % 2 == 0) player.level().addParticle(ParticleTypes.SOUL, player.getX(), player.getY() + 1, player.getZ(), (player.getRandom().nextDouble() * 2 - 1) / 2, (player.getRandom().nextDouble() * 2 - 1) / 2, (player.getRandom().nextDouble() * 2 - 1) / 2);
        }

        ItemStack totemStack;
        if (hand == InteractionHand.MAIN_HAND) {
            totemStack = player.getMainHandItem().copy();
            player.getMainHandItem().shrink(1);
        } else {
            totemStack = player.getOffhandItem().copy();
            player.getOffhandItem().shrink(1);
        }

        if (!level.isClientSide()) {
            player.getInventory().dropAll();

            StoredItemsComponent component = totemStack.get(ModComponent.STORED_ITEMS.get());
            if (component != null) {
                byte[] compressedData = component.data;
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(
                        Unpooled.wrappedBuffer(compressedData),
                        player.registryAccess()
                );
                List<ZippedItemData> recoveredList = ZippedItemData.decodeList(buf);
                buf.release();

                for (ZippedItemData data : recoveredList) {
                    if (data.slot() == TotemOfUnlosing.SHOULD_DROP_SLOT) player.drop(data.toStack(), true, true);
                    else player.getInventory().setItem(data.slot(), data.toStack());
                }
            }
        }
        return super.use(level, player, hand);
    }
}
