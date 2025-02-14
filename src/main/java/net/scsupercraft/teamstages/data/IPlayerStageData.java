package net.scsupercraft.teamstages.data;

import net.darkhax.gamestages.data.IStageData;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IPlayerStageData extends IStageData {
	UUID getPlayerID();
	@Nullable ServerPlayer getPlayer();
}
