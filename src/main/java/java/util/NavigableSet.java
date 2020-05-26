package java.util;

/**
 * A {@link SortedSet} extended with navigation methods reporting
 * closest matches for given search targets. Methods {@code lower},
 * {@code floor}, {@code ceiling}, and {@code higher} return elements
 * respectively less than, less than or equal, greater than or equal,
 * and greater than a given element, returning {@code null} if there
 * is no such element.  A {@code NavigableSet} may be accessed and
 * traversed in either ascending or descending order.  The {@code
 * descendingSet} method returns a view of the set with the senses of
 * all relational and directional methods inverted. The performance of
 * ascending operations and views is likely to be faster than that of
 * descending ones.  This interface additionally defines methods
 * {@code pollFirst} and {@code pollLast} that return and remove the
 * lowest and highest element, if one exists, else returning {@code
 * null}.  Methods {@code subSet}, {@code headSet},
 * and {@code tailSet} differ from the like-named {@code
 * SortedSet} methods in accepting additional arguments describing
 * whether lower and upper bounds are inclusive versus exclusive.
 * Subsets of any {@code NavigableSet} must implement the {@code
 * NavigableSet} interface.
 *
 * <p> The return values of navigation methods may be ambiguous in
 * implementations that permit {@code null} elements. However, even
 * in this case the result can be disambiguated by checking
 * {@code contains(null)}. To avoid such issues, implementations of
 * this interface are encouraged to <em>not</em> permit insertion of
 * {@code null} elements. (Note that sorted sets of {@link
 * Comparable} elements intrinsically do not permit {@code null}.)
 *
 * <p>Methods
 * {@link #subSet(Object, Object) subSet(E, E)},
 * {@link #headSet(Object) headSet(E)}, and
 * {@link #tailSet(Object) tailSet(E)}
 * are specified to return {@code SortedSet} to allow existing
 * implementations of {@code SortedSet} to be compatibly retrofitted to
 * implement {@code NavigableSet}, but extensions and implementations
 * of this interface are encouraged to override these methods to return
 * {@code NavigableSet}.
 *
 * @param <E> 此集合所维护的元素类型
 * @since 1.6
 */
public interface NavigableSet<E> extends SortedSet<E> {
    /**
     * 返回此 set 中严格小于给定元素的最大元素；如果不存在这样的元素，则返回 null。
     * @param e 需要匹配的值
     * @return 返回此 set 中严格小于给定元素的最大元素；如果不存在这样的元素，则返回 null。
     * @throws ClassCastException 如果指定的元素不能与集合中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为null，并且此集合不允许使用null元素
     */
    E lower(E e);

    /**
     * 返回此 set 中小于等于给定元素的最大元素；如果不存在这样的元素，则返回 null。
     * @param e 需要匹配的值
     * @return 返回此 set 中小于等于给定元素的最大元素；如果不存在这样的元素，则返回 null。
     * @throws ClassCastException 如果指定的元素不能与集合中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为null，并且此集合不允许使用null元素
     */
    E floor(E e);

    /**
     * 返回此 set 中大于等于给定元素的最小元素；如果不存在这样的元素，则返回 null。
     * @param e 需要匹配的值
     * @return 返回此 set 中大于等于给定元素的最小元素；如果不存在这样的元素，则返回 null。
     * @throws ClassCastException 如果指定的元素不能与集合中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为null，并且此集合不允许使用null元素
     */
    E ceiling(E e);

    /**
     * 返回此 set 中严格大于给定元素的最小元素；如果不存在这样的元素，则返回 null。
     * @param e 需要匹配的值
     * @return 返回此 set 中严格大于给定元素的最小元素；如果不存在这样的元素，则返回 null。
     * @throws ClassCastException 如果指定的元素不能与集合中当前的元素进行比较
     * @throws NullPointerException 如果指定的元素为null，并且此集合不允许使用null元素
     */
    E higher(E e);

    /**
     * @return 获取并移除第一个（最低）元素；如果此 set 为空，则返回 null。
     */
    E pollFirst();

    /**
     * @return 获取并移除最后一个（最高）元素；如果此 set 为空，则返回 null。
     */
    E pollLast();

    /**
     * @return 以升序返回在此 set 的元素上进行迭代的迭代器。
     */
    Iterator<E> iterator();

    /**
     * @return 返回此 set 中所包含元素的逆序视图。
     */
    NavigableSet<E> descendingSet();

    /**
     * @return 以降序返回在此 set 的元素上进行迭代的迭代器。
     */
    Iterator<E> descendingIterator();

