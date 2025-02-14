package net.scsupercraft.teamstages.data;

import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import net.darkhax.gamestages.data.StageData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.scsupercraft.teamstages.TeamStageHelper;
import net.scsupercraft.teamstages.TeamStages;
import net.scsupercraft.teamstages.config.CommonConfig;
import net.scsupercraft.teamstages.ftbquests.FtbQuestsIntegration;
import net.scsupercraft.teamstages.ftbquests.tasks.StageTask;
import net.scsupercraft.teamstages.util.FtbUtil;
import net.scsupercraft.teamstages.util.GameStageEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerStageData extends StageData implements ITeamStageData, IPlayerStageData {
	public static final String TAG_STAGES_TEAM_ID = "StagesTeamId";
	public static final String TAG_STAGES_PLAYER_ID = "StagesPlayerId";
	protected UUID teamID;
	protected UUID playerID;

	public PlayerStageData(UUID playerID, @Nullable UUID teamID) {
		super();
		this.playerID = playerID;
		if (teamID != null) {
			this.teamID = teamID;
		} else update();
	}

	@Override
	public UUID getPlayerID() {
		return playerID;
	}

	@Override
	public @Nullable ServerPlayer getPlayer() {
		return TeamStages.server.getPlayerList().getPlayer(playerID);
	}

	@Override
	public @Nullable UUID getTeamID() {
		return teamID;
	}

	@Override
	public @Nullable Team getTeam() {
		if (!TeamStageSaveHandler.isLoaded()) return null;

		TeamManager manager = FtbUtil.getTeamManager();
		if (manager == null) return null;

		Optional<Team> optionalTeam = manager.getTeamByID(teamID);
		return optionalTeam.orElse(null);
	}

	public @Nullable TeamStageData getTeamStageData() {
		return TeamStageSaveHandler.getTeamData(getTeamID());
	}

	public boolean needsUpdate() {
		ServerPlayer player = getPlayer();
		TeamManager manager = FtbUtil.getTeamManager();
		if (manager == null || player == null) return false;

		Optional<Team> optionalTeam = manager.getTeamForPlayer(getPlayer());
		return optionalTeam.filter(value -> value.getTeamId() != teamID).isPresent();
	}

	public void update() {
		if (!needsUpdate()) return;

		TeamManager manager = FtbUtil.getTeamManager();
		if (manager == null) return;

		Optional<Team> optionalTeam = manager.getTeamForPlayer(getPlayer());
		if (optionalTeam.isEmpty()) return;

		Team team = optionalTeam.get();
		teamID = team.getTeamId();

		TeamStageHelper.syncTeam(getTeam());
	}

	@Override
	public Collection<String> getStages() {
		return getStages(CommonConfig.listEffect);
	}

	public Collection<String> getStages(GameStageEffect effect) {
		Set<String> collection = new HashSet<>();

		if (effect.isTeamEffect()) {
			TeamStageData data = getTeamStageData();
			if (data != null) collection.addAll(data.getStages());
		}
		if (effect.isPlayerEffect()) {
			collection.addAll(super.getStages());
		}

		return Collections.unmodifiableCollection(collection);
	}

	@Override
	public boolean hasStage(@NotNull String stage) {
		return hasStage(stage, CommonConfig.checkEffect);
	}
	public boolean hasStage(@NotNull String stage, GameStageEffect effect) {
		return getStages(effect).contains(stage);
	}

	@Override
	public void addStage(String stage) {
		addStage(stage, true);
	}
	public void addStage(String stage, boolean sync) {
		addStage(stage, sync, CommonConfig.changeEffect);
	}
	public void addStage(String stage, boolean sync, GameStageEffect effect) {
		boolean syncTeam = false;
		if (effect.isTeamEffect()) {
			TeamStageData data = getTeamStageData();
			if (data != null) data.addStage(stage);
			if (sync) {
				syncTeam = true;
			}
		}
		if (effect.isPlayerEffect()) {
			super.addStage(stage);
		}
		if (syncTeam) { TeamStageHelper.syncTeam(getTeam()); } else if (sync) { TeamStageHelper.syncPlayer(getPlayer()); }
		if (FtbQuestsIntegration.enabled) StageTask.checkStages(getPlayer());
	}

	@Override
	public void removeStage(String stage) {
		removeStage(stage, true);
	}
	public void removeStage(String stage, boolean sync) {
		removeStage(stage, sync, CommonConfig.changeEffect);
	}
	public void removeStage(String stage, boolean sync, GameStageEffect effect) {
		boolean syncTeam = false;
		if (effect.isTeamEffect()) {
			TeamStageData data = getTeamStageData();
			if (data != null && data.hasStage(stage)) data.removeStage(stage);
			if (sync) {
				syncTeam = true;
			}
		}
		if (effect.isPlayerEffect() && super.hasStage(stage)) {
			super.removeStage(stage);
		}
		if (syncTeam) { TeamStageHelper.syncTeam(getTeam()); } else if (sync) { TeamStageHelper.syncPlayer(getPlayer()); }
	}

	@Override
	public void clear() {
		clear(true);
	}
	public void clear(boolean sync) {
		boolean syncTeam = false;
		if (CommonConfig.changeEffect.isTeamEffect()) {
			TeamStageData data = getTeamStageData();
			if (data != null) data.clear();
			if (sync) {
				syncTeam = true;
			}
		}
		if (CommonConfig.changeEffect.isPlayerEffect()) {
			super.clear();
		}
		if (syncTeam) { TeamStageHelper.syncTeam(getTeam()); } else if (sync) { TeamStageHelper.syncPlayer(getPlayer()); }
	}

	public Collection<String> getPlayerStages() {
		return super.getStages();
	}

	public boolean playerHasStage(@NotNull String stage) {
		return super.hasStage(stage);
	}

	public void addPlayerStage(String stage) {
		addPlayerStage(stage, true);
	}
	public void addPlayerStage(String stage, boolean sync) {
		super.addStage(stage);
		if (sync) TeamStageHelper.syncPlayer(getPlayer());
	}

	public void removePlayerStage(String stage) {
		removePlayerStage(stage, true);
	}
	public void removePlayerStage(String stage, boolean sync) {
		super.removeStage(stage);
		if (sync) TeamStageHelper.syncPlayer(getPlayer());
	}

	public void clearPlayer() {
		clearPlayer(true);
	}
	public void clearPlayer(boolean sync) {
		super.clear();
		if (sync) TeamStageHelper.syncPlayer(getPlayer());
	}

	@Override
	public void readFromNBT(CompoundTag tag) {
		ListTag list = tag.getList("Stages", Tag.TAG_STRING);

		for(int tagIndex = 0; tagIndex < list.size(); ++tagIndex) {
			this.addStage(list.getString(tagIndex), false, GameStageEffect.PLAYER);
		}

		if (tag.contains(TAG_STAGES_TEAM_ID)) {
			teamID = tag.getUUID(TAG_STAGES_TEAM_ID);
		} else {
			teamID = null;
		}
		playerID = tag.getUUID(TAG_STAGES_PLAYER_ID);
	}

	@Override
	public CompoundTag writeToNBT() {
		CompoundTag tag = super.writeToNBT();
		tag.putUUID(TAG_STAGES_TEAM_ID, teamID);
		tag.putUUID(TAG_STAGES_PLAYER_ID, playerID);
		return tag;
	}

	@Override
	public String toString() {
		return "PlayerStageData [owner=" + (getPlayer() != null ? getPlayer().getScoreboardName() : "unknown") + ", unlockedStages=" + this.getStages() + "]";
	}
}
