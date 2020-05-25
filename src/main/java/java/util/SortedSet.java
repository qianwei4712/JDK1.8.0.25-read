package java.util;

/**
 * 进一步提供了元素上的排序。<br/>
 * 元素使用其自然排序进行比较，或通过通常在已排序集合创建时提供的{@link Comparator}进行排序。<br/>
 * 集合的迭代器将以升序元素顺序遍历集合。<p>
 *
 * 插入排序集中的所有元素必须实现<tt> Comparable </tt> 接口（或被指定的比较器）。<br/>
 * 此外，所有这样的元素必须是<i>相互可比的</i>： <tt> e1.compareTo（e2）</tt>（或<tt> comparator.compare（e1，e2）</tt>）<br/>
 * 若元素无法比较<tt> e1 </tt>和<tt> e2 </tt>抛出<tt> ClassCastException </tt>。<p>
 *
 * 所有通用排序集实现类都应*提供四个“标准”构造函数：<br/>
 * 1）一个void（无参数）构造函数，该构造函数将创建一个空排序集，并根据其元素的自然顺序进行排序。<br/>
 * 2）具有类型为<tt> Comparator </tt>的单个参数的构造函数，它将创建一个空的排序集，并根据指定的比较器进行排序。<br/>
 * 3）具有单个参数类型为<tt> Collection </tt>的构造函数，创建一个新的排序集合，该集合具有与其参数相同的元素，并根据元素的自然顺序进行排序。<br/>
 * 4）具有单个类型为<tt> SortedSet </tt>的参数的构造函数，创建一个新的排序集，其元素与输入排序集的顺序相同。由于接口不能包含构造函数，因此无法强制执行此建议。<p>
 *
 * @param <E> 此集合所维护的元素类型
 * @since 1.2
 */

public interface SortedSet<E> extends Set<E> {
    /**
     * @return 返回本SortedSet的比较器，如果使用自然排序则返回 null
     */
    Comparator<? super E> comparator();

    /**
     * 返回此集合部分的视图，其元素范围从<tt> fromElement </tt>（包括）到<tt> toElement </tt>（不包括）。
     * （如果<tt> fromElement </tt>和<tt> toElement </tt>相等，则返回的集合为空。）
     * 此集合支持返回的集合，因此返回的集合中的更改将反映在中这组，反之亦然。返回的集合支持该集合支持的所有可选集合操作。
     * <p>超出范围抛出异常
     * @param fromElement 当前 set中起始节点
     * @param toElement 当前 set中高位结束节点
     * @return 此集合一部分的视图，其元素范围从 <tt> fromElement </tt>（包括）到 <tt> toElement </tt>（不包括）
     * @throws ClassCastException 如果使用该集合的比较器无法将<tt> fromElement </tt>和 <tt> toElement </tt>相互比较（或者，如果该集合没有比较器，则使用自然排序）。
     *         如果无法将<tt> fromElement </tt>或 <tt> toElement </tt>与当前集中的元素进行比较，则实现可能会（但并非必须）引发此异常。
     * @throws NullPointerException 如果起始节点或者结束节点为空，并且本 set不允许null节点
     * @throws IllegalArgumentException 如果<tt> fromElement </tt>是大于<tt> toElement </tt>；
     *         或如果此集合本身的范围受限制，并且<tt> fromElement </tt>或 <tt> toElement </tt>位于范围的边界之外
     */
    SortedSet<E> subSet(E fromElement, E toElement);

    /**
     * @param toElement e之前的元素，不包括e
     * @return 此集合的一部分的视图，其元素严格小于<tt> toElement </tt>
     * @throws ClassCastException 如果<tt> toElement </tt>与该集合的比较器不兼容（或者，如果集合中没有比较器，则如果<tt> toElement </ tt>不实现{@link Comparable}）。
     *         如果不能将<tt> toElement </tt>与当前集中的元素进行比较，则实现可以（但并非必须）抛出此异常。
     * @throws NullPointerException 如果<tt> toElement </tt>为null，并且此集合不允许null元素
     * @throws IllegalArgumentException 如果此集合本身具有限制范围，并且<tt> toElement </tt>位于该范围的范围之外
     */
    SortedSet<E> headSet(E toElement);

    /**
     * @param fromElement 返回集合的指定节点 E之后的所有结点（包括E）
     * @return 此集合中元素大于或等于<tt> fromElement </tt>的部分的视图
     * @throws ClassCastException 如果<tt> fromElement </tt>与该集合的比较器不兼容（或者，如果集合中没有比较器，则如果<tt> fromElement </tt>不实现{@link Comparable}）。
     *         如果<tt> fromElement </tt>无法与集合中当前的元素进行比较，则实现可能会（但并非必须）抛出此异常。
     * @throws NullPointerException 如果<tt> fromElement </tt>为null，并且此集合不允许 null元素
     * @throws IllegalArgumentException 如果此集合本身具有限制范围，并且<tt> fromElement </tt>位于该范围的范围之外
     */
    SortedSet<E> tailSet(E fromElement);

    /**
     * @return 返回当前 set中第一个（最低）节点
     * @throws NoSuchElementException 如果此集合为空
     */
    E first();

    /**
     * @return 返回当前 set中最后（最高）一个节点
     * @throws NoSuchElementException 如果此集合为空
     */
    E last();

    /**
     * Creates a {@code Spliterator} over the elements in this sorted set.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#DISTINCT},
     * {@link Spliterator#SORTED} and {@link Spliterator#ORDERED}.
     * Implementations should document the reporting of additional
     * characteristic values.
     *
     * <p>The spliterator's comparator (see
     * {@link java.util.Spliterator#getComparator()}) must be {@code null} if
     * the sorted set's comparator (see {@link #comparator()}) is {@code null}.
     * Otherwise, the spliterator's comparator must be the same as or impose the
     * same total ordering as the sorted set's comparator.
     *
     * @implSpec
     * The default implementation creates a
     * <em><a href="Spliterator.html#binding">late-binding</a></em> spliterator
     * from the sorted set's {@code Iterator}.  The spliterator inherits the
     * <em>fail-fast</em> properties of the set's iterator.  The
     * spliterator's comparator is the same as the sorted set's comparator.
     * <p>
     * The created {@code Spliterator} additionally reports
     * {@link Spliterator#SIZED}.
     *
     * @return 在此排序集中的元素上使用{@code Spliterator}
     * @since 1.8
     */
    @Override
    default Spliterator<E> spliterator() {
        return new Spliterators.IteratorSpliterator<E>(
                this, Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED) {
            @Override
            public Comparator<? super E> getComparator() {
                return SortedSet.this.comparator();
            }
        };
    }
}
