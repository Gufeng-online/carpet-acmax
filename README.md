# Carpet-ACMAX

**简体中文** | [English](README.en.md)

ACMAX 服务器自用的 [Carpet](https://github.com/gnembon/fabric-carpet) 扩展。
**纯服务端规则**：只装在服务端即可生效，客户端可以不装（除特别标注的规则外）。

## 版本与分支

| 分支 | Minecraft | Fabric API | Carpet 依赖 | 模组版本 |
| --- | --- | --- | --- | --- |
| `main` | 26.2 | 0.158.0+26.2 | 26.2 | v1.5.6 |
| `26.1.2` | 26.1.2 | 0.155.2+26.1.2 | 26.1 | v1.2 |

两个分支均提供原有五项规则；假人虚空交易的两项规则仅适用于 `main` 分支。

## 规则列表

规则分类为 `ACMAX`，通过 `/carpet` 指令或 `config/carpet-acmax.conf` 修改。

| 规则 | 默认值 | 说明 |
| --- | --- | --- |
| `endGatewayCooldown` | `false` | 禁用末地折跃门传送之间的 2 秒冷却，允许无限制连续传送。 |
| `itemEntityAlarm` | `0` | 掉落物堆积警报：某一区块内的掉落物堆数达到阈值时向全服通报坐标与数量。预设档位 `1000 / 2000 / 4000 / 6000`，也可填任意正整数，`0` 为关闭。 |
| `railForceStatePlacement` | `false` | 铁轨强制状态放置：只对「轻松放置」（Litematica 协议 V3）放置的铁轨生效。铁轨按投影形状放置（含悬空、无支撑的铁轨），且本次放置不会让周围铁轨变形。事务结束后立即恢复原版行为，红石换向、无支撑掉落等特性完全不受影响。 |
| `cactusWrenchRailEnhancement` | `false` | 仙人掌扳手铁轨增强（需同时开启 `flippinCactus`）：手持仙人掌右键铁轨可依次切换所有形态，且不触发方块 / 邻居更新。 |
| `trialSpawnerIntervalFix` | `false` | 修复试炼刷怪笼生成间隔：把试炼密室 `chamber_8`、`encounter_4` 结构中 160gt（8 秒）的生成间隔修正为正常的 20gt（1 秒）。 |
| `fakePlayerVoidTrading` | `false` | 开启假人虚空交易；关闭后停止正在运行的交易任务。 |
| `fakePlayerVoidTradingAllowNonOp` | `false` | 开启后所有玩家可使用全部 `/voidtrade` 指令；关闭时仅 OP（权限等级至少 2）可用。 |

### 虚空交易用法

```mcfunction
/carpet fakePlayerVoidTrading true
/carpet fakePlayerVoidTradingAllowNonOp true
```

使用 `setDefault` 保存设置，服务器重启后仍生效：

```mcfunction
/carpet setDefault fakePlayerVoidTrading true
/carpet setDefault fakePlayerVoidTradingAllowNonOp true
```

修改 Carpet 规则仍需要相应管理权限。交易总开关关闭时，`status` 和 `stop` 仍可使用，并遵守指令权限规则。

#### 准备装置和假人

需要可正常运行的虚空交易装置：假人打开村民交易后，经末地折跃门到达远端，使村民区块卸载；远端提供可交互的陷阱箱和返程机构。其他玩家或区块加载器不能让目标村民区块持续加载。

创建假人，将它放在村民附近，为它准备交易材料、潜影盒及背包空位：

```mcfunction
/player TradeBot spawn
/voidtrade TradeBot bind
```

绑定优先选择假人视线 5 格内的可交易村民；没有合适目标时选择半径 5 格内最近的可交易村民。绑定后显示该村民当前已解锁的所有报价，包括买入和卖出。运行中的假人须先停止交易再重新绑定。

#### 选择报价

交易列表形式为：`物品名称 [按轮次] [按数量]`。

- 悬停名称或按钮查看假人当前实际价格、材料、产物、附魔和剩余交易次数。
- 名称不可点击；两个按钮分别填入对应模式的交易指令。
- 在输入栏末尾填写目标数字后回车，点击按钮不会立即执行。
- 售罄报价不能点击；使用下面的命令刷新列表：

```mcfunction
/voidtrade TradeBot trades
```

#### 开始交易

**mod会自动检测假人被传送到主岛折跃门处交易时，外岛折跃门处的村民是否真正被卸载，如果未被卸载则会等待卸载**

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

#### 材料、收纳与停止

材料不足以完成目标时会提示，仍交易所有能够支付的部分。最后剩余材料只能支付较少交易时，也会继续成交；不足一笔报价的材料保留。每笔交易按该报价的完整数量成交，因此累计产物可能略超过 `count` 目标。

产物优先放入空潜影盒；堆叠盒按需一次展开一个。潜影盒容量不足或没有可用盒子时，剩余产物保留在假人背包并继续。只有背包和潜影盒都无法接收下一笔产物时才因空间不足停止。

查询或手动停止：

```mcfunction
/voidtrade TradeBot status
/voidtrade TradeBot stop
```

假人离线、服务端重启后，需要重新绑定和启动任务。菜单异常、报价变化或装置超时会提示停止原因。

#### 旧版命令

```mcfunction
/voidtrade TradeBot start minecraft:diamond 100
/voidtrade TradeBot start minecraft:enchanted_book 100 minecraft:mending
```

#### 返程冷却设置

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

## 构建

需要 **JDK 25**（Minecraft 26.x 要求）。

```bash
./gradlew build
```

产物：`build/libs/carpet-acmax-addition-v1.5.6.jar`（放进服务端 `mods/` 即可，客户端无需安装）。

## 运行依赖

- Minecraft 26.2（`main` 分支）或 26.1.2（`26.1.2` 分支）
- Fabric Loader ≥ 0.19.3（`main` 分支），26.1.2 分支沿用原依赖
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Carpet](https://modrinth.com/mod/carpet)

适用于假人虚空交易的依赖为 Carpet 26.2 和对应 Minecraft 26.2 的 Fabric API；不需要客户端前置。

从 [Releases](https://github.com/Gufeng-online/carpet-acmax/releases/tag/v1.5.6) 下载 `carpet-acmax-addition-v1.5.6.jar`，放入服务器 `mods`。单人游戏放入本地 Fabric 实例的 `mods`。客户端无需安装本扩展，请勿同时安装独立 `fake_player_void_trading`。

[旧版回滚](docs/ROLLBACK.zh.md) · [验证结果](VALIDATION.md)

## License

MIT
