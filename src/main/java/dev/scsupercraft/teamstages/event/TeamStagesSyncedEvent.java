package dev.scsupercraft.teamstages.event;

import net.darkhax.gamestages.data.IStageData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class TeamStagesSyncedEvent extends PlayerEvent {
    private final IStageData playerData;
    private final IStageData teamData;

    public TeamStagesSyncedEvent(IStageData playerData, IStageData teamData) {
        this(playerData, teamData, Minecraft.getInstance().player);
    }

    public TeamStagesSyncedEvent(IStageData playerData, IStageData teamData, Player player) {
        super(player);
        this.playerData = playerData;
        this.teamData = teamData;
    }

    public IStageData getTeamData() {
        return this.teamData;
    }

    public IStageData getPlayerData() {
        return playerData;
    }
}
