package cn.zjdf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling//开启定时任务
public class Covid19ProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(Covid19ProjectApplication.class, args);
    }
}