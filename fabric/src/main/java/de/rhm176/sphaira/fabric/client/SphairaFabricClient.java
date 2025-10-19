package de.rhm176.sphaira.fabric.client;

import de.rhm176.sphaira.client.SphairaClient;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ClientModInitializer;

@Entrypoint
public class SphairaFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SphairaClient.init(new SphairaClient.BaseImpl() {

        });
    }
}
