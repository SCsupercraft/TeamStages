package net.scsupercraft.teamstages.data;

import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.scsupercraft.teamstages.TeamStages;
import net.scsupercraft.teamstages.listeners.ServerEventListener;
import net.scsupercraft.teamstages.util.FtbUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamStageSaveHandler {
	private static boolean loaded = false;
	private static final Map<UUID, TeamStageData> GLOBAL_STAGE_DATA = new HashMap<>();
	@OnlyIn(Dist.CLIENT)
	private static TeamStageData clientData;

	public static void save() {
		File saveDir = getSaveDirectory().toFile();
		if (!saveDir.exists()) {
			saveDir.mkdirs();
		}

		GLOBAL_STAGE_DATA.forEach(TeamStageSaveHandler::save);
	}
	private static void save(UUID teamID, TeamStageData data) {
		try {
			CompoundTag tag = data.writeToNBT();
			NbtIo.writeCompressed(tag, getTeamFile(teamID.toString()));
		} catch (IOException e) {
			TeamStages.LOGGER.error("Failed to save data for team {}", data.getTeam() != null ? data.getTeam().getShortName() : "unknown", e);
		}
	}
	public static void load() {
		if (loaded) return;

		File saveDir = getSaveDirectory().toFile();
		GLOBAL_STAGE_DATA.clear();
		if (!saveDir.exists()) {
			saveDir.mkdirs();
			registerAllTeams();
			loaded = true;
			return;
		}

		try {
			File[] files = saveDir.listFiles(File::isFile);
			if (files == null) throw new IOException();

			for (File file: files) {
				try {
					TeamStageData data = new TeamStageData(null);
					CompoundTag tag = NbtIo.readCompressed(file);

					data.readFromNBT(tag);
					GLOBAL_STAGE_DATA.put(data.getTeamID(), data);

					TeamStages.LOGGER.debug("Loaded {} stages for {}.", data.getStages().size(), data.getTeam() != null ? data.getTeam().getShortName() : "unknown");
				} catch (IOException e) {
					TeamStages.LOGGER.error("Could not read team data for file {}.", file.getName(), e);
				}
			}
		} catch (Exception e) {
			TeamStages.LOGGER.error("An error occurred while trying to load team stages!", e);
		} finally {
			registerAllTeams();
			loaded = true;
		}
	}
	public static boolean isLoaded() {
		return loaded && ServerEventListener.isServerLoaded();
	}
	public static void markUnloaded() {
		loaded = false;
	}

	public static void registerAllTeams() {
		TeamManager manager = FtbUtil.getTeamManager();
		if (manager == null) return;

		// Party Teams
		for (Team team : manager.getTeams()) {
			UUID teamId = team.getTeamId();
			if (!GLOBAL_STAGE_DATA.containsKey(teamId)) createTeamData(teamId);
		}
		// Player Teams
		manager.getKnownPlayerTeams().forEach(((uuid, team) -> {
			if (!GLOBAL_STAGE_DATA.containsKey(uuid)) createTeamData(uuid);
		}));
	}
	public static void createTeamData(UUID uuid) {
		GLOBAL_STAGE_DATA.put(uuid, new TeamStageData(uuid));
	}
	public static void deleteTeamData(UUID uuid) {
		GLOBAL_STAGE_DATA.remove(uuid);

		File teamDataFile = getTeamFile(uuid.toString());
		if (teamDataFile.exists()) {
			teamDataFile.delete();
		}
	}
	@Nullable
	public static TeamStageData getTeamData(UUID uuid) {
		return GLOBAL_STAGE_DATA.get(uuid);
	}
	@Nullable
	public static PlayerStageData getPlayerData(UUID uuid) {
		return (PlayerStageData) GameStageSaveHandler.getPlayerData(uuid);
	}

	private static File getPlayerFile(File playerDir, String uuid) {
		File saveDir = new File(playerDir, "gamestages");
		if (!saveDir.exists()) {
			saveDir.mkdirs();
		}

		return new File(saveDir, uuid + ".dat");
	}

	private static File getTeamFile(String uuid) {
		File saveDir = getSaveDirectory().toFile();
		if (!saveDir.exists()) {
			saveDir.mkdirs();
		}

		return new File(saveDir, uuid + ".dat");
	}
	private static Path getSaveDirectory() {
		return TeamStages.server.getWorldPath(LevelResource.ROOT).resolve("ftbteams/teamstages");
	}

	@OnlyIn(Dist.CLIENT)
	public static TeamStageData getClientData() {
		return clientData;
	}

	@OnlyIn(Dist.CLIENT)
	public static void setClientData(TeamStageData stageData) {
		clientData = stageData;
	}
}
