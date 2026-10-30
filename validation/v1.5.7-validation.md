# Carpet-ACMAX v1.5.7

移除 fakePlayerVoidTradingAllowNonOp 规则、翻译及 OP 权限限制。fakePlayerVoidTrading 默认仍关闭，开启后所有玩家可使用绑定、列表、start、offer cycle/count 和旧交易形式。关闭时交易任务停止，status/stop 仍向所有玩家开放。修改 Carpet 规则仍沿用 Carpet 管理权限。

2026-10-19 使用 JDK 25 执行 `test --tests '*TradeChoicesTest' build`，15 项相关测试通过，构建成功。测试使用 NO_PERMISSIONS 命令源检查实际注册命令树：开启后可访问各功能；关闭后禁止绑定、列表和启动交易，允许 status/stop；再次开启恢复访问。测试也确认删除的规则字段不存在。未进行真实非 OP 客户端游戏内验证。

JAR 检查确认规则字段、权限判断引用及中英文说明已删除，版本为 v1.5.7。成品与源码 JAR 和 SHA-256 在 releases。日志 validation/v1.5.7-build.log。

修改前备份：../carpet-acmax-backups/carpet-acmax-before-open-commands-20261019-225212.zip，附 SHA-256。旧版成品仍保留在 releases。世界配置中如有 fakePlayerVoidTradingAllowNonOp 的旧保存项，该规则已失效，可删除。

## 2026-10-20 发布前验证

JDK 25 执行 `test build` 成功，全部 32 项测试通过，失败和错误均为 0。成品和源码包已与构建输出核对并生成 SHA-256。日志保留在本地 `validation/v1.5.7-release-build.log`。本次整理提交与发布说明，没有额外修改运行逻辑；真实非 OP 客户端游戏内验证仍未完成。GitHub 更新前的回滚标签为 `backup/pre-v1.5.7-20261020`。
