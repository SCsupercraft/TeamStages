package net.scsupercraft.teamstages.mixin;

import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.darkhax.gamestages.data.IStageData;
import net.darkhax.gamestages.data.StageData;
import net.darkhax.gamestages.event.StagesSyncedEvent;
import net.darkhax.gamestages.packet.GameStagesPacketHandler;
import net.darkhax.gamestages.packet.MessageStages;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import net.scsupercraft.teamstages.data.PlayerStageData;
import net.scsupercraft.teamstages.data.TeamStageSaveHandler;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

@Mixin(GameStagesPacketHandler.class)
public class GameStagesPacketHandlerMixin {
	private void processSyncStagesMessage(MessageStages message, Supplier<NetworkEvent.Context> ctx) {
		Minecraft mc = Minecraft.getInstance();
		IStageData clientData;
		if (mc.player != null && TeamStageSaveHandler.getClientData() != null) {
			clientData = new PlayerStageData(mc.player.getUUID(), TeamStageSaveHandler.getClientData().getTeamID());
		} else {
			clientData = new StageData();
		}

		if (clientData instanceof PlayerStageData playerStageData) {
			for (String stageName: message.getStages()) {
				playerStageData.addPlayerStage(stageName, false);
			}
		} else {
			for (String stageName: message.getStages()) {
				clientData.addStage(stageName);
			}
		}

		GameStageSaveHandler.setClientData(clientData);
		MinecraftForge.EVENT_BUS.post(new StagesSyncedEvent(clientData));
		ctx.get().setPacketHandled(true);
	}
}
