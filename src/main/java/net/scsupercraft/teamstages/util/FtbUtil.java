package net.scsupercraft.teamstages.util;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import dev.ftb.mods.ftbteams.api.client.ClientTeamManager;
import dev.ftb.mods.ftbteams.data.PlayerTeam;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class FtbUtil {
	public static FTBTeamsAPI.API getAPI() {
		return FTBTeamsAPI.api();
	}
	public static @Nullable TeamManager getTeamManager() {
		if (!getAPI().isManagerLoaded()) return null;
		return getAPI().getManager();
	}
	public static @Nullable ClientTeamManager getClientTeamManager() {
		if (!getAPI().isClientManagerLoaded()) return null;
		return getAPI().getClientManager();
	}

	public static @Nullable Team getTeam(UUID id, boolean isPlayerId) {
		TeamManager manager = getTeamManager();
		if (manager == null) return null;

		if (!isPlayerId) {
			Optional<Team> optionalTeam = manager.getTeamByID(id);
			return optionalTeam.orElse(null);
		} else {
			Collection<Team> teams = manager.getTeams();
			for (Team team : teams) {
				if (team.isPartyTeam() && team.getMembers().contains(id)) return team;
			}
			return getPlayerTeam(id);
		}
	}
	public static @Nullable Team getTeam(ServerPlayer player) {
		return getTeam(player.getUUID(), true);
	}

	public static @Nullable PlayerTeam getPlayerTeam(UUID playerId) {
		TeamManager manager = getTeamManager();
		if (manager == null) return null;

		Optional<Team> optionalTeam = manager.getPlayerTeamForPlayerID(playerId);
		return (PlayerTeam) optionalTeam.orElse(null);
	}
	public static @Nullable PlayerTeam getPlayerTeam(ServerPlayer player) {
		return getPlayerTeam(player.getUUID());
	}
}
