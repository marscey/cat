package com.xiaof.framework.config.druid;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 数据库连接配置
 *
 * @author Chaoyun.Yep
 */
@Configuration
@Slf4j
public class DataSourceConfig {
    @Resource
    private DruidConfig druidConfig;

    // 如果不是 druid 的 datasource, 直接用下面这个方式就可以, 但是 SpringBoot 默认还不支持 druid
    //    @Bean(name = "hkb2bDataSource")
    //    @ConfigurationProperties(prefix = "datasource.hkb2b")
    //    public DataSource hkb2bDataSource() {
    //                return DataSourceBuilder.create().build();
    //    }

    @Bean(name = "dataSource")
    public DataSource dataSource() {
        DruidDataSource datasource = new DruidDataSource();

        datasource.setUrl(druidConfig.getUrl());
        try {
            datasource.setUsername(druidConfig.getUsername());
            datasource.setPassword(druidConfig.getPassword());
        } catch (Exception e) {
            log.error("database user password decrypt", e);
            //e.printStackTrace();
        }
        datasource.setDriverClassName(druidConfig.getDriverClassName());

        datasource.setInitialSize(druidConfig.getInitialSize());
        datasource.setMinIdle(druidConfig.getMinIdle());
        datasource.setMaxActive(druidConfig.getMaxActive());
        datasource.setMaxWait(druidConfig.getMaxWait());
        datasource.setTimeBetweenEvictionRunsMillis(druidConfig.getTimeBetweenEvictionRunsMillis());
        datasource.setMinEvictableIdleTimeMillis(druidConfig.getMinEvictableIdleTimeMillis());
        datasource.setValidationQuery(druidConfig.getValidationQuery());
        datasource.setTestWhileIdle(druidConfig.isTestWhileIdle());
        datasource.setTestOnBorrow(druidConfig.isTestOnBorrow());
        datasource.setTestOnReturn(druidConfig.isTestOnReturn());
        datasource.setPoolPreparedStatements(druidConfig.isPoolPreparedStatements());
        datasource.setMaxPoolPreparedStatementPerConnectionSize(
                druidConfig.getMaxPoolPreparedStatementPerConnectionSize());

        try {
            datasource.setFilters(druidConfig.getFilters());
        } catch (SQLException e) {
            log.error("druid configuration initialization filter", e);
        }
        datasource.setConnectionProperties(druidConfig.getConnectionProperties());

        return datasource;

    }

//    @Primary
//    @Bean(name = "demoJdbcTemplate")
//    public NamedParameterJdbcTemplate demoJdbcTemplate() {
//        return new NamedParameterJdbcTemplate(demoDataSource());
//    }

    @Configuration
    @ConfigurationProperties(prefix = "spring.datasource")
    @Data
    public class DruidConfig {
        // http://www.voidcn.com/blog/blueheart20/article/p-6181465.html
        // http://my.oschina.net/letao/blog/518012
        // https://github.com/alibaba/druid/wiki/配置_DruidDataSource参考配置
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        private int initialSize;
        private int minIdle;
        private int maxActive;
        private int maxWait;
        private int timeBetweenEvictionRunsMillis;
        private int minEvictableIdleTimeMillis;
        private String validationQuery;
        private boolean testWhileIdle;
        private boolean testOnBorrow;
        private boolean testOnReturn;
        private boolean poolPreparedStatements;
        private int maxPoolPreparedStatementPerConnectionSize;
        private String filters;
        private String connectionProperties;
    }
}