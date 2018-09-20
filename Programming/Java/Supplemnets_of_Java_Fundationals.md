# Supplemnets of Java Fundationals

[TOC]



## 数据类型

### 基本数据类型（4整2浮1字1布）

**整型**：

byte：8位，-128到128空间只有int类型的四分之一
short：16位，-32768到32768，int二分之一
int: 32位；long：64位，默认0L
值通过包装类.MAX_VALUE查
十六进制用0x前缀，二进制0b。八进制有个前缀0，最好远离八进制和前缀为0的数
数字可加`_`，Java编译会删除
如果所处理的值不可能为负，且需要额外一bit，用`Byte.toUnsignedInt(b)`

**浮点**：float：32，0.0f；double：64，0.0d
0.0/0.0或负数平方根会产生NaN所有NaN都是不可比的，只能用isNaN(x)判断，其他还有`Double.isInfinite`和`Double.isFinite`（既不是无穷也不是NAN）
浮点数不适合金融计算，用BigDecimal

**char**：16位，UTF-16字符编码中的“编码单元”, 默认/最小'\u0000'即0，最大'\uffff'（即为65,535）

**boolean**：默认false

转换：

 - byte、short与char进行数学运算，会自动转换为int
 - 一个浮点数字面量默认是double类型，如果要定义float类型则要在字面量最后添加f
 - 占用内存空间小的类型可以自动转换为占用空间大的类型，反之则不成立。除非强制转换a = (int)b，但会有数据切割。int转到float和long转到float或double是例外`float f = 123456789`的结果为1.23456792E8，超出float的7位数字
 - 'J'+1中，char会转换为int

8种基本类型都不是类（所以无法直接调用方法），Java提供了对应的类，称之为包装类。包装类可以认为是将基本类型转换成的一个引用类型。



#### Array数组

创建（默认填充）固定大小，存储空间连续，增删效率低，无法保存映射关系（即不能通过里面的内容来查是否存在，这能通过index），缺乏封装（操作繁琐）。
数字和char为0
Boolean用false
其他object用null(此情况记得填充)，所以注意创建String后记得初始化

```java
int[] primes = {2,3,4,7};
primes = new int[] {...};
int[][] p = new int[4][4];
//换行
int[] temp = p[0];
p[0] = p[1];
p[1] = temp;

//static
Array.copyOf(list, range)
.fill
.sort / parallelSort
println(Arrays.toString(primes))//调试时用,不用遍历。二维用deepToString
```


### 容器
放引用类型，基本类型自动装箱。要加上泛型，否则取出来是Object，且能限制保存的数据类型，更安全。

主要分为Collection（不唯一无序）和Map

Collection：List（ArrayList和LinkedList）和Set（HashSet和TreeSet）

Map：HashMap和TreeMap

上述结构“基本”都有的方法: add, addAll（index可选，容器）, removeAll, retainAll, removeIf, size, isEmpty, contains, containsAll, stream, iterator, spliterator, toArray

Collections工具类的方法：
Collections.disjoint(两个collection是否不含相同元素), copy, fill, frequency, binarySearch, sort, max, min



#### List接口（有序不唯一）
有不少index有关的方法: add(index可选), get, set(index, element), replacell(`UnaryOperator<E>`), remove(index), indexOf, lastIndexOf, listIterator(index可选), sort, subList, swap, rotate, reverse, shuffe, singletoList

**ArrayList数组列表** 

为Array加上可变长的属性以及一些新方法。遍历和随机访问（通过index，底层是把index代入方程计算位置）访问快，但是插入、删除元素较慢（数据搬迁），按照内容访问慢（逐个比较），需要连续存储空间。

初始化（只有替代方式）
`ArrayList<String> copiedFriends = new ArrayList<>(Arrays.asList(" "," "," "))`或者填数字初始化大小

复制方法
names是数组，friends是ArrayList，里面还可以放map.keySet(), map.values()
`ArrayList<String> copiedFriends = new ArrayList<>(friends/Arrays.asList(names));`

数组列表转数组
`String[] names = friends.toArray(new String[0]);`
没有一种简单的方法完成基本类型数组和对应包装类数组列表的转换

方法：add（index可选，value）, remove, get, set(index, value), size,  toString
static: fill, sort, reverse...

**扩容**：底层通过数组elementData实现，一开始不传参，空间为0，add一个elem后变为默认的10，之后扩容一次性为50%，如果不够，就直接扩大为刚好装得下的容量，然后copy整个旧的ArrayList到新的ArrayList中。

> 对应早期的vector，vector线程安全，但效率较低。

**LinkedList链列表**
双向链表，内存空间不再连续，插入、删除和移动元素快，但是中间位置的操作耗时比较长（比ArrayList少实现了RandomAccess），比如访问中间的元素，不能想ArrayList通过方程计算位置，遍历时由于空间不连续，也慢。

增加了一些对首尾操作的方法。



#### Set（无序不重）
> 要实现“不重”，存入的elem要重写equals()
>
> 没有index相关方法，不能用普通for遍历。

**HashSet**
查询，增删都快。如果hash函数好的话，这个方法高效，如String，Path

放入的elem要重写hashCode()

**LinkedHashSet**

保留添加顺序

放入的elem要重写hashCode()

**TreeSet**
二叉、二次排序、平衡树（红黑树），有序，按内容查询比List快，但没HashSet快。存入时比较并调整根的位置，从而保证平衡。如果不平衡，在极端情况下，会变为linkedlist，查询速度就更慢了。读取的时候，由于存入时已近排好序，且根据结果可知，时间复杂度是logn。

放入的elem必须实现Comparable接口（在elem的类中实现，所以称为内部比较器），或在构造函数中提供Comparator外部比较器，描述符为`(T, T) -> int`，如下(如果多次使用，单独创建一个类继承Comparator更合适)

```java
countries = new TreeSet<>((u, v) ->
    u.equals(v) ? 0
        : u.equals(“AAA”) ? -1
            : v.equals(“AAA”) ? 1
                : u.compareTo(v));
// AAA always comes first
```
实现了SortedSet和NavigableSet接口

> SortedSet：first, last, headSet, subSet, tailSet
> NavigableSet: higher,ceiling, floor, lower, pollFirst, headSet, subSet, tailSet(可选是否包含)
> 寻找元素邻居

**补充：**

hashtable散列表，HashMap的早期对应类。HashTable线程安全，效率较低，且不允许null key

最简单的方式是顺序表+链表

存储步骤：hashCode()，利用hashCode计算存储位置，存入表。读取顺序一样。一个计算位置的简单公式：`x % SeqList.size`

hash函数（hashCode()使用）的三个特征：
A hash function will always produce a hash code of a fixed size
A hash function must always produce the same hash code for two equal objects, and
A hash function may produce the same hash code for two unequal objects.

减少冲突：

（1）load factor（要记录的数据量/主表长度）等于0.5时最优，当等于0.75时考虑扩大主表。

（2）哈希函数和地址计算公式的选择

（3）冲突的处理方法。JDK8的方法是，当某链表size >= 8，就把该链表改为红黑树



### Map

**方法**：

put (K key, V value),putAll,putIfAbsent（线程安全）
getOrDefault比get(key)好，get会出现null，remove（key）
获取所有键、值或键值对的集合（keyset, values, entrySet）
merge
compute(key, BiFunction)作用于key和value，结果为null时移除key,返回get(key),computeIfPresent/Absent(值非空/空)
remove, replace（两个都可以只输入key或输入KV对）
size, isEmpty, clear, forEach, replaceAll, containsKey/Value

```java
Map<String, Integer> counts = new HashMap<>();
counts.put(“Alice”, 1); 
counts.put(“Alice”, 2); //put重复就更新

counts.merge(word, 1, Integer::sum);//更新counts，如果word存在，就加1，否则put（word，1）

//遍历，数据量少用lambda，中等用for，大量用parallel
for (Map.Entry<String, Integer> entry : x.entrySet()) {
    String k = entry.getKey();
    Integer v = entry.getValue();
}
//或者
x.forEach((k,v) -> {});
```

**HashMap**
**LinkedHashMap**记录添加顺序的
**TreeMap**
**ConcurrentHashMap**不允许null的K or V

> 有些map会删除null
>
> HashMap源码：threshold扩容的阈值，capacity * load_factor

### 迭代器
`Collection implements Iterable<T>`
有remove方法，删除.next的对象，而非指向的对象。但对象直接用remove或removeIf更简单
```java
//ListIterator与LinkedList一起用
List<String> friends = new LinkedList<>();
ListIterator<String> iter = friends.listIterator();
iter.add(“Fred”); // Fred |
iter.add(“Wilma”); // Fred Wilma |
iter.previous(); // Fred | Wilma
iter.set(“Barney”); // Fred | Barney
//如果对一个数据结构使用多个迭代器，其中一个改变了数据结构，那其他会失效ConcurrentModificationException
```
Map

### Stream

特点：

* 编程遵循做什么而不是怎么做，stream会自动安排线程和执行顺序。
* 不存储元素，按需产生
* 不改变源数据（直接生成一个符合条件的新数据，而非在新数据上修改）
* 延迟执行（如只需前五，则得到第五个就会停止）

