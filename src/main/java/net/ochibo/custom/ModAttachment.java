package net.ochibo.custom;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.ochibo.TotemOfUnlosing;

import java.util.function.Supplier;

public class ModAttachment {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TotemOfUnlosing.MOD_ID);

    public static final Supplier<AttachmentType<ItemStack>> UNLOSING_TOTEM = ATTACHMENTS.register(
            "unlosing_totem",
            () -> AttachmentType.builder(() -> ItemStack.EMPTY).serialize(ItemStack.OPTIONAL_CODEC).build()
    );
}
