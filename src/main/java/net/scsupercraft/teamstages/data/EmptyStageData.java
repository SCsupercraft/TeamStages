package net.scsupercraft.teamstages.data;

import net.darkhax.gamestages.data.IStageData;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class EmptyStageData implements IStageData {
    @Override
    public Collection<String> getStages() {
        return List.of();
    }

    @Override
    public boolean hasStage(@NotNull String s) {
        return false;
    }

    @Override
    public void addStage(@NotNull String s) {

    }

    @Override
    public void removeStage(@NotNull String s) {

    }

    @Override
    public void clear() {

    }

    @Override
    public void readFromNBT(CompoundTag compoundTag) {

    }

    @Override
    public CompoundTag writeToNBT() {
        return new CompoundTag();
    }
}
