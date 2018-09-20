[TOC]

## Spring

### Spring概念

- 开源的轻量级框架。

- 由下面两部分组成。

  ioc：控制反转，交给spring配置创建类对象，而不是new

  aop：面向切面编程，扩展功能不是修改源代码实现（分离应用的业务逻辑与系统级服务）。有预编译和运行期代理两种方式实现。

- 一站式框架：在javaee三层结构中，每层都提供不同的解决技术。

  web层：springMVC，service：spring的ioc，dao层：spring的jdbcTemplate

- 一种容器：包含并管理应用对象的配置和生命周期

> 框架：是一种半成品，封装了特点的处理流程和控制逻辑，不断升级改进的软件。它专注于某一领域，而类库则更通用。

### ioc操作

原理：xml配置文件，dom4j解决xml，工程设计模式，反射

```java
public class UserServlet{
    UserFactory.getService();
}

//配置文件
//<bean id="userService" class="cn.itcast.UserService"/>

public class UserFactory{
    public static UserService getService(){
        //dom4j解决xml
        //根据id值userService，得到id值对应class属性值
        String classValue = "class属性";
        Class clazz = Class.forName(classValue);
        UserService service = clazz.newInstance();
        return service;
    }
}
```

**配置文件方式**

导入jar：beans, context, core, expression, log4j, commons-logging

创建类，在类里面创建方法

创建spring配置文件，配置创建类

写代码测试（也有其他实现方式，但看源码就会知道内在原理是一样的。）

```java
//bean1.xml里面加约束和上面配置文件的代码。
public class TestIOC{
    @Test
    public void testUser(){
        ApplicationContext context = new ClassPathXmlApplicationContext("bean.xml");
        User user = (User) context.getBean("user");
        user.add();
    }
}
```

Spring注入：在启动Spring容器加载bean配置时，完成对变量的赋值行为。（下面是上面代码的另一种实现）

```java
<bean id="injectionService" class="com.iapple.ioc.injection.service.InjectionServiceImpl">
  <property name="injectionDAO" ref="injectionDAO"></property> //设值注入
  <constructor-arg name="injectionDAO" ref="injectionDAO"></constructor-arg> //构造器注入
</bean>
<bean id="injectionDAO" class="com.iapple.ioc.injection.dao.InjectionDAOImpl"></bean>
        
//业务层
public interface InjectionService {
	public void save(String arg);	
}
public class InjectionServiceImpl implements InjectionService {
	
	private InjectionDAO injectionDAO;
	
	//构造器注入
	public InjectionServiceImpl(InjectionDAO injectionDAO1) {
		this.injectionDAO = injectionDAO1;
	}	
	//设值注入
	public void setInjectionDAO(InjectionDAO injectionDAO) {
		this.injectionDAO = injectionDAO;
	}
	public void save(String arg) {
		//模拟业务操作
		System.out.println("Service接收参数：" + arg);
		arg = arg + ":" + this.hashCode();
		injectionDAO.save(arg);
	}	
}

//数据输出层
public interface InjectionDAO {
	public void save(String arg);	
}
public class InjectionDAOImpl implements InjectionDAO {	
	public void save(String arg) {
		//模拟数据库保存操作
		System.out.println("保存数据：" + arg);
	}
}

//测试
@RunWith(BlockJUnit4ClassRunner.class)
public class TestInjection extends UnitTestBase {
	
	public TestInjection() {
		super("classpath:spring-injection.xml");
	}
	
	@Test
	public void testSetter() {
		InjectionService service = super.getBean("injectionService");//injectionService由上面的id决定
		service.save("这是要保存的数据");
	}
	
	@Test
	public void testCons() {
		InjectionService service = super.getBean("injectionService");
		service.save("这是要保存的数据");
	}
	
}
```



### bean

#### bean的配置项（常用）

Id, Class(必须), Scope, Constructor arguments, Properties, Autowiring mode, lazy-initialization mode, Initialization/ destruction method



#### bean的作用域Scope

singleton默认

prototype重新new一个，旧的被回收

request每次http请求创建一个实例，仅在当前request内

session同上，在session内

global session（暂略）

