# Concurrent Programming

[TOC]



## 理论基础

### CPU多级缓存：

因为CPU频率太快，常常要等待主存，这会浪费CPU时间，所以加入高速缓存，然而一级高速缓存容量较小，所以加入更大容量，但速度稍慢的另一级缓存。每个核都有自己的缓存

### 带有高速缓存的CPU执行计算的流程

- 程序以及数据被加载到主内存

- 指令和数据被加载到CPU的高速缓存

- CPU执行指令，把结果写到高速缓存

- 高速缓存中的数据写回主内存

### 局部性原理：

在CPU访问存储设备时，无论是存取数据抑或存取指令，都趋于聚集在一片连续的区域中。

时间局部性（Temporal Locality）：如果一个信息项正在被访问，那么在近期它很可能还会被再次访问。

空间局部性（Spatial Locality）：如果一个存储器的位置被引用，那么将来他附近的位置也会被引用。

### 缓存一致性（MESI）：

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



## 进程和线程

进程是执行着的程序，它有自己的地址空间，是资源分配的单位，而线程是进程内部的一个执行序列。一个进程可以有多个线程，线程可共享进程资源，是调度和执行的单位。

> 程序是指令集，一个静态概念。一个程序的动态概念就是一个进程。

**创建线程的方式(未完)：**

假设用名为“Test”的类来实现。

（1）继承Thread类，重写该类的run方法，该run方法的方法体就代表了线程要完成的任务。创建Thread子类的实例，即创建了线程对象，之后调用线程对象的start()方法来启动该线程（直接用run不能启动新线程）。使用：`new Test().start();`

（2）实现Runnable接口，实现该接口的run()方法，该方法同上。创建 Runnable实现类的实例，并依此实例作为Thread的target来创建Thread对象，该Thread对象才是真正的线程对象。使用：

```java
//下面代码，两个线程共享一个rtt资源（Test类里面的一些成员变量）
Test rtt = new Test();
Runnable th1 = new Thread(rtt,"线程名1").start();
Test rtt = new Test();
Runnable th2 = new Thread(rtt,"线程名2").start();

//如果rtt只被调用一次，则可以这样写：
Runnable th = new Thread(() -> {...}}               
```

（3）通过Callable和Future创建线程。实现Callable<后面call()返回类型>接口，并实现call()方法，该call()方法将作为线程执行体，并且有返回值。使用：

实例化Callable实现类，使用FutureTask类来包装Callable对象，该FutureTask对象封装了该Callable对象的call()方法的返回值。使用FutureTask对象作为Thread对象的target创建并启动新线程。调用FutureTask对象的get()方法来获得子线程执行结束后的返回值

```java
FutureTask<Integer> ft = new FutureTask<Integer>(callable接口);
new Thread(ft,"有返回值的线程").start();
Integer res = ft.get()//等线程执行完毕后才有
```

> FutureTask实现了future和runnable接口

比较：（1）不能再继承类，可直接用this来访问当前进程。（2）和（3）需要用Thread.currentThread()来访问当前进程。（2）方便多个线程共享资源。（3）应用于有返回值的，抛检查异常和运行时异常。前面（1）（2）只能抛运行时异常

**线程状态**

新生状态：刚取得内存空间

就绪(Runnable): .start()后，进入队列（调度器），等CPU。

运行中(Running)：线程`run()`里面的代码正在被执行，结果是其他状态。如变为Runnable，因CPU执行了0.01s后还没完成，CPU就要离开执行其他任务；而阻塞，睡眠，等待结束后也要重新排队。

阻塞：有三种，线程通信中的Waiting（结束后会到同步阻塞），I/O阻塞，同步阻塞synchronized

睡眠中(Sleeping)：线程被强制睡眠，让出CPU。主要用于测试？实现线程切换，从而发现一些安全问题。比如多线程中对共享变量的修改，当线程1达到if条件（余额充足），然后进行修改（取款）。一般情况下没问题，但如果线程1执行慢了（通过sleep来模拟），即在达到条件后不能马上修改，而线程2更快地通过条件并修改了共享变量，那么当线程1再次修改变量时，线程1可能在达不到条件的情况下修改（余额不足下取款）。synchronized代码块是一种方案。

