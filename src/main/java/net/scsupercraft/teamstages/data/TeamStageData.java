package net.scsupercraft.teamstages.data;

import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import net.darkhax.gamestages.data.StageData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.scsupercraft.teamstages.TeamStageHelper;
import net.scsupercraft.teamstages.util.FtbUtil;
import net.scsupercraft.teamstages.util.GameStageEffect;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TeamStageData extends StageData implements ITeamStageData {
	public static final String TAG_STAGES_TEAM_ID = "StagesTeamId";
	protected UUID teamID;

	public TeamStageData(UUID teamID) {
		this.teamID = teamID;
	}

	public UUID getTeamID() {
		return teamID;
	}
	public @Nullable Team getTeam() {
		if (!TeamStageSaveHandler.isLoaded()) return null;

		TeamManager manager = FtbUtil.getTeamManager();
		if (manager == null) return null;

		Optional<Team> optionalTeam = manager.getTeamByID(teamID);
		return optionalTeam.orElse(null);
	}

	public boolean memberHasStage(String stage) {
		return getMemberStages().contains(stage);
	}

	@Override
	public void addStage(String stage) {
		addStage(stage, true);
	}
	public void addStage(String stage, boolean sync) {
		super.addStage(stage);
		if (sync) TeamStageHelper.syncTeam(getTeam());
	}

	@Override
	public void removeStage(String stage) {
		removeStage(stage, true);
	}
	public void removeStage(String stage, boolean sync) {
		if (!hasStage(stage)) return;
		super.removeStage(stage);
		if (sync) TeamStageHelper.syncTeam(getTeam());
	}

	@Override
	public void clear() {
		clear(true);
	}
	public void clear(boolean sync) {
		super.clear();
		if (sync) TeamStageHelper.syncTeam(getTeam());
	}

	public Collection<String> getMemberStages() {
		Set<String> stages = new HashSet<>();

		Team team = getTeam();
		if (team == null) return Collections.unmodifiableCollection(stages);

		for (ServerPlayer player : team.getOnlineMembers()) {
			PlayerStageData data = TeamStageHelper.getPlayerData(player);
			if (data != null) stages.addAll(data.getStages(GameStageEffect.PLAYER));
		}

		return Collections.unmodifiableCollection(stages);
	}

	@Override
	public void readFromNBT(CompoundTag tag) {
		ListTag list = tag.getList("Stages", Tag.TAG_STRING);

		for(int tagIndex = 0; tagIndex < list.size(); ++tagIndex) {
			this.addStage(list.getString(tagIndex), false);
		}

		teamID = tag.getUUID(TAG_STAGES_TEAM_ID);
	}

	@Override
	public CompoundTag writeToNBT() {
		CompoundTag tag = super.writeToNBT();
		tag.putUUID(TAG_STAGES_TEAM_ID, teamID);
		return tag;
	}

	@Override
	public String toString() {
		return "TeamStageData [owner=" + (getTeam() != null ? getTeam().getShortName() : "unknown") + ", unlockedStages=" + this.getStages() + "]";
	}
}