#### 创建
1.已有资源创建
Collection: .stream, par

String, Object[], 多个参数: Stream.of(contents.split()),IntStream.of, Arrays.stream(array, from, to)

基本流:
mapToInt
.boxed()反操作

可直接用sum, average, max
有option基本类

并行流：
Collection: parallelStream
stream: parallel

使用的关键是无状态和顺序相关

小心forEach

默认情况下，有序集合（arrays,list）,range, generate, iterate, .Stream.sorted都是有序的。有序影响并不大，只需按顺序合并即可。

Stream.unordered后，一些操作会更快，如distinct。如果可以接受任意一个唯一元素，所有元素就可以并发了（使用一个共享集合来跟踪重复元素）。其他：limit

用groupingByConcurrent更好（但会无序）

当执行流操作时，不能修改（remove, add等）流底层依赖的集合，即使线性安全


2.从无到有
stream.empty()
Generate: `Stream<String> echos = Stream.generate(() -> "Echo");`
`Stream<Double> randoms = Stream.generate(Math::random);`
Iterate:`Stream<BigInteger> integers = Stream.iterate(BigInteger.ZERO, n -> n.add(BigInteger.ONE));`
range, rangeClosed
CharSequence的codePoints和chars

其他：
```
Stream<String> words = Pattern.compile(“\PL+”).splitAsStream(contents);

try (Stream<String> lines = Files.lines(path)) {
    Process lines
}
```
#### 中间操作
fliter, limit, skip, Stream.concat, distinct
sorted(Comparator.comparing().reversed())
peek(方便调试，每个元素调用consumer)
map：
这个操作会产生一个包含多个流的流`Stream<Stream<String>> result = words.stream().map(w -> letters(w));`用flatMap能把这些流的划分隔去掉

#### 终止操作
count, max, min, findFirst/findAngy(配合filter)
anyMatch, nonMatch

**1.Option类**
当不存在时的默认值
```
//返回某值
String result = optionalString.orElse("");

//返回计算值
String result = optionalString.orElseGet(() -> System.getProperty("user.dir"));

//返回异常
String result = optionalString.orElseThrow(IllegalStateException::new);
```
当存在时的值
```
//不会返回任何值
optionalValue.ifPresent(v -> Process v);
optionalValue.ifPresent(results::add);
//如果想对函数结果进行处理，用map
Optional<Boolean> added = optionalValue.map(results::add);
```

创建
Optional.empty, Optional.of
```
public static Optional<Double> inverse(Double x) {
    return x == 0 ? Optional.empty() : Optional.of(1 / x);
}
```
`Optional.ofNullable(obj)` 根据obj是否为null直接返回 `Optional.of(obj)` 或`Optional.empty()` 

配合flatMap
下面s.f()返回`Optional<T>`,而g只接受T，此时就要用flatMap了
`Optional<U> result = s.f().flatMap(T::g);`
例如，结合上面inverse方法
`Optional<Double> result = inverse(x).flatMap(MyMath::squareRoot);`

**2.收集**
**类型**
viod：forEach

数组：
toArray得Object[],想获得相应的数组，用`String[] result = stream.toArray(String[]::new);`

Set和List：
collect放到另一个目标容器`List<String> result = stream.collect(Collectors.toList());`也有toSet
控制具体类型`TreeSet<String> result = stream.collect(Collectors.toCollection(TreeSet::new))`

String：map可选，避免流有其他类型
`String result = stream.map(Object::toString).collect(Collectors.joining(","))`

引入`java.util.stream.Collectors.*`可省去Collector

Map:
下面collector操作最好只用分组往后的，其他用map, reduce, count, max等简单方式

对各个KV对操作
toConcurrentMap
```
Map<Integer, String> idToName = people.collect(
    Collectors.toMap(Person::getId, Person::getName));
//如果V为对象本身，用Function.identity()
//如果K有相同，要第三个函数调和
Locale::getDisplayLanguage,
Locale::getDisplayLanguage,
(existingValue, newValue) -> existingValue));//第三个参数，保持之前的值
//第四个参数可传构造函数
TreeMap::new
```
分组操作groupingBy和partitioningBy
```
Map<String, List<Locale>> countryToLocales = locales.collect(
    Collectors.groupingBy(Locale::getCountry));

Map<Boolean, List<Locale>> englishAndOtherLocales = locales.collect(
    Collectors.partitioningBy(l -> l.getLanguage().equals("en")));

//downstream对分组后的值进行操作
//如对上面第一个代码添加toSet()
Map<String, Set<Locale>> ... groupingBy(Locale::getCountry, toSet())
//其他选择：
counting()
summingInt(City::getPopulation)
maxBy(Comparator.comparing(City::getPopulation))
mapping(City::getName,maxBy(Comparator.comparing(String::length))//对downstram的结果进行操作
summarizingInt(City::getPopulation)
```


**统计结果**
```
IntSummaryStatistics summary = stream.collect(
    Collectors.summarizingInt(String::length));
double averageWordLength = summary.getAverage();//可选总和，极值
```
**归约**
乱序操作适合和，乘，concatenation, max, min, union, intersection
```
List<Integer> values = ...;
Integer sum = values.stream().reduce(0, Integer::sum)
```
不通用地方：如收集到BitSet中时用了并行，这时用collect更好`BitSet result = stream.collect(BitSet::new, BitSet::set, BitSet::or);`

### 其他常用数据类型 
#### String(CharSequence父类)

字符串可包含Unicode，如`Java\u2122`
`int + string`变为字符串，有时会有意外结果，用括号把int类括起来

null引用方法会出错，所以一般用`" ".equals(location)`即使location为null，仍能正常运行

连接：
`.join(", ", "apple", "boy")`
```
//量大时用下面方法
StringBuilder builder = new StringBuilder();

while(){
    builder.append();
}

String result = builder.toString();
```
拆开
`String ... .substring(index, index)`结果被保存到另一个string

`a.compareTo(b)`比较字典（Unicode）顺序，a在b前返回负数（不一定-1），反之。可读性排序用Collator

转换
`Integer.toString(n,base)`base是可选进制。不推荐“”+int转换
`Integer.parseInt(str,进制)`
Double也有相应的方法

其他常用API:
```
startsWith
endsWith
contains

//都可以fromIndex
indexOf
lastIndex

replace
toUpperCase
trim

charAt
String.format()//不打印的输出
```
#### Properties
一种map，适合存储和加载文本内容
由于历史原因，它是`Map<Object, Object>`的，但由于使用时当KV都是String，所以不用get直接取，而是用下面的getProperty
```
//这个例子产生一个file
Properties settings = new Properties(); 
settings.put("width", "200");
settings.put("title", "Hello, World!");
try (OutputStream out = Files.newOutputStream(path)) {
    settings.store(out, "Program Properties"); 
}
//结果文件用terminal打开（java9用UTF-8编码，之前用ASCII），或者如下
settings = new Properties();
try (InputStream in = Files.newInputStream(path)) {
    settings.load(in);
}
System.out.println(settings);
//getProperty根据key取值，后面为没有值时的默认值。
String title = settings.getProperty("title", "New Document");
```
API：
` System.getProperties`得到系统属性,forEach遍历全部，也可根据“key”提取

#### BitSet
用于存储bit，比boolean[]更高效
表示标志位序列或非负值的集合，第i个位置为1表示i在集合内。但BitSet不是`Collection<Integer>`
API：
```
BitSet(int可选)
set(index)//设置1或boolean
flip//取反
get//得boolean
next//得int
//还有交叉并对称等
```
#### 枚举set和map
直接用EnumSet和EnumMap
```
enum Weekday { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY };
Set<Weekday> always = EnumSet.allOf(Weekday.class);
Set<Weekday> workday = EnumSet.range(Weekday.MONDAY, Weekday.FRIDAY);//有EnumSet.of

EnumMap<Weekday, String> personInCharge = new EnumMap<>(Weekday.class);
personInCharge.put(Weekday.MONDAY, "Fred");
```

## 关系运算符
引用、算术、关系、逻辑、位、三元

算术：
格外小心/：整数/0会异常，浮点数/0会是无限(1.0/0.0)或NaN(0.0/0.0)
`n%2==0`判断偶数，`n%2!=0`时会有正负之分，小心负数
闹钟时针`((position + adjustment) % 12 + 12) % 12`
++n为递增后产生结果
strictfp修饰后，所有浮点运算都是严格可移植

'=='用来比较对象的引用，如`numbers.get(i) == numbers.get(j)`如果i不等于j，那这个判断是绝对不会true的;.equals("abcd" )比较实际内容

位运算符
```
x = 0011 1100
y = 0000 1101
-----------------
x & y = 0000 1100 //与
x | y = 0011 1101 //或
x ^ y = 0011 0001 //异域
~x  = 1100 0011 //非
x << 2 = 1111 0000 //左移
x >> 2 = 0000 1111
x >>> 2 = 0000 1111 //>>，若操作的值为正，则在高位插入0；若值为负，则在高位插入1；>>>，无论正负，都在高位插入0。
```
三元运算符
`y = x >= 0 ? x : -x;`

