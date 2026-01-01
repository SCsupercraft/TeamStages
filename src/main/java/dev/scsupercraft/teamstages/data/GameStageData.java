package dev.scsupercraft.teamstages.data;

import net.darkhax.gamestages.data.IStageData;
import net.darkhax.gamestages.data.StageData;
import net.minecraft.nbt.CompoundTag;
import dev.scsupercraft.teamstages.config.CommonConfig;
import dev.scsupercraft.teamstages.util.GameStageEffect;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class GameStageData implements IGameStageData {
    final IStageData playerStageData = new StageData();
    @NotNull IStageData teamStageData = new EmptyStageData();

    @Override
    public Collection<String> getStages() {
        return getStages(CommonConfig.listEffect);
    }

    @Override
    public Collection<String> getStages(@NotNull GameStageEffect effect) {
        Set<String> list = effect.isPlayerEffect()
                ? new HashSet<>(playerStageData.getStages())
                : new HashSet<>();
        if (effect.isTeamEffect()) list.addAll(teamStageData.getStages());

        return Collections.unmodifiableCollection(list);
    }

    @Override
    public boolean hasStage(@NotNull String stage) {
        return hasStage(stage, CommonConfig.checkEffect);
    }

    @Override
    public boolean hasStage(@NotNull String stage, @NotNull GameStageEffect effect) {
        return (effect.isPlayerEffect() && playerStageData.hasStage(stage))
                || (effect.isTeamEffect() && teamStageData.hasStage(stage));
    }

    @Override
    public void addStage(@NotNull String stage) {
        addStage(stage, CommonConfig.changeEffect);
    }

    @Override
    public void addStage(@NotNull String stage, @NotNull GameStageEffect effect) {
        if (effect.isPlayerEffect()) playerStageData.addStage(stage);
        if (effect.isTeamEffect()) teamStageData.addStage(stage);
    }

    @Override
    public void removeStage(@NotNull String stage) {
        removeStage(stage, CommonConfig.changeEffect);
    }

    @Override
    public void removeStage(@NotNull String stage, @NotNull GameStageEffect effect) {
        if (effect.isPlayerEffect()) playerStageData.removeStage(stage);
        if (effect.isTeamEffect()) teamStageData.removeStage(stage);
    }

    @Override
    public void clear() {
        clear(CommonConfig.changeEffect);
    }

    @Override
    public void clear(@NotNull GameStageEffect effect) {
        if (effect.isPlayerEffect()) playerStageData.clear();
        if (effect.isTeamEffect()) teamStageData.clear();
    }

    @Override
    public void readFromNBT(CompoundTag compoundTag) {
        playerStageData.readFromNBT(compoundTag);
    }

    @Override
    public CompoundTag writeToNBT() {
        return playerStageData.writeToNBT();
    }
}
