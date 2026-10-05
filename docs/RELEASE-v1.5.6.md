## Carpet-ACMAX v1.5.6

适用于 **Minecraft 26.2 / Fabric / Java 25**。需要 Fabric Loader 0.19.3+、Carpet 26.2 和对应 Fabric API，成品放入服务端 `mods`。单人游戏放入本地 Fabric 实例；客户端无需安装本扩展。

### 更新内容

- 手动绑定假人视线内的村民，没有合适目标时选择 5 格内最近的可交易村民。绑定后村民发光 3 秒，聊天栏显示中文交易列表。
- 悬停查看假人实际交易价格、输入材料、产物和附魔；点击 `[按轮次]` / `[按数量]` 填入指令，在末尾填写目标数字。
- 用 `offer` 编号选择报价，无需物品 ID。支持购买、出售、双材料交易及不同附魔书。
- 材料不足目标时先提示，仍尽力交易；耗尽可支付材料后正常结束。
- 产物优先放入空潜影盒，堆叠盒按需一次展开一个；盒子不足时保留背包，直至无法接收下一笔产物。
- 返程冷却默认 2 秒，可在 `config/fake_player_void_trading.json` 的 `returnCooldownSeconds` 修改为 0–60 秒。交易、收纳期间计时，收纳完成且冷却结束才返回。
- `fakePlayerVoidTradingAllowNonOp` 默认关闭，仅 OP 可使用全部交易指令；开启后所有玩家可用。
- 保留折跃门冷却、掉落物警报、轻松放置铁轨、仙人掌扳手及试炼刷怪间隔五项规则。

### 快速使用

需要已搭好的虚空交易装置，以及假人携带的交易材料和背包空位。

```mcfunction
/carpet fakePlayerVoidTrading true
/player TradeBot spawn
/voidtrade TradeBot bind
/voidtrade TradeBot offer 1 cycle 100
/voidtrade TradeBot offer 1 count 500
/voidtrade TradeBot status
/voidtrade TradeBot stop
```

`cycle` 与 `count` 是两种独立启动方式，按需选择一种；数字范围 1–1000000。

允许非 OP 玩家使用并保存设置：

```mcfunction
/carpet fakePlayerVoidTradingAllowNonOp true
/carpet setDefault fakePlayerVoidTrading true
/carpet setDefault fakePlayerVoidTradingAllowNonOp true
```

完整用法见 [README](https://github.com/Gufeng-online/carpet-acmax/blob/main/README.md)，旧版恢复见 [回滚说明](https://github.com/Gufeng-online/carpet-acmax/blob/main/docs/ROLLBACK.zh.md)。

### 下载文件

- `carpet-acmax-addition-v1.5.6.jar`：安装此文件。
- `carpet-acmax-addition-v1.5.6-sources.jar`：源码包，无需放入 mods。
- 对应 `.sha256`：文件校验值。