    /**
     * 返回此 set 的部分视图，其元素范围从 fromElement 到 toElement。
     * @param fromElement 起始元素节点
     * @param fromInclusive 判断是否包含起始边界
     * @param toElement 结束元素节点
     * @param toInclusive 判断是否包含结束边界
     * @return 返回此 set 的部分视图，其元素范围从 fromElement 到 toElement。
     * @throws ClassCastException 如果使用该集合的比较器无法将<tt> fromElement </tt>和 <tt> toElement </tt>相互比较（或者，如果该集合没有比较器，则使用自然排序）。
     *       如果无法将<tt> fromElement </tt>或 <tt> toElement </tt>与当前集中的元素进行比较，则实现可能会（但并非必须）引发此异常。
     * @throws NullPointerException 如果起始节点或者结束节点为空，并且本 set不允许null节点
     * @throws IllegalArgumentException 如果<tt> fromElement </tt>是大于<tt> toElement </tt>；
     *       或如果此集合本身的范围受限制，并且<tt> fromElement </tt>或 <tt> toElement </tt>位于范围的边界之外
     */
    NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
                           E toElement,   boolean toInclusive);

    /**
     * 返回此 set 的部分视图，其元素小于（或等于，如果 inclusive 为 true）toElement。
     * @param toElement 结束元素节点
     * @param inclusive 判断是否包含结束边界
     * @return 返回此 set 的部分视图，其元素小于（或等于，如果 inclusive 为 true）toElement。
     * @throws ClassCastException 如果<tt> toElement </tt>与该集合的比较器不兼容（或者，如果集合中没有比较器，则如果<tt> toElement </ tt>不实现{@link Comparable}）。
     *         如果不能将<tt> toElement </tt>与当前集中的元素进行比较，则实现可以（但并非必须）抛出此异常。
     * @throws NullPointerException 如果<tt> toElement </tt>为null，并且此集合不允许null元素
     * @throws IllegalArgumentException 如果此集合本身具有限制范围，并且<tt> toElement </tt>位于该范围的范围之外
     */
    NavigableSet<E> headSet(E toElement, boolean inclusive);

    /**
     * 返回此 set 的部分视图，其元素大于（或等于，如果 inclusive 为 true）fromElement。
     * @param fromElement 起始元素节点
     * @param inclusive 判断是否包含起始边界
     * @return 返回此 set 的部分视图，其元素大于（或等于，如果 inclusive 为 true）fromElement。
     * @throws ClassCastException 如果<tt> fromElement </tt>与该集合的比较器不兼容（或者，如果集合中没有比较器，则如果<tt> fromElement </tt>不实现{@link Comparable}）。
     *         如果<tt> fromElement </tt>无法与集合中当前的元素进行比较，则实现可能会（但并非必须）抛出此异常。
     * @throws NullPointerException 如果<tt> fromElement </tt>为null，并且此集合不允许 null元素
     * @throws IllegalArgumentException 如果此集合本身具有限制范围，并且<tt> fromElement </tt>位于该范围的范围之外
     */
    NavigableSet<E> tailSet(E fromElement, boolean inclusive);

    /**
     * 返回此 set 的部分视图，其元素从 fromElement（包括）到 toElement（不包括）
     * @throws ClassCastException       如果指定的元素不能与集合中当前的元素进行比较
     * @throws NullPointerException    如果起始节点或者结束节点为空，并且本 set不允许null节点
     * @throws IllegalArgumentException 如果<tt> fromElement </tt>是大于<tt> toElement </tt>；
     *                                  或如果此集合本身的范围受限制，并且<tt> fromElement </tt>或 <tt> toElement </tt>位于范围的边界之外
     */
    SortedSet<E> subSet(E fromElement, E toElement);

    /**
     * 返回此 set 的部分视图，其元素严格小于 toElement。
     * @throws ClassCastException       如果指定的元素不能与集合中当前的元素进行比较
     * @throws NullPointerException     如果<tt> toElement </tt>为null，并且此集合不允许 null元素
     * @throws IllegalArgumentException 如果此集合本身具有限制范围，并且<tt> toElement </tt>位于该范围的范围之外
     */
    SortedSet<E> headSet(E toElement);

    /**
     * 返回此 set 的部分视图，其元素大于等于 fromElement。
     * @throws ClassCastException          如果指定的元素不能与集合中当前的元素进行比较
     * @throws NullPointerException        如果<tt> fromElement </tt>为null，并且此集合不允许 null元素
     * @throws IllegalArgumentException    如果此集合本身具有限制范围，并且<tt> fromElement </tt>位于该范围的范围之外
     */
    SortedSet<E> tailSet(E fromElement);
}
