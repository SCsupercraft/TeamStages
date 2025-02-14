package net.scsupercraft.teamstages.packet.handler;

import net.darkhax.gamestages.event.StagesSyncedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import net.scsupercraft.teamstages.data.TeamStageData;
import net.scsupercraft.teamstages.data.TeamStageSaveHandler;
import net.scsupercraft.teamstages.packet.MessageTeamStages;

import java.util.function.Supplier;

public class MessageTeamStagesHandler {
	public static void handle(MessageTeamStages msg, Supplier<NetworkEvent.Context> ctx) {
		TeamStageData clientData = new TeamStageData(msg.getTeamId());

		for (String stage: msg.getStages()) {
			clientData.addStage(stage, false);
		}

		TeamStageSaveHandler.setClientData(clientData);
		MinecraftForge.EVENT_BUS.post(new StagesSyncedEvent(clientData));
	}
}
