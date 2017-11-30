// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import javax.sql.DataSource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DBConfig
{
    @Bean
    public DataSource dataSource() {
        final DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.sqlite.JDBC");
        dataSourceBuilder.url("jdbc:sqlite:mydb.db");
        return dataSourceBuilder.build();
    }
}
