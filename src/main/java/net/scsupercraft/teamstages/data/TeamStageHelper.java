package net.scsupercraft.teamstages.data;

import dev.ftb.mods.ftbteams.api.Team;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.advancement.HasStageTrigger;
import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.darkhax.gamestages.data.IStageData;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.scsupercraft.teamstages.TeamStages;
import net.scsupercraft.teamstages.config.CommonConfig;
import net.scsupercraft.teamstages.event.TeamStageEvent;
import net.scsupercraft.teamstages.packet.MessageTeamStages;
import net.scsupercraft.teamstages.packet.TeamStagesPacketHandler;
import net.scsupercraft.teamstages.util.FtbUtil;
import net.scsupercraft.teamstages.util.GameStageEffect;

import javax.annotation.Nullable;
import java.util.*;

public class TeamStageHelper {
    private static final PlayerHelper PLAYER = new PlayerHelper();
    private static final TeamHelper TEAM = new TeamHelper();

    public static boolean isValidStageName(String stageName) {
        return GameStageHelper.isValidStageName(stageName);
    }

    public static Set<String> getKnownStages() {
        return GameStageSaveHandler.getKnownStages();
    }

    public static boolean isStageKnown(String stage) {
        return GameStageSaveHandler.isStageKnown(stage);
    }

    public static PlayerHelper player() {
        return PLAYER;
    }

    public static TeamHelper team() {
        return TEAM;
    }

    private TeamStageHelper() {}

    public static void syncTeam(Team team) {
        team.getOnlineMembers().forEach(TeamStageHelper::syncPlayer);
    }

    public static void syncPlayer(ServerPlayer player) {
        GameStageData gameData = (GameStageData) GameStageHelper.getPlayerData(player);
        if (gameData == null) return;

        IStageData playerData = gameData.playerStageData;
        IStageData teamData = gameData.teamStageData;

        TeamStages.LOGGER.debug("Syncing {} player stages and {} team stages for {}.",
                playerData.getStages().size(), teamData.getStages().size(), player.getName());

        TeamStagesPacketHandler.sendToClient(player, new MessageTeamStages(
                playerData.getStages(), teamData.getStages()
        ));

        GameStageEffect effect = CommonConfig.checkEffect;

        List<String> list = effect.isPlayerEffect()
                ? new ArrayList<>(playerData.getStages())
                : new ArrayList<>();
        if (effect.isTeamEffect()) list.addAll(teamData.getStages());

        for (String stage : list) {
            HasStageTrigger.INSTANCE.trigger(player, stage);
        }
    }

    public static class PlayerHelper {
        private PlayerHelper() {

        }

        public boolean hasStage(Player player, String stage) {
            return hasStage(player, getPlayerData(player), stage);
        }

        public boolean hasStage(Player player, @Nullable IStageData data, String stage) {
            if (data != null) {
                GameStageEvent.Check event = new GameStageEvent.Check(player, stage, data.hasStage(stage));
                MinecraftForge.EVENT_BUS.post(event);
                return event.hasStage();
            } else {
                return false;
            }
        }

        public boolean hasAnyOf(Player player, String... stages) {
            return hasAnyOf(player, getPlayerData(player), stages);
        }

        public boolean hasAnyOf(Player player, Collection<String> stages) {
            return hasAnyOf(player, getPlayerData(player), stages);
        }

        public boolean hasAnyOf(Player player, @Nullable IStageData data, Collection<String> stages) {
            return stages.stream().anyMatch((stage) -> hasStage(player, data, stage));
        }

        public boolean hasAnyOf(Player player, @Nullable IStageData data, String... stages) {
            return Arrays.stream(stages).anyMatch((stage) -> hasStage(player, data, stage));
        }

        public boolean hasAllOf(Player player, String... stages) {
            return hasAllOf(player, getPlayerData(player), stages);
        }

        public boolean hasAllOf(Player player, Collection<String> stages) {
            return hasAllOf(player, getPlayerData(player), stages);
        }

        public boolean hasAllOf(Player player, @Nullable IStageData data, Collection<String> stages) {
            return stages.stream().allMatch((stage) -> hasStage(player, data, stage));
        }

        public boolean hasAllOf(Player player, @Nullable IStageData data, String... stages) {
            return Arrays.stream(stages).allMatch((stage) -> hasStage(player, data, stage));
        }

