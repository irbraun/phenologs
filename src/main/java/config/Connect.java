/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author irbraun
 */
public class Connect {
    
    public static Connection conn;
    
    public Connect() throws SQLException{
        String connectionStr = String.format("jdbc:sqlite:%s",Config.connPath);
        conn = (Connection) DriverManager.getConnection(connectionStr);
    }
    
}
