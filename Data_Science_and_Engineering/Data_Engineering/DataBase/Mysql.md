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
| VARCHAR    | 可变长度，最多不超过255字符，如在创建时指定VARCHAR(n)，则可存储0~n个字符的变长串。使用UTF8存储汉字时，Varchar(255)=765个字节 |
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

**外键约束**（知道理论，但不要在SQL实践，在Java代码层实现）

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



### 索引

1.对经常更新的表就避免对其进行过多的索引，对经常用于查询的字段应该创建索引。

2.数据量小的表最好不要使用索引。

3.在相同值少的列上不要建立索引，比如性别。反之。

4.where, group by, order, on从句中出现的列

5.索引字段越小越好

6.离散程度大（不同值多的列）的列放到联合索引的前面

**索引类型**

- 单列索引：一个索引只包含单个列，但一个表中可以有多个单列索引
  - MySQL中基本索引类型，没有什么限制，允许在定义索引的列中插入重复值和空值，纯粹为了查询数据更快一点。
  - 唯一索引：允许为空值
  - 主键索引：不允许空
- 组合索引：在表中的多个字段组合上创建的索引，只有在查询条件中使用了这些字段的左边字段时，索引才会被使用
- 全文索引：只有在MyISAM引擎上才能使用，只能在CHAR,VARCHAR,TEXT类型字段上使用

```mysql
CREATE TABLE 表名[字段名 数据类型]  [UNIQUE|FULLTEXT|SPATIAL|...] [INDEX|KEY] [索引名字] (字段名[length]) [ASC|DESC]

CREATE TABLE tb2( 
    ...
INDEX(year_publication),
UNIQUE INDEX UniqIdx(id),
PRIMARY KEY(id),
INDEX MultiIdx(id,name,age),
FULLTEXT INDEX FullTxtIdx(info)
);

create index index_name on table_name(col); # 顺序索引
```





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
SET (table1)goods_cate = (table2)cate_id ; #set后面不再加其他sql，表的确定在前面的join就写完了
# 更新自身表
update user1 a join (select b.`user_name` from user1 a join user2 b on a.`user_name`=b.`user_name`) b on a.`user_name`=b.`user_name` set a.over='xxx';

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

SUBSTRING('MYSQL', 1, 2), SUBSTRING('MYSQL', -1)

LIKE: `LIKE '%1%%' ESCAPE '1'` %表示匹配一个或多个，_表示任意一个



**数字运算**

CEIL(): 进一取整

DIV(): 整数除法

FLOOR(), MOD(), POWER(), ROUND(), TRUNCATE()



**比较运算**

`BETWEEN .. AND ..`,  `IN()`,  `IS NULL`



**日期时间**

now()

curdate()

curtime()

data_add('2014-3-12', interval 1 year)

datadiff('2014-3-12', '2014-3-13')

data-format('2014-3-12', '%m/%d/%y')



**信息**

`connection_id()`, `select datebase()`, `last_insert_id()`如果批量插入，只显示第一条记录的id, `select user()`, `select version()`, `DELIMITER //`修改分隔符, `select row_count`上一步操作（增删改）设计的行数



**聚合**

`avg`, `count`, `max`, `min`, `sum`



**加密**

`md5`, `set password=password("xxx")`



**自定义**（效率低，尽量少用）

`CREATE FUNCTION function_name RETURNS {STRING|INTER|REAL|DECIMAL} routine_body`

`begin ... end`包含复杂的语句, 	`DROP FUNCTION [IF EXISTS] function_name`

```mysql
create function f1() returns varchar(30)
return date_format(now(),'%Y年%m月%d日 %H点:%i分:%s秒');

CREATE FUNCTION f2(num1 SMALLINT UNSIGNED,num2 SMALLINT UNSIGNED)
RETURNS FLOAT(10，2) UNSIGNED
RETURN(num1+num2)

CREATE FUNCTION ADD_USER(p_id SMALLINT,username VARCHAR(20))
RETURNS INT UNSIGNED
BEGIN
INSERT user(p_id,username) VALUES(p_id,username);
RETURN LAST_INSERT_ID();
END
```