死亡(Dead)：线程完成了执行。

**补充**

优先级别：1-10，越高，运行的可能性越高。一般在new后设置。

join：用在start()后，thread.join()，这样thread线程执行完后才执行其他线程。

yield：让出CPU，但不用sleep，相当于时间片用完。让同级别或更高级别的线程获得更多机会。

setDaemon(true)：把线程设置为后台进程，这样，当主线程main结束，该线程也会停止（尽管是死循环的代码）。

interrupt()：不能终止死循环代码。



**线程通信**

“生产者—仓库—消费者”例子，生产者和消费者分别是两个线程，生产者交替生产product（实质上是修改product的成员变量，name和color），消费者get product的属性。为了两个线程共享这个product变量，在main中new product，并传入这两个线程的构造函数中。为了避免生产者修改name和color之间的时间被消费者消费product（出现错误的name和color配对），采取加锁方式，且要确保两个线程的执行需要同一把锁，所以用该共享product作为监听器，两个线程run里面的主代码（生成和消费）都加上锁synchronized。

通信：Object类都有wait, notify, notifyAll方法，规定写在synchronized代码块内部。wait是进入阻塞状态，并释放锁，阻塞状态解除后，排队（每个监视器一条队，和之前说的队不一样）。可加时间自动醒wait(time)。notify是随机唤醒，且唤醒后可能还要等锁。

product中增加一个flag变量作为判断是否有商品的指标。在生产者的synchronized代码中，加`if(product.flag) product.wait();`，生产后加`product.flag = false； product.notify();`。消费者思路相反。notify唤醒之后，会继续往下执行，如果像再确认product.flag，就用while。

> sleep和wait的区别，阻塞队列不一样，前者不放锁，但没有规定在synchronized才能用。



**线程池**

先创建一组线程放到一个group中，需要用时取，不需要放回，避免重复创建和销毁。

```java
//runnable
ExecutorService pool = Executors.newxxxThreadPool(); //Scheduled可以指定执行的延迟，速率等。
Runnable command = new MyRunnable();
pool.execute(command);//.submit配合future使用
pool.shutdown()；//shutdownNow()会中断正在执行的任务

//callable
ExecutorService pool = Executors.newxxxThreadPool();
List<Future> futureList = new ArrayList<>();
for(int i=0; i<20; i++){
    Callable<Integer> task = new MyCallable();
	Future future = pool.submit(task);
    futureList.add(future);
}
for(Future<Integer> f: futureLIst){
    Int res = future.get();//等线程执行完毕后才有，并继续执行。
}
pool.shutdown()；

//监控方法
getTaskCount, getCompletedTaskCount, getPoolSize（当前线程数）, getActiveCount()
```

线程池的7个属性：corePoolSize最小线程数，MaximumPoolSize， keepAliveTime和unit决定buffer的线程没工作时维持多久， workQueue任务队列，threadFactory，handler任务超过线程数时的策略，默认报错。

> ```
> .AbortPolicy:丢弃任务并抛出RejectedExecutionException异常。 
> .DiscardPolicy：也是丢弃任务，但是不抛出异常。 
> .DiscardOldestPolicy：丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
> .CallerRunsPolicy：由调用线程处理该任务 
> ```

设置策略：

- 计算密集型：CPU+1
- IO密集型：2倍CPU

### 并发任务

1.运行任务
单独处理器或同一处理器不同时间片。当任务小时，在同一线程，避免启动线程的开销。当任务计算量大时，一个处理器一个线程。

```java
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

```java
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

```java
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

线程安全类：当多个线程访问某个类时，不管运行时环境采用何种调度方式或者这些进程如何交替执行，并且在主调代码中不需要任何额外的同步或协同，这个类都能表现出正确的行为。

变量竞争：多个任务同时对一个变量进行修改

原子性：互斥访问。（看atomic包）

可见性：一个线程对主内存的修改可以及时被其他线程观察到。

有序性：在JMM中，允许编译器和处理器对指令进行重排序，不会影响单线程，但会影响多线程。

#### 原子性

##### Atomic 包

concurrent.atomic包，竞争激烈时性能比Lock好，但只能同步一个值。下面会介绍AtomicLong, LongAdder, AtomicReference, AtomicReferenceFieldUpdater, AtomicStampReference。

```java
//在main外
public static AtomicLong count = new AtomicLong();
//在main内
ExecutorService executor = Executors.newCachedThreadPool();
for (int i = 1; i <= 1000; i++) {
    int taskId = i;
    Runnable task = () -> {
        for (int k = 1; k <= 10000; k++)
            count.incrementAndGet();//每次加1，底层通过unsafe.getAndAddInt实现。它会在相加覆盖值前通过while检查主内存和公有变量的值是否一致，一致才相加并覆盖。
        System.out.println(taskId + ": " + count);
    };
    executor.execute(task);
}

