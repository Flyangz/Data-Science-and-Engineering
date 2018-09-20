# Redis

一个开源的数据库，其数据存储在内存，并通过异步持久化到硬盘。

- 支持多种数据类型，如String, Hash Table, Linked Lists, Sets 和 Sorted Sets。

- 可发布订阅、事务、Lua脚本、pipeline
- 场景：缓存系统、计算器、（简单）消息队列、排行榜、社交网络、实时系统



#### 基本使用

```bash
# 三种启动
redis-server # 默认配置启动
redis-server --port xxxx # 动态参数启动
redis-server configPath # 配置文件启动
ps -ef|grep redis # 查看进程
netstat -antpl|grep redis # 查看端口
redis-cli -h ip -p port # 客户端连接，ping测试是否连接

# 常用配置
daemonize # 推荐yes，日志会打印到指定文件中 是否是守护进程。
port # 单机多实例必须配置
logfile #系统日志名字
dir

# 通用命令和数据结构
keys [pattern] # O(n)
sadd myset a b c # a b c 是 myset的值
dbsize # O(1)
exists key # O(1)
del key [key ...] # O(1)
expire key seconds # 10为1s
ttl key # 查看剩余时间。如果key已经不存在，返回-2。没有过期时间返回-1。
persist key # 去掉过期时间
type key
```

#### 数据类型

##### **字符串**

value是字符串、整型、字节码等

使用场景：缓存、计数器、分布式锁等

```bash
get key # 不存在返回nil O(1)
set k v #setnx k v 键不存在时设置； set k v xx 键存在才设置
del k
incr | decr key # 每个用户的网页访问量 incr userid:pageview 执行一个这个代码访问量pageview就加一
incrby | decrby key num
mset k1 v1 k2 v2 ... # O(n)
mget k1 k2 # O(n), 比get两次好很多，省去1次网络时间。当k过多也有问题，毕竟单线程。
getset k nv # 原子操作，返回旧值
append k v
strlen k # O(1)
incrbyfloat k num
getrange k start end
setrange k index v 
```

利用redis提高响应速度

```java
public Info get(long id){
    String redisKey = redisPrefix + id;
    Info info = redis.get(redisKey);
    if(info == null){
        info = mysql.get(id);
        if(info != null){
            redis.set(redisKey, serialize(info));
        }
    }
    return info;
}
```



##### 哈希

每个key对应一个哈希表。key组成：set_name:field_num:info

```bash
hget k f # f 指 k:info ，k为哈希表中的key，而不是 redis 的key
hset k f v
hdel k f
hgetall k # k v 都返回。O(n)
hexists k f
hlen k # 获取 field 的数量
hmget k f1 f2 # O(n)
hmset k f1 v1 f2 v2 # O(n)
hincrby user:1:info pageview count # 计算用户访问量，1为用户id
hvals k # O(n)
hkeys k # 获取所有的 field， O(n)
```

**用户信息的存储**：

String的 id + json：更改某个field时，需要把整个对象取出来（包括序列化和反序列化过程）再改。

String的 user:1:field + string：不需整个取出，但用户信息不是一个整体，且内存占用较大。

hash方案：整体。ttl不好控制。



##### List

```bash
rpush k v1 v2 .. # O(1~n)，左侧开始 lpush
linsert k befor|after v nv
lpop k # rpop
lrem k count v # count>0从左删去count个等于v的项，=0删去全部等于v的项
ltrim k start end # 保留的范围
lrange k start end # 可以负数，如1 -1，-1 -3
lindex k index
llen k # O(1)
lset k index nv
blpop k timeout # 阻塞超时时间，也有brpop
```

**组合**

stack = lpush + lpop

queue = lpush + rpop

capped collection （有限容量的集合）= lpush + ltrim

message queue = lpush + brpop



##### **集合**

```bash
sadd user:1:follow elem
srem k elem
scard k # 计算集合大小
sismember k elem # 判断elem是否存在
srandmember k count # 随机抽取count个元素
spop k # 随机弹出元素
smembers k # 取出所有元素
sdiff, sinter, sunion, sdiff|sinter|suion + store destkey # 将结果保存到destkey中
```

**组合**

tagging = sadd

random item = spop/ srandmember

social graph = sadd + sinter



##### 有序集合

```bash
zadd set_name:set_desc score elem # O(logn)
zrem k elem
zscore k elem
zincrby k increScore elem
zcard k
zrank k elem
zrange k 0 -1 withscores # 返回所有elem，O(log(n)+M)，n为集合量，m为范围量
zrangebyscore k minScore maxScore 
zcount k minScore maxScore 
zremrangebyrank k start end
zremrangebyscore k minScore maxScore
zrevrange key start stop [WITHSCORES] # 返回有序集中指定区间内的成员，通过索引，分数从高到底。也有按照分数、排名
```

