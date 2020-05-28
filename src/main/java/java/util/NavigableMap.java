package java.util;

/**
 * @param <K> 此地图维护的key键的类型
 * @param <V> 映射值value的类型
 * @since 1.6
 */
public interface NavigableMap<K,V> extends SortedMap<K,V> {
    /**
     * @param key the key
     * @return 返回严格小于给定键的最大键值对，或者如果没有这样的键。
     * @throws ClassCastException 如果指定的键无法与当前map中的键进行比较
     * @throws NullPointerException 如果指定的键为null，并且此映射不允许使用null键
     */
    Map.Entry<K,V> lowerEntry(K key);

    /**
     * @param key the key
     * @return 返回严格小于给定键的最大键值对，或者如果没有这样的键。
     * @throws ClassCastException 如果指定的键无法与当前map中的键进行比较
     * @throws NullPointerException 如果指定的键为null，并且此映射不允许使用null键
     */
    K lowerKey(K key);

    /**
     * @param key the key
     * @return 返回小于或等于给定键的最大键值对，或者如果没有这样的键。
     * @throws ClassCastException 如果指定的键无法与当前map中的键进行比较
     * @throws NullPointerException 如果指定的键为null，并且此映射不允许使用null键
     */
    Map.Entry<K,V> floorEntry(K key);

    /**
     * @param key the key
     * @return 返回小于或等于给定键的最大键，或者如果没有这样的键。
     * @throws ClassCastException 如果指定的键无法与当前map中的键进行比较
     * @throws NullPointerException 如果指定的键为null，并且此映射不允许使用null键
     */
    K floorKey(K key);

    /**
     * @param key the key
     * @return 返回大于或等于给定键的最小键值对，或者如果没有这样的键。
     * @throws ClassCastException 如果指定的键无法与当前map中的键进行比较
     * @throws NullPointerException 如果指定的键为null，并且此映射不允许使用null键
     */
    Map.Entry<K,V> ceilingEntry(K key);

    /**
     * @param key the key
     * @return 返回大于或等于给定键的最小键，或者如果没有这样的键。
     * @throws ClassCastException 如果指定的键无法与当前map中的键进行比较
     * @throws NullPointerException 如果指定的键为null，并且此映射不允许使用null键
     */
    K ceilingKey(K key);

    /**
     * @param key the key
     * @return 返回严格大于给定键的最小键值对，或者如果没有这样的键。
     * @throws ClassCastException 如果指定的键无法与当前map中的键进行比较
     * @throws NullPointerException 如果指定的键为null，并且此映射不允许使用null键
     */
    Map.Entry<K,V> higherEntry(K key);

    /**
     * @param key the key
     * @return 返回严格大于给定键的最小键，或者如果没有这样的键。
     * @throws ClassCastException 如果指定的键无法与当前map中的键进行比较
     * @throws NullPointerException 如果指定的键为null，并且此映射不允许使用null键
     */
    K higherKey(K key);

    /**
     * @return 返回第一个（最小）键值对，如果不存在返回null
     */
    Map.Entry<K,V> firstEntry();

    /**
     * @return 返回最后一个（最大）键值对，如果不存在返回null
     */
    Map.Entry<K,V> lastEntry();

    /**
     * @return 返回第一个（最小）键值对并移除，如果不存在返回null
     */
    Map.Entry<K,V> pollFirstEntry();

    /**
     * @return 返回最后一个（最大）键值对并移除，如果不存在返回null
     */
    Map.Entry<K,V> pollLastEntry();

    /**
     * @return 返回此 map中包含的映射的逆序视图。
     */
    NavigableMap<K,V> descendingMap();

    /**
     * @return 返回一个Navigable的key的集合
     */
    NavigableSet<K> navigableKeySet();

    /**
     * @return 返回一个Navigable的key的倒序集合
     */
    NavigableSet<K> descendingKeySet();

    /**
     * @param fromKey 返回 map中键的起始点
     * @param fromInclusive 判断是否包含起始边界
     * @param toKey 返回 map中键的结束点
     * @param toInclusive 判断是否包含结束边界
     * @return 返回此 map 的部分视图，其元素范围从 fromElement 到 toElement。
     * @throws ClassCastException 如果使用该集合的比较器无法将<tt> fromKey </tt>和 <tt> toKey </tt>相互比较（或者，如果该集合没有比较器，则使用自然排序）。
     *      *       如果无法将<tt> fromKey </tt>或 <tt> toKey </tt>与当前集中的元素进行比较，则实现可能会（但并非必须）引发此异常。
     * @throws NullPointerException 如果指定的键为null，并且此映射不允许使用null键
     * @throws IllegalArgumentException 如果起始点位置大于结束点位置，或者超出范围
     */
    NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive,
                             K toKey,   boolean toInclusive);

    /**
     * @param toKey 结束节点 key
     * @param inclusive 判断是否包含结束边界
     * @return 此地图部分的视图，其键范围为起始节点到 toKey，inclusive决定是否包括边界
     * @throws ClassCastException 如果无法比较，或者地图没有比较器且参数没有实现Comparable接口。或者参数无法和地图内元素进行比较
     * @throws NullPointerException 如果指定的键为null，并且此映射不允许使用null键
     * @throws IllegalArgumentException 如果位置超出范围
     */
    NavigableMap<K,V> headMap(K toKey, boolean inclusive);

    /**
     * @param fromKey 起始节点 key
     * @param inclusive 判断是否包含起始边界
     * @return 此地图部分的视图，其键范围为 fromKey 到结束，inclusive决定是否包括边界
     * @throws ClassCastException 如果无法比较，或者地图没有比较器且参数没有实现Comparable接口。或者参数无法和地图内元素进行比较
     * @throws NullPointerException 如果指定的键为null，并且此映射不允许使用null键
     * @throws IllegalArgumentException 如果位置超出范围
     */
    NavigableMap<K,V> tailMap(K fromKey, boolean inclusive);

    /**
     * 此地图部分的视图，其键范围为 fromKey（包括边界） 到 toKey（不包括）
     * @throws ClassCastException       如果无法比较，或者地图没有比较器且参数没有实现Comparable接口。或者参数无法和地图内元素进行比较
     * @throws NullPointerException     如果指定的键为null，并且此映射不允许使用null键
     * @throws IllegalArgumentException 如果位置超出范围
     */
    SortedMap<K,V> subMap(K fromKey, K toKey);

    /**
     * 此地图部分的视图，其键范围为起始节点到 toKey（不包括）
     * @throws ClassCastException       如果无法比较，或者地图没有比较器且参数没有实现Comparable接口。或者参数无法和地图内元素进行比较
     * @throws NullPointerException     如果指定的键为null，并且此映射不允许使用null键
     * @throws IllegalArgumentException 如果位置超出范围
     */
    SortedMap<K,V> headMap(K toKey);

    /**
     * 此地图部分的视图，其键范围为 fromKey（包括边界） 到结束
     * @throws ClassCastException       如果 fromKey 与该地图的比较器不兼容（或者，如果地图没有比较器，则如果 fromKey 不实现{@link Comparable}）。
     *                                  如果 fromKey 无法与地图中当前的键进行比较，则实现可能会（但并非必须）抛出此异常。
     * @throws NullPointerException     如果指定的键为null，并且此映射不允许使用null键
     * @throws IllegalArgumentException 如果位置超出范围
     */
    SortedMap<K,V> tailMap(K fromKey);
}
