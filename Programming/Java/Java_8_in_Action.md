# Java 8 in Action
This notebook is written according to *Java 8 in Action*.

[TOC]



## Part 1 Fundamentals
### Chapter 1. Java 8: why should you care?
The Streams API supports many parallel operations to process data and resembles the way you might think in database query languages 
Techniques for passing code to methods
Default methods in interfaces

#### 1.1 Java 怎么还在变
某些语言只是更适合某些方面。比如，C和C++仍然是构建操作系统和各种嵌入式系统的流行工具，因为它们编出的程序尽管安全性不佳，但运行时占用资源少。Java和C#等安全型语言在诸多运行资源不太紧张的应用中已经取代了前者。

1.1.1 Java 在编程语言生态系统中的位置
Java虚拟机（JVM）及其字节码 可能会变得比Java语言本身更重要，而且对于某些应用来说， Java可能会被同样运行在JVM上的 竞争对手语言（如Scala或Groovy）取代。

Java是怎么进入通用编程市场的？
封装原则使得其软件工程问题比C少，“一次编写，随处运行”

程序员越来越多地要处理所谓的大数据，并希望利用多核计算机或计算集群来有效地处理。这意味着需要使用并行处理——Java以前对此并不支持。

1.1.2 流处理
`cat file1 file2 | tr "[A-Z]" "[a-z]" | sort | tail -3`
sort把一个行流作为输入，产生了另一个行流（进行排序）作为输出。请注意在Unix中，命令（ cat、 tr、sort和tail）是同时执行的，这样sort就可以在cat或tr完成前先处理头几行。

好处：
思路变成了把这样的流变成那样的流（就像写数据库查询语句时的那种思路），而不是一次只处理一个项目。
把输入的不相关部分拿到几个CPU内核上去分别执行你的Stream操作流水线

1.1.3 用行为参数化把代码传递给方法
让sort方法利用自定义的顺序进行比较。你可以写一个compareUsingCustomerId来比较两张发票的代码,而非写一个新Comparator对象

1.1.4 并行与共享的可变数据
并行：同时对不同的输入安全地执行。一般情况下这就意味着，你写代码时不能访问共享的可变数据。但如果要写入的是一个共享变量或对象，这就行不通了

这两个要点（1.1.4没有共享的可变数据，1.1.3将方法和函数即代码传递给其他方法的能力）是函数式编程范式的基石

1.1.5 Java 需要演变

#### 1.2 Java 中的函数
值是Java中的一等公民，但其他很多Java概念（如方法和类等）则是二等公民。用方法来定义类很不错，类还可以实例化来产生值，但方法和类本身都不是值。在运行时传递方法能将方法变成一等公民，这在编程中非常有用。函数式编程中的函数的主要意思是“把函数作为一等值”，不过它也常常隐含着第二层意思，即“执行时在元素之间无互动”

1.2.1 方法和 Lambda 作为一等公民
旧：把`isHidden`包在一个FileFilter对象里，然后传递给File.listFiles方法
```java
File[] hiddenFiles = new File(".").listFiles(new FileFilter() {
    public boolean accept(File file) {
        return file.isHidden();
    }
});
```
新
`File[] hiddenFiles = new File(".").listFiles(File::isHidden);`

`(int x) -> x + 1`

1.2.2 传递代码：一个例子
```java
public static boolean isGreenApple(Apple apple) {
    return "green".equals(apple.getColor());
}
public static boolean isHeavyApple(Apple apple) {
    return apple.getWeight() > 150;
}
public interface Predicate<T>{
    boolean test(T t);
} //平常只要从java.util.function导入就可以了
static List<Apple> filterApples(List<Apple> inventory, Predicate<Apple> p) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple: inventory){
        if (p.test(apple)) {
            result.add(apple);
        }
    }
    return result;
}
```
调用时`filterApples(inventory, Apple::isGreenApple);`

1.2.3 从传递方法到 Lambda
甚至都不需要为只用一次的方法写定义
`filterApples(inventory, (Apple a) -> "green".equals(a.getColor()) );`
`filterApples(inventory, (Apple a) -> a.getWeight() < 80 || "brown".equals(a.getColor()) );`

#### 1.3 流
```java
import static java.util.stream.Collectors.toList;
Map<Currency, List<Transaction>> transactionsByCurrencies =
    transactions.stream()
                .filter((Transaction t) -> t.getPrice() > 1000)
                .collect(groupingBy(Transaction::getCurrency));
```
多线程并非易事: 线程可能会同时访问并更新共享变量。因此，如果没有协调好，数据可能会被意外改变。Java 8也用Stream API（java.util.stream）解决了这两个问题：集合处理时的套路和晦涩，以及难以利用多核。

Collection主要是为了存储和访问数据，而Stream则主要用于描述对数据的计算。

顺序处理：
```java
import static java.util.stream.Collectors.toList;
List<Apple> heavyApples =
    inventory.stream().filter((Apple a) -> a.getWeight() > 150)
                      .collect(toList());
```
并行处理：
```java
import static java.util.stream.Collectors.toList;
List<Apple> heavyApples =
    inventory.parallelStream().filter((Apple a) -> a.getWeight() > 150)
                              .collect(toList());
```
#### 1.4 默认方法
主要是为了支持库设计师，让他们能够写出更容易改进的接口。
List调用sort方法。它是用Java 8 List接口中如下所示的默认方法实现的，它会调用Collections.sort静态方法：
```java
default void sort(Comparator<? super E> c) {
    Collections.sort(this, c); //this指Collections的具体变量
}
```
#### 1.5 来自函数式编程的其他好思想
Java中从函数式编程中引入的两个核心思想：将方法和Lambda作为一等值，以及在没有可变共享状态时，函数或方法可以有效、安全地并行执行。
其他：
`Optional<T>`类，如果你能一致地使用它的话，就可以帮助你避免出现NullPointer异常。
（结构）模式匹配，Java 8对模式匹配的支持并不完全

### Chapter 2. Passing code with behavior parameterization
行为参数化，就是把方法或者方法内部的行为作为参数传入另一方法中。
#### 2.1 应对不断变化的需求
背景：有一“列”苹果，需要根据不同标准（颜色，重量等）来筛选。当标准不断变化，如何处理？

下面是错误示范
```java
public static List<Apple> filterGreenApples(List<Apple> inventory) {
    List<Apple> result = new ArrayList<Apple>();
    for(Apple apple: inventory){
        if( "green".equals(apple.getColor() ) {//然后根据需求改这一行，或许有必要在上面增加引入的变量，如要判断color是否为red
            result.add(apple);
        }
    }
    return result;
}

//使用（符合更多标准时）
List<Apple> greenApples = filterApples(inventory, "green", 0, true);
```
这种方法实现不同需求，要写大量重复代码（只有评论和参数引入不同）
#### 2.2 行为参数化
进一步，写接口，每个标准写一个并实现该接口的方法，最后再写实现需求的方法。之后就可以使用了....
这种方法灵活，但依旧啰嗦
```java
//必写（当然，有的可以引用），可抽象为Predicate<T>
public interface ApplePredicate{
    boolean test (Apple apple);
}
//可省
public class AppleHeavyWeightPredicate implements ApplePredicate{
    public boolean test(Apple apple){
        return apple.getWeight() > 150;
    }
}
//必写,写法可能不一样（看2.4），另外可抽象为public static <T> List<T> filter(List<T> list, Predicate<T> p)
public static List<Apple> filterApples(List<Apple> inventory, ApplePredicate p){
    List<Apple> result = new ArrayList<>();
    for(Apple apple: inventory){
        if(p.test(apple)){
            result.add(apple);
        }
    }
    return result;
}
//可简，使用（记得要new）
List<Apple> redAndHeavyApples = filterApples(inventory, new AppleRedAndHeavyPredicate());
```
#### 2.3 简化
1.匿名类（简化一小步）
和上面一样，先写好接口和实现需求的方法，然后就可以用了。在用时再写上判断标准...
```java
List<Apple> redApples = filterApples(inventory, new ApplePredicate() {//接口名同样记得new，下面实现接口。如果实现中有this，则指该实现ApplePredicate
    public boolean test(Apple apple){
        return "red".equals(apple.getColor());
    }
})
```
2.Lambda 表达式（一大步，Java 8特征）
在接口处直接写所需参数，通过`->`后写行为
`List<Apple> result =
filterApples(inventory, (Apple apple) -> "red".equals(apple.getColor()));`
#### 2.4 真实的例子（暂时不需深究）
Java API包含很多可以用不同行为进行参数化的方法，如下面三种
1.用 Comparator 来排序
```java
public interface Comparator<T> {
    public int compare(T o1, T o2);
}

inventory.sort(new Comparator<Apple>() {
    public int compare(Apple a1, Apple a2){
        return a1.getWeight().compareTo(a2.getWeight());
    }
});

inventory.sort((Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight()));
```
2.用 Runnable 执行代码块
```java
public interface Runnable{
    public void run();
}

Thread t = new Thread(new Runnable() {
    public void run(){
        System.out.println("Hello world");
    }
});

Thread t = new Thread(() -> System.out.println("Hello world"));
```

3.GUI 事件处理
```java
Button button = new Button("Send");
button.setOnAction(new EventHandler<ActionEvent>() {
    public void handle(ActionEvent event) {
        label.setText("Sent!!");
    }
});

button.setOnAction((ActionEvent event) -> label.setText("Sent!!"));
```
### Chapter 3. Lambda expressions
#### 3.1 Lambda in a nutshell
根据chapter2中匿名方式和Lambda的比较，我们可以的出Lambda表达式实际是表示可传递的匿名函数的一种方式：它没有名称，但它有参数列表、函数主体、返回类型。（可能还有一个可以抛出的异常列表）

理论上来说，你在Java 8之前做不了的事情， Lambda也做不了。

