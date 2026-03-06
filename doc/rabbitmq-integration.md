# RabbitMQ 集成文档

## 概述

本项目使用 RabbitMQ 实现异步判题功能,提升系统性能和用户体验。

## 架构设计

```
用户提交代码
    ↓
QuestionSubmitService (生产者)
    ↓
发送消息到 RabbitMQ
    ↓
judge_exchange (交换机)
    ↓
judge_queue (队列)
    ↓
JudgeMessageConsumer (消费者)
    ↓
JudgeService 执行判题
    ↓
更新数据库结果
```

## 安装 RabbitMQ

### Windows 安装

1. 安装 Erlang: https://www.erlang.org/downloads
2. 安装 RabbitMQ: https://www.rabbitmq.com/download.html
3. 启动 RabbitMQ 服务:
```bash
rabbitmq-server
```

4. 启用管理插件:
```bash
rabbitmq-plugins enable rabbitmq_management
```

5. 访问管理界面: http://localhost:15672
   - 默认用户名: guest
   - 默认密码: guest

### Docker 安装 (推荐)

```bash
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3-management
```

## 配置说明

### application.yml (公共配置)

```yaml
rabbitmq:
  host: localhost
  port: 5672
  username: guest
  password: guest
  queue:
    judge: judge_queue
  exchange:
    judge: judge_exchange
  routing-key:
    judge: judge_routing_key
```

### application-dev.yml (开发环境)

```yaml
spring:
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    listener:
      simple:
        concurrency: 3          # 最小消费者数量
        max-concurrency: 10     # 最大消费者数量
        acknowledge-mode: manual # 手动确认
        prefetch: 1             # 每次只处理一个消息
```

## 核心组件

### 1. RabbitMQConfig (配置类)

- 创建判题队列 (judge_queue)
- 创建判题交换机 (judge_exchange)
- 绑定队列和交换机
- 配置消息转换器 (JSON格式)

### 2. JudgeMessageProducer (生产者)

负责发送判题消息到 RabbitMQ:

```java
@Resource
private JudgeMessageProducer judgeMessageProducer;

// 发送判题消息
judgeMessageProducer.sendJudgeMessage(questionSubmitId);
```

### 3. JudgeMessageConsumer (消费者)

监听判题队列,处理判题任务:

```java
@RabbitListener(queues = "${rabbitmq.queue.judge}", ackMode = "MANUAL")
public void receiveJudgeMessage(Long questionSubmitId, Channel channel, long deliveryTag) {
    // 执行判题
    judgeService.doJudge(questionSubmitId);
    // 手动确认
    channel.basicAck(deliveryTag, false);
}
```

## 消息确认机制

### 手动确认 (Manual Acknowledgment)

- **成功**: `channel.basicAck(deliveryTag, false)` - 确认消息已处理
- **失败**: `channel.basicNack(deliveryTag, false, false)` - 拒绝消息,不重新入队

### 为什么使用手动确认?

1. 确保判题任务真正完成后才确认消息
2. 判题失败时可以记录日志,不重新入队避免死循环
3. 更好的错误处理和监控

## 优势对比

### 使用 RabbitMQ 前 (线程池)

- ❌ 服务重启时,未处理的任务丢失
- ❌ 无法跨服务器分布式处理
- ❌ 难以监控任务队列状态
- ❌ 内存占用随任务增加而增加

### 使用 RabbitMQ 后

- ✅ 消息持久化,服务重启不丢失
- ✅ 支持多个消费者并行处理
- ✅ 可视化监控队列状态
- ✅ 削峰填谷,保护系统稳定性
- ✅ 解耦生产者和消费者

## 监控和管理

### 访问管理界面

http://localhost:15672

### 查看队列状态

- Ready: 等待处理的消息数
- Unacked: 正在处理的消息数
- Total: 总消息数

### 查看消费者

- Consumers: 当前消费者数量
- Consumer utilisation: 消费者利用率

## 性能优化

### 1. 并发消费

```yaml
listener:
  simple:
    concurrency: 3          # 最小3个消费者
    max-concurrency: 10     # 最大10个消费者
```

### 2. 预取数量

```yaml
prefetch: 1  # 每个消费者一次只处理1个消息
```

### 3. 消息持久化

队列和消息都设置为持久化,防止数据丢失。

## 故障处理

### 1. RabbitMQ 连接失败

检查:
- RabbitMQ 服务是否启动
- 端口 5672 是否开放
- 用户名密码是否正确

### 2. 消息堆积

原因:
- 消费者处理速度慢
- 消费者数量不足

解决:
- 增加消费者数量 (max-concurrency)
- 优化判题逻辑性能

### 3. 消息丢失

确保:
- 队列持久化 (durable=true)
- 消息持久化
- 使用手动确认模式

## 测试

### 1. 启动 RabbitMQ

```bash
docker start rabbitmq
# 或
rabbitmq-server
```

### 2. 启动后端服务

```bash
mvn spring-boot:run
```

### 3. 提交代码测试

访问前端页面提交代码,观察:
- 后端日志: 消息发送和接收
- RabbitMQ 管理界面: 队列消息变化

## 生产环境配置

### application-prod.yml

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
    virtual-host: /prod
    listener:
      simple:
        concurrency: 5
        max-concurrency: 20
        acknowledge-mode: manual
        prefetch: 1
```

### 环境变量

```bash
export RABBITMQ_HOST=your-rabbitmq-host
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=your-username
export RABBITMQ_PASSWORD=your-password
```

## 扩展功能

### 1. 死信队列 (DLX)

处理判题失败的消息,避免无限重试。

### 2. 延迟队列

实现定时判题功能。

### 3. 优先级队列

VIP 用户的判题任务优先处理。

## 参考资料

- [RabbitMQ 官方文档](https://www.rabbitmq.com/documentation.html)
- [Spring AMQP 文档](https://docs.spring.io/spring-amqp/reference/)
