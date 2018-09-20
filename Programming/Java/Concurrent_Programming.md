# Concurrent Programming

### 理论基础

### **CPU多级缓存**：

因为CPU频率太快，常常要等待主存，这会浪费CPU时间，所以加入高速缓存，然而一级高速缓存容量较小，所以加入更大容量，但速度稍慢的另一级缓存。每个核都有自己的缓存

### **带有高速缓存的CPU执行计算的流程**

- 程序以及数据被加载到主内存

- 指令和数据被加载到CPU的高速缓存

- CPU执行指令，把结果写到高速缓存

- 高速缓存中的数据写回主内存

### **局部性原理：**

在CPU访问存储设备时，无论是存取数据抑或存取指令，都趋于聚集在一片连续的区域中。

时间局部性（Temporal Locality）：如果一个信息项正在被访问，那么在近期它很可能还会被再次访问。

空间局部性（Spatial Locality）：如果一个存储器的位置被引用，那么将来他附近的位置也会被引用。

### **缓存一致性（MESI）：**

保证各级缓存内部数据的一致。下面是Cache line（缓存存储数据的单元）的四种状态介绍。

| 状态      | 描述                                                         | 监听任务                                                     |
| --------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| Modified  | 有效，数据被修改了，和内存中的数据不一致，数据只存在于本Cache中。 | 缓存行必须时刻监听所有试图读该缓存行所对应的主存的操作，这种操作必须在缓存将该缓存行写回主存并将状态变成S（共享）状态之前被延迟执行。 |
| Exclusive | 有效，数据和内存中的数据一致，数据只存在于本Cache中。        | 缓存行也必须监听其它缓存读主存中对应该缓存行的操作，一旦有这种操作，该缓存行需要变成S（共享）状态。 |
| Shared    | 有效，数据和内存中的数据一致，数据存在于很多Cache中。        | 缓存行也必须监听其它缓存使该缓存行无效或者独享该缓存行的请求。 |
| Invalid   | 无效（没有被使用）                                           | 无                                                           |

上述状态，假设只有两个cache情况下，只可能出现SS，I[MESI]?这8种情况

![Screen Shot 2018-09-12 at 12.19.30 PM](/Users/flyang/Documents/self-teaching/notebook/coding/Screen Shot 2018-09-12 at 12.19.30 PM.png)

loca operation

| Initial State | Operation | Response                                                     |
| ------------- | --------- | ------------------------------------------------------------ |
| Invalid(I)    | PrRd      | Issue BusRd to the bus. other Caches see BusRd and check if they have a non-invalid copy, inform sending cache. State transition to (S)**Shared**, if other Caches have non-invalid copy; to (E)**Exclusive**, if none (must ensure all others have reported). If other Caches have copy, one of them sends value, else fetch from Main Memory |
|               | PrWr      | Issue BusRdX signal on the busState transition to (M)**Modified** in the requestor Cache. If other Caches have copy, they send value, otherwise fetch from Main Memory. Other Caches Invalidate their copies. |
| Shared(S)     | PrWr      | Issues BusUpgr signal on the bus.State transition to (M)**Modified**.other Caches see BusUpgr and mark their copies of the block as (I)Invalid. |

remote operation

| Initial State | Operation | Response                                                     |
| ------------- | --------- | ------------------------------------------------------------ |
| Modified(M)   | BusRd     | Transition to (S)Shared.Put FlushOpt on Bus with data. Received by sender of BusRd and Memory Controller, which writes to Main memory. |
|               | BusRdX    | Transition to (I)Invalid.Put FlushOpt on Bus with data. Received by sender of BusRdx and Memory Controller, which writes to Main memory. |

### Java内存模型与硬件内存模型

JVM的堆和栈主要在主内存中，部分会在缓存和寄存器中。

一个线程的工作环境类似于有一个CPU+线程的缓存（有共享面量的副本）+主内存中的变量（都是共享的）

**同步八种操作**

lock, unlock：作用于主内存变量

read … load：read把主内存变量传到工作内存。load把read传入的变量放入工作内存的变量副本中。两个必须按顺序使用，中间可加入其他操作。

use, assign：use把工作内存的变量传递给执行引擎。assign把执行引擎计算后的值赋给工作内存的变量。

store ... write：store把工作内存的变量传到主内存中。write把store得到的变量写入主内存。两个必须按顺序使用，中间可加入其他操作。

- 没有assign，不能把变量写回到主内存中。
- 当变量在工作内存中被改变后，必须同步到主内存中。
- 新变量只能在主内存中诞生，不能在工作内存中直接使用未经过load或assign的变量。即使用use和store前要先执行assign和load。
- 同一线程可执行n次lock，但也要执行n次unlock才能解锁。lock和unlock要成对。无法unlock其他线程的lock变量。
- 第二次lock一个变量时，会清空工作内存中该变量值。
- unlock前要store和write。



### 并发任务

1.运行任务
单独处理器或同一处理器不同时间片。当任务小时，在同一线程，避免启动线程的开销。当任务计算量大时，一个处理器一个线程。

```
Runnable rtask = () -> { ... };
ExecutorService executor = ...;//下面的Executors
executor.execute(rtask);
executor.shutdown();

//Runnable只执行不返回值，返值用Callable.且用submit
Callable<V> ctask = new MyCallable();
Future<V> result = executor.submit(ctask);

//Executors
Executors.newCachedThreadPool()//适合小任务多或任务等待时间长的的程序。每个任务被分配在空闲的程上，没有空闲就分配新的，数量没有上限。线程空闲过长会被终止。
.newFixedThreadPool(nthreads)//固定线程数
int processors = Runtime.getRuntime().availableProcessors();//查看处理器数
```