基本语法
`(parameters) -> expression`一个string也是表达式
`(parameters) -> { statements; }`加上花括号，里面就要写语句，返回要return
一些例子
```java
//1
(String s) -> s.length()
//2
(Apple a) -> a.getWeight() > 150
//3
(int x, int y) -> {
    System.out.println("Result:");
    System.out.println(x+y);
}
//4
() -> 42
//5
(Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight())
//6
() -> {}
//7
() -> new Apple(10)
```
#### 3.2 在哪里以及如何使用 Lambda
1.函数式接口
在函数式接口上使用Lambda表达式。函数式接口就是只定义**一个**抽象方法的接口，如：
```java
public interface Predicate<T>{
    boolean test (T t);
}
```
当某个方法用`Predicate<T>`作为参数时，就可以用Lambda了
Lambda表达式可以被赋给一个变量，或传递给一个接受函数式接口作为参数的方法，如
`Runnable r1 = () -> System.out.println("Hello World 1");`
`process(() -> System.out.println("Hello World 3"));` process接受Runnable参数
tip:如果你用@FunctionalInterface定义了一个接口，而它却不是函数式接口的话，编译器将返回一个提示原因的错误。
2.函数描述符
函数式接口的抽象方法的签名基本上就是Lambda表达式的签名。我们将这种函数式接口的抽象方法(如下面的() -> void)的签名叫作函数描述符。
```java
//Lambda签名为() -> void，和Runnable的相同
execute(() -> {});
public void execute(Runnable r){
    r.run();
}
//可以在定义实践方法（指实现要求的主体方法）时直接用Lambda，签名为() -> String，和fetch相同
public Callable<String> fetch() {
    return () -> "Tricky example ;-)";
}
//签名为(Apple) -> Intege，Predicate不同；在实际当中其实不需要=及左部分
Predicate<Apple> p = (Apple a) -> a.getWeight();
```
#### 3.3 实战
环绕执行（ execute around） 模式: 资源处理（例如处理文件或数据库）时一个常见的模式就是打开一个资源，做一些处理，然后关闭资源。这个设置和清理阶段总是很类似，并且会围绕着执行处理的那些重要代码。
下面代码只能读文件的第一行。如果你想要返回头两行，甚至是返回使用最频繁的词，该怎么办呢
第一步，适合一种行为的方法：
```java
public static String processFile() throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
        return br.readLine();
    }
}//Java 7中的带资源的try语句，它已经简化了代码，因为你不需要显式地关闭资源了
```
第二步，思考一个接口，返回什么，这个返回又是通过什么得到的。下面返回头两行，需要有br，即BufferedReader
`String result = processFile((BufferedReader br) -> br.readLine() + br.readLine());`
写接口
```java
@FunctionalInterface
public interface BufferedReaderProcessor {
    String process(BufferedReader b) throws IOException;//和上面签名一致，接受BufferedReader -> String，还可以抛出IOException异常
}
```
第三步，修改实践方法
```java
public static String processFile(BufferedReaderProcessor p) throws IOException {
    try (BufferedReader br =
            new BufferedReader(new FileReader("data.txt"))) {
        return p.process(br);
    }
}
```
第四步，运用
`String twoLines = processFile((BufferedReader br) -> br.readLine() + br.readLine());`
#### 3.4 预设的函数式接口
1.
```java
//1.Predicate接受泛型T对象，并返回一个boolean，方法为test
//需求：表示一个涉及类型T的布尔表达式
//生成的Predicate<T>还能调用.and(), .or, .negate方法
@FunctionalInterface
public interface Predicate<T>{
    boolean test(T t);
}
//实际当中，引用上面就可以直接用了
Predicate<String> nonEmptyStringPredicate = (String s) -> !s.isEmpty();//与Predicate.isEmpty(s)相同？即使s为空也能工作。


//2.Consumer接受泛型T对象，没有返回（void），方法为accept。
//需求：访问类型T的对象，并对其执行某些操作
//其他方法andThen
@FunctionalInterface
public interface Consumer<T>{
    void accept(T t);
}


//3.Function接受一个泛型T的对象，并返回一个泛型R的对象，方法为apply
//需求：将输入对象的信息映射到输出
//其他方法andThen, compose, identity
@FunctionalInterface
public interface Function<T, R>{
    R apply(T t);
}
public static <T, R> List<R> map(List<T> list, Function<T, R> f) {
    List<R> result = new ArrayList<>();
    for(T s: list){
        result.add(f.apply(s));
    }
    return result;
}
// 将["lambdas","in","action"]转化为[7, 2, 6]
List<Integer> l = map(
                        Arrays.asList("lambdas","in","action"),
                        (String s) -> s.length()
);
//其他接口（详情看java.util.function,或core_java_for_the_impatient 3.6）
Runable ()-> void .run
Comparator<T,T,Integer> (T, T) -> int
Supplier<T> ()->T .get
UnaryOperator<T> T->T
BinaryOperator<T> (T,T)->T .apply
BiPredicate<L,R> (L,R)->boolean
BiConsumer<T,U> (T,U)->void
BiFunction<T,U,R> (T,U)->R
```

2.原始类型特化
boxing: Java里将原始类型转换为对应的引用类型的机制。装箱后的值本质上就是把原始类型包裹起来，并保存在堆里。因此，装箱后的值需要更多的内存，并需要额外的内存搜索来获取被包裹的原始值。
用`Predicate<Integer>`就会把参数1000装箱到一个Integer对象中,用`IntPredicate`可避免。一般来说，针对专门的输入参数类型的函数式接口的名称都要加上对应的原始类型前缀，比如DoublePredicate、 IntConsumer、 LongBinaryOperator、 IntFunction等。当然，输出也有。

不同的接口可是实现相同的Lambda，但效率可能不一样

3.关于异常
当没有办法自己创建一个接口时，比如`Function<T, R>`，可用下面的显式catch
```java
Function<BufferedReader, String> f = (BufferedReader b) -> {
    try {
        return b.readLine();
    }
    catch(IOException e) {
        throw new RuntimeException(e);
    }
};
```

#### 3.5 类型检查、类型推断以及限制
1.类型检查
实践方法所需参数，参数中的接口(目标类型`Predicate<Apple>`)，接口中的抽象方法，接受什么和返回什么，与Lambda比较是否匹配
如果Lambda表达式抛出一个异常，那么抽象方法所声明的throws语句也必须与之匹配
2.同样的 Lambda，不同的函数式接口
如果一个Lambda的主体是一个语句表达式， 它就和一个返回void的函数描述符兼容（当然需要参数列表也兼容）。
```java
// 合法，尽管Consumer返回了一个void
Consumer<String> b = s -> list.add(s);
```
3.类型推断
有时候显式写出类型更易读，有时候去掉它们更易读。
4.使用局部变量（类的方法中的变量）
```java
int portNumber = 1337;//必须显式声明为final，或事实上是final
Runnable r = () -> System.out.println(portNumber);
portNumber = 31337;//再加段就无法捕捉。这一限制不鼓励你使用改变外部变量的典型命令式编程模式（会阻碍很容易做到的并行处理
```
#### 3.6 方法引用
```java
inventory.sort((Apple a1, Apple a2)
                -> a1.getWeight().compareTo(a2.getWeight()));
//使用方法引用和java.util.Comparator.comparing）                
inventory.sort(comparing(Apple::getWeight));
```
1.In a nutshell
如果一个Lambda代表的只是“直接调用这个方法”，那最好还是用名称来调用它，而不是去描述如何调用它。如`Apple::getWeight`就是引用了Apple类中定义的方法getWeight。
实际是针对仅仅涉及单一方法的Lambda的语法糖

方法引用主要有三类
```java
//1.指向静态方法（Integer::parseInt）
(String s) -> Integer.parseInt(s)//Integer::parseInt

//2.指向实例方法
//第一个参数为方法接收者
(x, y) ->
x.compareToIgnoreCase(y) //String::compareToIgnoreCase

//3.指向现有对象的实例方法
//调用一个已经存在的外部对象中的方法
(args) -> expr.instanceMethod(args)//expr::instanceMethod

// 例子
() -> Thread.currentThread().dumpStack() //Thread.currentThread()::dumpStack
(String s) -> System.out.println(s) //System.out::println

```
2.构造函数引用
```java
//默认构造
Supplier<Apple> c1 = Apple::new; //() -> new Apple()
Apple a1 = c1.get();

//其他构造
Function<Integer, Apple> c2 = Apple::new;//(weight) -> new Apple(weight);
Apple a2 = c2.apply(110);
//对于三个及以上参数来构造的，要自己写一个interface
public interface TriFunction<T, U, V, R>{
    R apply(T t, U u, V v);
}

```
注意不同接口，相同的引用。只有在调用接口方法时才不同
```java
//上文的map可以如下引用，让map根据一个list来构造不同的object
public static List<Apple> map(List<Integer> list,
                              Function<Integer, Apple> f){
    List<Apple> result = new ArrayList<>();
    for(Integer e: list){
        result.add(f.apply(e));
    }
    return result;
}

List<Integer> weights = Arrays.asList(7, 3, 4, 10);
List<Apple> apples = map(weights, Apple::new);
```
一个有趣的实现：使用Map来将构造函数(`Function<Integer, Fruit>`)映射到字符串值。这样就可以通过string和构造所需参数获得相应的构造出来的对象
```java
static Map<String, Function<Integer, Fruit>> map = new HashMap<>();
static {
    map.put("apple", Apple::new);
    map.put("orange", Orange::new);
    // etc...
}
public static Fruit giveMeFruit(String fruit, Integer weight){
    return map.get(fruit.toLowerCase())
              .apply(weight);
```
#### 3.7 Lambda 和方法引用实战
用不同的排序策略给一个Apple列表排序
1.传递代码
`void sort(Comparator<? super E> c)`
它需要一个Comparator对象来比较两个Apple！这就是在Java中传递策略的方式：它们必须包裹在一个对象里。

```java
//普通方法
public class AppleComparator implements Comparator<Apple> {
    public int compare(Apple a1, Apple a2){
        return a1.getWeight().compareTo(a2.getWeight());
    }
}
inventory.sort(new AppleComparator());

//匿名方法
inventory.sort(new Comparator<Apple>() {
    public int compare(Apple a1, Apple a2){
        return a1.getWeight().compareTo(a2.getWeight());
    }
});

//Lambda（根据Comparator的函数描述符(T, T) -> int）
inventory.sort((Apple a1, Apple a2)//不写Apple也可
                -> a1.getWeight().compareTo(a2.getWeight())
);

//更进一步
//Comparator具有一个叫作comparing的静态辅助方法，它可以接受一个能提取Comparable键值的Function，并生成一个Comparator对象
Comparator<Apple> c = Comparator.comparing((Apple a) -> a.getWeight());//注意你现在传递的Lambda只有一个参数： Lambda说明了如何从苹果中提取需要比较的键值
//或
import static java.util.Comparator.comparing;
inventory.sort(comparing((a) -> a.getWeight()));

//最终
inventory.sort(comparing(Apple::getWeight));
```
逆向思路：对`List<Apple>`排序，由于Apple不是Comparator，所以需要用Comparator包裹，而产生Comparator的方式可以是1.comparing(提取Comparable键值的function)，恰好Apple本身就有.getWeight，而且只需一个function，所以为comparing(Apple::getWeight)

