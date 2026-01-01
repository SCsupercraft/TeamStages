package dev.scsupercraft.teamstages.data;

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
import dev.scsupercraft.teamstages.TeamStages;
import dev.scsupercraft.teamstages.config.CommonConfig;
import dev.scsupercraft.teamstages.event.TeamStageEvent;
import dev.scsupercraft.teamstages.packet.MessageTeamStages;
import dev.scsupercraft.teamstages.packet.TeamStagesPacketHandler;
import dev.scsupercraft.teamstages.util.FtbUtil;
import dev.scsupercraft.teamstages.util.GameStageEffect;

import javax.annotation.Nullable;
import java.util.*;

/**
 * A helper class for managing stages.
 */
public class TeamStageHelper {
    private static final PlayerHelper PLAYER = new PlayerHelper();
    private static final TeamHelper TEAM = new TeamHelper();

    /**
     * Checks if a string is valid as a stage name.
     *
     * @param stageName The potential name.
     * @return Whether the name is valid as a stage name.
     */
    public static boolean isValidStageName(String stageName) {
        return GameStageHelper.isValidStageName(stageName);
    }

    /**
     * Gets an immutable set of all the stages defined in the known stages json file.
     *
     * @return An immutable set of all known stages.
     */
    public static Set<String> getKnownStages() {
        return GameStageSaveHandler.getKnownStages();
    }

    /**
     * Checks if a stage has been defined in the known stages file.
     *
     * @param stage The stage name to search for.
     * @return Whether the stage name exists in the known stages.
     */
    public static boolean isStageKnown(String stage) {
        return GameStageSaveHandler.isStageKnown(stage);
    }

    /**
     * {@return the player stage helper}
     */
    public static PlayerHelper player() {
        return PLAYER;
    }

    /**
     * {@return the team stage helper}
     */
    public static TeamHelper team() {
        return TEAM;
    }

    private TeamStageHelper() {}

    /**
     * Goes through the team's online members and syncs their stage data from the server to the client.
     *
     * @param team The team to sync.
     * @see #syncPlayer(ServerPlayer)
     */
    public static void syncTeam(Team team) {
        team.getOnlineMembers().forEach(TeamStageHelper::syncPlayer);
    }

    /**
     * Syncs a player's stage data from the server to the client.
     *
     * @param player The player to sync.
     * @see #syncTeam(Team)
     */
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

    /**
     * A helper class for managing player stages.
     * <p>
     * Get an instance of this class using {@link TeamStageHelper#player()}.
     */
    public static class PlayerHelper {
        private PlayerHelper() {

        }

        /**
         * Checks if a player has a stage.
         *
         * @param player The player to check.
         * @param stage The stage to look for.
         * @return Whether or not they have the stage.
         */
        public boolean hasStage(Player player, String stage) {
            return hasStage(player, getPlayerData(player), stage);
        }

        /**
         * Checks if a player has a stage.
         *
         * @param player The player to check.
         * @param data The player's stage data.
         * @param stage The stage to look for.
         * @return Whether or not they have the stage.
         */
        public boolean hasStage(Player player, @Nullable IStageData data, String stage) {
            if (data != null) {
                GameStageEvent.Check event = new GameStageEvent.Check(player, stage, data.hasStage(stage));
                MinecraftForge.EVENT_BUS.post(event);
                return event.hasStage();
            } else {
                return false;
            }
        }

        /**
         * Checks if the player has at least one of many possible stages.
         *
         * @param player The player to check.
         * @param stages The stages to look for.
         * @return Whether or not the player had at least one of the stages.
         */
        public boolean hasAnyOf(Player player, String... stages) {
            return hasAnyOf(player, getPlayerData(player), stages);
        }

        /**
         * Checks if the player has at least one of many possible stages.
         *
         * @param player The player to check.
         * @param stages The stages to look for.
         * @return Whether or not the player had at least one of the stages.
         */
        public boolean hasAnyOf(Player player, Collection<String> stages) {
            return hasAnyOf(player, getPlayerData(player), stages);
        }

