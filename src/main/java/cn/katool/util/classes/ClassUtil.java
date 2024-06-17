package cn.katool.util.classes;

import cn.hutool.extra.spring.SpringUtil;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import com.qiniu.util.UrlUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

@Slf4j
@Component
public class ClassUtil{
    // 尝试编译次数
    Integer tryLimit=3;

    ThreadLocal<Integer> threadLocal=new ThreadLocal();
    public Class urlLoader(String url, String packageName,String className){
        Class clazz = null;
        String fileName = className;
        className = packageName+"."+className;
        String resouceUrl = url.replace(
                className.substring(0, className.lastIndexOf('.')).replace('.', '/') + "/",
                "");
        KaToolClassLoader classLoader = new KaToolClassLoader(
                resouceUrl,Thread.currentThread().getContextClassLoader());
        try {
            if ((clazz=classLoader.findClass(packageName,fileName)) != null){
                return clazz;
            }
            String replace = null;
            if (className.contains(".")){
                replace = className.substring(className.lastIndexOf(".")+1);
            }
            else {
                replace = className;
            }
            log.info("url:{}",url);
            String fileDir = (url+ ((url.charAt(url.length()-1)=='/'||url.charAt(url.length()-1)=='\\')?"":"\\")  + replace.concat(".class")).replace("/","\\");
            log.info("class file dir:{}",fileDir);
            File file = new File(fileDir);
            // 如果class文件不存在，Sleep阻塞等待3次，再进行判断
            if (!file.exists()){
                Integer integer = threadLocal.get();
                if (integer ==null|| integer ==0){
                    threadLocal.set(1);
                }
                if (Objects.equals(integer, tryLimit)){
                    log.info("{}加载错误",file.getAbsolutePath());
                    throw new KaToolException(ErrorCode.FILE_ERROR,"加载错误，请检查.class文件是否正确");
                }
                threadLocal.set(integer +1);
                Thread.sleep(10000L/ integer);
                Class aClass = urlLoader(url,packageName, className);
                if (Objects.nonNull(integer)){
                    threadLocal.remove();   // 进行清理，避免后续线程错误
                }
                return aClass;
            }
            clazz = classLoader.findClass(className);
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return clazz;
    }



    public Pair<Boolean,String> complieClass(String souceCodeFilePath,String className){
        if (new File(souceCodeFilePath+"\\"+className+".java").exists()&&new File(souceCodeFilePath+"\\"+className+".class").exists()){
            return new Pair<>(true,"it is had complied");
        }
        String execResult= "";
        OutputStream outputStream = null;
        Pair<Boolean,String> result=null;
        Boolean call = null;
        log.info("【KaTool::ClassUtil::complieClass】complieClass: {} className:{}",souceCodeFilePath,className);
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            System.out.println(compiler.getClass().getName());
            StandardJavaFileManager sjfm = compiler.getStandardFileManager (null, null, null);
            if (!new File(souceCodeFilePath+"\\"+className+".java").exists()){
                if (threadLocal.get()==null||threadLocal.get()==0){
                    threadLocal.set(1);
                }
                if (Objects.equals(threadLocal.get(), tryLimit)){
                    throw new KaToolException(ErrorCode.OPER_ERROR,"【KaTool::ClassUtil::complieClass】编译错误，请检查代码文件是否正确");
                }
                threadLocal.set(threadLocal.get()+1);
                Thread.sleep(10000L/threadLocal.get());
                return complieClass(souceCodeFilePath,className);
            }
            threadLocal.remove();
            Iterable units = sjfm.getJavaFileObjects (souceCodeFilePath+"\\"+className+".java");
            JavaCompiler.CompilationTask ct = compiler.getTask (null, sjfm, null, null, null, units);
            // 动态编译可执行的代码
            call = ct.call();
            sjfm.close();
            while(!new File(souceCodeFilePath+"\\"+className+".class").exists()){
                Thread.sleep(5000L);
            }
            if (null!=call && true == call){

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KaToolException e) {
            throw new RuntimeException(e);
        }
        return new  Pair<>(call,execResult);
    }
    public Class initer(String className){
        Class<?> classTemp;
        try {
            classTemp = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return classTemp;
    }

    public Class loader(String className) {
        Class<?> classTemp;

        try {
            classTemp=ClassLoader.getSystemClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return classTemp;
    }
}