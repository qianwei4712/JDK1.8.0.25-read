package java.util;

/**
 * Map 进一步在其键上提供了排序。
 * map是根据其键的可比自然排序（key实现Comparable接口）进行的，或者通常是通过在排序的映射创建时提供的{@link Comparator} 比较器进行排序的。
 * 在迭代排序后的地图的集合视图时（由 entrySet， keySet 和  values方法返回）反映了此顺序。
 * 提供了一些附加的操作以利用顺序。 （和{@link SortedSet}类似。）<p>
 *
 * 插入键值对的 key都必须实现 {@code Comparable} 接口，或者此 SortedMap必须指定比较器。
 * 此外，所有这样的键必须是<em>相互可比较的</em>：{@code k1.compareTo（k2）}（或{@code comparator.compare（k1，k2）}）。
 * 否则抛出{@code ClassCastException}.<p>
 *
 * 请注意，如果排序图要正确实现{@code Map}接口，则排序图（无论是否提供显式比较器）所维护的顺序必须<em>与equals </em>一致。
 * （请参见 {@code Comparable}接口或{@code Comparator}接口，以获取<em>与equals </em>一致的精确定义。）
 * 之所以这样，是因为{@code Map}接口是在{@code equals} 操作的术语，但是排序后的映射使用其 {@code compareTo}（或{@code compare}）方法执行所有键比较，
 * 因此，被该方法视为相等的两个键是：从排序图的角度来看相等。TreeMap 的行为是明确定义的，即使其顺序与equals不一致；它只是不能遵守{@code Map}接口的一般约束。
 *
 * @param <K> 此 map维护的 key的类型
 * @param <V> 映射值 value的类型
 * @since 1.2
 */
public interface SortedMap<K,V> extends Map<K,V> {
    /**
     * @return 返回用于在此 map中对键进行排序的比较器；如果此映射使用其键的{@linkplain Comparable }自然排序，则返回 null。
     */
    Comparator<? super K> comparator();

    /**
     * @param fromKey 返回 map中键的低端点（包括边界）
     * @param toKey 返回 map中键的高端点（不包括）
     * @return 此地图部分的视图，其键范围为 fromKey（包括边界） 到 toKey（不包括）
     * @throws ClassCastException 如果 fromKey 和 toKey 无法使用此映射的比较器相互比较（或者，如果映射没有比较器，则使用自然顺序）。
     *         如果 fromKey 或 toKey 无法与地图中当前的键进行比较，则实现可能会（但并非必须）抛出此异常。
     * @throws NullPointerException 如果 fromKey 或 toKey 为空，并且此映射不允许空键
     * @throws IllegalArgumentException 如果 fromKey 大于 toKey；或此地图本身的范围受到限制并且 fromKey 或 toKey 位于范围之外
     */
    SortedMap<K,V> subMap(K fromKey, K toKey);

    /**
     * @param toKey 返回 map中键的高端点（不包括）
     * @return 此地图部分的视图，其键范围为起始节点到 toKey（不包括）
     * @throws ClassCastException 如果 toKey 与该地图的比较器不兼容（或者，如果地图没有比较器，则如果 toKey 不实现{@link Comparable}）。
     *        如果 toKey 无法与地图中当前的键进行比较，则实现可能会（但并非必须）抛出此异常。
     * @throws NullPointerException 如果 toKey 为空，并且此映射不允许空键
     * @throws IllegalArgumentException 如果位置超出范围
     */
    SortedMap<K,V> headMap(K toKey);

    /**
     * @param fromKey 返回 map中键的低端点（包括边界）
     * @return 此地图部分的视图，其键范围为 fromKey（包括边界） 到结束
     * @throws ClassCastException 如果 fromKey 与该地图的比较器不兼容（或者，如果地图没有比较器，则如果 fromKey 不实现{@link Comparable}）。
     *        如果 fromKey 无法与地图中当前的键进行比较，则实现可能会（但并非必须）抛出此异常。
     * @throws NullPointerException 如果 fromKey 为空，并且此映射不允许空键
     * @throws IllegalArgumentException 如果位置超出范围
     */
    SortedMap<K,V> tailMap(K fromKey);

    /**
     * @return 当前在此地图中的第一个（最小）键
     * @throws NoSuchElementException 如果此地图为空
     */
    K firstKey();

    /**
     * @return 当前在此地图中的最后一个（最大）键
     * @throws NoSuchElementException 如果此地图为空
     */
    K lastKey();

    /**
     * @return 此 map中包含的 key的set集合，以 key比较后升序排列
     */
    Set<K> keySet();

    /**
     * @return 此 map中包含的 value值的集合，按 key比较后升序排列
     */
    Collection<V> values();

    /**
     * @return 此 map中包含的键值对的集合视图，按key比较后升序排列
     */
    Set<Map.Entry<K, V>> entrySet();
}