2.Futures
上面Callable被submit后得到的Futures有get，cancel，isDone等方法。下面是处理一List的callable任务

```
String word = ...;
Set<Path> paths = ...;
List<Callable<Long>> tasks = new ArrayList<>();
for (Path p : paths) tasks.add(
    () -> { return number of occurrences of word in p });
List<Future<Long>> results = executor.invokeAll(tasks);//可设定时间停止
    // This call blocks until all tasks have completed
long total = 0;
for (Future<Long> result : results) total += result.get();

//书中还有按顺序处理结果，而非等所有结果出来后再处理的代码。

//invokeAny：只要某一任务执行结果符合条件就停止，如下
() -> { if (word occurs in p) return p; else throw ... }
```

### 异步计算

1.Completable Futures

```
CompletableFuture<String> f = ...; 
f.thenAccept((String s) -> Process the result s);

//HttpClient也可获得CompletableFuture

//异步执行
CompletableFuture<String> f = CompletableFuture.supplyAsync(
    () -> { String result; Compute the result; return result; },//Supplier<T>，不像Callable可以抛checked exception
    executor);//不设置executor，task会在默认executor执行
    
//whenComplete应对CompletableFuture完成后的处理
f.whenComplete((s, t) -> {
    if (t == null) { Process the result s; } 
    else { Process the Throwable t; }
});

//可以设定完成的值。下面的代码可以多条，只要有一个计算完设定complete，其他都会停止
executor.execute(() -> {
    int n = workHard(arg);
    f.complete(n); 
});
```

2.Composing Completable Futures
各种CompletableFuture的API及其组合



### 线程安全

1.变量更新可见
final初始化后
static初始化后
volatile变量变化可见
在解锁前的变化可见（对于需要相同锁的）

2.变量竞争
多个任务同时对一个变量进行修改

3.策略
限制，不能有共享变量
不变性，共享不可修改的对象。
final变量，方法，甚至类;非私有方法不能返回可被修改的数据引用，返回复制更可考；不存储任何引用到可变对象中，用复制；Don’t let the this reference escape in a constructor？

锁（高代价）

### 并行计算

Stream：
enough data
data in memory
efficiently splittable 
stream operations should do a substantial amount of work
stream operations should not block

Arrays:
parallelSetAll(V, Function)
parallelSort(V, Comparator)

### 线程安全的数据结构

1.ConcurrentHashMap
`map.compute(word, (k, v) -> v == null ? 1 : v + 1);`
`map.merge(word, 1L, Long::sum);`
传递给这两个方法的函数不应该计算太旧，且不能修改映射。
下面是compute出现前的做法

```
do {
    oldValue = map.get(word);
    newValue = oldValue + 1;
} while (!map.replace(word, oldValue, newValue));
```

还有其他针对此类数据结构的方法，如以search, reduce, forEach开头的

2.阻塞队列Blocking Queues
null值在这类数据结构中是不合法的
三种结果：blocking, exception, failure indicator

3.ConcurrentSkipListMap

### Atomic Counters and Accumulators

concurrent.atomic包

```
//在main外
public static AtomicLong count = new AtomicLong();
//在main内
ExecutorService executor = Executors.newCachedThreadPool();
for (int i = 1; i <= 1000; i++) {
    int taskId = i;
    Runnable task = () -> {
        for (int k = 1; k <= 10000; k++)
            count.incrementAndGet();//每次加1
        System.out.println(taskId + ": " + count);
    };
    executor.execute(task);
}

//还有其他方法用于setting, adding, subtracting

//更复杂的计算，用updateAndGet
largest.accumulateAndGet(observed, Math::max);
```

当有大量线程访问原子值时，需要用下面的class
LongAdder：不同线程有不同的累加器，最后才加总。所以用在上面代码的话，count不能保持稳定增加，但最后的结果还是对的。
方法count.increment();

LongAccumulator

```
LongAccumulator largest = new LongAccumulator(Long::max, 90);
    largest.accumulate(42L);
    long max = largest.get();
```

### 锁（最后的选择）

通常使用不可变数据或者将可变数据传递给另外一个线程来。如果必须共享，用上面提到的安全的数据结构。

### 线程

```
//启动
Thread thread = new Thread(task); 
thread.start();

Thread.sleep(millis);//其他线程可继续
Thread.join(millis);//等待一个线程完成

//终止
Runnable task = () -> {
    while (more work to do) {
        if (Thread.currentThread().isInterrupted()) return;
        Do more work
    }
};

Thread.interrupted//终止当前进程

//如果进程sleep或者wait，则会抛出InterruptedException，这需要捕获。如果进程被中断时调用sleep同样会抛异常
Runnable task = () -> { 
    try {
        while (more work to do) {          Do more work
            Thread.sleep(millis); 
        }
    }
    catch (InterruptedException ex) {
        Thread.currentThread().interrupt(); 
    }
};

//线程级的局部变量
public static final ThreadLocal<NumberFormat> currencyFormat
    = ThreadLocal.withInitial(() -> NumberFormat.getCurrencyInstance());

String amountDue = currencyFormat.get().format(total);
```

线程的属性

### 进程

略



参考资料：

*Core_Java_SE_9_for_the_Impatient_2ed*

[CPU缓存一致性协议MESI](https://www.cnblogs.com/yanlong300/p/8986041.html)

[Java并发编程入门与高并发面试](https://coding.imooc.com/class/195.html)

[MESI protocol](https://en.wikipedia.org/wiki/MESI_protocol)