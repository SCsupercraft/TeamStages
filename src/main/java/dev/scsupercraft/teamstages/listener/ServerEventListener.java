package dev.scsupercraft.teamstages.listener;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import dev.scsupercraft.teamstages.TeamStages;
import dev.scsupercraft.teamstages.data.TeamStageHelper;
import dev.scsupercraft.teamstages.data.TeamStageSaveHandler;

public class ServerEventListener {
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		TeamStages.server = event.getServer();
		TeamStageSaveHandler.load();
	}

	@SubscribeEvent
	public void onServerStopping(ServerStoppingEvent event) {
		TeamStageSaveHandler.save();
		TeamStageSaveHandler.markUnloaded();
		TeamStages.server = null;
	}

    @SubscribeEvent
    public void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TeamStageHelper.syncPlayer(player);
        }
    }
}
