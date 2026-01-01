package dev.scsupercraft.teamstages.event;

import net.darkhax.gamestages.data.IStageData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * An event that is fired after stages are synced to the client.
 */
public class TeamStagesSyncedEvent extends PlayerEvent {
    private final IStageData playerData;
    private final IStageData teamData;

    /**
     * Creates a new team stages synced event.
     *
     * @param playerData the synced player data
     * @param teamData   the synced team data
     */
    public TeamStagesSyncedEvent(IStageData playerData, IStageData teamData) {
        this(playerData, teamData, Minecraft.getInstance().player);
    }

    /**
     * Creates a new team stages synced event.
     *
     * @param playerData the synced player data
     * @param teamData   the synced team data
     * @param player     the player the data was synced for
     */
    public TeamStagesSyncedEvent(IStageData playerData, IStageData teamData, Player player) {
        super(player);
        this.playerData = playerData;
        this.teamData = teamData;
    }

    /**
     * {@return the synced team data}
     */
    public IStageData getTeamData() {
        return this.teamData;
    }

    /**
     * {@return the synced player data}
     */
    public IStageData getPlayerData() {
        return playerData;
    }
}
