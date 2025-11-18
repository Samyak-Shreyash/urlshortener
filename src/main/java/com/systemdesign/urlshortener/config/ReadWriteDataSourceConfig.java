package com.systemdesign.urlshortener.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Configuration
@PropertySource("classpath:application.properties")
public class ReadWriteDataSourceConfig {

    @Value("${MYSQL_USER}")
    private String mysqlUser;

    @Value("${MYSQL_USER_PASSWORD}")
    private String mysqlPassword;
    
    @Value("${db.master.port}")
    private String dbMasterPort;

    @Value("${db.slave1.port}")
    private String slave1Port;

    @Value("${db.slave2.port}")
    private String slave2Port;

    @Bean
    @Primary
    public DataSource dataSource() {
        ReadWriteRoutingDataSource routingDataSource = new ReadWriteRoutingDataSource();
        
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("WRITE", writeDataSource());
        dataSourceMap.put("READ", readDataSource());
        
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(writeDataSource());
        return routingDataSource;
    }
    
    @Bean
    public DataSource writeDataSource() {        
        return DataSourceBuilder.create()
            .url("jdbc:mysql://mysql_master:"+dbMasterPort+"/url_shortener")
            .username(mysqlUser)
            .password(mysqlPassword)
            .build();
    }
    
    @Bean
    public DataSource readDataSource() {
        // Round-robin between slaves
        return DataSourceBuilder.create()
            .url("jdbc:mysql:loadbalance://mysql_slave1:"+slave1Port+",mysql_slave2:"+slave2Port+"/url_shortener?loadBalanceAutoCommitStatementThreshold=5&loadBalanceHostRemovalGracePeriod=15000&loadBalanceBlacklistTimeout=5000")
            .username(mysqlUser)
            .password(mysqlPassword)
            .build();
    }
}

// Custom routing datasource
class ReadWriteRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly() 
            ? "READ" 
            : "WRITE";
    }
}