## 控制流

```
//1.可循环前声明多个变量
for (int i = 0, j = n - 1; i < j; i++, j--)

//2.outer跳至所标记的层外
outer:
while (){
    ...
    while (){
        ...
        if () break outer;
        ...
    }
...
}
//此处


//3.如果真的不需要break，在switch上面写上@SuppressWarnings
switch(){
    case 1 ://其他类型包括char, byte, short, int及其包装类；字符串；枚举
        ...
        break; //记得加
    default:
        ...
}

//4.局部变量
//注意下面的next必须在循环外声明，因为如果在循环内部声明，其作用范围在do{}的内部就结束了
//循环内部不能再声明next，会重叠
int next
do{
    next = generator.nextInt(10);
    count++;
}while (next != target);
//另外，在循环后，next依然可以用。for循环一样。
```
## 常用class

### Math类
.pow, .squrt, ...
.multiplyExact(x,y)在溢出时会报错，普通“*”不会。这类方法，参数都是int或long
toIntExact无法将long转int时报错
.round四舍五入
BigInteger.valueOf(long)将long转换为BigInteger。字符串也可new BigInteger。大数中不允许操作符
BigDecimal.valueOf(2,0).substract(BigDecimal.valueOf(11,1))计算2.0-1.1
其他类的数学方法：
处理无符号值compareUnigned, divideUnsigned, remainderUnsigned
StrictMath严格可重现的浮点运算

### Random类
`Int a = new Random().nextInt()`或创建Random对象后再用`.nextInt(10)`0～9随机整数

### 日期
```
LocalDate date = LocalDate.of(year, month, day);
date = date.plusDays(1);
.getDayOfMonth

```


## 面向对象(类)
class：一类object的状态和行为的模板
成员变量，方法中声明的变量都属于局部变量
变量成为域；域、方法、嵌套类/接口统称类成员

方法类型：
Accessor:越来越普遍，尤其并发
Mutator:如果一个类没有这类方法，就不怕同时修改问题（两个变量引用同一对象时），如String, LocalDate

编写Java程序实际就是不断定义类和使用类

### 1.定义类
1.1
```
class Car{
    private int color;
    private Engine engine;
}
```
1.1.1名字：
一般情况下，都是一个类一个.java文件
如果一个.java文件里有多个类，只可能有一个public的类，而且文件名必须和public类同名
如果文件里所有类都不是public的，那么文件名和任意一个类同名即可

1.1.2变量可见性（方法的一样）
private(实例变量，如果方法与类用户无关), public（类和方法都是最常用的）
protected：其子类（在其他包也可）可以访问和同包类。保护域要慎用，保护方法和构造函数比较常见。
没有修饰：同一个包的类可以访问

类的可见性：同上，不同在于private:只能用于修饰内部静态类，一般不提倡；没有protected

1.1.3定义类的属性（也称为成员变量）

1.2定义类的方法
this在方法内部能区分局部变量和其他变量，而且能省去给变量取其他名字，如下
`this.salary = salary;`

在Java中，所有参数——对象引用以及基本类型，都是值传递？

注意作用域，在方法内改变形参的引用的效果不会超出方法，如下
```
//下面如果传入某参数，该参数并不会被改变。
public void increaseRandomly(double x){
    double amout = x * generator.nextFouble();
    x += amount;//使x引用新值，方法结束后，作用域就结束了
}
```
方法名相同，但是参数不同的现象，称之为方法重载。(仅仅是返回类型不一样不能构成方法重载)
```
class Car {
    public void run(int speed) {
        System.out.println("run with speed" + speed);
    }
    
    public void run() {
        System.out.println("run with speed 80");
    }
}
```

1.3定义类的构造方法（也称构造器或者构造函数）

默认构造器不包含任何参数，会自动调用其父类的无参数构造器。因此如果父类没有无参数的构造器，会发生编译错误。
如果已经有一个构造函数，又想要一个无参数构造函数，就必须亲自创建

构造器重载
```
public Post(String title, String content) {//不能加void，否则就变成普通方法
        this("", salary);//调用其他构造函数，这类语句必须写在第一条；这里的this和下面的不一样，是一种特殊语法
        this.title = title;
        this.content = content;
    }
```
构造器有时时private的，如LocalDate，它通过.of()或者.now来创建，而非new
### 2.使用类

2.1 new
每次基于类创建对象时（即访问类的构造器创造引用变量），就会在**堆**中分配新的内存来保存新的对象信息，而引用变量本身（和所记录的引用位置）则存储在**栈**中
堆中的变量可以指向堆里的其他变量，比如某类的一些变量为引用类型时

基本类型变量和其的值本身都在栈中，但其作用域结束（比如main方法执行结束）时，其栈内存会自动释放。

2.2方法调用
会跳转到方法的内部执行，方法执行完之后，形参专用的内存就会被释放掉

### 3.其他类的类型
**嵌套类**
静态型（对应内部类）
```
public class Invoice {
    private static class Item {//嵌套类，private，只有Invoice类能访问。如果用public，这个类就跟普通的一样了，只是调用时要在前面加上Invoice.
        String description；
        int quantity;
        double unitPrice;
        
        double price(){ return quantity * unitPrice; }
    }
    private ArrayList<Item> items = new ArrayList<>();
    
    public voice addItem (String description, int quantity, double unitPrice) {//构造嵌套类
        Item newItem =new Item();
        newItem.description = description;
        newItem.quantity = quantity;
        newItem.unitPrice = unitPrice;
        items.add(newItem);
    }
}
```
当嵌套类的实例不需要外部类的具体实例时，就可以用static修饰（就像静态方法不能有实例变量）。

**内部类**
内部类可以调用外部类的实例变量和方法
内部类被创建时会记住其对应的外部类
外部类任何实例可以调用内部类构造函数
`Network.Member wilma = myFace.new Member("wwww");`myFace是Network的一个实例
内部类不能声明静态成员（会有歧义）

**局部类**
方法内定义的类。该类不需要声明权限，因对方法外部而言，永远不可访问。
局部类的方法可以访问其被包含的方法的传入的参数，如下面next方法引用了low，high和generator
```
private static Random generator = new Random();
public static IntSequence randomInts(int low, int high) {
    class RandomSequence implements IntSequence {//如果变为嵌套类，就要再弄个构造函数接受这些引用的变量
    public int next() { return low + generator.nextInt(high - low + 1); }
    public boolean hasNext() { return true; }
    } 
    return new RandomSequence();
}
//上面可以改为匿名类return new Interface(){method}
public static IntSequence randomInts(int low, int high) {
    return new IntSequence() {
        public int next() { return low + generator.nextInt(high - low + 1); }
        public boolean hasNext() { return true; }
    }
}
//只有当提供两个或以上方法时，匿名类才是必须的。如果IntSequence其中一个方法已默认实现，那就可以改为Lambda
public static IntSequence randomInts(int low, int high) {
    return () -> low + generator.nextInt(high - low + 1);
}
```

## 静态变量与静态方法
静态：所有实例都共享该成员变量/方法，可直接通过类名访问这类变量。
下面代码不适用于并发构造对象
```
public class Employee {
    
    private static int lastId = 0;
    private int id;
    private static final Random generator = new Random();//静态常量，避免重复创造
    
    static {//JVM会按照static代码块在类中出现的顺序依次执行它们，且每个代码块只能执行一次。
        //比如给某个static变量进行for的初始化 
    }
    public Employee(){
        lastId++;
        id = lastId;
    }
    
    public static final int getCount(){//final修饰方法不让子类覆盖，而且高效
        return lastId;
    }
}

Post.getCount()//直接使用，不需要创建实例
```
静态方法中不能用this和super关键字；只能访问static数据，引用任何实例变量都是非法的；仅能调用其他的static方法。

实例可以调用static方法，但不是好习惯

工厂方法（构造器的补充）：返回一个类的新实例的static方法

