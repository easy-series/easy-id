package com.leaf.starter.common;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 工具类
 */
@Slf4j
public class Utils {
    /**
     * 获取本机IP
     *
     * @return 本机IP
     */
    public static String getIp() {
        String ip;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && !address.isLinkLocalAddress() && address.isSiteLocalAddress()) {
                        ip = address.getHostAddress();
                        log.info("获取到本机IP: {}", ip);
                        return ip;
                    }
                }
            }
            
            // 如果没有找到合适的IP，则使用本地回环地址
            log.warn("未找到合适的IP地址，将使用本地回环地址");
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.error("获取本机IP失败", e);
            return "127.0.0.1";
        }
    }
    
    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 当前时间戳
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }
} 