package java.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.io.Serializable;

/**
 * 将键映射到值的对象。映射不能包含重复的键； 每个键最多可以映射到一个值。
 * 代替 抽象类Dictionary
 * 注意1：将对象作为 MAP的键，需要格外小心。对象的属性变化可能会影响 equals的结果。
 * 注意2：不要将 Map自身作为键，虽然技术上可行，但是最好不要
 * @since 1.2
 */
public interface Map<K,V> {
    // 查询操作

    //返回此映射中的键值映射数。如果映射包含超过 Integer.MAX_VALUE个元素，则返回 Integer.MAX_VALUE
    int size();

    //如果不包含任何键值对，则返回 true
    boolean isEmpty();

    /**
     * 包含指定 key 则返回true
     * @param key 需要进行测试的Key
     * @return 如果包含指定 key 则返回true
     * @throws ClassCastException key类型不对
     * @throws NullPointerException key为null,并且map没有key为null的值
     */
    boolean containsKey(Object key);

    /**
     * 包含指定 value则返回 true
     * @param value 需要被检测的 value
     * @return 如果存在返回 true
     * @throws ClassCastException key类型不对
     * @throws NullPointerException key为null,并且map没有key为null的值
     */
    boolean containsValue(Object value);

    /**
     * 返回指定键所映射到的值；如果此映射不包含键的映射关系，则返回或 null
     * @param key the key whose associated value is to be returned
     * @return 返回特定 key对应的 value值；如果不存在则返回 null
     * @throws ClassCastException key类型不对
     * @throws NullPointerException key为null,并且map没有key为null的值
     */
    V get(Object key);

    // 修改操作

    /**
     * 将指定值与此映射中的指定键关联。如果该映射先前包含键的映射，则旧值将替换为指定值。
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return 如果 key键原先不存在，则返回null，若存在返回原先key对应的 value值
     * @throws UnsupportedOperationException 如果此映射不支持 put 操作
     * @throws ClassCastException 如果指定键或值的类使其无法存储在此映射中
     * @throws NullPointerException key为null,并且map没有key为null的值
     * @throws IllegalArgumentException 如果指定键或值的某些属性阻止将其存储在此映射中
     */
    V put(K key, V value);

    /**
     * 根据 key移除键值对，并返回原value
     * @param key key whose mapping is to be removed from the map
     * @return 返回 key对应的 value值；若不存在返回null
     * @throws UnsupportedOperationException 如果当前map没有实现 remove操作
     * @throws ClassCastException 如果 key的类型不符合此 Map
     * @throws NullPointerException key为null,并且map没有key为null的值
     */
    V remove(Object key);


    // 批量操作

    /**
     * 批量存储
     * @param m 需要存储的 map
     * @throws UnsupportedOperationException 如果此 map没有提供该方法
     * @throws ClassCastException 类型不匹配
     * @throws NullPointerException map为null，或者map不允许 null键或值，但是参数 m允许了null
     * @throws IllegalArgumentException 如果指定键或值的某些属性阻止将其存储在此映射中
     */
    void putAll(Map<? extends K, ? extends V> m);

    /**
     * 移除全部键值对
     * @throws UnsupportedOperationException 如果此 map没有提供该方法
     */
    void clear();


    // 遍历

    /**
     * 返回 map包含的所有 key 的 set视图；
     * set集合由 map支持，对 map的更改会在 set中反映，反之亦然。
     * 如果在对 set 进行迭代时修改了 map（通过迭代器自己的 remove操作除外），则迭代的结果是不确定。
     * set集合支持元素删除，通过 Iterator.remove， Set.remove， removeAll从 map中删除相应的映射，retainAll 和 clear操作。
     * 它不应该支持 add 或 addAll 操作。
     * @return 返回 map包含的所有 key 的 set视图
     */
    Set<K> keySet();

    /**
     * 返回当前 map的所有 value的 collection集合。
     * 其余特性和 keySet 相同。
     * @return 返回当前 map的所有 value的 collection集合。
     */
    Collection<V> values();

