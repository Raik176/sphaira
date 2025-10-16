package de.rhm176.test_mod;

import de.rhm176.sphaira.ConfigUtils;
import de.rhm176.sphaira.ModLoaderUtils;
import net.fabricmc.api.ModInitializer;

public class TestMod implements ModInitializer {
    public static final String MOD_ID = "test_mod";

    @Override
    public void onInitialize() {
        ConfigUtils.registerScreen(
                MOD_ID,
                () -> ModLoaderUtils.isModLoaded("cloth-config"),
                ConfigScreenFactory.class
        );
    }
}
