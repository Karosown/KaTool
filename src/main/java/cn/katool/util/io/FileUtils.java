/**
 * Title
 *
 * @ClassName: FileUtils
 * @Description:
 * @author: Karos
 * @date: 2023/1/2 1:52
 * @Blog: https://www.wzl1.top/
 */

package cn.katool.util.io;

import cn.hutool.http.HttpUtil;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

@Component
@Slf4j
public class FileUtils {
    public static File downloadFile(String urlPath, String downloadDir,String fileName) {
        File file = null;
        try {

            // 统一资源
            URL url = new URL(urlPath);
            // 连接类的父类，抽象类
            URLConnection urlConnection = url.openConnection();
            // http的连接类
            int fileLength;
            BufferedInputStream bin = null;

            if (HttpUtil.isHttp(urlPath)) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                //设置超时
                httpURLConnection.setConnectTimeout(5*60*1000);
                httpURLConnection.setReadTimeout(5*60*1000);
                //设置请求方式，默认是GET
                httpURLConnection.setRequestMethod("POST");
                // 设置字符编码
                httpURLConnection.setRequestProperty("Charset", "UTF-8");
                // 打开到此 URL引用的资源的通信链接（如果尚未建立这样的连接）。
                httpURLConnection.connect();
                // 文件大小
                fileLength = httpURLConnection.getContentLength();
                bin = new BufferedInputStream(httpURLConnection.getInputStream());

            }

            else if (HttpUtil.isHttps(urlPath)){
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) urlConnection;
                //设置超时
                httpsURLConnection.setConnectTimeout(5*60*1000);
                //设置请求方式，默认是GET
                httpsURLConnection.setRequestMethod("POST");
                // 设置字符编码
                httpsURLConnection.setRequestProperty("Charset", "UTF-8");
                // 打开到此 URL引用的资源的通信链接（如果尚未建立这样的连接）。
                httpsURLConnection.connect();
                // 文件大小
                fileLength = httpsURLConnection.getContentLength();
                bin = new BufferedInputStream(httpsURLConnection.getInputStream());

            }
            else {
                throw new KaToolException(ErrorCode.PARAMS_ERROR,"请传入http/https的URL链接");
            }

            // 建立链接从请求中获取数据
            URLConnection con = url.openConnection();
            // 指定文件名称(有需求可以自定义)
            // 指定存放位置(有需求可以自定义)
            String path = downloadDir + File.separatorChar + fileName;
            file = new File(path);
            // 校验文件夹目录是否存在，不存在就创建一个目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            OutputStream out = new FileOutputStream(file);
            int size = 0;
            int len = 0;
            byte[] buf = new byte[2048];
            while ((size = bin.read(buf)) != -1) {
                len += size;
                out.write(buf, 0, size);
            }
            // 关闭资源
            bin.close();
            out.close();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("文件下载失败！");
        } finally {
            return file;
        }

    }
    public static File createTempFile(){
        return createTempFile(true);
    }
    public static File createTempFile(boolean isReCreate){
        int num=0;
        while (true) {
            try {
                File tempFile = File.createTempFile("Katool", (String) null, (File) null).getCanonicalFile();
                if (isReCreate==true){
                   tempFile.delete();
                   tempFile.createNewFile();
                }
                log.info("【KaTool::FileUtils】 =>  Info: 临时文件创建成功");
                return tempFile;
            } catch (IOException e) {
                log.warn("【KaTool::FileUtils】 =>  Warn: 临时文件创建重试");
                if (num++>50){
                    log.error("【KaTool::FileUtils】 =>  Error: 临时文件创建失败");
                    throw new KaToolException(ErrorCode.FILE_ERROR);
                }
            }
        }
    }
}
