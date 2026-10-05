# Fake Player Void Trading

Minecraft **26.2 / Fabric / Java 25** 服务端模组，使用 **Carpet 假人**自动运行虚空交易装置。本文描述整合版 v1.5.6；原客户端项目已独立修复材料不足并升级 3.1.9。

## 安装

将 `releases/carpet-acmax-addition-v1.5.6.jar` 放入服务器 `mods` 文件夹，同时安装适用于 26.2 的 Fabric API 和 [Carpet 26.2](https://modrinth.com/mod/carpet/version/26.2)。客户端无需安装本模组，也不需要 Item Scroller、MaLiLib 或 Quick Shulker。单人游戏可装在本地 Fabric 实例中，逻辑仍运行于内置服务器。

## 规则与绑定

总开关 `fakePlayerVoidTrading` 默认 false，使用 `/carpet fakePlayerVoidTrading true` 开启。自动绑定村民规则与触发逻辑已移除。执行 `/voidtrade NAME bind` 手动绑定，优先视线目标、否则选5格内最近村民。

## 使用

1. 用 Carpet 的 `/player TradeBot spawn` 创建假人，将它放在目标成年村民 4.5 格交互范围内。给假人携带绿宝石、需要的书、潜影盒，并留出背包空间。绿宝石和书也可放在假人携带的潜影盒里。
2. 装置必须能在打开交易后将假人送入末地折跃门，在远端提供假人能够右键的陷阱箱，并通过机关将假人送回原村民。模组负责交易和点击容器；位置移动由现有装置完成。
3. 使用以下命令启动，命令默认要求 OP 2 级或服务器控制台权限：

```mcfunction
/voidtrade TradeBot start minecraft:diamond 100
/voidtrade TradeBot start minecraft:enchanted_book 100 minecraft:mending
/voidtrade TradeBot status
/voidtrade TradeBot stop
```

`cycles` 为完整交易轮数，范围 1–1000000。附魔书必须提供附魔 ID，匹配类型而不限制等级。输出物品 ID 支持补全。控制台输入时省略 `/`。

## 流程与装置要求

- 初次绑定最近的可交互成年村民；后续只使用这个村民的 UUID。目标村民被其他玩家交易时会等待。
- 选择未售罄、单位产出绿宝石价格最低的匹配报价。支持原版单输入及双输入，包含绿宝石加其他材料和出售物品。点击指定 offer 时精确使用该报价。
- 打开真实原版交易菜单，等待假人在折跃门附近发生至少 16 格的位置跳变，且服务端村民以 `UNLOADED_TO_CHUNK` 原因卸载，然后再等待默认 6 tick。
- 每 tick 进行一次原版交易，直至选定报价售罄或材料不足以支付下一笔。真正消耗材料并执行原版产物、交易次数和经验逻辑，不重置报价。
- 关闭交易菜单，将本轮产物存入假人携带的潜影盒。服务端直接修改背包数据组件，不需要模拟客户端开盒协议。
- 尚有剩余轮次时，右键 4.5 格范围内可交互的最近陷阱箱，默认打开 1 tick 后关闭，等待装置返程，再重复。正常达到轮数上限时仍在远端停止；因材料用尽提前结束时会完成收纳与返程后正常停止。开始时连一次完整报价都付不起则直接正常结束。

**村民区块必须真正卸载**。其他玩家、区块加载器或强制加载让村民保持加载时，会等待直到超时。仅客户端看不到村民不够。模组不自动搭建设备、寻路或直接传送假人。

26.2 原版菜单会校验村民是否存活及交互距离。本模组仅对当前自动化假人绑定的菜单保留有效性，以便自然卸载后完成交易；手动交易和真人菜单保持原版行为。完成或停止后该特例立即撤销。

## 配置

首次启动生成 `config/fake_player_void_trading.json`，修改后重启服务端生效。时间单位均为游戏 tick，正常速度 20 tick/秒：

```json
{
  "phaseTimeoutTicks": 2400,
  "gatewayArmTicks": 20,
  "postTeleportTradeDelayTicks": 6,
  "trappedChestOpenTicks": 1,
  "trappedChestSearchTicks": 600
}
```

`phaseTimeoutTicks` 控制村民寻找、传送卸载和返程的等待上限；`gatewayArmTicks` 控制折跃门附近检测的有效时间；陷阱箱需持续输出红石信号的装置可增大 `trappedChestOpenTicks`。

## 停止与物品保护

材料提取先在背包副本上规划，材料不够或没有容纳完整产物的空间时不扣除新一笔材料。潜影盒按背包槽位顺序使用，先合并同组件堆叠，再填空槽；有战利品表的盒子跳过。

菜单关闭或替换、报价改变、村民非正常卸载、假人死亡/离线/跨维度、背包和潜影盒均无法接收下一笔产物或装置超时都会停止，并向启动命令的执行者和服务器日志报告原因。已交易产物保留在背包，交易输入通过原版菜单关闭流程退回。仅关闭模组自己打开的菜单，不关闭替换进来的其他容器。不要同时用 Carpet 的 use/attack/drop 动作或其他模组操作同一个假人背包和菜单。

材料不足不报错误，不扣无法完成交易的那一笔材料，零头保留。任务不跨服务器重启恢复；重启后需要重新启动命令。

## 构建

```powershell
$env:JAVA_HOME='你的 JDK 25 路径'
.\gradlew.bat test build
```

开发测试服使用 `./gradlew.bat runServer`，Carpet 需自行放入 `run-server/mods`。不要把 `run-server` 测试世界拷贝到正式服务器。

启动时按假人当前实际价格估算资源，预计无法完成全部轮数时黄色提示但仍继续。鼠标悬停也使用假人声望和村庄英雄等原版价格；实际成交前重新获取最新报价。绑定后 ID 补全仅显示该村民未售罄报价的输出。停止命令：`/voidtrade TradeBot stop`。

潜影盒不足时尽量部分收纳，剩余产物留在假人背包，继续轮次；交易中也会尝试把已有产物移入盒中腾空间。只有背包也不能接收完整下一笔产物才停止，材料不因此被额外扣除。

## v1.5.6 指令与堆叠盒

`/voidtrade BOT offer 1 cycle 数字` 按完整轮数；`/voidtrade BOT offer 1 count 数字` 按本次新产物数量。可选物品 ID 放在 offer 前，旧命令保留。目标数量不足仍交易能成交的部分，最后一次完整报价可能越过目标。输入不足一笔时零头保留，不扣除无法完成交易的部分。堆叠盒在空位允许时拆开，保留内容与组件，预留一格产物槽。

## 可修改的返程冷却

默认 2 秒，`returnCooldownSeconds` 可设 0–60，支持小数。假人配置在服务端 `config/fake_player_void_trading.json`，修改后重启服务器；已有配置启动时会补写新字段。客户端按 C → 高级设置 → 第5页「返程冷却」，修改「传送后返程冷却（秒）」并保存；配置路径 `config/auto_void_trading.json`。设置在下一次远端传送时锁定。计时后台进行，不阻塞交易和收纳；只在产物收纳流程完成且冷却结束后点击陷阱箱。收纳超过冷却时间时收纳结束即返回；若盒子不足，则沿用剩余物品留背包并返回。

交易列表格式为「物品名称 [按轮次] [按数量]」。名称只显示悬停报价，不可点击；两个按钮分别填入 cycle/count 命令，最后由玩家输入目标数字。已售罄时两个按钮都不可点击。

## 假人交易指令权限

`fakePlayerVoidTradingAllowNonOp` 默认 false，控制全部 `/voidtrade` 子指令（绑定、列表、交易、状态、停止及旧指令形式）。false 时仅 OP 权限等级至少 2 可用；true 时所有玩家可用。服务器控制台可继续使用。修改后在线玩家的命令补全会自动刷新，无需重登。此规则不替代交易功能总开关，也不更改 Carpet 管理规则自身的权限。

```mcfunction
/carpet fakePlayerVoidTradingAllowNonOp true
/carpet fakePlayerVoidTradingAllowNonOp false
/carpet setDefault fakePlayerVoidTradingAllowNonOp true
```

最后一条保存「所有玩家可用」为世界默认值。首次升级保持原来的仅 OP 权限。