//还有其他方法用于setting, adding, subtracting
```

当有大量线程访问原子值时，需要用下面的class（此情况下上面的方法效率低）
`LongAdder`不同线程有不同的累加器，最后才加总。所以用在上面代码的话，count不能保持稳定增加，但最后的结果“一般”是对的。要绝对正确，如序列化号生成等，还是要用atomic。

对于Boolean类型，compareAndSet(pre, update)是当原本为false，当执行了某个操作变为true，这里面保证原子性，该方法只有一个线程能调用。如果某段代码想它只执行一次，可以考虑用这个。AtomicReference\<T>的类都可以用。下面是类似的例子：

```java
//在Example1类中。AtomicIntegerFieldUpdater是对实例的成员变量进行修改。
private static AtomicIntegerFieldUpdater<Example1> updater =
            AtomicIntegerFieldUpdater.newUpdater(Example1.class, "count");

@Getter
public volatile int count = 100;

//main
Example1 example = new Example1();
if (updater.compareAndSet(example, 100, 99)) {
    log.info("update succeeded 1 ,{}", example.getCount());
}
```

AtomicStampReference是为了解决CAS的ABA问题，给变量加版本号。用法和上面差不多。

LongAccumulator

```
LongAccumulator largest = new LongAccumulator(Long::max, 90);
    largest.accumulate(42L);
    long max = largest.get();
```

##### 锁（最后的选择）

通常使用不可变数据或者将可变数据传递给另外一个线程来。如果必须共享，用线程安全的数据结构。

**synchronized和Lock**

synchronized依赖JVM，对作用域锁。适合竞争不激烈时用，可读性好。

Java中每一个对象都有一把锁和一个监视器（负责监视synchronized所做用的范围）。线程执行synchronized修饰的方法（粗粒度）或代码块（细粒度），就获得了该方法或代码块的锁，获取后其他线程要执行这些方法或代码块时就只能等待取走锁的线程完成执行任务后释放锁。

```Java
public synchronized void method1(){}//第一个执行的线程会取走此方法所在实例的锁（如果是static方法，就是所有实例的锁），该实例包括本方法和其他synchronized方法都不能被其他线程执行。method1不能并行或并发执行。所以对run方法加synchronized没意义。另外，如果子类继承这个方法，要重新写上synchronized。

public void method2(){
    synchronized (obj){//线程会取走obj监视器（一般为共享变量，只能是引用类型）的锁，如果obj是this，则效果和上面一样。如果obj是当前类，就相当于上面修饰静态方法的情况
        //内部不能改变obj的引用，即obj = xxx。因此，obj最好不是String或Integer，改值改引用，一定要用就用前加final。
    }
}
```

Lock依赖CPU指令，代码层面锁。可中断锁，多样化同步。性能比Atomic差。

独有功能：公平锁、分组唤醒需要唤醒的线程、中断等待锁的线程。

```java
private Lock lock = new ReentrantLock();//传入boolean决定是否公平锁
try{
    lock.lock();//tryLock没有就不锁，可设置timeout；tryInterruptibly等待锁时接受中断
    ...
}catch(Exception e){
    e.printStackTrace();
}final{
    lock.unlock()
}

