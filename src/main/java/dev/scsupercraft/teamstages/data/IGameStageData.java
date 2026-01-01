package dev.scsupercraft.teamstages.data;

import net.darkhax.gamestages.data.IStageData;
import dev.scsupercraft.teamstages.util.GameStageEffect;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * An extension of {@link IStageData} that replaces game stage's implementation.
 */
public interface IGameStageData extends IStageData {
    /**
     * Gets a collection of all unlocked stages matching the provided effect.
     *
     * @param effect the affected stages
     * @return a collection containing all of the unlocked stages.
     */
    Collection<String> getStages(@NotNull GameStageEffect effect);

    /**
     * Checks if a stage is unlocked matching the provided effect.
     * <p>
     * Both means <b>either</b> the player <b>or</b> the team needs the stage.
     *
     * @param stage The stage to check.
     * @param effect The affected stages.
     * @return Whether or not the stage has been unlocked.
     */
    boolean hasStage(@Nonnull String stage, @NotNull GameStageEffect effect);

    /**
     * Adds a stage to the unlocked stages collection matching the provided effect.
     *
     * @param stage The stage to unlock.
     * @param effect The affected stages.
     */
    void addStage(@Nonnull String stage, @NotNull GameStageEffect effect);

    /**
     * Removes a stage from the unlocked stages collection matching the provided effect.
     *
     * @param stage The stage to remove.
     * @param effect The affected stages.
     */
    void removeStage(@Nonnull String stage, @NotNull GameStageEffect effect);

    /**
     * Clears all of the unlocked stages matching the provided effect.
     *
     * @param effect The affected stages.
     */
    void clear(@NotNull GameStageEffect effect);
}