### 存储过程（了解，此功能难以调试和扩展，更没有移植性）

MYSQL执行流程：命令 -> 引擎分析 -> 编译 -> 执行结果 -> 返回

存储过程是SQL语句和控制语句的预编译集合，以一个名称存储并作为一个单元处理。

```mysql
Create [definer = {user|current_user}] procedure sp_name ([[in|out|inout] param_name type]) [COMMENT ......] routine_body

# 不能修改routine_body
ALTER PROCEDURE sp_name [characteristic]
COMMENT 'string'
|{CONTAINS SQL|NO SQL|READS SQL DATA|MODIFIES SQL DATA}
|SQL SECURITY{DEFINER|INVOKER}
# 解析
Contains Sql：包含sql语句，但不包含读和写数据的语句
No sql ： 不包含sql语句
Reads sql data：包含读数据的语句
Modifies sql data：包含写数据的语句
Sql security {definer|invoker}指明谁有权限来执行

DROP PROCEDURE [IF EXISTS] sp_name;

# 例子。忘了加关键词IN 和 OUT ，导致用户变量的返回值为NULL
CREATE PROCEDURE removerUserAndReturnUserName(IN showID INT UNSIGNED,OUT showName INT UNSIGNED)
BEGIN
DELETE FROM user WHERE id = showID;# 参数名不能用和表中字段相同的名字
SELECT count(ID) FROM user INTO showName;# 将结果保存到showName中
END//

CALL removerUserAndReturnUserName(10,@nums); # @nums本身当前用户所特有的变量，CALL可能因为过程的书写有问题而没有返回值，但是其他正确的部分却依然会被执行。
SELECT @nums; #得到过程中select语句的结果
```

**与自定义函数的选择：**

存储过程：经常针对表操作，可返回多个值，独立使用

自定义函数：不针对表操作，返回一个值，其他SQL语句的组成部分



### 存储引擎

存储数据到内存的技术，每种存储引擎使用不同的存储机制、索引技巧、锁定水平等。

锁：共享锁（读锁）、排他锁（写锁）；表锁（开销小）、行锁（开销大）

事务：一组要么同时执行成功，要么同时执行失败的SQL语句，是数据库操作的一个执行单元。

- 开始于：执行一条DML语句

- 结束于：执行COMMIT或ROLLBACK语句；执行DDL语句或DCL；断开连接；执行DML语句失败（会执行ROLLBACK）

- 特点：原子，一致，隔离，持续

- 隔离级别（效率越来越低）：读取未提交，读取已提交，可重复读（一个事务中重复读结果一样），序列化



MYISAM适合事务处理不多，读多写少

InnoDB适合事务处理多，需要外键支持

Archive适合存储日志

| **功 能**    | **MyISAM** | **Memory** | **InnoDB** | **Archive** |
| ------------ | ---------- | ---------- | ---------- | ----------- |
| 存储限制     | 256TB      | RAM        | 64TB       | 无          |
| 事务安全     | -          | -          | 支持       | -           |
| 索引：全文   | 支持       | -          | -          | -           |
| 索引：数     | 支持       | 支持       | 支持       | -           |
| 索引：哈希   | -          | 支持       | -          | -           |
| 锁颗粒       | 表         | 表         | 行         | 行          |
| 数据压缩     | 支持       | -          | -          | 支持        |
| 支持数据缓存 | -          | N/A        | 支持       | -           |
| 支持外键     | -          | -          | 支持       | -           |



## 优化

硬件、系统配置、表结构、索引和代码

```mysql
#启用mysql慢查日志
show variables like 'slow_query_log' #查看是否开启
show variables like 'slow_query_log_file' #查看写到哪
set global slow_query_log_file = path;
set global long_query_time=1 #多久算慢
set global log_queries_not_using_indexes=on #记录没有索引的查询
set global slow_query_log=on #开启

#慢查日志的分析工具用法
mysqldumpslow -h 
pt-query-digest -h

#执行计划
explain sqlquery
```

**发现有问题的SQL**

