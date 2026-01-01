package dev.scsupercraft.teamstages.ftbquests;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import net.minecraft.resources.ResourceLocation;
import dev.scsupercraft.teamstages.TeamStages;
import dev.scsupercraft.teamstages.ftbquests.rewards.StageReward;

public class Rewards {
	static void register() {
        Stage = RewardTypes.register(new ResourceLocation(TeamStages.MOD_ID, "change_stage"), StageReward::new, () -> Icons.CONTROLLER);
    }

	public static RewardType Stage;
}