    /**
     * 返回 键值对 set集合，Entry为内部类
     * @return 返回 键值对 set集合
     */
    Set<Map.Entry<K, V>> entrySet();

    /**
     * map的 键值对。Map.entrySet方法返回的就是 map的 Entry 的 Set集合。
     * 获取 Map.Entry 引用的唯一方法是从此迭代器中获取。
     * 这些 Map.Entry对象在迭代过程中仅有效；如果迭代器返回条目后，已经修改了原map，则该 entry的行为是不确定的，除非通过该 entry的 setValue操作。
     * @since 1.2
     */
    interface Entry<K,V> {
        /**
         * @return 本条键值对的 key
         * @throws IllegalStateException 如果这个 entry 在原 map中已经被移除；没有强制要求实现。
         */
        K getKey();

        /**
         * 返回与此 entry对应的value。如果已从 map中删除（通过迭代器的 remove操作），则此调用的结果是不确定的。
         * @return 返回与此 entry对应的value
         * @throws IllegalStateException 如果这个 entry 在原 map中已经被移除；没有强制要求实现。
         */
        V getValue();

        /**
         * 用指定的值替换与此原有值，并写入 map（可选操作）
         *
         * @param value 需要添加的新 value
         * @return 返回此 entry原有 value
         * @throws UnsupportedOperationException 如果原 map不支持 put操作
         * @throws ClassCastException 类型不匹配
         * @throws NullPointerException 若原 map不支持 null值，并且参数值为null
         * @throws IllegalArgumentException 如果参数不支持当前 map
         * @throws IllegalStateException 如果这个 entry 在原 map中已经被移除；没有强制要求实现。
         */
        V setValue(V value);

        /**
         * entry的 key 和 value 都相等，则返回true
         * @param o 需要和本条 entry 比较的对象
         * @return 如果相同返回true
         */
        boolean equals(Object o);

        /**
         * hashcode计算方式： （e.getKey()==null   ? 0 : e.getKey().hashCode()) ^ (e.getValue()==null ? 0 : e.getValue().hashCode())
         * @return 本条 entry 的hashcode
         */
        int hashCode();

        /**
         * 返回 key的自然顺序比较器
         * 比较器需要可序列化，并且比较 null键时抛出 NullPointerException
         *
         * @param  <K> the {@link Comparable} type of then map keys
         * @param  <V> the type of the map values
         * @return 自然顺序比较器，用于比较 Map.Entry上的键
         * @since 1.8
         */
        public static <K extends Comparable<? super K>, V> Comparator<Map.Entry<K,V>> comparingByKey() {
            return (Comparator<Map.Entry<K, V>> & Serializable)
                (c1, c2) -> c1.getKey().compareTo(c2.getKey());
        }

        /**
         * 返回 value的自然顺序比较器
         * 比较器需要可序列化，并且比较 null 值时抛出 NullPointerException
         *
         * @param <K> the type of the map keys
         * @param <V> the {@link Comparable} type of the map values
         * @return 自然顺序比较器，用于比较 Map.Entry上的值
         * @since 1.8
         */
        public static <K, V extends Comparable<? super V>> Comparator<Map.Entry<K,V>> comparingByValue() {
            return (Comparator<Map.Entry<K, V>> & Serializable)
                (c1, c2) -> c1.getValue().compareTo(c2.getValue());
        }

        /**
         * 返回一个比较器，该比较器使用给定的 cmp通过 key比较
         * 比较器需要可序列化，cmp也需序列化
         *
         * @param  <K> the type of the map keys
         * @param  <V> the type of the map values
         * @param  cmp the key {@link Comparator}
         * @return a comparator that compares {@link Map.Entry} by the key.
         * @since 1.8
         */
        public static <K, V> Comparator<Map.Entry<K, V>> comparingByKey(Comparator<? super K> cmp) {
            Objects.requireNonNull(cmp);
            return (Comparator<Map.Entry<K, V>> & Serializable)
                (c1, c2) -> cmp.compare(c1.getKey(), c2.getKey());
        }

