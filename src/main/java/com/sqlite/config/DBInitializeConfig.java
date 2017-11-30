// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.config;

import javax.annotation.PostConstruct;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import com.sqlite.domain.ContextManager;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DBInitializeConfig
{
    @Autowired
    private DataSource dataSource;
    
    @PostConstruct
    public void initialize() {
        try {
            final Connection connection = this.dataSource.getConnection();
            final Statement statement = connection.createStatement();
            final String dateStr = ContextManager.dataStr;
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS pass_inspects_" + dateStr + "(" + "seq INTEGER, " + "is_leading bit, " + "tid varchar(130)," + "epc varchar(130)," + "epc_multiple varchar(255)," + "time varchar(130), " + "status varchar(130)," + "order_num varchar(130))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS [order_stat] (duplicate_count INTEGER, leading_count INTEGER, pieces_count INTEGER, read_multiple_count INTEGER, sku_count INTEGER, good_count INTEGER, un_encoded_count INTEGER, unreadable_count INTEGER, wrong_count INTEGER, code_type varchar(130),order_num varchar(130))");
            statement.close();
            connection.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
