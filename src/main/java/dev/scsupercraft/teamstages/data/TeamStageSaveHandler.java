package dev.scsupercraft.teamstages.data;

import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamCreatedEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import net.darkhax.gamestages.GameStages;
import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.darkhax.gamestages.data.IStageData;
import net.darkhax.gamestages.data.StageData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.scsupercraft.teamstages.TeamStages;
import dev.scsupercraft.teamstages.util.FtbUtil;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * The class responsible for saving and loading team stages.
 */
public class TeamStageSaveHandler {
	private static boolean loaded = false;
	private static final Map<UUID, IStageData> GLOBAL_STAGE_DATA = new HashMap<>();
    private static Map<UUID, IStageData> GLOBAL_PLAYER_STAGE_DATA = Collections.emptyMap();

    /**
     * DO NOT USE
     * @param stageData the new map for storing global player stage data
     */
    @ApiStatus.Internal
    public static void setGlobalPlayerStageData(Map<UUID, IStageData> stageData) {
        if (GLOBAL_PLAYER_STAGE_DATA != Collections.EMPTY_MAP) throw new IllegalStateException(
                "Attempted to set global player stage data, but it was already set");
        GLOBAL_PLAYER_STAGE_DATA = stageData;
    }

    /**
     * DO NOT USE
     * @throws ClassNotFoundException if game stages is not installed
     */
    @ApiStatus.Internal
    public static void init() throws ClassNotFoundException {
        Class.forName("net.darkhax.gamestages.data.GameStageSaveHandler");

        TeamEvent.PLAYER_CHANGED.register(TeamStageSaveHandler::onPlayerChangeParty);

        TeamEvent.CREATED.register(TeamStageSaveHandler::onPartyCreated);
        TeamEvent.DELETED.register(TeamStageSaveHandler::onPartyDeleted);
    }

    private static void onPlayerChangeParty(PlayerChangedTeamEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player == null) return;

