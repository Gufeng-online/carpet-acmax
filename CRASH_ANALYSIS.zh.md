# 2026-10-02 客户端崩溃分析

依据：用户提供的 crash-2026-10-02_10.46.40-client.txt。Minecraft 26.2 / Java 25，Auto Void Trading 3.1.10、Carpet ACMAX v1.5、Item Scroller 0.32.1、Quick Shulker 3.2.7、Visible Traders 2.5.1。多人服务器，报告显示 fabric (Velocity)。

## 可以确定的事实

异常为 `IllegalStateException: Accessing LegacyRandomSource from multiple threads`。关键调用链是 `LegacyRandomSource.next → BitRandomSource.nextDouble → Entity.doWaterSplashEffect → Entity.updateFluidInteraction → Villager.tick → ClientLevel.tickEntities → Minecraft.tick`。正在 tick 的实体是无业村民，实体 ID 4810386，位置约 (843.50, 68.82, -625.07)。异常发生在 Render thread；报告的 Thread dumps 只有这条线程，没有提供另一条访问同一随机数的线程。

这是客户端实体入水效果的随机数访问冲突，日志中没有材料不足、容器槽越界、交易报价或扣料异常，也没有 Auto Void Trading、Item Scroller 或 Quick Shulker 的调用帧。报告与末次双材料交易的时间相关，但仅凭这一点不能认定交易自动化导致，也不能排除它触发了与其它 Mod 的交互。更不能仅凭装有某个性能 Mod 就指认其为根因。

## 本地代码检查与本次处理

独立客户端自动化由 Fabric END_CLIENT_TICK 驱动，同步调用 Item Scroller，未创建工作线程，也未访问 RandomSource。整合版假人逻辑运行于服务器 tick；报告来自非内置多人客户端，不能把服务器假人的扣料实现视为客户端堆栈。

本次补充双材料资源不平衡测试，并在独立服务端用实际村民、原版菜单复测：64 绿宝石＋2 书，以 2 绿宝石＋1 书换 4 钻石，最后一笔用尽书后完成共 2 次交易，剩 60 绿宝石、产出 8 钻石，正常收纳返程，没有材料不足报错。64 个空潜影盒只分离一个收纳产物，剩余 63 个保持堆叠。无空盒时 64 绿宝石＋1 书成交一次，4 钻石保留背包，正常返程。

已修复按需拆盒、点击陷阱箱前最低 40 tick 等待，以及加载中的菜单重新选取最便宜匹配附魔书报价；这些修正解决可证实的交易流程问题，不是对随机数并发异常的根因修复。没有修改或吞掉原版随机数异常。

## 尚未验证及后续定位

用户的完整客户端 Mod 组合、真实装置与 Item Scroller/Quick Shulker 点击联动尚未重现此崩溃，因此不能保证不再崩溃。再次发生时应保留崩溃前的 latest.log；若复现，先用仅自动交易与必要前置的独立测试实例重复同一双材料耗尽场景，再逐组加入其它 Mod，用相同交易、村民和装置确定冲突范围。优先需要另一线程调用栈或稳定复现条件，当前报告不足以指认某个 Mod。

原报告 SHA-256：`5cfa32861457a4a251d4ea55939f574b4c77532cd555522222686d1fcdf146d1`。原文件未修改。