        /**
         * Checks if the player has at least one of many possible stages.
         *
         * @param player The player to check.
         * @param data The player's stage data.
         * @param stages The stages to look for.
         * @return Whether or not the player had at least one of the stages.
         */
        public boolean hasAnyOf(Player player, @Nullable IStageData data, Collection<String> stages) {
            return stages.stream().anyMatch((stage) -> hasStage(player, data, stage));
        }

        /**
         * Checks if the player has at least one of many possible stages.
         *
         * @param player The player to check.
         * @param data The player's stage data.
         * @param stages The stages to look for.
         * @return Whether or not the player had at least one of the stages.
         */
        public boolean hasAnyOf(Player player, @Nullable IStageData data, String... stages) {
            return Arrays.stream(stages).anyMatch((stage) -> hasStage(player, data, stage));
        }

        /**
         * Checks if the player has all of the stages.
         *
         * @param player The player to check.
         * @param stages The stages to look for.
         * @return Whether or not the player had all the stages.
         */
        public boolean hasAllOf(Player player, String... stages) {
            return hasAllOf(player, getPlayerData(player), stages);
        }

        /**
         * Checks if the player has all of the stages.
         *
         * @param player The player to check.
         * @param stages The stages to look for.
         * @return Whether or not the player had all the stages.
         */
        public boolean hasAllOf(Player player, Collection<String> stages) {
            return hasAllOf(player, getPlayerData(player), stages);
        }

        /**
         * Checks if the player has all of the stages.
         *
         * @param player The player to check.
         * @param data The player's stage data.
         * @param stages The stages to look for.
         * @return Whether or not the player had all the stages.
         */
        public boolean hasAllOf(Player player, @Nullable IStageData data, Collection<String> stages) {
            return stages.stream().allMatch((stage) -> hasStage(player, data, stage));
        }

        /**
         * Checks if the player has all of the stages.
         *
         * @param player The player to check.
         * @param data The player's stage data.
         * @param stages The stages to look for.
         * @return Whether or not the player had all the stages.
         */
        public boolean hasAllOf(Player player, @Nullable IStageData data, String... stages) {
            return Arrays.stream(stages).allMatch((stage) -> hasStage(player, data, stage));
        }

        /**
         * Attempts to give a player a stage. Events may cancel this.
         *
         * @param player The player to give the stage.
         * @param stages The stage to give.
         */
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

        /**
         * Attempts to remove a stage from a player. Events may cancel this.
         *
         * @param player The player to remove the stage from.
         * @param stages The stage to remove.
         */
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

        /**
         * Removes all stages from a player.
         *
         * @param player The player to clear the stages of.
         * @return The amount of stages that were removed.
         */
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

        /**
         * Attempts to resolve the player stage data for a player. If it is a real server player it will
         * look up their data using UUID. If it's a FakePlayer it will check the fake player data
         * file. If it's a client player it will use the client's synced data cache.
         *
         * @param player The player to resolve.
         * @return The stage data that was found. Will be null if nothing could be found.
         */
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

    /**
     * A helper class for managing team stages.
     * <p>
     * Get an instance of this class using {@link TeamStageHelper#team()}.
     */
    public static class TeamHelper {
        private TeamHelper() {

        }

        /**
         * Checks if a player's team has a stage.
         *
         * @param player The player to check.
         * @param stage The stage to look for.
         * @return Whether or not they have the stage.
         */
        public boolean hasStage(Player player, String stage) {
            return hasStage(player, getTeamData(player), stage);
        }

        /**
         * Checks if a player's team has a stage.
         *
         * @param player The player to check.
         * @param data The player's stage data.
         * @param stage The stage to look for.
         * @return Whether or not they have the stage.
         */
        public boolean hasStage(Player player, @Nullable IStageData data, String stage) {
            return hasStage(FtbUtil.getTeam(player), data, stage);
        }