#### 3.8 复合 Lambda 表达式的有用方法
1.比较器复合
Comparator接口的一些默认方法：
逆序reversed
`comparing(Apple::getWeight).reversed()`
比较器链thenComparing
```java
inventory.sort(comparing(Apple::getWeight)
         .reversed()
         .thenComparing(Apple::getCountry));
```
2.谓词（一个返回boolean的函数）复合
Predicate接口的一些默认方法：
非negate
`Predicate<Apple> notRedApple = redApple.negate();`
链（从左向右确定优先级的）
```java
Predicate<Apple> redAndHeavyAppleOrGreen =
    redApple.and(a -> a.getWeight() > 150)
            .or(a -> "green".equals(a.getColor()));
```
3.函数复合
Function接口的一些默认方法：
andThen：从左往右f —> g
compose：从右往左f <— g
```java
Function<Integer, Integer> f = x -> x + 1;
Function<Integer, Integer> g = x -> x * 2;

Function<Integer, Integer> h = f.andThen(g);//g(f(x))
int result = h.apply(1);//返回4

Function<Integer, Integer> h = f.compose(g);//f(g(x))
int result = h.apply(1);//返回3

// 另一个例子
Function<String, String> addHeader = Letter::addHeader;
Function<String, String> transformationPipeline= 
                         addHeader.andThen(Letter::checkSpelling)
                                  .andThen(Letter::addFooter);
```
## Part 2. Functional-style data processing
### Chapter 4. Introducing streams
#### 4.1 流是什么
1.声明性，可复合，可并行
```java
List<String> lowCaloricDishesName =
                menu.stream()//利用多核架构用parallelStream()
                    .filter(d -> d.getCalories() < 400)
                    .sorted(comparing(Dish::getCalories))
                    .map(Dish::getName)
                    .collect(toList());
```
2.流简介
从支持数据处理操作的源生成的元素序列
元素序列：可以访问特定元素类型的一组有序值。但流的目的在于表达计算
源：从有序集合生成流时会保留原有的顺序
数据处理操作：支持类似于数据库的操作。顺序执行，也可并行执行。
#### 4.2 流与集合
集合与流之间的差异就在于什么时候进行计算。比如前者需要全部计算完在显示（在一个时间点上全体存在），后者元素则是按需计算的，算到哪显示到哪（数据分布在一段时间里）。
1.一个stream只能遍历一次(和迭代器类似)
```java
List<String> title = Arrays.asList("Java8", "In", "Action");
Stream<String> s = title.stream();
s.forEach(System.out::println);
s.forEach(System.out::println);//报错
```
集合可多次（不过其实第二次就相当于第二个迭代器了）
2.外部迭代与内部迭代
```java
//外部
List<String> names = new ArrayList<>();
for(Dish d: menu){
    names.add(d.getName());
    
//内部：Stream帮你把迭代做了，你只需要给出函数表示要做的事情
List<String> names = menu.stream()
                         .map(Dish::getName)
                         .collect(toList());
```
内部的好处：可以透明地并行处理，或者用更优化的顺序进行处理
####4.3 流操作（类似Spark的transform-action）
```java
menu.stream()
    .filter(d -> d.getCalories() > 300)
    .map(Dish::getName)
    .limit(3)
    .collect(toList());
```
1.intermediate operations

* 上面的代码，由于limit和短路技巧（stay tuned），filter和map只操作三个对象
* 尽管filter和map是两个独立的操作，但它们合并到同一次遍历中

常用中间操作
| 操 作        | 操作参数   |  函数描述符  |
| --------   | -----  | ---- |
|filter|`Predicate<T>`| T -> boolean|
|map   |`Function<T, R>`|T -> R|
|flatMap|`Function<T, Stream<R>>`| `T -> Stream<R>`|
|limit|||
|sorted|`Comparator<T>`|(T, T) -> int|
|distinct|
|skip|
2.terminal operations
`menu.stream().forEach(System.out::println);`
常用终端操作
| 操 作 | 返回类型 | 操作参数 | 函数描述符|
| -------- | -----  |-----  | ---- |
|anyMatch| boolean| `Predicate<T>`| T -> boolean|
|noneMatch | boolean| `Predicate<T>`| T -> boolean|
|allMatch| boolean| `Predicate<T>`| T -> boolean|
|findAny|`Optional<T>`|
|findFirst| `Optional<T>`|
|forEach| void| `Consumer<T>`| T -> void|
|collect|R|`Collector<T, A, R>`|
|reduce| `Optional<T>`| `BinaryOperator<T>`|(T, T) -> T|
|count | long|
forEach, count, collect
3.使用流
数据源-.stream()-中间操作-终端操作
### Chapter 5. Working with streams
#### 5.1 筛选和切片(略)
#### 5.2 映射
```java
//words为["Hello","World"]
List<String> uniqueCharacters =
    words.stream()
         .map(w -> w.split(""))// 得[["H","e","l","l", "o"],["W","o","r","l","d"]]流（stream<String[]>）
         .flatMap(Arrays::stream)//使得使用map(Arrays::stream)时生成的单个流都被合并起来（stream<String>）
         .distinct()
         .collect(Collectors.toList());
```
map是一变一，flatmap是多变一
```java
//[1, 2, 3]和[3, 4]变为总和能被3整除的数对[(2, 4), (3, 3)]
List<Integer> numbers1 = Arrays.asList(1, 2, 3);
List<Integer> numbers2 = Arrays.asList(3, 4);
List<int[]> pairs =
    numbers1.stream()
            .flatMap(i -> numbers2.stream()
                                  .filter(j -> (i + j) % 3 == 0)
                                  .map(j -> new int[]{i, j})
                    )
            .collect(toList());
```
#### 5.3 查找和匹配
都运用了短路技巧
anyMatch, allMatch, noneMatch
findAny,findFirst（并行时不合适）
```java
Optional<Dish> dish = //Optional<T>类是一个容器类，代表一个值存在或不存在
    menu.stream()
        .filter(Dish::isVegetarian)
        .findAny();//返回当前流中的任意元素
        .ifPresent(d -> System.out.println(d.getName());
```
Optional里面几种可以迫使你显式地检查值是否存在或处理值不存在的情形的方法
`isPresent()`, `ifPresent(Consumer<T> block)`, `orElse(T other)`
#### 5.4 归约
`int sum = numbers.stream().reduce(0, Integer::sum);`
`Integer::min`
并行化
`int sum = numbers.parallelStream().reduce(0, Integer::sum);`
但要并行执行也要付一定代价：传递给reduce的Lambda不能更改状态（如实例变量），而且要满足结合律。

Tips：流操作的状态
无状态：不需要知道过去流的计算记录，如map或filter
有状态:须知道,如有界的（reduce、 sum、 max）和无界的（sort、distinct）

#### 5.5 付诸实践（摘录）
```java
//1.
.distinct()
.collect(toList());
//上面可改为，当然，返回类型不一样
.collect(toSet())

//2.字符串的合并
.reduce("", (n1, n2) -> n1 + n2);
//用下面的更好
.collect(joining());

//3.找到交易额最小的交易
Optional<Transaction> smallestTransaction =
    transactions.stream()
                .min(comparing(Transaction::getValue));
```
#### 5.6 数值流
对`.reduce(0, Integer::sum);`的改进
Streams接口没有定义sum方法，因为一个像menu那样的`Stream<Dish>`，把各种菜加起来是没有任何意义的

1.原始类型流特化
映射到数值流：mapToInt、 mapToDouble和mapToLong
```java
.mapToInt(Dish::getCalories)
.sum()//特化后就可以用sum、max、 min、 average等方法了
```
返回的是一个特化流（此处为数值流IntStream），而不是Stream<T>
如果流是空的， sum默认返回0。

转换回对象流(把上面未用sum的IntStream转换回来)
`Stream<Integer> stream = intStream.boxed();`

默认值（针对非sum）
OptionalInt、 OptionalDouble和OptionalLong
在赋值给一个OptionalInt后，可以显示提供默认值,以防没有值
`int max = maxCalories.orElse(1);`

2.数值范围
range和rangeClosed
```java
IntStream evenNumbers = IntStream.rangeClosed(1, 100)
                                 .filter(n -> n % 2 == 0);//还没计算
```

3.数值流应用
生成a和b在100内的勾股三元数组，三个数都为整数
```java
Stream<double[]> pythagoreanTriples2 =
    IntStream.rangeClosed(1, 100).boxed()
             .flatMap(//注意flatMap的应用
                a -> IntStream.rangeClosed(a, 100)
                              .mapToObj(
                                 b -> new double[]{a, b, Math.sqrt(a*a + b*b)})
                              .filter(t -> t[2] % 1 == 0));//判断第三个数是否为整数用 (数 % 1 == 0)
```
注意上面正常map的话要在map前加boxed(),因为map会为流中的每个元素返回一个int数组，IntStream中的map方法只能为流中的每个元素返回另一个int
#### 5.7 构建流
```java
//由值创建流
Stream<String> stream = Stream.of("Java 8 ", "Lambdas ", "In ", "Action");
stream.map(String::toUpperCase).forEach(System.out::println);

//空流
Stream<String> emptyStream = Stream.empty();

//由数组创建流（numbers为int[]）
int sum = Arrays.stream(numbers).sum();

//由文件生成流，并计算不同单词的数量
long uniqueWords = 0;
try(Stream<String> lines =
            Files.lines(Paths.get("data.txt"), Charset.defaultCharset())){//会自动关闭
uniqueWords = lines.flatMap(line -> Arrays.stream(line.split(" ")))
                   .distinct()
                   .count();
}
catch(IOException e){}

//由函数生成流
//iterate
Stream.iterate(0, n -> n + 2)
      .limit(10)
      .forEach(System.out::println);
      
Stream.iterate(new int[]{0, 1},
                t -> new int[]{t[1], t[0]+t[1]})
      .limit(20)
      .forEach(t -> System.out.println("(" + t[0] + "," + t[1] +")"));

//generate
Stream.generate(Math::random)
      .limit(5)
      .forEach(System.out::println);
```
### Chapter 6. Collecting data with streams
```java
Map<Currency, List<Transaction>> transactionsByCurrencies =
        transactions.stream().collect(groupingBy(Transaction::getCurrency));
```
#### 6.1 收集器简介
收集器定义collect用来生成结果集合的标准。
对流调用collect方法将对流中的元素触发一个归约操作（由Collector来参数化）。
三大功能：将流元素归约和汇总为一个值，元素分组，元素分区
#### 6.2 归约和汇总
数一数菜单里有多少种菜
`long howManyDishes = menu.stream().count();`
```java
//1.查找流中的最大值和最小值
Comparator<Dish> dishCaloriesComparator =
    Comparator.comparingInt(Dish::getCalories);
Optional<Dish> mostCalorieDish =
    menu.stream()
        .collect(maxBy(dishCaloriesComparator));

//2.汇总（求和，averagingInt，SummaryStatistics）
int totalCalories = menu.stream().collect(summingInt(Dish::getCalories));//summingInt可接受一个把对象映射为求和所需int的函数，并返回一个收集器

IntSummaryStatistics menuStatistics =
        menu.stream().collect(summarizingInt(Dish::getCalories));
//打印结果
IntSummaryStatistics{count=9, sum=4300, min=120,
                     average=477.777778, max=800}

//3.连接字符串
//返回的收集器会把对流中每一个对象应用toString方法得到的所有字符串连接成一个字符串
String shortMenu = menu.stream().map(Dish::getName).collect(joining(", "));
#如果Dish类有一个toString方法
String shortMenu = menu.stream().collect(joining(", "));

//4.广义的归约汇总reducing
//求和需要三个参数：起始值、转换函数、累积函数<T,T,T>
int totalCalories = menu.stream().collect(reducing(
                0, Dish::getCalories, Integer::sum));
//或
menu.stream().map(Dish::getCalories).reduce(Integer::sum).get();
//或（首推，简单，没有箱操作）
menu.stream().mapToInt(Dish::getCalories).sum();

//求最值
Optional<Dish> mostCalorieDish =
    menu.stream().collect(reducing(
        (d1, d2) -> d1.getCalories() > d2.getCalories() ? d1 : d2));
        
//求数量
reducing(0L, e -> 1L, Long::sum)
```
reduce方法旨在把两个值结合起来生成一个新值，它是一个不可变的归约。一旦以错误的语义使用reduce，就难以并行（影响性能）。
collect适合表达可变容器上的归约和并行操作

