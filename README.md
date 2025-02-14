# Team Stages

Team Stages is an addon for Game Stages, allowing stages to be given to both players and teams.

Team Stages adds a new command, /teamstage, which can be used to give stages to and remove stages from the specified player or team.
The Game Stages API and the /gamestage command by default only effect the player but this can be changed in the config to effect a team or both the player and the team.

## FTB Quests Integration

Team Stages integrates with FTB Quests adding one new task and reward.

### Stage Task

The stage task has 2 settings, stage, and effect.

- Stage is the stage you want to check for.
- Effect is one of three options and changes whose stages are being checked. Allowed options: player, team, or both. If set to both either the player or the team has to have the stage.

### Stage Reward

The stage reward has 3 settings, stage, effect, and remove.

- Stage is the stage you want to grant or remove.
- Effect is one of three options and changes who stages are granted to or removed from. Allowed options: player, team, or both.
- Remove determines whether stages will be granted or removed, with true removing a stage.