        /**
         * 返回一个比较器，该比较器使用给定的 cmp通过 value比较
         * 比较器需要可序列化，cmp也需序列化
         *
         * @param  <K> the type of the map keys
         * @param  <V> the type of the map values
         * @param  cmp the value {@link Comparator}
         * @return a comparator that compares {@link Map.Entry} by the value.
         * @since 1.8
         */
        public static <K, V> Comparator<Map.Entry<K, V>> comparingByValue(Comparator<? super V> cmp) {
            Objects.requireNonNull(cmp);
            return (Comparator<Map.Entry<K, V>> & Serializable)
                (c1, c2) -> cmp.compare(c1.getValue(), c2.getValue());
        }
    }

    // 比较和哈希

    /**
     * 比较两个 map是否相同；需要进行 entrySet 遍历比较，全部相同才行
     * @param o 需要比较的对象
     * @return 如果比较对象和当前map相等，返回true
     */
    boolean equals(Object o);

    /**
     * 返回当前 map 的hashcode。当前map的 hashcode为 每个 entry的 hashcode之和。
     * @return 当前 map 的hashcode
     */
    int hashCode();

    // 默认方法

    /**
     * 返回指定 key对应的 value值，如果为不存在指定 key或者为值为 null,返回默认 defaultValue
     * 默认实现不保证此方法的同步或原子性。提供原子性保证的任何实现都必须重写此方法并记录其并发属性。
     *
     * @param key the key whose associated value is to be returned
     * @param defaultValue 参数 key 默认的value值，如果 key 不存在
     * @return 指定 key对应的 value值，如果为不存在指定 key或者为值为 null,返回默认 defaultValue
     * @throws ClassCastException key类型不兼容这个map
     * @throws NullPointerException 若原 map不支持 null值，并且参数值为null
     * @since 1.8
     */
    default V getOrDefault(Object key, V defaultValue) {
        V v;
        return (((v = get(key)) != null) || containsKey(key)) ? v : defaultValue;
    }

