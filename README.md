# Carpet-ACMAX

ACMAX 服务器自用的 [Carpet](https://github.com/gnembon/fabric-carpet) 扩展。
**纯服务端规则**：只装在服务端即可生效，客户端可以不装（除特别标注的规则外）。

## 版本与分支

| 分支 | Minecraft | Fabric API | Carpet 依赖 | 模组版本 |
| --- | --- | --- | --- | --- |
| `main` | 26.2 | 0.158.0+26.2 | 26.2 | v1.2 |
| `26.1.2` | 26.1.2 | 0.155.2+26.1.2 | 26.1 | v1.2 |

两个分支的功能完全一致，只有 Minecraft / Fabric API / Carpet 的目标版本不同。

## 规则列表

规则分类为 `ACMAX`，通过 `/carpet` 指令或 `config/carpet-acmax.conf` 修改。

| 规则 | 默认值 | 说明 |
| --- | --- | --- |
| `endGatewayCooldown` | `false` | 禁用末地折跃门传送之间的 2 秒冷却，允许无限制连续传送。 |
| `itemEntityAlarm` | `0` | 掉落物堆积警报：某一区块内的掉落物堆数达到阈值时向全服通报坐标与数量。预设档位 `1000 / 2000 / 4000 / 6000`，也可填任意正整数，`0` 为关闭。 |
| `railForceStatePlacement` | `false` | 铁轨强制状态放置：只对「轻松放置」（Litematica 协议 V3）放置的铁轨生效。铁轨按投影形状放置（含悬空、无支撑的铁轨），且本次放置不会让周围铁轨变形。事务结束后立即恢复原版行为，红石换向、无支撑掉落等特性完全不受影响。 |
| `cactusWrenchRailEnhancement` | `false` | 仙人掌扳手铁轨增强（需同时开启 `flippinCactus`）：手持仙人掌右键铁轨可依次切换所有形态，且不触发方块 / 邻居更新。 |
| `trialSpawnerIntervalFix` | `false` | 修复试炼刷怪笼生成间隔：把试炼密室 `chamber_8`、`encounter_4` 结构中 160gt（8 秒）的生成间隔修正为正常的 20gt（1 秒）。 |

## 构建

需要 **JDK 25**（Minecraft 26.x 要求）。

```bash
./gradlew build
```

产物：`build/libs/carpet-acmax-addition-v1.2.jar`（放进服务端 `mods/` 即可，客户端无需安装）。

## 运行依赖

- Minecraft 26.2（`main` 分支）或 26.1.2（`26.1.2` 分支）
- Fabric Loader ≥ 0.15.0
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Carpet](https://modrinth.com/mod/carpet)

## License

MIT