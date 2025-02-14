package net.scsupercraft.teamstages;

import dev.ftb.mods.ftbteams.api.Team;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.darkhax.gamestages.data.IStageData;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.scsupercraft.teamstages.data.PlayerStageData;
import net.scsupercraft.teamstages.data.TeamStageData;
import net.scsupercraft.teamstages.data.TeamStageSaveHandler;
import net.scsupercraft.teamstages.packet.MessageTeamStages;
import net.scsupercraft.teamstages.packet.TeamStagesPacketHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

public class TeamStageHelper extends GameStageHelper {
	public static boolean playerHasStage(Player player, String stage) {
		return playerHasStage(player, getPlayerData(player), stage);
	}

	public static boolean playerHasStage(Player player, @Nullable IStageData data, String stage) {
		if (data instanceof PlayerStageData playerStageData) {
			GameStageEvent.Check event = new GameStageEvent.Check(player, stage, playerStageData.playerHasStage(stage));
			MinecraftForge.EVENT_BUS.post(event);
			return event.hasStage();
		} else {
			return false;
		}
	}

	public static boolean playerHasAnyOf(Player player, String... stages) {
		return playerHasAnyOf(player, getPlayerData(player), stages);
	}

	public static boolean playerHasAnyOf(Player player, Collection<String> stages) {
		return playerHasAnyOf(player, getPlayerData(player), stages);
	}

	public static boolean playerHasAnyOf(Player player, @Nullable IStageData data, Collection<String> stages) {
		return stages.stream().anyMatch((stage) -> playerHasStage(player, data, stage));
	}

	public static boolean playerHasAnyOf(Player player, @Nullable IStageData data, String... stages) {
		return Arrays.stream(stages).anyMatch((stage) -> playerHasStage(player, data, stage));
	}

	public static boolean playerHasAllOf(Player player, String... stages) {
		return playerHasAllOf(player, getPlayerData(player), stages);
	}

	public static boolean playerHasAllOf(Player player, Collection<String> stages) {
		return playerHasAllOf(player, getPlayerData(player), stages);
	}

	public static boolean playerHasAllOf(Player player, @Nullable IStageData data, Collection<String> stages) {
		return stages.stream().allMatch((stage) -> playerHasStage(player, data, stage));
	}

	public static boolean playerHasAllOf(Player player, @Nullable IStageData data, String... stages) {
		return Arrays.stream(stages).allMatch((stage) -> playerHasStage(player, data, stage));
	}

	public static void addPlayerStage(ServerPlayer player, String... stages) {
		PlayerStageData data = getPlayerData(player);
		if (data != null) {
			for (String stage: stages) {
				data.addPlayerStage(stage, false);
			}
			syncPlayer(player);
		}
	}

	public static void removePlayerStage(ServerPlayer player, String... stages) {
		PlayerStageData data = getPlayerData(player);
		if (data != null) {
			for (String stage: stages) {
				data.removePlayerStage(stage, false);
			}
			syncPlayer(player);
		}
	}

	public static int clearPlayerStages(ServerPlayer player) {
		PlayerStageData stageInfo = getPlayerData(player);
		if (stageInfo != null) {
			int stageCount = stageInfo.getPlayerStages().size();
			stageInfo.clearPlayer();
			return stageCount;
		} else {
			return 0;
		}
	}

	@Nullable
	public static PlayerStageData getPlayerData(Player player) {
		if (player != null) {
			if (player instanceof ServerPlayer) {
				return (PlayerStageData) GameStageSaveHandler.getPlayerData(player.getUUID());
			}

			if (EffectiveSide.get().isClient()) {
				return (PlayerStageData) GameStageSaveHandler.getClientData();
			}
		}

		return null;
	}

	public static boolean teamHasStage(Team team, String stage) {
		return teamHasStage(getTeamData(team), stage);
	}

	public static boolean teamHasStage(@Nullable IStageData data, String stage) {
		if (data instanceof PlayerStageData stageData) {
			if (stageData.getTeamStageData() == null) return false;
			return stageData.getTeamStageData().hasStage(stage);
		} else if (data instanceof TeamStageData stageData) {
			return stageData.hasStage(stage);
		} else {
			return false;
		}
	}

	public static boolean teamHasAnyOf(Team team, Collection<String> stages) {
		return stages.stream().anyMatch((stage) -> teamHasStage(team, stage));
	}

	public static boolean teamHasAnyOf(Team team, String... stages) {
		return Arrays.stream(stages).anyMatch((stage) -> teamHasStage(team, stage));
	}

	public static boolean teamHasAllOf(Team team, Collection<String> stages) {
		return stages.stream().allMatch((stage) -> teamHasStage(team, stage));
	}

	public static boolean teamHasAllOf(Team team, String... stages) {
		return Arrays.stream(stages).allMatch((stage) -> teamHasStage(team, stage));
	}

	public static void addTeamStage(Team team, String... stages) {
		TeamStageData data = getTeamData(team);
		if (data != null) {
			for (String stage: stages) {
				data.addStage(stage, false);
			}
			syncTeam(team);
		}
	}

	public static void removeTeamStage(Team team, String... stages) {
		TeamStageData data = getTeamData(team);
		if (data != null) {
			for (String stage: stages) {
				data.removeStage(stage, false);
			}
			syncTeam(team);
		}
	}

	public static int clearTeamStages(Team team) {
		TeamStageData stageInfo = getTeamData(team);
		if (stageInfo != null) {
			int stageCount = stageInfo.getStages().size();
			stageInfo.clear();
			return stageCount;
		} else {
			return 0;
		}
	}

	@Nullable
	public static TeamStageData getTeamData(Team team) {
		if (team != null) {
			if (team.isClientTeam()) {
				return TeamStageSaveHandler.getClientData();
			}
			return TeamStageSaveHandler.getTeamData(team.getTeamId());
		}
		return null;
	}

	public static void syncTeam(Team team) {
		TeamStageData info = getTeamData(team);
		if (info != null) {
			TeamStages.LOGGER.debug("Syncing {} stages for {}.", info.getStages().size(), team.getShortName());

			for (ServerPlayer player : team.getOnlineMembers()) {
				TeamStagesPacketHandler.sendToClient(player, new MessageTeamStages(team.getTeamId(), info.getStages()));
				syncPlayer(player);
			}
		}
	}
}
