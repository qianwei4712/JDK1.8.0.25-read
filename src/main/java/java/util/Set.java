package java.util;

/**
 * 一个不包含重复元素，即满足e1.equals(e2)条件。
 * 如果允许null，只可存在一个 null 元素
 * @author shiva   2020/4/25 16:38
 * @since 1.2
 */
public interface Set<E> extends Collection<E> {
    // 查询操作

    //返回此集合中的元素数。如果此集包含超过 Integer.MAX_VALUE，则返回 Integer.MAX_VALUE。
    int size();

    //如果集合不包含任何元素，则返回true
    boolean isEmpty();

    //如果此集合包含指定的元素，则返回 true。
    //更正式地讲，当且仅当此集合包含一个元素 e，使得（o == null？e == null：o 时，才返回 true）。
    boolean contains(Object o);

    //返回此集合中元素的迭代器。元素以不特定的顺序返回（除非此集合是提供保证的某些类的实例）。
    Iterator<E> iterator();

    //转数组
    Object[] toArray();

    //转到指定数组
    <T> T[] toArray(T[] a);

    // 修改操作

    //如果指定的元素尚不存在，则将其添加到该集合（可选操作）。
    //更正式地讲，如果集合中不包含任何元素 e2 满足e == null？e2 == null ：e.equals（e2）
    //如果此集合已经包含元素，则调用将使该集合保持不变，并返回 false。
    //结合构造函数上的限制，可以确保集合永远不会包含重复元素。
    boolean add(E e);

    //如果存在指定的元素，则从该集合中删除（可选操作）。
    //更正式地讲，删除元素 e，满足o == null？e == null：o.equals（e）
    //如果此集合包含元素，则返回 true（或者等效地，如果此集合作为调用结果更改）。
    //（一旦调用返回，此集合将不包含该元素。）
    boolean remove(Object o);

    // 批量操作

    //包含全部
    boolean containsAll(Collection<?> c);

    //批量添加
    boolean addAll(Collection<? extends E> c);

    //删除差集
    boolean retainAll(Collection<?> c);

    //删除交集
    boolean removeAll(Collection<?> c);

    //删除所有元素，操作后集合为空
    void clear();

    // 比较和哈希

    //比较指定对象与此设置的相等性。
    //如果指定对象也是一个集合，则两个集合具有相同的大小，并且指定集合的每个成员都包含在此集合中（或等效地，该集合的每个成员），则返回 true。
    //此定义确保 equals方法可在set接口的不同实现中正常工作。
    boolean equals(Object o);

    //返回此集合的哈希码值。
    //集合的哈希码定义为集合中元素的哈希码之和，其中 null元素的哈希码定义为零。
    //这确保 s1.equals（s2）意味着 s1.hashCode（）== s2.hashCode（）,对于任意两个 s1 和 s2，这是hashCode的一般约束所要求的。
    int hashCode();

    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.DISTINCT);
    }
}