    /**
     * 在此map中为每个entry执行给定的操作，直到所有条目已被处理或该操作引发异常。
     * 除非由实现类另外指定，否则操作将按照输入集迭代的顺序执行（如果指定了迭代顺序。）操作引发的异常将中继到调用者。
     * 该方法等价于:
     * <pre> {@code
     * for (Map.Entry<K, V> entry : map.entrySet())
     *     action.accept(entry.getKey(), entry.getValue());
     * }</pre>
     * 默认实现不保证此方法的同步或原子性。提供原子性保证的任何实现都必须重写此方法并记录其并发属性。
     * @param action 每个entry要执行的操作action
     * @throws NullPointerException 如果参数为null
     * @throws ConcurrentModificationException 如果发现entry在迭代过程中被删除
     * @since 1.8
     */
    default void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        for (Map.Entry<K, V> entry : entrySet()) {
            K k;
            V v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch(IllegalStateException ise) {
                // 这通常意味着 entry不再在 map中。
                throw new ConcurrentModificationException(ise);
            }
            action.accept(k, v);
        }
    }

    /**
     * 遍历map,用给定 function传入entry的key和value，并用function的返回值替换原value。直到处理完所有条目或函数引发异常为止。
     * 该方法等价于:
     * <pre> {@code
     * for (Map.Entry<K, V> entry : map.entrySet())
     *     entry.setValue(function.apply(entry.getKey(), entry.getValue()));
     * }</pre>
     * 默认实现不保证此方法的同步或原子性。提供原子性保证的任何实现都必须重写此方法并记录其并发属性。
     *
     * @param function 应用于每个entry的功能
     * @throws UnsupportedOperationException 如果此map的entry迭代器不支持 set操作。
     * @throws ClassCastException 类型不匹配
     * @throws NullPointerException 如果指定的函数为null，或者指定的替换值为null，并且此映射不允许 null值
     * @throws IllegalArgumentException 如果某些参数不支持当前 map
     * @throws ConcurrentModificationException 如果发现entry在迭代过程中被删除
     * @since 1.8
     */
    default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        for (Map.Entry<K, V> entry : entrySet()) {
            K k;
            V v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch(IllegalStateException ise) {
                // 这通常意味着 entry不再在 map中。
                throw new ConcurrentModificationException(ise);
            }
            v = function.apply(k, v);
            try {
                entry.setValue(v);
            } catch(IllegalStateException ise) {
                // 这通常意味着 entry不再在 map中。
                throw new ConcurrentModificationException(ise);
            }
        }
    }

    /**
     * 如果参数 key不存在，或者其值为null;将新的 key-value 保存到 map,并但会原 value值
     * 该方法等价于:
     * <pre> {@code
     * V v = map.get(key);
     * if (v == null)
     *     v = map.put(key, value);
     * return v;
     * }</pre>
     * 默认实现不保证此方法的同步或原子性。提供原子性保证的任何实现都必须重写此方法并记录其并发属性。
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return 如果参数 key不存在，或者其值为null;将新的 key-value 保存到 map,并但会原 value值
     * @throws UnsupportedOperationException 如果这个map不支持添加操作
     * @throws ClassCastException key或者value类型不兼容
     * @throws NullPointerException 如果 key 或者 value 是 null，并且当前map不允许 null
     * @throws IllegalArgumentException 如果某些参数不支持当前 map
     * @since 1.8
     */
    default V putIfAbsent(K key, V value) {
        V v = get(key);
        if (v == null) {
            v = put(key, value);
        }
        return v;
    }

    /**
     * 如果传入key的键值和传入value相同，则移除
     * 该方法等价于:
     * <pre> {@code
     * if (map.containsKey(key) && Objects.equals(map.get(key), value)) {
     *     map.remove(key);
     *     return true;
     * } else
     *     return false;
     * }</pre>
     * 默认实现不保证此方法的同步或原子性。提供原子性保证的任何实现都必须重写此方法并记录其并发属性。
     * @param key 指定的 key键
     * @param value 指定 key期望相对应的 value值
     * @return 移除成功返回 true
     * @throws UnsupportedOperationException 如果这个map不支持添加操作
     * @throws ClassCastException key或者value类型不兼容
     * @throws NullPointerException 如果 key 或者 value 是 null，并且当前map不允许 null
     * @since 1.8
     */
    default boolean remove(Object key, Object value) {
        Object curValue = get(key);
        //如果当前map内的键值和参数value不想等，或则简直为空，或者不包含key，返回false
        if (!Objects.equals(curValue, value) ||
            (curValue == null && !containsKey(key))) {
            return false;
        }
        remove(key);
        return true;
    }

    /**
     * 如果传入key的键值和传入oldValue相同，则替换为新 newValue
     * 该方法等价于:
     * <pre> {@code
     * if (map.containsKey(key) && Objects.equals(map.get(key), value)) {
     *     map.put(key, newValue);
     *     return true;
     * } else
     *     return false;
     * }</pre>
     *
     * 默认实现没有抛出 NullPointerException，如果map不允许null值，需要自行实现
     * 默认实现不保证此方法的同步或原子性。提供原子性保证的任何实现都必须重写此方法并记录其并发属性。
     *
     * @param key 指定的 key键
     * @param oldValue 指定 key期望相对应的 value值
     * @param newValue 需要替换为的新 value值
     * @return 如果替换成功返回 true
     * @throws UnsupportedOperationException 如果这个map不支持添加操作
     * @throws ClassCastException key或者value类型不兼容
     * @throws NullPointerException 如果 key、value、newValue 是 null，并且当前map不允许 null
     * @throws IllegalArgumentException 如果某些参数不支持当前 map
     * @since 1.8
     */
    default boolean replace(K key, V oldValue, V newValue) {
        Object curValue = get(key);
        if (!Objects.equals(curValue, oldValue) ||
            (curValue == null && !containsKey(key))) {
            return false;
        }
        put(key, newValue);
        return true;
    }

    /**
     * 如果key存在，并且不为null，则将键值替换为 value
     * 该方法等价于:
     * <pre> {@code
     * if (map.containsKey(key)) {
     *     return map.put(key, value);
     * } else
     *     return null;
     * }</pre>
     * 默认实现不保证此方法的同步或原子性。提供原子性保证的任何实现都必须重写此方法并记录其并发属性。
      *
     * @param key 指定的 key键
     * @param value 指定 key需要被替换的 value值
     * @return 操作成功，返回原键值
     * @throws UnsupportedOperationException 如果这个map不支持添加操作
     * @throws ClassCastException 如果 key或value 的类型不符合此 Map
     * @throws NullPointerException 如果 key、value 是 null，并且当前map不允许 null
     * @throws IllegalArgumentException 如果某些参数不支持当前 map
     * @since 1.8
     */
    default V replace(K key, V value) {
        V curValue;
        if (((curValue = get(key)) != null) || containsKey(key)) {
            curValue = put(key, value);
        }
        return curValue;
    }

    /**
     *
     * 如果传入 key的对应键值为null, 使用传入方法 mappingFunction重新计算 value值(新value值为null则跳出)，并 put进map
     *
     * <p>如果函数返回null，则不会记录任何映射。
     * 如果该函数本身引发（未经检查的）异常，则将抛出该异常，并且不记录任何映射。
     * 最常见的用法是构造一个新对象，用作初始映射值或备注化的结果，如下所示：
     * <pre> {@code
     * map.computeIfAbsent(key, k -> new Value(f(k)));
     * }</pre>
     *
     * <p>或者实施多值映射，支持每个键多个值：
     * <pre> {@code
     * map.computeIfAbsent(key, k -> new HashSet<V>()).add(v);
     * }</pre>
     *
     * 默认实现等效于此 map的以下步骤，然后返回当前值；如果现在不存在，则返回 null：
     * <pre> {@code
     * if (map.get(key) == null) {
     *     V newValue = mappingFunction.apply(key);
     *     if (newValue != null)
     *         map.put(key, newValue);
     * }
     * }</pre>
     *
     * 默认实现不保证此方法的同步或原子性。提供原子性保证的任何实现都必须重写此方法并记录其并发属性。
     *
     * @param key 指定的 key键
     * @param mappingFunction 补充 value值的方法
     * @return 与指定键关联的当前值；如果计算的值为null，则返回null
     * @throws NullPointerException 如果指定 key是 null并且当前 map不允许 null作为key;或者mappingFunction为 null
     * @throws UnsupportedOperationException 如果这个map不支持添加操作
     * @throws ClassCastException 如果 key或value 的类型不符合此 Map
     * @since 1.8
     */
    default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V v;
        if ((v = get(key)) == null) {
            V newValue;
            if ((newValue = mappingFunction.apply(key)) != null) {
                put(key, newValue);
                return newValue;
            }
        }
        return v;
    }

    /**
     * 1. 如果指定 key的键值不为null,通过 remappingFunction获得新键值；为null则直接返回null
     * 2. 如果新键值不为null，替换原有键值；
     * 3. 如果新键值为null,移除原有键值对
     *
     * 默认实现等效于对此 map执行以下步骤，然后返回当前值；如果现在不存在，则返回 null：
     * <pre> {@code
     * if (map.get(key) != null) {
     *     V oldValue = map.get(key);
     *     V newValue = remappingFunction.apply(key, oldValue);
     *     if (newValue != null)
     *         map.put(key, newValue);
     *     else
     *         map.remove(key);
     * }
     * }</pre>
     * 默认实现不保证此方法的同步或原子性。提供原子性保证的任何实现都必须重写此方法并记录其并发属性。
     * @param key 指定的 key键
     * @param remappingFunction 补充 value值的方法
     * @return 与指定 key关联的新 value值；如果没有，则为null
     * @throws NullPointerException 如果指定 key为 null并且这个map不允许key为null,或者 remappingFunction是null
     * @throws UnsupportedOperationException 如果这个map不支持添加操作
     * @throws ClassCastException 如果 key或value 的类型不符合此 Map
     * @since 1.8
     */
    default V computeIfPresent(K key,
            BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V oldValue;
        if ((oldValue = get(key)) != null) {
            V newValue = remappingFunction.apply(key, oldValue);
            if (newValue != null) {
                put(key, newValue);
                return newValue;
            } else {
                remove(key);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 1. 获得指定 key原有的键值，计算该 key的新键值
     * 2. 如果新键值为 null并且旧简直存在且不为null，移除该键值对，返回null
     * 3. 如果新键值不为 null,替换老键值并返回新简直
     *
     * <pre> {@code
     * map.compute(key, (k, v) -> (v == null) ? msg : v.concat(msg))}</pre>
     * (Method {@link #merge merge()} is often simpler to use for such purposes.)
     *
     * 默认实现等效于对此 map执行以下步骤，然后返回当前值；如果现在不存在，则返回 null：
     * <pre> {@code
     * V oldValue = map.get(key);
     * V newValue = remappingFunction.apply(key, oldValue);
     * if (oldValue != null ) {
     *    if (newValue != null)
     *       map.put(key, newValue);
     *    else
     *       map.remove(key);
     * } else {
     *    if (newValue != null)
     *       map.put(key, newValue);
     *    else
     *       return null;
     * }
     * }</pre>
     *
     * 默认实现不保证此方法的同步或原子性。提供原子性保证的任何实现都必须重写此方法并记录其并发属性。
     * @param key 指定的 key键
     * @param remappingFunction 补充 value值的方法
     * @return 与指定键关联的新值；如果没有，则为null
     * @throws NullPointerException 如果指定 key为 null并且这个map不允许key为null,或者 remappingFunction是null
     * @throws UnsupportedOperationException 如果当前 map没有提供 put方法
     * @throws ClassCastException 如果 key或value 的类型不符合此 Map
     * @since 1.8
     */
    default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V oldValue = get(key);
        V newValue = remappingFunction.apply(key, oldValue);
        if (newValue == null) {
            if (oldValue != null || containsKey(key)) {
                remove(key);
                return null;
            } else {
                return null;
            }
        } else {
            put(key, newValue);
            return newValue;
        }
    }

    /**
     * 1. 获得指定key的value值
     * 2. 如果旧键值为null，使用默认参数value为新键值；如果不为null，重新计算键值
     * 3. 如果新键值为 null，移除该键值对，否则替换键值对；并返回新键值
     * <pre> {@code
     * map.merge(key, msg, String::concat)
     * }</pre>
     *
     * 默认实现等效于对此 map执行以下步骤，然后返回当前值；如果现在不存在，则返回 null：
     * <pre> {@code
     * V oldValue = map.get(key);
     * V newValue = (oldValue == null) ? value :
     *              remappingFunction.apply(oldValue, value);
     * if (newValue == null)
     *     map.remove(key);
     * else
     *     map.put(key, newValue);
     * }</pre>
     *
     * 默认实现不保证此方法的同步或原子性。提供原子性保证的任何实现都必须重写此方法并记录其并发属性。
     * @param key 指定的 key键
     * @param value 要与键相关联的现有值合并的非null值，或者如果没有现有值或null值与该键相关联，则要与该键相关联
     * @param remappingFunction 重新计算value的方法
     * @return 与指定键关联的新值；如果没有值与键关联，则为null
     * @throws UnsupportedOperationException 如果当前 map没有提供 put方法
     * @throws ClassCastException 如果 key或value 的类型不符合此 Map
     * @throws NullPointerException 如果指定 key为 null并且这个map不允许key为null,或者 remappingFunction是null
     * @since 1.8
     */
    default V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        V oldValue = get(key);
        V newValue = (oldValue == null) ? value :
                   remappingFunction.apply(oldValue, value);
        if(newValue == null) {
            remove(key);
        } else {
            put(key, newValue);
        }
        return newValue;
    }
}