#### 6.3 分组groupingBy
groupingBy(f)实际上是groupingBy(f, toList())，其结果是`map<key,List<value>>`
```java
//有方法转换成所需标准的
Map<Dish.Type, List<Dish>> dishesByType =
                      menu.stream().collect(groupingBy(Dish::getType));

//自定义标准
public enum CaloricLevel { DIET, NORMAL, FAT }//枚举
Map<CaloricLevel, List<Dish>> dishesByCaloricLevel = menu.stream().collect(
        groupingBy(dish -> {
                if (dish.getCalories() <= 400) return CaloricLevel.DIET;
                else if (dish.getCalories() <= 700) return
    CaloricLevel.NORMAL;
        else return CaloricLevel.FAT;
        } ));

//1.多级分组
//二级groupingBy情况下为`map<key,map<key,List<value>>>`
Map<Dish.Type, Map<CaloricLevel, List<Dish>>> dishesByTypeCaloricLevel =
menu.stream().collect(
      groupingBy(Dish::getType, 上一段代码的groupingBy)
);
        

Map<Dish.Type, Long> typesCount = menu.stream().collect(


//2.按子组收集数据                    
//求数量
Map<Dish.Type, Long> typesCount = menu.stream().collect(
                    groupingBy(Dish::getType, counting()));

//求最值
Map<Dish.Type, Dish> mostCaloricByType =
    menu.stream()
        .collect(groupingBy(Dish::getType,
                 collectingAndThen(
                    maxBy(comparingInt(Dish::getCalories)),
                 Optional::get)));//去掉maxBy产生的optional

//与mapping配合
//和之前两个groupingBy相比，要加上第二个参数toSet()，也可以是toCollection(HashSet::new) ；结果为map<key, Set<value>>，即只有CaloricLevel，没有菜名
Map<Dish.Type, Set<CaloricLevel>> caloricLevelsByType =
menu.stream().collect(
groupingBy(Dish::getType, mapping(上面根据calories分组的Lambda, toSet() )));
```
#### 6.4 分区partitioningBy
分组的特殊情况：由一个谓词（返回一个布尔值函数，只能两组）作为分类（分区）函数
```java
Map<Boolean, List<Dish>> partitionedMenu =
            menu.stream().collect(partitioningBy(Dish::isVegetarian));

List<Dish> vegetarianDishes = partitionedMenu.get(true);
```
1.分区的优势
和groupingBy一样可以多级分

```java
Map<Boolean, Dish> mostCaloricPartitionedByVegetarian =
menu.stream().collect(
    partitioningBy(Dish::isVegetarian,
        collectingAndThen(maxBy(comparingInt(Dish::getCalories)),
                          Optional::get)));
//结果
{false=pork, true=pizza}
```
2.将数字按质数和非质数分区

```java
//分区函数
public boolean isPrime(int candidate) {
    int candidateRoot = (int) Math.sqrt((double) candidate);
    return IntStream.rangeClosed(2, candidateRoot)
                    .noneMatch(i -> candidate % i == 0);//全为false是返回true，即不能被（2，candidateRoot）里面任何一个数整除
}

//生成质数和非质数区
public Map<Boolean, List<Integer>> partitionPrimes(int n) {
    return IntStream.rangeClosed(2, n).boxed()
                    .collect(partitioningBy(candidate -> isPrime(candidate)));
}
```

#### Collectors类的静态工厂方法表
| 工厂方法 | 操作参数   |  使用示例  |
| --------   | -----  | ---- |
|toList| `List<T>`|  `List<Dish> ... .collect(toList())`|
|toSet |`Set<T>`| `Set<Dish> ... (toSet())`|
|toCollection| `Collection<T>`|`Collection<Dish> ... (toCollection(),ArrayList::new)`|
|counting |Long| `long ... (counting())`|
|summingInt| Integer|`int ... (summingInt(Dish::getCalories))`|
|averagingInt|Double|`double ... (averagingInt(Dish::getCalories))`|
|summarizingInt| IntSummaryStatistics|`IntSummaryStatistics... (summarizingInt(Dish::getCalories))`|
|joining| String| `String ... .map(Dish::getName).collect(joining(", "))`|
|maxBy/minBy  |`Optional<T>`|`Optional<Dish> ... (maxBy(comparingInt(Dish::getCalories)))`|
|reducing| 归约操作产生的类型|`int... (reducing(0, Dish::getCalories, Integer::sum))`|
|collectingAndThen |AndThen转换的类型|`int ... (collectingAndThen(toList(), List::size))`|
|groupingBy| `Map<K, List<T>>` |`Map<Dish.Type,List<Dish>> ... (groupingBy(Dish::getType))`|
|partitioningBy| `Map<Boolean,List<T>>`| `Map<Boolean,List<Dish>> ... (partitioningBy(Dish::isVegetarian))` |

#### 6.5 收集器接口
```
public interface Collector<T, A, R> {
    Supplier<A> supplier();
    BiConsumer<A, T> accumulator();
    Function<A, R> finisher();
    BinaryOperator<A> combiner();
    Set<Characteristics> characteristics();
}
```
对于实现一个`ToListCollector<T>`类，将`Stream<T>`中的所有元素收集到一个`List<T>`里，它的代码如下
```java
public class ToListCollector<T> implements Collector<T, List<T>, List<T>> {
    
    @Override
    //1.supplier方法必须返回一个无参数函数，在调用时它会创建一个空的累加器实例，供数据收集过程使用。
    public Supplier<List<T>> supplier() {
        return ArrayList::new;
    }
    
    @Override
    //2.accumulator方法会返回执行归约操作的函数
    //两个参数：保存归约结果的累加器（已收集了流中的前n-1个项目），还有第n个元素本身
    public BiConsumer<List<T>, T> accumulator() {
        return List::add;
    }
    
    @Override
    //3.在遍历完流后， finisher方法将累加器对象转换为最终结果
    public Function<List<T>, List<T>> finisher() {
        return Function.indentity();//无需进行转换
    }
    
    @Override
    //4.combiner方法会返回一个供归约操作使用的函数，它定义了并行处理时如何合并
    public BinaryOperator<List<T>> combiner() {
        return (list1, list2) -> {
            list1.addAll(list2);
            return list1;
        };
    }
   
    @Override
    //5.characteristics会返回一个不可变的Characteristics集合，它定义了收集器的行为:UNORDERED,CONCURRENT(用了UNORDERED或者无序数据源才用),IDENTITY_FINISH（暗示将累加器A不加检查地转换为结果R是可行的）
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(
            IDENTITY_FINISH, CONCURRENT));
    }
}
```

实现时`List<Dish> dishes = menuStream.collect(new ToListCollector<Dish>());`
标准下`List<Dish> dishes = menuStream.collect(toList());`
#### 6.6 开发你自己的收集器以获得更好的性能
优化上面提到的质数和非质数分区
1.仅用质数做除数
```
//takeWhile的方法，给定一个排序列表和一个谓词，它会返回元素满足谓词的最长前缀。
//即用于产生小于candidateRoot的质数列表
public static <A> List<A> takeWhile(List<A> list, Predicate<A> p) {
    int i = 0;
    for (A item : list) {
        if (!p.test(item)) {
            return list.subList(0, i);
        }
        i++;
    }
    return list;
}

//根据上面的表对candidate进行判断（是否为质数）
public static boolean isPrime(List<Integer> primes, int candidate){
    int candidateRoot = (int) Math.sqrt((double) candidate);
    return takeWhile(primes, i -> i <= candidateRoot)
                    .stream()
                    .noneMatch(p -> candidate % p == 0);
}
```
自定义collector
```java
//确定类的签名，这里收集Integer流，累 加 器 和 结 果 类 型如下
public class PrimeNumbersCollector 
        implements Collector<Integer,
                             Map<Boolean, List<Integer>>,
                             Map<Boolean, List<Integer>>> {

    @Override
    //不但创建了用作累加器的Map，还为true和false两个键下面初始化了对应的空列表
    public Supplier<Map<Boolean, List<Integer>>> supplier() {
        return () -> new HashMap<Boolean, List<Integer>>() {{
                                put(true, new ArrayList<Integer>());
                                put(false, new ArrayList<Integer>());
                                }};
    }
    
    @Override
    //现在在任何一次迭代中，都可以访问收集过程的部分结果，也就是包含迄今找到的质数的累加器
    public BiConsumer<Map<Boolean, List<Integer>>, Integer> accumulator() {
        return (Map<Boolean, List<Integer>> acc, Integer candidate) -> {
                acc.get(isPrime(acc.get(true), candidate))//判断后得true_list或false_list
                   .add(candidate);//符合的添加到相应的List
                };
    }
    
    @Override
    //实际上这个收集器是不能并行使用的，因为该算法本身是顺序的。这意味着永远都不会调用combiner方法
    public BinaryOperator<Map<Boolean, List<Integer>>> combiner() {
        return (Map<Boolean, List<Integer>> map1,
                Map<Boolean, List<Integer>> map2) -> {
                    map1.get(true).addAll(map2.get(true));
                    map1.get(false).addAll(map2.get(false));
                    return map1;
                };
    }
    @Override
    //用不着进一步转换
    public Function<Map<Boolean, List<Integer>>,
                    Map<Boolean, List<Integer>>> finisher() {
                        return Function.identity();
    }
    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(IDENTITY_FINISH));
    }
}
```
使用
```
public Map<Boolean, List<Integer>>
                    partitionPrimesWithCustomCollector(int n) {
    return IntStream.rangeClosed(2, n).boxed()
                    .collect(new PrimeNumbersCollector());
}
```
### Chapter 7. Parallel data processing and performance
#### 7.1 并行流
parallelStream和sequential
配置并行流使用的线程池:
并行流内部使用了默认的ForkJoinPool，它默认的线程数量就是你的处理器数量，这个值是由Runtime.getRuntime().availableProcessors()得到的。
可通过`System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism","12");`改，但不建议
1.测量流性能
启示：
iterate生成的是装箱的对象，必须拆箱成数字才能求和；
很难把iterate分成多个独立块来并行执行（你必须意识到某些流操作比其他操作更容易并行化）

