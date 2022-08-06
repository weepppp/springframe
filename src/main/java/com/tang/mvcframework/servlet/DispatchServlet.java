package com.tang.mvcframework.servlet;

import com.tang.mvcframework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author weepppp 2022/8/6 8:20
 * 容器初始化配置类
 * <p>
 * 框架在初始化的时候都要做那些事情:
 * 1.加载配置文件（init类的doLoadConfig）
 * 2.扫描所有相关的类（init类的doScanner）
 * 3.初始化所有相关联的类，并且将其保存在IOC容器里面（init类的doInstance方法）
 * 4.执行依赖注入（给加了@Autowired注解的字段赋值）（init类的doAutowired方法）
 * 5.Spring 和核心功能已经完成 IOC、DI
 * 6.构造HandlerMapping，将URL和Method进行关联（init类的HandlerMapping方法）
 **/
public class DispatchServlet extends HttpServlet {

    /**
     * 此变量用来保存application.properties配置文件中的内容
     */
    private Properties contextConfig = new Properties();

    /**
     * 保存扫描的所有类名
     */
    private List<String> classNames = new ArrayList<>();

    /**
     * 这就是传说中的IOC容器
     * 为了简化程序，先不考虑ConcurrentHashMap，主要还是关注设计思想和原理
     * key默认是类名首字母小写，value就是对应的实例对象
     */
    private Map<String, Object> ioc = new HashMap<>();

    /**
     * 保存url和Method的对应关系
     */
    private Map<String, Method> handlerMapping = new HashMap<>();

    /**
     * 核心方法init()，相关初始化步骤都被封装成方法
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        System.out.println("==============================");
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        doScanner(contextConfig.getProperty("scanPackage"));
        doInstance();
        doAutowired();
        doInitHandlerMapping();
        System.out.println("Mars MVC framework initialized");
    }

    /**
     * 1.加载配置文件
     *
     * @param contextConfigLocation 读取xml文件中初始化的配置的文件名
     */
    private void doLoadConfig(String contextConfigLocation) {
//        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("application.properties");
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 2.扫描所有相关的类
     *
     * @param scanPackage 从配置文件中读到的包类信息
     */
    private void doScanner(String scanPackage) {
        //转换为文件路径，实际上就是把 . 替换为 /
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                //全类名 = 包名.类名
                String classname = (scanPackage + "." + file.getName().replace(".class", ""));
                classNames.add(classname);
            }
        }
    }

    /**
     * 3.初始化所有相关的类，并且将其保存在IOC容器里面（也就是加了bean相关注解的那些类）
     */
    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                //什么样的类才需要初始化呢？———
                //加了注解的类才初始化>>>模拟Spring框架中的注解开发——
                //只用@Controller和@Service举例
                if (clazz.isAnnotationPresent(Controller.class)) {
                    //Spring bean名 默认类名首字母小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    //在多个包下可能出现相同的Service注解下的类名
                    //自定义命名
                    String beanName = clazz.getAnnotation(Service.class).value();
                    if ("".equals(beanName.trim())) {
                        //或 默认的类名首字母小写
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    //或 如果是接口
                    //判断有多少个实现类，如果只有一个，默认就选择这个实现类
                    //如果有多个，只能抛异常
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The " + i.getName() + " is exists!!");
                        }
                        ioc.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 封装一个处理类名首字母大写转小写的工具方法
     *
     * @param simpleName
     * @return
     */
    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        //使用字母的ascii编码前移实现
        //32为是char类型大小写的差数，-32是小写变大写，+32是大写变小写
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * 4.实现依赖注入（给加了@Autowired注解的字段赋值）
     */
    public void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }
        // 遍历ioc容器的键值对
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // entry.getValue().getClass().getDeclaredFields()) ： ioc容器所有类的所有公共字段
            for (Field field : entry.getValue().getClass().getDeclaredFields()) {
                //首先排除没有加Autowired注解的字段
                if (!field.isAnnotationPresent(Autowired.class)) {
                    continue;
                }
                Autowired autowired = field.getAnnotation(Autowired.class);
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    //得到加了Autowired注解的变量的类型的名字
                    beanName = field.getType().getName();
                }
                //开启暴力访问（取得私有属性的值）
                field.setAccessible(true);
                try {
                    //解释下这一句：
                    // public void set(Object obj, Object value) : 在指定对象obj中，将当前 Field 对象表示的成员变量设置为指定的新值
                    // entry.getValue()就是储存的值的任意实例
                    // ioc.get(beanName)就是通过Autowired注释的变量名从ioc中找到的实例值
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 5.构造HandlerMapping，将URL和Method进行关联
     * 也就是mvc中通过处理器映射器找到url相应的处理器中的一部分
     */
    private void doInitHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }
            //相当于提取 class上配置的url
            //也就是@RequestMapping上的路径
            String baseUrl = "";
            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                baseUrl = requestMapping.value();
            }
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                // 类url+方法url (//demo//query)
                //TODO 这里的replaceAll("/+","/")是什么？可以去掉吗？
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                handlerMapping.put(url, method);
                System.out.println("Mapped: " + url + "," + method);
            }
        }
    }

    /**
     * 容器初始化部分完成后，进入运行时处理逻辑部分，即
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException {
        //拿到除去host（域名或者ip）部分的路径
        String url = req.getRequestURI();
        //拿到上面路径的根路径
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");
        //找到该url对应的方法
        Method method = this.handlerMapping.get(url);
        //保存请求的url参数列表
        Map<String, String[]> params = req.getParameterMap();
        //获取方法的形参列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        //保存赋值参数的位置
        Object[] paramValues = new Object[parameterTypes.length];
        //根据参数位置动态赋值
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class){
                paramValues[i] = req;
            } else if(parameterType == HttpServletResponse.class){
                paramValues[i] = resp;
            } else if(parameterType == String.class) {
                //如果请求或者响应类型中没有方法参数的值的类型，就通过param注解在运行时去获取
                //Annotation[][]:返回该方法参数的所有注解（返回第几个参数的第几个注解）
                Annotation[][] pa = method.getParameterAnnotations();
                for (int j = 0; j < pa.length; j++) {
                    for(Annotation a : pa[j]){
                        if (a instanceof RequestParam){
                            // 拿到RequestParam注解的值
                            String paramName = ((RequestParam) a).value();
                            if (!"".equals(paramName.trim())){
                                String value = Arrays.toString(params.get(paramName))
                                        .replaceAll("\\[|\\]", "")
                                        .replaceAll("\\s+", ",");
                                paramValues[i] = value;
                            }
                        }
                    }
                }
            }
        }
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        method.invoke(ioc.get(beanName),paramValues);
    }

}