//ReentrantReadWriteLock在没有任何读写锁的时候才能获取锁。了解，不适合读写极度不平衡情况
//StampedLock了解
```



**死锁**

线程1和2分别取走了obj1和obj2的锁，在释放锁之前，线程1和2分别要求获得obj2和obj的锁。下面是死锁的四个必要条件：

1. 互斥条件：一个资源每次只能被一个线程使用，即synchronized的出现。
2. 保持和请求条件：一个线程因请求资源而阻塞时（另一个对象的synchronized被另一线程执行），对已获得资源保持不放。
3. 不可剥夺调教：线程已获得资源，在未使用完成前，不能被剥夺。
4. 循环等待条件：若干线程之间形成一种头尾相接的循环等待资源关系。

解决方案：（用到锁的地方记得打印日志）

- 调整synchronized：同时对竞争的两个obj锁进行锁定

- 用ReentrantLock设置等待时间



#### 可见性

1.变量更新可见
final初始化后
static初始化后
volatile变量变化可见
在解锁前的变化可见（对于需要相同锁的）

**synchronized**线程安全：线程解锁前，必须把共享变量的值刷新到主内存。线程加锁时，情况工作内存中共享变量的值，从而使用共享变量时，需要从主内存中重新读取最新值。

**volatile**并非线程安全：

适合场景：状态标志量（对当前写操作不依赖于当前值）、double check

```java
volatile boolean inited = false;
//Thread1
context = loadContext();
inited = true;
//Thread2
while(!inited){
    sleep();
}
doSomethingWithConfig(context);

//线程安全的懒汉模式
public class SingletonExample5 {
    private SingletonExample5() {...}

    // 1、memory = allocate() 分配对象的内存空间
    // 2、ctorInstance() 初始化对象
    // 3、instance = memory 设置instance指向刚分配的内存

    // 单例对象 volatile + 双重检测机制 -> 禁止指令重排
    private volatile static SingletonExample5 instance = null;

    // 静态的工厂方法
    public static SingletonExample5 getInstance() {
        if (instance == null) { // 双重检测机制        // B
            synchronized (SingletonExample5.class) { // 同步锁
                if (instance == null) {
                    instance = new SingletonExample5(); // A - 3
                }
            }
        }
        return instance;
    }
}
```







#### 有序性

volatile、synchronized、lock

**规则**：没被这些规则规定的代码可乱序执行

- 程序次序规则：相对于单个线程有序

- 锁定规则：unlock先于后面对同一对象的lock

- volatile变量规则：对volatile变量的写先于后面对同一变量的读操作。对其修饰的变量进行写操作时，会在写操作之后加入一条StoreStore屏障指令，防止后面的写跳到前面，前面加Storeload指令，防止本volatile写与下面可能有的volatile读写重排序，最终按顺序地把本地内存中的共享变量刷新到主内存。而读操作，则会在操作前加入LoadLoad（防止与前面的普通读重排）和LoadStore（防止与下面的写重排）指令，从主内存中读取共享变量。这些指令间禁止重排序。总的来说，总能看到最新值。

- 传递规则：A先于B，B先于C，则A先于C

- 简单的四条，了解

  - 线程启动规则：start()先于该线程所有动作

  - 线程中断规则：interrupt()先于代码检测到中断事件
  - 线程终结规则：线程所有操作先于线程终止检测。Thread.join结束进程，Thread.isAlive检测是否已终止
  - 对象终止规则：对象初始化先于finalize()



#### 安全发布对象

使对象能被当前范围之外的代码使用。对象逸出：对象还没构造完就使它被其他线程使用

```java
//错误例子
//可以用getter调出states进行修改
private String[] states = {"a", "b"};
public String[] getter(){
    return states;
}

