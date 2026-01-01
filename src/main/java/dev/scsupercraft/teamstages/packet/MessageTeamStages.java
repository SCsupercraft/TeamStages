package dev.scsupercraft.teamstages.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import dev.scsupercraft.teamstages.packet.handler.MessageTeamStagesHandler;

import java.util.*;
import java.util.function.Supplier;

public record MessageTeamStages(Collection<String> player, Collection<String> team) {
    public static void encode(MessageTeamStages msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeCollection(msg.player, MessageTeamStages::writeStage);
        friendlyByteBuf.writeCollection(msg.team, MessageTeamStages::writeStage);
    }

    public static MessageTeamStages decode(FriendlyByteBuf friendlyByteBuf) {
        return new MessageTeamStages(readStages(friendlyByteBuf), readStages(friendlyByteBuf));
    }

    private static void writeStage(FriendlyByteBuf friendlyByteBuf, String s) {
        friendlyByteBuf.writeUtf(s, 512);
    }

    private static String readStage(FriendlyByteBuf friendlyByteBuf) {
        return friendlyByteBuf.readUtf(512);
    }

    private static Collection<String> readStages(FriendlyByteBuf friendlyByteBuf) {
        return Collections.unmodifiableCollection(friendlyByteBuf.readCollection(ArrayList::new, MessageTeamStages::readStage));
    }

    public static void handle(MessageTeamStages msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> MessageTeamStagesHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
