package dev.scsupercraft.teamstages.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import dev.scsupercraft.teamstages.TeamStages;
import dev.scsupercraft.teamstages.util.GameStageEffect;

@Mod.EventBusSubscriber(modid = TeamStages.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	private static final ForgeConfigSpec.EnumValue<GameStageEffect> CHANGE_EFFECT = BUILDER
			.comment("Changes who stages are granted to when using the gamestages api")
			.comment("Changes who stages are removed from when using the gamestages api")
			.defineEnum("change", GameStageEffect.TEAM);
	private static final ForgeConfigSpec.EnumValue<GameStageEffect> CHECK_EFFECT = BUILDER
			.comment("Changes who's stages are checked when using the gamestages api")
			.defineEnum("check", GameStageEffect.BOTH);
	private static final ForgeConfigSpec.EnumValue<GameStageEffect> LIST_EFFECT = BUILDER
			.comment("Changes who's stages are listed by the gamestages api")
			.defineEnum("list", GameStageEffect.BOTH);

	public static final ForgeConfigSpec SPEC = BUILDER.build();

	public static GameStageEffect changeEffect;
	public static GameStageEffect checkEffect;
	public static GameStageEffect listEffect;

	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
		changeEffect = CHANGE_EFFECT.get();
		checkEffect = CHECK_EFFECT.get();
		listEffect = LIST_EFFECT.get();
	}
}