        /**
         * Checks if the player's team has at least one of many possible stages.
         *
         * @param player The player to check.
         * @param stages The stages to look for.
         * @return Whether or not the player had at least one of the stages.
         */
        public boolean hasAnyOf(Player player, String... stages) {
            return hasAnyOf(player, getTeamData(player), stages);
        }

        /**
         * Checks if the player's team has at least one of many possible stages.
         *
         * @param player The player to check.
         * @param stages The stages to look for.
         * @return Whether or not the player had at least one of the stages.
         */
        public boolean hasAnyOf(Player player, Collection<String> stages) {
            return hasAnyOf(player, getTeamData(player), stages);
        }

        /**
         * Checks if the player's team has at least one of many possible stages.
         *
         * @param player The player to check.
         * @param data The player's stage data.
         * @param stages The stages to look for.
         * @return Whether or not the player had at least one of the stages.
         */
        public boolean hasAnyOf(Player player, @Nullable IStageData data, Collection<String> stages) {
            return stages.stream().anyMatch((stage) -> hasStage(player, data, stage));
        }

        /**
         * Checks if the player's team has at least one of many possible stages.
         *
         * @param player The player to check.
         * @param data The player's stage data.
         * @param stages The stages to look for.
         * @return Whether or not the player had at least one of the stages.
         */
        public boolean hasAnyOf(Player player, @Nullable IStageData data, String... stages) {
            return Arrays.stream(stages).anyMatch((stage) -> hasStage(player, data, stage));
        }

        /**
         * Checks if the player's team has all of the stages.
         *
         * @param player The player to check.
         * @param stages The stages to look for.
         * @return Whether or not the player had all the stages.
         */
        public boolean hasAllOf(Player player, String... stages) {
            return hasAllOf(player, getTeamData(player), stages);
        }

        /**
         * Checks if the player's team has all of the stages.
         *
         * @param player The player to check.
         * @param stages The stages to look for.
         * @return Whether or not the player had all the stages.
         */
        public boolean hasAllOf(Player player, Collection<String> stages) {
            return hasAllOf(player, getTeamData(player), stages);
        }

        /**
         * Checks if the player's team has all of the stages.
         *
         * @param player The player to check.
         * @param data The player's stage data.
         * @param stages The stages to look for.
         * @return Whether or not the player had all the stages.
         */
        public boolean hasAllOf(Player player, @Nullable IStageData data, Collection<String> stages) {
            return stages.stream().allMatch((stage) -> hasStage(player, data, stage));
        }

        /**
         * Checks if the player's team has all of the stages.
         *
         * @param player The player to check.
         * @param data The player's stage data.
         * @param stages The stages to look for.
         * @return Whether or not the player had all the stages.
         */
        public boolean hasAllOf(Player player, @Nullable IStageData data, String... stages) {
            return Arrays.stream(stages).allMatch((stage) -> hasStage(player, data, stage));
        }

        /**
         * Attempts to give a player's team a stage. Events may cancel this.
         *
         * @param player The player to give the stage.
         * @param stages The stage to give.
         */
        public void addStage(ServerPlayer player, String... stages) {
            addStage(FtbUtil.getTeam(player), stages);
        }

        /**
         * Attempts to remove a stage from a player's team. Events may cancel this.
         *
         * @param player The player to remove the stage from.
         * @param stages The stage to remove.
         */
        public void removeStage(ServerPlayer player, String... stages) {
            removeStage(FtbUtil.getTeam(player), stages);
        }

        /**
         * Removes all stages from a player's team.
         *
         * @param player The player to clear the stages of.
         * @return The amount of stages that were removed.
         */
        public int clearStages(ServerPlayer player) {
            return clearStages(FtbUtil.getTeam(player));
        }

        /**
         * Checks if a team has a stage.
         *
         * @param team The team to check.
         * @param stage The stage to look for.
         * @return Whether or not they have the stage.
         */
        public boolean hasStage(Team team, String stage) {
            return hasStage(team, getTeamData(team), stage);
        }

        /**
         * Checks if a team has a stage.
         *
         * @param team The team to check.
         * @param data The team's stage data.
         * @param stage The stage to look for.
         * @return Whether or not they have the stage.
         */
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

