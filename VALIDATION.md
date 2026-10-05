# Carpet-ACMAX v1.5.6

新增 ACMAX 分类 Carpet 规则 fakePlayerVoidTradingAllowNonOp，默认 false。false 时全部 /voidtrade 子指令要求原有 OP 权限（等级至少2），true 时全部玩家可用。包括 bind/trades/start/offer cycle/count/status/stop 和旧命令形式。控制台沿用原权限。交易功能仍受 fakePlayerVoidTrading 总开关控制。

权限判断在根命令节点动态读取规则。权限规则变化与功能总开关变化共用下一次服务器 tick 的在线玩家指令树刷新，无需重登；Carpet 自身管理权限不改变。权限规则仅控制指令访问，不中止此前合法启动的任务。

仅运行相关 TradeChoicesTest，14 项通过，构建成功。新增测试覆盖默认关闭、关闭拒绝非OP/允许OP、开启允许双方、再次关闭恢复限制，以及 finally 恢复测试状态。日志 validation/v1.5.6-build.log，成品与 SHA-256 在 releases。JAR 确认只有最终 AllowNonOp 规则，没有临时 OpOnly 规则。尚未使用实际非OP客户端端到端验证权限切换和补全刷新。

修改前备份 ../carpet-acmax-backups/carpet-acmax-code-20261004-003735.zip；旧成品在 server-v1.5.5-before-v1.5.6。独立客户端没有修改。此前验证见 validation/v1.5.5-validation.md。

## 2026-10-05 GitHub 上传前验证

JDK 25 执行 `test build` 成功，全部 31 项测试通过，失败/错误为 0。本次仅整理提交和回滚说明，没有修改运行逻辑。实际非 OP 客户端权限切换的端到端验证仍未完成。诊断日志、构建缓存和测试世界保留本地。回滚步骤见 `docs/ROLLBACK.zh.md`。
