package dev.scsupercraft.teamstages.util;

public enum GameStageEffect {
	PLAYER,
	TEAM,
	BOTH;

	public boolean isPlayerEffect() {
		return this == PLAYER || this == BOTH;
	}
	public boolean isTeamEffect() {
		return this == TEAM || this == BOTH;
	}
}
