
package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Connect {
    
    public static Connection conn;
    
    public Connect() throws SQLException{
        String connectionStr = String.format("jdbc:sqlite:%s",Config.connPath);
        conn = (Connection) DriverManager.getConnection(connectionStr);
    }
    
}
