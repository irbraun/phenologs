package unused;


import config.Config;
import config.Connect;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class BNLPOutputReader {

    
    
    public BNLPOutputReader() throws SQLException, SAXException, ParserConfigurationException, IOException{
                
        Statement stmt = Connect.conn.createStatement();
        String dropTableIfExists;
        String createTable;
        
        // Create tables to hold the character annotations and biological entity annotations.
        dropTableIfExists = "DROP TABLE IF EXISTS " + Config.tempCharTable;
        stmt.execute(dropTableIfExists);
        createTable = "CREATE TABLE " + Config.tempCharTable + " (chunk_id TEXT, species TEXT, atom_chunk_num TEXT, atom_num INT, chunk_num INT, chunk TEXT, char_name TEXT, char_value TEXT, char_constraint TEXT, char_modifier TEXT);";
        stmt.execute(createTable);
        
        dropTableIfExists = "DROP TABLE IF EXISTS " + Config.tempEntTable;
        stmt.execute(dropTableIfExists);
        createTable = "CREATE TABLE " + Config.tempEntTable + " (chunk_id TEXT, species TEXT, atom_chunk_num TEXT, atom_num INT, chunk_num INT, chunk TEXT, ent_name TEXT, ent_name_original TEXT, ent_type TEXT, ent_constraint TEXT);";
        stmt.execute(createTable);
        
        // Loop through and parse all the output xml files.
        File folder = new File(Config.charparOutputPath);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles){
            if (file.isFile() && !file.getName().startsWith(".")){
                parseXML(file);
            }
        }
        

        // Update the character annotations table using the conversions table.
        dropTableIfExists = "DROP TABLE IF EXISTS " + Config.annotCharTable;
        stmt.execute(dropTableIfExists);
        String updateStr = "CREATE TABLE " + Config.annotCharTable + " AS SELECT " +
                Config.tempCharTable + ".chunk_id," +
                Config.tempCharTable + ".species," +
                Config.tempCharTable + ".atom_chunk_num," +
                Config.tempCharTable + ".atom_num," +
                Config.tempCharTable + ".chunk_num," +
                Config.tempCharTable + ".chunk," +
                Config.tempCharTable + ".char_name," +
                Config.tempCharTable + ".char_value," +
                Config.tempCharTable + ".char_constraint," +
                Config.tempCharTable + ".char_modifier," +
                Config.convTable + ".ppn_id " +
                "FROM " + Config.tempCharTable + " INNER JOIN " + Config.convTable + " ON " + 
                Config.tempCharTable + ".species=" + Config.convTable + ".species AND " +
                Config.tempCharTable + ".atom_num=" + Config.convTable + ".atom_num;";
        stmt.execute(updateStr);
        
        // Remove the temporary table.
        String removeTempStr = "DROP TABLE IF EXISTS " + Config.tempCharTable;
        stmt.execute(removeTempStr);
        
        
        // Update the biological entity annotations table using the conversions table.
        dropTableIfExists = "DROP TABLE IF EXISTS " + Config.annotEntTable;
        stmt.execute(dropTableIfExists);
        updateStr = "CREATE TABLE " + Config.annotEntTable + " AS SELECT " +
                Config.tempEntTable + ".chunk_id," +
                Config.tempEntTable + ".species," +
                Config.tempEntTable + ".atom_chunk_num," +
                Config.tempEntTable + ".atom_num," +
                Config.tempEntTable + ".chunk_num," +
                Config.tempEntTable + ".chunk," +
                Config.tempEntTable + ".ent_name," +
                Config.tempEntTable + ".ent_name_original," +
                Config.tempEntTable + ".ent_type," +
                Config.tempEntTable + ".ent_constraint," +
                Config.convTable + ".ppn_id " +
                "FROM " + Config.tempEntTable + " INNER JOIN " + Config.convTable + " ON " + 
                Config.tempEntTable + ".species=" + Config.convTable + ".species AND " +
                Config.tempEntTable + ".atom_num=" + Config.convTable + ".atom_num;";
        stmt.execute(updateStr);
        
        // Remove the temporary table.
        removeTempStr = "DROP TABLE IF EXISTS " + Config.tempEntTable;
        stmt.execute(removeTempStr);
        
        
    }
        
        

    
    private void parseXML(File file) throws SAXException, ParserConfigurationException, IOException, SQLException{
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        
        NodeList taxonList = doc.getElementsByTagName("taxon_name");
        Node onlyTaxonName = taxonList.item(0);
        Element taxonNameElement = (Element) onlyTaxonName;
        String speciesName = taxonNameElement.getTextContent();
        
        
        NodeList statementList = doc.getElementsByTagName("statement");
        
        // Loop through all the statements in the file.
        for (int temp = 0; temp < statementList.getLength(); temp++) {
            
            
            Node statementNode = statementList.item(temp);
            if (statementNode.getNodeType() == Node.ELEMENT_NODE) {
                
                Element statementElement = (Element) statementNode;
 
                // ID of the statement.
                String statementID = statementElement.getAttribute("id");
                // statementID is in the form "d$_s$" where the variable after d refers to the atomized statement,
                // and the variable after s refers to the number of this particular chunk that charaparser broke it into, starting at 0.
                
                // Find the atom and chunk ID numbers alone.
                int underScorePos = statementID.indexOf("_");
                String atomIDStr = statementID.substring(1, underScorePos);  ///account for d
                int atomNum = Integer.parseInt(atomIDStr);
                String chunkIDStr = statementID.substring(underScorePos+2);  //account for _s
                int chunkNum = Integer.parseInt(chunkIDStr);
               
                // Text of the statement.
                String chunkText = statementElement.getElementsByTagName("text").item(0).getTextContent();
                
                
                // If the attribute doesn't exist for that string, getAttributes() returns "" not null.
                

                
                
                // All Biological Entities
                NodeList bioEntityList = statementElement.getElementsByTagName("biological_entity");
                for (int i=0; i<bioEntityList.getLength(); i++){
                    Node bioEntityNode = bioEntityList.item(i);
                    Element bioEntityElement = (Element) bioEntityNode;
                    
                    //String id = bioEntityElement.getAttribute("id");
                    String name = bioEntityElement.getAttribute("name");
                    String nameOriginal = bioEntityElement.getAttribute("name_original");
                    String type = bioEntityElement.getAttribute("type");
                    String constraint = bioEntityElement.getAttribute("constraint");
                    
                    String chunkID = Integer.toString(speciesName.hashCode())+statementID;
                    
                    Object[] data = {Config.tempEntTable,"chunk_id","species","atom_chunk_num","atom_num","chunk_num","chunk","ent_name","ent_name_original","ent_type","ent_constraint",chunkID,speciesName,statementID,atomNum,chunkNum,chunkText,name,nameOriginal,type,constraint};
                    String stmtStr = String.format("INSERT INTO %s (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES (\"%s\",\"%s\",\"%s\",%d,%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\");", data);
                    Statement insertStmt = Connect.conn.createStatement();
                    insertStmt.execute(stmtStr);
                }
                
                
                // All Characters
                NodeList characterList = statementElement.getElementsByTagName("character");
                for (int i=0; i<characterList.getLength(); i++){
                    Node characterNode = characterList.item(i);
                    Element characterElement = (Element) characterNode;
                    
                    String name = characterElement.getAttribute("name");
                    String value = characterElement.getAttribute("value");
                    String constraint = characterElement.getAttribute("constraint");
                    String modifier = characterElement.getAttribute("modifier");
                    
                    // Arbitrary manipulation of species name and statement ID, used for iterating.
                    String chunkID = Integer.toString(speciesName.hashCode())+statementID;
                    
                    // Add the contents to the database.
                    Object[] data = {Config.tempCharTable,"chunk_id","species","atom_chunk_num","atom_num","chunk_num","chunk","char_name","char_value","char_constraint","char_modifier",chunkID,speciesName,statementID,atomNum,chunkNum,chunkText,name,value,constraint,modifier};
                    String stmtStr = String.format("INSERT INTO %s (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES (\"%s\",\"%s\",\"%s\",%d,%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\");", data);
                    Statement insertStmt = Connect.conn.createStatement();
                    insertStmt.execute(stmtStr);
                }
                
                
                
                // TODO
                // All Relations
                // ...

                
            }

        }
        
    }
    
    
     
    
    
}

