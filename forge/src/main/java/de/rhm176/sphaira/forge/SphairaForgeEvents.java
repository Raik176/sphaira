package de.rhm176.sphaira.forge;

import de.rhm176.sphaira.api.resource.ResourceUtils;
import de.rhm176.sphaira.SphairaCommon;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.fml.common.Mod;

//? if >=1.21.6 {
/*import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
*///?} else {
import net.minecraftforge.eventbus.api.SubscribeEvent;
//?}

@Mod.EventBusSubscriber(modid = SphairaCommon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SphairaForgeEvents {
    @SubscribeEvent
    //? if >=1.18 {
    public static void serverStarted(net.minecraftforge.event.server.ServerStartedEvent event) {
    //?} else {
    /*public static void serverStarted(net.minecraftforge.fmlserverevents.FMLServerStartedEvent event) {
    *///?}
        ResourceUtils.reload(event.getServer());
    }

    //TODO: probably not ideal as this is ran every time a player joins OR the reload command is executed
    @SubscribeEvent
    public static void serverReloaded(OnDatapackSyncEvent event) {
        ResourceUtils.reload(event.getPlayerList().getServer());
    }
}
