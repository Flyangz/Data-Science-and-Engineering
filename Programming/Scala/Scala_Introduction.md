# Scala Introduction
## IDEA
1. shortcuts for mac  
    ^Space 补全  
    optionF7 找引用  
    F1 查documentation  
    command+B 查declaration  
    command+F12现实members of the current class  
    shiftF+6改class，方法和变量名  
    ^O override methods  
    ^I implement methods  
    ^+shift+Space    
    optionF1快速找一个project的某个位置  
    shift+esc隐藏窗口并回到editor  
    option+command+T catch exception的code   fragment  
    new之后用^+shift+Space  
    option+command+B navigatie to the   implementation?  
    option+command+V选择加重构？  
    shift+F1打开网址（要设定file | settings | web browsers）  
    command+P 在call method的括号里  
2. 压缩  
    maven自动编译如果关闭，打包时就只编译Java，所以需要加maven插件



可以选择一段代码，refactor -> extract -> method来创建方法

## scala
### 基本框架
```scala
class Teat1{
    def hello(){ ... }  
}
object Test{//名字相同的话会变成伴生对象，后面会介绍
    
    //Scala没有static修饰词，而object单例类（相当于静态类）能代为实现static功能，所以main方法出现在object类中。
    def main(args: Array[String]): Unit = {
        val str = "aaa"
        printin(str)
        Teat1.hello() //不需实例化，直接用
    }
}
```

> 由于相当于静态类，object中定义的方法在使用时就是“类名.方法名”

```
val a = 123 //“引用”不可变，如果是array，里面的元素可变（引入包的情况下）， var可变
val a: Int = 123//也可
```



### 数据类型（无引用类型）

Byte, Char, Short, Int, Long, Float, Double, Boolean等  
其他Unit, Null, Nothing, Any, AnyRef  
AnyVal相当于java的object  
Unit相当于void  

### 判断，循环，方法和函数
```scala
//判断
val y = if(x>1) 1 else -1
val z = if(x>1) 1 else "error"

//循环
1 to 10 //1.to(10)
1 until 10 //1到9
for(i<-1 to 10){...}
for(i<-arr) println(i)
for(i<-arr.reverse) println(i)
for(i<-1 to 3; j <- 1 to 3 if (i !=j)) println(i*10+j)
val res = for(i <- 1 until 10) yield i //yield生成vector(....)
//跳出循环的三种方式，boolean, 函数中的return, break
while (flag){
    ....
    if (condition) flag = false
}

def test() : Int = {    
    var i = 0
    while (true) {
        i += 1
        if (i == 5) {return i}
    }
}

import scala.util.control.Breaks._
var i = 0
breakable{
    while (true) {
        i += 1
        if (i == 5) break
    }
}

//方法
def m1(x: Int, y:Int): Int = x + y
m1(3,4)
def m1(x:Int) = x*x //也可以
//函数
val f1 =(x: Int, y:Int) => x+y
f1(2,3)
val f1:

//方法用函数作参数
def m2(f:(Int, Int)=> Int) =f(2,4)
val f1 =(x:Int,y:Int) => x+y
m2(f1)//结果为7

//方法转换为函数
val f1 = m1 _
```


