# ElasticSearch

[TOC]



## 基础概念

索引：含有相同属性的文档集合（小写不含下划线），相当于database。分结构化和非结构化

类型：索引可以定义一个或多个类型，文档必须属于一个类型，相当于table

文档：可以被索引的基本数据单位，相当于record

分片：每个索引都有很多个分片，每个分片是一个Lucene索引。只能创建索引时指定数量，默认为5

备份：拷贝一份分片就完成了分片的备份



## 基本用法

Restful API基本格式：http://\<ip>:\<port>/<索引>/<类型>/<文档id>

常用的四种请求方式：GET、PUT、POST、DELETE

 PUT: 创建索引和文档增加

 POST: 文档增加、查询索引和文档修改

 GET: 查询文档

 DELETE: 删除文档和删除索引



**创建结构化索引**

在postman中put下面json到localhost:9200/index_name

```json
例子1
{
	"settings": {
		"number_of_shards": 3,
		"number_of_replicas": 1
	},
	"mappings": {
		"man": {
			"properties": {
				"name": {
					"type": "text"
				},
				"country": {
					"type": "keyword"
				},
				"age": {
					"type": "integer"
				},
				"date": {
					"type": "date",
					"format": "yyyy-MM-dd HH:mm:ss || yyyy-MM-dd||epoch_millis"
				}
			}
		}
	}
}

例子2
{
  "settings": {
    "number_of_replicas": 0,
    "number_of_shards": 5, 一般将分片限制在10～20G
    "index.store.type": "niofs" ,性能更好
    "index.query.default_field": "title", 默认查询字段
    "index.unassigned.node_left.delayed_timeout": "5m" 当某个节点挂掉时，不马上回复分片
  },
  "mappings": {
    "house": {
      "dynamic": false, 用"strict"就完全不让结构变化
      "_all": {
      "enabled": false 6已经废除，默认为true。会将全部字段拼接为整体作全文索引
      },
      "properties": {
        "houseId": {
          "type": "long"
        },
        "title": {
          "type": "text",
          "index": "analyzed" 需要分词
        },
        "price": {
          "type": "integer"
        },
        "createTime": {
          "type": "date",
          "format": "strict_date_optional_time||epoch_millis"
        },
        "cityEnName": {
          "type": "keyword" 不需要分词
        },
        "regionEnName": {
          "type": "keyword"
        },
        "tags": {
          "type": "text"
        }
      }
    }
  }
}
```



**插入数据**

在postman用put + http://\<ip>:\<port>/<索引>/<类型>/<文档id> + json代码即可

自动生成id的话，用post + 去掉\<文档id>



**修改**

直接修改：post + 文档id/_update + json{"doc"：{"属性": "值"}}

脚本修改（painless是内置语言）：

```json
{
    "script": {
        "lang": "painless",
        "inline": "ctx._source.age = params.age",
        "params": {
            "age": 100
        }
    }
}
```



**删除**

在postman用delete，或者在插件中操作。



## 查询

在postman用post + http://\<ip>:\<port>/<索引>/_search

### 子条件查询

特定字段查询所指特定值。分为Query context和Filter context。

- Query context：除了判断文档是否满足条件外，还会计算\_score来标识匹配度。
  - 全文本查询：针对文本类型数据。分模糊匹配、习语匹配、多个字段匹配、语法查询
  - 字段级别查询：针对结构化数据，如数字、日期等。
- Filter context：返回准确的匹配，比Query快。

```json
模糊查询，下面匹配会返回含有China或Country的数据。改为match_phrase就是准确匹配。
from是从哪一行开始search，size是返回多少条符合的数据
{
    "query": {
        "match": {
        	"country": "China Country"
        }
    },
    "from": 0,
    "size": 2,
    "sort": [
        {"age": {"order": "desc"}}
    ]
}

多字段查询，下面address和country列中有apple的都会查询出来
{
    "query": {
        "multi_match": {
            "query": "apple",
        	"fields": ["address", "country"]
        }
    }
}

语法查询
{
    "query": {
        "query_string": {
            "query": "apple OR pear"
        }
    }
}

字段查询，下面聚合查询的term有s。准确匹配（和习语的区别？）。
{
    "query": {
        "term": {
            "author": "apple"
        }
    }
}
{
    "query": {
        "range": {
            "age": {
                "gte": 10,
                "lte": 50
            }
        }
    }
}

filter
{
    "query": {
        "bool": {
            "filter": {
                "term": {
                    "age": 20
                }
            }
        }
    }
}
```

聚合查询

下面得出各个年龄的数据行数。terms可改为stats, min, max等

```json
{
    "aggs": {
        "group_by_age": {
            "terms": {
                "field": "age"
            }
        },
        "group_by_xxx": {
            ...
        }
    }
}
```





### 复合条件查询

以一定逻辑组合子条件查询。常用的分为固定分数查询、布尔查询等

```json
固定分数查询，这里把查询的分数固定为2，即filter中返回yes的数据分数为2
{
    "query": {
        "constant_score": {
            "filter": {
                "match": {
                    "title": "apple"
                }
            },
            "boost": 2
        }
    }
}

布尔查询，这里should表示满足一个条件就够了。must就是都要满足，还要must not
{
    "query": {
        "bool": {
            "should": [
                {
                    "match": {
                        "author": "apple"
                    }
                },
                {
                    "match": {
                        "tittle": "fruit"
                    }
                }
            ],
            "filter": [
                {
                    "term": {
                        "age": 20
                    }
                }
            ]
        }
    }
}
```



## 配置

client.transport.sniff为true来使客户端去嗅探整个集群的状态，把集群中其它机器的ip地址自动加到客户端中

当ES服务器监听使用内网服务器IP而访问使用外网IP时，不要使用client.transport.sniff为true，在自动发现时会使用内网IP进行通信，导致无法连接到ES服务器，而直接使用addTransportAddress方法进行指定ES服务器。



```java
// 一些优化说明
//boolQuery中must和should的配合
//通过boost增加权重
boolQuery.must(
        QueryBuilders.matchQuery(HouseIndexKey.TITLE, rentSearch.getKeywords())
                .boost(2.0f)
);

post
localhost:9200/index_name/type_name/_search?explain=true
    
禁止删除索引时使用通配符
put + http://<ip>:<port>/_cluster/settings 动态方式改设置
{
    "transient": {
        "action.destructive_requires_name": true
    }
}

put + http://<ip>:<port>/_all/_settings?preserve_existing=true
{
    index.refresh_interval: "30s"
}

非动态改设置，即在config文件中改
discovery.zen.fd.ping_interval: 10s
discovery.zen.fd.ping_timemout: 120s
discovery.zen.fd.ping_retries: 3

master节点一般不存储数据
node.master: true
node.data: false
针对数据节点，关闭http功能。从而减少一些插件安装到这些节点，浪费资源。
http.enable: false

负载均衡节点：master和data都为false，但一般不用自带的

内存设定：JVM针对内存小于32G才会优化，所以每个节点不要与这个值

写入数据从index改为bulk
```



