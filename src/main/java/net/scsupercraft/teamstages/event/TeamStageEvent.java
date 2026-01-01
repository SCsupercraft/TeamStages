package net.scsupercraft.teamstages.event;

import dev.ftb.mods.ftbteams.api.Team;
import net.darkhax.gamestages.data.IStageData;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

public class TeamStageEvent extends Event {
    private final String stageName;
    private final Team team;

    public TeamStageEvent(@NotNull Team team, @NotNull String stageName) {
        this.team = team;
        this.stageName = stageName;
    }

    public @NotNull String getStageName() {
        return this.stageName;
    }

    public @NotNull Team getTeam() {
        return team;
    }

    @Cancelable
    public static class Add extends TeamStageEvent {
        public Add(@NotNull Team team, @NotNull String stageName) {
            super(team, stageName);
        }

        public GameStageEvent.Add asGameStageEvent(Player player) {
            return new GameStageEvent.Add(player, getStageName());
        }
    }

    public static class Added extends TeamStageEvent {
        public Added(@NotNull Team team, @NotNull String stageName) {
            super(team, stageName);
        }

        public GameStageEvent.Added asGameStageEvent(Player player) {
            return new GameStageEvent.Added(player, getStageName());
        }
    }

    @Cancelable
    public static class Remove extends TeamStageEvent {
        public Remove(@NotNull Team team, @NotNull String stageName) {
            super(team, stageName);
        }

        public GameStageEvent.Remove asGameStageEvent(Player player) {
            return new GameStageEvent.Remove(player, getStageName());
        }
    }

    public static class Removed extends TeamStageEvent {
        public Removed(@NotNull Team team, @NotNull String stageName) {
            super(team, stageName);
        }

        public GameStageEvent.Removed asGameStageEvent(Player player) {
            return new GameStageEvent.Removed(player, getStageName());
        }
    }

    public static class Cleared extends Event {
        private final IStageData stageData;
        private final Team team;

        public Cleared(@NotNull Team team, @NotNull IStageData stageData) {
            this.team = team;
            this.stageData = stageData;
        }

        public @NotNull IStageData getStageData() {
            return this.stageData;
        }

        public @NotNull Team getTeam() {
            return team;
        }

        public GameStageEvent.Cleared asGameStageEvent(Player player) {
            return new GameStageEvent.Cleared(player, stageData);
        }
    }

    public static class Check extends TeamStageEvent {
        private final boolean hasStageOriginal;
        private boolean hasStage;

        public Check(@NotNull Team team, @NotNull String stageName, boolean hasStage) {
            super(team, stageName);
            this.hasStageOriginal = hasStage;
            this.hasStage = hasStage;
        }

        public boolean hadStageOriginally() {
            return this.hasStageOriginal;
        }

        public boolean hasStage() {
            return this.hasStage;
        }

        public void setHasStage(boolean hasStage) {
            this.hasStage = hasStage;
        }

        public GameStageEvent.Check asGameStageEvent(Player player) {
            return new GameStageEvent.Check(player, getStageName(), hasStageOriginal);
        }
    }
}
