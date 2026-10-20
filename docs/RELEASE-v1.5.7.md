## Carpet-ACMAX v1.5.7

适用于 **Minecraft 26.2 / Fabric / Java 25**。需要 Fabric Loader 0.19.3+、Carpet 26.2 和对应 Fabric API。

### 更新内容

- 移除 `fakePlayerVoidTradingAllowNonOp` 规则。
- 开启 `fakePlayerVoidTrading` 后，所有玩家均可使用假人绑定、交易列表、按轮次/数量交易、状态和停止指令，无需 OP。
- 交易总开关默认仍为关闭；关闭时停止正在运行的任务，所有玩家仍可查询状态或停止任务。
- 修改 Carpet 规则仍需要相应管理权限。

### 安装与使用

将 `carpet-acmax-addition-v1.5.7.jar` 放入服务端 `mods`，替换旧版本。单人游戏放入本地 Fabric 实例的 `mods`。客户端无需安装本扩展。

管理员开启并保存交易功能：

```mcfunction
/carpet fakePlayerVoidTrading true
/carpet setDefault fakePlayerVoidTrading true
```

为已创建并备好材料的 Carpet 假人绑定村民、查看报价，再按需要选择一种交易模式：

```mcfunction
/voidtrade TradeBot bind
/voidtrade TradeBot trades
/voidtrade TradeBot offer 1 cycle 100
/voidtrade TradeBot offer 1 count 500
/voidtrade TradeBot status
/voidtrade TradeBot stop
```

仍需要可正常运行的虚空交易装置。完整用法见 [README](https://github.com/Gufeng-online/carpet-acmax/blob/main/README.md)。

### 升级说明

原有 `fakePlayerVoidTradingAllowNonOp` 配置项不再生效，无需再开启权限规则。需要恢复旧版时参见 [回滚说明](https://github.com/Gufeng-online/carpet-acmax/blob/main/docs/ROLLBACK.zh.md)。

### 附件

- `carpet-acmax-addition-v1.5.7.jar`：安装文件。
- `carpet-acmax-addition-v1.5.7-sources.jar`：源码包，无需放入 mods。
- 对应 `.sha256`：文件校验值。
