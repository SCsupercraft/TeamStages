package net.scsupercraft.teamstages.packet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.scsupercraft.teamstages.TeamStages;

import java.util.Optional;

public class TeamStagesPacketHandler {
	private static int packetCount;
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(TeamStages.MOD_ID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	public static void init() {
		INSTANCE.registerMessage(packetCount++, MessageTeamStages.class, MessageTeamStages::encode, MessageTeamStages::decode, MessageTeamStages::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}

	public static <MSG> void sendToServer(MSG packet) {
		INSTANCE.sendToServer(packet);
	}

	public static <MSG> void sendToClient(ServerPlayer player, MSG packet) {
		INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
	}
}
