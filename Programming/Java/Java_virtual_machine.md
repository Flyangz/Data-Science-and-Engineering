# Java virtual machine

[TOC]

内存溢出：

`-xx:+HeapDumpOnOutOfMemoryError -Xms20m -Xmx20m`拍照，设置程序初始化时内存栈大小，和程序最大的内存栈大小

Idea下载Memory Analyzer的插件来查看拍照得到的文件，看是哪个object占用的内存最多。

Java技术体系：Java程序设计语言， 个硬件平台上的Java虚拟机，Class文件格式，Java API， 第三方Java类库

## Java管理的内存布局

线程独占区：虚拟机栈，本地方法栈，程序计数器

线程共享区：方法区，java堆

**程序计数器**：较小的内存空间，可看作当前线所执行的代码的行号指示器，便于CPU回到刚刚工作的地方。如果执行的是native方法，这个计数器的值为undefined。此区域唯一一个没有规定OutOfMemoryError的。

**虚拟机栈**：描述Java方法执行的动态内存模型。调用方法过多就会出现StackOverFlowError或OutOfMemory。

- 栈帧：每个方法执行都会创建一个，用来存储局部变量表，操作数栈，动态链接，方法出口等。

- 局部变量表：存放编译期可知的各种基本数据类型，引用类型，returnAddress类型。它们的内存空间（指引用，非实例所需内存）在编译期就完成了分配。当进入一个方法时，这个方法所需的栈帧的内存是固定的，运行期间不会改变局部变量表的大小。

**本地方法栈**：为native方法服务，其他和虚拟机栈类似。

**方法区**：存储虚拟机加载的类信息（类版本、字段、方法、接口）、常量、静态变量、即时编译器编译后的代码等数据。垃圾回收在方法区中的行为比较少。异常的定义。

- 运行时常量池Constant Pool Table。用于存放编译期生成的各种字面量和符号引用。Java语言并不要求常量一定只能在编译期产生，运行期间也可能产生新的常量，这些常量被放在运行时常量池中。这里所说的常量包括：基本类型包装类（包装类不管理浮点型，整形只会管理-128到127）和String（也可以通过String.intern()方法可以强制将String放入常量池）。例如`String s1 = "a";`，这个“a”会存到常量池中，里面相当于一个HashSet。如果是`String s2 = new String("a");`就会直接放到堆中，`s2.intern() == s1`返回true。所以注意这两种方法产生的值，用==比较时的效果。

**java堆**：空间最大，存放对象实例（不一定全部），垃圾收集器管理的主要区域。分有新生代（Eden、Survivor、Tenured Gen），老年代，永久代（java8前，但它是分在方法区中）。

> 直接内存：nio中的buffer对象所需内存，是堆外内存。

MetaSpace取代永久代解决永久代的溢出问题，但不属于堆内内存



## 类加载

new -> 根据new的参数在常量池中定位这个类的符号引用，如果没有找到，说明没有被加载，要进行加载和初始化 -> 

加载，验证，准备，解析，初始化

- 加载：将class文件字节码内容加载到内存中，并将静态数据转换为方法区中的运行时数据结构，在heap中生成这个类的对象（对象指针指向方法区的数据）。
- 准备：为static变量分配方法区中的内存，并设置初始值
- 解析：JVM常量池内符号引用替换为直接引用（每个类都有一个常量池）
- 初始化：执行类构造器`<clinit>()`（不是平时说的自己编写构造器）。编译器自动收集类中的所有static的语句合并产生。当发现类的父类没有初始化，则先初始化父类。JVM会保证一个类的`<clinit>()`线程安全。

调用构造函数

> 类初始化的情况：new；调用静态成员变量（非final）和方法；反射类；main方法所在的类，作为父类被初始化。
>
> 不会触发初始化情况：通过数组定义类引用；直接调用常量；自类访问父类的static域，自类不会被初始化。

```java
//下面代码，先按顺序加载Test中的static语句，并设置默认值。（准备阶段，还没赋值，如果static块放在public static前面，打印出Test.a为0）先执行Test.main再加载A类。
public class Test {
    
    public static int a = 100;
    
    static {
        System.out.println("initialize Test");
        System.out.println(Test.a);
    }
    
    public static void main(String[] args) {
        System.out.println("executing main");
        System.out.println(A.MAX); //调用final变量属于被动引用，不会初始化A
        A a = new A();
        System.out.println(A.width);
    }
}

class A {
    
    public static int width = 100;
    public static final int MAX = 100;

    static {
        System.out.println("initialize A");
    }

    public A() {
        System.out.println("creating A");
    }
}
```

**为对象分配堆内存**：指针碰撞，空闲列表。用哪种取决于垃圾回收的策略。

**线程安全**：线程同步，本地线程分配缓冲。前者性能低，后者为每个线程分配一份内存，当内存满了才通过线程同步来扩容。

