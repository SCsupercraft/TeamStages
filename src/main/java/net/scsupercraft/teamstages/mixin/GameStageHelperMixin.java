package net.scsupercraft.teamstages.mixin;

import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.GameStages;
import net.darkhax.gamestages.advancement.HasStageTrigger;
import net.darkhax.gamestages.data.IStageData;
import net.darkhax.gamestages.event.GameStageEvent;
import net.darkhax.gamestages.packet.MessageStages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.scsupercraft.teamstages.TeamStageHelper;
import net.scsupercraft.teamstages.TeamStages;
import net.scsupercraft.teamstages.data.PlayerStageData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Iterator;

@Mixin(GameStageHelper.class)
public class GameStageHelperMixin {
	/**
	 * @author SCsupercraft
	 * @reason Changing the function to return the amount of stages removed, instead of the amount of stages that the player had before removal
	 */
	@Overwrite(remap = false)
	public static int clearStages(ServerPlayer player) {
		PlayerStageData data = TeamStageHelper.getPlayerData(player);
		if (data != null) {
			int stageCount = data.getStages().size();
			data.clear();
			MinecraftForge.EVENT_BUS.post(new GameStageEvent.Cleared(player, data));
			return stageCount - data.getStages().size();
		} else {
			return 0;
		}
	}

	/**
	 * @author SCsupercraft
	 * @reason Replacing StageData with TeamStage's PlayerStageData implementation
	 */
	@Overwrite(remap = false)
	public static void syncPlayer(ServerPlayer player) {
		PlayerStageData info = TeamStageHelper.getPlayerData(player);
		if (info != null) {
			GameStages.LOG.debug("Syncing {} stages for {}.", info.getPlayerStages().size(), player.getName());
			GameStages.NETWORK.syncPlayerStages(player, new MessageStages(info.getPlayerStages()));

			for (String stage: info.getStages()) {
				HasStageTrigger.INSTANCE.trigger(player, stage);
			}
		}
	}
}