### 数组
```scala
定长
val arr1 = new Array[Int](8) //调用静态类
println(arr1.toBurrer)
val arr2 = Array("java","scala","c#")
println(arr2(0))
Array.fill(4)(0)
Array.ofDim[Int](3,4)//二维空数组，(1)(1)取值
val multiDimArr = new Array[Array[Int]](3)//同上n维数组，但可自定义里面一层每个数组的长度

变长
import scala.collection.mutable.ArrayBuffer
val arr3 = ArrayBuffer[Int]()
arr3 +=(2,3,4)//得（2，3，4）
arr3.insert(0,-1,0)//结果为(-1,0,2,3,4)
arr3.remove(2,2)//在index=2删两个

方法
sorted, reserve, min, max, sum, head, last, tail, count(p: (T) ⇒ Boolean): Int, exists, filter, filterNot, take, drop, takeWhile(p: (T) ⇒ Boolean): ArrayBuffer取subArray，直到不符合，后面符合的不管, dropWhile, contains, startWith(that: GenSeq[B], offset: Int): Boolean, indexOf
例子：
count(_ % 2 == 0)//返回符合条件的元素数量

zip
arr1.zip(arr2).toMap//新arr，每个pair为Tuple2，可转为Map
```
### 集合
序列list
```scala
//不可变
val list1 = List(1,2,3)
val list2 = 0 +: list1//结果在list1前面加个0，也可::
val list3 = list1.::(0)//结果和上面一样 
val list4 = list :+ 4//在后面加4
val list5 = list1 ++ list2//合并两个，也可++: or :::
//可变
import
val list1 =ListBuffer(6,7,8)
list1 ++= list2//可变后，list1重新引用++生成的新值
list5.append(5)
```
> - `::` 头部追加数据 `x::list` （`x`为加入到头部的元素，无论`x`是列表与否，它都只将成为新生成列表的第一个元素）`等价于`list.::(x)`。该符号还可用于配型匹配
> - `:+`和`+:` 追加，冒号靠近集合类型
> - `:::` 两个List类型
> - `++` 两个集合list1++list2

映射map

```scala
map("scala"->1)
map((scala, 1))
#要引入包后重新创建才能改里面的值
map.getOrRlse("c#",-1)//最好用这种方式取值，直接用map("c#")会出一大串错误

//数组转换为map
arr.toMap

val map1 = new HashMap[String, Int]()
map1("scala")=1
map1 +=(("java",2))
map1.put ("c++",5)
```
元组
```scala
//不可变
val t = ("scala", 3,14, ("spark",1))//index从1开始
t._1
t._4._1
val t,(a,b,c) = ("scala", 3,14, ("spark",1))

//HashSet要import
import
val set1 = new HashSet[Int]()
val set3 = set2 ++ Set(2,3,4)
//可变
import
set += 1

方法
set.remove()或-=
```
### 练习
1.基础
```scala
//创建list
val list0 = List (1,2,3,4,5)
//将list0中每个元素乘以2，并创建新list
val list1 = list0.map(_ *2)//下划线df示逐个地取出list0里所有元素
//将list0中的偶数取出来生成新集合
val list2 = list0.filter(_ % 2 ==0)
//将list0排序后反转生成新集合
val list3 = list0.sorted.reverse//没有参数不需加()
//将list0中元素4个一组，生成迭代
val it = list0.grouped(4)// println(it.toBuffer)查看
//迭代器转为list
val list4 = it.toList//得到List(List(),List()..)
//压扁
val list5 = list4.flatten
//先按空格切分再压扁
val lines = ("hello world","..","..")
val worlds = lines.map(_.split(" "))//得出三个不知道是什么的object
val flatWords = words.flatten
//或
val flatWords2 = lines.flatMap(_.split(" "))
//并行计算求和
val arr=Array(1,2,3,4)
val res = arr.par.sum//进程分配随机
//特定顺序进行聚合
val res = arr.reduce(_+_)//相当于reduceLeft
val res = arr.par.reduce(_+_)//注意这种情况不能用减法
//折叠
val res = arr.fold(0)(_+_)//在fold这个初始值上聚合；实现了柯里化
val res = arr.foldLeft(10)(_+_)
val res = List(1,2,3,4).foldRight(10)(_-_) //结果是8，1-(2-(3-(4-10)))，相当于右边的_作为累加器
//聚合
val list6 = List(List(),List()..)
val res = list6.flatten.reduce(_+_)
val res = list6.aggregate(0)(_+_.sum, _+_)//0代表每个循环的初始值，第一个_为初始值，第二个为第一个内部List，第二个_+_为不同内部List的求和
//集
val l1=List(1,2,3)
val l2=List(1,2,3)
val res =l1 union l2 //还有diff, intersect，结果为List
```
2.wordcount
```
object WordCount{
    def main(args: Array[String]): Unit = {
        val lines = List("hello world","hello scala","")
        //压平
        val words = lines.flatMap(_.split(" "))
        //生成键对
        val pair = words.map((_,1))//每个string变为对，仍未List
        //分组
        val grouped = pair.groupBy(_._1)//注意这里_1取得1（元组从1开始），然后变为Map，如scala映射List（(scala,1),(scala,1),(scala,1)）。另外，groupByKey后是key和value分开的，即(key,(1,1,1,1))。groupBy缺是保留的
        //统计
        val total = grouped.mapValues(_.size)//对value进行操作，_为List（(scala,1),(scala,1),(scala,1)），求其size。最终结果还是Map
        //排序
        val sorted = total.toList.sortBy(_._2)
    }
}
```

### class
```scala
//伴生对象用object修饰，且名字要跟class名字一样
//一般object修饰为静态类
class Person {
    val id: String = "100" //只有get方法
    var name: String = _ //有get有set，会获取null
    private var age: Int = _ //伴生对象也访问，会获取0
    private[this] val gender = "男" //伴生也不能访问
}

