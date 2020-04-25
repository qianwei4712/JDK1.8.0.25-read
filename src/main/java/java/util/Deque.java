package java.util;

/**
 * @author shiva   2020/3/31 21:18
 * @since  1.6
 */
public interface Deque<E> extends Queue<E> {

    /**
     * 父类 Queue 方法
     */

    //队尾添加元素，超出抛出IllegalStateException
    boolean add(E e);

    //队尾添加元素，超出返回false
    boolean offer(E e);

    //移除队头元素并返回，如果队列为空抛出NoSuchElementException
    E remove();

    //移除队头元素并返回，如果队列为空返回null
    E poll();

    //返回队头元素，但是不删除，如果队列为空抛出NoSuchElementException
    E element();

    //返回队头元素，但是不删除，如果队列为空返回null
    E peek();


    /**
     * Deque 新增方法
     */

    //队头添加元素，超出抛出IllegalStateException
    void addFirst(E e);

    //队尾添加元素，超出抛出IllegalStateException
    void addLast(E e);

    //队头添加元素，超出返回false
    boolean offerFirst(E e);

    //队尾添加元素，超出返回false
    boolean offerLast(E e);

    //移除队头元素并返回，如果队列为空抛出NoSuchElementException
    E removeFirst();

    //移除队尾元素并返回，如果队列为空抛出NoSuchElementException
    E removeLast();

    //移除队头元素并返回，如果队列为空返回null
    E pollFirst();

    //移除队尾元素并返回，如果队列为空返回null
    E pollLast();

    //获得队头元素，但是不删除，如果队列为空抛出NoSuchElementException
    E getFirst();

    //获得队尾元素，但是不删除，如果队列为空抛出NoSuchElementException
    E getLast();

    //返回队头元素，但是不删除，如果队列为空返回null
    E peekFirst();

    //返回队尾元素，但是不删除，如果队列为空返回null
    E peekLast();

    //删除第一个相同元素，从头开始检索
    //若传入的类型和泛型不兼容，抛出ClassCastException
    //如果指定元素为null,并且这个队列不允许为null，抛出 NullPointerException
    boolean removeFirstOccurrence(Object o);

    //删除第一个相同元素，从尾开始检索
    //若传入的类型和泛型不兼容，抛出ClassCastException
    //如果指定元素为null,并且这个队列不允许为null，抛出 NullPointerException
    boolean removeLastOccurrence(Object o);


    /**
     * 栈Stack 的方法
     */

    //将元素压入栈，并返回
    //若违反容量限制，抛出IllegalStateException
    //若传入的类型和泛型不兼容，抛出ClassCastException
    //如果指定元素为null,并且这个队列不允许为null，抛出 NullPointerException
    //若传入元素的某些属性阻止压入，那么抛出IllegalArgumentException
    void push(E e);

    //弹出栈顶的元素
    E pop();


    /**
     * Collection 的方法
     */

    //移除第一个指定元素，顺序检索
    boolean remove(Object o);

    //顺序判断是否包含
    boolean contains(Object o);

    //返回队列长度
    public int size();

    //返回迭代器
    Iterator<E> iterator();

    //返回增强迭代器
    Iterator<E> descendingIterator();
}
