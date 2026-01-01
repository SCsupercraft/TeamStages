package net.scsupercraft.teamstages.ftbquests.tasks;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.AbstractBooleanTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.scsupercraft.teamstages.data.TeamStageHelper;
import net.scsupercraft.teamstages.data.TeamStageSaveHandler;
import net.scsupercraft.teamstages.ftbquests.Tasks;
import net.scsupercraft.teamstages.util.GameStageEffect;

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
		buffer.writeUtf(stage, 512);
		buffer.writeEnum(effect);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		stage = buffer.readUtf(512);
		effect = buffer.readEnum(GameStageEffect.class);
	}

	@Override
	public int autoSubmitOnPlayerTick() {
		return 20;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		return TeamStageSaveHandler.getGameDataForPlayer(player.getUUID()).hasStage(stage, effect);
	}
}
