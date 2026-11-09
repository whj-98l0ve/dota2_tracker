package com.whj_98l0ve.dota2_tracker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 开启定时轮询解析任务
@MapperScan("com.whj_98l0ve.dota2_tracker.mapper") // 扫描你的 mapper 接口包
public class Dota2TrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(Dota2TrackerApplication.class, args);
	}
}