//下面代码中，Escape还没构造完，内部类的构造函数就调用了Escape的thisCanBeEscape。
public class Escape {
    private int thisCanBeEscape = 0;
    public Escape () {
        new InnerClass();
    }
    private class InnerClass {
        public InnerClass() {
            log.info("{}", Escape.this.thisCanBeEscape);
        }
    }
    public static void main(String[] args) {
        new Escape();
    }
}
```

**安全发布的四个方法**

- 在静态初始化函数中初始化一个对象。如下面懒汉模式利用静态工程方法实例化对象。

- 将对象的引用保存到volatile类型域或者AtomicRerence对象中。如上面“线程安全的懒汉模式”

- 将对象的引用保存到某个正确构造对象的final类型域中（stay tuned）
- 加对象的引用保存到一个由锁保护的域中。如下面懒汉模式用synchronized

```java
//懒汉模式，单例实例在第一次使用时进行创建。非线程安全。
public class SingletonExample1 {
    // 单例对象
    private static SingletonExample1 instance = null;
    // 私有构造函数
    private SingletonExample1() {...}    
    // 静态的工厂方法，加上synchronized线程安全，但性能消耗大。另一种方案是volatile + double check。看上面volatile的使用。
    public static SingletonExample1 getInstance() {
        if (instance == null) {
            instance = new SingletonExample1();
        }
        return instance;
    }
}
//也可以利用静态内部类实现
public class StaticInnerClassSingleton {
    private static class InnerClass{
        private static StaticInnerClassSingleton staticInnerClassSingleton = new StaticInnerClassSingleton();
    }
    public static StaticInnerClassSingleton getInstance(){
        return InnerClass.staticInnerClassSingleton;
    }
    private StaticInnerClassSingleton(){
        if(InnerClass.staticInnerClassSingleton != null){
            throw new RuntimeException("单例构造器禁止反射调用");
        }
    }
}

//饿汉模式，单例实例在类装载时进行创建。场景：该单例一定会被使用，且构造函数中的任务不能太多。
public class SingletonExample2 {
    // 私有构造函数
    private SingletonExample2() {
        //防反射进行构造（在懒汉模式的多线程就无法避免）
        if(instance != null){
            throw new RuntimeException("单例构造器禁止反射调用");
        }
    }
    // 单例对象
    private static final SingletonExample2 instance = new SingletonExample2();
    // 静态的工厂方法
    public static SingletonExample2 getInstance() {
        return instance;
    }
    //防止反序列化时变成其他对象。
    private Object readResolve(){
        return hungrySingleton;
    }
}

//枚举模式，推荐。反序列化和反射无影响。
public class SingletonExample7 {
    private SingletonExample7() {...}
    public static SingletonExample7 getInstance() {
        return Singleton.INSTANCE.getInstance();
    }
    private enum Singleton {
        INSTANCE;
        private SingletonExample7 singleton;
        // JVM保证这个方法绝对只调用一次
        Singleton() {
            singleton = new SingletonExample7();
        }
        public SingletonExample7 getInstance() {
            return singleton;
        }
    }
}

```

**final**

final修饰类，该类的发布是安全的，但再也不能被继承。

final方法，不想被继承类修改才会用。private方式隐式为final。

final变量，引用类型作用大减。可用immutable包中的数据结构来防止实现final效果，但它是运行时修改时会抛异常，可通过编译。



#### 线程封闭

堆栈封闭：局部变量无线程安全问题，全局“变量”才有。所有在方法内部用线程不安全类能提高性能。

线程私有空间：

```java
//线程级的局部变量
public static final ThreadLocal<NumberFormat> currencyFormat
    = ThreadLocal.withInitial(() -> NumberFormat.getCurrencyInstance());

String amountDue = currencyFormat.get().format(total);

