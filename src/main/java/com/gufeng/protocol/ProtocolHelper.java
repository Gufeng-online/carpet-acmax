package com.gufeng.protocol;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * 协议辅助类 - 将方块状态编码/解码到 hitVec.x 中
 * 
 * 协议编码方式（与 Carpet 协议兼容）：
 * - 用 hitVec.x 相对于方块原点的偏移编码协议信息
 * - relativeHitX >= 2.0 表示包含协议值
 * - protocolValue = ((int)relativeHitX - 2) >>> 1
 * - 最低 bit0 留给浮点误差兼容
 */
public class ProtocolHelper {

    /**
     * 从 hitVec 相对于方块原点的 x 偏移中解码协议值
     */
    public static int decodeProtocolValueFromHitX(Vec3 hitPos, BlockPos blockPos) {
        double relativeHitX = hitPos.x - blockPos.getX();
        return ((int) relativeHitX - 2) >>> 1;
    }

    /**
     * 获取 hitPos 相对于 blockPos 的 x 偏移
     */
    public static double getRelativeHitX(Vec3 hitPos, BlockPos blockPos) {
        return hitPos.x - blockPos.getX();
    }

    /**
     * 将协议值编码进 hitVec（相对于方块原点的偏移）
     */
    public static Vec3 encodeProtocolValueToHitVec(int protocolValue, Vec3 hitVec) {
        double newX = hitVec.x + (double) ((protocolValue << 1) + 2);
        return new Vec3(newX, hitVec.y, hitVec.z);
    }

    /**
     * 判断 hitVec 是否包含协议值
     */
    public static boolean hasProtocolValue(Vec3 hitPos, BlockPos blockPos) {
        double relativeHitX = getRelativeHitX(hitPos, blockPos);
        return relativeHitX >= 2.0;
    }
}
