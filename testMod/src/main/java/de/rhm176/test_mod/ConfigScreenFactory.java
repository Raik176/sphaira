package de.rhm176.test_mod;

import de.rhm176.sphaira.ConfigUtils;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreenFactory implements ConfigUtils.Factory {
    @Override
    public Screen createConfig(Screen screen) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(screen)
                .setTitle(Component.literal("Test Mod"));

        ConfigCategory general = builder.getOrCreateCategory(Component.empty());

        return builder.build();
    }
}
