package com.gufeng.settings;

import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;

public class ACMAXSettings {
    public static final String ACMAX = "ACMAX";

    @Rule(
            categories = {ACMAX, RuleCategory.FEATURE, RuleCategory.SURVIVAL},
            options = {"true", "false"},
            strict = false
    )
    public static boolean endGatewayCooldown = false;

    @Rule(
            categories = {ACMAX, RuleCategory.FEATURE, RuleCategory.SURVIVAL},
            options = {"true", "false"},
            strict = false
    )
    public static boolean itemEntityAlarm = false;

    @Rule(
            categories = {ACMAX, RuleCategory.FEATURE, RuleCategory.CREATIVE},
            options = {"true", "false"},
            strict = false
    )
    public static boolean railForceStatePlacement = false;

    @Rule(
            categories = {ACMAX, RuleCategory.FEATURE, RuleCategory.CREATIVE},
            options = {"true", "false"},
            strict = false
    )
    public static boolean disableRailShapeUpdate = false;

    @Rule(
            categories = {ACMAX, RuleCategory.FEATURE, RuleCategory.SURVIVAL},
            options = {"true", "false"},
            strict = false
    )
    public static boolean cactusWrenchRailEnhancement = false;
}
