package com.gufeng.protocol;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

import java.util.HashSet;
import java.util.Set;

/**
 * 轻松放置上下文 —— 铁轨协议放置期间的瞬时保护
 *
 * 与旧版“永久标记”不同，这里只用 ThreadLocal 记录【当前正在被轻松放置的那一格铁轨位置】。
 * 保护范围严格限定在 BlockItem.place 执行期间（beginPlacement…endPlacement 之间）：
 *   - 放置瞬间允许悬浮（canSurvive 放行）
 *   - 放置瞬间阻止原版 updateDir / shouldBeRemoved 把强制形状改回或让铁轨掉落
 * 一旦放置事务结束，endPlacement 立即清理该位置 —— 之后周边任何更新（邻居变化、
 * 支撑方块被破坏等）都走原版逻辑，铁轨正常掉落 / 变形，即“周围一更新就恢复”。
 *
 * 该机制是线程安全的（服务端主线程），且不残留任何跨刻的永久状态。
 */
public class EasyPlaceContext {

    /** 当前线程正在放置的强制铁轨位置集合（同一刻最多一个，用 Set 兜底防重入） */
    private static final ThreadLocal<Set<PosKey>> PLACING = ThreadLocal.withInitial(HashSet::new);

    private record PosKey(String dimension, int x, int y, int z) {
        static PosKey of(LevelReader level, BlockPos pos) {
            if (level instanceof Level l) {
                return new PosKey(l.dimension().identifier().toString(), pos.getX(), pos.getY(), pos.getZ());
            }
            return new PosKey("", pos.getX(), pos.getY(), pos.getZ());
        }
    }

    /** 开始放置：把该格标记为“正在被强制放置的铁轨” */
    public static void beginPlacement(LevelReader level, BlockPos pos) {
        PLACING.get().add(PosKey.of(level, pos));
    }

    /** 结束放置：清理该格标记，之后恢复原版行为 */
    public static void endPlacement(LevelReader level, BlockPos pos) {
        Set<PosKey> set = PLACING.get();
        set.remove(PosKey.of(level, pos));
        if (set.isEmpty()) {
            PLACING.remove();
        }
    }

    /** 该位置当前是否正处于被强制放置的事务中 */
    public static boolean isPlacing(LevelReader level, BlockPos pos) {
        return PLACING.get().contains(PosKey.of(level, pos));
    }
}