object Person{
    def main(args: Array[String]): Unit = {
        val p = new Person
        
}

object Test{
}
```

**说明：**

1.Scala把类中的static集中放到伴生object对象中,伴生对象和类文件必须是同一个源文件,可以用伴生对象做一些初始化操作. 

2.object不能提供构造器参数,也就是说object必须是无参的



```
//括号内为构造器,默认为val。faceValue的值伴生对象也不能访问
class StructDemo(val name: String, var age: Int, faceValue: Int = 90){
    
    val gender: String = _
    def getFaceValue(): Int = {
        FaceValue//最后一句代码的值作为返回
    }
    //辅助构造器
    def this(name: String, age: Int, faceValue: Int, gender: String){
    this(name, age, faceValue)
    this.gender = gender
}


object StructDemo{
    def main(args: Array[String]): Unit = {
        val s = new StrucDemo("abc", 26, 98)
    println(s.getFaceValue)//这样才能访问FaceValue
    }
}
```
```
//object修饰的类实现单例模式，用作工具类，存放常量，工具方法
//单例模式，就是该类最多只存在一个实例。在多线程下，有三种策略（加锁，预先创建和双重加锁）
object StructDemo{
    def main(args: Array[String]): Unit = {
    
    val factory = SessionFactory
    println(factory.getSessions)
    println(factory.getSessions.size)
    println(factory.getSessions(0))
    println(factory.removeSession)
    //结果为：
    //SessionFactory被执行
    //一个ArrayBuffer, 里面有五个Session类
    //5
    //第一个Session
    //...
    //（）来源于Unit
    }
}
object SessionFactory{
    println("SessionFactory被执行")
    var i = 5
    private val session = new ArrayBuffer[Session]()
    while(i>0){
        session += new Session
        i -= 1
    }
    
