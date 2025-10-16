package de.rhm176.sphaira.fabric.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.rhm176.sphaira.ConfigUtils;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.minecraft.client.gui.screens.Screen;

import java.util.Map;
import java.util.stream.Collectors;

@Entrypoint("modmenu")
public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return null;
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return ConfigUtils.apply().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> (ConfigScreenFactory<Screen>) parent -> entry.getValue().apply(parent)
        ));
    }
}
