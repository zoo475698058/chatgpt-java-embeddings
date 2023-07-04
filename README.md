## chatgpt-java-embeddings

### 简介

站在巨人肩膀上的整合工程，简化一些已有轮子的代码，改为maven引入，方便结合自己业务的DEMO工程

工程基于ChatGpt、Milvus向量数据库，使用embeddings、chat接口

### 原理

1.将本地资料库内容分割，通过embeddings接口转为向量存储

2.将问题通过embeddings接口转为向量，通过milvus向量数据库查询相似度最高的资料

3.将问题和资料拼接，通过chat接口生成回答


### 安装Milvus向量数据库

```
wget https://github.com/milvus-io/milvus/releases/download/v2.2.2/milvus-standalone-docker-compose.yml -O docker-compose.yml
sudo docker-compose up -d
```
#### 检查容器状态
sudo docker-compose ps
#### 停止Milvus
要停止Milvus单机版，运行：
sudo docker-compose down
要在停止Milvus后删除数据，运行：
sudo rm -rf  volumes

### 可视化组件
```
docker run -d -p 8800:3000 -e HOST_URL=http://{your_ip}:8800 -e MILVUS_URL={your_ip}:19530 milvusdb/milvus-insight:latest
```

### 项目配置
application.yml中配置向量数据库连接地址和端口
项目test文件夹下的ChatgptJavaEmbeddingsApplicationTests.java，运行prepare函数创建表结构


### 参考项目
https://github.com/Grt1228/chatgpt-java

https://github.com/bigcyy/customized-chatgpt