        public void addStage(ServerPlayer player, String... stages) {
            for(String stage : stages) {
                if (!MinecraftForge.EVENT_BUS.post(new GameStageEvent.Add(player, stage))) {
                    IStageData data = getPlayerData(player);
                    if (data != null) {
                        data.addStage(stage);
                        syncPlayer(player);
                        MinecraftForge.EVENT_BUS.post(new GameStageEvent.Added(player, stage));
                    }
                }
            }
        }

        public void removeStage(ServerPlayer player, String... stages) {
            for(String stage : stages) {
                if (!MinecraftForge.EVENT_BUS.post(new GameStageEvent.Remove(player, stage))) {
                    IStageData data = getPlayerData(player);
                    if (data != null) {
                        data.removeStage(stage);
                        syncPlayer(player);
                        MinecraftForge.EVENT_BUS.post(new GameStageEvent.Removed(player, stage));
                    }
                }
            }
        }

        public int clearStages(ServerPlayer player) {
            IStageData stageInfo = getPlayerData(player);
            if (stageInfo != null) {
                int stageCount = stageInfo.getStages().size();
                stageInfo.clear();
                syncPlayer(player);
                MinecraftForge.EVENT_BUS.post(new GameStageEvent.Cleared(player, stageInfo));
                return stageCount;
            } else {
                return 0;
            }
        }

        @Nullable
        public IStageData getPlayerData(Player player) {
            if (player != null) {
                if (player instanceof ServerPlayer) {
                    if (player instanceof FakePlayer) {
                        return GameStageSaveHandler.getFakeData(player.getName().getString());
                    }

                    return TeamStageSaveHandler.getPlayerData(player.getUUID());
                }

                if (EffectiveSide.get().isClient()) {
                    return TeamStageSaveHandler.getClientPlayerData();
                }
            }

            return null;
        }
    }

    public static class TeamHelper {
        private TeamHelper() {

        }

        public boolean hasStage(Player player, String stage) {
            return hasStage(player, getTeamData(player), stage);
        }

        public boolean hasStage(Player player, @Nullable IStageData data, String stage) {
            return hasStage(FtbUtil.getTeam(player), data, stage);
        }

        public boolean hasAnyOf(Player player, String... stages) {
            return hasAnyOf(player, getTeamData(player), stages);
        }

        public boolean hasAnyOf(Player player, Collection<String> stages) {
            return hasAnyOf(player, getTeamData(player), stages);
        }

        public boolean hasAnyOf(Player player, @Nullable IStageData data, Collection<String> stages) {
            return stages.stream().anyMatch((stage) -> hasStage(player, data, stage));
        }

        public boolean hasAnyOf(Player player, @Nullable IStageData data, String... stages) {
            return Arrays.stream(stages).anyMatch((stage) -> hasStage(player, data, stage));
        }

        public boolean hasAllOf(Player player, String... stages) {
            return hasAllOf(player, getTeamData(player), stages);
        }

        public boolean hasAllOf(Player player, Collection<String> stages) {
            return hasAllOf(player, getTeamData(player), stages);
        }

        public boolean hasAllOf(Player player, @Nullable IStageData data, Collection<String> stages) {
            return stages.stream().allMatch((stage) -> hasStage(player, data, stage));
        }

        public boolean hasAllOf(Player player, @Nullable IStageData data, String... stages) {
            return Arrays.stream(stages).allMatch((stage) -> hasStage(player, data, stage));
        }

        public void addStage(ServerPlayer player, String... stages) {
            addStage(FtbUtil.getTeam(player), stages);
        }

        public void removeStage(ServerPlayer player, String... stages) {
            removeStage(FtbUtil.getTeam(player), stages);
        }

        public int clearStages(ServerPlayer player) {
            return clearStages(FtbUtil.getTeam(player));
        }

        public boolean hasStage(Team team, String stage) {
            return hasStage(team, getTeamData(team), stage);
        }

        public boolean hasStage(Team team, @Nullable IStageData data, String stage) {
            if (data != null) {
                boolean hasStage = data.hasStage(stage);

                TeamStageEvent.Check event = new TeamStageEvent.Check(team, stage, hasStage);
                MinecraftForge.EVENT_BUS.post(event);
                hasStage = event.hasStage();

                for (ServerPlayer member : team.getOnlineMembers()) {
                    GameStageEvent.Check gameStageEvent = new GameStageEvent.Check(member, stage, hasStage);
                    MinecraftForge.EVENT_BUS.post(gameStageEvent);
                    hasStage = event.hasStage();
                }

                return hasStage;
            } else {
                return false;
            }
        }

