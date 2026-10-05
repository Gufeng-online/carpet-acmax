# Carpet-ACMAX v1.5.6

[简体中文](README.md) | **English**

A Carpet extension for **Minecraft 26.2 / Fabric / Java 25**, featuring fake-player void trading, end-gateway cooldown control, item-entity alerts and rail tools.

## What's new in v1.5.6

- Bind a bot to a villager in its line of sight, or the nearest tradeable villager within five blocks. The villager glows for three seconds and the caller receives a Chinese trade list.
- Hover for actual prices, ingredients, outputs and enchantments. Use separate cycle/count buttons to fill in a command, then enter the target number.
- Buy, sell and use two-ingredient offers; select enchanted books by offer number without an item ID.
- Insufficient materials trigger a notice and the bot trades everything it can afford.
- Prefer empty shulkers, split stacked boxes one at a time as needed, and keep overflow in the bot inventory.
- Configurable return cooldown, default two seconds, runs during trading and storage. Return waits for both storage completion and cooldown expiry.
- Optional access for non-OP players; commands default to OP-only.

## Installation

Download `carpet-acmax-addition-v1.5.6.jar` from [Releases](https://github.com/Gufeng-online/carpet-acmax/releases/tag/v1.5.6) and put it in the server's `mods` folder. Requires Fabric Loader **0.19.3+**, [Fabric API for 26.2](https://modrinth.com/mod/fabric-api) and [Carpet 26.2](https://modrinth.com/mod/carpet/version/26.2).

Clients do not need this extension. For single-player, install it in your Fabric instance. Do not install the standalone `fake_player_void_trading` alongside it.

## Carpet rules

View rules with `/carpet list ACMAX`.

| Rule | Default | Function |
| --- | --- | --- |
| `fakePlayerVoidTrading` | `false` | Enable fake-player void trading. Turning it off stops active tasks. |
| `fakePlayerVoidTradingAllowNonOp` | `false` | Allow everyone to use all `/voidtrade` commands when true; otherwise require OP level 2+. |
| `endGatewayCooldown` | `false` | Remove the end gateway's two-second teleport cooldown. |
| `itemEntityAlarm` | `0` | Broadcast a chunk's coordinates and item-stack count at the chosen threshold. Zero disables it. |
| `railForceStatePlacement` | `false` | Place rails in the schematic's shape with easy place, including unsupported rails. |
| `cactusWrenchRailEnhancement` | `false` | With Carpet's `flippinCactus`, right-click rails with a cactus to change their shape. |
| `trialSpawnerIntervalFix` | `false` | Change abnormal eight-second spawn intervals in certain trial chambers to one second. |

```mcfunction
/carpet fakePlayerVoidTrading true
/carpet fakePlayerVoidTradingAllowNonOp true
/carpet setDefault fakePlayerVoidTrading true
/carpet setDefault fakePlayerVoidTradingAllowNonOp true
```

`setDefault` persists the setting across restarts. Managing Carpet rules still requires administrative permission. With trading disabled, `status` and `stop` remain available subject to the command-access rule.

## Fake-player void trading

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

## Return cooldown

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

## Downloads and previous versions

[Download v1.5.6](https://github.com/Gufeng-online/carpet-acmax/releases/tag/v1.5.6) · [Rollback instructions](docs/ROLLBACK.zh.md) · [Validation](VALIDATION.md)

MIT license; third-party licenses are in `licenses/`.
