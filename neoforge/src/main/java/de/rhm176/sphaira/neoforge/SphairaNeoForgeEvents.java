package de.rhm176.sphaira.neoforge;

import de.rhm176.sphaira.api.resource.ResourceUtils;
import de.rhm176.sphaira.SphairaCommon;
import net.neoforged.fml.common.Mod;
//? if >=1.20.6
/*import net.neoforged.fml.common.EventBusSubscriber;*/
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@
//? if <1.20.6
        Mod.
        EventBusSubscriber(modid = SphairaCommon.MOD_ID
        //? if <1.21.3 {
        , bus =
        //? if <1.20.6
        Mod.
        EventBusSubscriber.Bus.
                //? if 1.20.4 {
                FORGE
                //?} else {
                /*GAME
                *///?}
        //?}
        )
public class SphairaNeoForgeEvents {
    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent event) {
        ResourceUtils.reload(event.getServer());
    }

    @SubscribeEvent
    public static void serverReloaded(OnDatapackSyncEvent event) {
        ResourceUtils.reload(event.getPlayerList().getServer());
    }
}