```java
//singleton后，下面service1和service2是同一对象。但如果用两个test分别执行，那就变成两个容器中执行了，singleton自然体现不出来。
public void test() {
	InjectionService service1 = super.getBean("beanScope");
	service1.say();
	InjectionService service2 = super.getBean("beanScope");
	service2.say();
}
```



#### bean生命周期

定义，初始化，使用，销毁

初始化和销毁都有两种方法：实现InitializingBean和DisposableBean接口并覆盖afterPropertiesSet和destroy方法，或者像下面那样配置init-method和destroy-method。当两种方法都实现时，先执行前面的方法。

```java
//设置所有bean的初始和销毁方法。引号内都是BeanLifeCycle的方法名。
default-init-method="defautInit" default-destroy-method="defaultDestroy">
//设置变量名为beanLifeCycle的初始化和销毁方法。
<bean id="beanLifeCycle" class="com.iapple.lifecycle.BeanLifeCycle"  init-method="start" destroy-method="stop"></bean>
```



#### Aware

Spring中一些以Aware结尾的接口，实现这些接口的bean在被初始化后，可以获得相应资源如UnitTestBase内部的ApplicationContext context，然后把它设置为类实例的成员变量，这样在类的方法中可以调用context的方法（例子看resources）。接口方法中的操作可以对这些资源进行操作



#### Autowiring

No, byName（id不会有重复，所以不会抛异常）, byType（找到多个抛异常）, Constructor（没找到抛异常）

```java
//设置下面选项后，某容器中的类如果需要引用另外一个类，那么这个类会从其他bean的id中找名字和这个类定义的变量名一样的class并注入。类似之前提到的“设值注入”
default-autowire="byName"
```



#### resources

.getResource("path")中的path前缀有classpath（project中的resource文件夹）、file（URL from filesystem）、http、（none）（ApplicationContext设置，默认classpath）

```java
public class MoocResource implements ApplicationContextAware  {
	
	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public void resource() throws IOException {
		Resource resource = applicationContext.getResource("config.txt");
		System.out.println(resource.getFilename());
	}
}
```

#### 注解

**Classpath扫描与组件管理**

```java
//通常就用component-scan，后面指使用了注解的class所在的文件夹
<context:component-scan base-package="com.iapple.beanannotation" name-generator="实现了BeanNameGenerator接口的类的自定义命名方法"> //scope也有自定义可以实现
    <context:include-filter type="regex"
        expression=""/>//对扫描的文件夹作进一步筛选。type有很多类型。
</context:component-scan>

//@Component("bean")
@Scope
@Component//通用bean注解，默认变成首字母小写的类名。其他还有@Repository, @Service, @Controller
public class BeanAnnotation {
	
	public void say(String arg) {
		System.out.println("BeanAnnotation : " + arg);
	}	
}
```

加上bean类注解会自动注册为bean，也可以禁用自动注册。

@Required用于setter方法。

@Autowired更常用，用于setter，构造函数和成员变量上。(required=false)可以避免setter找不到类时抛异常，只有一个构造器可以用(required=true)。由BeanPostProcessor处理，所以自己的BeanPostProcessor或BeanFactoryPostProcessor中不能用，只能通过xml或@Bean加载。

```java
@Component
public class BeanInvoker {
	
	@Autowired
	private List<BeanInterface> list;//scan包中实现了BeanInterface接口的类都加载。在这些BeanInterface接口上，可以加上@Order(value)来排序
	
	@Autowired
	private Map<String, BeanInterface> map;
	
	@Autowired//这种组合可用于fields（本例），constructors，multi-argument methods
	@Qualifier("beanImplTwo")//这里只加载beanImplTwo。如果用到上面的集合类型，就不能用@Autowired+@Qualifier，而是用@Resource+@Qualifier。resource看下面的@Resource
	private BeanInterface beanInterface;
}
```

@Qualifier：按类型自动装配可能有多个bean实例，该注解用于缩小范围。上面就是一个例子。它还可以用到方法的参数中。如`public void prepare(@Qualifier("beanImplTwo")BeanInterface beanInterface)`。可自定义。在xml中：

```java
<bean class"...">
    <qualifier value="beanImplTwo"/>
```



@Bean标识一个用于配置和初始化一个由SpringIoC容器管理的新对象的方法，类似于XML配置文件的\<bean>，默认单例，通常是@Configuration+@Bean

