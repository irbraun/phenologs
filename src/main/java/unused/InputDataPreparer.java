package unused;


import config.Config;
import config.Connect;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;


public class InputDataPreparer {
    
    
    public InputDataPreparer() throws SQLException, SAXException, ParserConfigurationException, IOException{
        
        // Create the conversion table so that annotations can be mapped to the original data.
        Statement stmt = Connect.conn.createStatement();
        String attemptDropTable = "DROP TABLE IF EXISTS " + Config.convTable;
        stmt.execute(attemptDropTable);
        String attemptCreateTable = "CREATE TABLE IF NOT EXISTS " + Config.convTable + " (species TEXT, ppn_id INT, atom_num INT);";
        stmt.execute(attemptCreateTable);
    }
    
    
    // Find all the atomized statements in the database of the tagged dataset and write them to a file.
    // This is done in preparation for making them easier to enter into Bio NLP annotation program.
    public void findAndWriteAtoms() throws SQLException, FileNotFoundException, UnsupportedEncodingException{
        
        // Get a list of different species present in the original data.
        String speciesStmtStr = String.format("SELECT DISTINCT Species FROM %s;", Config.dataTable);
        Statement speciesSelectStmt = Connect.conn.createStatement();
        speciesSelectStmt.execute(speciesStmtStr);
        ResultSet speciesResultSet = speciesSelectStmt.getResultSet();
        List speciesNames = new ArrayList<>();
        while (speciesResultSet.next()){
            speciesNames.add(speciesResultSet.getString("Species").trim());
        }
        
        // Loop through each species name.
        for (String speciesName : (ArrayList<String>) speciesNames){

            File outputFile = new File(String.format("%s%s_atomized_statements.txt",Config.charparTextPath,speciesName));
            PrintWriter writer = new PrintWriter(outputFile);
            writer.println(String.format("species name: %s, unspecified", speciesName));
            
            String stmtStr = String.format("SELECT ppn_id,atomized_statement FROM %s WHERE Species=\"%s\";", Config.dataTable, speciesName);
            Statement selectStmt = Connect.conn.createStatement();
            selectStmt.execute(stmtStr);

            ResultSet resultSet = selectStmt.getResultSet();

            int atomNumber = 0;
            while(resultSet.next()){
                
                int uniqueID = Integer.parseInt(resultSet.getString("ppn_id").trim());
                
                Object[] data = {Config.convTable, "species", "ppn_id", "atom_num", speciesName, uniqueID, atomNumber};    
                String insertStmtStr = String.format("INSERT INTO %s (%s,%s,%s) VALUES (\"%s\",%d,%d);", data);
                Statement insertStmt = Connect.conn.createStatement();
                insertStmt.execute(insertStmtStr);
                
                String atomizedStmt = resultSet.getString("atomized_statement").trim();
                writer.println("<description type=\"morphology\">" + atomizedStmt + "</description>");
                
                atomNumber++;
            }
            writer.close();
        }    
    }
    
}
