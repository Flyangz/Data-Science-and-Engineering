# 设计模式

[TOC]



## UML类图及时序图

Unified Modeling Language，第三代建模和规约语言。用于说明、可视化、构建和编写一个正在开发的面相对象的、软件密集型系统的制品的开放方法。

**类图**

实箭继承，虚箭实现（可能有棒棒糖表示）

实现关联（成员变量中有其他类），虚线依赖（方法需要其他类作为参数）

空心菱形——聚合，实心菱形——组合

**时序图**：对象之间交互的图，按时间顺序排列

## 设计原则

- 开闭原则

  类、模块和函数应该对扩展开放（新增类），对修改关闭。

  - 即用抽象构建框架，用实现扩展细节。

  优点：提高代码的可复用性和可维护性。

  如interface -> 实现类（实现基本方法） -> 自类（扩展细节）

- 依赖倒置原则

  高层模块不应依赖底层模块，两者都应该依赖其抽象。

  - 即抽象不应该依赖细节，细节应该依赖抽象
  - 针对接口编程（新增不同的类去实现接口）而不是实现编程（在一个类中添加不同的方法，开闭原则中的修改关闭）。

  优点：减少类间耦合性、提高系统稳定性，提高代码可读性和可维护性，降低修改程序所造成的风险。

  如interface course -> implements java course, python course. student(interface course)新增课程就是新增实现interface course的类（开闭原则中的扩展开放），student的代码不需改，因为高层student只依赖底层抽象，是针对接口编程。

  ```java
  Student student = new Student();
  student.setCourse(new JavaCourse());
  student.studyCourse();
  
  student.setCourse(new PythonCourse());
  student.studyCourse();
  ```

- 单一职责原则

  不要存在多于一个导致类变更的原因（职责）。

  - 即一个类/接口/方法只负责一项职责。后两者尽量做到。

  优点：降低类的复杂性、提高类的可读性、提高系统的可维护性、降低变更引起的风险

- 接口隔离

  多个专门的接口，而不使用单一的总接口，客户端不应该依赖它不需要的接口。一个类/接口对一个类/接口的依赖应该建立在最小的接口上。当然，适度。

  优点：符合高内聚低耦合的思想，从而是类具有很好的可读性、可扩展性和可维护性。

  > 单一职责考虑的是实现，接口隔离考虑的是整个框架。

- 迪米特原则（最少知道原则）

  一个对象应该对其他对象保持最少的了解。

  - 适度
  - 如果方法放在本类和外类都可以，那就本类。

  优点：降低耦合。

  > 朋友：出现在成员变量、方法的输入输出参数中的类

- 里氏替换原则

  如果对每一个类型为T1的对象o1，都有类型为T2的对象o2，使得以T1定义的所有程序P在所有的对象o1都替换成o2时，程序P的行为没有发生变化，那么类型T2是类型T1的子类。

  - 扩展就是子类可以扩展父类的功能，但不能改变父类原有的功能。
  - 在重载父类方法时，方法的输入要比父类的输入条件更宽。
  - 子类实现父类方法时（重写/重载/实现抽象）的输出要更严格。
  - 如果父子不符合里氏替换原则，则考虑创建一个共同父类/接口

  优点：约束继承泛滥，加强程序的健壮性。

- 合成/聚合复用原则

  尽量使用对象组合/聚合，而不是继承关系达到复用的目的。

  优点：系统更灵活，降低类间耦合度。

  ```java
  //组合
  public class FeedbackServiceImpl implements IFeedbackService {
  
      private final HbaseTemplate hbaseTemplate;
  
      @Autowired
      public FeedbackServiceImpl(HbaseTemplate hbaseTemplate) {
          this.hbaseTemplate = hbaseTemplate;
      }
  }  
   
  //聚合
  public class Society {
      private List<People> people; //一个家庭里有许多孩子
      // ...
  }
  ```



## 设计模式

### 工厂方法

定义一个创建对象的接口，但让实现这个接口的类来决定实例化哪个类，让类的实例化推迟到子类中进行。

- 补充：与抽象工厂相比，更适合产品等级更新，但产品族也可以，只是没有抽象工厂那样清晰（划分好一个工厂生产一系列产品），应用时可能出现混淆（将不同族的产品组合）。

- 场景
  - 创建对象需要大量的重复代码
  - 客户端/应用层不依赖于产品类实例如何被创建
  - 一个类通过其子类来指定创建哪个对象
