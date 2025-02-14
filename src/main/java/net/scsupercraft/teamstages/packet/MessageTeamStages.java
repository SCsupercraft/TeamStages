package net.scsupercraft.teamstages.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.scsupercraft.teamstages.packet.handler.MessageTeamStagesHandler;

import java.util.*;
import java.util.function.Supplier;

public class MessageTeamStages {
	private final UUID teamId;
	private final List<String> stages;

	public MessageTeamStages(UUID teamId, Collection<String> stages) {
		this.teamId = teamId;
		this.stages = new ArrayList<>(stages);
	}

	public List<String> getStages() {
		return this.stages;
	}
	public UUID getTeamId() {
		return this.teamId;
	}

	public static void encode(MessageTeamStages msg, FriendlyByteBuf friendlyByteBuf) {
		CompoundTag tag = new CompoundTag();
		ListTag listTag = new ListTag();

		for (String stage : msg.stages) {
			listTag.add(StringTag.valueOf(stage));
		}

		tag.put("Stages", listTag);
		tag.putUUID("TeamId", msg.teamId);

		friendlyByteBuf.writeNbt(tag);
	}

	public static MessageTeamStages decode(FriendlyByteBuf friendlyByteBuf) {
		CompoundTag nbt = friendlyByteBuf.readAnySizeNbt();
		ListTag stages = nbt.getList("Stages", Tag.TAG_STRING);
		Collection<String> stagesCollection = stages.stream().map((Tag::getAsString)).toList();

		return new MessageTeamStages(nbt.getUUID("TeamId"), stagesCollection);
	}

	public static void handle(MessageTeamStages msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> MessageTeamStagesHandler.handle(msg, ctx));
		ctx.get().setPacketHandled(true);
	}
}
