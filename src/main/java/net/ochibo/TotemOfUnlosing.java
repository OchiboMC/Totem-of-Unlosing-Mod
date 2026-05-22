package net.ochibo;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.ochibo.custom.ModAttachment;
import net.ochibo.custom.ModComponent;
import net.ochibo.custom.ModEvents;
import net.ochibo.custom.ModItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(TotemOfUnlosing.MOD_ID)
public class TotemOfUnlosing {
    public static final String MOD_ID = "totem_of_unlosing";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Statuses
    public static final byte PROTECTED_BY_TOTEM_OF_UNLOSING = 75;
    public static final byte SHOULD_DROP_SLOT = -37;

    public TotemOfUnlosing(IEventBus modEventBus) {
        ModItem.ITEMS.register(modEventBus);
        ModComponent.COMPONENTS.register(modEventBus);
        ModAttachment.ATTACHMENTS.register(modEventBus);
    }
}