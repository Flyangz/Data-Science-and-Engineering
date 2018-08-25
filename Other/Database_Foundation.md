# 数据库原理

## 数据库系统概述
### 概念
* DBMS 提供 DDL，DML，管理，建立和维护。
* DBS 包括DB,DBMS,应用系统，DBA和用户。
* 数据管理： 收集、整理、组织、存储、维护、检索、传送
* 处理： 数据处理：对数据进行加工、计算、提炼

## 数据模型
### 概念模型(信息模型）
* 把现实世界中的客观对象抽象成的某种信息结构
现实、信息（概念模型）、机器世界（数据模型，将E-R转换为某一种数据模型）
* 实体、属性、关键词、实体型（表头属性）、实体集（整个二维表）
* 实体之间的联系：1:1, 1:n, m:n。核心问题之一：如何表示和处理实体及实体间的联系
* 表示方法之一：实体—联系方法（Entity-Relationship Approach）

### 数据模型
* 三要素： 数据结构、数据操作、完整性约束
* 层次模型： 用树形结构来表示实体以及实体间联系的模型。1:n
* 网状模型： 用图结构来表示实体以及实体间联系的模型。m:n，高效复杂（在计算机上实现时， m:n的联系仍需分解成若干个1:n的联系）
* 关系模型： 用二维表（关系）来描述实体及实体间联系的模型。每张表一个关系名。关系名（属性1，属性2， …，属性n）方便地表示m:n联系，易懂易用，低效

### DBS的结构
* 三级模式
外模式（与某一应用有关的逻辑表示）、模式（型和关系，一个数据库一个）、内模式（数据的物理结构和存储方式，一个数据库一个）

* 两级映象
外模式/模式：对应同一个模式可以有多个外模式，对每个外模式都有一个外模式/模式映象。模式变，可修改映象使外模式保持不变。
模式/内模式映象：模式/内模式的映象是唯一的。存储结构变，可修改映象使逻辑结构（模式）保持不变。

### 数据库系统的组成
用户——应用程序——辅助软件——操作系统——数据库（DBA负责后三个）

## 关系数据库
### 概念
* 关系的数学定义
域（ Domain）
笛卡尔积（Cartesian Product）eg：
    D1={张三，李四}
    D2={数学，语文}
    D3={优，良}
有8种组合（笛卡尔积），即八个元组（行\ record），每个元组三个分量
* 关系模型
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

### RDBS的数据操纵语言：关系代数
* 传统的集合运算
并、交、差、积
* 专门的关系运算
选择、投影(groupby)、条件连接(join)、自然连接(merge)
除：R（x，y）和S（y，z），则R÷S=P（x1）（某个例子下）
①	求R中x可以取哪些值，并求各值的象集。
②	求S在属性组y上的投影K。
③	检查每个象集是否包含K
* 写关系代数表达式
① 多个关系时，一般使用投影
② “否定”时，一般用差
③ “全部”时，一般用除
④ “至少”时，一般用×

### 关系数据库规范化理论
*  函数依赖
函数依赖
完全函数依赖：
(1)(XH,KH) → CJ， 但拆开就不行
(2)若x→y，且x只包含一个属性
部分函数依赖
传递函数依赖：x→y,y→z,但y 不依赖x, 则x传递函数依赖z
* 平凡函数依赖与非平凡函数依赖
x→y，如果y是x的子集，则该依赖是平凡的
y中至少有一个属性不在x中，则该依赖是非平凡的
* 函数依赖的推理规则
设有关系R，x、y、z为R的一个属性集，则有：
①自反律：若y是x的子集 ，则x→y。
②增广律：若x→y，则xz→yz。
③传递律：若x→y，y→z，则x→z。
注意传递律与传递函数依赖的区别。
④合并律：若x→y，x→z,则x→yz。
⑤分解律：若x→yz，则x→y，x→z。

### 关系模式的规范化
* 一个好的关系模式应满足：
① 冗余应尽可能少；
② 应尽可能避免插入、删除异常；(新开课程没有学生选修时，新开课程的课程号、课程名插不进去。)
③ 消去关系中不合适的属性依赖关系。(如选修某门课的学生毕业了，在删除学生信息的同时，把课程信息也删除掉。)
* 范式
一个关系的非主属性函数依赖于主码的程度
关系规范化： 一个关系从低级范式向高级范式的转换过程
* 关系模式的规范化

第一范式（1NF）：若关系R的所有属性不能再分。
R对各个属性的关系，既有部分依赖，又有完全依赖。R分为二，将满足完全依赖的属性集组成一个关系；将满足部分依赖的属性集组成另一个关系。
（XH,KH）→CJ， KH→KM,XM,DZ

第二范式（2NF）:若关系R∈1NF，且它的每个非主属性完全依赖于主码，则称R∈2NF。R2 一分为二

第三范式（3NF）：消去非主属性对主码的传递依赖

 ①若R∈1NF，且主码只含一个属性，则R一定为2NF。
② 若R∈2NF，且只有0~1个非主属性，则R一定为3NF。
③ 3NF一般控制了数据冗余，一般避免了操作异常。
④ 范式并非越高越好，适可而止。

