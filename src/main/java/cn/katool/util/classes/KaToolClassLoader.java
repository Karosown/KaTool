package cn.katool.util.classes;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.util.io.FileUtils;
import com.alibaba.excel.util.StringUtils;
import org.aspectj.apache.bcel.util.ClassPath;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class KaToolClassLoader extends ClassLoader{
    private String classPath;


    //    F:/demo1/
    //    完整路径为：F:/demo1/server/MyServlet.class
    public KaToolClassLoader(String classPath){
        this.classPath = classPath;
    }

    public KaToolClassLoader(String classPath, ClassLoader privateLoader){
        super(privateLoader);
        this.classPath = classPath;
    }
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass("",name);
    }


    protected Class<?> findClass(String packageName,String name) throws ClassNotFoundException {
        return findClass(packageName,name,true);
    }

    protected Class<?> findClass(String packageName,String name, boolean isThrow)  {
        byte[] bytes;
        Class<?> clazz;
        try{
            bytes = getClassByte(packageName,name);
            //继承ClassLoader是为了用defineClass方法。
            clazz = defineClass(packageName+"."+name, bytes, 0, bytes.length);
        }catch (Exception e){
            throw new KaToolException(ErrorCode.FILE_ERROR, Arrays.toString(e.getStackTrace()));
        }
        return clazz;
    }

    private byte[] getClassByte(String packageName,String name){
        String realPath = this.classPath;
        File f;

        if (HttpUtil.isHttp(realPath)||HttpUtil.isHttps(realPath)){
            String path = packageName.replace(".", "/");
            f = FileUtils.downloadFile(realPath, this.getClass().getClassLoader().getResource("").getPath() +path,name+".class");
        }
        else {
            f = new File(realPath);
        }
        if(f.exists()){
            byte[] bytes = new byte[0];

            try {
                FileInputStream fis = new FileInputStream(f);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[1024];
                while ((len = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                bytes = baos.toByteArray();
                fis.close();
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return  bytes;
        }else{
            return null;
        }
    }
}