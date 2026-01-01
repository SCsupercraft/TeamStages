package net.scsupercraft.teamstages.mixin;

import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.GameStages;
import net.darkhax.gamestages.data.IStageData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.scsupercraft.teamstages.data.TeamStageHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;

@Mixin(GameStages.class)
public class GameStagesMixin {
    /**
     * @author SCsupercraft
     * @reason Replacing StageData with TeamStage's implementation
     */
    @Overwrite(remap = false)
	@OnlyIn(Dist.CLIENT)
	private void onF3Text(CustomizeGuiOverlayEvent.DebugText event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.renderDebug) {
			ArrayList<String> F3Text = event.getRight();
			if (mc.player != null && mc.player.isShiftKeyDown()) {
                IStageData gameData = GameStageHelper.getPlayerData(mc.player);
				IStageData playerData = TeamStageHelper.player().getPlayerData(mc.player);
                IStageData teamData = TeamStageHelper.team().getTeamData(mc.player);
                if (gameData == null) return;

				if (playerData != null && teamData != null) {
					F3Text.add(ChatFormatting.GOLD + ChatFormatting.UNDERLINE.toString() + "GameStages");

					F3Text.add("PlayerCount: " + playerData.getStages().size());
                    F3Text.add("TeamCount: " + teamData.getStages().size());
					F3Text.add("Type: " + gameData.getClass().getName());
                    F3Text.add("PlayerStages: " + playerData.getStages().toString());
                    F3Text.add("TeamStages: " + teamData.getStages().toString());
				} else {
                    F3Text.add(ChatFormatting.GOLD + ChatFormatting.UNDERLINE.toString() + "GameStages");
                    F3Text.add("Count: " + gameData.getStages().size());
                    F3Text.add("Type: " + gameData.getClass().getName());
                    F3Text.add("Stages: " + gameData.getStages().toString());
                }
			} else {
				F3Text.add(ChatFormatting.GOLD + ChatFormatting.UNDERLINE.toString() + "GameStages [Shift]");
			}
		}
	}
}