    def getSessions = session
    def removeSession: Unit = {
        val s = session(0)
        session.remove(0)
        println("...")
        
}
class Session{}
```
```
//类和其半生对象可以互相访问私有变量。list =List(1,2,3,4])就调用了半生对象，都没有用new
class Dog{
    private var name = "..."
    def printName():Unit ={
        println(Dog.CONSTANT + name)
    }
}

object Dog{
    private val CONSTANT = "..."
    def main(args: Array[String]): Unit = {
    val p = naw Dog
    println(p.name)
}
```
```
class ApplyDemo(val name: String, var age: Int, var faceValue: Int){

}
object ApplyDemo{
    def apply(name: String, age: Int, faceValue: Int): ApplyDemo = new ApplyDemo(name, age, faceValue)//注入方法，在半生对象里用于初始化；其参数不需要和构造器的统一
    def unaply(applyDemo: ApplyDemo):Option[(String, Int, Int)] = {
        if (applyDemo == null){
            None
        }else{
            Some(applyDemo.name, applyDemo.age, applyDemo.faceValue)//对应上面的option
        }
    }//提取方法，用于提取固定数量的对象，返回一个序列（Option），内部用Some来存放
}
object Test{
    def main(args: Array[String]): Unit = {
    val applyDemo = ApplyDemo("...", 24, 90) //不需要用new，直接用类名就会引用半生对象，加参数就引用apply方法
    applyDemo match {
        case ApplyDemo("...", age, faceValue) => println(s"age: $age")//非赋值的情况下，就会用unapply
        case _ => println("no match")
    }
}
```
```
//表明哪个包有访问权限
//第二个private表明只有伴生可访问，在其他类使用s.faceValue可以通过IDE的检查，但是执行时并不能执行
private [package_name]class private PrivateDemo(val gender: Int, var faceValue: Int){
    private val name = "..."//私有字段
    private[this] var age = 23//对象私有字段
    private def sayHello(): Unit = {
        println(s".. $age")
    }
}
```
```scala
object ClassDemo{
    def main(args: Array[String]): Unit = {
    }
}
//特质
trait Flyale{
    //声明一个没有值的字段
    val distance: Int
    //没有实现的方法
    def fight: String
    //实现了的方法
    def fly: Unit = {
        println()
    }
}
//抽象类
abstract class Animal{
    //声明没有赋值的字段
    val name:String
    //没有实现的方法
    def run(): String
    //有实现的
    def climb: String ={
        "..."
    }
}
//如果只实现特质，直接用extends
class Human extends Animal with Flyable{
    //补充没有赋值的变量
    //补充抽象类没有实现的方法，当然已经实现了的也可以
    //实现上面特质没有实现的方法
}
```
内部类：下面每个Class的实例都有一个对应的Student类。如果c1和c2都是Class的实例，leo是c2通过register创建的Student，则c1.studentList += leo会出错。如果把内部类放到伴生对象中，或者运用类型投影，则上述行为可以实现。

```scala
class Class{ out => //加上这个，Student就可以调用其自身定义的方法
    class Student(name : String){}
    val studentList = new ArrayBuffer[Student]//当class Student被放到伴生对象中时，这里Student要改为Class.Student；类型投影改为Class#Student
    def register(name : String) : Student = {new Student(name)}
}
```



匹配

```
object MatchStr{
    def main(args: Array[String]): Unit = {
        val arr = Array("..", "jfkdjfk", "dfjdk")
        val name = arr(Random.nextInt(arr.length))
        println(name)
        name match{
            case ".." => println("点点")
            case "jfkdjfk" => println("乱语")
             case _ => println("Nothing")
            
        }
    } 
```
```
object MatchType{
    def main(args: Array[String]): Unit = {
        val arr = Array("..", 100, True, MatchType)
        val element = arr(Random.nextInt(arr.length))
        println(element)
        element match{
            case str:String => println("点点")
            case int:Int => println("乱语")
            case _: Any => println("Nothing")
            
        }
    } 
class MatchTest{
}
```
```
object MatchArr_Set_List{
    def main(args: Array[String]): Unit = {
        //数组
        val arr = Array(1,2,3,4)
        arr match{
            case Array(1,a,b,c) => println("点点")
            case Array(_,x,y) => println("乱语")
            case _ => println("Nothing")
            
        }//a,b,c和_一样？
        //元组
        val tup = (1,2,3,4)
        tup match{
            case (1,a,b,c) => println("点点")
            case (_,x,y) => println("乱语")
            case _: Any => println("Nothing")
        }
        //序列
        val list1 = List(1,2,3,4)
        tup match{
            case List(1,2,3,a) => println("case1") 
            case 0::Nil => println("点点") //Nil为空List
            case a::b::c::d::Nil => println("乱语")//::表示从右向左合并集合，第一个默认为List
            case _: Any => println("Nothing")
class MatchTest{
}
```
```scala
object MatchClass{
    def main(args: Array[String]): Unit = {
        val arr = Array(CheckTimeOoutTask, SubmiTask("sdwg", "dfeg"), HearBeat(10), MatchType)
        val arr(Random.nextInt(arr.length)) match{
            case HearBeat(time) => println("点点")
            case SubmiTask(sbg, task) => println("乱语")
            case CheckTimeOoutTask => println("Nothing")            
        }
    } 
case class HearBeat(time:Long)//样例类，默认有getter, setter, apply 和 unapply
case class SubmiTask(id:String, taskName:string)
case objec CheckTimeOoutTask //单例类
```
> 添加case能够给class或object带来下面变化
>
> 1. pattern matching support
> 2. default implementations of `equals` and `hashCode`
> 3. default implementations of serialization
> 4. a prettier default implementation of `toString`, and 
> 5. the small amount of functionality that they get from automatically inheriting from `scala.Product`.
>
> 另外，对于case class，实例化不用new。但对于case objec，我们一般只用到它的serialization和toString特征。

偏函数

```scala
//m1等价于m2
//String为参数类型，Int为输出类型
//常用做输入模式匹配
//def可以改为val
def m1: PartialFunction[String, Int] = {
    case "one" => 1
    case "two" => 2
    case _ => 0
}

def m2 (num:String): Int = num match {
    case "one" => 1
    case "two" => 2
    case _ => 0
}

m1.isDefinedAt("string")//判断string是否符合其中一个case
m1("one")//返回1。实际上是调用了apply方法，该方法的调用就是直接名称加参数name(arg)。这也是为什么Scala创建对象不需要new的原因
```
### 高阶函数
```
arr.map(func) //相当于python的lambda函数
```
### 柯里化
```
def currying(x:Int)(y:Int)=x*y//也可以def curry(x: Int) = (y:Int) => x*y
currying(3)(4) //12
val curry = currying(3) //先给一个值
curry(4) //12
def m2(x:Int)(implicity y:Int=5)=x*y
m2(4) //20
m2(4)(3) //12
implicit val x = 100
m2(5) //500
implicit val y = 100
m2(5) //出错，因有多个implicit
val arr = Array(("scala", 1),("scala",2))
arr.foldLeft(0)(_+_._2) //6

//练习
object Context{ //这个放在CurryingDemo下面会出错。不过这段一般放到其他页面里
    implicit val a = "java"
    implicit val b = "python"
}

object CurryingDemo{
    def m1(str:String)(implicit name:String = "scala")={
        println(str + name)
    }
    
    def main(args: Array[String]): Unit = {
        //val func = m1("Hi") 这里会出错，m1没有返回值，但这里却用了=，除非上面去掉println
        import Context.a //这样implicit才不会混淆
        m1("Hi")
    }
}
```
### 隐式
什么情况出现隐式：方法中传入的参数类型不匹配；对象引用的方法在其本身的类中不存在；类调用其自身方法，但导入的类型不匹配 。这些情况下，程序就会去搜索implicit的函数，看有没有把不匹配的类型转化成匹配类型。
Scala默认搜索1.源类型及其伴生对象内2.当前程序作用域找隐式函数。否则要用import，建议在需要用的时候才import，比如在某个函数或方法内，减少转化对其他代码的影响
```scala
//隐式转换
implicit def dog2person(obj:Object): Person = if (obj.isInstanceOf[Dog]){
    val dog = obj.asInstanceOf[Dog]; new Person(dog.name)) else Nil
//加强现有类型，下面规定只转换Man。这样，即便Man没有Supermen的方法，man对象依然能用Supermen的方法
implicit def man2superman(man:Man): Supermen = new Superman(man.name)
//导入import隐式转换

//隐式参数
class SignPen{ 
    def write(content:String) = println(content)
}
implicit val = signPen = new SignPen
def signForExam(name:String)(implicit signPen:SignPen){
    signPen.write(name+"...")
}
signForExam("leo") //这样就已经可以执行SignPen的write了
    

object MyPredef{ //day1package里另一个class MyPredef
    implicit def fileToRichFile(file:String) = new RichFile(file)
}

class RichFile(val file: String){
    def read():String = {
        Source.fromFile(file).mkString("")
    }
}
object RichFile{
    def main(args: Array[String]): Unit = {
        //显式
        val file = "path"
        val content: String = new RichFile(file).read()
        println(content)
        //隐式
        import day1.MyPredef.fileToRichFile
        val file = "path"
        val content = file.read()
        println(content)
    }
}
    
//导入Java和Scala的隐式转换
import scala.collection.JavaConversions.asScalaBuffer//可将Java类方法生成的List变为Buffer
import scala.collection.JavaConversions.bufferAsJavaList//可以将ArrayBuffer传入Java类的方法中（变为List）进行使用
```
练习
```
object ImplicitContext{
    implicit object OderingGirl extends Ordering[Girl]{
        override def compare(x:Girl,y:Girl): Int = if (x.faceValue > y.faceValue) 1 else -1
    }
}

class Girl(val name: String, var faceValue: Int){
    override def toString: String = s"$name, $faceValue"
}
class Goddess[T:Odering](val v1: T, val v2:T){
    def choose()(implicit ord: Ordering[T]) = if (ord.gt(v1,v2)) v1 else v2
}

object Goddess{
    def main(args: Array[String]): Unit = {
    import ImplicitContext.OderingGirl
    val g1 = new Girl("",90)
    val g2 = new Girl("",80)
    val goddess = new Goddess(g1,g2)
    val res = goddess.choose()
    println(res)
```

### 泛型
基础
```
//泛型类
class Student[T](val localId:T){
    def getSchoolId(hukouId:T) = "S-" + hukouId + "-" + localId
}
val marry = new Student[Int](1) //scala会根据传入参数自动判断类型，此处不加[Int]也行
//泛型函数
def getCard[T](content:T) ={
    if(conent.isInstanceOf[Int]) ""
    else if(conent.isInstanceOf[Int]) ""
    else ""
}
```
`[B <: A]`上界，A为父类  
`[B <% A]`B类型要转换成A类，B可以是A类、其子类或者通过隐式类型转换的类型  
```
class Person(val name:String){
    def makeFriends(p:Person){}
}

class Student(name:String) extends Person(name) //有了这个Student才能进入Party（进入的必须为Person的子类）
class Party[T <: Person](p1: T, p2: T){
    def play = p1.makeFriends(p2) //有了上界是这里makeFriends成为可能
}

class Dog(val name: String){}

//隐式类型转换
implicit def dog2person(obj:Object): Person = if (obj.isInstanceOf[Dog]){
    val dog = obj.asInstanceOf[Dog]; new Person(dog.name)) else Nil
```
`[B : A]` context bound需要一个隐式转换的值?  
```
class Calculator[T:Ordering](val number1: T, val number2: T){
    def max(implicit order: Ordering[T]) = if (order.compare(number1,number2)>0) number1 else number2
}
val cal = new Calculator(1,2)
cal.max

//Manifest ContextBounds
//如果生成两个类，meat和vegetable，他们都能放到下面的方法里面
def packageFood[T:Manifest](foods:T*) = { //可以放多个参数
    val foodPackage = new Array[T](foods.length)
        for (i <- 0 until foods.length) foodPackage(i) = food(i)
        foodPackage
}

```
`[-A]`逆变，作为参数类型，如果A是T的子类，那么`C[T]`是`C[A]`的子类  
`[+B]`协变，作为返回类型，如果B是T的子类，那么`C[B]`是`C[T]`的子类  
```
class Master
class Professional extends Master
class Card[+T](val name:String)//这样下面的a和b都可以用enterMeet，因为Professional父类为Master，Card[+T]使得传入的子类协变为父类，所以Card[父类]才能用的方法，Card[子类]也能用
def enterMeet(card: Card[Master]){}
val a = new Card[Master]("a")
val b = new Card[Professional]("b")
```

```
class UpperBoundDemo[T <: Comparable[T]{
    def select(first: T, second: T): T= {
        if (first.compareTo(second) > 0) first else second
    }
}

object UpperBounDemo{
    def main(args: Array[String]): Unit = {
        val u = new UpperBoundDemo[MissRight]
        
        val m1 = new MissRight("",123)
        val m2 = new MissRight("",125)
        
        val res = u.select(m1,m2)
        println(res.name)
    }
}

class MissRight(val name: String, val faceValue: Int) extends Comparable[MissRight]{
    override def compareTo(o:MissRight):Int = {
        this.faceValue - o.faceValue
    }
}
```
**重写field**

```scala
//下面代码中，创建的PEStudent的classScores会是Array[Int]()。原因是自类的默认构造函数会先调用夫类的默认构造函数，但由于classNumber将要被重写，这可能使得getter方法取出了null值，这时classScores的创建结果就是Array[Int]()
class Student {
    val classNumber: Int = 10
    val classScores: Array[Int] new Array[Int](classNumber)
}

class PEStudent extends Student {
    override val classNumber: Int = 3
}
//下面这种方式是在调用夫类构造函数之前就已经重写好classNumber，所以结果会是Array[Int](3)
class PEStudent {
    override val classNumber: Int = 3
} extends Student
```

**Scala继承层级**

顶端是两个trait，Nothing（Any类继承）和Null（null对象）

Any增加了isInstanceOf, equals, hashCode等方法

Anyval和AnyRef都继承了Any，前者是所有值类的基类，后者是所有引用类的基类增加了wait, notify, synchronized等方法。

对象相等行：重写equals方法，一般先asInstanceOf[class]，判断是否为null，然后比较该对象的成员变量是否相等。最后要重写hashCode方法，一般是各变量的.hashCode相加。

### I/O

```scala
//读
import scala.io.Source
val source = Source.fromFile("path", "encode_format")//source本身就已经是iterator了，遍历它会得到一个个字符。也有fromURL, fromString
val lineIterator = source.getLines //getLines一旦调用，source的内容都被读取完了了，再调用会是empty-iterator。要重新调用，则再执行一次fromFile。也有mkString
source.close()

//写，可调用Java的PrintWriter
out = new PrintWriter(path)
out.println(content)
```



### 补充

package特性：

```scala
//一个class可以包含一个或多个包；一个包可以在不同的class中

//子包中的类可以访问夫包中的类，不需import

//绝对和相对包名
_root_.scala.collection....//绝对，平常用的都是相对，从最近的包去找，一旦有重名，就可能找不到实际想找的那个包

//可见性
package com.ibeifeng.scala
private[scala] def ... //表明在scala层可见
```

Actor

```scala
class HelloAcator extends Actor{
    def act(){
        while (true){
            receive{
                case name: String -> println...
            }    
        }
    }
}
val helloActor = new HelloActor
helloActor.start()
helloActor ! "leo" //发消息给Actor

val reply = actor !? message //同步，发送后马上得到返回值
val reply = future //手动获得返回值
```

lazy：用lazy定义的变量，只有在被调用时才创建

```scala
class Teat{
}
object Test1{
    def init(): Unit ={
        println("...")
    }
    
    def main(args: Array[String]): Unit = {
        val str = init()
        println("str")
        println(str)
    }
}
#结果为
...
str
()
```

遍历子目录

```scala
def getSubdirIterator(dir: File) : Iterator[File] = {
    val childDirs = dir.listFiles.filter(_.isDirectory)
    childDirs.toIterator ++ chidDirs.toIterator.flatMap(getSubdirIterator(_))
}

val iterator = getSubdirIterator(new File(path))
```

序列化和反序列化

```scala
@SeridalVersionUID(42L) class Person(val name : String) extends Serializable
val leo = new Person("leo")
//下面ObjectOutputStream的writeObject可以将类以序列化的形式写入文件
val oos = new ObjectOutputStream(new FileOutputStream(path))
oos.writeObject(leo)
oos.close
//下面是反序列化
val ois = new ObjectInputStream(new FileInputStream(path))
ois.readObject().asInstanceOf[Person]
restoredLeo.name
```

运行terminal命令

```scala
import sys.process._
"ls -l"!
"javac HelloWorld.java"!
"java HelloWorld"!
```

正则表达

```scala
val string = "xxxx"
val pattern = "[a-z]+".r//返回scala.util.matching.Regex
pattern.findAllIn(string)//返回迭代器。还有findFirstIn()第一个，直到不符合，replaceFirstIn(string, "replacement")
```

提取器

```scala
class Person(name: String, age: Int)
object Person{
    def apply(name: String, age: Int) = new Person(name, age)
    def unapply(str: String) = {
        val splitIndex = str.indexOf(" ")
        if (splitIndex == -1) None //返回结果只能一个？
        else Some((str.substring(0, splitIndex), str.substring(splitIndex +1)))
    }
}

object Person{
    def apply(name: String, age: Int) = new Person(name, age)
    def unapply(str: String) = Some(str)
}
Person("string", int)//创建一个object
val Person(n, a) = "k 2"//返回n: String = kaka 和 a: String = 2
```

注解

```scala
(scores.get("Leo"): @unchecked) match {case score => println(score)}//不检查类型是否匹配，直接得到什么打印什么
//如果要在主构造函数前加注解，需要加上一个圆括号

//开发注解
class Test extends annotation.Annotation
@Test//可以用了

//常用注解
@volatile var name = "leo" //不是百分百安全，线程在获取共享变量时，强制刷新该变量的值，让该值是最新的状态
@transient var name = "leo" //瞬态字段，不会序列化该字段
@SerialVersionUID(value) //标记类的序列化版本号，如果某类改变了，就要重新生成一个版本号，否则原先类的反序列化时会报错
@native //标记用c语言实现的本地方法
@throws(classOf[Exception]) def test(){} //相当于Java的throws
@varargs def test(args: String*) //表示方法接收变长参数
@BeanProperty //给JavaBean风格的类生成getter和setter方法
@BooleanBeanProperty //生成返回boolean的getter方法
```

XML

```scala
val books = <books><book>book1</book></books>  //返回scala.xml.Elem,也可写平级，返回NodeBuffer。有label/ child等方法

import scala.xml._
val booksBuffer = new scala.xml.NodeBuffer //得到上面空白的对象
val books: NodeSeq = booksBuffer //得到NodeSeq对象，可遍历

val books = <book id = "1">book1</book>
val id = books.attributes("id").text //取得上面id的值。books.attributes可遍历

//嵌入scala代码
val arr = Array("a", "b")
val books = <books>{arr(1)}</books>
val books1 = <books>{for (book <- arr) yield <book>{book}</book>}</books>
val books2 = <books id = {arr(0)}>book</books>
//修改元素
val books3 = books.copy(child = books.child ++ <book>book2</book>) //得到<books>b<book>book2</book></books>，由于上面books.child是b
//修改属性
val bk = <book id="1">book1</book>
val changed = bk % Attribute(null, "id", "200", Null)
//添加属性
val added = bk % Attribute(null, "id", "200", Attribute(null, "id2", "100", Null))
//加载和写入外部xml文件
val books = XML.load(new InputStreamReader(new FileInputStream(path), "UIF-8"))
```



```scala
Array[T] for Some{type T}
Array[_]

List(1,9,2,4,5).span(_ < 3)// (List(1),List(9, 2, 4, 5))，碰到不符合就结束
List(1,9,2,4,5).partition(_ < 3) // (List(1, 2),List(9, 4, 5))，扫描所有
List(1,3,5,7,9).splitAt(2) // (List(1, 3),List(5, 7, 9))
List(1,3,5,7,9).groupBy(5 < _) // Map((true,List(7, 9)), (false,List(1, 3, 5)))
```




