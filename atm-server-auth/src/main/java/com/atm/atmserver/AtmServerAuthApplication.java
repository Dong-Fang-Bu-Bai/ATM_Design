package com.atm.atmserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

// 扫描 com.atm.atmserver.mapper 包下的所有 Mapper 接口
@MapperScan("com.atm.atmserver.mapper")

@SpringBootApplication
public class AtmServerAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AtmServerAuthApplication.class, args);
	}

}
