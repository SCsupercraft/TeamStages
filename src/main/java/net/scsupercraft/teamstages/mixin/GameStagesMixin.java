package net.scsupercraft.teamstages.mixin;

import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.GameStages;
import net.darkhax.gamestages.data.IStageData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.scsupercraft.teamstages.data.PlayerStageData;
import net.scsupercraft.teamstages.data.TeamStageSaveHandler;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;

@Mixin(GameStages.class)
public class GameStagesMixin {
	@OnlyIn(Dist.CLIENT)
	private void onF3Text(CustomizeGuiOverlayEvent.DebugText event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.renderDebug) {
			ArrayList<String> F3Text;
			ChatFormatting formatting;
			if (mc.player != null && mc.player.isShiftKeyDown()) {
				IStageData data = GameStageHelper.getPlayerData(mc.player);
				if (data != null) {
					F3Text = event.getRight();
					formatting = ChatFormatting.GOLD;
					F3Text.add(formatting + ChatFormatting.UNDERLINE.toString() + "GameStages");
					F3Text.add("Count: " + data.getStages().size());
					F3Text.add("Type: " + data.getClass().getName());
					if (data instanceof PlayerStageData playerStageData) {
						F3Text.add("Stages: " + playerStageData.getPlayerStages().toString());
					} else {
						F3Text.add("Stages: " + data.getStages().toString());
					}
					if (TeamStageSaveHandler.getClientData() != null) F3Text.add("TeamStages: " + TeamStageSaveHandler.getClientData().getStages().toString());
				}
			} else {
				F3Text = event.getRight();
				formatting = ChatFormatting.GOLD;
				F3Text.add(formatting + ChatFormatting.UNDERLINE.toString() + "GameStages [Shift]");
			}
		}
	}
}
