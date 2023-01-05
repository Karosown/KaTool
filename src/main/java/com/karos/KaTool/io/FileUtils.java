/**
 * Title
 *
 * @ClassName: FileUtils
 * @Description:
 * @author: Karos
 * @date: 2023/1/2 1:52
 * @Blog: https://www.wzl1.top/
 */

package com.karos.KaTool.io;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class FileUtils {
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
                return tempFile;
            } catch (IOException e) {
                if (num++>50)throw new RuntimeException(e);
            }
        }
    }
}
