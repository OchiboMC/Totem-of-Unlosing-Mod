package net.ochibo.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
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
    private static final TrackedData<Boolean> WAS_PROTECTED_ITEM = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    /* DataTracker */
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(WAS_PROTECTED_ITEM,false);
    }

    /* save */
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void write(NbtCompound nbt, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        nbt.putBoolean("WasProtectedItem",player.getDataTracker().get(WAS_PROTECTED_ITEM));
    }

    /* load */
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void read(NbtCompound nbt, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        player.getDataTracker().set(WAS_PROTECTED_ITEM,nbt.getBoolean("WasProtectedItem"));
    }

    @Override
    public boolean getWasProtectedItem() {
        return ((PlayerEntity)(Object)this).getDataTracker().get(WAS_PROTECTED_ITEM);
    }

    @Override
    public void setWasProtectedItem(boolean value) {
        ((PlayerEntity)(Object)this).getDataTracker().set(WAS_PROTECTED_ITEM, value);
    }


}
