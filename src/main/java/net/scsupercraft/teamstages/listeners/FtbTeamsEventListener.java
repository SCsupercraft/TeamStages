package net.scsupercraft.teamstages.listeners;

import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamCreatedEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import net.minecraft.server.level.ServerPlayer;
import net.scsupercraft.teamstages.TeamStageHelper;
import net.scsupercraft.teamstages.data.PlayerStageData;
import net.scsupercraft.teamstages.data.TeamStageSaveHandler;

public class FtbTeamsEventListener {
	public static void listen() {
		TeamEvent.PLAYER_CHANGED.register(FtbTeamsEventListener::onPlayerChangeParty);

		TeamEvent.CREATED.register(FtbTeamsEventListener::onPartyCreated);
		TeamEvent.DELETED.register(FtbTeamsEventListener::onPartyDeleted);
	}

	private static void onPlayerChangeParty(PlayerChangedTeamEvent event) {
		ServerPlayer player = event.getPlayer();
		if (player == null) return;

		PlayerStageData data = TeamStageHelper.getPlayerData(player);
		if (data != null) data.update();
	}

	private static void onPartyCreated(TeamCreatedEvent event) {
		TeamStageSaveHandler.createTeamData(event.getTeam().getTeamId());
	}
	private static void onPartyDeleted(TeamEvent event) {
		TeamStageSaveHandler.deleteTeamData(event.getTeam().getTeamId());
	}
}