1、查询次数多且每次查询占用时间长的SQL
通常为pt-query-digest分析的前几个查询
2、IO大的SQL
注意pt-query-digest分析中的Rows examine项，既是扫描的行数越多，IO消耗越大
3、未命中索引的SQL
注意pt-query-digest分析中Rows examine和Rows send的对比，如果扫描的行数远大于发送的行数，那命中率太低了。



**执行计划结果**

explain返回各列的含义

- id：值相同由上至下，值越大越先执行。如果为null，则是union的结果集
- select_type：
  - SIMPLE 不包含子查询或是UNION操作的查询
  - PRIMARY 查询中如果包含任何子查询，那么最外层的查询则被标记为PRIMARY
  - SUBQUERY SELECT 列表中的子查询 
  - DEPENDENT SUBQUERY 子查询中的第一个SELECT，取决于外面的查询，要优化，如join优化自查询、或两个查询按顺序执行。
  - UNION union操作的第二个或是之后的查询的值为union
  - DEPENDENT UNION 当union作为子查询时，第二或者是第二个后的查询的值 
  - UNION RESULT union产生的结果集 
  - DERIVED 出现在from子句中的子查询
- table：显示这一行的数据是关于哪张表的
- type：显示连接使用了何种类型。从最好到最差（至少要达到range级）：
   - system，这是const连接类型的一个特例，当查询的表只有一行时使用。
   - const，表中有且只有一个匹配的行时使用，如对主键或是唯一索引的查询
   - eq_ref，唯一索引或者是主键索引查找，对于每个索引键，表中只有一条记录与之匹配，比如join另一个表的主键
   - ref，非唯一索引查找，返回匹配某个单独值的所有行。
   - ref_or_null，类似于ref类型的查询，但是附加了对NULL值列的查询。
   - index_merge，该联接类型表示使用了索引合并优化方法。
   - range，索引范围扫描，常见于between、>、<、这样的查询条件。
   - index， 全索引扫描
   - all，全表扫描
- possible_keys：显示可能应用在这张表中的索引。
- key：实际使用的索引。此处的值可能不出现在POSSIBLE_KEYS，说明使用了覆盖索引。
- key_len：使用的索引的最大可能长度。在不损失精确性的情况下，长度越短越好。字段定义计算而来，并非数据的实际长度。
- ref：显示索引的哪一列被使用了，一个常数效果最好
- rows：需要扫描的行数（抽样结果）
- filtered：越大越好，说明返回结果行数占读取行数的比。
- extra列需要注意的返回值
   - Using filesort：需要优化。MYSQL需要进行额外的步骤来发现如何对返回的行排序，根据连接类型以及存储排序键值和匹配条件的全部行的行指针来排序全部行
   - Using temporary：需要优化，MYSQL需要创建一个临时表来存储结果，这通常发生在对不同的列集进行ORDER BY上，而不是GROUP BY上。



**限制**：存储过程、触发器、UDF无法显示

```mysql
#max
创建索引

#count
select count(year='2007' or null) as '2007_count', count(year='2008' or null) as '2008_count' #注意加上or null

#有时子查询可变为jion，但要注意district去重复
#join优化聚合自查询，改为先join所有表，然后group，再having
select*
from user1 a join user_kills b on a.id = b.user_id
where b.kills=(select max(c.kills) 
               from user_kills c 
               where c.user_id = b.user_id);
#优化为               
select * 
from user1 a
join user_kills b on a.id = b.user_id
join user_kills c on c.user_id = b.user_id
group by a.user_name, b.timestr, b.kills
having b.kills = max(c.kills)

#再如
select user_id, timestr, kills, (select count(*) from user_kills b where b.user_id=a.user_id and a.kills <= b.kills) as cnt from user_kills a group by user_id, timestr, kills;
#优化为
select a.user_id, a.timestr, a.kills, count(b.kills) as cnt from user_kills as a join user_kills b on a.user_id = b.user_id where a.kills <= b.kills group by a.user_id, a.timestr, a.kills

#group by，先子group后join优于先join后group;因为一般inner join可能两个表比较大，从而需要使用（执行时自动）临时表。
select count(*)
from table1
inner join table2 using(col1)
group by table1.col1
#上面可优化
select c.cnt
from table1 
inner join (select col1, count(*) as cnt from table2 group by col1) as c
using(col1);

#limit，下面语句改为order by 主键/有索引的列
select film_id, description from sakila.film order by title limit 50, 5; #第50行开始往后5行记录
#记录上次返回的主键，在下次查找时用主键过滤
select film_id, description from sakila.film where film_id > 55 and film_id <= 60 order by title limit 1, 5;
```