## 数据库设计
### 设计的步骤
① 需求分析
② 概念结构设计：E-R图
③ 逻辑结构设计：将E-R图转换为某一种数据模型，并优化。
④ 物理结构设计
⑤ 数据库实施
⑥ 数据库运行与恢复

### 概念结构设计
* 局部E-R图设计 
1．确定局部范围
各个部门或各个主要功能作为局部
2．确定实体与属性
① 属性是不能再分的数据项；
② 联系只发生在两实体之间；
③ 原则上，能够作为属性，就不要作为实体。
* 合并成总体E-R图
1．消除各局部E-R图的冲突问题。
2．按公共实体名合并，生成初步E-R图。
3．消除冗余的属性和冗余的联系，生成总体E-R图。

### 逻辑结构设计
#### 联系的属性：必须包含相关联的各实体型的主码。 

#### 联系的主码
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

### 物理结构设计与数据库实施
* 物理结构设计
存储结构：即决定每个表的记录顺序
存取方式：即决定为哪些属性建立非聚集索引，以便加快查找速度。一般把经常查询的属性名指定为非聚集索引
* 数据库实施
定义数据库结构
组织数据入库
编写应用程序
数据库试运行

## SQL
视图（外模式）view， 基本表（模式）table， 存储文件和索引（内模式）index

### SQL语法
DML: select, update, delete, insert into
DDL: CREATE DATABASE, ALTER DATABAS, CREATE TABLE, ALTER TABLE, DROP TABLE, CREATE INDEX, DROP INDEX

* select
SELECT DISTINCT 列名称/* FROM 表名称
WHERE 列 运算符 值 AND/OR (....)
ORDER BY 列 DESC， 

* insert into
INSERT INTO 表名称 VALUES (值1, 值2,....)
INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....)

* update
UPDATE 表名称 SET 列名称 = 新值 WHERE 列名称 = 某值

* delete
DELETE FROM 表名称 WHERE 列名称 = 值
没有where就删除整表

* TOP
SELECT TOP number|percent column_name(s)
FROM table_name

* LIKE
WHERE City (NOT) LIKE 'N%'
%	替代一个或多个字符
_	仅替代一个字符
[charlist]	字符列中的任何单一字符[!charlist]

* IN
WHERE column_name IN (value1,value2,...)

* BETWEEN
NOT BETWEEN 'Adams' AND 'Carter'

* AS
SELECT po.OrderID, p.LastName, p.FirstName
SELECT LastName AS Family, FirstName AS Name

* JOIN
FROM Persons
LEFT/ RIGHT/ FULL JOIN Orders
ON Persons.Id_P = Orders.Id_P

* UNION 
每条 SELECT 语句中的列的顺序必须相同
如果允许重复的值，请使用 UNION ALL

* SELECT INTO 
SELECT *
INTO Persons_backup
FROM Persons
SELECT *
INTO Persons IN 'Backup.mdb'
FROM Persons

* CREATE DATABASE
CREATE DATABASE my_db

* CREATE TABLE
CREATE TABLE Persons
(
Id_P int(size) NOT NULL PRIMARY KEY AUTO_INCREMENT,
LastName decimal(size,d) DEFAULT 'Sandnes',
FirstName char(255),固定长度
Address varchar(255),可变长度
City date(yyyymmdd)
UNIQUE (Id_P)
CHECK (Id_P>0)
)
* 约束

ALTER TABLE Persons
ADD UNIQUE (Id_P)

ALTER TABLE Persons
ADD CONSTRAINT uc_PersonID UNIQUE (Id_P,LastName)

ALTER TABLE Persons
DROP INDEX uc_PersonID

* 外键
Id_P int FOREIGN KEY REFERENCES Persons(Id_P)

* INDEX
CREATE (UNIQUE) INDEX index_name
ON table_name (column_name ASC)

* DROP
ALTER TABLE table_name DROP INDEX index_name
DROP TABLE 表名称
DROP DATABASE 数据库名称
TRUNCATE TABLE 表名称（仅仅删除表格中的数据）

* ALTER
ALTER TABLE table_name
ALTER COLUMN column_name datatype

* VIEW
CREATE VIEW view_name AS
DROP VIEW view_name

* Date 
NOW()	日期和时间
CURDATE()	日期
CURTIME()	时间
DATE()	提取日期或日期/时间表达式的日期部分
EXTRACT()	返回日期/时间按的单独部分
DATE_ADD()	给日期添加指定的时间间隔
DATE_SUB()	从日期减去指定的时间间隔
DATEDIFF()	返回两个日期之间的天数
DATE_FORMAT()	用不同的格式显示日期/时间

DATE - 格式 YYYY-MM-DD
DATETIME - 格式: YYYY-MM-DD HH:MM:SS
TIMESTAMP - 格式: YYYY-MM-DD HH:MM:SS
YEAR - 格式 YYYY 或 YY

### 补充
不能删除列，新增列的值一律为空值， 可增加列宽，但一般不能减小列宽，修改可能 会破坏已有数据