## 继承
### 基本例子
```java
class Animal {

	protected String name;//如果private，子类代码无法访问name，protected允许direct subclass访问
	protected String species;//也可以设private，让子类通过get获取
	protected int age;

	public Animal() {//显式地增加默认构造器；如果用private则无法用这个constructor（其实new相当于调用constuctor这个方法）
        //super()其实这里隐含调用super()，不用显式表示。另外super()和this()不能写在一起，this()会调用super()
		this.name = "Animal";
		this.age = 2;
		this.species = "species";
	}

	public Animal(String name, String species, int age) {
		this.name = name;
		this.age = age;
		this.species = species;
	}
	public final int getAge() {//子类不能覆盖，如Object的getClass
		return age;
	}
	public String getName() {
		return name;
	}
	public String getSpecies() {
		return species;
	}

	public void printInfo() { 
		System.out.println(getAge() + "," +getName() + "," + getSpecies());
	}
}

final class Elephant extends Animal {//在没有其他subclass时可用final防止被继承
    //此处定义父类中已有的变量无意义

{
    species = "Elephant"; //构造块可用于多个构造器复用代码；如果比较长，可以将它放在辅助方法中，并在构造函数中调用
}
	public Elephant() {//没有定义的默认从父类的默认构造器取值
		this.name = "Elle";
	}

	public Elephant(String name, int age) {
		this.name = name;
		this.age = age;
	}
	public String getName() {//重载父类方法。1.如果此处要加入参数，则成了与父类同名的新方法，要在前面@Override（父类没有这个名字的方法却override会报错。2.返回类型也可以改.3.override的权限不能不能比父类窄，父为public，override也要public，不能漏.4.可通过super::method，super.method调用父类方法）
		return super.getSpecies + name;//如果要调用父类方法，加上super
	}
}

final class Panda extends Animal {

{
    species = "Panda";
}

	public Panda() {
		this.name = "Spot";
	}

	public Panda(String name, int age) {
		this.name = name;
		this.age = age;
	}
}

class Zoo {

	private ArrayList<Animal> animals = new ArrayList<>(5);

	public void addAnimal(Animal a) {
		animals.add(a);
	}

	public void printAllInfo() {
		for (Animal c : animals) {
			c.printInfo();
		}
	}
}

public class ZooBuilder {
	public static void main(String[] args) {
		Animal a = new Animal();
		Panda b = new Panda();
		Elephant c = new Elephant("apple", 10);
		Panda d = new Panda("boy", 20);
		Elephant e = new Elephant();
		Zoo zoo = new Zoo();
		zoo.addAnimal(a);
		zoo.addAnimal(b);
		zoo.addAnimal(c);
		zoo.addAnimal(d);
		zoo.addAnimal(e);
		zoo.printAllInfo();
		System.out.println(c.name + " " + c.species + " " + c.age);

	}
}
```
结果为
2,Animal,species
2,Spot,Panda
10,apple,Elephant
20,boy,Panda
2,Elle,Elephant

补充：
```
Manager boss = new Manager(…);
Employee empl = boss;
double salary = empl.getSalary();//调用子类方法，编辑器会找实际类型

Manager[] bosses = new Manager[10];
Employee[] empls = bosses; // Legal in Java
empls[0] = new Employee(…); // 运行时出错ArrayStoreException，反过来Employee下new Manager就可以

//创建对象，并以父类作类型后，不能调用子类方法
Employee empl = new Manager(…);
empl.setBonus(10000); // Compile-time error
//可以通过转换解决
if (empl instanceof Manager) {
    Manager mgr = (Manager) empl;
    mgr.setBonus(10000);
}
```
### 匿名子类（调试方便，但不推荐）
```
//下面100被传ArrayList<String>构造函数中，即会有100个位置，另外add方法是覆盖ArrayList<String>的方法。
ArrayList<String> names = new ArrayList<String>(100) {//此处开始就是一个类了

    {//初始化模块
        add.("Hello");//调用父类add
    }
    public void add(int index, String element) {
        super.add(index, element);
        System.out.printf(“Adding %s at %d\n”, element, index);
    }
};
```
### Object类
方法
```
.toString
.equals()
Int .hasCode
protected clone
Class<?> .getClass()
.finalize//当垃圾回收器回收对象时被调用
.wait
.notify
.notifyAll
```
1.toString
默认为类名和哈希码？，当与String连接时，自动调用，在println也是
```
//重写的惯例写法
public String toString() {
    return getClass().getName() + “[name=” + name
        + “,salary=” + salary + “]”;
}
```
打印数组时推荐用println(Arrays.toString(p))，p为数组 

2.equals()
推荐使用静态方法Object.equals(obj,other)
覆盖时必须同时提供一个兼容的hashCode
惯例先调用父类equals，如if(!super.equals(otherOBject)) return false;但注意父类使用equals还是instanceof会决定子类的对称性
```
public class Item {
    private String description;
    private double price;
    
    public boolean equals(Object otherObject) {
    //引用是否相同
    if (this == otherObject) return true;
    //参数是否为空，equals与null比较一律为false，所以只能用==
    if (otherObject == null) return false;
    // 类型
    if (getClass() != otherObject.getClass()) return false;
    // 具体值，转换前检查。用instanceof时，子类等于父类，父类不等于子类。
    Item other = (Item) otherObject;//因为传入是类型为Object，所以要转换回原类型，这样才能查看实例变量。
    return Objects.equals(description, other.description) && price == other.price;}//注意对String和数值的不同处理；担心正负无穷和NaN的话，也用equals 
    public int hashCode() { … } 
}
```
如果是数组，可以用Array.equals来比较长度和具体元素。
equal遵循对称性，所以父类和子类是不等的。

3.int .hasCode
如果对象不等，hashCode也很可能不等
必须与equals兼容，如果x.equals(y)为true，那么x.hashCode() == y.hashCode()也为true。
```
//联合各个实例变量的哈希码
class Item {
    …
    public int hashCode() {
        return Objects.hash(description, price, Array.hashCode(a));//假设有个数组a的实例变量。Objects.hash方法的参数可变
    }
}
```
4.protected clone
此类不会经常被覆盖
（1）不提供clone，什么都不用改，因为clone是protected的，和Object不同包的其他非子类调用不了。
（2）提供继承的clone
（3）需要深拷贝clone
浅拷贝：从原对象中拷贝所有实例变量拷贝到另一个对象中。即如果变量不全是基本类，则会共享。
深拷贝(重写)
```
//Message的变量如下
public final class Message implements Cloneable{
    private String sender;
    private ArrayList<String> recipients;
    private String text;
}

//情况（2）：实现Cloneable，throw Exception
public class Employee implements Cloneable {
    public Employee clone() throws CloneNotSupportedException {//改为public方便使用
    return (Employee) super.clone();
    }
}

//情况（3）
public Message clone() {
    Message cloned = new Message(sender, text);
    cloned.recipients = new ArrayList<>(recipients);//recipients里的元素为string不会共享，此方法可行，否则看下面
    return cloned;
}

public Message clone() {
    try {
        Message cloned = (Message) super.clone();
        @SuppressWarnings(“unchecked”) ArrayList<String> clonedRecipients
            = (ArrayList<String>) recipients.clone();//历史原因，这样避免警告
        cloned.recipients = clonedRecipients;//如果recipients是数组，则不需转换，直接= recipients.clone()
        return cloned;
    } catch (CloneNotSupportedException ex) {
        return null; // Can’t happen因为Message是Cloneable且final
    }
}
```
### 枚举
```
public enum Size { SMALL, MEDIUM, LARGE, EXTRA_LARGE //枚举实例};
//使用
Modifier a = Modifier.SMALL;
//如果import Modifier.*，就不用加Modifier
```
由于每个枚举类型，上面的Size，都有固定的实例集，所以可以直接用==而非equals
不需提供toString
.values是枚举数组`Size[] allValues = Size.values();`，可以用增强for遍历
`.MEDIUM.ordinal()`返回index
1.构造函数、方法和域
每个枚举类型实例保证只被构造一次？
```
public enum Size {
    SMALL(“S”), MEDIUM(“M”), LARGE(“L”), EXTRA_LARGE(“XL”);
    private String abbreviation;
    Size(String abbreviation) {//默认私有，且只能私有
        this.abbreviation = abbreviation;
    } 
    public String getAbbreviation() { return abbreviation; }
}
```
实例的实现体（方法）
```
public enum Operation {
    ADD {
        public int eval(int arg1, int arg2) { return arg1 + arg2; }
    },
    SUBTRACT {
        public int eval(int arg1, int arg2) { return arg1 - arg2; }
    };
    public abstract int eval(int arg1, int arg2);
}
//每个枚举常量都属于Operation的一个匿名子类，可放入匿名子类的任何东西
//使用
int result = op.eval(1,2);op为其中一个实例
```
静态成员
```
//下面每个枚举实例都有一个mask值
public enum Modifier {
	PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, ABSTRACT;
	private int mask;
	static {//不能引用内部实例变量
		int maskBit = 1;
		for (Modifier m : Modifier.values()) {
			m.mask = maskBit;
			maskBit *= 2;
		}
	}
}
```
switch枚举对象
```
public static int eval(Operation op, int arg1, int arg2) {
    int result = 0;
    switch (op) {
        case ADD: result = arg1 + arg2; break;
        case SUBTRACT: result = arg1 - arg2; break;
        case MULTIPLY: result = arg1 * arg2; break;
        case DIVIDE: result = arg1 / arg2; break;
    }
    return result;
}
```



## 抽象类，接口与Lambda
### 抽象类和方法
抽象类不能实例化对象，所以抽象类必须被继承，才能被使用
如果一个类包含抽象方法，那么该类必须是抽象类。
任何子类必须重写父类的抽象方法，否则就必须声明自身为抽象类

```
abstract class Graph {

    String name;
    
    public Graph(){}
    
    public Graph(String name) {
        this.name = name;
    }
    
    public void show() {//可以有非抽象方法
        System.out.println("I'm a graph");
    }
    
    public abstract double area(); //因为我们不知道图形的形状，我们是无法给出实现的，只能交给特定的子类去实现；方法名后面直接跟一个分号，而不是花括号
}

//实现
class Rectangle extends Graph{
    
    ...
    
    public double area() {
        return width * height;
    }
}

//虽然不能构造抽象类实例，但可以有类型为抽象类的变量。创造其子类，然后转化为父类
```

