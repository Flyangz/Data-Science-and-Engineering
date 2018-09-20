# Mysql

[TOC]

bin可执行文件，data数据，docs文档，include存头文件，share错误信息和字符集文件

配置文件：default-character-set=utf8，character-set-server=utf8

> 一些基础补充：
>
> mysql> 提示符，可修改
>
> select 可以接 version(), user(); now();

## 基础

### 概念和入口

- DDL: Data Definition Language，如create
- TPL: transaction processing language
- DCL: Data Control Language ，如grant

- DML: Data Manipulation Language，如select, insert，update或delete

```mysql
# 创建数据库
CREATE {DATABASE | SCHEMA}[IF NOT EXISTS] db_name CHARACTER SET [=] charset; 
#一般udf8，但查询前，可SET col_name gbk:来改变中文的显示（免乱码）

# 查看数据库
SHOW {DATABASES | SCHEMA};
# 查看警告信息，比如重复创建表后出现警告。
SHOW WARNINGS;
# 显示创建db_name时使用的指令，可查看编码信息。
SHOW CREATE DATABASE db_name
# 修改数据库
ALTER DATABASE test CHARACTER SET = gbk
# 删除数据库
DROP {DATABASE | SCHEMA}[IF EXISTS] db_name
# 使用
USE db_name;
# 查看当前数据库
SELECT DATABASE();
```



### 数据类型

| 数值类型       | 存储需求                                        | 取值范围                                                     |
| -------------- | ----------------------------------------------- | ------------------------------------------------------------ |
| tinyint[(m)]   | 1字节                                           | 有符号值：-128 到127（- 2^7 到2^7 – 1） 无符号值：0到255（0 到2^8 – 1） |
| smallint[(m)]  | 2字节                                           | 有符号值：-32768 到32767（- 2^15 到2^15 – 1） 无符号值：0到65535（0 到21 6 – 1） |
| mediumint[(m)] | 3字节                                           | 有符号值：-8388608 到8388607（- 2^23 到2^23 – 1 ） 无符号值：0到16777215（0 到2^24 – 1） |
| int[(m)]       | 4字节                                           | 有符号值：-2147683648 到2147683647（- 2^31 到2^31- 1） 无符号值：0到4294967295（0 到2^32 – 1） |
| bigint[(m)]    | 8字节                                           | 有符号值：-9223372036854775808 到9223373036854775807（- 2^63到2^63-1） 无符号值：0到18446744073709551615（0到2^64 – 1） |
| float[(m, d)]  | 4字节                                           | 最小非零值：±1.175494351e – 38，可到7位小数                  |
| double[(m, d)] | 8字节                                           | 最小非零值：±2.2250738585072014e – 308                       |
| decimal (m, d) | m字节（mysql < 3.23），m+2字节（mysql > 3.23 ） | 可变；其值的范围依赖于m 和d                                  |

| 时间类型  | 存储字节数 | 取值范围                                 |
| --------- | ---------- | ---------------------------------------- |
| DATE      | 4          | 1000-01-01——9999-12-31                   |
| TIME      | 3          | -838:59:59——838:59:59                    |
| DATETIME  | 8          | 1000-01-01 00:00:00——9999-12-31 23:59:59 |
| TIMESTAMP | 4          | 19700101080001——20380119111407           |
| YEAR      | 1          | 1901——2155                               |

| 文字类型   | 说明                                                         |
| ---------- | ------------------------------------------------------------ |
| CHAR       | 1~255个字符的定长串，它的长度必须在创建时指定，否则MySQL假定为CHAR（1） |
| VARCHAR    | 可变长度，最多不超过255字节，如在创建时指定VARCHAR（n），则可存储0~n个字符的变长串 |
| TINYTEXT   | 同TEXT，最大长度为255字节                                    |
| MEDUIMTEXT | 同TEXT，最大长度为16K                                        |
| TEXT       | 最大长度为64K的变长文本                                      |
| LONGTEXT   | 同Text，最大长度为4GB（纯文本，一般不会到4G）                |
| ENUM       | 接受最多64K个串组成的预定义集合的某个串                      |
| SET        | 接受最多64K个串组成的预定义集合的零个或多个串                |



### 表使用

