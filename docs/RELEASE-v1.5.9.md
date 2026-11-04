## Carpet-ACMAX v1.5.9

适用于 **Minecraft 26.2 / Fabric / Java 25**。需要 Fabric Loader 0.19.3+、Carpet 26.2 和对应 Fabric API。

### 更新内容

- 合并上游 v1.5.8：假人虚空交易在多个轮次之间复用潜影盒。
- 仅将刷怪空气层 `Y=-63` 的天空状态和天光视为遮挡。
- `Y=-64` 与 `Y=-62` 及以上完全保持原版。
- 天光修复也覆盖方块放置后的增量光照更新。
- `voidSkylightFix` 规则默认关闭，可用 `/carpet voidSkylightFix true` 手动开启。

### 安装与使用

将 `carpet-acmax-addition-v1.5.9.jar` 放入服务端 `mods`，替换旧版同名模组。单人游戏放入本地 Fabric 实例的 `mods`。客户端无需安装本扩展，请勿同时安装独立 `fake_player_void_trading`。

需要虚空天光修复时，由管理员开启并保存：

```mcfunction
/carpet voidSkylightFix true
/carpet setDefault voidSkylightFix true
```

放置在地面 `Y=-64` 上的可刷怪空气格实际位于 `Y=-63`。修复只覆盖该刷怪层的天空状态、天光查询和下发数据，其它高度保持原版。

假人虚空交易的已有用法保持不变。先准备可正常运行的装置、假人、交易材料、潜影盒和背包空位，再开启规则、绑定村民并选择交易模式：

```mcfunction
/carpet fakePlayerVoidTrading true
/carpet setDefault fakePlayerVoidTrading true
/voidtrade TradeBot bind
/voidtrade TradeBot trades
/voidtrade TradeBot offer 1 cycle 100
/voidtrade TradeBot offer 1 count 500
/voidtrade TradeBot status
/voidtrade TradeBot stop
```

`cycle` 与 `count` 选择一种启动；规则开启后普通玩家也可使用交易命令。展开堆叠潜影盒至少需要两个普通背包空槽。使用该折跃门装置时，在已安装 CCA 的服务端开启 `/cca GatewayCannotLoadingChunks true`，以避免传送回主岛后等待约 13 秒让村民区块卸载。其他玩家或区块加载器也不能让目标村民区块持续加载。

返程冷却在 `config/fake_player_void_trading.json` 中通过 `returnCooldownSeconds` 设置，默认 2 秒，允许 0–60 秒，修改后重启服务器。

### 附件

- `carpet-acmax-addition-v1.5.9.jar`：安装文件。
- `carpet-acmax-addition-v1.5.9-sources.jar`：源码包，无需放入 mods。
- 两个对应的 `.sha256` 文件：SHA-256 校验值。GitHub 自动生成的源码归档也不用于安装。

本次发布沿用提交 `2dfb3526bccc28fa0e3ea4f3e13ad6fa43b97918` 的[成功 CI 构建](https://github.com/Gufeng-online/carpet-acmax/actions/runs/41328916317)，已核对归档摘要、JAR 版本、依赖与源码一致性；本次未进行游戏内验证。

完整用法见 [README](https://github.com/Gufeng-online/carpet-acmax/blob/main/README.md)，验证范围见 [验证记录](https://github.com/Gufeng-online/carpet-acmax/blob/main/VALIDATION.md)，旧版保留在 [Releases](https://github.com/Gufeng-online/carpet-acmax/releases)。
