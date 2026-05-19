# MobMind Implementation Status

## Implemented
- NeoForge 1.21.1 project skeleton.
- `mobmind` mod id.
- Per-monster Data Attachment.
- Zombie awareness growth.
- Debug commands for awareness, leader, and group state.
- Zombie leader election.
- Level-1 team capacity: 6 zombies total.
- Member roles:
  - leader
  - guard
  - miner
  - patrol
  - builder
- Leader presentation:
  - golden helmet
  - glowing effect
  - custom name
  - larger scale
  - health and attack modifiers
- Group combat:
  - leader sees player
  - leader is attacked
  - group attacks same target
- Retreat:
  - leader health below 30 percent.
  - group falls back toward leader or shelter.
- Daylight avoidance:
  - exposed groups seek shade.
- Persistent group storage:
  - 108-slot shared inventory.
  - shelter anchor.
  - mining target and progress.
  - block change throttling.
- Miner:
  - real block breaking through safety rules.
  - pickaxe/shovel switching.
  - swing animation.
  - block crack progress.
  - drops go into shared inventory first.
- Builder:
  - consumes ordinary blocks from shared inventory.
  - places safe blocks to fill shelter shell gaps.
- Config:
  - `enableMobBlockBreaking`
  - `enableMobBlockPlacing`
  - `maxBlocksChangedPerGroupPerMinute`

## Not Implemented Yet
- Higher leader levels.
- GUI for group shared inventory.
- BossBar.
- Full nest identity and long-term nest ownership.
- Decorations such as cobwebs, mossy cobblestone, bones, soul sand.
- Multi-species logic.
- Player-facing guidebook.
- Advanced pathfinding or tactical formations.
- Persistent group manager beyond SavedData-backed inventory/shelter data.

## Known Balance Concerns
- Default block editing is enabled, so testing should happen in disposable worlds first.
- Shelter carving is intentionally basic and may create crude cubic spaces.
- Builder only uses simple ordinary blocks.
- Mining speed is abstracted, not tied to exact vanilla hardness.
- Current safety rules are conservative, but should be expanded before wide playtesting.

## Recommended Next Milestones
1. Add `/mobmind debug group inventory` summary/detail command.
2. Add a config option for leader election threshold and group size.
3. Add visual particles when a leader awakens.
4. Add BossBar for leader groups.
5. Add level-2 leader design.
6. Add true nest ownership and decoration pass.

