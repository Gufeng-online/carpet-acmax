# Carpet-ACMAX

[简体中文](README.md) | **English**

A [Carpet](https://github.com/gnembon/fabric-carpet) extension written for the ACMAX server.
**Server-side only** – install it on the server and it just works; clients do not need it
(unless a rule says otherwise).

## Versions and branches

| Branch | Minecraft | Fabric API | Carpet | Mod version |
| --- | --- | --- | --- | --- |
| `main` | 26.2 | 0.158.0+26.2 | 26.2 | v1.5.6 |
| `26.1.2` | 26.1.2 | 0.155.2+26.1.2 | 26.1 | v1.2 |

Both branches provide the five rules below. The `main` branch also includes fake-player void trading.

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

## Additional feature: fake-player void trading

Available on `main` for Minecraft 26.2. Requires a working void-trading device.

### Enable trading and command access

| Rule | Default | Function |
| --- | --- | --- |
| `fakePlayerVoidTrading` | `false` | Enable fake-player trading; turning it off stops active tasks. |
| `fakePlayerVoidTradingAllowNonOp` | `false` | Allow everyone to use all `/voidtrade` commands when true; otherwise require OP level 2+. |

```mcfunction
/carpet fakePlayerVoidTrading true
/carpet fakePlayerVoidTradingAllowNonOp true
/carpet setDefault fakePlayerVoidTrading true
/carpet setDefault fakePlayerVoidTradingAllowNonOp true
```

`setDefault` persists the setting across restarts. Managing Carpet rules still requires administrative permission. With trading disabled, `status` and `stop` remain available subject to the command-access rule.

### Usage

Prepare a working void-trading device: the bot opens a villager trade, travels through an end gateway so the villager's chunk unloads, then uses a trapped chest and return mechanism at the remote end. Other players or chunk loaders must not keep the villager loaded.

Create the bot near the villager and supply ingredients, shulkers and inventory space:

```mcfunction
/player TradeBot spawn
/voidtrade TradeBot bind
/voidtrade TradeBot trades
```

Binding prefers a villager in the bot's view within five blocks, falling back to the nearest tradeable villager within that radius. Stop an active task before rebinding.

The list shows `item name [按轮次] [按数量]`. Hover for the bot's actual costs, quantities, enchantments and remaining uses. The name is hover-only; the buttons suggest cycle/count commands. Enter the number at the end and press Enter. Clicking does not execute a command. Sold-out offers cannot be clicked.

```mcfunction
/voidtrade TradeBot offer 1 cycle 100
/voidtrade TradeBot offer 1 count 500
/voidtrade TradeBot minecraft:diamond offer 1 count 500
/voidtrade TradeBot status
/voidtrade TradeBot stop
```

Offer numbers start at one. The item ID is optional and must match the selected offer. Cycle means device rounds; count means newly obtained outputs during this task, excluding existing items. Target range: **1–1000000**. Offer numbers distinguish enchanted books sharing the same item ID. Buying, selling and two-ingredient offers are supported.

When materials cannot meet the target, the bot warns and continues every affordable trade, including the final smaller batch. Materials insufficient for one whole trade remain in inventory. Each trade produces its full quoted output, so the count target may be slightly exceeded.

Outputs prefer empty shulkers. Stacked boxes split one at a time as needed. When boxes are absent or full, overflow stays in the bot inventory and trading continues. Space stops trading only when the next output fits neither inventory nor shulkers. Rebind and restart after bot logout or server restart. Menu problems, changed offers and device timeouts report a stop reason.

Legacy commands:

```mcfunction
/voidtrade TradeBot start minecraft:diamond 100
/voidtrade TradeBot start minecraft:enchanted_book 100 minecraft:mending
```

### Return cooldown

Edit the server's `config/fake_player_void_trading.json` and restart:

```json
{
  "phaseTimeoutTicks": 2400,
  "gatewayArmTicks": 20,
  "postTeleportTradeDelayTicks": 6,
  "returnCooldownSeconds": 2.0,
  "trappedChestOpenTicks": 1,
  "trappedChestSearchTicks": 600
}
```

`returnCooldownSeconds` defaults to **2 seconds**, accepts **0–60**, and supports decimals. Timing starts at outbound arrival and includes trading and storage. The bot clicks the trapped chest only after storage finishes and the cooldown expires. If storage takes longer, return starts immediately afterward. With no usable boxes, outputs stay in inventory and the same cooldown applies.

Other durations use game ticks, normally 20 ticks per second. Increase `trappedChestOpenTicks` if your device needs a longer redstone pulse.

## Download

Get `carpet-acmax-addition-v1.5.6.jar` from [Releases](https://github.com/Gufeng-online/carpet-acmax/releases/tag/v1.5.6) and put it in the server `mods` folder, or your local Fabric instance for single-player. Clients do not need this extension. Do not install standalone `fake_player_void_trading` alongside it.

[Rollback](docs/ROLLBACK.zh.md) · [Validation](VALIDATION.md)

## Building

Requires **JDK 25** (Minecraft 26.x requires it).

```bash
./gradlew build
```

Output: `build/libs/carpet-acmax-addition-v1.5.6.jar` – drop it into the server `mods/` folder,
no client installation needed.

## Requirements

- Minecraft 26.2 (`main` branch) or 26.1.2 (`26.1.2` branch)
- Fabric Loader ≥ 0.19.3 for `main`; the 26.1.2 branch keeps its original requirements
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Carpet](https://modrinth.com/mod/carpet)

## License

MIT
