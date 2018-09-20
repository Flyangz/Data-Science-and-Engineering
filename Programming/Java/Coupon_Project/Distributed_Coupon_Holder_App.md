# 分布式优惠券系统后台

---

基础工具：JDK8、Maven

框架：SpringBoot

消息队列：Kafka

存储层：Mysql、Hbase、Redis



### 介绍：

**商户投放子系统**（商户开放平台）

创建商户、查看商户信息、投放优惠券、上传token/使用口令

**用户应用子系统**（用户使用入口）

创建用户、目前可领取的优惠券信息、领取优惠券、目前拥有的优惠券信息、使用优惠券、已经使用了的优惠券信息、可领取的、反馈（对优惠券或者App）、查看反馈信息

**辅助功能**

日志分析：行为、用户id、时间、地区、具体请求信息。

日志记录：时间、事情



### 数据存储

**商户投放子系统**

- Mysql：商户信息。`name`、logo`logo_url`、营业执照`business_license_url`、联系电话`phone`、地址`address`、审核`is_audit`
- 优惠券信息PassTemplate通过Kafka存到Hbase
- 优惠券Token信息存到Redis

**用户应用子系统**（HBase）

- pass表：用户所拥有的优惠券信息。`user_id`, `template_id`（映射到Hbase中的具体优惠券信息）, `token`, `assigned_date`(领取日期), `con_date`(消费日期，-1还没被消费) 
- passtemplate表：商户投放的所以优惠券信息。所属商户id`id`、标题（全局唯一）`title`、摘要`summary`、详细信息`desc`、数量限制`limit`、是否有token`has_token`、背景色`background`、开始时间`start`、结束时间`end`
- feedback表：反馈信息。`user_id`, `type`, `template_id`(-1为对app的评论), comment
- user表：用户基本信息`name`, `age`, `sex`。其他信息`phone`, `address`



### 代码概要

 **文件结构**

constants：常量、枚举

controller：负责http请求，一个controller提供一类服务

dao：数据库操作相关类，如增删改查等

entity：实体类，对应mysql中的一个表

log：日志的封装

mapper：ORM，表数据与java对象之间的映射。

service：实现业务逻辑的类

utils：大多日期、文件之类的处理类

vo(value object)：请求和响应的类，一般建个统一的Response，里面的返回对象就装这些类。

```bash
.# 商户投放子系统
├── MerchantsApplication.java
├── constant
│   ├── Constants: Kafka topic、通用token、token info
│   ├── Errorcode：枚举类，各种error的号码和描述
│   └── TemplateColor：枚举类，优惠券三种颜色
├── controller
│   └── MerchantsCtl：对创建商户、查看商户信息、投放优惠券的http请求进行日志打印和调用相应功能的方法
├── dao
│   └── MerchantsDao：mysql接口，可根据商户id或名称获取商户对象
├── entity
│   └── Merchants：商户mysql表的实体类
├── security
│   ├── AccessContext：线程独享环境存储商户请求时带有的token
│   └── AuthCheckInterceptor：权限拦截器，校验token
├── service
│   ├── IMerchantsServ：功能接口
│   └── impl
│       └── MerchantsServImpl：创建商户、查看商户信息、投放优惠券的功能实现
└── vo
    ├── CreateMerchantsRequest：创建商户请求的对象类，包括校验商户是否已登记、信息是否完整和转换为Merchants的方法
    ├── CreateMerchantsResponse：创建商户响应的对象类
    ├── PassTemplate：投放优惠券请求的对象类，包括校验商户是否已登记。
    └── Response：统一响应类，包括错误码、错误信息、响应对象。
    
.# 用户应用子系统
├── UserApplication.java
├── advice
│   └── GlobalExceptionHandler.java
├── constant
│   ├── Constants：优惠券的kafka topic、token文件存储目录、已使用的token文件后缀、redis key、存储在HBase中pass, passtemplate, user, feedback四张表的列族和列名
│   ├── FeedbackType：枚举类，反馈是针对优惠券还是app
│   └── PassStatus：枚举类，查看优惠券信息的三类情况：未被使用、已使用、全部
├── controller
│   ├── CreateUserController：处理创建用户的http请求
│   ├── PassbookController：处理查看拥有的全部优惠券信息、查看已使用的优惠券信息、查看可领取的优惠券、领取优惠券、使用优惠券、创建评论、获取评论信息的http请求
│   └── TokenUploadController：处理上传token的http请求
├── dao
│   └── MerchantsDao：mysql接口，可根据商户id或名称获取商户对象
├── entity
│   └── Merchants：商户mysql表的实体类
├── log
│   ├── LogConstants：用户动作名称
│   ├── LogGenerator：日志生成器
│   └── LogObject：日志对象类，包括表示动作、用户id、ip等信息的成员变量
├── mapper：映射类，提取上面Constants中定义的四个表的属性并作为HBase的列族和列名提取value并实例化回相应的对象
│   ├── FeedbackRowMapper.java
│   ├── PassRowMapper.java
│   ├── PassTemplateRowMapper.java
│   └── UserRowMapper.java
├── service
│   ├── ConsumePassTemplate：Kafka 消费者，打印日志和调用下面HBasePassServiceImpl中的把优惠券写入HBase的方法
│   ├── IFeedbackService：功能接口
│   ├── IGainPassTemplateService：功能接口
│   ├── IHBasePassService：功能接口
│   ├── IInventoryService：功能接口
│   ├── IUserPassService：功能接口
│   ├── IUserService：功能接口
│   └── impl
│       ├── FeedbackServiceImpl：创建和获取反馈
│       ├── GainPassTemplateServiceImpl：领取优惠券，给用户添加优惠券，将已使用的 token 记录到文件中
│       ├── HBasePassServiceImpl：把优惠券写入HBase
│       ├── InventoryServiceImpl：获取库存信息，只返回没有领取的
│       ├── UserPassServiceImpl：查询所拥有的、已使用的、未使用的优惠券信息，使用优惠券
│       └── UserServiceImpl：创建用户，包括user rowkey生成
├── utils
│   └── RowKeyGenUtil：为feedback, pass, passTemplate生成RowKey
└── vo
    ├── ErrorInfo：统一的错误信息属性类
    ├── Feedback：反馈属性类，包括校验内容完整性
    ├── GainPassTemplateRequest
    ├── InventoryResponse
    ├── Pass：pass属性类
    ├── PassInfo：用户领取的优惠券信息
    ├── PassTemplate：投放的优惠券的属性类
    ├── PassTemplateInfo：passTemple和merchant组成的类
    ├── Response：统一响应类，包括错误码、错误信息、响应对象
    └── User：用户属性类
```



