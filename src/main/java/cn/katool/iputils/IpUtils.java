/**
 * Title
 *
 * @ClassName: IpUtils
 * @Description:
 * @author: Karos
 * @date: 2022/12/18 22:41
 * @Blog: https://www.wzl1.top/
 */

package cn.katool.iputils;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
@Component
public class IpUtils {
    /**
     * 获取反向代理IP
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
