package net.scsupercraft.teamstages.ftbquests;

public class FtbQuestsIntegration {
	public static final boolean ENABLED;

	static {
        boolean installed = true;
		try {
			Class.forName("dev.ftb.mods.ftbquests.quest.task.TaskType");
		} catch (ClassNotFoundException e) {
			installed = false;
		}
        ENABLED = installed;
	}

	public static void init() {
		if (!ENABLED) return;

		Tasks.register();
		Rewards.register();
	}
}
