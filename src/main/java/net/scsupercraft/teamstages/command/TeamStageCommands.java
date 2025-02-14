package net.scsupercraft.teamstages.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.data.TeamArgument;
import net.darkhax.gamestages.command.StageArgumentType;
import net.darkhax.gamestages.data.IStageData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.scsupercraft.teamstages.TeamStageHelper;
import net.scsupercraft.teamstages.data.PlayerStageData;
import net.scsupercraft.teamstages.data.TeamStageData;
import net.scsupercraft.teamstages.util.FtbUtil;

import java.util.Collection;
import java.util.stream.Collectors;

public class TeamStageCommands {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		for (CommandNode<CommandSourceStack> node : dispatcher.getRoot().getChildren()) {
			if (node.getName().equals("gamestage")) {
				// Register the alias command
				dispatcher.register(Commands.literal("gs").redirect(node));
				break;
			}
		}

		LiteralArgumentBuilder<CommandSourceStack> commandLiteral = Commands.literal("teamstage");

		commandLiteral.then(createCommands("player", false));
		commandLiteral.then(createCommands("team", true));

		LiteralCommandNode<CommandSourceStack> command = dispatcher.register(commandLiteral);

		dispatcher.register(Commands.literal("ts")
				.redirect(command));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> createCommand(String key, int permissions, boolean forTeam, Command<CommandSourceStack> command, Command<CommandSourceStack> commandNoPlayer) {
		RequiredArgumentBuilder<CommandSourceStack, ?> targetArg = forTeam ? Commands.argument("target", TeamArgument.create()) : Commands.argument("target", EntityArgument.player());

		return Commands.literal(key).requires((sender) -> sender.hasPermission(permissions))
				.executes(commandNoPlayer).then(
						targetArg.executes(command)
				);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> createSilentStageCommand(String key, int permissions, boolean forTeam, Command<CommandSourceStack> command, Command<CommandSourceStack> silent) {
		RequiredArgumentBuilder<CommandSourceStack, ?> targetArg = forTeam ? Commands.argument("target", TeamArgument.create()) : Commands.argument("target", EntityArgument.player());

		return Commands.literal(key).requires((sender) -> sender.hasPermission(permissions)).then(
				targetArg.then((
								Commands.argument("stage", new StageArgumentType()).executes(command)
						).then(
								Commands.argument("silent", BoolArgumentType.bool()).executes(silent)
						)
				)
		);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> createStageCommand(String key, int permissions, boolean forTeam, Command<CommandSourceStack> command, Command<CommandSourceStack> commandNoPlayer) {
		RequiredArgumentBuilder<CommandSourceStack, ?> targetArg = forTeam ? Commands.argument("target", TeamArgument.create()) : Commands.argument("target", EntityArgument.player());

		return Commands.literal(key).requires((sender) -> sender.hasPermission(permissions)).then(
				Commands.argument("stage", new StageArgumentType()).executes(commandNoPlayer)
		).then(
				targetArg.then(Commands.argument("stage", new StageArgumentType()).executes(command))
		);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> createCommands(String key, boolean forTeam) {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(key);

		command.then(createSilentStageCommand("add", 2, forTeam,
				(ctx) -> changeStages(ctx, true, false, forTeam),
				(ctx) -> changeStages(ctx, true, true, forTeam)
		));

		command.then(createCommand("all", 2, forTeam,
				(ctx) -> grantAll(ctx, true, forTeam),
				(ctx) -> grantAll(ctx, false, forTeam)
		));

		command.then(createStageCommand("check", 2, forTeam,
				(ctx) -> checkStage(ctx, true, forTeam),
				(ctx) -> checkStage(ctx, false, forTeam)
		));

		command.then(createCommand("clear", 2, forTeam,
				(ctx) -> clearStages(ctx, true, forTeam),
				(ctx) -> clearStages(ctx, false, forTeam)
		));

		command.then(createCommand("info", 0, forTeam,
				(ctx) -> getStageInfo(ctx, true, forTeam),
				(ctx) -> getStageInfo(ctx, false, forTeam)
		));

		command.then(createSilentStageCommand("remove", 2, forTeam,
				(ctx) -> changeStages(ctx, false, false, forTeam),
				(ctx) -> changeStages(ctx, false, true, forTeam)
		));

		return command;
	}

	public static int grantAll(CommandContext<CommandSourceStack> context, boolean hasTarget, boolean forTeam) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();

		if (forTeam) {
			Team team = hasTarget ? TeamArgument.get(context, "target") : FtbUtil.getTeam(source.getPlayerOrException());
			TeamStageData data = TeamStageHelper.getTeamData(team);

			if (data == null) return 1;
			TeamStageHelper.addTeamStage(team, TeamStageHelper.getKnownStages().toArray(new String[0]));

			for (ServerPlayer player : team.getOnlineMembers()) {
				player.displayClientMessage(Component.translatable("commands.gamestage.all.target"), false);
				if (player != source.getEntity()) {
					source.sendSuccess(() -> Component.translatable("commands.gamestage.all.sender", player.getDisplayName()), true);
				}
			}
		} else {
			ServerPlayer player = hasTarget ? EntityArgument.getPlayer(context, "target") : source.getPlayerOrException();
			PlayerStageData data = TeamStageHelper.getPlayerData(player);

			if (data == null) return 1;
			TeamStageHelper.addPlayerStage(player, TeamStageHelper.getKnownStages().toArray(new String[0]));

			player.displayClientMessage(Component.translatable("commands.gamestage.all.target"), false);
			if (player != source.getEntity()) {
				source.sendSuccess(() -> Component.translatable("commands.gamestage.all.sender", player.getDisplayName()), true);
			}
		}

		return 1;
	}

	public static int checkStage(CommandContext<CommandSourceStack> context, boolean hasTarget, boolean forTeam) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		String stage = StageArgumentType.getStage(context, "stage");

		if (forTeam) {
			Team team = hasTarget ? TeamArgument.get(context, "target") : FtbUtil.getTeam(source.getPlayerOrException());
			if (team == null) return 1;

			boolean hasStage = TeamStageHelper.teamHasStage(team, stage);

			source.sendSuccess(() -> Component.translatable(hasStage ? "commands.gamestage.check.success" : "commands.gamestage.check.failure", team.getShortName(), stage), false);
			return hasStage ? 1 : 0;
		} else {
			ServerPlayer player = hasTarget ? EntityArgument.getPlayer(context, "target") : source.getPlayerOrException();
			boolean hasStage = TeamStageHelper.playerHasStage(player, stage);

			source.sendSuccess(() -> Component.translatable(hasStage ? "commands.gamestage.check.success" : "commands.gamestage.check.failure", player.getDisplayName(), stage), false);
			return hasStage ? 1 : 0;
		}
	}

	public static int clearStages(CommandContext<CommandSourceStack> context, boolean hasTarget, boolean forTeam) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();

		if (forTeam) {
			Team team = hasTarget ? TeamArgument.get(context, "target") : FtbUtil.getTeam(source.getPlayerOrException());
			if (team == null) return 1;

			int removedStages = TeamStageHelper.clearTeamStages(team);

			for (ServerPlayer player : team.getOnlineMembers()) {
				player.displayClientMessage(Component.translatable("commands.gamestage.clear.target", removedStages), false);
				if (player != source.getEntity()) {
					source.sendSuccess(() -> Component.translatable("commands.gamestage.clear.sender", removedStages, player.getDisplayName()), true);
				}
			}
		} else {
			ServerPlayer player = hasTarget ? EntityArgument.getPlayer(context, "target") : source.getPlayerOrException();
			int removedStages = TeamStageHelper.clearPlayerStages(player);

			player.displayClientMessage(Component.translatable("commands.gamestage.clear.target", removedStages), false);
			if (player != source.getEntity()) {
				source.sendSuccess(() -> Component.translatable("commands.gamestage.clear.sender", removedStages, player.getDisplayName()), true);
			}
		}

		return 1;
	}

