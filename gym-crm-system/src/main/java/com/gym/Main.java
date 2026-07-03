package com.gym;

import com.gym.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
public class Main {
    public static void main(String[] args) {
        log.info("Starting App");

        var context = new AnnotationConfigApplicationContext(AppConfig.class);

        context.close();

        log.info("App finished");
    }
}