而采用rangeClosed：原始类型的long数字，没有装箱拆箱的开销；会生成数字范围，很容易拆分为独立的小块
```java
public static long rangedSum(long n) {
    return LongStream.rangeClosed(1, n)
                     .parallel()
                     .reduce(0L, Long::sum);
}
```
除了数据产生方式，数据结构之外，我们还需保证在内核中并行执行工作的时间比在内核之间传输数据的时间长。
2.正确使用并行流
```java
//一个错误示范
public static long sideEffectSum(long n) {
    Accumulator accumulator = new Accumulator();
    LongStream.rangeClosed(1, n).forEach(accumulator::add);
    return accumulator.total;
}
public class Accumulator {
    public long total = 0;
    public void add(long value) { total += value; }
}

public static long sideEffectParallelSum(long n) {
    Accumulator accumulator = new Accumulator();
    LongStream.rangeClosed(1, n).parallel().forEach(accumulator::add);//这里foreach里的accumulator被不同线程抢用
    return accumulator.total;
}
```
共享可变状态会影响并行流以及并行计算，所以要避免
3.高效使用并行流
任何关于什么时候该用并行流的定量建议都是不可能也毫无意义的——机器不同

* 有疑问，测量
* 留意装箱
* 某些操作本身不适合并行，如limit和findFirst等依赖于元素顺序的操作。findFirst可以换findAny，或调用unordered来把有序流变无序。对无序流用limit可能会有改观
* 考虑流的操作流水线的总计算成本。设N是要处理的元素的总数， Q是一个元素通过流水线的大致处理成本，QN为总成本。 Q值较高就意味着使用并行流可能更好。
* 数据量
* 数据结构：如ArrayList的拆分效率比LinkedList高得多。HashSet和TreeSet都比较好。可以自己实现Spliterator来完全掌控分解过程
* 流自身的特点，以及流水线中的中间操作修改流的方式，都可能会改变分解过程的性能。如经过filter后的流的大小是未知的。
* 考虑终端操作中合并步骤的代价

#### 7.2 分支/合并框架
1.使用 RecursiveTask
把任务提交到这个池，必须创建`RecursiveTask<R>`的一个子类，其中R是并行化任务（以及所有子任务）产生的结果类型，或者如果任务不返回结果，则是RecursiveAction类型
```java
public class ForkJoinSumCalculator
             extends java.util.concurrent.RecursiveTask<Long> {
             
    private final long[] numbers;
    private final int start;
    private final int end;
    
    public static final long THRESHOLD = 10_000;
    
    public ForkJoinSumCalculator(long[] numbers) {
        this(numbers, 0, numbers.length);
    }
    
    private ForkJoinSumCalculator(long[] numbers, int start, int end) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }
    
    @Override
    protected Long compute() {
        int length = end - start;
        if (length <= THRESHOLD) {
            return computeSequentially();
        }
        ForkJoinSumCalculator leftTask =
            new ForkJoinSumCalculator(numbers, start, start + length/2);
        leftTask.fork();
        ForkJoinSumCalculator rightTask =
            new ForkJoinSumCalculator(numbers, start + length/2, end);
        Long rightResult = rightTask.compute();
        Long leftResult = leftTask.join();
        return leftResult + rightResult;
    }
    private long computeSequentially() {
        long sum = 0;
        for (int i = start; i < end; i++) {{
            sum += numbers[i];
        }
        return sum;
    }
}
```
使用
```java
public static long forkJoinSum(long n) {
    long[] numbers = LongStream.rangeClosed(1, n).toArray();//必须先要把整个数字流都放进一个long[]，所以性能会差一点
    ForkJoinTask<Long> task = new ForkJoinSumCalculator(numbers);//ForkJoinTask（RecursiveTask的父类）
    return new ForkJoinPool().invoke(task);
}
```
在实际应用时，使用多个ForkJoinPool是没有什么意义的。这里创建时用了其默认的无参数构造函数，这意味着想让线程池使用JVM能够使用的所有处理器（Runtime.availableProcessors的返回值，包括超线程生成的虚拟内核）。

2.使用分支/合并框架的最佳做法

* 对一个任务调用join方法会阻塞调用方，直到该任务做出结果。因此，有必要在两个子任务的计算都开始之后再调用它。
* 不应该在RecursiveTask内部使用ForkJoinPool的invoke方法。相反，你应该始终直接调用compute或fork方法，只有顺序代码才应该用invoke来启动并行计算。
* 对子任务调用fork方法可以把它排进ForkJoinPool，但不要左右都用。
* 调试使用分支/合并框架的并行计算比较麻烦
* 一个惯用方法是把输入/输出放在一个子任务里，计算放在另一个里，这样计算就可以和输入/输出同时进行。
* 分支/合并框架需要“预热”或者说要执行几遍才会被JIT编译器优化。
* 编译器内置的优化可能会为顺序版本带来一些优势（例如执行死码分析——删去从未被使用的计算）
* 设定好最小划分标准

3.工作窃取
分出大量的小任务一般来说都是一个好的选择。因为，理想情况下，划分并行任务时，能让所有的CPU内核都同样繁忙。即便实际中子任务花的时间差别还是很大，利用work stealing能解决这一问题。其过程是空闲的线程窃取其他线程的工作。
#### 7.3 Spliterator（略）
虽然在实践中可能用不着自己开发Spliterator，但了解一下它的实现方式会让你对并行流的工作原理有更深入的了解。 
```java
public interface Spliterator<T> {
    boolean tryAdvance(Consumer<? super T> action);//类似于普通的Iterator
    Spliterator<T> trySplit();//划分元素给其他Spliterator使用
    long estimateSize();//估计还剩下多少元素要遍历
    int characteristics();
}
```
## Part 3. Effective Java 8 programming
### Chapter 8. Refactoring, testing, and debugging
#### 8.1 为改善可读性和灵活性重构代码
1.从匿名类到 Lambda 表达式的转换
注意事项：在匿名类中， this代表的是类自身，但是在Lambda中，它代表的是包含类
匿名类可以屏蔽包含类的变量，而Lambda表达式不
能（它们会导致编译错误）
```java
//下面会出错
int a = 10;
Runnable r1 = () -> {
    int a = 2;
    System.out.println(a);
};
```
当以某预设接口相同的签名声明函数接口时，Lambda要加上标注区分
```java
//例如下面接口用了相同的函数描述符<T> -> void
public static void doSomething(Runnable r){ r.run(); }
public static void doSomething(Task a){ a.execute(); }
//Lambda要在前面表明是哪个
doSomething((Task)() -> System.out.println("Danger danger!!"));
```
2.从 Lambda 表达式到方法引用的转换

将之前的dishesByCaloricLevel方法进行修改。
把groupingBy里面的内容改为Dish里面的一个方法getCaloricLevel，这样就可以在groupingBy里面用方法引用了（）Dish::getCaloricLevel

尽量考虑使用静态辅助方法，比如comparing、 maxBy。如`inventory.sort(comparing(Apple::getWeight));`

用内置的集合类而非map+reduce，如`int totalCalories = menu.stream().collect(summingInt(Dish::getCalories));`

3.从命令式的数据处理切换到 Stream
所有使用迭代器这种数据处理模式处理集合的代码都转换成Stream API的方式。因为Stream清晰，而且可以进行优化
但这是一个困难的任务，需要考虑控制流语句(一
些工具可以帮助我们完成)
```java
//筛选和抽取的混合，不好并行
List<String> dishNames = new ArrayList<>();
for(Dish dish: menu){
    if(dish.getCalories() > 300){
        dishNames.add(dish.getName());
    }
}

menu.parallelStream()
    .filter(d -> d.getCalories() > 300)
    .map(Dish::getName)
    .collect(toList());
```
4.灵活性
**有条件的延迟执行**
```java
//问题代码
if (logger.isLoggable(Log.FINER)){
    logger.finer("Problem: " + generateDiagnostic());
}
//日志器的状态（它支持哪些日志等级）通过isLoggable方法暴露给了客户端代码
//每次输出一条日志之前都去查询日志器对象的状态

//改进
//Java 8替代版本的log方法的函数签名如下
public void log(Level level, Supplier<String> msgSupplier)

logger.log(Level.FINER, () -> "Problem: " + generateDiagnostic());//在检查完该对象的状态之后才调用原来的方法
```
如果你发现你需要频繁地从客户端代码去查询一个对象的状态（比如前文例子中的日志器的状态），只是为了传递参数、调用该对象的一个方法（比如输出一条日志），那么可以考虑实现一个新的方法，以Lambda或者方法表达式作为参数，新方法在检查完该对象的状态之后才调用原来的方法

**环绕执行**
同样的准备和清理阶段
上面有例子，搜索环绕执行

#### 8.2 使用Lambda重构面向对象的设计模式

1.策略模式
算法接口， 算法实现，客户

