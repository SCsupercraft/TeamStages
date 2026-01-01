package dev.scsupercraft.teamstages.mixin;

import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.darkhax.gamestages.data.IStageData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import dev.scsupercraft.teamstages.data.TeamStageSaveHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Map;
import java.util.UUID;

@Mixin(GameStageSaveHandler.class)
public class GameStageSaveHandlerMixin {
	@Shadow(remap = false) @Final private static Map<UUID, IStageData> GLOBAL_STAGE_DATA;

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void onInit(CallbackInfo ci) {
        TeamStageSaveHandler.setGlobalPlayerStageData(GLOBAL_STAGE_DATA);
    }

	/**
	 * @author SCsupercraft
	 * @reason Replacing StageData with TeamStage's implementation
	 */
    @SuppressWarnings("DataFlowIssue")
    @Overwrite(remap = false)
    @SubscribeEvent
	public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
		File playerFile = getPlayerFile(event.getPlayerDirectory(), event.getPlayerUUID());
		TeamStageSaveHandler.onPlayerLoad(playerFile, event.getEntity().getUUID(), event.getEntity().getName());
	}

	@Shadow(remap = false)
	private static File getPlayerFile(File playerDir, String uuid) { return null; }
}
