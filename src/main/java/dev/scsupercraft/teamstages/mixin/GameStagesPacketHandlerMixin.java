package dev.scsupercraft.teamstages.mixin;

import net.darkhax.gamestages.packet.GameStagesPacketHandler;
import net.darkhax.gamestages.packet.MessageStages;
import net.minecraftforge.network.NetworkEvent;
import dev.scsupercraft.teamstages.packet.MessageTeamStages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Supplier;

@Mixin(GameStagesPacketHandler.class)
public class GameStagesPacketHandlerMixin {
    /**
     * @author SCsupercraft
     * @reason This should never be called due to being replaced by {@link MessageTeamStages}
     */
    @Overwrite(remap = false)
	private void processSyncStagesMessage(MessageStages message, Supplier<NetworkEvent.Context> ctx) {
		throw new AssertionError();
	}
}