```java
public interface Store<T> {
}

public class StringStore implements Store<String> {	
	public void init() {
		System.out.println("This is init.");
	}
	
	public void destroy() {
		System.out.println("This is destroy.");
	}	
}

@Configuration//只是把配置内容加到class代码中，还是要编写xml文件
@ImportResource("classpath:config.xml")//xml中有<context:property-placeholder location="classpath:/config.properties"/>，该config.properties有下面url，username，password的keys信息。
public class StoreConfig {
	
	@Value("${url}")
	private String url;
	
	@Value("${jdbc.username}")//jdbc前缀为了实现唯一性
	private String username;
	
	@Value("${password}")
	private String password;
	
	@Bean
	public MyDriverManager myDriverManager() {
		return new MyDriverManager(url, username, password);
	}


	@Bean(name = "stringStore", initMethod="init", destroyMethod="destroy")
	public Store stringStore() {
		return new StringStore();
	}


	@Bean(name = "stringStore")
	@Scope(value="prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)//代理方式
	public Store stringStore() {
		return new StringStore();
	}

    @Autowired
    private Store<String> s1;//interface Store<T>中定义了泛型
    
    @Autowired
    private Store<Integer> s2;
    
    @Bean
    public StringStore stringStore() {
    	return new StringStore();
    }
    
    @Bean
    public IntegerStore integerStore() {
    	return new IntegerStore();
    }

	@Bean(name = "stringStoreTest")
	public Store stringStoreTest() {//返回类型为Store，不能StringStore，如果用了，上面s1的Autowired会不知道找哪个函数加载。
		System.out.println("s1 : " + s1.getClass().getName());
		System.out.println("s2 : " + s2.getClass().getName());
		return new StringStore();
	}
}
```

@Named，@Resource和@Inject

```java
@Repository
public class JsrDAO {
	
	public void save() {
		System.out.println("JsrDAO invoked.");
	}	
}

//@Service
@Named//等效于@Component
public class JsrServie {
	
	@Resource(name="默认从下面的属性或方法名取")//这个和下面setter的resource选一个注解，效果一样。和Autowired效果类似。
	@Inject//这个和下面setter的Inject选一个注解
	private JsrDAO jsrDAO;
	
	@Resource
	@Inject
	public void setJsrDAO(@Named("jsrDAO") JsrDAO jsrDAO) {
		this.jsrDAO = jsrDAO;
	}
	
	@PostConstruct//这个和下面的@PreDestroy，相当于在xml设置初始化和销毁方法。
	public void init() {
		System.out.println("JsrServie init.");
	}
	
	@PreDestroy
	public void destroy() {
		System.out.println("JsrServie destroy.");
	}

	public void save() {
		jsrDAO.save();
	}	
}
```



### AOP基础

AOP只是为了与IOC容器整合，而不是一个全面的AOP解决方案。

#### 概念

**切面**（Aspect）：一个关注点的模块化，这个关注点可能会横切多个对象。日志服务会单独写到一个类中（模块化），如LogHandler，这个类叫做切面。

**Target Object**：被一个或多个切面所通知的对象。

**JoinPoint**：在目标对象中，将会应用Advice的方法或属性（AspectJ支持将属性作为JoinPoint），这些叫做连接点。

**Advice**：在切面的某个特定连接点上执行的动作。在LogHandler这个类中对日志服务做的具体实现：可以是一句代码（system.out.println），也可是一个方法。Advice**分类**：BeforeAdvice，AfterAdvice，区别在于Advice在目标方法之前调用还是之后调用。其他还有Throw Advice, finally advice, around advice

**PointCut**：指Advice会应用在目标对象的哪些目标方法中，如只应用在add，delete，modify方法上，不应用在select方法上，实际当中需要用**表达式**进行限制。

- execution(public * *(..)) 执行所有public方法
- execution(* set*(..)) 执行所有set开始的方法
- execution( * com.xyz.service.AccountService.*(..)) 执行AccountService类中所有方法
- execution( * com.xyz.service..(..))执行包下所有方法
- execution( * com.xyz.service...(..))执行包以及子包下所有方法（三个点）
- …要查

**Introduction**：在不修改类代码前提下，动态为某个类增加或减少方法和属性。（看下面的declare-parents）

