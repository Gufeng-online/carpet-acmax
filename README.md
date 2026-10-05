# Carpet-ACMAX v1.5.6

**简体中文** | [English](README.en.md)

适用于 **Minecraft 26.2 / Fabric / Java 25** 的 Carpet 扩展，提供假人虚空交易、折跃门冷却调整、掉落物警报和铁轨增强等功能。

## v1.5.6 更新内容

- 假人可手动绑定村民，绑定优先选择视线内目标，没有目标时选择 5 格内最近的可交易村民；绑定后村民发光 3 秒，并向执行者显示中文交易列表。
- 悬停交易名称查看实际价格、所需材料、产物和附魔；点击 `[按轮次]` 或 `[按数量]` 填入命令，再填写目标数字。
- 支持购买、出售和双材料交易；可按报价编号选择不同附魔书，无需填写物品 ID。
- 目标高于现有材料可交易的数量时，提示后继续尽力交易，材料不足时正常结束。
- 产物优先存入空潜影盒，盒子不足时留在假人背包；堆叠潜影盒按需一次展开一个。
- 返程冷却默认 2 秒，可自定义；交易和收纳期间也计时，收纳完成且冷却结束后再点击陷阱箱。
- 新增非 OP 指令权限开关，默认仅 OP 可用，开启后所有玩家均可使用 `/voidtrade`。

## 安装

