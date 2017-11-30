// 
// Decompiled by Procyon v0.5.30
// 

package boot;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

public class SQLiteJDBCDriverConnection
{
    public static void connect() {
        Connection conn = null;
        try {
            final String url = "jdbc:sqlite:C:/zd2.db";
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            try {
                if (conn != null) {
                    conn.close();
                }
            }
            catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            }
            catch (SQLException ex2) {
                System.out.println(ex2.getMessage());
            }
        }
    }
    
    public static void main(final String[] args) {
        connect();
    }
}
