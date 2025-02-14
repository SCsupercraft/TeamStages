package net.scsupercraft.teamstages.data;

import dev.ftb.mods.ftbteams.api.Team;
import net.darkhax.gamestages.data.IStageData;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ITeamStageData extends IStageData  {
	UUID getTeamID();
	@Nullable Team getTeam();
}
