package com.gufeng.protocol;

import net.minecraft.world.level.block.state.properties.RailShape;

/**
 * 铁轨协议辅助类 - RailShape 与协议值间的编解码
 */
public class RailProtocol {

    /**
     * 普通铁轨 10 种形状：0~9
     */
    public static final int RAIL_SHAPE_MASK = 0b0000_1111;

    /**
     * 动力/探测铁轨 6 种形状：0~5
     */
    public static final int POWERED_RAIL_SHAPE_MASK = 0b0000_0111;

    /**
     * RailShape → 协议值（普通铁轨）
     */
    public static int toProtocolValue(RailShape shape) {
        return shape.ordinal() & RAIL_SHAPE_MASK;
    }

    /**
     * 协议值 → RailShape（普通铁轨，10种）
     */
    public static RailShape fromProtocolValue(int protocolValue) {
        return RailShape.values()[protocolValue % 10];
    }

    /**
     * RailShape → 协议值（动力/探测铁轨，6种）
     */
    public static int poweredToProtocolValue(RailShape shape) {
        return shape.ordinal() & POWERED_RAIL_SHAPE_MASK;
    }

    /**
     * 协议值 → RailShape（动力/探测铁轨，6种）
     */
    public static RailShape poweredFromProtocolValue(int protocolValue) {
        return RailShape.values()[protocolValue % 6];
    }
}