        public boolean hasAnyOf(Team team, String... stages) {
            return hasAnyOf(team, getTeamData(team), stages);
        }

        public boolean hasAnyOf(Team team, Collection<String> stages) {
            return hasAnyOf(team, getTeamData(team), stages);
        }

        public boolean hasAnyOf(Team team, @Nullable IStageData data, Collection<String> stages) {
            return stages.stream().anyMatch((stage) -> hasStage(team, data, stage));
        }

        public boolean hasAnyOf(Team team, @Nullable IStageData data, String... stages) {
            return Arrays.stream(stages).anyMatch((stage) -> hasStage(team, data, stage));
        }

        public boolean hasAllOf(Team team, String... stages) {
            return hasAllOf(team, getTeamData(team), stages);
        }

        public boolean hasAllOf(Team team, Collection<String> stages) {
            return hasAllOf(team, getTeamData(team), stages);
        }

        public boolean hasAllOf(Team team, @Nullable IStageData data, Collection<String> stages) {
            return stages.stream().allMatch((stage) -> hasStage(team, data, stage));
        }

        public boolean hasAllOf(Team team, @Nullable IStageData data, String... stages) {
            return Arrays.stream(stages).allMatch((stage) -> hasStage(team, data, stage));
        }

        public void addStage(Team team, String... stages) {
            IStageData data = getTeamData(team);
            if (data != null) {
                for (String stage: stages) {
                    if (!MinecraftForge.EVENT_BUS.post(new TeamStageEvent.Add(team, stage))) {
                        boolean cancelled = false;
                        for (ServerPlayer member: team.getOnlineMembers()) {
                            if (MinecraftForge.EVENT_BUS.post(new GameStageEvent.Add(member, stage))) {
                                cancelled = true;
                                break;
                            }
                        }
                        if (cancelled) continue;

                        data.addStage(stage);
                        syncTeam(team);
                        MinecraftForge.EVENT_BUS.post(new TeamStageEvent.Added(team, stage));
                        for (ServerPlayer member: team.getOnlineMembers()) {
                            MinecraftForge.EVENT_BUS.post(new GameStageEvent.Added(member, stage));
                        }
                    }
                }
            }
        }

        public void removeStage(Team team, String... stages) {
            IStageData data = getTeamData(team);
            if (data != null) {
                for (String stage: stages) {
                    if (!MinecraftForge.EVENT_BUS.post(new TeamStageEvent.Remove(team, stage))) {
                        boolean cancelled = false;
                        for (ServerPlayer member: team.getOnlineMembers()) {
                            if (MinecraftForge.EVENT_BUS.post(new GameStageEvent.Remove(member, stage))) {
                                cancelled = true;
                                break;
                            }
                        }
                        if (cancelled) continue;

                        data.removeStage(stage);
                        syncTeam(team);
                        MinecraftForge.EVENT_BUS.post(new TeamStageEvent.Removed(team, stage));
                        for (ServerPlayer member: team.getOnlineMembers()) {
                            MinecraftForge.EVENT_BUS.post(new GameStageEvent.Removed(member, stage));
                        }
                    }
                }
            }
        }

        public int clearStages(Team team) {
            IStageData stageInfo = getTeamData(team);
            if (stageInfo != null) {
                int stageCount = stageInfo.getStages().size();
                stageInfo.clear();
                syncTeam(team);
                MinecraftForge.EVENT_BUS.post(new TeamStageEvent.Cleared(team, stageInfo));
                for (ServerPlayer member : team.getOnlineMembers()) {
                    MinecraftForge.EVENT_BUS.post(new GameStageEvent.Cleared(member, stageInfo));
                }
                return stageCount;
            } else {
                return 0;
            }
        }

        @Nullable
        public IStageData getTeamData(Player player) {
            if (player != null) {
                if (player instanceof ServerPlayer) {
                    if (player instanceof FakePlayer) {
                        return new EmptyStageData();
                    }

                    return TeamStageSaveHandler.getTeamDataForPlayer(player.getUUID());
                }

                if (EffectiveSide.get().isClient()) {
                    return TeamStageSaveHandler.getClientTeamData();
                }
            }

            return null;
        }

        @Nullable
        public IStageData getTeamData(Team team) {
            if (team != null) {
                if (EffectiveSide.get().isClient()) {
                    return TeamStageSaveHandler.getClientTeamData();
                }

                return TeamStageSaveHandler.getTeamData(team.getTeamId());
            }

            return null;
        }
    }
}