### 接口
一组抽象方法的集合

 - 必须实现接口内所描述的所有方法，否则就必须声明为抽象类
 - **与抽象的比较**
    在抽象类中可以为部分方法提供默认的实现，从而避免在子类中重复实现它们；但是抽象类不支持状态的多继承（继承类）。接口不能提供任何方法的实现（Java8可以提供），但是支持多继承。
 - 接口访问权限有两种：public权限和默认权限。如果接口的访问权限是public的话，所有的方法和变量都是public。
 - 接口也可以extends 
 - 接口的变量自动为public static final(public情况下)，可通过interfaceName.variableName访问，当实现接口时，可直接写变量名，但不推荐

```
interface Animal {

    static final int TIMES_OF_EATING = 3; //一般是final和static类型的，要以常量来初始化，实现接口的类不能改变接口中的变量。 
     
    void eat();//接口中的方法都是外部可访问的，因此我们可以不需要用public修饰；接口中的方法是不能为静态的
    void sleep();
}
```
 - 使用接口类型来声明一个变量，那么这个变量可以引用到一个实现该接口的对象
`Animal cat = new Cat()`Animal为接口，把它作为类型
这让程序更具有扩展性，比如某方法可以返回`List<Post>`，而不需要指定为具体的`ArrayList<Post>`。这样将来如果我们希望返回`LinkedList<Post>`的时候也无需修改接口。
但是该对象实际上是它本身的类，而非接口
 - 有时有必要转换回子类，如下
```
if (object instanceof Type){//判断object实质上是不是Type，然后再转
    Cat a = (Cat) b;//这样才能调用子类的方法
}
```
 - 实现的方法声明用public
 - 静态方法和默认方法：前者用于工厂方法，调用者无须关心它是哪个类；后者即默认实现，对于就代码能正常使用（没默认），需要重新编译才能有默认方法
```
//静态方法
public interface IntSequence {
    …
    public static IntSequence digitsOf(int n) {
        return new DigitSequence(n);
    }
}

//默认方法
public interface IntSequence {
    default boolean hasNext() { return true; }
        // By default, sequences are infinite
    int next();
}

//解决默认方法冲突：自己重新实现，或者如下
public class Employee implements Person, Identified {//只要两个interface有同名方法就会冲突，不管另外一个有没有默认实现
    public int getId() { return Identified.super.getId(); }//指明哪一个，并加上.super
    …
}
//如果所继承的父类与接口有同名方法，那么会忽略接口的同名方法，没有冲突
```
 - 接口例子
Comparable内部比较器
```
 //如果一个类想启用对象排序（不同对象标准不一样），就要实现该接口
public interface Comparable<T> {
    int compareTo(T other);
}

x.compareTo(y)//返回负数，0，正数，返回数任意，这样允许返回两个“非负整”数的差。如果有“负整“数，用Interger.compare。浮点数都用Double.compare
public class Employee implements Comparable<Employee> {
    …
    public int compareTo(Employee other) {
        return getId() - other.getId(); // 不需要结果刚好等于-1或1。在Java中，方法能够访问自己所在类对象的任何私有变量
    }
}

//只要实现上述接口，该class就能通过Arrays.sort(Array<class>)进行排序。注意，编译时不会检查参数对象是否Comparable，而是在运行时抛出异常
```
 Comparator外部比较器
```
//第二个版本的Arrays.sort
public interface Comparator<T> {
    int compare(T first, T second);
}

class LengthComparator implements Comparator<String> {
    public int compare(String first, String second) {
        return first.length() - second.length();
    }
}
//用时还需要创建以一个实例，不是静态方法
Comparator<String> comp = new LengthComparator();
if (comp.compare(words[i], words[j]) > 0)

//对数组进行排列时，要传入实现该接口的方法，这样才能实现其他（不同于上面comparable的）sort方式
Arrays.sort(friends, new LengthComparator);//写Lambda更好
```
Runable
```
class HelloTask implements Runnable {
    public void run() {
        for (int i = 0; i < 1000; i++) {
            System.out.println(“Hello, World!”);
        }
    }
}

Runnable task = new HelloTask();
Thread thread = new Thread(task);
thread.start();
//现在HelloTask就在另外一个线程执行
```
UI（User Interface）回调
```
//JavaFX中，该接口用来报告事件
public interface EventHandler<T> {
    void handle(T event);
}
//单击事件的反应
class CancelAction implements EventHandler<ActionEvent> {
    public void handle(ActionEvent event) {
        System.out.println(“Oh noes!”);
    }
}
//创建按钮
Button cancelButton = new Button(“Cancel”);
cancelButton.setOnAction(new CancelAction());
```
### Lambda和方法引用
**Lambda**
1.特点
不需返回类型
作用域
```
//下面会出错，first已被定义
int first = 0;
Comparator<String> comp = (first, second) -> first.length() - second.length();

//下面this调用Application的方法
public class Application() {
    public void doWork() {
        Runnable runner = () -> { …; System.out.println(this.toString()); ...};
        …
    }
}
```
闭包
带有自由变量值（既不是参数变量，也不是代码内部定义的变量，如下面的Lambda中的i）的代码块。
Lambda会访问（捕获）定义在闭合域的变量（如在一个方法里面创建Lambda）
不能依赖编译器去捕获所有并发访问的错误。
```
//会出错，i不是作为参数传入的，所以不能变，否则Lambda不知道用哪个i。Lambda只能访问来自闭合作用域的final局部变量
for (int i = 0; i < n; i++) {//但是可以用增强for，这种方式中变量是有效final的
    new Thread(() ->System.out.println(i)).start();
}
//Lambda内部也不能改变捕获的变量，下面方法可以避免，但不适合多线程
int[] counter = new int[1];
button.setOnAction(event -> counter[0]++);
```

2.思路
(1)思考参数和行为`(String first, String second) -> first.length() - second.length()`。
根据参数是否为空，行为是否能够一个表达式解决。
如果参数类型可推断，类型也可以省。
只有一个参数，且能被推断。
(2)对象为标准类时，看其方法是否接受函数式接口的参数，函数描述符是否匹配，如
`list.removeIf(e -> e == null)`接受`Predicate<T>`
尽量使用预设接口。当然，如果实在无法满足需求，自建，如Function接口接受泛型，这对于基本型会自动装箱，为了避免降低效率，可创建如下面的接口
```
@FunctionalInterface//记得写上
public interface PixelFunction {
    Color apply(int x, int y);
}
//运用时（这里忽略了createImage的代码）
BufferedImage frenchFlag = createImage(150, 100,
(x, y) -> x < 50 ? Color.BLUE : x < 100 ? Color.WHITE : Color.RED);
```
**方法引用**
构造函数的引用
创造一个员工列表`Stream<Employee> stream = names.stream().map(Employee::new);`由于names是`List<String>`所以调用的构造函数是Employee(String)。类似的如`int[]::new`
这方法能创造不同类型的数组，如
`Employee[] buttons = stream.toArray(Employee[]::new);`不加方法引用，此处只能得到`Object[]`

**高阶函数**
处理或返回函数的函数
```
Arrays.sort(people, Comparator
    .comparing(Person::getLastName)
    .thenComparing(Person::getFirstName));
//下下面更好    
Arrays.sort(people, Comparator.comparing(Person::getName,(s, t) -> s.length() - t.length()));

Arrays.sort(people, Comparator.comparingInt(p -> p.getName().length()));
//如果getMiddleName会返回null，用nullsFirst
Arrays.sort(people, comparing(Person::getMiddleName,
nullsFirst(naturalOrder())));//nullsFirst里面需要一个比较器，也有reverseOrder
```




## 异常、断言与日志
### 异常
Throwable: Error（内存耗尽等）和Exception
后者：已检查异常checked exception(可预料的)，未检查异常RuntimeException（不可预料的）。

Checkedexception：IOException、SQLException等以及用户自定义的Exception异常。对于这种异常，JAVA编译器强制要求我们必需对出现的这些异常进行catch并处理，否则程序就不能编译通过。

RuntimeException：NullPointerException、IndexOutOfBoundsException等。当出现这样的异常时，总是由虚拟机接管。如果没有捕获处理这个异常（即没有catch），系统会把异常一直往上层抛，一直到最上层，如果是多线程就由Thread.run()抛出，如果是单线程就被main()抛出。最后要么是线程中止，要么是主程序终止。