**Proxy**：（可有可无），动态织入创建代理效率慢，有些是在编译时静态织入，不会产生代理类。

**织入**（Weave）：即Advice应用在JoinPoint的过程，这个过程有个专业术语，叫织入



**有无接口区别**：使用JavaSE动态代理，也可以使用CGLIB代理（没有实现接口时）

Spring所有的切面和通知器都必须放在一个\<aop:config>内，里面包含pointcut, advisor, aspect（固定顺序）

#### 配置

切面类中写了各种方法，这种方式只适用于单例模式。

```xml
<bean id="appleAspect" class="com.iapple.aop.schema.advice.appleAspect"></bean> #切面类	，里面有通知所需要的所有方法。
<bean id="aspectBiz" class="com.iapple.aop.schema.advice.biz.AspectBiz"></bean> #业务类
<aop:config>
	<aop:aspect id="appleAspectAOP" ref="appleAspect">
		<aop:pointcut expression="execution(* com.iapple.aop.schema.advice.biz.*Biz.*(..))" id="applePiontcut"/> #执行业务类所在包中，所有Biz结尾的类中的所有方法。这里的配置有很多种方式，详细看上面的pointcut。
		<aop:before method="before" pointcut-ref="applePiontcut"/> #切面类有一个before方法。执行上面的所有方法之前都会执行这个方法。
		<aop:after-returning method="afterReturning" pointcut-ref="applePiontcut"/> 
		<aop:after-throwing method="afterThrowing" pointcut-ref="applePiontcut"/> 
		<aop:after method="after" pointcut-ref="applePiontcut"/> #相当于finally advice
		<aop:around method="around" pointcut-ref="applePiontcut"/> #具体看下面
		
		<aop:around method="aroundInit" pointcut="execution(* com.iapple.aop.schema.advice.biz.AspectBiz.init(String, int))  
						and args(bizName, times)"/> #bizName和times是形参。类似下面的around，不过多了其他参数输入

			<aop:declare-parents types-matching="com.iapple.aop.schema.advice.biz.*(+)" #biz下所有类。
						implement-interface="com.iapple.aop.schema.advice.Fit" #接口
						default-impl="com.iapple.aop.schema.advice.FitImpl"/> #实现上面接口的类
	</aop:aspect>
</aop:config>
```

```java
//around方法
public Object around(ProceedingJoinPoint pjp) {//around方法都是接收ProceedingJoinPoint
	Object obj = null;
	try {
		System.out.println("MoocAspect around 1.");
		obj = pjp.proceed();
		System.out.println("MoocAspect around 2.");
	} catch (Throwable e) {
		e.printStackTrace();
	}
	return obj;
}

//上面declare-parents中的test，返回对象要加(Fit)转换。原本getBean只能返回上面提到的业务类AspectBiz，但利用introduction，就可以对业务类进行类型转换，为业务类定义了一个父类。
@Test
public void testFit() {
	Fit fit = (Fit)super.getBean("aspectBiz");
	fit.filter();
}
```

### AOP的API介绍（暂略）

### Aspectj（暂略）

## SpringBoot

注解：

@SpringBootApplication包含@SpringBootConfiguration, @EnableAutoConfiguration, ComponentScan

@Controller：处理http请求（没有的话浏览器看不到，要配合模版使用，测试才用得上）

@RestController：返回json

@RequestMapping：配置url映射，如加上("/hello")，访问时就要在网址上多加/hello

@PathVariable：获取url中的数据

```java
@RequestMapping(value = "/say/{id}", method = RequestMethod.GET)
public String say(@PathVariable("id") Integer id){
    return "id: " + id;
```

@RequestParam：获取请求参数的值，网址中用"/say?id=100"

```JAVA
@GetMapping(value = "/say")
public String say(@RequestParam("id", required = false, defaultValue = "0") Integer myid){
    return "id: " + myid;
```

@GetMapping：组合注解，上面的注解和上上面的注解一样。

@PostMapping和@RequestBody：前端传过来的JSON对象，要通过@RequestBody才能序列化为CreateMerchantsRequest

```java
@PostMapping("/create")
public Response createMerchants(@RequestBody CreateMerchantsRequest request)
```

@Transactional：事务，同时成功才实现

@ResponseBody：将返回的对象变成json，可以传到客户端。