```mysql
CREATE TABLE [IF NOT EXISTS] TABLE_NAME(
  column_name data_type,
)
# 例子1
CREATE TABLE tb1(
  username VARCHAR(20),
  age TINYINT UNSIGNED,
  salary FLOAT(8,2) UNSIGNED, 
)
# 例子2
create table t_user(
    id int(10) not null auto_increment,#必须与主键组合，默认1,2,3..，浮点小数要为0
    username varchar(30) default null,
    pwd varchar(10) default null,
    regTime timestamp null default null on update current_timestamp,
    primary key(id)
)engine=InnoDB default charset=utf8;
#例子3
CREATE TABLE tb6( 
    id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, 
    username VARCHAR(20) NOT NULL UNIQUE KEY, 
    sex ENUM('1','2','3') DEFAULT '3' 
);

# 显示数据库中数据表
SHOW TABLES [FROM db_name];
# 表中各列的设置，相当于SHOW COLUMNS FROM tbl_name;
describe tbl_name 
# 插入记录，可给部分col插入值
INSERT [INTO] tbl_name [(col_name),...] VALUES (val,...)
# 查看记录
SELECT expr,... FROM tbl_name
空值与非空(NULL , NOT NULL)
ex:
CREATE TABLE tb2( username VARCHAR(20) NOT NULL, age TINYINT UNSIGNED NULL );

```

**约束**

```mysql
# 约束
UNIQUE KEY #约束的字段可以为null
NOT NULL #非空约束
PRIMARY KEY #主键约束
UNIQUE KEY #唯一约束，字段可以为null
DEFAULT #默认约束
FOREIGN KEY #外键约束

show indexes from table_name;
```

**外键约束**

要求

1. 外键是表中的一列，其值必须列在另一个表的主键中（在另一个表是唯一约束）。
2. 父表（自表参照的表）和子表（具有外健列的表）必须使用相同引擎，而且禁止使用临时表。
3. 数据表的存储引擎只能为InnoDB（配置设置）
4. 外键列和参照列必须具有相似的数据类型。其中数字的长度或是否有符号位必须相同；而字符的长度则可以不同
5. 外键列和参照列必须创建索引（设置为主键就有了索引）。如果外键列不存在索引的话，MySql将自动创建索引

`pid BIGINT, FOREIGN KEY (pid) REFERENCES provinces(id) ON DELETE CASCADE `

操作

1. CASCADE：从父表删除或更新自动删除或者更新子表中的外键行（操作先在父表中进行，如果子表插入了父表主键中没有的值，会出错，但设置递增的id还是会变）
2. SET NULL：从父表删除或者更新自动设子表中的外建行为null（子表中该列自然不能定义为）
3. RESTRICT：拒绝对父表的删除或者更新操作
4. NO ACTION：标准的SQL的关键字，在MySQL中于RESTRICT相同



### 修改表

添加单列`ALTER TABLE tbl_name ADD [COLUMN] col_name column_definition [FIRST | AFTER col_name]`

添加多列`ALTER TABLE tbl_name ADD [COLUMN] (col_name col_definition)`列名和定义要加在小括号内，且只能在末尾添加

删除列`ALTER TABLE tbl_name DROP [COLUMN] col_name`

添加主键约束`ALTER TABLE tbl_name ADD [CONSTRAINT [symbol]] PRIMARY KEY [index_type](index_col_name)` [CONSTRAINT [symbol]]是添加约束名字

删除主键`ALTER TABLE tbl_name DROP PRIMARY KEY`

唯一约束类似，drop后用`INDEX index_col_name`

外间约束，修改在括号后加上REFERENCES table_name(col_name)，删除`DROP FOREIGN KEY fk_symbol`

默认约束用alter而不是add`ALTER TABLE tbl_name ALTER [COLUMN] col_name {SET DEFAULT literal | DROP DEFAULT}`

修改列定义`ALTER TABLE tbl_name MODIFY [COLUMN] col_name col_definition [FIRST | AFTER col_name]`

修改列名`ALTER TABLE tbl_name CHANGE [COLUMN] old_col_name new_col_name col_definition [FIRST | AFTER col_name]`修改列定义多了一个选项

修改表名：`RENAME TABLE tbl_name TO new_tbl_name [, tbl_name2 TO new tbl_name2]`用alter也可以



### 记录操作

**更新**

`INSERT [INTO] tbl_name [(col_name)] {VALUES | VALUE}`

主键如果设置了自增，传‘NULL’或者‘DEFAULT’，如果设置了default，直接传Default。

values(default, "a", 3*7),("null", "b", 1)一次插入两条

用到自查询的话，用得少`INSERT [INTO] tbl_name SET col_name = { expr | DEFAULT},...`

用select`INSERT [INTO] tbl_name [(col_name,...)] SELECT ...`

更新`UPDATE [LOW_PRIORITY][IGNORE] table_reference SET col_name1={ expr1 | DEFAULT} [, col_name2 = { expr2 | DEFAULT}]... [WHERE where_condition]`

**删除**

`DELETE FROM tbl_name [WHERE where_condition]`