思路：
策略的函数签名，判断是否有预设的接口
创建/修改类（含有实现某接口的构造器，调用该接口方法的方法）
```java
//下面，Validator类接受实现了ValidationStrategy接口的对象为参数
public class Validator{
    private final ValidationStrategy strategy;
    public Validator(ValidationStrategy v){
        this.strategy = v;
    }
    public boolean validate(String s){
        return strategy.execute(s); }//execute为ValidationStrategy接口的方法，该接口签名为String -> boolean
        
//创建具有特定功能（通过符合签名的Lambda传入）的类
Validator v3 = new Validator((String s) -> s.matches("\\d+"));
//使用该类
v3.validate("aaaa");
```
2.模版方法
如果你需要采用某个算法的框架，同时又希望有一定的灵活度，能对它的某些部分进行改进。
例如上面的例子，希望只用一个Validator，且保留validate方法。为了保持策略的多样，需要对validate方法进行改进，这可以给方法引入第二个参数（函数接口），从而提高方法的灵活性。书中有一个类似的例子，构建一个在线银行应用，在保持只有一个银行类的情况下，让相同的方法给客户不同的反馈。如下：
```java
public void processCustomer(int id, Consumer<Customer> makeCustomerHappy){
    Customer c = Database.getCustomerWithId(id);
    makeCustomerHappy.accept(c);
}
```
3.观察者模式（简单情况下可用Lambda）
某些事件发生时（如状态转变），一个对象（主题）需要自动通知多个对象（观察者）
简单来说，一个主题类有观察者名单，有一方法（包含通知参数）能遍历地调用观察者的方法（接受通知参数，并作相应行为）
例子：
```java
//观察者实现的接口
interface Observer {
    void notify(String tweet);
}
//其中一个观察者类
class NYTimes implements Observer{
    public void notify(String tweet) {
        if(tweet != null && tweet.contains("money")){
            System.out.println("Breaking news in NY! " + tweet);
        }
    }
}
//主题接口
interface Subject{
    void registerObserver(Observer o);
    void notifyObservers(String tweet);
}
//主体类
class Feed implements Subject{
    private final List<Observer> observers = new ArrayList<>();
    public void registerObserver(Observer o) {
        this.observers.add(o);
    }
    public void notifyObservers(String tweet) {
        observers.forEach(o -> o.notify(tweet));
    }
}

//上面的简单例子能用下面的Lambda实现，只需一个主题类即可
f.registerObserver((String tweet) -> {
    if(tweet != null && tweet.contains("money")){
        System.out.println("Breaking news in NY! " + tweet);
    }
});
```
4.责任链模式
创建处理对象序列（比如操作序列）的通用方案
通常做法是构建一个代表处理对象的抽象类来实现。如下
```java
//抽象类有一个同类的successor的protected变量，设置successor的方法，处理任务的抽象方法，整合任务处理以及传递的handle方法
public abstract class ProcessingObject<T> { 
    
    protected ProcessingObject<T> successor;
    
    public void setSuccessor(ProcessingObject<T> successor){
        this.successor = successor;
    }
    
    abstract protected T handleWork(T input);
    
    public T handle(T input){
        T r = handleWork(input);
        if(successor != null){
            return successor.handle(r);
        }
        return r; 
    }
}

//实现阶段，创建继承上面抽象类的类，并实现handleWork方法。这样，在实例化继承类后并通过setSuccessor构成处理链。当第一个实例调用handle就能实现链式处理了。

//运用Lambda方式，构建实现UnaryOperator接口的不同处理对象，然后通过Function的andThen把处理对象连接起来，构成pipeline。
UnaryOperator<String> headerProcessing =
        (String text) -> "From Raoul, Mario and Alan: " + text;

UnaryOperator<String> spellCheckerProcessing =
        (String text) -> text.replaceAll("labda", "lambda");
    
Function<String, String> pipeline =
    headerProcessing.andThen(spellCheckerProcessing);

//直接调用pipeline
String result = pipeline.apply("Aren't labdas really sexy?!!")
```
5.工厂模式(不适合Lambda)
无需向客户暴露实例化的逻辑就能完成对象的创建
```java
public class ProductFactory {
    public static Product createProduct(String name){
        switch(name){
            case "loan": return new Loan();
            case "stock": return new Stock();
            case "bond": return new Bond();
            default: throw new RuntimeException("No such product " + name);
        }
    }
}
```
#### 8.3 测试Lambda表达式
一般的测试例子
```java
@Test
public void testMoveRightBy() throws Exception {
    Point p1 = new Point(5, 5);
    Point p2 = p1.moveRightBy(10);
    assertEquals(15, p2.getX());
    assertEquals(5, p2.getY());
}
```
1.对于Lambda，由于没有名字，而需要借用某个字段访问Lambda。如point类中增加了如下字段
```java
public final static Comparator<Point> compareByXAndThenY =
    comparing(Point::getX).thenComparing(Point::getY);

//测试时
@Test
public void testComparingTwoPoints() throws Exception {

    Point p1 = new Point(10, 15);
    Point p2 = new Point(10, 20);
    int result = Point.compareByXAndThenY.compare(p1 , p2);
    assertEquals(-1, result);
}
```
2.如果Lambda是包含在一个方法里面，就直接测试该方法的最终结果即可。

3.对于复杂的Lambda，将其分到不同的方法引用(这时你往往需要声明一个新的常规方法)。之后，你可以用常规的方式对新的方法进行测试。
可参照笔记的8.1.2或书的8.1.3例子

4.高阶函数测试
直接根据接口签名写不同的Lambda测试

#### 8.4 调试
peek对stream调试

### Chapter 9. Default methods
辅助类的意义已经不大？
兼容性：二进制、源代码和函数行为
```java
public interface Sized {
    int size();
    default boolean isEmpty() {
        return size() == 0;
    }
}
```
1.设计接口时，保持接口minimal and orthogonal

2.函数签名冲突：类方法优先，底层接口优先，显式覆盖

- 如果父类的方法是“继承”“默认”的（非重写），则不算
- 需要显式覆盖的情况：`B.super.hello();`B为接口名，hello为重名方法
- 菱形问题中，A有默认方法，B，C接口继承A（没重写），D实现B，C，此时D回调用A的方法。如果B，C其中一个

### Chapter 10. Using Optional as a better alternative to null
#### 10.1 null与Optional入门
1.null带来的问题
NullPointerException
代码膨胀（null检查）
在Java类型系统的漏洞（null不属于任何类型）
2.Optional类
设置为`Optional<Object>`的变量，表面它的null值在实际业务中是可能的。而非Optional类的null则在现实中是不正常的。
下面的例子中，人可能没车，车也可能没有保险，但是没有公司的保险是不可能的。
```java
public class Person {
    private Optional<Car> car;
    public Optional<Car> getCar() { return car; }
}

public class Car {
    private Optional<Insurance> insurance;
    public Optional<Insurance> getInsurance() { return insurance; }
}

public class Insurance {
    private String name;
    public String getName() { return name; }
}
```
上面Optional类的存在让我们不需要在遇到NullPointerException时（来自Insurance的name缺失）单纯地添加null检查。因为这个异常的出现代表数据出了问题（保险不可能没有相应的公司），需要检查数据。
所以，一直正确使用Optional能够让我们在遇到异常时，知道问题是语法上还是数据上。

#### 10.2 应用Optional
1.创建

- 声明空的：`Optional<Car> optCar = Optional.empty();`
- 从现有的构建：`Optional.of(car)`如果car为null会直接抛异常，而非等到访问car时才说。
- 接受null的Optional`Optional.ofNullable(car)`

2.使用map从Optional对象中提取和转换值
Optional对象中的map是只针对一个对象的（与Stream对比）
map操作保持Optional的封装，所以，如果某方法的返回值是`Optional<Object>`，则一般会用下面的flatMap

3.使用flatMap来链接Optional
```java
//下面代码，第一个map返回的是Optional<Optional<Car>>，这样第二个map中的变量就是Optional<Car>而非Car，故不能调用getCar
optPerson.map(Person::getCar)
         .map(Car::getInsurance)
```
```java
public String getCarInsuranceName(Optional<Person> person) { 
    return person.filter(p -> p.getAge() >= minAge)//后面API处介绍
                 .flatMap(Person::getCar)
                 .flatMap(Car::getInsurance)
                 .map(Insurance::getName)
                 .orElse("Unknown");
}
```
Optional的序列化，通过方法返回Optional变量
```java
public class Person {
         private Car car;
         public Optional<Car> getCarAsOptional() {
             return Optional.ofNullable(car);
    } 
}
```
4.多个Optional的组合
下面函数是一个nullSafe版的findCheapestInsurance，它接受`Optional<Person>`和`Optional<Car>`并返回一个合适的`Optional<Insurance>`
```java
public Optional<Insurance> nullSafeFindCheapestInsurance(
                            Optional<Person> person, Optional<Car> car) {
    return person.flatMap(p -> car.map(c -> findCheapestInsurance(p, c))); //很好地处理各种null的情况
}
```
5.API
`.get`只有确保有值采用
`.orElse(T other)`
`orElseGet(Supplier<? extends T> other)`如果创建默认值consuming时用
`orElseThrow(Supplier<? extends X> exceptionSupplier)`
`ifPresent(Consumer<? super T>)`
`isPresent`
`filter`符合条件pass，否则返回空Optional
#### 10.3 Optional的实战示例
```java
//希望得到一个Optional封装的值
Optional<Object> value = Optional.ofNullable(map.get("key"));//即使可能为null也要取得的值

//用Optional.empty()代替异常。建议将多个类似下面的代码封装到一个工具类中
public static Optional<Integer> s2i(String s) {
    try {
        return of(Integer.parseInt(s));
    } catch (NumberFormatException e) {
        return empty();
    }
}

//暂时避开基本类Optional，因为他们没有map、filter方法。

//下面针对Properties进行转换，如果K（name）对应的V是正整数，则返回该V的int，其他情况返回0
public int readDuration(Properties props, String name) {
    return Optional.ofNullable(props.getProperty(name))//提取V，允许null。如果null，则只有orElse需要执行
                   .flatMap(OptionalUtility::stringToInt)//上例中提到的方法
                   .filter(i -> i > 0)//是否为正数
                   .orElse(0);
}
```
### Chapter 11. CompletableFuture: composable asynchronous programming
#### 11.1 Future接口
Future接口提供了方法来检测异步计算是否已经结束(使用isDone方法)，等待异步操作结束，以及获取计算的结果。CompletableFuture在此基础上增加了不同功能。
同步API与异步API：异步是需要新开线程的
#### 11.2 实现异步API
1.getPrice
下面代码是异步获取价格的方法。首先新建一个CompletableFuture，然后是一个新线程，这个线程的任务是calculatePrice（该方法添加1秒延迟来模拟网络延迟）。这个方法的返回变量futurePrice会马上得出，但是里面的结果要等到另外一个线程计算后才能取得，即完成.complete。
```java
public Future<Double> getPriceAsync(String product) {
    CompletableFuture<Double> futurePrice = new CompletableFuture<>();
    new Thread( () -> {
                try {
                    double price = calculatePrice(product);
                    futurePrice.complete(price);//计算正常的话设置结果，此时原线程的futurePrice就可以get到结果了。但一般不用普通get方法，重制get能设置等待时间
                } catch (Exception ex) {
                    futurePrice.completeExceptionally(ex);//将异常返回给原线程
                }
    }).start();
    return futurePrice;
}
```
`return CompletableFuture.supplyAsync(() -> calculatePrice(product));
}`
supplyAsync的函数描述符`() -> CompletableFuture<T>`