//在Web开发中
public class RequestHolder {
    private final static ThreadLocal<Long> requestHolder = new ThreadLocal<>();
    public static void add(Long id) {//intercepter的preHandle调用，或者filter的doFilter中调用
        requestHolder.set(id);
    }
    public static Long getId() {//处理请求时可调用。
        return requestHolder.get();
    }
    public static void remove() {//intercepter的afterCompletion调用
        requestHolder.remove();
    }
}
```



#### 其他线程安全类与不安全类

> 注意：即便是线程安全类，也是可以在多线程中写出问题。比如遍历它们时进行remove操作（这个需求可以先遍历记录要删除的值，遍历完后再删除）。

StringBuilder -> StringBuffer安全，简单地用了synchronized

SimpleDateFormat -> FastDateFormat（commons-lang3）, joda-time(功能多一点)

`if (condition(a)) {handle(a);}`如果条件中的变量是共享的，那就要小心。



**同步容器**

ArrayList -> Vector, Stack

HashMap -> HashTable

Collections.synchronizedxxx(new collection类)安全，只需了解



**并发容器**

ArrayList -> CopyOnWriteArrayList写操作时先复制一份，这份复制会被加锁，不会在多线程下复制多份。适合读多写少的场景。读时不一定是最新值。

HashSet -> CopyOnWriteArraySet

TreeSet -> ConcurrentSkipListSet（批量方法all的方法不安全，它是调用collections的方法）

HashMap -> ConcurrentHashMap（性能通常比下面好）

TreeMap -> ConcurrentSkipListMap(并发程度对它影响不大)

AbstractQueuedSynchronizer(AQS)双向列表，伴随一个或多个可选的condition queue。用Node实现的FIFO的队列，用于构建锁或者其他同步装置的继承框架，要继承。利用int status表示总体获取锁的状态。可同时实现排他锁或共享锁模式。它的同步组件

- CountDownLatch通过计数判断线程是否还需要阻塞

```java
public class CountDownLatchExample2 {
    private final static int threadCount = 200;
    public static void main(String[] args) throws Exception {
        ExecutorService exec = Executors.newCachedThreadPool();
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i;
            exec.execute(() -> {
                try {
                    test(threadNum);
                } catch (Exception e) {
                    log.error("exception", e);
                } finally {
                    countDownLatch.countDown();//可以放到try中，但在这里的话，即使try出现异常也会计数
                }
            });
        }
        countDownLatch.await(10, TimeUnit.MILLISECONDS);//不设时间就等countDown减到0。等待try执行的时间，在10毫秒还没执行完就直接执行这里下面的代码了。可防止死等待（countDown一直不到0）
        log.info("finish");
        exec.shutdown();//等已启动的线程执行完才关闭
    }
    private static void test(int threadNum) throws Exception {
        Thread.sleep(100);
        log.info("{}", threadNum);
    }
}
```

- Semaphore控制同一时间并非线程的数目。适合在前一步线程数大于后一步可用的线程数时

```java
//接上面例子
final Semaphore semaphore = new Semaphore(3);//可用线程数
//try里面
semaphore.acquire(3); // 获取多个许可，不设就一个。有个tryAcquire方法，一次性获取，没获取的就不直接finally。也可设置等待时间；每次获取n个许可+等待时间
test(threadNum);
semaphore.release(3); // 释放多个许可
```

- CyclicBarrier：n个非主线程同时等待，直到规定数量的N个线程都满足条件后才执行。计数器可循环使用（数N个后再数N个）

```java
private static CyclicBarrier barrier = new CyclicBarrier(5, 可选runnable接口);//这个接口是规定5个线程准备好后，主线程先执行runnable任务，然后那5个线程才执行
//try里面。当await的线程等于5个时，5个线程一起继续执行后面的代码。
barrier.await();
doSomething
```

- ReentrantLock：具体看上面的Lock

- Condition：了解

- FutureTask：搜上面的“FutureTask”

- Fork/Join框架用的不多，一般任务并行流可处理。详细看《java 8 in action》

- BlockingQueue：用于生产者和消费者场景。

  四类方法：不能马上执行时，抛异常/ 返回特殊值/ 阻塞/ timesout

  - ArrayBlockingQueue：定长队列
  - DelayQueue：里面元素要实现delay接口。达到时间会被取出
  - LinkedBlockingQueue
  - PriorityBlockingQueue：可获得迭代器，但不保证按规定的优先级迭代。
  - SynchronousQueue：一个元素的队列



#### 策略

线程限制：ThreadLocal

可共享只读，不可修改的对象

线程安全对象

final变量，方法，甚至类

非私有方法不能返回可被修改的数据引用，返回复制更可考

使用本地变量

锁对象：最小化锁S=1/(1-a+a/n)

使用同步也不要使用线程的wait和notify

并发集合而不是加锁的同步集合

小心有状态对象



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



### 线程

```java
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
```



## 高并发（未完）

扩容：垂直或水平

缓存适合读多写少环境，更详细看“Redis”笔记

清空策略：FIFO（实时性）、LFU、LRU（热点数据）、过期时间等

本地缓存：Guava Cache

分布式缓存：Memcache、Redis



参考资料：

*Core_Java_SE_9_for_the_Impatient_2ed*

[CPU缓存一致性协议MESI](https://www.cnblogs.com/yanlong300/p/8986041.html)

[Java并发编程入门与高并发面试](https://coding.imooc.com/class/195.html)

[MESI protocol](https://en.wikipedia.org/wiki/MESI_protocol)