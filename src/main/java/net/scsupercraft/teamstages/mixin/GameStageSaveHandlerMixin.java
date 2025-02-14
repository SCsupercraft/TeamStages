package net.scsupercraft.teamstages.mixin;

import net.darkhax.gamestages.GameStages;
import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.darkhax.gamestages.data.IStageData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.scsupercraft.teamstages.data.PlayerStageData;
import net.scsupercraft.teamstages.util.FtbUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Mixin(GameStageSaveHandler.class)
public class GameStageSaveHandlerMixin {
	@Shadow(remap = false) @Final private static Map<UUID, IStageData> GLOBAL_STAGE_DATA;

	@Accessor(value = "GLOBAL_STAGE_DATA", remap = false)
	public static Map<UUID, IStageData> getGlobalStageData() {
		return GLOBAL_STAGE_DATA;
	}

	/**
	 * @author SCsupercraft
	 * @reason Replacing StageData with TeamStage's PlayerStageData implementation
	 */
	@Overwrite(remap = false)
	@SubscribeEvent
	public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
		UUID playerUUID = UUID.fromString(event.getPlayerUUID());
		UUID teamUUID = FtbUtil.getTeamManager() != null && FtbUtil.getTeamManager().getTeamForPlayerID(playerUUID).isPresent() ? FtbUtil.getTeamManager().getTeamForPlayerID(playerUUID).orElseThrow().getTeamId() : null;
		File playerFile = getPlayerFile(event.getPlayerDirectory(), event.getPlayerUUID());
		IStageData playerData = new PlayerStageData(playerUUID, teamUUID);
		if (playerFile.exists()) {
			try {
				CompoundTag tag = NbtIo.readCompressed(playerFile);
				tag.putUUID(PlayerStageData.TAG_STAGES_PLAYER_ID, playerUUID);
				if (teamUUID != null) { tag.putUUID(PlayerStageData.TAG_STAGES_TEAM_ID, teamUUID); }
				playerData.readFromNBT(tag);
				GameStages.LOG.debug("Loaded {} stages for {}.", playerData.getStages().size(), event.getEntity().getName());
			} catch (IOException var4) {
				GameStages.LOG.error("Could not read player data for {}.", event.getEntity().getName());
				GameStages.LOG.catching(var4);
			}
		}

		getGlobalStageData().put(event.getEntity().getUUID(), playerData);
	}

	@Shadow(remap = false)
	private static File getPlayerFile(File playerDir, String uuid) { return null; }
}
