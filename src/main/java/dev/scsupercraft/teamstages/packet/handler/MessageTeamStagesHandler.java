package dev.scsupercraft.teamstages.packet.handler;

import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.darkhax.gamestages.event.StagesSyncedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import dev.scsupercraft.teamstages.data.TeamStageSaveHandler;
import dev.scsupercraft.teamstages.event.TeamStagesSyncedEvent;
import dev.scsupercraft.teamstages.packet.MessageTeamStages;

import java.util.function.Supplier;

public class MessageTeamStagesHandler {
	public static void handle(MessageTeamStages msg, Supplier<NetworkEvent.Context> ctx) {
        TeamStageSaveHandler.setClientData(msg.player(), msg.team());

        MinecraftForge.EVENT_BUS.post(new StagesSyncedEvent(
                GameStageSaveHandler.getClientData()
        ));
		MinecraftForge.EVENT_BUS.post(new TeamStagesSyncedEvent(
                TeamStageSaveHandler.getClientPlayerData(),
                TeamStageSaveHandler.getClientTeamData()
        ));
	}
}