```
try {
    int x = 1 / 0;
    System.out.println(x);
} catch (Exception1 | Exception2 e) { //可以多个异常执行相同
    logger.log(level, message, e);//记录
    throw ex;//重新抛出
    
    throw new ServletException("..",e);//可以捕获异常后，抛出一个更高级别的异常。其他异常可能只接受string参数，此时按下面方式处理。如果是自定义异常（查5.1.7）
    Throwable e2 = new xxxException("");
    e2.initCause(e);
    throw e2;
    
    e.printStackTrace();//打印堆栈踪迹信息。getStackTrace()可得更详细的信息
}
```
可能抛出异常的任何方法都必须加上throws
当覆盖一个方法时，其异常不能比父类的多（父类无throws时自然一个exception都不能有），且不能无关。
```
public class HelloWorld {    

    private static void foo() throws Exception {//throws关键字，然后增加这个方法可能抛出的异常Exception,调用者就必须使用try/catch进行处理
        int x = 5 / 0;
        System.out.println(x);
    }

    public static void main(String[] args) {
        try {
            foo();
        } catch (NullPointerException e) {//各种catch代码块的放置顺序非常重要。如果使用catch(Exception e)语句，那么它不能放在其他catch语句的前面，否则后面的catch永远得不到执行
            e.printStackTrace();
        } catch (ArithmeticException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {//finally中的代码块不能被执行的唯一情况是：在被保护代码块中执行了System.exit(0)。
            System.out.println("programe will run `finally` code block!");
        }
        System.out.println("program is still running here!");
    }
}
```

每个try语句必须有一个或多个catch语句对应，try代码块与catch代码块及finally代码块之间不能有其他语句。

要避免finally出现异常，否则它会覆盖try里面的异常。前者的return同样会覆盖后者的return。对于这种情况，改try/catch/finally为try-with-resources或try{try/catch}/finally

自定义异常类
```
public class BlogAppException extends Exception {

    private static final long serialVersionUID = 1L;
    
    private String command;// 可以给自定义异常增加成员变量，用以保存额外的异常信息
    
    public BlogAppException(String command) {
        this.command = command;
    }
    
    public String toString(){
        return "Exception happened when executing command " + command;
    }
}
```
```
//构造自定义异常时，最好一个不带参，一个带信息的
public class FileFormatException extends IOException {
    public FileFormatException() {}
    public FileFormatException(String message) {
        super(message);
    }
    // Also add constructors for chained exceptions—see Section 5.1.7
}
```

一个方法被覆盖时，覆盖它的方法必须扔出相同的异常或异常的子类。
如果父类抛出多个异常，那么重写（覆盖）方法必须扔出那些异常的一个子集,也就是说，不能扔出新的异常。

检查参数是否为空
```
public void process(String Directions){
    this.directions = Objects.requireNonNull(directions,"..."option);
}
```
### 断言
断言机制允许在测试时加入测试条件，并且在生产代码中自动移除他们
而抛出异常的检查会减慢运行速度，当然，如果需要报告，也只能用抛出异常
```
assert x>=0;
assert x>= 0 : x;
```
启用和禁用
`java -ea MainClass`
`java -ea:MyClass -ea:com.mycompany.mylib… MainClass`特定类和整个包断言
`java -ea:… -da:MyClass MainClass`禁用部分
系统类用-esa
也可通过编程方式控制加载器的状态

### 日志
1.介绍
`Logger.getGlobal().info(“Opening file ” + filename);`info记录
如果用了`Logger.global.setLevel(Level.OFF);`info则失效，但“Opening file ” + filename依然会被创建，若想消去创建的影响，用`Logger.getGlobal().info(() -> “Opening file ” + filename);`

但你不会把所有记录到给一个global logger
`Logger logger = Logger.getLogger(“com.mycompany.myapp”);`
下一次调用该变量就会产生相同的logger对象
如果关闭父包logger（com.mycompany），子包也会被关闭

其他方式
```
//FINER级别的日志
public int read(String file, String pattern) {
    logger.entering(“com.mycompany.mylib.Reader”, “read”,
        new Object[] { file, pattern });
    …
    logger.exiting(“com.mycompany.mylib.Reader”, “read”, count);
    return count;
}

//记录意外的异常
try {...}catch (IOException ex) {
logger.log(Level.SEVERE, “Cannot read configuration”, ex);
}

if (…) {
    IOException ex = new IOException(“Cannot read configuration”);
    logger.throwing(“com.mycompany.mylib.Reader”, “read”, ex);//记录FINER级别记录和一条THROW开头信息
    throw ex;
}
```

2.日志等级
SEVERE, WARNING, INFO, CONFIG, FINE, FINER,
FINEST
默认情况下，前三个等级会被记录，如果设置一个更后的等级，其前面的都会被记录。
例：
`logger.warning(message)`
```
Level level = …;
logger.log(level, message);
```

3.日志记录配置项
`java -Djava.util.logging.config.file=configFile MainClass`
日志管理器在VM启动阶段初始化，所以在main设置没效果。

要在控制台看到FINE级别信息，要设置
`java.util.logging.ConsoleHandler.level=FINE`

4.日志处理器
默认情况下，日志处理器将记录发给ConsoleHandler，后者通过System.err流打印出日志
logger和handler默认都是INFO，想要更低等级就要像下面那样改
```
Logger logger = Logger.getLogger(“com.mycompany.myapp”);
logger.setLevel(Level.FINE);
logger.setUseParentHandlers(false);//避免记录两次INFO以上
Handler handler = new ConsoleHandler();
handler.setLevel(Level.FINE);
logger.addHandler(handler);
```
记录可以发到其他地方FileHandler和SocketHandler
```
//记录到文档中
FileHandler handler = new FileHandler();
logger.addHandler(handler);
```
路径在javan.log文件中，并被格式化为XML

5.过滤器和格式化器

补充：被虚拟机优化后，某些信息可能不存在了。logp获取准确位置`void logp(Level l, String className, String methodName, String message)`

## 泛型
1.基础
泛型类，泛型方法
把类型作为参数传入到泛型类中
类型参数不能是原生类型，必须是引用类型，如要用Integer，而非Int

泛型方法
不要求这个方法所属的类为泛型类。泛型类和方法中的T是没有关联的，如下
```
//假设定义时，类和方法的参数都是（T t）
Test1<String> t = new Test1();
Integer i = t.testMethod(new Integer(1));
```

```
public class Printer {
    public static <T> void printArray(T[] objects) {//需要在方法返回值前用尖括号声明泛型类型名
        if (objects != null) {
            for(T element : objects){  
                System.out.printf("%s",element);  
            }  
        }
    }
    
    public static void main(String[] args) {  
        Character[] charArray = { 'T', 'I', 'A', 'N', 'M', 'A', 'Y', 'I', 'N', 'G' };
        
        printArray(charArray);
    }     
}

```
对泛型进行限定
```
//数组的话不需要类型限定，其子类可以直接进参数里（协变性）
public static void closeAll(AutoCloseable[] elems) throws Exception
//数组可以由子类[]转换到父类[]，但是子类[]存父类对象还是会报异常

//数组列表
public static <T extends AutoCloseable & ...> void closeAll(ArrayList<T> elems)
throws Exception {//可限定多个接口一个类，且类要放在首位
    for (T elem : elems) elem.close();
}
```
如果子类ArrayList能赋值给父类ArrayList，那么子类ArrayList就可能存放父类元素，这会损坏数组列表。

2.通配符
如果一个方法并不对ArrayList做写操作，则不同ArrayList之间转换是安全的。如下
```
public static void printNames(ArrayList<? extends Employee> staff) {
    for (int i = 0; i < staff.size(); i++) {
        Employee e = staff.get(i);//如果用add就有问题，null可以
        System.out.println(e.getName());
    }
}
```
这代码说明，子类可转父类，反转不行，子类有多种
逆变，适合consumer，用super。PECS
```
public static void printAll(Employee[] staff, Predicate<? super Employee> filter) {
    for (Employee e : staff)
        if (filter.test(e))
            System.out.println(e.getName());
}
```
带类型变量的通配符
```
public static <T> void printAll(T[] elements, Predicate<? super T> filter)

public static <T extends Comparable<? super T>> void sort(List<T> list)
//假设F类实现了Comparable<F类>接口，而S类继承了F类，那S类也只实现了Comparable<F类>接口但他是Comparable<? super S类>的子类。所以上述方法就不需要子类重新实现接口
```
```
//推荐用回T
public static boolean hasNulls(ArrayList<?> elements) {//elements只能调用类型无关的方法
    for (Object e : elements) {
        if (e == null) return true;
    }
    return false;
}

//?可用Helper捕捉
```
3.Java虚拟机中的泛型（略）
类型擦除：变为object或者规定上限
所以用反射是要注意，例如`getDeclaredMethod("methodName",Object.class)`

转换插入

桥方法

4.泛型约束