- 优缺点
  - 优：符合开闭原则。用户只需关心所需产品对应哪个工厂。
  - 缺：类容易过多。增加了系统的抽象性和理解难度。
- 例子：video 有三个子类，那么就新建一个统一的video工厂，对三种video分别再新建三个继承video工厂的子类。这样需要video时，应用层直接调用对应的工厂即可。



### 抽象工厂

提供一个创建一系列相关或互相依赖对象（非具体类）的接口。

- 补充：这样防止出现非同系列的组合。在扩展产品族时，需要增加新实现工厂，工厂内的产品类，旧的工厂和产品不需要改变。应用层只需依赖所需要的工厂类。但如果产品等级经常更新，就不适合抽象工厂，因为此时工厂接口，每个工厂实现都要更新产品。

- 场景
  - 客户端/应用层不依赖于产品类实例如何被创建
  - 创建“一系列”对象（同一产品族）需要大量的重复代码
  - 提供一个产品类库，所有产品以同样的接口出现，从而使客户端不依赖具体实现
- 优缺点
  - 优：用户无需关心创建细节。将一个系列的产品族统一到一起创建。
  - 缺：规定了所有可能被创建的产品集合，产品族中扩展新的产品困难，需要修改抽象工厂的接口。增加了系统的抽象性和理解难度。

- 例子：一个工厂接口，里面有一个产品族使用产品的生产方法。其子类是不同牌子的工厂，里面具体生产对应的产品类，如A牌子的空调和A牌子的冰箱。当然，A牌子空调和A牌子冰箱都从空调父类和冰箱父类继承而来。另一个牌子的结构一样。应用层需要哪个牌子的产品就调用哪个牌子的工厂即可。



### 建造者

将一个复杂对象的构建与它的表示分离，使得同样的构建过程可以创建不同的表示。

- 场景
  - 对象有非常复杂的内部结构
  - 想把对象创建和使用分离
- 优缺点：
  - 优：用户只需指定需要创造的类型。封装性好，创建和使用分离。扩展性好、创建类之间独立。
  - 缺：产生多余的builder。产品内部发生变化时，建造者都需要修改。

- 例子：内部构造者Computer类，里面有一个builder类。

```java
//从下面方法可知，调用内部类ComputerBuilder中的方法设置内部类的成员变量（内部和外部成员变量重复），然后通过build调用外部类的构造方法，该方法传入ComputerBuilder，然后根据builder中成员变量值设置Computer中的成员变量值。
Computer computer = new Computer.ComputerBuilder().buildCPU("酷睿I7").buildMainBoard("华硕主板").build();

//ComputerBuilder中的build
return new Computer(this)；
```

- 例子：外部构造者ActualBuilder（继承Builder抽象类），computer类，Boss类。
  - ActualBuilder中有成员变量computer
  - Boss中有成员变量builder，有setBuilder和createComputer方法，里面是调用builder的方法设置其成员变量computer的成员变量，并返回builder的createComputer方法。

```java
Builder builder = new ActualBuilder();
Boss boss = new Boss();
directorBoss.setBuilder(builder);
Computer computer = boss.createComputer("酷睿I7","华硕主板");
```





### 单例模式

保证一个类仅有一个实例，并提供一个全局访问点。

- 补充：注意私有构造器，线程安全，延迟加载，序列化，反射。

- 优缺点：
  - 优：减少内存开销。严格控制访问。
  - 缺：没有接口，扩展困难。

```java
//懒汉，饿汉，枚举单例看Concurrent_Programming

//容器单例，非线程安全
public class ContainerSingleton {
    private ContainerSingleton(){...}
    private static Map<String,Object> singletonMap = new HashMap<String,Object>();
    public static void putInstance(String key,Object instance){
        if(StringUtils.isNotBlank(key) && instance != null){
            if(!singletonMap.containsKey(key)){
                singletonMap.put(key,instance);
            }
        }
    }
    public static Object getInstance(String key){
        return singletonMap.get(key);
    }
}

//线程单例
public class ThreadLocalInstance {
    private static final ThreadLocal<ThreadLocalInstance> threadLocalInstanceThreadLocal
             = ThreadLocal.withInitial(() -> new ThreadLocalInstance());
    private ThreadLocalInstance(){}
    public static ThreadLocalInstance getInstance(){
        return threadLocalInstanceThreadLocal.get();
    }
}
```



### 原型模式

指定创建对象的种类，通过拷贝这些原型创建新的对象。（不调用构造函数）

