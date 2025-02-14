package net.scsupercraft.teamstages.ftbquests.tasks;

import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.AbstractBooleanTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.scsupercraft.teamstages.TeamStageHelper;
import net.scsupercraft.teamstages.data.PlayerStageData;
import net.scsupercraft.teamstages.data.TeamStageData;
import net.scsupercraft.teamstages.data.TeamStageSaveHandler;
import net.scsupercraft.teamstages.ftbquests.Tasks;
import net.scsupercraft.teamstages.util.FtbUtil;
import net.scsupercraft.teamstages.util.GameStageEffect;

import javax.annotation.Nullable;

public class StageTask extends AbstractBooleanTask {
	protected String stage = "";
	protected GameStageEffect effect = GameStageEffect.PLAYER;
	public StageTask(long id, Quest quest) {
		super(id, quest);
	}

	@Override
	public TaskType getType() {
		return Tasks.Stage;
	}

	public String getStage() {
		return stage;
	}
	public void setStage(String stage) {
		if (TeamStageHelper.isValidStageName(stage)) this.stage = stage;
	}

	public GameStageEffect getEffect() {
		return effect;
	}
	public void setEffect(GameStageEffect effect) {
		this.effect = effect;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addString("stage", stage, this::setStage, "").setNameKey("teamstages.quests.tasks.stage.stage");
		config.addEnum("effect", effect, this::setEffect, NameMap.of(GameStageEffect.PLAYER, GameStageEffect.values()).create()).setNameKey("teamstages.quests.tasks.stage.effect");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("teamstages.quests.tasks.stage.altTitle", getStage());
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("stage", stage);
		nbt.putString("effect", effect.toString());
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		stage = nbt.getString("stage");
		effect = GameStageEffect.valueOf(nbt.getString("effect"));
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(stage, 32767);
		buffer.writeEnum(effect);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		stage = buffer.readUtf(32767);
		effect = buffer.readEnum(GameStageEffect.class);
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 20;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		Team team = FtbUtil.getTeam(teamData.getTeamId(), false);
		return teamCondition(team) || playerCondition(player);
	}

	private boolean teamCondition(@Nullable Team team) {
		if (team == null) return false;

		TeamStageData data = TeamStageHelper.getTeamData(team);
		if (data == null) return false;

		return effect.isTeamEffect() && data.hasStage(stage);
	}
	private boolean playerCondition(ServerPlayer player) {
		PlayerStageData data = TeamStageHelper.getPlayerData(player);
		if (data == null) return false;

		return effect.isPlayerEffect() && data.playerHasStage(stage);
	}

	public static void checkStages(ServerPlayer player) {
		if (player == null || !TeamStageSaveHandler.isLoaded()) return;
		Team team = FtbUtil.getTeam(player);
		if (team == null) return;

		TeamData data = ServerQuestFile.INSTANCE != null && !PlayerHooks.isFake(player) ? ServerQuestFile.INSTANCE.getOrCreateTeamData(team) : null;
		if (data != null && !data.isLocked()) {
			ServerQuestFile.INSTANCE.withPlayerContext(player, () -> {
				for (Task task: ServerQuestFile.INSTANCE.getAllTasks()) {
					if (task instanceof StageTask && data.canStartTasks(task.getQuest())) {
						task.submitTask(data, player);
					}
				}
			});
		}
	}
}