从 [Releases](https://github.com/Gufeng-online/carpet-acmax/releases/tag/v1.5.6) 下载 `carpet-acmax-addition-v1.5.6.jar`，放入服务器的 `mods` 文件夹，同时安装：

- Fabric Loader **0.19.3 或更新版本**。
- 适用于 Minecraft 26.2 的 [Fabric API](https://modrinth.com/mod/fabric-api)。
- [Carpet 26.2](https://modrinth.com/mod/carpet/version/26.2)。

客户端无需安装本扩展。单人游戏放入本地 Fabric 实例的 `mods` 文件夹即可。请勿同时安装独立的 `fake_player_void_trading`。

## Carpet 规则

使用 `/carpet list ACMAX` 查看规则。

| 规则 | 默认值 | 功能 |
| --- | --- | --- |
| `fakePlayerVoidTrading` | `false` | 开启假人虚空交易。关闭后停止正在运行的交易任务。 |
| `fakePlayerVoidTradingAllowNonOp` | `false` | 开启后所有玩家可使用全部 `/voidtrade` 指令；关闭时仅 OP（权限等级至少 2）可用。 |
| `endGatewayCooldown` | `false` | 开启后取消末地折跃门的 2 秒传送冷却。 |
| `itemEntityAlarm` | `0` | 区块内掉落物堆数达到设定值时通报坐标和数量；`0` 关闭，可设任意正整数。 |
| `railForceStatePlacement` | `false` | 使用轻松放置时，按投影形状放置铁轨，支持悬空铁轨。 |
| `cactusWrenchRailEnhancement` | `false` | 配合 Carpet 的 `flippinCactus`，手持仙人掌右键切换铁轨形态。 |
| `trialSpawnerIntervalFix` | `false` | 将特定试炼密室中异常的 8 秒刷怪间隔调整为 1 秒。 |

开启交易功能：

```mcfunction
/carpet fakePlayerVoidTrading true
```

允许非 OP 玩家使用绑定、交易、查询和停止指令：

```mcfunction
/carpet fakePlayerVoidTradingAllowNonOp true
```

使用 `setDefault` 保存规则，服务器重启后仍生效：

```mcfunction
/carpet setDefault fakePlayerVoidTrading true
/carpet setDefault fakePlayerVoidTradingAllowNonOp true
```

修改 Carpet 规则仍需要相应管理权限。交易总开关关闭时，`status` 和 `stop` 仍可使用，并遵守指令权限规则。

## 假人虚空交易用法

### 准备装置和假人

需要可正常运行的虚空交易装置：假人打开村民交易后，经末地折跃门到达远端，使村民区块卸载；远端提供可交互的陷阱箱和返程机构。其他玩家或区块加载器不能让目标村民区块持续加载。

创建假人，将它放在村民附近，为它准备交易材料、潜影盒及背包空位：

```mcfunction
/player TradeBot spawn
/voidtrade TradeBot bind
```

绑定优先选择假人视线 5 格内的可交易村民；没有合适目标时选择半径 5 格内最近的可交易村民。绑定后显示该村民当前已解锁的所有报价，包括买入和卖出。运行中的假人须先停止交易再重新绑定。

### 选择报价

交易列表形式为：`物品名称 [按轮次] [按数量]`。

- 悬停名称或按钮查看假人当前实际价格、材料、产物、附魔和剩余交易次数。
- 名称不可点击；两个按钮分别填入对应模式的交易指令。
- 在输入栏末尾填写目标数字后回车，点击按钮不会立即执行。
- 售罄报价不能点击；使用下面的命令刷新列表：

```mcfunction
/voidtrade TradeBot trades
```

### 开始交易

按装置轮数交易：

```mcfunction
/voidtrade TradeBot offer 1 cycle 100
```

按本次任务新获得的产物数量交易：

```mcfunction
/voidtrade TradeBot offer 1 count 500
```

`offer 1` 表示列表中的第 1 条报价，编号从 1 开始。目标数字范围为 **1–1000000**。`cycle` 表示装置轮数，`count` 不计入假人原先已有的物品。

无需填写 `minecraft:物品ID`。也可使用以下形式，物品 ID 须与报价产物一致：

```mcfunction
/voidtrade TradeBot minecraft:diamond offer 1 count 500
```

多个相同物品 ID 的附魔书可用不同报价编号区分。交易支持绿宝石、绿宝石加其他物品，以及出售物品换取绿宝石。

### 材料、收纳与停止

材料不足以完成目标时会提示，仍交易所有能够支付的部分。最后剩余材料只能支付较少交易时，也会继续成交；不足一笔报价的材料保留。每笔交易按该报价的完整数量成交，因此累计产物可能略超过 `count` 目标。

产物优先放入空潜影盒；堆叠盒按需一次展开一个。潜影盒容量不足或没有可用盒子时，剩余产物保留在假人背包并继续。只有背包和潜影盒都无法接收下一笔产物时才因空间不足停止。

查询或手动停止：

```mcfunction
/voidtrade TradeBot status
/voidtrade TradeBot stop
```

假人离线、服务端重启后，需要重新绑定和启动任务。菜单异常、报价变化或装置超时会提示停止原因。

### 旧版命令

```mcfunction
/voidtrade TradeBot start minecraft:diamond 100
/voidtrade TradeBot start minecraft:enchanted_book 100 minecraft:mending
```

## 返程冷却设置

配置文件：服务器的 `config/fake_player_void_trading.json`。修改后重启服务器。

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

`returnCooldownSeconds` 默认 **2 秒**，允许 **0–60 秒**，支持小数。从假人传送到远端开始计时，交易和放入潜影盒的时间也计入。收纳完成且冷却结束后才点击陷阱箱；收纳用时超过冷却则完成后直接返回。没有可用盒子时，产物保留背包并按相同冷却返回。

其余时间设置单位为游戏 tick，正常速度为 20 tick/秒。`phaseTimeoutTicks` 是阶段等待上限，`trappedChestSearchTicks` 是寻找陷阱箱的等待时间；陷阱箱需要较长红石信号时可增大 `trappedChestOpenTicks`。

## 下载与旧版

[下载 v1.5.6](https://github.com/Gufeng-online/carpet-acmax/releases/tag/v1.5.6) · [回滚说明](docs/ROLLBACK.zh.md) · [验证结果](VALIDATION.md)

MIT 许可证；第三方许可见 `licenses/`。
