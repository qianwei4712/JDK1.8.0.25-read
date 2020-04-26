package java.util;

/**
 * Hashtable 的父类；
 * 不允许 null 作为 key 和 value。实际存储类需要自行重写 equals 方法
 * 该类已过时。新的实现应实现 Map接口，而不是扩展此类
 * @since   JDK1.0
 */
public abstract
class Dictionary<K,V> {
    //无参构造器
    public Dictionary() {
    }

    //返回字典条目数
    abstract public int size();

    /**
     * 当且仅当该词典不包含任何条目时，结果才为true。
     * @return 如果字典没有任何键值对，返回 true;否则返回 false
     */
    abstract public boolean isEmpty();

    /**
     * 返回此字典中键的枚举。 keys方法的一般约定是返回 Enumeration，该对象将生成此词典包含条目的所有键
     * @return  此字典中的 key键的枚举
     */
    abstract public Enumeration<K> keys();

    /**
     * 返回此字典中的值的枚举。elements 方法的一般约定是返回一个 Enumeration，它将生成此词典的条目中包含的所有元素
     * @return  此字典中的 value值的枚举
     */
    abstract public Enumeration<V> elements();

    /**
     * 返回 key所对应的 value值，若无返回 null
     * @return 该字典中 key 所映射到的 value值
     * @param   key  指定 key 值
     * @exception NullPointerException 如果 key是null，抛出异常
     */
    abstract public V get(Object key);

    /**
     * 将指定键值对映射到字典。若字典已经存在该 key，覆盖并返回先前 value
     * @param      key     the hashtable key.
     * @param      value   the value.
     * @return    此字典中 key映射到的先前值；如果键先前没有映射，则返回 null
     * @exception  NullPointerException  如果 key 或者 value 是 null
     */
    abstract public V put(K key, V value);

    /**
     * 移除指定 key 的键值对
     * @param   key  需要被移除的键值对的 key
     * @return  被移除的指定 key 的 value值；如果这个 key不存在于字典中，返回 null
     * @exception NullPointerException 如果 key是null，抛出异常
     */
    abstract public V remove(Object key);
}
