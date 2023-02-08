/**
 * Title
 *
 * @ClassName: expBase64Util
 * @Description:
 * @author: 巫宗霖
 * @date: 2023/2/9 1:12
 * @Blog: https://www.wzl1.top/
 */

package cn.katool.util;

import org.springframework.util.Base64Utils;

public class expBase64Util extends Base64Utils {
    private static String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
    public static Boolean isBase64(String base64){
        if (base64.length()>=11&"data:image/".equals(base64.substring(0,11))){
            base64=base64.substring(base64.indexOf(',')+1);
        }
        return base64.matches(base64);
    }
}
