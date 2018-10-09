# 寻屋项目

[TOC]



## 基本介绍

### 框架

用户表现层：用户界面：普通会员、管理员

业务逻辑层：后台管理、权限控制、房源浏览、搜索、地图找房（暂未实现）、会员系统、预约看房

应用支持层：基于ES搜索引擎、异步索引构建

数据存储层：MySQL、Redis

```java
.
├── HousingApplication: 
├── base
│   ├── ApiDataTableResponse: 页面表格插件Data tables的响应类，继承自下面的ApiResponse。
│   ├── ApiResponse: API响应类
│   ├── AppErrorController: 错误请求全局控制器，非服务功能控制器
│   ├── HouseOperation: 常量类（可能多余），与HouseStatus枚举类一样，表示未审核、已出租等
│   ├── HouseSort: 排序生成器，返回Sort(direction, key)，key自己设定，可以作为排序的属性，direction为Sort.Direction类
│   ├── HouseStatus: 看HouseOperation
│   ├── HouseSubscribeStatus: 预约看房状态枚举类
│   ├── LoginUserUtil: 用户登录工具类，包含邮箱和手机号规则检查、获取当前用户id方法
│   └── RentValueBlock: 带区间的常用数值定义，正常来说会存在数据库，这里简单地存到内存
├── config
│   ├── ElasticSearchConfig: 
│   ├── JPAConfig: 
│   ├── RedisSessionConfig: 
│   ├── WebFileUploadConfig: 图片上传配置
│   ├── WebMvcConfig: Thymeleaf 模版引擎配置类
│   └── WebSecurityConfig: 权限限制类
├── entity
│   ├── House: 
│   ├── HouseDetail: 
│   ├── HousePicture: 
│   ├── HouseSubscribe: 
│   ├── HouseTag: 
│   ├── Role: 
│   ├── Subway: 
│   ├── SubwayStation: 
│   ├── SupportAddress: 
│   └── User: 
├── repository
│   ├── HouseDetailRepository: 
│   ├── HousePictureRepository: 
│   ├── HouseRepository: 
│   ├── HouseTagRepository: 
│   ├── IHouseSubscribeRespository: 
│   ├── RoleRepository: 
│   ├── SubwayRepository: 
│   ├── SubwayStationRepository: 
│   ├── SupportAddressRepository: 
│   └── UserRepository: 
├── security：权限控制相关类
│   ├── AuthFilter: 
│   ├── AuthProvider: 
│   ├── CustomPasswordEncoderFactory: 
│   ├── LoginAuthFailHandler: 
│   └── LoginUrlEntryPoint: 
├── service
│   ├── ISmsService: 
│   ├── IUserService: 
│   ├── ServiceMultiResult: 
│   ├── ServiceResult: 
│   ├── SmsServiceImpl: 
│   ├── house
│   │   ├── AddressServiceImpl: 
│   │   ├── HouseServiceImpl: 
│   │   ├── IAddressService: 
│   │   └── IHouseService: 
│   ├── search
│   │   ├── HouseIndexKey: 
│   │   ├── HouseIndexMessage: 
│   │   ├── HouseIndexTemplate: 
│   │   ├── HouseSuggest: 
│   │   ├── ISearchService: 
│   │   └── SearchServiceImpl: 
│   └── user
│       └── UserServiceImpl: 
└── web
    ├── controller
    │   ├── HomeController: 
    │   ├── admin
    │   │   └── AdminController: 
    │   ├── house
    │   │   └── HouseController: 
    │   └── user
    │       └── UserController: 
    ├── dto：传递给前端的类
    │   ├── HouseDTO: 
    │   ├── HouseDetailDTO: 
    │   ├── HousePictureDTO: 
    │   ├── HouseSubscribeDTO: 
    │   ├── QiNiuPutRet: 传递给七牛云的DTO，暂未实现。
    │   ├── SubwayDTO: 
    │   ├── SubwayStationDTO: 
    │   ├── SupportAddressDTO: 
    │   └── UserDTO: 
    └── form：一些前端取得的数据格式，比如表格、搜索页面等
        ├── DatatableSearch: 
        ├── HouseForm: 
        ├── PhotoForm: 
        └── RentSearch: 
```



### 实现功能

**管理员模块**（AdminController）：

addHouse：添加房源
addHousePage：新增房源功能页
addHouseTag：增加房源标签
adminCenterPage：管理员中心页面
adminLoginPage：管理员登录页面
finishSubscribe：标记完成看房
getUserInfo：（开发调试用？）
houseEditPage：房源编辑页面
houseListPage：房源管理页面
houses：房源管理页面中的房屋列表，可以进行多维度排序、翻页、搜索功能
houseSubscribe：看房预约页面
operateHouse：房源审核接口
removeHousePhoto：移除房源照片
removeHouseTag：移除房源标签
subscribeList：看房预约页面中的房屋列表
updateCover：更新房源封面
updateHouse：修改房源
uploadPhoto：上传房源照片
welcomePage：管理员中心页面中的欢迎页面

**普通会员模块**（UserController）：

cancelSubscribe：取消预约看房
centerPage：普通会员中心页面
loginPage：普通会员登录页面
subscribeDate：预约看房
subscribeHouse：将房源添加到待预约列表
subscribeList：待预约列表
updateUserInfo：更新普通会员信息

**房源模块**（HouseController）：
autocomplete：搜索功能中自动补全
getSupportCities：获取支持城市列表
getSupportRegions：获取对应城市支持区域列表
getSupportSubwayLine：获取具体城市所支持的地铁线路
getSupportSubwayStation：获取对应地铁线路所支持的地铁站点
rentHousePage：搜房页面
show：具体某房源信息页面

### 技术相关

相关技术：ES + MySQL + Kafka

框架：SpringBoot

数据库：MySQL + Spring Data JPA

前端：Bootstrap + thymeleaf + jquery

安全：Spring Security

图片上传：七牛云 + webUpload（实现本地上传）

免注册登录：阿里短信（暂未实现）

负载安全：ES + Nginx（暂未实现）

数据分析：ELK（暂未实现）





