package org.icij.datashare;

import net.codestory.http.WebServer;
import org.icij.datashare.mode.CommonMode;

import java.util.Properties;

public class WebApp {

    public static void main(String[] args) {
        start(null);
    }

    public static void start(Properties properties) {
        CommonMode mode = CommonMode.create(properties);
        new WebServer()
                .withThreadCount(10)
                .withSelectThreads(2)
                .withWebSocketThreads(1)
                .configure(mode.createWebConfiguration()).start();
    }
}