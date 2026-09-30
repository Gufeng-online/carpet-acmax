# Carpet-ACMAX

[简体中文](README.md) | **English**

A [Carpet](https://github.com/gnembon/fabric-carpet) extension written for the ACMAX server.
**Server-side only** – install it on the server and it just works; clients do not need it
(unless a rule says otherwise).

## Versions and branches

| Branch | Minecraft | Fabric API | Carpet | Mod version |
| --- | --- | --- | --- | --- |
| `main` | 26.2 | 0.158.0+26.2 | 26.2 | v1.2 |
| `26.1.2` | 26.1.2 | 0.155.2+26.1.2 | 26.1 | v1.2 |

Both branches contain exactly the same features – only the Minecraft / Fabric API / Carpet
target version differs.

## Rules

All rules live in the `ACMAX` category and can be changed with the `/carpet` command or in
`config/carpet-acmax.conf`.

| Rule | Default | Description |
| --- | --- | --- |
| `endGatewayCooldown` | `false` | Disables the 2-second cooldown between end gateway teleports, allowing unlimited teleporting. |
| `itemEntityAlarm` | `0` | Item pile-up alarm: alerts everyone in chat with coordinates and stack count when a single chunk reaches the threshold. Presets are `1000 / 2000 / 4000 / 6000`, any custom positive integer works, and `0` disables the alarm. |
| `railForceStatePlacement` | `false` | Forced rail state placement: only affects rails placed with Easy Place (Litematica protocol V3). Rails keep the shape from the schematic (floating / unsupported rails included) and the placement does not reshape the surrounding rails. Everything returns to vanilla behaviour as soon as the placement transaction ends, so redstone reshaping and unsupported rails dropping are completely unaffected. |
| `cactusWrenchRailEnhancement` | `false` | Cactus wrench rail enhancement (also requires `flippinCactus`): right-click rails with a cactus to cycle through every shape without triggering block or neighbour updates. |
| `trialSpawnerIntervalFix` | `false` | Fixes the trial spawner interval: the 160gt (8 second) interval used by the `chamber_8` and `encounter_4` structures of trial chambers becomes the normal 20gt (1 second). |

## Building

Requires **JDK 25** (Minecraft 26.x requires it).

```bash
./gradlew build
```

Output: `build/libs/carpet-acmax-addition-v1.2.jar` – drop it into the server `mods/` folder,
no client installation needed.

## Requirements

- Minecraft 26.2 (`main` branch) or 26.1.2 (`26.1.2` branch)
- Fabric Loader ≥ 0.15.0
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Carpet](https://modrinth.com/mod/carpet)

## License

MIT