2.findPrices（查询某product在一列shops的价格）
这里的计算是一条线的，collect的执行需要所有getPrice执行完才可以执行，所以没有必要开异步。如果collect在getPrice执行完之前还有其他事情可以做，此时才用异步
```java
//shops是List<Shop>
//通过并行实现
public List<String> findPricesParallel(String product) {
    return shops.parallelStream()
                .map(shop -> shop.getName() + " price is " + shop.getPrice(product))
                .collect(Collectors.toList());
}

//异步实现
//一个stream只能同步顺序执行，但取值不需要等所有值都得出才取，所以join分在另一个stream里
public List<String> findPrices(String product) {
    List<CompletableFuture<String>> priceFutures =
       shops.stream()
            .map(shop -> CompletableFuture.supplyAsync(() -> shop.getName() + " price is "
                    + shop.getPrice(product), executor))//返回CompletableFuture<String>。这里使用了异步，可提供自定义executor
            .collect(Collectors.toList());

    List<String> prices = priceFutures.stream()
        .map(CompletableFuture::join)//join相当于get，但不会抛出检测到的异常，不需要try/catch
        .collect(Collectors.toList());
    return prices;
}
```
假设默认有4个线程（`Runtime. getRuntime().availableProcessors()`可查看），那么在4个shops的情况下，并行需要1s多点的时间（getPrice设置了1s的延迟），异步需要2s多点。如果5个shops，并行还是要2s。其实可以大致理解为异步有一个主线程，三个支线程。
然而异步的优势在于可配置Executor

**定制执行器的建议**
Nthreads = NCPU * UCPU * (1 + W/C)
N为数量，U为使用率，W/C为等待时间和计算时间比例
上面例子在4核，CPU100%使用率，每次等待时间1s占据绝大部分运行时间的情况下，建议设置线程池容量为400。当然，线程不应该多于shops，而且要考虑机器的负荷来调整线程数。下面是设置执行器的代码。设置好后，只要shop数没超过阈值，程序都能1s内完成。
```java
private final Executor executor = Executors.newFixedThreadPool(Math.min(shops.size(), 100), new ThreadFactory() {
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);//设置为保护线程，程序退出时线程会被回收
        return t;
    }
});
```
**并行与异步的选择**
并行：计算密集型的操作，并且没有I/O（就没有必要创建比处理器核数更多的线程）
异步：涉及等待I/O的操作(包括网络连接等待)

#### 11.3 对多个异步任务进行流水线操作
1.连续异步（第一个CompletableFuture需要第二个CompletableFuture的结果）
此处getPrice的返回格式为`Name:price:DiscountCode`
`Quote::parse`对接受的String进行split，并返回一个`new Quote(shopName, price, discountCode)`
第二个map没有涉及I/O和远程服务等，不会有太多延迟，所以可以采用同步。
第三个map涉及异步，因为计算Discount需要时间（设置的1s）。此时可以用thenCompose方法，它允许你对两个异步操作进行流水线，第一个操作完成时，将其结果作为参数传递给第二个操作
```java
public List<String> findPrices(String product) {
    List<CompletableFuture<String>> priceFutures =
        shops.stream()
             .map(shop -> CompletableFuture.supplyAsync(
                                () -> shop.getPrice(product), executor))
             .map(future -> future.thenApply(Quote::parse))
             .map(future -> future.thenCompose(quote ->
                            CompletableFuture.supplyAsync(
                             () -> Discount.applyDiscount(quote), executor)))
             .collect(toList());
             
    return priceFutures.stream()
                       .map(CompletableFuture::join)
                       .collect(toList());
}
```
由于Discount.applyDiscount消耗1s时间，所以总时间比之前多了1s
2.整合异步（两个不相关的CompletableFuture整合起来）
下面代码的combine操作只是相乘，不会耗费太多时间，所以不需要调用thenCombineAsync进行进一步的异步
```java
Future<Double> futurePriceInUSD =
    CompletableFuture.supplyAsync(() -> shop.getPrice(product)) 
    .thenCombine(//这里和上一个同样是在新1线程
               CompletableFuture.supplyAsync(//这个是新2线程
                   () ->  exchangeService.getRate(Money.EUR, Money.USD)),
               (price, rate) -> price * rate
           );
```
#### 11.4 响应CompletableFuture的completion事件
.thenAccept接收`CompletableFuture<T>`，返回`CompletableFuture<Void>`
下面的findPricesStream是连续异步中去掉的三个map外的代码
```java
CompletableFuture[] futures = findPricesStream("myPhone")
        .map(f -> f.thenAccept(System.out::println))
        .toArray(size -> new CompletableFuture[size]);
    CompletableFuture.allOf(futures).join();//allOf接收CompletableFuture数组并返回所有CompletableFuture。也有anyOf
```
### Chapter 12. New Date and Time API
#### 12.1 LocalDate、LocalTime、Instant、Duration以及Period
下面都没有时区之分，不能修改
时点
```java
//LocalDate
LocalDate date = LocalDate.of(2014, 3, 18);
int year = date.getYear();
Month month = date.getMonth();
int day = date.getDayOfMonth();
DayOfWeek dow = date.getDayOfWeek();
int len = date.lengthOfMonth();
boolean leap = date.isLeapYear();
LocalDate today = LocalDate.now();
int year = date.get(ChronoField.YEAR);//get接收一个ChronoField枚举，也是获得当前时间
date.atTime(time);
date.atTime(13, 45, 20);

//LocalTime
LocalTime time = LocalTime.of(13, 45, 20);//可以只设置时分
//同样是getXXX
time.atDate(date);

//通用方法
.parse()

//LocalDateTime
LocalDateTime dt1 = LocalDateTime.of(2014, Month.MARCH, 18, 13, 45, 20); LocalDateTime.of(date, time);
toLocalDate();
toLocalTime();

//机器时间
Instant.ofEpochSecond(3);
Instant.ofEpochSecond(4, -1_000_000_000);//4秒之前的100万纳秒(1秒)
Instant.now()
//Instant的设计初衷是为了便于机器使用。它包含的是由秒及纳秒所构成的数字。所以，它 无法处理那些我们非常容易理解的时间单位，不要与上面的方法混用。
```
时段
```java
//Duration
Duration d1 = Duration.between(time1, time2);//也可以是Instant，LocalDateTimes，但LocalDate不行
//Period
Period tenDays = Period.between(LocalDate1, LocalDate2)

//通用方法
Duration.ofMinutes(3);
Duration.of(3, ChronoUnit.MINUTES);

Period.ofDays(10);
twoYearsSixMonthsOneDay = Period.of(2, 6, 1);
//还有很多方法
```

## Part 4. Beyond Java 8
### Chapter 13. Thinking functionally
#### 13.1 实现和维护系统
有synchronized关键字的不要维护
**容易使用的程序**

- Stream的无状态的行为(函数不会由于需要等待从另一个方法中读取变量，或者由于需要写入的变量同时有另一个方法正在写入，而发生中断)让我们
- 最好类的 结构应该反映出系统的结构
- 提供指标对结构的合理性进行评估，比如耦合性(软件系统中各组件之间是否相互独立)以及内聚性(系统的各相关部分之间如何协作)

不过对于日常事务，最关心的是代码维护时的调试：代码遭遇一些无法预期的值就有可能发生崩溃。这些无法预知的变量都源于共享的数据结构被你所维护的代码中的多个方法读取和更新。

对此，函数式编程提出的“无副作用”以及“不变性”

**无副作用**：
函数：如果一个方法既不修改它内嵌类的状态，也不修改其他对象的状态，使用return返回所有的计算结果，那么我们称其为无副作用的。函数如果抛出异常，I/O和对类中的数据进行任何修改（除构造器内的初始化）都是有副作用的。
- 变量：final型

**声明式编程**
一般通过编程实现一个系统，有两种思考方式。一种专注于如何实现，另一种方式则更加关注要做什么。
前一种为经典的面向对象编程，命令式；后一种为内部迭代，声明式。
第二种方式编写的代码更加接近问题陈述。

函数式编程实现了上述的两种思想：使用不相互影响的表达式，描述想要做什么（由系统来选择如何实现）。

#### 13.2 函数式编程
1.函数式Java编程
Java语言无法实现纯粹函数式（完全无副作用）的程序，只能接近（副作用不会被察觉）。
这种函数只能修改局部变量，它的引用对象（参数及其他外部引用）都是不可修改对象（复制后再使用非函数式行为，如add）。除此之外，不抛异常（用Optional，或者局部抛异常），不进行I/O。

2.引用透明性（上面规定的隐含）
一个函数只要传递同样的参数值，它总是返回同样（==）的结果。

3.例子
```java
//给定一个List<value>，返回其子集，类型为List<List<Integer>>，下面是整体算法，下下面是函数式的实践
static List<List<Integer>> subsets(List<Integer> list)
    if (list.isEmpty()) {
        List<List<Integer>> ans = new ArrayList<>();
        ans.add(Collections.emptyList());
        return ans;
    }
    Integer first = list.get(0);
    List<Integer> rest = list.subList(1,list.size());
    List<List<Integer>> subans = subsets(rest);
    List<List<Integer>> subans2 = insertAll(first, subans);
    return concat(subans, subans2);

//    
static List<List<Integer>> insertAll(Integer first,
       List<List<Integer>> lists) {
    List<List<Integer>> result = new ArrayList<>();
    for (List<Integer> list : lists) {
        List<Integer> copyList = new ArrayList<>();//复制新list，而不是直接用参数调用.add
        copyList.add(first);
        copyList.addAll(list);
        result.add(copyList);
    }
    return result;
}
//下面方式相同
static List<List<Integer>> concat(List<List<Integer>> a,
    List<List<Integer>> b) {
        List<List<Integer>> r = new ArrayList<>(a);
        r.addAll(b);
        return r;
}
```

#### 13.3 递归和迭代
将增强for改为迭代器方式没有副作用？
```java
Iterator<Apple> it = apples.iterator();
    while (it.hasNext()) {
       Apple apple = it.next();
        // ... 
}
```
利用递归而非迭代来消除没步都需更新迭代变量（但是使用迭代在Java效率通常更差），如阶乘：
```java
//下面代码除效率问题，还有StackOverflowError风险
static long factorialRecursive(long n) {
        return n == 1 ? 1 : n * factorialRecursive(n-1);
}

//尾迭代能解决StackOverflowError问题。每次调用函数时，把新的结果传入函数。遗憾Java目前还不支持这种优化，Scala可以。
static long factorialTailRecursive(long n) {
        return factorialHelper(1, n);
}

static long factorialHelper(long acc, long n) {
    return n == 1 ? acc : factorialHelper(acc * n, n-1);
}

//Stream更简单
static long factorialStreams(long n){
        return LongStream.rangeClosed(1, n)
                         .reduce(1, (long a, long b) -> a * b);
}

```
总结：尽量使用Stream取代迭代操作，从而避免变化带来的影响。此外，如果递归能不带任何副作用地让你以更精炼的方式实现算法，你就应该用递归替换迭代，因为它更加易于阅读、实现和理解。大多数时候编程的效率要比细微的执行效率差异重要得多。

