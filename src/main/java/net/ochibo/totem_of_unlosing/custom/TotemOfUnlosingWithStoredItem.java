package net.ochibo.totem_of_unlosing.custom;

import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.ochibo.totem_of_unlosing.TotemOfUnlosingMod;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TotemOfUnlosingWithStoredItem extends Item {
    public TotemOfUnlosingWithStoredItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        TriConsumer<SoundEvent, Float, Float> play = (ev, volume, pitch) -> {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ev, SoundSource.PLAYERS, volume, pitch);
        };

        play.accept(SoundEvents.WARDEN_HEARTBEAT, 1f, 0.8f);
        play.accept(SoundEvents.WITHER_BREAK_BLOCK, 0.5f, 2f);
        play.accept(SoundEvents.WITCH_DRINK, 1f, 0.9f);
        play.accept(SoundEvents.EVOKER_CAST_SPELL, 0.5f, 1.3f);

        if (world.isClientSide()) {
            for (int i = 0; i < 20; i++) {
                Vec3 vec3d = new Vec3((player.getRandom().nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
                vec3d = vec3d.xRot(-player.getXRot() * (float) (Math.PI / 180.0));
                vec3d = vec3d.yRot(-player.getYRot() * (float) (Math.PI / 180.0));
                double d = -player.getRandom().nextFloat() * 0.6 - 0.3;
                Vec3 vec3d2 = new Vec3((player.getRandom().nextFloat() - 0.5) * 0.3, d, 0.6);
                vec3d2 = vec3d2.xRot(-player.getXRot() * (float) (Math.PI / 180.0));
                vec3d2 = vec3d2.yRot(-player.getYRot() * (float) (Math.PI / 180.0));
                vec3d2 = vec3d2.add(player.getX(), player.getEyeY(), player.getZ());

                world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemStack),
                        vec3d2.x, vec3d2.y, vec3d2.z, vec3d.x, vec3d.y + 0.05, vec3d.z);
            }

            for (int i = 0; i < 100; i++) {
                world.addParticle(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1, player.getZ(),
                        player.getRandom().nextDouble() * 3 - 1.5, player.getRandom().nextDouble() * 4 - 2, player.getRandom().nextDouble() * 3 - 1.5);
                if (i % 2 == 0) {
                    world.addParticle(ParticleTypes.SOUL, player.getX(), player.getY() + 1, player.getZ(),
                            (player.getRandom().nextDouble() * 2 - 1) / 2, (player.getRandom().nextDouble() * 2 - 1) / 2, (player.getRandom().nextDouble() * 2 - 1) / 2);
                }
            }
        }

        ItemStack totemStack = itemStack.copy();
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        player.getInventory().dropAll();

        CompoundTag tag = totemStack.getTag();
        if (tag != null && tag.contains("StoredItems")) {
            byte[] compressedData = tag.getByteArray("StoredItems");

            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(compressedData));

            List<ZippedItemData> recoveredList = ZippedItemData.decodeList(buf);

            for (ZippedItemData data : recoveredList) {
                if (data.slot() == TotemOfUnlosingMod.SHOULD_DROP_SLOT) {
                    player.drop(data.toStack(), true, true);
                } else {
                    player.getInventory().setItem(data.slot(), data.toStack());
                }
            }
        }

        int dropXp = 0;
        if (tag != null && tag.contains("StoredExp")) {
            dropXp = tag.getInt("StoredExp");
        }

        if (world instanceof ServerLevel serverWorld) {
            ExperienceOrb.award(serverWorld, player.position(), dropXp);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("item.totem_of_unlosing.totem_of_unlosing.protected_desc").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }
}
