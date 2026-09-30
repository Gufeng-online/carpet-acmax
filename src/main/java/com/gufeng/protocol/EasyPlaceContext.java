package com.gufeng.protocol;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

import java.util.HashSet;
import java.util.Set;

/**
 * 轻松放置上下文 —— 铁轨协议放置期间的瞬时保护（纯服务端）
 *
 * 用 ThreadLocal 记录【当前正在被轻松放置的那一格铁轨位置】。
 * 保护范围严格限定在 BlockItem.place 执行期间（beginPlacement…endPlacement 之间）：
 *   - 放置瞬间允许悬浮（canSurvive 放行）
 *   - 放置瞬间阻止原版 updateDir / shouldBeRemoved 把强制形状改回或让铁轨掉落
 * 一旦放置事务结束，endPlacement 立即清理该位置 —— 之后周边任何更新都走原版逻辑。
 *
 * 每个标记带超时兜底：即使 place 因异常未能走到 endPlacement，
 * 标记也会自动过期，不会残留成“永久保护”。
 */
public class EasyPlaceContext {

    /** 标记最长存活时间（毫秒），防止异常路径导致永久残留 */
    private static final long STALE_MILLIS = 1000L;

    /** 当前线程正在放置的强制铁轨位置集合（服务端主线程） */
    private static final ThreadLocal<Set<PosKey>> PLACING = ThreadLocal.withInitial(HashSet::new);

    private record PosKey(String dimension, int x, int y, int z, long expiresAt) {

        static PosKey of(LevelReader level, BlockPos pos) {
            return new PosKey(dimensionOf(level), pos.getX(), pos.getY(), pos.getZ(),
                    System.currentTimeMillis() + STALE_MILLIS);
        }

        boolean matches(LevelReader level, BlockPos pos) {
            return dimension().equals(dimensionOf(level))
                    && x() == pos.getX() && y() == pos.getY() && z() == pos.getZ();
        }
    }

    /** 开始放置：把该格标记为“正在被强制放置的铁轨” */
    public static void beginPlacement(LevelReader level, BlockPos pos) {
        PLACING.get().add(PosKey.of(level, pos));
    }

    /** 结束放置：清理该格标记，之后恢复原版行为 */
    public static void endPlacement(LevelReader level, BlockPos pos) {
        Set<PosKey> set = PLACING.get();
        prune(set);
        set.removeIf(k -> k.matches(level, pos));
        if (set.isEmpty()) {
            PLACING.remove();
        }
    }

    /** 该位置当前是否正处于被强制放置的事务中 */
    public static boolean isPlacing(LevelReader level, BlockPos pos) {
        Set<PosKey> set = PLACING.get();
        prune(set);
        return set.stream().anyMatch(k -> k.matches(level, pos));
    }

    /** 当前线程是否正处在一个轻松放置事务中（同一维度内的任意位置） */
    public static boolean isTransactionActive(LevelReader level) {
        Set<PosKey> set = PLACING.get();
        prune(set);
        String dim = dimensionOf(level);
        return set.stream().anyMatch(k -> k.dimension().equals(dim));
    }

    private static void prune(Set<PosKey> set) {
        long now = System.currentTimeMillis();
        set.removeIf(k -> k.expiresAt() < now);
        if (set.isEmpty()) {
            PLACING.remove();
        }
    }

    private static String dimensionOf(LevelReader level) {
        if (level instanceof Level l) {
            return l.dimension().identifier().toString();
        }
        return "";
    }
}