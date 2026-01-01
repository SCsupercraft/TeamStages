# Team Stages

Team Stages is an addon for Game Stages, allowing stages to be given to both players and teams.

Team Stages adds a new command, /teamstage, which can be used to give stages to and remove stages from the specified player or team.
The Game Stages API and the /gamestage command by default only affects the player's team but this can be changed in the config to affect just the player or both the player and the team.

## FTB Quests Integration

Team Stages integrates with FTB Quests adding one new task and reward.

### Stage Task

The stage task has 2 settings, stage, and effect.

- **Stage**: The stage you want to check for.
- **Effect**: Whose stages should be checked. Allowed options: player, team, or both. If set to both either the player _or_ the team has to have the stage.

### Stage Reward

The stage reward has 3 settings, stage, effect, and remove.

- **Stage**: The stage you want to grant or remove.
- **Effect**: Who will the stages be granted to or removed from. Allowed options: player, team, or both.
- **Remove**: Determines whether stages will be granted or removed, with true removing a stage.
