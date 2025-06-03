package io.zabbixplus.framework.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"io.zabbixplus.framework.ui", "io.zabbixplus.framework.core"})
public class MainUIApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainUIApplication.class, args);
    }
}
