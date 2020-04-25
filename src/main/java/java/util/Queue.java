package java.util;

/**
 * @author shiva   2020/3/31 21:17
 * @since 1.5
 */
public interface Queue<E> extends Collection<E> {

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
}
