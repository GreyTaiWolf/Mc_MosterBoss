# MobMind Testing Guide

## Launch
Use:

```powershell
.\gradlew.bat runClient
```

If `java` is not on PATH in this environment, temporarily use:

```powershell
$env:JAVA_HOME='C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat runClient
```

## Quick Leader Test
1. Create a test world with cheats enabled.
2. Spawn at least 6 normal zombies close together.
3. Set each zombie awareness to 100:

```mcfunction
/mobmind debug awareness set <zombie> 100
```

4. Wait up to 10 seconds.
5. Expected:
   - one zombie becomes leader
   - leader is bigger, glowing, named, and wearing a golden helmet
   - members receive roles and equipment

Inspect a zombie:

```mcfunction
/mobmind debug group get <monster>
```

## Combat Test
1. Attack the leader.
2. Expected:
   - group switches to `DEFEND_LEADER`
   - guards rush the attacker first
   - other members can also join combat

## Retreat Test
1. Damage the leader below 30 percent health.
2. Expected:
   - group switches to `RETREAT`
   - members stop normal work
   - group moves toward leader/shelter anchor

## Daylight And Shelter Test
1. Set the time to day.
2. Expose the leader group to the sky.
3. Expected:
   - group switches to shade-seeking behavior
   - group moves toward a safe non-sky-exposed anchor
   - miner starts clearing a 15x15x3 shelter area if needed
   - builder fills shell gaps using shared inventory blocks

## Shared Inventory Test
1. Let the miner break allowed natural blocks.
2. Run:

```mcfunction
/mobmind debug group get <monster>
```

3. Expected:
   - `sharedInventory` used slots/items increase.
   - blocks are not dropped unless inventory is full.

## Config
Server config values:

```toml
enableMobBlockBreaking = true
enableMobBlockPlacing = true
maxBlocksChangedPerGroupPerMinute = 24
```

Disable block editing if testing near important builds.

