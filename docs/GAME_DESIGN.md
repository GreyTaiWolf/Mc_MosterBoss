# MobMind / 怪物觉醒 Game Design

## Core Fantasy

Vanilla monsters slowly develop awareness. When enough aware monsters gather, one becomes a leader and starts behaving like the mind of a small hostile group.

The mod should feel like Minecraft is still Minecraft, but the monsters have begun to organize.

## MVP Species

- Current focus: vanilla zombie only.
- Do not expand to husks, drowned, zombie villagers, skeletons, spiders, or creepers until the zombie loop is fun and stable.

## Awareness

- Awareness increases over time while a zombie survives.
- Growth is faster at night.
- Growth is faster near other zombies.
- Awareness is stored per monster through Data Attachments.

## Level-1 Zombie Leader

Trigger:

- Nearby group of 6 base zombies.
- Each candidate must have enough awareness.

Result:

- Highest-awareness zombie becomes leader.
- The team becomes:
  - 1 leader
  - 2 guards
  - 1 miner
  - 1 patrol
  - 1 builder

Leader presentation:

- Golden helmet.
- Glowing highlight.
- Custom name: 觉醒僵尸首领.
- Larger body using vanilla scale attribute.
- Small combat stat boost.

## Roles

### Leader

- Controls group target, retreat, shade-seeking, and shelter work.
- If attacked, commands the group to defend.

### Guards

- Stay close to the leader.
- Respond fastest when the leader is attacked.
- Receive priority combat equipment.

### Patrol

- Roams around the leader/shelter area.
- Searches for players.
- If it sees a player, it summons the team into a hunt.

### Miner

- Searches for shelter work blocks.
- Switches between pickaxe and shovel.
- Plays attack animation and block crack animation while mining.
- Sends drops into the group shared inventory.

### Builder

- Uses ordinary blocks from the shared inventory.
- Repairs/fills simple shelter shell holes.
- Does not place decorative blocks yet.

## Shelter Behavior

Purpose:

- Keep zombies alive during the day.
- Create a rough 15x15x3 protected work area.

Behavior:

- If the team is exposed to sunlight, it seeks nearby shade.
- If no good safe shade exists, shelter work begins around the chosen anchor.
- Miner clears the interior.
- Builder fills shell gaps.

Restrictions:

- This is not a full nest system yet.
- No complex architecture, path networks, traps, or decorative rooms.
- All block changes must pass safety checks.

## Shared Inventory

- Each group owns a shared 108-slot inventory.
- The inventory stores mined drops and building materials.
- Builders consume ordinary blocks from it.
- On leader death, contents drop into the world.
