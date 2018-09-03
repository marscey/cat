package com.xiaof;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;


/**
 * @author Chaoyun.Yip
 * 服务启动入口
 */
// 如果 XXApplication 没有在默认的 com.hna.meeting 下，则需要通过下面的三个 Scan 来设置扫描路径
// @ComponentScan(value = {"com.hna.meeting"})
// @EntityScan(value = {"com.hna.meeting"})
// @EnableJpaRepositories(value = {"com.hna.meeting"}
//@EnableScheduling
//@EnableAsync
@SpringBootApplication
//Mybatis中的 Mapper类上面若配置了@Mapper，则不用指定扫描，反之亦然
//@MapperScan(basePackages = "com.xiaof.repository.mapper")
//druid service方法监控
//@ImportResource(locations={"classpath:config/druid-bean.xml"})
public class UserApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserApplication.class, args);
		//new SpringApplicationBuilder(UserApplication.class).web(true).run(args);
	}
}
