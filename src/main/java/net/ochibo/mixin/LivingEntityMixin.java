package net.ochibo.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.ochibo.TotemOfUnlosing;
import net.ochibo.custom.ModAttachment;
import net.ochibo.custom.ModComponent;
import net.ochibo.custom.ModItem;
import net.ochibo.custom.StoredItemsComponent;
import net.ochibo.custom.ZippedItemData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
    private void tryUseUnlosingTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) return;

        ItemStack itemStack = null;

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemStack2 = self.getItemInHand(hand);
            if (itemStack2.is(ModItem.TOTEM_OF_UNLOSING.get())) {
                itemStack = itemStack2.copy();
                itemStack2.shrink(1);
                break;
            }
        }

        if (itemStack != null) {
            if (player instanceof ServerPlayer) {
                self.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                onUseTotem(player);
            }
            self.level().broadcastEntityEvent(self, TotemOfUnlosing.PROTECTED_BY_TOTEM_OF_UNLOSING);
        }
    }

    private void onUseTotem(Player player) {
        List<ZippedItemData> stored_item = new ArrayList<>();

        NonNullList<ItemStack> main = player.getInventory().items;
        NonNullList<ItemStack> armor = player.getInventory().armor;
        List<ItemStack> waitingItem = new ArrayList<>();

        // main
        for (int i = 0; i < main.size(); i++) {
            var item = main.get(i);
            if (!item.isEmpty()) {
                if (item.is(ModItem.TOTEM_OF_UNLOSING_PROTECTED.get())) {
                    waitingItem.addAll(unpackTotem(item, player));
                } else {
                    stored_item.add(ZippedItemData.of(item.copy(), i));
                }
            }
        }

        // armor
        for (int i = 0; i < armor.size(); i++) {
            var item = armor.get(i);
            if (!item.isEmpty()) {
                stored_item.add(ZippedItemData.of(item.copy(), i + main.size()));
            }
        }

        // offhand
        ItemStack offhandItem = player.getInventory().offhand.getFirst();
        if (!offhandItem.isEmpty()) {
            if (offhandItem.is(ModItem.TOTEM_OF_UNLOSING_PROTECTED.get())) {
                waitingItem.addAll(unpackTotem(offhandItem, player));
            } else {
                stored_item.add(ZippedItemData.of(offhandItem.copy(), Inventory.SLOT_OFFHAND));
            }
        }

        // store waitingItem (previously stored items)
        for (ItemStack waitingStack : waitingItem) {
            if (!waitingStack.isEmpty()) {
                if (stored_item.size() < 60) {
                    stored_item.add(ZippedItemData.of(waitingStack.copy(), TotemOfUnlosing.SHOULD_DROP_SLOT));
                } else {
                    player.drop(waitingStack, true, false);
                }
            }
        }

        // Clear inventory to prevent dropping items on death
        player.getInventory().clearContent();

        ItemStack newTotem = new ItemStack(ModItem.TOTEM_OF_UNLOSING_PROTECTED.get());
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        byte[] compressedData = ZippedItemData.encodeList(stored_item, buf);
        buf.release();
        newTotem.set(ModComponent.STORED_ITEMS.get(), new StoredItemsComponent(compressedData));
        player.setData(ModAttachment.UNLOSING_TOTEM, newTotem);
    }

    private static String generateKey(ItemStack stack) {
        return Item.getId(stack.getItem()) + "_" + stack.getComponentsPatch().hashCode();
    }

    private static List<ItemStack> unpackTotem(ItemStack totem, Player player) {
        StoredItemsComponent component = totem.get(ModComponent.STORED_ITEMS.get());
        List<ItemStack> stackList = new ArrayList<>();
        if (component == null) return stackList;
        byte[] data = component.data;
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(
                Unpooled.wrappedBuffer(data),
                player.registryAccess()
        );
        ZippedItemData.decodeList(buf).forEach(zippedItemData -> stackList.add(zippedItemData.toStack()));
        buf.release();
        return stackList;
    }
}
