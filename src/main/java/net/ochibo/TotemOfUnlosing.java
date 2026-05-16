package net.ochibo;

import net.fabricmc.api.ModInitializer;

import net.ochibo.custom.ModComponent;
import net.ochibo.custom.ModEvents;
import net.ochibo.custom.ModItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TotemOfUnlosing implements ModInitializer {
	public static final String MOD_ID = "totem-of-unlosing";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //  Statuses
    public static final byte PROTECTED_BY_TOTEM_OF_UNLOSING = 75;
    public static final byte UNKNOWN_SLOT = -37;


	@Override
	public void onInitialize() {
        ModEvents.registerModServerEvents();
        ModItem.registerModItem();
        ModComponent.registerModComponent();
	}
}