	public static int getStageInfo(CommandContext<CommandSourceStack> context, boolean hasTarget, boolean forTeam) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();

		IStageData data;
		String infoOwner;

		if (forTeam) {
			Team team = hasTarget ? TeamArgument.get(context, "target") : FtbUtil.getTeam(source.getPlayerOrException());
			data = TeamStageHelper.getTeamData(team);
			infoOwner = team != null ? team.getShortName() : "unknown";
		} else {
			ServerPlayer player = hasTarget ? EntityArgument.getPlayer(context, "target") : source.getPlayerOrException();
			data = TeamStageHelper.getPlayerData(player);
			infoOwner = player.getScoreboardName();
		}

		if (data == null) {
			source.sendSuccess(() -> Component.translatable("commands.gamestage.info.empty", infoOwner), false);
			return 1;
		}

		Collection<String> stages = data instanceof PlayerStageData playerStageData ? playerStageData.getPlayerStages() : data.getStages();
		String stageInfo = stages.stream().map(Object::toString).collect(Collectors.joining(", "));

		if (stageInfo.isEmpty()) {
			source.sendSuccess(() -> Component.translatable("commands.gamestage.info.empty", infoOwner), false);
		} else {
			source.sendSuccess(() -> Component.translatable("commands.gamestage.info.stages", infoOwner, stageInfo), false);
		}

		return 1;
	}

	private static int changeStages(CommandContext<CommandSourceStack> context, boolean adding, boolean hasSilentArg, boolean forTeam) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		String stage = StageArgumentType.getStage(context, "stage");
		boolean silent = hasSilentArg && BoolArgumentType.getBool(context, "silent");

		if (forTeam) {
			Team team = TeamArgument.get(context, "target");
			TeamStageData data = TeamStageHelper.getTeamData(team);

			if (data == null || adding == data.hasStage(stage)) return 1;
			if (adding) { data.addStage(stage); } else { data.removeStage(stage); }

			if (silent) return 1;

			for (ServerPlayer player : team.getOnlineMembers()) {
				player.displayClientMessage(Component.translatable(adding ? "commands.gamestage.add.target" : "commands.gamestage.remove.target", stage), false);
				if (player != source.getEntity()) {
					source.sendSuccess(() -> Component.translatable(adding ? "commands.gamestage.add.sender" : "commands.gamestage.remove.sender", stage, player.getDisplayName()), true);
				}
			}
		} else {
			ServerPlayer player = EntityArgument.getPlayer(context, "target");
			PlayerStageData data = TeamStageHelper.getPlayerData(player);

			if (data == null || adding == data.playerHasStage(stage)) return 1;
			if (adding) { data.addPlayerStage(stage); } else { data.removePlayerStage(stage); }

			if (silent) return 1;

			player.displayClientMessage(Component.translatable(adding ? "commands.gamestage.add.target" : "commands.gamestage.remove.target", stage), false);
			if (player != source.getEntity()) {
				source.sendSuccess(() -> Component.translatable(adding ? "commands.gamestage.add.sender" : "commands.gamestage.remove.sender", stage, player.getDisplayName()), true);
			}
		}

		return 1;
	}
}
