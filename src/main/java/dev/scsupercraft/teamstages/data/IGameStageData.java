package dev.scsupercraft.teamstages.data;

import net.darkhax.gamestages.data.IStageData;
import dev.scsupercraft.teamstages.util.GameStageEffect;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface IGameStageData extends IStageData {
    Collection<String> getStages(@NotNull GameStageEffect effect);

    boolean hasStage(@Nonnull String stage, @NotNull GameStageEffect effect);

    void addStage(@Nonnull String stage, @NotNull GameStageEffect effect);

    void removeStage(@Nonnull String stage, @NotNull GameStageEffect effect);

    void clear(@NotNull GameStageEffect effect);
}