- 无基本类型参数
- 所有类型在运行时都是原始的
`if (a instanceof ArrayList<String>)`泛型中会报错
下面方式无效，但有时必须。如果result是outcome of a very general process（如反射），编译器不知道它的类型，程序员必须像下面转换，以避免警告 
```
@SuppressWarnings(“unchecked”) ArrayList<String> list
    = (ArrayList<String>) result;
```
- 不能实例化类型变量
如new T
```
//数组
public static <T> T[] repeat(int n, T obj, IntFunction<T[]> constr) {
    T[] result = constr.apply(n);
    for (int i = 0; i < n; i++) result[i] = obj;
    return result;
}

//调用
String[] greetings = Arrays.repeat(10, “Hi”, String[]::new);

//推荐数组列表
public static <T> ArrayList<T> repeat(int n, T obj) {
    ArrayList<T> result = new ArrayList<>(); // OK
    for (int i = 0; i < n; i++) result.add(obj);
    return result;
}

public class ArrayList<E> {
    private Object[] elementData;
    public E get(int index) {
        return (E) elementData[index];
    }
    …
}
```
- 不能构造参数化类型的数组
```
Entry<String, Integer>[] entries = new Entry<String, Integer>[100];//Entry后为泛型

//用数组列表就没问题
ArrayList<Entry<String, Integer>> entries = new ArrayList<>(100);
```
- 静态变量和方法不能用类型变量
- 擦除后方法冲突
擦除后，T变为object，使得同名方法真正重叠而冲突。
父类实现了comparable<F类>，而子类不能再实现comparable<S类>
- 不能抛出或捕获一个泛型类对象。实际不能构建一个Throwable的泛型子类（xxx<T> extends Exception），catch(T e)错误。throws声明可以用T（throws T）

5.反射与泛型（略）

## IO
java.io包，主要涉及文件，网络数据流，内存缓冲等的输入输出
字节流：数据流中最小的数据单元是字节，多用于读取或书写二进制数据
字符流：数据流中最小的数据单元是字符， Java中的字符是Unicode编码，一个字符占用两个字节
在最底层，所有的输入/输出都是字节形式的。基于字符的流只为处理字符提供方便有效的方法。

> File类可以是文件或路径，获取对象的属性，进行创建删除等。但不能访问内容。下面有更详细介绍。

### 字节流

的最顶层是两个抽象类：InputStream和OutputStream
可读写任何文件。既有读又有写的，用`try(..){..}`结构把代码都放在{}中。

```java
//从文件读写，每次read和write就是一次多硬盘的操作，次数越多越影响速度。
Path path = Paths.get("alice.txt");
try(InputStream in = Files.newInputStream(path); OutputStream out = ...){
    //一次性读完
    byte[] bytes = in.readAllBytes();
    //分批读
    byte[] buff = new byte[1024];
    buff = in.read();//每次读一个字节
    
    //写入
    int len = if.read(buff);
    while(n != -1){
        out.write(buff, 0, len);
        len = in.read(buff);
    }
}
//一步到位的读取
byte[] bytes = Files.readAllBytes(path);
//一步到位实现上面读写，复制input流到output流
try (InputStream in = ...; OutputStream out = ...) {
    in.transferTo(out);
}

//从URL获取流
URL url = new URL("http://.."); 
try (...url.openStream()){}

//从字节数组获取流
byte[] a =
InputStream in = new ByteArrayInputStream(a);


//读取字节流对应的十进制
InputStream in = ...;
int b = in.read();//返回字节对应整数0～255，尾部可能-1。Java字节类型范围是-128～127，在没有-1情况下可以转为字节类型

//读取流的部分
byte[] bytes...
int bytesRead = in.readNBytes(bytes, offset, n);

//输出
try (OutputStream out = ...) {
    out.write(bytes);
}

// fle to an OutputStream或InputStream to a fle
Files.copy(path, out);
Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);

//输出到字节数组，如果想写其他类型
ByteArrayOutInputStream out = new ByteArrayOutputStream();
out.write(helloBytes);  
byte[] bytes = out.toByteArray();
//如果是对象的话
try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
     ObjectOutput out = new ObjectOutputStream(bos)) {
    out.writeObject(object);
    byte[] bytes = out.toByteArray();
} 

//Buffer，先读取填满默认大小的缓冲区，再对缓冲区read。写入时满了就写（flush），关闭流时会把不满的写出。
BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(path));
bis.close;//关高层就可以
```
如果资源类实现了AutoCloseable，try后就不需要.close。当new PrintWriter出错时，会调用in.close，然后抛出异常
```java
try (Scanner in = new Scanner(System.in);
        PrintWriter out = new PrintWriter(“output.txt”)) {
    while (in.hasNext())
        out.println(in.next().toLowerCase());
}
```
close也可能抛出异常，比较复杂...



### 字符流
处理16位 unicode 的输入和输出（文本文件，word不是）

顶层抽象类是Reader和Writer

```java
//比较短的文件
//字节
String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

//行
List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

//stream
try (Stream<String> lineStream = Files.lines(path, StandardCharsets.UTF_8)) {}

//数字或单词。用FileReader也可。
try (Scanner in = new Scanner(path, "UTF-8")) {
    in.useDelimiter("\\PL+");//正则分隔
    int words = 0;
    while (in.hasNext()) {
        in.next();
        words++;
    }
    System.out.println("Words: " + words);
}
try(Reader fr = new FileReader(new File("path"))){
    int ch = fr.read();//读取一个字符
}

//Buffer,不用stream读行
try (BufferedReader br = Files.newBufferedReader();
        PrintWriter out = new PrintWriter(“output.txt”)) {
    String str = br.readLine();
    while(str != null){
        pw.println(str);
        str = br.readLine();
    }
}

//非文件，如url
try (BufferedReader reader
        = new BufferedReader(new InputStreamReader(url.openStream()))) {
    Stream<String> lineStream = reader.lines();
}
```
文本输出
```java
//可用writer，但PrintWriter比较方便，有printf等方法
path = Paths.get("hello.txt");
try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
    out.println("Hello");
}
//如果写到另一个outStream
PrintWriter out = new PrintWriter(outStream, charset);

//如果已经有内容，就用
String content = "";
Files.write(path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);//没有APPEND就会覆盖了
//或，lines是上面的，List<String>
Files.write(path, lines, StandardCharsets.UTF_8);
```



### 其他流

**打印流：**

PrintStream和PrintWriter


> System.out/err就是PrintStream类。System.in，其类型是InputStream。这些流都有默认的设备(键盘和屏幕)，但它们可以重定向到任何兼容的输入/输出设备。

PrintStream可以写入各种数据类型，且换行。问题在于要加入特殊字符分割内容，且读取得到的都是String。PrintWriter类似。

格式输出：
`System.out.printf("%8.2f", 1000.0/3.0)`8为结果宽度，333.33前面还有两个空格
`"%,+.2f"`“，”分组，“+”正负

**数据和对象流：**

```java
//数据流。对象流就Data改Object，cast，且object要实现序列化
try (OutputStream os = Files.newOutputStream();
     BufferedOutputStream bos = new BufferedOutputStream(os);
     DataOutputStream dos = new DataOutputStream(bos)) {
    dos.writeInt(123);
    dos.writeInt(123);
    //oos.writeObject(new Date());
}//这里try如果出现失败，不会停止下面的执行。

try (InputStream inStream = Files.newInputStream(path)) {            
    DataInput in = new DataInputStream(inStream);
    int i = in.readInt();
    int i = in.readInt();
    //Date date = (Date) ois.readObject();
}
```

> **序列化**
>
> Serializable对象与字节之间的转换
> 如果所有实例变量都是基本类型、枚举和其他序列化的对象，那么实现该接口是安全的。

如果读取密码，用Console。这样比存在String安全，且完成后能重写数组

```java
Console terminal = System.console();
String username = terminal.readLine("...");
char[] passwd = terminal.readPassord("...");
```



### 路径、文件和目录

**路径**
```java
Path path = Paths.get("/", "home", "cay");//会判断书写是否正确
Path workPath = path.resolve("myprog/work");//在path后接上myprog/work
Path tempPath = workPath.resolveSibling("temp");//将最有一个文件work改为temp
relative = path.relativize(Paths.get("/home/fred/myprog"));//与path重叠部分变..加上非重叠部分
.normalize()//..变为相应的操作，如返回上层
absolute = Paths.get("config").toAbsolutePath();//查本目录的默认绝对路径
.getParent()
.getFileName(); 
.getRoot(); 
.getName(0);//第一个文件名
.subpath(0, p.getNameCount() - 1);
//Path可遍
```
有toPath和toFile的方法，方便转换
**文件**
```java
//创建
Files.createDirectory(path);//path中除了最后一个文件外，其他都需要存在
.createDirectories(path);//可创建多个
.createFile(path);//文件已存在会出错
.exists(path)//检查
.isDirectory
.isRegularFile
.createTempFile(dir, prefix, suffix)//在临时目录创建，前后缀可null

//复制移动等
.copy(fromPath, toPath, StandardCopyOption)
.move(StandardCopyOption)
boolean deleted = Files.deleteIfExists(path);
//其他，详细查书
StandardOpenOption: .newBufferedWriter, newInputStream, newOutputStream, write
.find
.walkFileTree
//复制和删除目录的例子

//访问
try (Stream<Path> entries = Files.list(path)) {}//相当于ls。
.walk//能够深度访问，可设定depth。
.find//能在walk基础上过滤，配合一个接受路径和BasicFileAttributes对象的判定函数

//ZIP，包括复制/遍历包里的文件；创建新ZIP等
//ZipInputStream类
```
### URL连接
读取用URL.getInputStream 
添加内容建议用jdk9的HttpClient（方便以及HTTP/2支持），但jdk10才定型