**对象结构**：

- Header：自身运行时数据32bit或64bit，包括哈希值、GC分代年龄、锁状态、线程持有的锁、偏向线程ID、偏向时间戳，根据不同的锁状态给这些信息分配不同的大小。类型指针，指明对象是哪个类的实例
- InstanceData（同长度数据放在一起，父类信息在子类前），Padding（填充不足构成8kit的整数倍的空间）

**对象访问定位**：句柄，直接（HotSpot）。前者：栈通过线程共享区确定对象实例数据的位置（java堆）和对象类型数据的位置（方法区），这样即使GC，栈中的定位也不需要改变，但每次访问句柄也会影响效率。后者直接定位上述两个数据的位置。





## GC

**垃圾的判断**

- 引用计数法（基本不用）：引用一次+1，引用为null时-1，而达到0时被回收。无法解决内部（堆内）引用非0，而外部为0的回收。

- 可达性分析法：对象包括虚拟机栈，方法区的类属性、常量所引用的对象，本地方法栈

**回收方式**

- 策略：
  - 标记清除算法：在原地清除后，空间不连续，难一次性分配大空间，找不到时又会促发一次GC，从而有效率和空间性能问题
  - 复制算法：空间利用率低
  - 标记整理算法：耗时
  - 分代收集：上两种的结合
- 回收器
  - 串行Serial：新生代单线程，适合桌面应用。`-XX:+UseSerialGC`控制吞吐率。SerialOld用于老年代。
  - 并行Parallel：Parallel Scavenge：新生代多线程，CMS默认的新生代收集器，可控吞吐量（代码运行时间/(代码运行时加+GC时间)，Parallel Old。
  - 并发Cms：初始标记-并发标记-重新标记-并发清理。优点是并发收集低停顿，缺点是耗CPU资源，无法处理浮动垃圾，可能出现ConcurrentModeFailure（收集时程序创建的对象存放的空间不够，会出发大GC），空间碎片。jdk6以前,如果老年代使用了CMS,那么新生代只能使用Serial或ParNew收集器。
  - 并发G1：初始标记-并发标记-最终标记-筛选回收。并行与并发，分代收集，空间整合，可预测停顿

> **并行**：垃圾收集线程的并行，用户线程是等待的。
>
> **并发**：用户线程和垃圾回收线程同步执行（不一定并行，可能交替），收集时不会停顿用户线程。

**内存分配策略**

优先分配到YoungGen的eden，接下来空间不够，促发mini GC，如果survivor不够大放下旧的数据，旧的会直接到OldGen。

大对象直接老年代。

长期存活的老年代。

只要老年代的连续空间大于新生代对象总大小或者历次晋升的平均大小就会进行Minor GC，否则将进行Full GC。

逃逸分析和栈上分配。当方法体内部引用了外部的变量，就会逃逸。

young区复制算法，Old区用标记清楚或者标记整理

## 工具

`jps -mlv`：m参数，l全名，v虚拟机参数

`jstat -gcutil xxxx 1000 10`：每1s查xxxx的GC统计信息一次，查10次。-class/ -compiler等options

`jinfo -flag UseG1GC xxxx`:实时查看和调整虚拟机的各项参数。查看xxxx是否使用UseG1GC参数。

`jmap -dump:format=b,file=path xxxx`：拍照。分析一般看histogram和dominator_tree

`jstack`：打印线程信息。



Java Heap space区可大致分为Eden，survival1，survival2，老年区等。当Eden区满后会促发minor GC，存活的放到survival1，第二次满就将Eden和survival1存活的存到survival2，重复，多次存活放到老年区，老年区满了会促发GC。但如果某次存放到survival时满了，数据会直接到老年区，使得老年区更快满载而频繁GC。

```
-xx:+HeapDumpOnOutOfMemoryError //拍照 -XX:HeapDumpPath= //照片保存路径
-Xms20M //设置程序初始化时堆的大小
-Xmx20M //程序最大的堆大小
-Xmn10M //新生代10m
-verbose:gc -xx:+PrintGCDetail
-XX:MaxGCPauseMillis //收集器停顿时间
-XX:GCTimeRatio //吞吐量大小，0～100
-XX:SurvivorRatio=8 //8表示eden占80%，总量为10？
-XX:NewRatio=4 //4表示老年代占80%，总量为5？
-XX:PretenureSizeThreshold=6M //6M作为大对象，直接放入老年代
-XX:MaxTenuringThreshold=10 //存活10次移入老年代
-XX:TargetSurvivorRatio //垃圾回收存活比例，大于这个比例中的平均次数和上面的值的最小值就会成为老年代
-XX:+HandlePromotionFailure //+为开启，-为关闭。但这个参数在jdk 6好像已经不再使用。
-XX:+PrintFlagsFinal：打印jvm所有最终参数，:=表示修改过的。
```

