package com.spark;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/1/25 10:21
 * .............................................
 * .............................................
 * 佛祖保佑             永无BUG
 */
// 指定dao层位置
@MapperScan("com.spark.dao")
// 启动一个后台任务调度器，用于管理定时任务
@EnableScheduling
// 启动任务调度器异步执行，每次每个任务占用一个单独的线程
@EnableAsync
@SpringBootApplication
public class SparkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SparkApplication.class, args);
        System.out.println("\n" +
                " * +++/\\_/\\\n" +
                " * + ( °w° )=\n" +
                " * +++)   (  //\n" +
                " * + (__ __)//   星火云服务端启动成功 \n" +
                "\n");
    }

}
