## chatgpt-java-embeddings

### 简介

站在巨人肩膀上的整合工程，简化一些已有轮子的代码，改为maven引入，方便结合自己业务的DEMO工程
工程基于ChatGpt、Milvus向量数据库，使用embeddings、chat接口

### 流程

1.将本地资料库内容分割，通过embeddings接口转为向量存储
2.将问题通过embeddings接口转为向量，通过milvus向量数据库查询相似度最高的资料
3.将问题和资料拼接，通过chat接口生成回答


### 安装Milvus向量数据库

```
wget https://github.com/milvus-io/milvus/releases/download/v2.2.2/milvus-standalone-docker-compose.yml -O docker-compose.yml
sudo docker-compose up -d
```

初始化Milvus向量数据库表结构以及配置代理
application.yml中配置向量数据库连接地址和端口(本地不需要修改)，然后配置你的代理ip和端口
找到项目test文件夹下的ChatgptJavaEmbeddingsApplicationTests.java，运行prepare函数创建表结构


### 参考项目
https://github.com/Grt1228/chatgpt-java
https://github.com/bigcyy/customized-chatgpt
