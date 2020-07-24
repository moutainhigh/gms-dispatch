package com.baseboot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;


@SpringBootApplication
@Order(value= Ordered.HIGHEST_PRECEDENCE)
public class BaseBootApplication implements CommandLineRunner {

    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("          base system start:0.0.1              ");
        System.out.println("===============================================");


        /*System.setProperty("REDIS_SERVER_HOST","192.168.2.126");
        System.setProperty("RABBITMQ_SERVER_HOST","192.168.2.126");*/

        System.setProperty("REDIS_SERVER_HOST","192.168.43.232");
        System.setProperty("RABBITMQ_SERVER_HOST","192.168.43.232");

        /*System.setProperty("REDIS_SERVER_HOST","192.168.143.232");
        System.setProperty("RABBITMQ_SERVER_HOST","192.168.143.232");*/

        System.setProperty("SERVER_PWD","123456");
        SpringApplication.run(BaseBootApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        //Thread.currentThread().join();
    }
}