- 场景：
  - 类初始化消耗较多资源
  - new产生的一个对象需要非常繁琐的过程（数据准备、访问权限等）
  - 构造函数比较复杂
  - 循环体中产生大量对象
- 优缺点：
  - 优：比直接new对象性能高。简化创建过程
  - 缺：必须配备克隆方法，所以对克隆复杂对象或对克隆出的对象进行复杂改造时，有风险。

```java
//下面例子中，如果saveOriginMailRecord想保存new生产的mail，且不在循环里面new对象（多次循环耗性能），可考虑克隆。克隆要让Mail类实现cloneable接口，默认浅克隆，最好用深克隆。
public static void main(String[] args) throws CloneNotSupportedException {
    Mail mail = new Mail();
    mail.setContent("初始化模板");
    for(int i = 0;i < 10;i++){
        Mail mailTemp = (Mail) mail.clone();
        mailTemp.setName("姓名"+i);
        mailTemp.setContent("xxx");
        MailUtil.sendMail(mailTemp);
    }
    MailUtil.saveOriginMailRecord(mail);
}
//深克隆，将里面的field也克隆
@Override
protected Object clone() throws CloneNotSupportedException {
    Mail mail = (Mail)super.clone();
    mail.name = mail.name.clone();
    mail.content = mail.content.clone();
    return mail;
}
```



### 外观模式

提供一个统一的接口，用来访问子系统中的一群接口。（定义一个高阶接口，让子系统更容易使用）

- 场景：
  - 子系统复杂，需要多层结构简化。
- 优缺点：
  - 优：简化过程，无需深入了解子系统。减少系统依赖。更好划分访问层次
  - 缺：增加/扩展子系统容易引入风险。不符合开闭原则。

> 外观模式是外界和子系统；中介者是子系统之间。
>
> 外观模式的外观对象做成单例模式。
>
> 外观模式通过抽象工厂获取子系统实例。

- 例子：Computer、CPU和Disk类
  - Computer类中的成员变量有CPU和Disk类。有一个open和close方法，里面分别调用成员变量的方法。



### 装饰者模式

不改变原有对象的基础上，将功能附加到对象上。

- 补充：提供了比继承更有弹性的替代方案（扩展原有对象功能）
- 场景：
  - 扩展一个类的功能/职责
  - 动态添加和取消功能
- 优缺点：
  - 优：继承的补充，比继承灵活，在不改变原对象的情况下给一个对象扩展功能。装饰类多层组合。符合开闭原则。
  - 缺：更多代码，特别动态装饰和多层装饰时更复杂。

> 装饰者关注动态添加方法，代理者关注控制访问权限。
>
> 装饰者可以和被装饰者实现相同接口，或者前者是后者的子类。适配器和被适配器可能有不同的接口。

- 例子：一个煎饼抽象类，其子类有煎饼实现类和煎饼装饰类，煎饼装饰类的子类有加香肠装饰类和加鸡蛋装饰类。

  - 煎饼装饰类：成员变量有煎饼实现类，其继承的方法就改成调用这个实现类的方法。这个类是否抽象，看下面两个装饰类子类是否都要特定不一样的操作。
  - 加香肠装饰类：构造器取父类的煎饼实现类。实现继承方法时，调用父类方法并在此基础上增加功能。
  - 加鸡蛋装饰类：同上。

  ```java
  //最终aBattercake的功能就是煎饼实现类 + 两个加鸡蛋装饰类 + 一个加香肠装饰类，即4个功能的组合。
  ABattercake aBattercake = new Battercake();
  aBattercake = new EggDecorator(aBattercake);
  aBattercake = new EggDecorator(aBattercake);
  aBattercake = new SausageDecorator(aBattercake);
  ```



### 适配器模式

将一个类的接口转换成客户期望的另一个接口，从而使原来不能兼容的类可以一起工作。

- 场景：
  - 已存在的类的方法和需求不匹配时（方法结果相同或相似）
  - 不是设计时考虑，而是维护时考虑。
- 优缺点：
  - 优：提高透明性和复用。目标类和适配器类解耦，提高扩展性。
  - 缺：增加复杂性。

> 适配器复用原有接口，添加一个协同工作的接口。外观模式也是新接口，但和原接口没太大关联。

