package com.dinghz.dumper;

import com.dinghz.dumper.core.Config;
import com.dinghz.dumper.core.Dumper;
import com.dinghz.dumper.http.HttpClient;
import com.dinghz.dumper.http.HttpServer;
import com.dinghz.dumper.tcp.TcpClient;
import com.dinghz.dumper.tcp.TcpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BootStrap
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class BootStrap {
    static Logger logger;

    public static void main(String[] args) {
        if (args == null || args.length != 3) {
            help();

            return;
        }

        Config.REMOTE_HOST = args[0];
        Config.REMOTE_PORT = Integer.valueOf(args[1]);
        Config.LOCAL_PORT = Integer.valueOf(args[2]);

        String logDir = System.getProperty("log.dir");
        if (logDir == null) {
            System.setProperty("log.dir", "./logs");
        }

        String proName = System.getProperty("pro.name");
        if (proName == null) {
            System.setProperty("pro.name", "" + Config.LOCAL_PORT);
        }

        logger = LoggerFactory.getLogger(BootStrap.class);

        logger.info("config: enable.log = {}, enable.ssl = {}, protocol.type = {}, remote.ip = {}, remote.port = {}, local.port = {}", Config.LOG, Config.SSL, Config.PROTOCOL_TYPE, Config.REMOTE_HOST, Config.REMOTE_PORT, Config.LOCAL_PORT);

        Dumper client, server;

        if ("http".equals(Config.PROTOCOL_TYPE)) {
            client = HttpClient.instance();
            server = HttpServer.instance();
        } else if ("tcp".equals(Config.PROTOCOL_TYPE)) {
            client = TcpClient.instance();
            server = TcpServer.instance();
        } else {
            help();

            return;
        }

        start(client);
        start(server);
    }

    private static void help() {
        String help = "usage: java -Dlog.dir=./logs -Dpro.name=8080 -Denable.log=false -Denable.ssl=false -Dprotocol.type=tcp -jar dumper-jar-with-dependencies.jar <remote.ip> <remote.port> <local.port>";

        if (logger != null) {
            logger.error(help);
        } else {
            System.err.println(help);
        }
    }

    private static void start(Dumper client) {
        new Thread(() -> {
            try {
                client.start();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }).start();
    }

}
