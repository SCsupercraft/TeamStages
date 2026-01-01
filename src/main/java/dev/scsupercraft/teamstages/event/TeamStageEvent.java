package dev.scsupercraft.teamstages.event;

import dev.ftb.mods.ftbteams.api.Team;
import dev.scsupercraft.teamstages.data.TeamStageHelper;
import net.darkhax.gamestages.data.IStageData;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

/**
 * This class holds all the various team stage events.
 * The main class itself should not be treated as an event.
 */
public class TeamStageEvent extends Event {
    private final String stageName;
    private final Team team;

    /**
     * The base constructor for all team stage events.
     *
     * @param team the team the event is for.
     * @param stageName the stage the event is for.
     */
    public TeamStageEvent(@NotNull Team team, @NotNull String stageName) {
        this.team = team;
        this.stageName = stageName;
    }

    /**
     * Gets the stage name for the event.
     *
     * @return the stage name for the event
     */
    public @NotNull String getStageName() {
        return this.stageName;
    }

    /**
     * Gets the team the event is for.
     *
     * @return the team
     */
    public @NotNull Team getTeam() {
        return team;
    }

    /**
     * This event is fired every time a stage is added to a team via
     * {@link TeamStageHelper.TeamHelper#addStage}.
     * Canceling this event will prevent the stage from being added.
     */
    @Cancelable
    public static class Add extends TeamStageEvent {
        /**
         * Creates a new add team stage event.
         *
         * @param team      the team the event is for
         * @param stageName the stage name for the event
         */
        public Add(@NotNull Team team, @NotNull String stageName) {
            super(team, stageName);
        }
    }

    /**
     * This event is fired after a stage has been successfully added using
     * {@link TeamStageHelper.TeamHelper#addStage}.
     * This can not be canceled.
     */
    public static class Added extends TeamStageEvent {
        /**
         * Creates a new added team stage event.
         *
         * @param team      the team the event is for
         * @param stageName the stage name for the event
         */
        public Added(@NotNull Team team, @NotNull String stageName) {
            super(team, stageName);
        }
    }

    /**
     * This event is fired when a stage is removed from a player via
     * {@link TeamStageHelper.TeamHelper#removeStage}.
     * Canceling this event will prevent it from being added.
     */
    @Cancelable
    public static class Remove extends TeamStageEvent {
        /**
         * Creates a new remove team stage event.
         *
         * @param team      the team the event is for
         * @param stageName the stage name for the event
         */
        public Remove(@NotNull Team team, @NotNull String stageName) {
            super(team, stageName);
        }
    }

    /**
     * This event is fired after a stage has been successfully removed using
     * {@link TeamStageHelper.TeamHelper#removeStage}.
     * This can not be canceled.
     */
    public static class Removed extends TeamStageEvent {
        /**
         * Creates a new removed team stage event.
         *
         * @param team      the team the event is for
         * @param stageName the stage name for the event
         */
        public Removed(@NotNull Team team, @NotNull String stageName) {
            super(team, stageName);
        }
    }

    /**
     * This event is fired after the stages have been cleared from a player.
     */
    public static class Cleared extends Event {
        private final IStageData stageData;
        private final Team team;

        /**
         * Creates a new cleared team stages event.
         *
         * @param team      the team the event is for
         * @param stageData the stage data that was cleared
         */
        public Cleared(@NotNull Team team, @NotNull IStageData stageData) {
            this.team = team;
            this.stageData = stageData;
        }

        /**
         * Gets the stage data that was cleared.
         *
         * @return the stage data that was cleared
         */
        public @NotNull IStageData getStageData() {
            return this.stageData;
        }

        /**
         * Gets the team the event is for.
         *
         * @return the team
         */
        public @NotNull Team getTeam() {
            return team;
        }
    }

    /**
     * This event is fired when a stage check is done on a player using
     * {@link TeamStageHelper.TeamHelper#hasStage}.
     */
    public static class Check extends TeamStageEvent {
        private final boolean hasStageOriginal;
        private boolean hasStage;

        /**
         * Creates a new check team stage event.
         *
         * @param team      the team the event is for
         * @param stageName the stage name for the event
         * @param hasStage  whether the team originally had this stage
         */
        public Check(@NotNull Team team, @NotNull String stageName, boolean hasStage) {
            super(team, stageName);
            this.hasStageOriginal = hasStage;
            this.hasStage = hasStage;
        }

        /**
         * Checks if the team originally had the stage.
         *
         * @return Whether or not the team originally had this stage.
         */
        public boolean hadStageOriginally() {
            return this.hasStageOriginal;
        }

        /**
         * Checks if the team has the stage according to the event.
         *
         * @return Whether or not the event says they have the stage.
         */
        public boolean hasStage() {
            return this.hasStage;
        }

        /**
         * Sets the result of the event.
         *
         * @param hasStage Whether or not the team should have this event.
         */
        public void setHasStage(boolean hasStage) {
            this.hasStage = hasStage;
        }
    }
}