### 查询

自查询外层可以是：select, insert, update, set, do

```mysql
SElECT select_expr [, select_expr] 
[ 
  FROM table_references 
  [WHERE where_condition]
  [GROUP BY { col_name | position} [ASC | DESC],...] //查询结果分组
  [HAVING where_condition] //分组条件
  [ORDER BY {col_name | expr | position} [ASC | DESC],...] 
  [LIMIT {[offset,] row_count | row_count OFFSET offset}]
]

# 子查询
select col1, col2 from table1 where col1 >= (select round(avg(col_name),2) from table_name);
# = ANY 或 = SOME 等价于 IN
SELECT goods_id,goods_name,goods_price FROM tdb_goods 
WHERE goods_price = ANY(SELECT goods_price FROM tdb_goods WHERE goods_cate = '超级本')
ORDER BY goods_price DESC;

# 根据查询更新表
INSERT table1 (col1) SELECT goods_cate FROM table2 GROUP BY col2;
# 更简便是创建表同时更新表
CREATE TABLE tdb_goods_brands (
  cate_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  cate_name VARCHAR(40) NOT NULL
) SELECT brand_name FROM tdb_goods GROUP BY brand_name;
# 根据分类表table2的id替换原表table1各类的名称
UPDATE table1 INNER JOIN table2 ON (table1)goods_cate = (table2)cate_name 
SET (table1)goods_cate = (table2)cate_id ;

# join
table_reference {[ INNER | CROSS ] JOIN | { LEFT | RIGHT } [ OUTER ] JOIN } table_reference ON conditional_expr
# 三表连接
SELECT goods_id,goods_name,cate_name,brand_name,goods_price FROM tdb_goods AS g
RIGHT JOIN tdb_goods_cates AS c ON g.cate_id = c.cate_id
RIGHT JOIN tdb_goods_brands AS b ON g.brand_id = b.brand_id;

# foreign key物理连接，实际用的不多。多建立几张分类表更常见。建立后要把原表的字符串分类改为smallint类型。这种方法的缺陷在于，原表可以插入分类表中没有的编号

# 无限分类

# 删除id较大的重复记录：子查询中得出了重复记录的id和name，且id是小的那条记录的id。原表对子查询表进行连接左连接，而且是原表id大于子查询表的id，能够连接的记录就从原表中删除。
DELETE t1 FROM tdb_goods AS t1 LEFT JOIN (SELECT goods_id,goods_name FROM tdb_goods GROUP BY goods_name HAVING count(goods_name) >= 2 ) AS t2 ON t1.goods_name = t2.goods_name WHERE t1.goods_id > t2.goods_id;
```



### 运算

**字符运算**

CONCAT()：`CONCAT(col1, col2)`

CONCAT_WS():`CONCAT_WS('|', 'A', 'B')`

FORMAT(): `FORMAT(NUM, 数位)`， 返回字符型

LOWER(), UPPER(), LEFT(), RIGHT(): `LOWER(LEFT('AAA',2))`

LENGTH(), LTRIM(), RTRIM(), TRIM(LEADING/BOTH/TRAILING '?' FROM '??MYSQL'), LIKE(), REPLACE('??MY??SQL??', '?', '')

SUBSTRING('MYSQL', 1, 2), SUBSTRING('mYSQL', -1)

LIKE: `LIKE '%1%%' ESCAPE '1'` %表示匹配一个或多个，_表示任意一个



**数字运算**

CEIL(): 进一取整

DIV(): 整数除法

FLOOR(), MOD(), POWER(), ROUND(), TRUNCATE()



**比较运算**

BETWEEN .. AND ..

IN()

IS NULL



索引



事务：一组要么同时执行成功，要么同时执行失败的SQL语句，是数据库操作的一个执行单元。

开始于：执行一条DML语句

结束于：执行COMMIT或ROLLBACK语句；执行DDL语句或DCL；断开连接；执行DML语句失败（会执行ROLLBACK）

特点：原子，一致，隔离，持续

隔离级别（效率越来越低）：读取未提交，读取已提交，可重复读（一个事务中重复读结果一样），序列化



### 其他

```mysql
CREATE USER 'username'@'host' IDENTIFIED BY 'password';
GRANT privileges ON databasename.tablename TO 'username'@'host'
flush privileges;

GRANT SELECT,INSERT,UPDATE,DELETE ON azkaban.* to 'test'@'%' WITH GRANT OPTION;
```



### **JDBC**

为Java开发者使用数据库提供了统一的编程接口，有一组java类和接口组成。

访问数据库流程：加载JDBC，建立连接，发送查询，得到结果

