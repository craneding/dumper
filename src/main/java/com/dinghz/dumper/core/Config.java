package com.dinghz.dumper.core;

/**
 * Config
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class Config {

    public static final boolean LOG = Boolean.valueOf(System.getProperty("enable.log", "false"));

    public static final boolean SSL = Boolean.valueOf(System.getProperty("enable.ssl", "false"));

    public static final String PROTOCOL_TYPE = System.getProperty("protocol.type", "tcp");

    public static Integer LOCAL_PORT;
    public static String REMOTE_HOST;
    public static Integer REMOTE_PORT;

}
