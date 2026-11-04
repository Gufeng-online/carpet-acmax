# Carpet-ACMAX 验证与版本记录

## v1.5.8 → v1.5.9：2026-11-05 Release 发布

本地工程由 v1.5.8 快进同步至远端已有的 v1.5.9；远端功能版本保持 v1.5.9，本次发布资料修订为 v1.5.9-docs.1。沿用远端提交 `2dfb3526bccc28fa0e3ea4f3e13ad6fa43b97918` 的源码与成品，未修改 Java 源码、资源或构建配置。v1.5.9 新增默认关闭的 `voidSkylightFix`，针对 `Y=-63` 刷怪空气层的天空状态、天光查询和下发数据；保留 v1.5.8 跨轮复用潜影盒等假人交易行为。

[GitHub CI #32](https://github.com/Gufeng-online/carpet-acmax/actions/runs/41328916317) 的 `./gradlew build` 已成功，本次直接使用该 CI 输出，不重复编译。CI 归档 SHA-256 为 `8a0a9b23e85555391029e3623c0a544345089bb1601a01628e01daca4f14d3a1`，与 GitHub artifact digest 相符；安装 JAR 的 Mod 版本为 v1.5.9，要求 Minecraft 26.2、Fabric Loader >=0.19.3、Java >=25，并已包含天光修复类及 Mixin 配置。源码 JAR 中全部 main/client Java 文件逐项匹配该构建提交。安装包 SHA-256 为 `a368b937783a35a07023aaa8baf508a1ad6e5bce72b715bd6612a425633733cf`；源码包 SHA-256 为 `e720f46422b20e981b885d7d305ce4048fb2d2bdffed956183bfb0fc6ff009f5`。本次未进行游戏内天光、刷怪或装置验证，不将 CI 构建通过视为实机验证。

中英文 README 保留原章节与规则表，补齐天光开关、保存默认值和 v1.5.9 下载链接；详细交易说明更新安装文件名。Release 说明包含兼容性、安装、天光开关、假人交易命令、CCA 设置与附件用途。工作区文档版本 1.0.14 → 1.0.15，MODS.md 同步版本、功能及用法。v1.5.9 安装 JAR、源码 JAR、两份校验值、发布辅助资料和核验结果均为首次创建，旧版成品、发布与标签保留。

修改前备份：`../carpet-acmax-backups/carpet-acmax-v1.5.8-before-v1.5.9-release-2026-11-05-8841e39f/`。`project-v1.5.8.zip` 保存本地源码、构建配置、说明与已有成品，295 个文件 SHA-256 核对通过；`workspace-docs/` 保存工作区文档，两个 Git bundle 保留本地及远端完整历史，`project-remote-v1.5.9-before-release-docs.zip` 保存发布资料修改前的远端文件，`remote-before-release.json` 保存更新前发布和标签状态。所有备份及清单为首次创建，核验摘要见同目录 `ARTIFACT_VERIFICATION.json`。

发布标签 `v1.5.9` 指向原 CI 构建提交 `2dfb3526bccc28fa0e3ea4f3e13ad6fa43b97918`，发布资料使用后续普通提交维护。更新前远端 main 同样保存为带说明的标签 `backup/pre-v1.5.9-release-20261105-8841e39f`；作者、提交者和新标签署名使用已认证用户 CNTrident，原有历史和版本标签不移动。回滚方法见 [回滚说明](docs/ROLLBACK.zh.md)。

## v1.5.8：历史功能与验证

假人交易同步独立客户端 Auto Void Trading 3.1.16 的收纳行为：新目标优先空盒，跨轮持续使用已选盒子，装不下剩余产物才换盒；只有仍有待存物品时才按需展开一个堆叠盒。收纳扫描全部同组件产物，包括上一轮留在背包的部分。空盒或容量不足时保留背包并继续流程。保持盒子数量、物品数量和组件，禁止将潜影盒产物嵌套放入另一潜影盒。

旧式未指定 offer 的交易按假人实际折扣价格选择匹配报价；明确指定 offer 仍使用所选报价。达到轮数/数量目标后，也会完成收纳、剩余冷却及返程，再结束任务。默认 2 秒可配置冷却、材料不足正常结束及双材料交易保留。指令对所有玩家开放的行为未改变。

2026-10-23 使用 JDK 25 执行 `test build`，38 项测试全部通过。新增回归覆盖：连续三批各 12 件同组件不可堆叠产物，第一盒依次收纳 12、24、27 件，第二盒收纳剩余 9 件；仅展开两个盒子，64 个盒子总数与产物组件保持不变；当前部分填充盒优先于另一个空盒；无产物时不拆盒；拆盒空位不足时先使用已有盒并保留溢出；要求完整收纳失败时不提交部分移动；禁止潜影盒嵌套。已有付款、价格、命令权限及返程计时测试通过。

测试过程中修正了一处测试夹具的组件匹配（显式空容器组件与默认空组件不同）；最终构建成功，日志在 validation/v1.5.8-build.log。没有新增客户端依赖，也没有修改独立客户端项目。本次未进行真实游戏内装置、假人跨轮交互及最终返程的端到端验证。

修改前备份：../carpet-acmax-backups/carpet-acmax-before-client-storage-sync-20261023-234306.zip，附 SHA-256。旧版 JAR 保留；v1.5.8 成品、源码 JAR 及 SHA-256 位于 releases。2026-10-23 构建时尚未推送或发布 GitHub。

## v1.5.7 → v1.5.8：2026-10-31 GitHub 同步

将已经完成的本地 v1.5.8 同步到 `Gufeng-online/carpet-acmax` 的 `main`，沿用 v1.5.8，不另增功能版本。原版本为 v1.5.7，远端原提交为 `f9d82587bf0762a0d0d9ef952dd433790facfd66`。优化和更新包括首次优先全空盒、同一任务跨轮持续填充当前盒、按需逐个展开堆叠盒、重试背包同组件溢出产物、按实际折扣选价，以及达到目标后完成收纳与返程。交易源码沿用本地已经完成的改动；本次补充中英文使用边界、成品下载入口和回滚说明。

JDK 25 重新执行 `test --offline --console=plain --rerun-tasks`：38 项测试全部通过，失败和错误为 0；随后执行 `build --offline --console=plain` 成功，复用相同源码对应的测试结果。构建日志：`validation/v1.5.8-github-push-20261031.log`。安装 JAR、源码 JAR、Minecraft 26.2 / Java 25 版本声明和对应 SHA-256 均核对。未进行真实游戏装置、假人菜单及返程的端到端验证。

额外库存模拟确认：同一任务每次收纳 1 本附魔书、连续 30 次时，只使用 2 个盒子，分别为 27 本和 3 本。每轮 12 本、连续 3 轮时同样持续填盒，结果为 27 本和 9 本。重新启动任务会重置选盒记录。拆堆叠盒仍要求至少两个普通背包空槽：若产物先占用拆盒所需空位，即使仍有堆叠空盒也可能提前停止；这是保留的既有边界，未在 v1.5.8 修复。

推送前另行备份包含未提交源码、构建配置、说明及已有成品的完整项目：`../carpet-acmax-backups/carpet-acmax-v1.5.8-before-github-2026-10-31-3c59962d/project-v1.5.8.zip`；同目录的 `history-before-v1.5.8-push.bundle` 已通过 Git 校验，`BACKUP.json` 记录两个文件的 SHA-256。备份文件及本节为首次创建。版本记录沿用本文件，旧版 v1.5.7 验证说明保存在 `validation/v1.5.7-validation.md`。

更新前远端代码的回滚标签为 `backup/pre-v1.5.8-20261031`；版本标签为 `v1.5.8`。Git 提交和标签使用已认证用户 CNTrident 的署名。本次仅推送仓库源码、说明、成品和标签，不创建 GitHub Release。

## 2026-10-31：CCA 卸载设置说明（文档版本 1.0.6 → 1.0.7）

Mod 版本保持 v1.5.8，源码、构建配置、JAR 和校验值保持不变。中英文 README 的装置准备部分，以及详细使用说明，补充 `/cca GatewayCannotLoadingChunks true`：未开启时，假人传送回主岛后可能在 `WAIT_GATEWAY` 等待约 13 秒，直到外岛村民区块真正卸载才交易。根目录 MODS.md 同步这项用法，工作区文档版本由 1.0.6 升级至 1.0.7。

验证依据：用户实测开启该规则后等待消失；本地游戏日志记录规则名称为 `GatewayCannotLoadingChunks`，并记录其默认值已保存为 true。核对主分支中英文 README 的当前版本和下载文件名均为 v1.5.8；旧版发布、标签和回滚记录独立保留。本次为纯文档更新，检查命令大小写、中英文一致性、链接及 Git diff，不重复构建或运行测试。

修改前备份：`../carpet-acmax-backups/carpet-acmax-v1.5.8-docs-1.0.6-before-cca-readme-2026-10-31-e2fa22f7/`。其中 `project-v1.5.8.zip` 保存项目源码、配置、说明和已有成品，`history-before-cca-readme.bundle` 已通过 Git 校验，`workspace-docs/` 保存修改前工作区说明，SHA-256 见 `BACKUP.json`。备份文件及本节为首次创建。更新前远端提交 `0cd6044156b673aa428e11f0d228e41235cb6f7f` 的回滚标签为 `backup/pre-cca-readme-20261031`；可恢复该标签文件后新建普通提交回滚。

用户进一步确认旧版显示来自仓库右侧的 Releases / Latest，本批次同时将 Latest 更新为 v1.5.8。新增 `docs/RELEASE-v1.5.8.md` 为首次创建；使用已存在且不移动的 v1.5.8 标签及原已验证成品发布，附安装 JAR、源码 JAR 和两份 SHA-256。旧版 Release 和回滚标签保留。发布说明同步 CCA 设置和卸载等待行为。