### Chapter 14. Functional programming techniques
函数式语言更广泛的含义是：函数可以作参数、返回值，还能存储。
1.高阶函数
接受至少一个函数做参数，返回结果是一个函数

接收的作为参数的函数可能带来的副作用以文档的方式记录下来，最理想的情况下，接收的函数参数应该没有任何副作用。
2.科里化
一种将具备n个参数(比如，x和y)的函数f转化为使用m（m < n）个参数的函数g，并且这个函数的返回值也是一个函数，它会作为新函数的一个参数。后者的返回值和初始函数的 返回值相同。
```java
//将下面函数科里化，即预设各种f和b的组合，需要使用时只需调用相应的函数加上确实的x。
static double converter(double x, double f, double b) {
    return x * f + b;
}

//创建高阶函数
static DoubleUnaryOperator curriedConverter(double f, double b){ 
    return (double x) -> x * f + b;
}
//其中一种组合
DoubleUnaryOperator convertUSDtoGBP = curriedConverter(0.6, 0);
//使用
double gbp = convertUSDtoGBP.applyAsDouble(1000);
```

### 14.2 持久化数据结构
这里指的不是数据库中的持久化
链表例子（火车旅行）
```java
//TrainJourney类（火车站）有两个公有变量，price和onward（下一站），构造函数如下
public TrainJourney(int p, TrainJourney t) {
    price = p;
    onward = t; 
}

//link方法把两个单向链表（一列火车站）连成一体。下面代码在a的基础上连接，这样会破坏原来a的结构。如果a原本在其他地方有应用，那么那些地方也会受到影响。
static TrainJourney link(TrainJourney a, TrainJourney b){
    if (a==null) return b;
    TrainJourney t = a;
    while(t.onward != null){
        t = t.onward;
    }
    t.onward = b;
    return a; 
}

//函数式实现。下面的实现的结果是a的副本，后面接上b。所以要确保结果不被修改，否则b也会没修改。这也包括下面的tree例子
static TrainJourney append(TrainJourney a, TrainJourney b){
    return a==null ? b : new TrainJourney(a.price, append(a.onward, b));
}

//函数式难免会有一定程度的复制，上面例子至少只复制了a，而不是在一个全新的list上连接a和b
```
树例子（个人信息）
```java
//节点信息，如果强制遵守函数式编程，可以将下面变量声明为final
class Tree { 
    private String key;
    private int val;
    private Tree left, right;
    public Tree(String k, int v, Tree l, Tree r) {
        key = k; val = v; left = l; right = r;
  } 
} 

//函数式的节点更新，每次更新都会创建一个新tree，通常而言，如果树的深度为d，并且保持一定的平衡性，那么这棵树的节点总数是2^d
public static Tree fupdate(String k, int newval, Tree t) {
    return (t == null) ?
        new Tree(k, newval, null, null) :
            k.equals(t.key) ?
                new Tree(k, newval, t.left, t.right) :
            k.compareTo(t.key) < 0 ?
                new Tree(t.key, t.val, fupdate(k,newval, t.left), t.right) :
                new Tree(t.key, t.val, t.left, fupdate(k,newval, t.right));
}
```
？实现部分函数式（某些数据更新对某些用户可见）：
- 典型方式：只要你使用非函数式代码向树中添加某种形式的数据结构，请立刻创建它的一份副本
- 函数式：改动前，复制修改处之前的部分，然后接上剩余部分

### 14.3 Stream 的延迟计算
有一个延迟列表的实现例子
如果延迟数据结构能让程序设计更简单，就尽量使用它们。如果它们会带来无法接受的性能损失，就尝试以更加传统的方式重新实现它们。

### 14.4 模式匹配（Java暂未提供）
1.访问者设计模式
一个式子简化的代码，如5+0变为5，使用Expr.simplify。但一开始要对expr进行各种检查，如expr的类型，不同类型有不同变量，当符合条件才返回结果。这个过程涉及instanceof和cast等操作，比较麻烦。
而访问者设计模式能得到一定的简化，它需要创建一个单独的类（SimplifyExprVisitor），这个类封装了一个算法（下面的visit），可以“访问”某种数据 类型。
```scala
class BinOp extends Expr{
    String opname; 
    Expr left, right;
    
    public Expr accept(SimplifyExprVisitor v){
            return v.visit(this);
    } 
}

public class SimplifyExprVisitor {
    ...
    public Expr visit(BinOp e){
        if("+".equals(e.opname) && e.right instanceof Number && ...){
            return e.left;
        }
        return e;
    }
}

//Java 中模式的判断标签被限制在了某些基础类型、枚举类型、封装基础类型的类以及String类型。
//Scala的简单实现
def simplifyExpression(expr: Expr): Expr = expr match {
    case BinOp("+", e, Number(0)) => e
    case BinOp("*", e, Number(1)) => e
    case BinOp("/", e, Number(1)) => e
    case _ => expr
}
```
### 14.5 杂项
1.缓存或记忆表（并非函数式方案）
```java
final Map<Range,Integer> numberOfNodes = new HashMap<>();
Integer computeNumberOfNodesUsingCache(Range range) {
    Integer result = numberOfNodes.get(range);
    if (result != null){
        return result;
    }
    result = computeNumberOfNodes(range);
    numberOfNodes.put(range, result);
    return result;
}
```
这段代码虽然是透明的，但并不是线程安全的(numberOfNodes可变)
2.结合器

### Chapter 15. comparing Java 8 and Scala
下面默认先写Scala，或只写Scala
#### 15.1 Scala 简介
1.你好
```scala
//命令式
object Beer {//单例对象
  def main(args: Array[String]/*Java先类型后变量*/){//不需要void，通常非递归方法不需要写返回类型；对象声明中的方法是静态的
    var n : Int = 2
    while( n <= 6 ){
      println(s"Hello ${n} bottles of beer")
      n += 1 
    }
  } 
}

//函数式
2 to 6 /*Int的to方法，接受Int，返回区间，即也可以用2.to(6)。后面foreach理解相同*/foreach { n => println(s"Hello ${n} bottles of beer") }
```
同样一切为对象，但没有基本类型之分
Scala中用匿名函数或闭包指代lambda

2.数据结构
Map：
`val authorsToAge = Map("Raoul" -> 23)`Java需要创建后put
`val authors = List("Raoul", "Mario")`

Scala中的集合默认都是持久化的:更新一个Scala集合会生成一个新的集合，这个新的集合和之前版本的集合共享大部分的内容，最终的结果是数据尽可能地实现了持久化。由于这一属性，代码的隐式数据依赖更少:人们对代码中集合变更的困惑(比如在何处更新了集合，什么时候做的更新)也会更少。
`val newNumbers = numbers + 8`numbers为Set，添加元素是创建一个新Set对象

Java的不可变（immutable）比不可修改(unmodifiable)更彻底

```scala
val fileLines = Source.fromFile("data.txt").getLines.toList() 
val linesLongUpper
      = fileLines.filter(l => l.length() > 10)
                 .map(l => l.toUpperCase())
//另一种表达，多加.par表示并行
fileLines.par filter (_.length() > 10) map(_.toUpperCase())
```

元组
`val book = (2014, "Java 8 in Action", "Manning")`可不同类型，任意长度（上限23）
Java需要自己建pair类，�且3个以上元素的pair比较麻烦

Stream
Scala中可以访问之前计算的值，可以通过索引访问，同时内存效率会变低。

Option
和Java很像
```scala
def getCarInsuranceName(person: Option[Person], minAge: Int) = 
    person.filter(_.getAge() >= minAge)
          .flatMap(_.getCar)
          .flatMap(_.getInsurance)
          .map(_.getName)
          .getOrElse("Unknown")
```
#### 15.2 函数
Scala多了“能够读写非本地变量”和对科里化的支持
1.一等函数
```scala
//filter的函数签名
def filter[T](p: (T) => Boolean/*Java用函数式接口Predicate<T>或 者Function<T, Boolean>，Scala直接用函数描述符或名为函数类型*/): List[T]//参数类型

//定义函数
def isShortTweet(tweet: String) : Boolean = tweet.length() < 20
//使用函数，tweets是List[String]
tweets.filter(isShortTweet).foreach(println)
```
2.匿名函数和闭包
```scala
//上面代码的匿名方式如下（都是语法糖）
val isLongTweet : String => Boolean
    = (tweet : String) => tweet.length() > 60

isLongTweet("A very short tweet")

//Java的匿名方式
Function<String, Boolean> isLongTweet = (String s) -> s.length() > 60;

boolean long = isLongTweet.apply("A very short tweet");

//闭包
var count = 0
val inc = () => count+=1
inc()
println(count)
//Java
int count = 0;
Runnable inc = () -> count+=1;//会出错，count必须为final或效果为final
inc.run();
```
3.科里化
Java需要手工地切分函数，麻烦在于多参数情况
```scala
//Java
static Function<Integer, Integer> multiplyCurry(int x) {
    return (Integer y) -> x * y;
}

Stream.of(1, 3, 5, 7)
      .map(multiplyCurry(2))
      .forEach(System.out::println);

//Scala
def multiplyCurry(x :Int)(y : Int) = x * y

val multiplyByTwo : Int => Int = multiplyCurry(2)
val r = multiplyByTwo(10)
```

#### 15.3 类和trait
1.类
Scala中的getter和setter都是隐式实现的
```scala
class Student(var name: String, var id: Int)

val s = new Student("Raoul", 1)
println(s.name)//getter
s.id = 1337//setter
```
2.trait
与interface类似，有抽象方法、默认方法、接口多继承。但trait还有抽象类的字段。Java支持行为的多继承，但还不支持对状态的多继承。
Scala可以在类实例化时才决定trait
```scala
val b1 = new Box() with Sized
```
### Chapter 16. Conclusions
Java8的发展体现了两种趋势：多核处理的需求（独立CPU速度瓶颈）->并行计算；更简洁地对抽象数据进行操作。
1.行为参数化：Lambda和方法引用
2.对大量数据的处理Stream：能在一次遍历中完成多种操作，而且按需计算。
并行处理中的重点：无副作用、Lambda、方法引用、内部迭代
3.CompletableFuture提供了像thenCompose、thenCombine、allOf这样的操作，避免Future中的命令式编程
4.Optional：能显式表示缺失值。正确使用能够发现数据缺失的原因。还有一些与Stream类似的方法。
5.默认方法


一些习惯？
1.生产`List<Object>`
`List<Point> points = Arrays.asList(new Point(5, 5)...);`