```Java
//Statement
.createStatement//发送简单的sql语句（了解）
.prepareStatement//发送多个sql语句，效率高，防止SQL注入
.prePareCall//用于调用存储过程
    
execute();//看不到结果
ResultSet rs = ps.executeQuery();//运行select语句，返回结果
while(rs.next()){
    System.out.println(rs.getInt(1), rs.getString(2));
}
executeUpdate();//运行insert/update/delete，返回更新的行数
```

```scala
object MySQLUtils {

  def getConnect() = {
    DriverManager.getConnection("jdbc:mysql://localhost:3306/table_name","user", "password")
  }

  def release(connection: Connection, pstmt: PreparedStatement): Unit ={
    try{
      if (pstmt != null) {
        pstmt.close()
      }
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      if (connection != null) {
        connection.close()
      }
    }
  }

  def main(args: Array[String]): Unit = {
    println(getConnect())
  }
}

object StatDAO {

  def inserDayVideoAccessTopN(list: ListBuffer[DayVideoAccessStat]): Unit = {

    var connection: Connection = null
    var pstmt: PreparedStatement = null

    try{
      connection = MySQLUtils.getConnect()

      val sql = "insert into day_video_access_topn_stat(day, cms_id, times) values (?, ?, ?)"
      val pstmt = connection.prepareStatement(sql)

      connection.setAutoCommit(false)

      for (ele <- list) {
        pstmt.setString(1, ele.day)
        pstmt.setLong(2, ele.cmsId)
        pstmt.setLong(3, ele.times)

        pstmt.addBatch()
      }

      pstmt.executeBatch()
      connection.commit()

    } catch {
      case e:Exception => e.printStackTrace()
    } finally {
      MySQLUtils.release(connection, pstmt)
    }
  }
}
```



## 数据库

### 概念

- DBMS 提供 DDL，DML，管理，建立和维护。
- DBS 包括DB,DBMS,应用系统，DBA和用户。
- 数据管理： 收集、整理、组织、存储、维护、检索、传送
- 处理： 数据处理：对数据进行加工、计算、提炼

### 数据模型

