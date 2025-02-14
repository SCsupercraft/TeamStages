package net.scsupercraft.teamstages.listeners;

import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.scsupercraft.teamstages.TeamStageHelper;
import net.scsupercraft.teamstages.TeamStages;
import net.scsupercraft.teamstages.data.TeamStageSaveHandler;
import net.scsupercraft.teamstages.util.FtbUtil;

import java.util.Optional;

public class ServerEventListener {
	private static boolean serverLoaded = false;
	public static boolean isServerLoaded() {
		return serverLoaded;
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		TeamStages.server = event.getServer();
		TeamStageSaveHandler.load();
	}
	@SubscribeEvent
	public void onServerStarted(ServerStartedEvent event) {
		serverLoaded = true;
	}

	@SubscribeEvent
	public void onServerStopping(ServerStoppingEvent event) {
		TeamStageSaveHandler.save();
		TeamStageSaveHandler.markUnloaded();
		TeamStages.server = null;
		serverLoaded = false;
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer serverPlayer) {
			TeamManager manager = FtbUtil.getTeamManager();
			if (manager == null) return;

			Optional<Team> optionalTeam = manager.getTeamForPlayer(serverPlayer);
			optionalTeam.ifPresent(TeamStageHelper::syncTeam);
		}
	}
}
