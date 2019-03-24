package com.yh.learn.servlet;

import com.yh.learn.annotation.MyComponent;
import com.yh.learn.annotation.MyController;
import com.yh.learn.annotation.MyRequestMapping;
import com.yh.learn.util.ClassUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by yanghua on 2019/3/24.
 */
public class MyDispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();

    private List<String> classListName = new ArrayList<>();

    private Map<String, Object> ioc = new HashMap<>();

    private Map<String, String> controllerMap = new HashMap<>();

    private Map<String, Method> handlerMap = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 加载配置文件
        initConfig(config);
        // 扫描包中的类
        initScanner(properties.getProperty("scanPackage"));
        // 初始化扫描后的类
        initInstance();
        // 初始化HandlerMapping(将url和method对应上)
        initHandlerMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatcher(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        if(handlerMap.isEmpty()) {
            return;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        if(!this.handlerMap.containsKey(url)){
            resp.getWriter().write("404 NOT FOUND!");
            return;
        }

        doHandle(url, req, resp);
    }

    private void initConfig(ServletConfig config) {
        String location = config.getInitParameter("contextConfigLocation");
        //把web.xml中的contextConfigLocation对应value值的文件加载到流里面
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(location);
        try {
            //用Properties文件加载文件里的内容
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //关流
            if(null!=resourceAsStream){
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void initScanner(String packageName) {
        // 获取类的路径
        String dirPath = ClassUtil.getRootPath()+packageName.replaceAll("\\.", "/");
        try {
            dirPath = URLDecoder.decode(dirPath, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        File dir = new File(dirPath);
        for(File file : dir.listFiles()) {
            if(file.isDirectory()) {
                //如果是文件夹，就递归
                initScanner(packageName + "." + file.getName());
            } else {
                String className = packageName + "." + file.getName().replace(".class", "");
                classListName.add(className);
            }
        }
    }

    private void initInstance() {
        if(classListName.isEmpty()) {
            return;
        }
        for(String className : classListName) {
            try {
                Class<?> clazz = Class.forName(className);
                // 如果添加了MyComponent注解，就创建一个实例
                if(clazz.isAnnotationPresent(MyComponent.class)) {
                    ioc.put(ClassUtil.toLoweFirstWord(clazz.getName()), clazz.newInstance());
                } else {
                    continue;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            continue;
        }
    }

    private void initHandlerMapper() {
        if(ioc.isEmpty()) {
            return;
        }
        for(Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<? extends Object> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(MyController.class)){
                continue;
            }
            String baseUrl = "";
            if(clazz.isAnnotationPresent(MyRequestMapping.class)) {
               baseUrl = clazz.getAnnotation(MyRequestMapping.class).value();
            }

            Method[] methods = clazz.getMethods();
            for(Method m : methods) {
                if(!m.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }

                MyRequestMapping annotation = m.getAnnotation(MyRequestMapping.class);
                String subUrl = annotation.value();
                String url = (baseUrl + "/" + subUrl).replaceAll("/+", "/");
                controllerMap.put(url, ClassUtil.toLoweFirstWord(clazz.getName()));
                handlerMap.put(url, m);
            }
        }
    }

    private void doHandle(String url, HttpServletRequest request, HttpServletResponse response) {
        Method method = handlerMap.get(url);
        //获取方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        //获取请求的参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        //保存参数值
        Object [] paramValues= new Object[parameterTypes.length];
        for(int i=0;i<parameterTypes.length;i++) {
            //根据参数名称，做某些处理
            String requestParam = parameterTypes[i].getSimpleName();


            if (requestParam.equals("HttpServletRequest")){
                //参数类型已明确，这边强转类型
                paramValues[i] = request;
                continue;
            }
            if (requestParam.equals("HttpServletResponse")){
                paramValues[i] = response;
                continue;
            }
            if(requestParam.equals("String")){
                for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                    String value =Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
                    paramValues[i]=value;
                }
            }

        }

        try {
            method.invoke(ioc.get(controllerMap.get(url)), paramValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

//    private void initHandlerAdapter() {
//
//    }
}