        GameStageData data = (GameStageData) GLOBAL_PLAYER_STAGE_DATA.get(player.getUUID());
        if (data != null) {
            data.teamStageData = GLOBAL_STAGE_DATA.get(event.getTeam().getTeamId());
            TeamStageHelper.syncPlayer(player);
        }
    }

    private static void onPartyCreated(TeamCreatedEvent event) {
        Team team = event.getTeam();
        UUID teamId = team.getTeamId();

        Team playerTeam = FtbUtil.getPlayerTeam(event.getCreatorId());

        if (team.isPartyTeam() && playerTeam != null)
            copyTeamData(playerTeam.getTeamId(), teamId);
        else createTeamData(teamId);
    }

    private static void onPartyDeleted(TeamEvent event) {
        UUID uuid = event.getTeam().getTeamId();
        GLOBAL_STAGE_DATA.remove(uuid);

        File file = getTeamFile(uuid.toString());
        if (file.exists() && !file.delete())
            TeamStages.LOGGER.warn("Failed to delete file: {}", file);
    }

    /**
     * Hook for the player LoadFromFile event. Allows game stage data to be loaded when the player's data is loaded.
     *
     * @param playerFile the file where the player's data is stored
     * @param uuid       the player's UUID
     * @param name       a component holding the player's display name
     */
    @ApiStatus.Internal
    public static void onPlayerLoad(File playerFile, UUID uuid, Component name) {
        GameStageData stageData = new GameStageData();
        if (playerFile.exists()) {
            try {
                CompoundTag tag = NbtIo.readCompressed(playerFile);
                stageData.readFromNBT(tag);

                Team team = FtbUtil.getTeam(uuid, true);
                IStageData data = team != null ? getTeamData(team.getTeamId()) : null;
                if (data != null)
                    stageData.teamStageData = data;
                else TeamStages.LOGGER.warn("Failed to get team data for player '{}'", name);

                GameStages.LOG.debug("Loaded {} stages for {}.", stageData.getStages().size(), name);
            } catch (IOException exception) {
                GameStages.LOG.error("Could not read player data for {}.", name);
                GameStages.LOG.catching(exception);
            }
        }

        GLOBAL_PLAYER_STAGE_DATA.put(uuid, stageData);
    }

    /**
     * Saves all team stage data.
     */
    @ApiStatus.Internal
	public static void save() {
        createSaveDirectory();
		GLOBAL_STAGE_DATA.forEach(TeamStageSaveHandler::save);
	}

	private static void save(UUID teamID, IStageData data) {
		try {
			CompoundTag tag = data.writeToNBT();
			NbtIo.writeCompressed(tag, getTeamFile(teamID.toString()));
		} catch (IOException e) {
            Team team = FtbUtil.getTeam(teamID, false);
			TeamStages.LOGGER.error("Failed to save data for team {}", team != null ? team.getShortName() : "unknown", e);
		}
	}

    /**
     * Loads all team stage data.
     */
    @ApiStatus.Internal
	public static void load() {
		if (loaded) return;

		File saveDir = getSaveDirectory().toFile();
		GLOBAL_STAGE_DATA.clear();
		if (!saveDir.exists()) {
			registerAllTeams();
			loaded = true;
			return;
		}

		try {
			File[] files = saveDir.listFiles(File::isFile);
			if (files == null) throw new IOException();

			for (File file: files) {
				try {
					IStageData data = new StageData();

                    UUID teamId;
                    {
                        String fileName = file.getName();
                        int extensionIndex = fileName.lastIndexOf('.');

                        teamId = UUID.fromString(fileName.substring(0, extensionIndex));
                    }

					CompoundTag tag = NbtIo.readCompressed(file);

					data.readFromNBT(tag);
					GLOBAL_STAGE_DATA.put(teamId, data);

                    Team team = FtbUtil.getTeam(teamId, false);

					TeamStages.LOGGER.debug("Loaded {} stages for {}.", data.getStages().size(), team != null ? team.getShortName() : "unknown");
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

    /**
     * Lets the save handler know that the data needs loading from the disk.
     */
    @ApiStatus.Internal
	public static void markUnloaded() {
		loaded = false;
	}

	private static void registerAllTeams() {
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

	private static void createTeamData(UUID uuid) {
		GLOBAL_STAGE_DATA.put(uuid, new StageData());
	}

	private static void copyTeamData(UUID srcTeamId, UUID destTeamId) {
		IStageData stageData = new StageData();
		IStageData srcData = getTeamData(srcTeamId);

		if (srcData == null) return;
		for (String stage : srcData.getStages()) {
			stageData.addStage(stage);
		}

		GLOBAL_STAGE_DATA.put(destTeamId, stageData);
	}

    /**
     * Gets the stage data for the team with the provided UUID.
     *
     * @param uuid the team's UUID
     * @return the stage data
     */
	@Nullable
	public static IStageData getTeamData(UUID uuid) {
		return GLOBAL_STAGE_DATA.get(uuid);
	}


    /**
     * Gets the team stage data for the player with the provided UUID.
     *
     * @param uuid the player's UUID
     * @return the stage data
     */
    @Nullable
    public static IStageData getTeamDataForPlayer(UUID uuid) {
        GameStageData data = (GameStageData) GLOBAL_PLAYER_STAGE_DATA.get(uuid);
        return data != null ? data.teamStageData : null;
    }

    /**
     * Gets the player stage data for the player with the provided UUID.
     *
     * @param uuid the player's UUID
     * @return the stage data
     */
    @Nullable
    public static IStageData getPlayerData(UUID uuid) {
        GameStageData data = (GameStageData) GLOBAL_PLAYER_STAGE_DATA.get(uuid);
        return data != null ? data.playerStageData : null;
    }

    /**
     * Gets the game stage data for the player with the provided UUID.
     *
     * @param uuid the player's UUID
     * @return the stage data
     */
    public static IGameStageData getGameDataForPlayer(UUID uuid) {
        return ((IGameStageData) GLOBAL_PLAYER_STAGE_DATA.get(uuid));
    }

	private static File getTeamFile(String uuid) {
		return new File(createSaveDirectory(), uuid + ".dat");
	}

	private static Path getSaveDirectory() {
		return TeamStages.server.getWorldPath(LevelResource.ROOT).resolve("ftbteams/teamstages");
	}

    private static File createSaveDirectory() {
        File saveDir = getSaveDirectory().toFile();
        if (!saveDir.exists() && !saveDir.mkdirs())
            TeamStages.LOGGER.error("Failed to create save directory: {}", saveDir);

        return saveDir;
    }

    /**
     * Sets the client's synced stage data.
     *
     * @param player the player stage data for the client
     * @param team   the team stage data for the client
     */
    public static void setClientData(Collection<String> player, Collection<String> team) {
        GameStageData stageData = new GameStageData();
        stageData.teamStageData = new StageData();

        for (String stage : player) {
            stageData.playerStageData.addStage(stage);
        }

        for (String stage : team) {
            stageData.teamStageData.addStage(stage);
        }

        GameStageSaveHandler.setClientData(stageData);
    }

    /**
     * Gets the player stage data of the current player that has been synced to the client.
     *
     * @return the current client side stage data
     */
    @OnlyIn(Dist.CLIENT)
	public static IStageData getClientPlayerData() {
		return GameStageSaveHandler.getClientData() instanceof GameStageData data
                ? data.playerStageData
                : null;
	}

    /**
     * Gets the team stage data of the current player that has been synced to the client.
     *
     * @return the current client side stage data
     */
    @OnlyIn(Dist.CLIENT)
    public static IStageData getClientTeamData() {
        return GameStageSaveHandler.getClientData() instanceof GameStageData data
                ? data.teamStageData
                : null;
    }
}