**概念模型(信息模型）**

- 把现实世界中的客观对象抽象成的某种信息结构
  现实、信息（概念模型）、机器世界（数据模型，将E-R转换为某一种数据模型）
- 实体、属性、关键词、实体型（表头属性）、实体集（整个二维表）
- 实体之间的联系：1:1, 1:n, m:n。核心问题之一：如何表示和处理实体及实体间的联系
- 表示方法之一：实体—联系方法（Entity-Relationship Approach）

**数据模型**

- 三要素： 数据结构、数据操作、完整性约束
- 层次模型： 用树形结构来表示实体以及实体间联系的模型。1:n
- 网状模型： 用图结构来表示实体以及实体间联系的模型。m:n，高效复杂（在计算机上实现时， m:n的联系仍需分解成若干个1:n的联系）
- 关系模型： 用二维表（关系）来描述实体及实体间联系的模型。每张表一个关系名。关系名（属性1，属性2， …，属性n）方便地表示m:n联系，易懂易用，低效

**DBS的结构**

- 三级模式
  外模式（与某一应用有关的逻辑表示）、模式（型和关系，一个数据库一个）、内模式（数据的物理结构和存储方式，一个数据库一个）
- 两级映象
  外模式/模式：对应同一个模式可以有多个外模式，对每个外模式都有一个外模式/模式映象。模式变，可修改映象使外模式保持不变。
  模式/内模式映象：模式/内模式的映象是唯一的。存储结构变，可修改映象使逻辑结构（模式）保持不变。

**数据库系统的组成**

用户——应用程序——辅助软件——操作系统——数据库（DBA负责后三个）

### 关系数据库

**概念**

- 关系的数学定义
  域（ Domain）
  笛卡尔积（Cartesian Product）eg：
    D1={张三，李四}
    D2={数学，语文}
    D3={优，良}
  有8种组合（笛卡尔积），即八个元组（行\ record），每个元组三个分量
- 关系模型
  数据结构: 
  关系的三种类型： 基本，查询和视图
  关系操作：
  查询：选择、投影、连接、除、并、交、差
  维护：增加、删除、修改
  完整性：
  实体完整性：指关系的所有主属性都不能取空值
  参照完整性：指一个关系外码的取值必须是相关关系中主码的有效值或空值。
    例：班级( 班名,人数)
    学生(学号,姓名,性别,密码,班名)
  在学生表中，班名的取值必须是班级表[班名]的值或空值。
  用户定义的完整性

#### RDBS的数据操纵语言：关系代数

- 传统的集合运算
  并、交、差、积
- 专门的关系运算
  选择、投影(groupby)、条件连接(join)、自然连接(merge)
  除：R（x，y）和S（y，z），则R÷S=P（x1）（某个例子下）
  ①	求R中x可以取哪些值，并求各值的象集。
  ②	求S在属性组y上的投影K。
  ③	检查每个象集是否包含K
- 写关系代数表达式
  ① 多个关系时，一般使用投影
  ② “否定”时，一般用差
  ③ “全部”时，一般用除
  ④ “至少”时，一般用×

#### 关系数据库规范化理论

- 函数依赖
  函数依赖
  完全函数依赖：
  (1)(XH,KH) → CJ， 但拆开就不行
  (2)若x→y，且x只包含一个属性
  部分函数依赖
  传递函数依赖：x→y,y→z,但y 不依赖x, 则x传递函数依赖z
- 平凡函数依赖与非平凡函数依赖
  x→y，如果y是x的子集，则该依赖是平凡的
  y中至少有一个属性不在x中，则该依赖是非平凡的
- 函数依赖的推理规则
  设有关系R，x、y、z为R的一个属性集，则有：
  ①自反律：若y是x的子集 ，则x→y。
  ②增广律：若x→y，则xz→yz。
  ③传递律：若x→y，y→z，则x→z。
  注意传递律与传递函数依赖的区别。
  ④合并律：若x→y，x→z,则x→yz。
  ⑤分解律：若x→yz，则x→y，x→z。

#### 关系模式的规范化

- 一个好的关系模式应满足：
  ① 冗余应尽可能少；
  ② 应尽可能避免插入、删除异常；(新开课程没有学生选修时，新开课程的课程号、课程名插不进去。)
  ③ 消去关系中不合适的属性依赖关系。(如选修某门课的学生毕业了，在删除学生信息的同时，把课程信息也删除掉。)
- 范式
  一个关系的非主属性函数依赖于主码的程度
  关系规范化： 一个关系从低级范式向高级范式的转换过程
- 关系模式的规范化

第一范式（1NF）：若关系R的所有属性不能再分。
R对各个属性的关系，既有部分依赖，又有完全依赖。R分为二，将满足完全依赖的属性集组成一个关系；将满足部分依赖的属性集组成另一个关系。
（XH,KH）→CJ， KH→KM,XM,DZ

第二范式（2NF）:若关系R∈1NF，且它的每个非主属性完全依赖于主码，则称R∈2NF。R2 一分为二

第三范式（3NF）：消去非主属性对主码的传递依赖

 ①若R∈1NF，且主码只含一个属性，则R一定为2NF。
② 若R∈2NF，且只有0~1个非主属性，则R一定为3NF。
③ 3NF一般控制了数据冗余，一般避免了操作异常。
④ 范式并非越高越好，适可而止。



### 数据库设计

#### 设计的步骤

① 需求分析
② 概念结构设计：E-R图
③ 逻辑结构设计：将E-R图转换为某一种数据模型，并优化。
④ 物理结构设计
⑤ 数据库实施
⑥ 数据库运行与恢复

#### 概念结构设计

- 局部E-R图设计 
  1．确定局部范围
  各个部门或各个主要功能作为局部
  2．确定实体与属性
  ① 属性是不能再分的数据项；
  ② 联系只发生在两实体之间；
  ③ 原则上，能够作为属性，就不要作为实体。
- 合并成总体E-R图
  1．消除各局部E-R图的冲突问题。
  2．按公共实体名合并，生成初步E-R图。
  3．消除冗余的属性和冗余的联系，生成总体E-R图。

#### 逻辑结构设计

**联系的属性**：必须包含相关联的各实体型的主码。 

**联系的主码**

1：1联系：可以是相关联的任一实体型的主码。
班长(XH，XM，NL，BH)
班级（BH，RS）
BH作关联，且1：1关系

1：n联系：必须是n方实体型的主码。
学生（XH，XM，NL，BH）
班级（BH，RS，XH）
XH为n方（学生）的主码，第二个XH为班长学号

m：n联系：必须是相关联的各实体型的主码之和。
学生(sno，sname， ssex， sage， sdept)
课程（cno， cname，credit）
选修（sno， cno， grade）
sno， cno为主码，其组合为学生主码sno和课程主码之和。多出了选修表，即不能消化该关系。

#### 物理结构设计与数据库实施

- 物理结构设计
  存储结构：即决定每个表的记录顺序
  存取方式：即决定为哪些属性建立非聚集索引，以便加快查找速度。一般把经常查询的属性名指定为非聚集索引
- 数据库实施
  定义数据库结构
  组织数据入库
  编写应用程序
  数据库试运行