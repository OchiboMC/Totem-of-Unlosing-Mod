package net.ochibo.custom;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.ochibo.TotemOfUnlosing;

public class ModComponent {
    public static final ComponentType<byte[]> STORED_ITEMS = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(TotemOfUnlosing.MOD_ID, "stored_items"),
            ComponentType.<byte[]>builder()
                    .codec(Codec.BYTE_BUFFER.xmap(java.nio.ByteBuffer::array, java.nio.ByteBuffer::wrap))
                    .build()
    );
    public static void registerModComponent() {}
}
