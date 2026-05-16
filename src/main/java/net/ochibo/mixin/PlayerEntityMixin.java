package net.ochibo.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.ochibo.util.CustomPlayerNbtAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements CustomPlayerNbtAccessor {

    @Unique
    private static final TrackedData<ItemStack> UNLOSING_TOTEM = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    /* DataTracker */
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(UNLOSING_TOTEM,ItemStack.EMPTY);
    }

    /* save */
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void write(NbtCompound nbt, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        nbt.put("UnlosingTotem",player.getDataTracker().get(UNLOSING_TOTEM).encode(player.getRegistryManager()));
    }

    /* load */
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void read(NbtCompound nbt, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        player.getDataTracker().set(UNLOSING_TOTEM,ItemStack.fromNbtOrEmpty(player.getRegistryManager(),nbt.getCompound("UnlosingTotem")));
    }

    @Override
    public ItemStack getUnlosingTotem() {
        return ((PlayerEntity)(Object)this).getDataTracker().get(UNLOSING_TOTEM);
    }

    @Override
    public void setUnlosingTotem(ItemStack value) {
        ((PlayerEntity)(Object)this).getDataTracker().set(UNLOSING_TOTEM, value);
    }


}
