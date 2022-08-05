### *spring框架*
---
#### 框架在初始化的时候都要做那些事情
1. 加载配置文件（init类的doLoadConfig）
2. 扫描所有相关联的类（init类的doScanner）
3. 初始化所有相关联的类，并且将其保存在IOC容器里面（init类的doInstance方法）
4. 执行依赖注入（把加了@Autowired注解的字段赋值）（init类的doAutowired方法）
5. Spring 和核心功能已经完成 IOC、DI
6. 构造HandlerMapping，将URL和Method进行关联（init类的HandlerMapping方法）