**索引优化**

```bash
#查重
pt-duplicate-key-checker\
-uroot\
-p ``\
-h localhost

#查很少使用的
pt-index-usage\
-uroot\
-p ``
mysql-slow.log
```



### 编程习惯

**强制**

1. 区分`count(*)`和`count(col)`：前者会统计null行，后者不会。
2. count(distinct col) 计算该列除NULL之外的不重复行数，注意 count(distinct col1, col2) 如果其中一列全为NULL，那么即使另一列有不同的值，也返回为0。
3. 当某一列的值全是NULL时，count(col)的返回结果为0，但sum(col)的返回结果为NULL，因此使用sum()时需注意NPE问题。 `SELECT IF(ISNULL(SUM(g)),0,SUM(g)) FROM table; 
4. 使用 ISNULL()来判断是否为 NULL 值。 NULL与任何值的直接比较都为null
5. 在代码中写分页查询逻辑时，若count为0应直接返回，避免执行后面的分页语句。 
6. 不得使用外键与级联，一切外键概念必须在应用层解决（为了高并发）。
7. 禁止使用存储过程，存储过程难以调试和扩展，更没有移植性。 
8. 数据订正（特别是删除、修改记录操作）时，要先select，避免出现误删除，确认无误才能执行更新语句。 



**推荐**

1. in操作能避免则避免，若实在避免不了，需要仔细评估in后边的集合元素数量，控制在1000个之内。

2. 如果有全球化需要，所有的字符存储与表示，均以utf-8编码，注意字符统计函数的区别。

   ```
   SELECT LENGTH("轻松工作")； 返回为12
   SELECT CHARACTER_LENGTH("轻松工作")； 返回为4
   ```

   如果需要存储表情，那么选择utf8mb4来进行存储，注意它与utf-8编码的区别。

3. 代码开发阶段不建议使用TRUNCATE TABLE

4. 使用预编译语句进行操作（下面JDBC操作已提及）

5. 避免使用双%的查询条件（不能调用索引，右%可以）

6. 使用left join或not exists优化not in

7. where中禁止进行函数转换和计算

8. 禁止使用order by rand()，而是在程序中生成随机值然后获取数据

9. 用where createtime >= 'xxx' and createtime < 'xxx'来替代where date(createtime) = 'xxx'

10. 在明显不会有重复值时使用union all（不会去重）而不是union

## 练习

```mysql
#join表并分组topn
select d.user_name, c.timestr, kills
from(select a.user_id, a.timestr, a.kills, count(b.kills) as cnt 
     from user_kills as a left join user_kills b on a.user_id = b.user_id and a.kills < b.kills      
     group by a.user_id, a.timestr, a.kills
) c join user1 d on c.user_id = d.id
where cnt <= 1;
#注意left join和join会有不同结果。前者保留最大值，右侧b表为null，后者去掉最大值
#如果是a.kills <= b.kills，就排除并列，即如果第二名有两个，那么第二名就不显示了，如果显示前三名。

#join表并行转列
select sum(case when user_name='a' then kills end) as 'a',
sum(case when user_name='b' then kills end) as 'b',
sum(case when user_name='c' then kills end) as 'c',
from user1 a join user_kills b on a.id = b.user_id;

#explode。对一列包含多个字段或者对多列进行explode，太复杂，还是用其他语言吧

#利用SQL建立特殊需求的序列号（感觉还是java好）

#删除重复数据(仅仅根据一列，复杂的用其他语言)
delete a
from user1_test a join(
  select user_name, count(*), max(id) as id
  from user1_test
  group by user_name having count(*) > 1
) b on a.user_name = b.user_name where a.id < b.id
```





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



---

参考：

高性能可扩展MySQL数据库设计及架构优化 电商项目，sqlercn，https://coding.imooc.com/class/79.html