URLConnection的读写
```
//获取对象
URLConnection connection = url.openConnection();//for HTTP URL, HttpURLConnection
//对象设置
connection.setRequestProperty("Accept-Charset", "UTF-8, ISO-8859-1");
//发送数据给服务器（在这之前可查询header信息，详细看书）
connection.setDoOutput(true);
try (OutputStream out = connection.getOutputStream()) {
    // Write to out 
}

//有一个发送表单数据的例子
```
HttpClient
```
--add-modules jdk.incubator.httpclient
```
### 正则表达
```
//基础
.*+?{   //次数
|()  //两个配合
[  //群，配合^表示非，[^0-9]
\  //转义，在群中只需转义"["和"\"；字符关键词，如\d
^$  //首尾

//注意：[]^-] 匹配里面三个；\Q这里特殊字符都失效\E ，除非字符中有\E

//使用
//一次匹配
Pattern.matches(String regex,  CharSequence input)
//多次匹配
patten = Pattern.compile(regex)
.matcher(input)

//在集合或流中匹配
Stream<String> strings = ...;
Stream<String> result = strings.filter(pattern.asPredicate());

//对string
matcher = pattern.matcher(input);
while (matcher.find()) {
    String match = matcher.group();//match就是结果。此处matcher后可用.results()得到Stream<MatchResult>
    
//对于文件，可用scanner的findAll产生流
Scanner in = new Scanner(path, "UTF-8"); Stream<String> words = in.findAll("\\pL+")
    .map(MatchResult::group);
    
//分组匹配
"Blackwell Toaster    USD29.95"
Pattern.compile("(?<item>\\p{Alnum}+(\\s+\\p{Alnum}+)*)\\s+(?<currency>[A-Z]{3})(?<price>[0-9.]*)")//for repetition？
//matcher(input)后
String item = matcher.group("item");

//分隔
"\\s*,\\s*"//逗号以及两边的空白
pattern.split(input)//或splitAsStream。String的split（regex）

//替换
matcher.repleceAll(",")

//Flags标记，调整匹配方式的，如Pattern.CASE_INSENSITIVE。具体看书
```


### 省略部分
输入输出流：
随机存取
内存映射
文件锁

序列化：
readObject和writeObject的修改
readResolve和writeReplace
版本化

## 运行时类型信息、资源和反射
### class类
```
//三种获得class类（包含Type和name，其实上面接口，基本类型，void都不是类，所以这里用Type区分）的方法
//1.getClass
Object obj = …;
Class<?> a = obj.getClass();

//2.class
Class<?> a = Runnable.class//得到interface java.lang.Runnable

//3.Class.forName已知name（数组只能用这种方法）
Class<?> cl = Class.forName("java.util.Scanner");

String b = a.getName();
```
这些类可以直接用==比较

得到class类后可以用各种API了解class的各方面
### 资源、类、上下文等加载
如果config.txt和MyClass.class放在同一个目录里
```
InputStream stream = MyClass.class.getResourceAsStream(“config.txt”);
Scanner in = new Scanner(stream);
```
如果很多类打包进JAR，资源和类一起压缩，位置会被定位

**（未完）**

### 反射
反射是一种动态获取信息以及动态调用对象方法的机制。
在运行时判断任意一个对象所属的类；getClass()
在运行时构造任意一个类的对象；
在运行时判断任意一个类所具有的成员变量（包括类型和修饰符，可以改变对应属性的可见性，也可以通过set()方法进行属性的赋值）和方法；
在运行时调用任意一个对象的方法；
生成动态代理。
```
//下面是取得class的三种方法
public class ReflectionTest {
    public static void main(String[] args) throws Exception {
        Class<?> class1 = null;
        Class<?> class2 = null;
        Class<?> class3 = null;
        
        // 第一种方式
        class1 = Class.forName("com.tianmaying.ReflectionTest");
        // 取得父类
        Class<?> parentClass = class1.getSuperclass();
        // 获取所有的接口
        Class<?> intes[] = class1.getInterfaces();
        
        // 第二种方式
        class2 = new ReflectionTest().getClass();
        
        // 第三种方式
        class3 = ReflectionTest.class;
    }
}
```
通过反射来创建某类对象
```
public class ReflectionTest {
    public static void main(String[] args) throws Exception {
    
        Class<?> class1 = Class.forName("com.tianmaying.User");

        // 第一种方法，调用Class的newInstance()方法，这会调用类的默认构造方法     
        User user = (User) class1.newInstance();//创建User类的实例
        
        user.setAge(20);//使用这个User类的实例
        
        
        // 第二种方法 先取得全部的构造函数，然后使用构造函数创建对象
        Constructor<?> cons[] = class1.getConstructors();
        
        for (int i = 0; i < cons.length; i++) {
            // 查看每个构造方法需要的参数
            Class<?> clazzs[] = cons[i].getParameterTypes();
            
            // 打印构造函数的签名
            System.out.print("cons[" + i + "] (");
            for (int j = 0; j < clazzs.length; j++) {
                if (j == clazzs.length - 1)
                    System.out.print(clazzs[j].getName());
                else
                    System.out.print(clazzs[j].getName() + ",");
            }
            System.out.println(")");
        }

		// 调用构造函数
        user = (User) cons[0].newInstance("tianmaying");
        System.out.println(user);

		// 调用另一个构造函数
        user = (User) cons[1].newInstance(20, "tianmaying");
        System.out.println(user);
    }
}
```
## 并发编程(查看Concurrent Programming)



## 各种
1.
JDK：包含JRE之外，提供了开发Java应用的各种工具，比如编译器和调试器。
JRE：包括JVM和JAVA核心类库和支持文件，是Java的运行平台，所有的Java程序都要在JRE下才能运行。
JVM：将Java字节码（通过Java程序编译得到）映射到本地的 CPU 的指令集或 OS 的系统调用。

在实际开发过程中，我们首先编写Java代码，然后通过JDK中的编译程序（javac）将Java文件编译成Java字节码，JRE加载和验证Java字节码，JVM解释字节码，映射到CPU指令集或O的系统调用，完成最终的程序功能。

2.
`javac -d name.java`命令用于将Java源文件编译为字节码并保存为.class。一旦编译完成就能在任何Java虚拟机运行。加d可以为该class单独创建目录
`java -cp .:../libs/libq.jar:../libs/lib2.jar com.mycompany.MainClass`命令启动虚拟机，虚拟机则加载和运行class字节码文件，最终在控制台执行
`java -jar name.jar`
`jar cvfe program.jar com.mycompany.MainClass com/mycompany/*.class`打包程序，只打包类库用`cvf`并去掉MainClass

3.变量
变量只能持有对象的引用
尽可能晚地声明变量，例如
```
System.out.println("Who is your name? ")
int age = in.nextInt();
```
常量或final修饰的变量通常大写
常量可以用在多个方法中，其他类中使用加上类名，如`Calendar.DAYS_PER_WEEK`(System.out是例外)
一组相关常量用枚举`enum Weekday { MON, TUE, WED, THU, FRI, SAT, SUN }`声明时`Weekday startDay = Weekday.MON`

4.When a Java program is run, by default it begins executing from the start of main().

5.fully reusable, and object-oriented指对某个常用方法新建一个类，这样任何项目都能通过引用这个类来达到fully reusable。另外最好是static，这样连new都省了

6.可变参数
`public static double max (double first, double... rest){}`
rest既可以是数组，也可以是拆开的多个double

7.final限定的是引用地址不变而非引用内容，如`final ArrayList<Person> friends`中，friends可用add改变其内容

8.包
包不能嵌套：如`java.util`和`java,util.regex`两者并没有关系
*只能导入class
class的冲突：如果两个包(util和sql)都有相同的class(Date)，可以像下面那样声明
```
import java.util.*;
import java.sql.*;
import java.sql.Date;
```
如果确实需要两个相同名称的class，其中一个要用全限定名

9.class也有静态导入`import static ...`默认包不行

10.文档注释
```
//class
/**
*一个<code>Invoice</code>对象代表发货单
*描述
*@author
*@version
**/

//method
/**
*作用
*@param
*@return
**/

//链接
//@see package.class#feature label(如类、方法或者变量的名词，省去部分package或class会使程序在当前包或类中定位)
//@see <a href="网址">label</a>
//@see "文字信息"
```



### supplements
1.
every Java object has the method hashCode()
Whenever you implement equals(), you have to implement hashCode() as well.
it is best to refrain from using hashCode values as keys, if you don't want to lose any information.
Do not assume hashCode will return the same values between executions of applications. 
If you have an object with four fields - age, familyName, givenName, and id - then you can use the function:

```
@Override public int hashCode() {
 	return Objects.hash(age, familyName, givenName, id);
 }
```

2.Dynamically sized hash tables and Consistent Hashing
Java's hash function generates a 32 bit integer - that is there are 232 unique codes that can be generated, however not all hash tables will need 232 entries. 

3.Hash functions differ significantly in their computational complexity. 




