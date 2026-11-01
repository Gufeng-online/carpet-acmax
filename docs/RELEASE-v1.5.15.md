## Carpet-ACMAX v1.5.15

适用于 **Minecraft 26.2 / Fabric / Java 25**。需要 Fabric Loader 0.19.3+、Carpet 26.2 和对应 Fabric API。

### 更新内容

- 仅将刷怪空气层 `Y=-63` 的天空状态和天光视为遮挡。
- `Y=-64` 与 `Y=-62` 及以上完全保持原版。
- 所有光照包在最终序列化前再次清除 `Y=-63`，覆盖方块放置后的增量光照更新。
- `voidSkylightFix` 规则默认关闭，可用 `/carpet voidSkylightFix true` 手动开启。

### 根因

放置在地面 `Y=-64` 上的可刷怪空气格实际位于 `Y=-63`。修复只覆盖该刷怪层的天空状态、天光查询和下发数据，其它高度保持原版。

### 附件

- `carpet-acmax-addition-v1.5.15.jar`：安装文件。
- `carpet-acmax-addition-v1.5.15-sources.jar`：源码包，无需放入 mods。
