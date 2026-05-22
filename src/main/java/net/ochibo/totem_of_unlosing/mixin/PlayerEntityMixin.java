package net.ochibo.totem_of_unlosing.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.ochibo.totem_of_unlosing.util.CustomPlayerNbtAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerEntityMixin implements CustomPlayerNbtAccessor {

    @Unique
    private static final EntityDataAccessor<ItemStack> UNLOSING_TOTEM = SynchedEntityData.defineId(Player.class, EntityDataSerializers.ITEM_STACK);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void initTracker(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        player.getEntityData().define(UNLOSING_TOTEM, ItemStack.EMPTY);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void write(CompoundTag nbt, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        ItemStack stack = player.getEntityData().get(UNLOSING_TOTEM);
        if (!stack.isEmpty()) {
            nbt.put("UnlosingTotem", stack.save(new CompoundTag()));
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void read(CompoundTag nbt, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (nbt.contains("UnlosingTotem")) {
            player.getEntityData().set(UNLOSING_TOTEM, ItemStack.of(nbt.getCompound("UnlosingTotem")));
        } else {
            player.getEntityData().set(UNLOSING_TOTEM, ItemStack.EMPTY);
        }
    }

    @Override
    public ItemStack getUnlosingTotem() {
        return ((Player) (Object) this).getEntityData().get(UNLOSING_TOTEM);
    }

    @Override
    public void setUnlosingTotem(ItemStack value) {
        ((Player) (Object) this).getEntityData().set(UNLOSING_TOTEM, value);
    }
}