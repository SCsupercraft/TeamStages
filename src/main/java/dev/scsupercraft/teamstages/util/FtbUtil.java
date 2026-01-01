package dev.scsupercraft.teamstages.util;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import dev.ftb.mods.ftbteams.api.client.ClientTeamManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.util.thread.EffectiveSide;

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

    public static @Nullable Team getClientTeam(UUID id, boolean isPlayerId) {
        ClientTeamManager manager = getClientTeamManager();
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

	public static @Nullable Team getTeam(Player player) {
        if (player instanceof ServerPlayer)
            return getTeam(player.getUUID(), true);
        return EffectiveSide.get().isClient() ? getClientTeam(player.getUUID(), true) : null;
	}

	public static @Nullable Team getPlayerTeam(UUID playerId) {
		TeamManager manager = getTeamManager();
		if (manager == null) return null;

		Optional<Team> optionalTeam = manager.getPlayerTeamForPlayerID(playerId);
		return optionalTeam.orElse(null);
	}

    public static @Nullable Team getClientPlayerTeam(UUID playerId) {
        ClientTeamManager manager = getClientTeamManager();
        if (manager == null) return null;

        Optional<Team> optionalTeam = manager.getTeamByID(playerId);
        return optionalTeam.orElse(null);
    }

	public static @Nullable Team getPlayerTeam(Player player) {
        if (player instanceof ServerPlayer)
            return getPlayerTeam(player.getUUID());
        return EffectiveSide.get().isClient() ? getClientPlayerTeam(player.getUUID()) : null;
	}
}
