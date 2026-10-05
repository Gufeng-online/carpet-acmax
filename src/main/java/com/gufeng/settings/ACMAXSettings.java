package com.gufeng.settings;

import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;

public class ACMAXSettings {
    public static final String ACMAX = "ACMAX";

    @Rule(categories = {ACMAX, RuleCategory.FEATURE, RuleCategory.SURVIVAL}, options = {"true", "false"}, strict = false)
    public static boolean fakePlayerVoidTrading = false;

    @Rule(categories = {ACMAX, RuleCategory.FEATURE, RuleCategory.SURVIVAL}, options = {"true", "false"}, strict = false)
    public static boolean fakePlayerVoidTradingAllowNonOp = false;

    @Rule(
            categories = {ACMAX, RuleCategory.FEATURE, RuleCategory.SURVIVAL},
            options = {"true", "false"},
            strict = false
    )
    public static boolean endGatewayCooldown = false;

    /**
     * 掉落物堆积警报阈值（堆数）。
     * 预设档位：1000 / 2000 / 4000 / 6000；也可自定义任意正整数；0 表示关闭（默认）。
     */
    @Rule(
            categories = {ACMAX, RuleCategory.FEATURE, RuleCategory.SURVIVAL},
            options = {"1000", "2000", "4000", "6000"},
            strict = false
    )
    public static int itemEntityAlarm = 0;

    @Rule(
            categories = {ACMAX, RuleCategory.FEATURE, RuleCategory.CREATIVE},
            options = {"true", "false"},
            strict = false
    )
    public static boolean railForceStatePlacement = false;

    @Rule(
            categories = {ACMAX, RuleCategory.FEATURE, RuleCategory.SURVIVAL},
            options = {"true", "false"},
            strict = false
    )
    public static boolean cactusWrenchRailEnhancement = false;

    @Rule(
            categories = {ACMAX, RuleCategory.FEATURE, RuleCategory.BUGFIX},
            options = {"true", "false"},
            strict = false
    )
    public static boolean trialSpawnerIntervalFix = false;
}
