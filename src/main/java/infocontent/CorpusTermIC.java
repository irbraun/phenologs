/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package infocontent;

import enums.Ontology;
import config.Config;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static main.Main.logger;
import ontology.Onto;


//TODO: rewrite this to use the new Text object design, much simpler, everything should already be built-in for doing this.



/**
 *
 * @author irbraun
 */
class CorpusTermIC {
    
    private HashMap<String,Integer> counts;
    private int countsSum;
    private HashMap<Ontology,Onto> ontoObjects;

    
    CorpusTermIC(HashMap<Ontology,Onto> ontoObjects) throws SQLException{
        
        this.ontoObjects = ontoObjects;
        
        logger.info("finding ontolgoy term frequencies in data");
        String relevantColumns = "quality_ID, PATO_Qualifier_ID_optional, primary_entity1_ID, primary_entity2_ID_optional, secondary_entity1_ID_optional, secondary_entity2_ID_optional, developmental_stage_ID_optional";
        Object[] data = {relevantColumns, Config.dataTable};
        ResultSet rs = utils.Util.sqliteCall(String.format("SELECT %s FROM %s", data));
        
        counts = new HashMap<>();
        
        while (rs.next()){

            // Retrieve those ontology IDs, unused components will be blank strings.
            String primaryEntityID1 = rs.getString("primary_entity1_ID").replace(":", "_").trim();
            String primaryEntityID2 = rs.getString("primary_entity2_ID_optional").replace(":", "_").trim();
            String qualityID = rs.getString("quality_ID").replace(":", "_").trim();
            String qualifierID = rs.getString("PATO_Qualifier_ID_optional").replace(":", "_").trim();
            String secondaryEntityID1 = rs.getString("secondary_entity1_ID_optional").replace(":", "_").trim();
            String secondaryEntityID2 = rs.getString("secondary_entity2_ID_optional").replace(":", "_").trim();
            String developmentalStageID = rs.getString("developmental_stage_ID_optional").replace(":", "_").trim();
            String[] components = {primaryEntityID1,primaryEntityID2,qualityID,qualifierID,secondaryEntityID1,secondaryEntityID2,developmentalStageID};
            
            // Update counts.
            for (String termID: components){
                if (!termID.equals("")){
                    // Found a valid term ID, grab all other terms it inherits.
                    List<String> inheritedTerms = new ArrayList<>();
                    
                    // Try to add all the inherited nodes, works if this term comes from a supported ontology.
                    try{
                        inheritedTerms.addAll(ontoObjects.get(utils.Util.inferOntology(termID)).getTermFromTermID(termID).inheritedNodes);
                    }
                    catch(NullPointerException e){
                    }
                    // Add this term itself no matter what.
                    inheritedTerms.add(termID);
                    
                    for (String innerTermID: inheritedTerms){
                        if (counts.containsKey(innerTermID)){
                            counts.put(innerTermID, counts.get(innerTermID)+1);
                        }
                        else {
                            counts.put(innerTermID, 1);
                        }
                    }
                }
            }
        }
        
        countsSum = 0;
        for (Integer count: counts.values()){
            countsSum += count;
        }
        
    }
    

    double getIC(String termID){
        double freqHat;
        int numAvailableTerms = ontoObjects.get(utils.Util.inferOntology(termID)).getTermListSize();
        if (counts.containsKey(termID)){
            freqHat = (double) counts.get(termID) / (double) countsSum;
        }
        else {
            freqHat = (double) 1.000 / (double) numAvailableTerms;
        }
        return (double) -1.000 * (double) Math.log(freqHat);
    }
    
}
