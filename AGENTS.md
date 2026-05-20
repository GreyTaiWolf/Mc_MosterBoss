# MobMind / 怪物觉醒 Agent Notes

## Project

- Minecraft version: 1.21.1.
- Loader: NeoForge.
- Mod id: `mobmind`.
- Main package: `com.greytaiwolf.mobmind`.
- Build command: `.\gradlew.bat build`.
- Client test command: `.\gradlew.bat runClient`.

## Hard Rules

- Do not use Mixins unless there is no practical NeoForge event/API path.
- Do not add custom entities, models, or textures for MVP-style features.
- Prefer vanilla mobs and vanilla visuals: equipment, glowing, names, particles, effects, and vanilla blocks.
- Do not add `SynchedEntityData` to vanilla entities.
- Per-mob state must use NeoForge Data Attachments.
- World/group state must use `SavedData` or a server-side manager.
- Mob AI, leader selection, orders, shelter work, and block changes must run only on the logical server.
- Any block-breaking or block-placing logic must go through config gates and safety checks.
- After code changes, run `.\gradlew.bat build`.

## Current Architecture

- `attachment/`
  - `MobMindData`: per-monster state, including awareness, group id, leader id, role, order, nest/shelter references.
  - `ModAttachments`: registers the `mobmind:mob_mind` attachment and exposes Monster-safe helpers.
- `hive/`
  - `HiveSelector`: forms level-1 zombie groups and applies leader presentation.
  - `HiveManager`: server-memory group registry and group order helpers.
  - `HiveSavedData`: persistent group inventory and shelter work data.
  - `HiveOrder` / `HiveRole`: group command and member role enums.
  - `BlockChangeRules` / `ShelterPlanner`: block safety and shelter planning.
- `ai/`
  - `OrderExecutor`: group-level priority loop.
  - Role executors: guard, patrol, miner, builder.
  - Order executors: hunt player, defend leader, retreat, seek shade.
- `event/`
  - `LevelTickHandler`: group scans and order execution.
  - `CombatEventHandler`: leader damage/death reactions.
- `config/`
  - `MobMindConfig`: server config for block breaking/placing and throttling.

## Gameplay Defaults

- A level-1 leader controls one small team: leader + 5 members.
- Level-1 roles:
  - 2 guards
  - 1 miner
  - 1 patrol
  - 1 builder
- Leader visuals:
  - golden helmet
  - glowing effect
  - custom name
  - larger scale
  - small health/attack boost
- Shared group inventory:
  - 108 slots, equivalent to 4 chests.
  - Stored in `HiveSavedData`.
  - Dropped when the leader dies and the group disbands.

## Safety Notes

- `BlockChangeRules` is the central safety boundary for all block edits.
- Never bypass `MobMindConfig.ENABLE_MOB_BLOCK_BREAKING` or `ENABLE_MOB_BLOCK_PLACING`.
- Never allow workers to modify containers, beds, doors, redstone, rails, spawners, command blocks, block entities, water, or lava.
- Shelter work is intentionally simple and should remain throttled.

## Useful Debug Commands

- Set awareness:
  - `/mobmind debug awareness set <zombie> 100`
- Inspect awareness:
  - `/mobmind debug awareness get <zombie>`
- Inspect group, role, order, and shared inventory:
  - `/mobmind debug group get <monster>`
- Force leader flag for quick visual checks:
  - `/mobmind debug leader set <monster> true`

## User-Facing Docs

- `README.md`: GitHub landing page.
- `docs/USAGE.md`: install, run, awareness, leader conditions, debug commands.
- `docs/RULES.md`: gameplay and development rules.
- `CHANGELOG.md`: release/update log.
