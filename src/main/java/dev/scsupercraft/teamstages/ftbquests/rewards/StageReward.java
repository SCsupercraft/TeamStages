package dev.scsupercraft.teamstages.ftbquests.rewards;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.scsupercraft.teamstages.data.IGameStageData;
import dev.scsupercraft.teamstages.data.TeamStageHelper;
import dev.scsupercraft.teamstages.data.TeamStageSaveHandler;
import dev.scsupercraft.teamstages.ftbquests.Rewards;
import dev.scsupercraft.teamstages.util.GameStageEffect;

public class StageReward extends Reward {
	protected String stage = "";
	protected GameStageEffect effect = GameStageEffect.PLAYER;
	protected boolean remove = false;
	public StageReward(long id, Quest q) {
		super(id, q);
		this.autoclaim = RewardAutoClaim.INVISIBLE;
	}

	@Override
	public RewardType getType() {
		return Rewards.Stage;
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

	public boolean getRemove() {
		return remove;
	}
	public void setRemove(boolean remove) {
		this.remove = remove;
	}

	@Override
	public void claim(ServerPlayer player, boolean notify) {
		IGameStageData data = TeamStageSaveHandler.getGameDataForPlayer(player.getUUID());
		if (data == null) return;

		if (remove) {
			data.removeStage(stage, effect);
		} else {
			data.addStage(stage, effect);
		}

        TeamStageHelper.syncPlayer(player);

		if (!notify) return;
		if (remove) {
			player.sendSystemMessage(Component.translatable("commands.gamestage.remove.target", stage), true);
		} else {
			player.sendSystemMessage(Component.translatable("commands.gamestage.add.target", stage), true);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		config.addString("stage", stage, this::setStage, "").setNameKey("teamstages.quests.rewards.stage.stage");
		config.addEnum("effect", effect, this::setEffect, NameMap.of(GameStageEffect.PLAYER, GameStageEffect.values()).create()).setNameKey("teamstages.quests.rewards.stage.effect");
		config.addBool("remove", remove, this::setRemove, false).setNameKey("teamstages.quests.rewards.stage.remove");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public MutableComponent getAltTitle() {
		return Component.translatable("teamstages.quests.rewards.stage.altTitle", getStage());
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		nbt.putString("stage", stage);
		nbt.putString("effect", effect.toString());
		nbt.putBoolean("remove", remove);
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		stage = nbt.getString("stage");
		effect = GameStageEffect.valueOf(nbt.getString("effect"));
		remove = nbt.getBoolean("remove");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(stage, 32767);
		buffer.writeEnum(effect);
		buffer.writeBoolean(remove);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		stage = buffer.readUtf(32767);
		effect = buffer.readEnum(GameStageEffect.class);
		remove = buffer.readBoolean();
	}
}
