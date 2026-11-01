## Carpet-ACMAX v1.5.8

适用于 Minecraft 26.2 / Fabric / Java 25。需要 Fabric Loader 0.19.3+、Carpet 26.2 和对应的 Fabric API。

### 更新内容

- 新任务优先选择完全空的潜影盒，选中后跨轮持续填充，当前盒装满且仍有产物时再换盒。
- 堆叠潜影盒按需一次展开一个，不提前拆开用不到的盒子，避免不断开新盒占满背包。
- 收纳会处理背包中所有同组件产物，包括之前未存下的部分；盒子不足时保留溢出产物。
- 未指定报价的旧式命令按假人当前实际折扣选择低价报价；明确指定 offer 时仍使用所选报价。
- 达到轮数或产物数量目标后，完成收纳、剩余冷却及返程，再结束任务。

### 安装与装置配置

将 `carpet-acmax-addition-v1.5.8.jar` 放入服务端 `mods`。单人游戏放入本地 Fabric 实例的 `mods`。客户端无需安装本扩展，不要同时安装独立 `fake_player_void_trading`。

使用已有可正常运行的折跃门虚空交易装置。为避免折跃门维持村民区块加载，需要在已安装 CCA 的服务端开启：

```mcfunction
/cca GatewayCannotLoadingChunks true
```

未开启时，假人传送回主岛后可能在 `WAIT_GATEWAY` 等待约 13 秒，直到外岛村民所在区块真正卸载后才会进行交易。其他玩家或区块加载器也不能让该区块持续加载。

管理员开启并保存假人交易功能：

```mcfunction
/carpet fakePlayerVoidTrading true
/carpet setDefault fakePlayerVoidTrading true
```

为已有假人准备材料、潜影盒及背包空位，绑定村民后选择报价：

```mcfunction
/voidtrade TradeBot bind
/voidtrade TradeBot trades
/voidtrade TradeBot offer 1 cycle 100
/voidtrade TradeBot offer 1 count 500
/voidtrade TradeBot status
/voidtrade TradeBot stop
```

`cycle` 和 `count` 选择一种启动。规则开启后普通玩家也能使用交易命令。交易任务内保留当前收纳盒选择；停止后重新启动会重新优先选择空盒。展开堆叠盒至少需要两个普通背包空槽，请在交易前留足空间。

返程冷却在 `config/fake_player_void_trading.json` 中通过 `returnCooldownSeconds` 设置，默认 2 秒，允许 0–60 秒；修改后重启服务器。

### 附件

- `carpet-acmax-addition-v1.5.8.jar`：安装文件。
- `carpet-acmax-addition-v1.5.8-sources.jar`：项目构建的源码包，无需放入 `mods`；与 GitHub 自动生成的源码归档不同。
- 两个对应的 `.sha256` 文件：校验值。

本地 38 项测试及该版本 GitHub CI 通过；用户实测开启上述 CCA 规则后，约 13 秒的等待消失。单元测试不代表所有装置与模组组合均已完成游戏内验证。

完整用法见 [README](https://github.com/Gufeng-online/carpet-acmax/blob/main/README.md)，旧版与历史记录保留，回滚方法见 [回滚说明](https://github.com/Gufeng-online/carpet-acmax/blob/main/docs/ROLLBACK.zh.md)。
