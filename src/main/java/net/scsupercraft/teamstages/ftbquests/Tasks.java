package net.scsupercraft.teamstages.ftbquests;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.resources.ResourceLocation;
import net.scsupercraft.teamstages.TeamStages;
import net.scsupercraft.teamstages.ftbquests.tasks.StageTask;

public class Tasks {
	static void register() {
		Stage = TaskTypes.register(new ResourceLocation(TeamStages.MOD_ID, "has_stage"), StageTask::new, () -> Icons.CONTROLLER);
	}

	public static TaskType Stage;
}
