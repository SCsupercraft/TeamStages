package net.scsupercraft.teamstages.mixin;

import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.darkhax.gamestages.data.IStageData;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.scsupercraft.teamstages.config.CommonConfig;
import net.scsupercraft.teamstages.data.TeamStageHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GameStageHelper.class)
public class GameStageHelperMixin {
	/**
	 * @author SCsupercraft
	 * @reason Changing the function to return the amount of stages removed,
     *  instead of the amount of stages that the player had before removal.
     *  This is required since it might not remove all stages depending on the value of {@link CommonConfig#changeEffect}
	 */
	@Overwrite(remap = false)
	public static int clearStages(ServerPlayer player) {
		IStageData data = GameStageSaveHandler.getClientData();

		if (data != null) {
			int stageCount = data.getStages().size();
			data.clear();
            TeamStageHelper.syncPlayer(player);
			MinecraftForge.EVENT_BUS.post(new GameStageEvent.Cleared(player, data));
			return stageCount - data.getStages().size();
		} else {
			return 0;
		}
	}

	/**
	 * @author SCsupercraft
	 * @reason Replacing StageData with TeamStage's implementation.
	 */
	@Overwrite(remap = false)
	public static void syncPlayer(ServerPlayer player) {
        TeamStageHelper.syncPlayer(player);
	}
}
