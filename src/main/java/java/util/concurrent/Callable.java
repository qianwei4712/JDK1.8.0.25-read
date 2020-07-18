package java.util.concurrent;

/**
 * 返回结果并可能引发异常的任务。 实现者定义一个没有参数的单一方法，称为 call 。
 * Callable 接口类似于 Runnable ，因为它们都是为其实例可能由另一个线程执行的类设计的。
 * 然而，A Runnable 不返回结果，也不能抛出被检查的异常。
 * 该 Executors 类包含的实用方法，从其他普通形式转换为 Callable 类。
 * @since 1.5
 * @param <V> the result type of method {@code call}
 */
@FunctionalInterface
public interface Callable<V> {
    /**
     * 计算一个返回值，如果不能计算那么抛出异常
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    V call() throws Exception;
}
