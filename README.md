# netty-in-action
## 1.架构设计
## 2.线程模型
### 2.1 常见线程模型
#### 2.1.1 传统阻塞I/O服务模型
#### 2.1.2 Reactor 模型
根据Reactor的数量和处理资源线程池的数量不同，有以下三种实现
* 单 Reactor 单线程
* 单 Reactor 多线程
* 主从 Reactor 多线程（多个Reactor）

而Netty线程模型基于主从Reactor多线程模型做了一定的改进