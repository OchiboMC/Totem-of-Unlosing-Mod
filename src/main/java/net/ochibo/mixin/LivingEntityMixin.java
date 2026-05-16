package net.ochibo.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.event.GameEvent;
import net.ochibo.TotemOfUnlosing;
import net.ochibo.custom.ModItem;
import net.ochibo.util.CustomPlayerNbtAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "tryUseTotem",at = @At("HEAD"),cancellable = true)
    private void tryUseUnlosingTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir){
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof PlayerEntity)) return;

        ItemStack itemStack = null;

        for (Hand hand : Hand.values()) {
            ItemStack itemStack2 = self.getStackInHand(hand);
            if (itemStack2.isOf(ModItem.TOTEM_OF_UNLOSING)) {
                itemStack = itemStack2.copy();
                itemStack2.decrement(1);
                break;
            }
        }

        if (itemStack != null) {
            // トーテムがある場合の処理
            CustomPlayerNbtAccessor nbt = ((CustomPlayerNbtAccessor) self);
            if (self instanceof ServerPlayerEntity) {
                self.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
                nbt.setWasProtectedItem(true);
            }
            self.getWorld().sendEntityStatus(self, TotemOfUnlosing.PROTECTED_BY_TOTEM_OF_UNLOSING);
        }
        cir.setReturnValue(itemStack != null);
    }
}