        /**
         * Checks if the team has at least one of many possible stages.
         *
         * @param team The team to check.
         * @param stages The stages to look for.
         * @return Whether or not the team had at least one of the stages.
         */
        public boolean hasAnyOf(Team team, String... stages) {
            return hasAnyOf(team, getTeamData(team), stages);
        }

        /**
         * Checks if the team has at least one of many possible stages.
         *
         * @param team The team to check.
         * @param stages The stages to look for.
         * @return Whether or not the team had at least one of the stages.
         */
        public boolean hasAnyOf(Team team, Collection<String> stages) {
            return hasAnyOf(team, getTeamData(team), stages);
        }

        /**
         * Checks if the team has at least one of many possible stages.
         *
         * @param team The team to check.
         * @param data The team's stage data.
         * @param stages The stages to look for.
         * @return Whether or not the team had at least one of the stages.
         */
        public boolean hasAnyOf(Team team, @Nullable IStageData data, Collection<String> stages) {
            return stages.stream().anyMatch((stage) -> hasStage(team, data, stage));
        }

        /**
         * Checks if the team has at least one of many possible stages.
         *
         * @param team The team to check.
         * @param data The team's stage data.
         * @param stages The stages to look for.
         * @return Whether or not the team had at least one of the stages.
         */
        public boolean hasAnyOf(Team team, @Nullable IStageData data, String... stages) {
            return Arrays.stream(stages).anyMatch((stage) -> hasStage(team, data, stage));
        }

        /**
         * Checks if the team has all of the stages.
         *
         * @param team The team to check.
         * @param stages The stages to look for.
         * @return Whether or not the team had all the stages.
         */
        public boolean hasAllOf(Team team, String... stages) {
            return hasAllOf(team, getTeamData(team), stages);
        }

        /**
         * Checks if the team has all of the stages.
         *
         * @param team The team to check.
         * @param stages The stages to look for.
         * @return Whether or not the team had all the stages.
         */
        public boolean hasAllOf(Team team, Collection<String> stages) {
            return hasAllOf(team, getTeamData(team), stages);
        }

        /**
         * Checks if the team has all of the stages.
         *
         * @param team The team to check.
         * @param data The team's stage data.
         * @param stages The stages to look for.
         * @return Whether or not the team had all the stages.
         */
        public boolean hasAllOf(Team team, @Nullable IStageData data, Collection<String> stages) {
            return stages.stream().allMatch((stage) -> hasStage(team, data, stage));
        }

        /**
         * Checks if the team has all of the stages.
         *
         * @param team The team to check.
         * @param data The team's stage data.
         * @param stages The stages to look for.
         * @return Whether or not the team had all the stages.
         */
        public boolean hasAllOf(Team team, @Nullable IStageData data, String... stages) {
            return Arrays.stream(stages).allMatch((stage) -> hasStage(team, data, stage));
        }

        /**
         * Attempts to give a team a stage. Events may cancel this.
         *
         * @param team The team to give the stage.
         * @param stages The stage to give.
         */
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

        /**
         * Attempts to remove a stage from a team. Events may cancel this.
         *
         * @param team The team to remove the stage from.
         * @param stages The stage to remove.
         */
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

        /**
         * Removes all stages from a team.
         *
         * @param team The team to clear the stages of.
         * @return The amount of stages that were removed.
         */
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

        /**
         * Attempts to resolve the team stage data for a player. If it is a real server player it will
         * look up their data using UUID. If it's a FakePlayer it will check the fake player data
         * file. If it's a client player it will use the client's synced data cache.
         *
         * @param player The player to resolve.
         * @return The stage data that was found. Will be null if nothing could be found.
         */
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

        /**
         * Attempts to resolve the stage data for a team. If called on the client-side
         * it will use the client's synced data cache. Otherwise, it will look up
         * the team's data using UUID.
         *
         * @param team The team to resolve.
         * @return The stage data that was found. Will be null if nothing could be found.
         */
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
