package net.scsupercraft.teamstages.ftbquests;

public class FtbQuestsIntegration {
	public static boolean enabled = false;
	static {
		try {
			Class.forName("dev.ftb.mods.ftbquests.quest.task.TaskType");
			enabled = true;
		} catch (ClassNotFoundException e) {
			enabled = false;
		}
	}

	public static void init() {
		if (!enabled) return;

		Tasks.register();
		Rewards.register();
	}
}