- 例子：有一个220V交流电类AC220。一个变压器接口（也可以是类，接口优先）。一个变压器接口实现类。
  - AC220：有一个outputAC220V方法。
  - 变压器接口：一个抽象方法outputDC5V
  - 变压器接口实现类：成员变量是AC220（也可以是实现AC220的接口），通过outputDC5V把AC220的outputAC220V方法的输出进行改造，如除以一个值，从而降低电压输出。



### 享元模式

提供了减少对象数量从而改善应用所需的对象结构。即运用共享技术有效地支持大量细粒度的对象。

- 场景：

  - 系统底层开发，需要大量相似对象、缓冲池。

- 优缺点：

  - 优：减少对象创建，省内存。
  - 缺：关注内外部状态和线程安全问题。复杂化。

- 例子：Employee接口，其子类Manager。EmployeeFactory类

  - Employee有report方法
  - Manager有department和reportContent两个私有变量（外部状态，通过外部传入值来设置。如果是固定内容就是内部状态），有setReportContent和Manager(String department)的构造方法
  - EmployeeFactory有HashMap存Manager，有getManager(String department)方法，没有就new并put到HashMap中。

  - 这个Manager的HashMap就存放了不同的Manager，需要的时候取出而不是new。



### 组合模式

将对象组合成树形结构以表示“部分-整体”的层次结构，从而使客户端对单个对象和组合对象保持一致的方式处理。

- 场景：
  - 希望客户端忽略组合对象与单个对象的差异。
  - 处理一个树形结构
- 优缺点：
  - 优：将对象分层。让客户端忽略层次差异。
  - 缺：限制类型时比较复杂。设计变得更抽象。
- 例子：抽象类CatalogComponent，子类Course和CourseCatalog。
  - CatalogComponent定义了子类需要的大部分方法，不一定子类都需要，可能一些子类需要一些不需要，需要的重写方法，不需要的保留默认实现，比如抛出异常，说明没有这个方法的实现。



### 桥接模式

将抽象部分与它的具体部分实现部分分离，使它们都可以独立地变化。通过组合的方式建立两个类之间的联系。

- 场景：
  - 抽象和具体实现之间增加更多灵活性
  - 一个类存在两个或以上独立变化的维度，且这些维度都需要独立进行扩展
  - 不希望使用继承或因为多层继承导致系统类的个数剧增
- 优缺点：
  - 优：分离抽象及其具体部分。提高可扩展性。
  - 缺：增加难度，需要识别出系统两个独立变化维度。
- 例子：有两个体系：实现体系Account，子类DepositAccount和SavingAccount；抽象体系Bank，子类ABCBank和ICBCBank。
  - Bank中有成员变量Account，构造方法要传入这个Account
  - ABCBank和ICBCBank的方法实现要调用Account的方法。
  - 这样，两个系统扩展就很方便。因为一个系统时，如果增加一个Account子类，每个Bank子类都要改。



### 代理模式

为其他对象提供一种代理，来控制对这个对象的访问。代理对象在客户端和目标对象之间起到中介作用。

- 场景：
  - 保护目标对象
  - 增强目标对象
- 优缺点：
  - 优：将代理对象和真实被调用的目标对象分离。一定程度降低了耦合度。增强目标对象。
  - 缺：类数目增加。请求速度变慢。复杂。
- 例子：web应用的controller



### 模版方法模式

定义一个算法骨架，并允许子类为一个或多个步骤提供实现，从而使子类可以在不改变算法结构的情况下，重新定义算法的某些步骤。

- 场景：
  - 一次性实现一个算法的不变部分，将可变的留给子类。
  - 各子类中公共的行为被提取出来并集中到一个公共父类中，从而避免代码重复。
- 优缺点：
  - 优：复用，扩展。
  - 缺：类数目增加，增加系统实现复杂度。继承自身缺点，如果父类添加新的抽象方法，所有子类都要改。
- 例子：模版类ACourse，子类DesignPatternCourse和FECourse
  - ACourse有一个makeCourse方法，规定了其他方法的执行顺序或条件。
  - 子类重写部分父类方法。子类可以添加成员变量，作为构造方法的参数，从而让应用层决定子类的一些行为（父类中方法执行条件以这些成员变量的值作为标准）。



### 迭代器模式

提供一种方法，顺序访问一个集合对象中的各个元素，而又不暴露该对象的内部表示。

- 场景：
  - 访问一个集合对象的内容而无需暴露它的内部表示。
  - 为遍历不同的集合结构提供一个统一的接口
- 优缺点：
  - 优：分离了集合对象的遍历行为。
  - 缺：类数目增加。