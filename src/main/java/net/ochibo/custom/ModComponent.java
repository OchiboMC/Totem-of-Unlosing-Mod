package net.ochibo.custom;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ochibo.TotemOfUnlosing;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;

public class ModComponent {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, TotemOfUnlosing.MOD_ID);

    public static final Supplier<DataComponentType<StoredItemsComponent>> STORED_ITEMS = COMPONENTS.register("stored_items", () -> 
            DataComponentType.<StoredItemsComponent>builder()
                    .persistent(Codec.BYTE_BUFFER.xmap(
                            buffer -> {
                                byte[] bytes = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                return new StoredItemsComponent(bytes);
                            }, 
                            component -> java.nio.ByteBuffer.wrap(component.data)
                    ))
                    .build()
    );
}
