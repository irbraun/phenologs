/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package text;

import composer.EQStatement;
import config.Config;
import enums.Species;
import enums.TextDatatype;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import structure.Chunk;
import static utils.Util.sqliteCall;



/**
 * Class for managing all the information relating to the text data.
 * Text data is read in from the SQLite database and stored by this object which assigns 
 * relevant identifiers and provides methods for relating parts of the data to each other 
 * and accessing it.
 * TODO: Don't need three separate processes that look at the database table, could just 
 * do this once and populate the three types of structures.
 * @author irbraun
 */
public class Text {
    
    // Mapping between different types in the tagged data.
    private final HashMap<Integer,ArrayList<Integer>> phenotypeIDtoAtomIDs;
    private final HashMap<Integer,ArrayList<Integer>> phenotypeIDtoSplitIDs;
    private final HashMap<Integer,Integer> atomIDtoPhenotypeID;
    private final HashMap<Integer,Integer> splitIDtoPhenotypeID;
    private final HashMap<Integer,EQStatement> atomIDtoEQStatement;

    // Lists holding the entirety of data for each type.
    private ArrayList<Chunk> phChunks;
    private ArrayList<Chunk> atChunks;
    private ArrayList<Chunk> spChunks;
    
    // Mapping between the ID field of each chunk and the entire object.
    private final HashMap<Integer,Chunk> phChunkMap;
    private final HashMap<Integer,Chunk> atChunkMap;
    private final HashMap<Integer,Chunk> spChunkMap;
    private final HashMap<Integer,String> atSpeciesMap;
    
    
    
    public Text() throws SQLException{
        // Initialize all the maps.
        phenotypeIDtoAtomIDs = new HashMap<>();
        phenotypeIDtoSplitIDs = new HashMap<>();
        atomIDtoPhenotypeID = new HashMap<>();
        atomIDtoEQStatement = new HashMap<>();
        splitIDtoPhenotypeID = new HashMap<>();
        phChunkMap = new HashMap<>();
        atChunkMap = new HashMap<>();
        spChunkMap = new HashMap<>();
        atSpeciesMap = new HashMap<>();
        // Fill them with the appropriate information from the database.
        populateCuratedEQStmts();
        buildChunks();
    }


    // Retrieve atomized statement IDs given another kind of ID.
    public ArrayList<Integer> getAtomIDsFromPhenotypeID(int phenotypeID){
        return phenotypeIDtoAtomIDs.get(phenotypeID);
    }
    public ArrayList<Integer> getSplitIDsFromPhenotypeID(int phenotypeID){
        return phenotypeIDtoSplitIDs.get(phenotypeID);
    }
    // Retrieve phenotype ID given another kind of ID.
    public int getPhenotypeIDfromAtomID(int atomID){
        return atomIDtoPhenotypeID.get(atomID);
    }
    public int getPhenotypeIDfromSplitPhenotypeID(int spID){
        return splitIDtoPhenotypeID.get(spID);
    }
    
    
    
    
    // Generating the chunk objects for the text types have to be done in this order.
    private void buildChunks() throws SQLException{
        phChunks = buildPhenotypeChunks();
        atChunks = buildAtomChunks();
        spChunks = buildSplitChunks();
        // Put in the maps to be easily accessed from other functions.
        for (Chunk c: phChunks){
            phChunkMap.put(c.chunkID, c);
        }
        for (Chunk c: atChunks){
            atChunkMap.put(c.chunkID, c);
        }
        for(Chunk c: spChunks){
            spChunkMap.put(c.chunkID, c);
        }
    }
    
   
    
    
    // Uses the SQL table and assigns new IDs to phenotype but takes atomizes statement IDs from the table.
    private ArrayList<Chunk> buildPhenotypeChunks() throws SQLException{
        ArrayList<Chunk> chunks = getUntaggedPhenotypes();
        return chunks;
    } 
    
    // Uses the SQL table and takes IDs for each atomized statement directly from there.
    private ArrayList<Chunk> buildAtomChunks() throws SQLException{
        ArrayList<Chunk> chunks = getUntaggedAtoms();
        return chunks;
    } 

    // Split the phenotype descriptions by semicolons and periods and give each a unique ID.
    private ArrayList<Chunk> buildSplitChunks() throws SQLException{
        ArrayList<Chunk> chunks = new ArrayList<>();
        int splitID=1;
        for (Chunk c: phChunks){
            ArrayList<Integer> splitChunkIDsForThisPhenotype = new ArrayList<>();
            String phenotype_desc = c.getRawText();
            for (String s1: phenotype_desc.split("\\.")){
                for (String s2: s1.split(";")){
                    Chunk spChunk = new Chunk(splitID, TextDatatype.SPLIT_PHENOTYPE, s2.trim());
                    chunks.add(spChunk);
                    splitIDtoPhenotypeID.put(splitID, c.chunkID);
                    splitChunkIDsForThisPhenotype.add(spChunk.chunkID);
                    splitID++;
                }
            }
            phenotypeIDtoSplitIDs.put(splitID, splitChunkIDsForThisPhenotype);
        }
        return chunks;
    }     

    
    
    
    public ArrayList<Chunk> getChunksOfKind(String choice) throws SQLException{
        return getChunksOfKind(utils.Util.inferTextType(choice));
    }
    public ArrayList<Chunk> getChunksOfKind(TextDatatype choice) throws SQLException{
        switch(choice){
        case PHENE: 
            return getAllAtomChunks();
        case PHENOTYPE: 
            return getAllPhenotypeChunks();
        default:
            return null;
        }
    }
    
    public ArrayList<Chunk> getChunksOfKindAndSpecies(TextDatatype choice, Species species) throws SQLException{
        ArrayList<Chunk> chunks = new ArrayList<>();
        switch(choice){
        case PHENE:
            for (Chunk c: getAllAtomChunks()){
                if (c.species.equals(species)){
                    chunks.add(c);
                }
            }
            break;
        case PHENOTYPE:
            for (Chunk c: getAllPhenotypeChunks()){
                if (c.species.equals(species)){
                    chunks.add(c);
                }
            }
            break;
        }
        return chunks;
    }
    
    
    
    
    
    public ArrayList<Chunk> getAllPhenotypeChunks() throws SQLException{
        return phChunks;
    }
    
    public ArrayList<Chunk> getAllAtomChunks() throws SQLException{
        return atChunks;
    }
    
    public ArrayList<Chunk> getAllSplitPhenotypeChunks(){
        return spChunks;
    }
    
    public ArrayList<Chunk> getAllChunksOfDType(String dtype_str) throws Exception{
        TextDatatype dtype = utils.Util.inferTextType(dtype_str);
        switch(dtype){
            case PHENOTYPE:
                return phChunks;
            case PHENE:
                return atChunks;
            case SPLIT_PHENOTYPE:
                return spChunks;
            default:
                throw new Exception();
        }    
    }
    
    
    
    
    
    // not used, to be deleted.
    public ArrayList<Chunk> getAllAtomChunksNoDuplicates() throws SQLException{
        ArrayList<Chunk> chunks = atChunks;
        List<Chunk> uniqueChunks = new ArrayList<>();
        HashSet instanceHash = new HashSet<>();
        for (Chunk c: chunks){
            
            String curatedEQStmtStr = atomIDtoEQStatement.get(c.chunkID).toIDText();
            String chunkTextStr = getAtomizedStatementStr(c.chunkID);
            Object[] data = {chunkTextStr, curatedEQStmtStr};
            String hashStr = String.format("%s:%s", data);
            int hashInt = hashStr.hashCode();
            if (!instanceHash.contains(hashInt)){
                uniqueChunks.add(c);
                instanceHash.add(hashInt);
            }
        }
        chunks.retainAll(uniqueChunks);
        return chunks;
    }
    
    
    
    
    
    
    
    
    
    
    /* NOTES
    Currently when populating the list of used term IDs and the roles of those terms,
    the duplicates of where a term is used more than once is not removed, so there is the potential
    for a single term T to show up in the list of used terms more than once, and also to be assigned
    to more than one role. 
    
    Currently, the consquence of this is that in the main script:
    1). when checking if the pairing is a match, we're looking for the term.ID in a list that has duplicates, not a problem.
    2). when checking for the corresponding role to the term we're currently looking at in the pair, the role that is found
    is in the corresponding position of the first occurence of that term in the list of term IDs, even if it shows up later
    in the list and that corresponding role is different. This is maybe not a problem.
    */
    
    
    
    
    
    /*
    public List getETermIDs(int id, TextType textType){
        if (textType == TextType.ATOM){
            return getETermIDsAtom(id);
        }
        else if (textType == TextType.PHENOTYPE){
            return getETermIDsPhen(id);
        }
        return null;
    }
    
    public List getQTermIDs(int id, TextType textType){
        if (textType == TextType.ATOM){
            return getQTermIDsAtom(id);
        }
        else if (textType == TextType.PHENOTYPE){
            return getQTermIDsPhen(id);
        }
        return null;
    }
    */
    
    
    public List getAllTermIDs(int id, TextDatatype textType){
        
        switch(textType){
        case PHENE:
            return getAllTermIDsAtom(id);
        case PHENOTYPE:
            return getAllTermIDsPhen(id);
        default:
            return null;
        }
       
    }
    
    /*
    public List getETermRoles(int id, TextType textType){
        if (textType == TextType.ATOM){
            return getETermRolesAtom(id);
        }
        else if (textType == TextType.PHENOTYPE){
            return getETermRolesPhen(id);
        }
        return null;
    }

    public List getQTermRoles(int id, TextType textType){
        if (textType == TextType.ATOM){
            return getQTermRolesAtom(id);
        }
        else if (textType == TextType.PHENOTYPE){
            return getQTermRolesPhen(id);
        }
        return null;
    }
    
    */
    
    
    
    public List getAllTermRoles(int id, TextDatatype textType){
        switch(textType){
        case PHENE:
            return getAllTermRolesAtom(id);
        case PHENOTYPE:
            return getAllTermRolesPhen(id);
        default:
            return null;
        }
    }
    
    
    
    
    
    private List getAllTermIDsAtom(int atomID){
        EQStatement eqStmt = atomIDtoEQStatement.get(atomID);
        return eqStmt.getAllTermIDs();
    }
    
    
    
    private List getAllTermRolesAtom(int atomID){
        EQStatement eqStmt = atomIDtoEQStatement.get(atomID);
        return eqStmt.getAllTermRoles();
    }
    
    
    private List getAllTermIDsPhen(int phenotypeID){
        ArrayList<Integer> atomIDs = phenotypeIDtoAtomIDs.get(phenotypeID);
        ArrayList<String> allTermsIDs = new ArrayList<>();
        for (int atomID: atomIDs){
            allTermsIDs.addAll(getAllTermIDsAtom(atomID));
        }
        return allTermsIDs;
    }
    
    private List getAllTermRolesPhen(int phenotypeID){
        ArrayList<Integer> atomIDs = phenotypeIDtoAtomIDs.get(phenotypeID);
        ArrayList<String> allTermRoles = new ArrayList<>();
        for (int atomID: atomIDs){
            allTermRoles.addAll(getAllTermRolesAtom(atomID));
        }
        return allTermRoles;
    }

    
    
    
    
    
    
    
    
 
    private void populateCuratedEQStmts() throws SQLException{
        
        String relevantColumns = "ppn_id, quality_ID, PATO_Qualifier_ID_optional, primary_entity1_ID, primary_entity2_ID_optional, secondary_entity1_ID_optional, secondary_entity2_ID_optional, developmental_stage_ID_optional";
        ResultSet rs = sqliteCall(String.format("SELECT %s from %s", relevantColumns, Config.dataTable));
        
        while(rs.next()){
            int atomID = rs.getInt("ppn_id");
            // These term ID's coming from the tagged dataset also need to be converted to the owl ontology format.
            // Retrieve those ontology IDs, unused components will be blank strings.
            String qualityID = rs.getString("quality_ID").replace(":", "_").trim();
            String qualifierID = rs.getString("PATO_Qualifier_ID_optional").replace(":", "_").trim();
            String primaryEntityID1 = rs.getString("primary_entity1_ID").replace(":", "_").trim();
            String primaryEntityID2 = rs.getString("primary_entity2_ID_optional").replace(":", "_").trim();
            String secondaryEntityID1 = rs.getString("secondary_entity1_ID_optional").replace(":", "_").trim();
            String secondaryEntityID2 = rs.getString("secondary_entity2_ID_optional").replace(":", "_").trim();
            String developmentalStageID = rs.getString("developmental_stage_ID_optional").replace(":", "_").trim();
            
            String[] eqStmtComponents = {qualityID, qualifierID, primaryEntityID1, primaryEntityID2, secondaryEntityID1, secondaryEntityID2, developmentalStageID};
            EQStatement eqStmt = new EQStatement(eqStmtComponents);
            atomIDtoEQStatement.put(atomID, eqStmt);
        }  
    }
    
    
    
    
    /* to be deleted.
    // NOTE: rows 327, 541 and 542 just don't have any entities or qualities in them.
    // those should probably just be thrown out in the original file and ignored.
    // need to account for the few cases where UBERON isn't used, check this stuff here.
    // the RE in just one case is not UBERON, account for this.
    private void populateCuratedEQStmtsGS() throws SQLException{
        String relevantColumns = "Unique_ID, Entity, Quality, Related_Entity";
        String dataTable = "gsdata";
        ResultSet rs = sqliteCall(String.format("SELECT %s FROM %s", relevantColumns, dataTable));
        while (rs.next()){
            int atomID = rs.getInt("Unique_ID");
            // These term ID's coming from the gold standard dataset also need to be converted to use the _ instead of :, as is in the owl files.
            // Also using a substring part here to exclude the functional descriptions that are present in this column.
            // This should probably be done with regex instead.
            String entityID="";
            String qualityID="";
            try {
                entityID = rs.getString("Entity").substring(0, 14).replace(":", "_").trim();
                qualityID = rs.getString("Quality").substring(0, 12).replace(":", "_").trim();
            }
            catch(Exception e){
                System.out.println("entity"+ atomID + " = " + rs.getString("Entity"));
                System.out.println("quality"+ atomID + " = " + rs.getString("Quality"));
            }
            String relatedEntityID = rs.getString("Related_Entity").replace(":", "_").trim();
            if (!relatedEntityID.equals("")){
                if (relatedEntityID.length()>13){
                    relatedEntityID = relatedEntityID.substring(0,14);
                }
                else{
                    System.out.println("RE = " + relatedEntityID);
                }
            }
            String[] eqStmtComponents = {qualityID, "", entityID, "", relatedEntityID, "", ""};
            EQStatement eqStmt = new EQStatement(eqStmtComponents);
            atomIDtoEQStatement.put(atomID, eqStmt);
        }
    }
    */
        
    
    
    
    
   
    /* to be deleted.
    private ArrayList<Chunk> getUntaggedAtomsGSChar() throws SQLException{
        ArrayList<Chunk> chunks = new ArrayList<>();
        ResultSet rs;
        String dataTable = "gsdata";
        rs = sqliteCall(String.format("SELECT Unique_ID, Character_Description, State_Description FROM %s", dataTable));
        while(rs.next()){
            int chunkID = rs.getInt("Unique_ID");
            String atomizedStatement = rs.getString("Character_Description");
            Chunk chunk = formUntaggedAtom(chunkID, atomizedStatement, Species.UNKNOWN);
            chunks.add(chunk); 
        }
        return chunks;
    }
    private ArrayList<Chunk> getUntaggedAtomsGSState() throws SQLException{
        ArrayList<Chunk> chunks = new ArrayList<>();
        ResultSet rs;
        String dataTable = "gsdata";
        rs = sqliteCall(String.format("SELECT Unique_ID, Character_Description, State_Description FROM %s", dataTable));
        while(rs.next()){
            int chunkID = rs.getInt("Unique_ID");
            String atomizedStatement = rs.getString("State_Description");
            Chunk chunk = formUntaggedAtom(chunkID, atomizedStatement, Species.UNKNOWN);
            chunks.add(chunk); 
        }
        return chunks;
    }*/
    
    
    
    
    
    
    
    
    
    
    


    
    /**
     * This is a method where the column names for the SQL data actually matter.
     * Would have to change how this works if updating the requirements for a table of input text.
     * Note untagged means that the words in the atomized statements are not marked up, as they
     * could be by a BioNLP parser.
     * @return
     * @throws SQLException 
     */
    private ArrayList<Chunk> getUntaggedAtoms() throws SQLException{
        ArrayList<Chunk> chunks = new ArrayList<>();
        ResultSet rs;
        if (Config.quick){
            Object[] data = {Config.dataTable, Config.species};
            rs = sqliteCall(String.format("SELECT Species,ppn_id,atomized_statement FROM %s WHERE Species=\"%s\"", data));
        }
        else {
            rs = sqliteCall(String.format("SELECT Species,ppn_id,atomized_statement FROM %s", Config.dataTable));
        }
        
        while (rs.next()){
            int chunkID = rs.getInt("ppn_id");
            String atomizedStatement = rs.getString("atomized_statement");
            String speciesStr = rs.getString("Species");
            Species species = utils.Util.inferSpecies(speciesStr);
            Chunk chunk = formUntaggedAtom(chunkID, atomizedStatement, species);
            chunks.add(chunk);
        }
        return chunks;
        
    }
    // Supporting methods.
    private Chunk formUntaggedAtom(int chunkID, String atomizedStatement, Species species){
        Chunk chunk = new Chunk(chunkID, TextDatatype.PHENE, atomizedStatement, species);
        return chunk;
        
    }
    
    /**
     * This is a method where the column names for the SQL data actually matter.
     * Would have to change how this works if updating the requirements for a table of input text.
     * Note un-tagged means that the words in the atomized statements are not marked up, as they
     * could be by a BioNLP parser.
     * @return
     * @throws SQLException 
     */
    
    // Modifying this method to fix the phenotype numbering problem, has to take gene name into account. (gene_symbol, Gene_Identifier)
    private ArrayList<Chunk> getUntaggedPhenotypes() throws SQLException{
        ArrayList<Chunk> chunks = new ArrayList<>();
        int chunkID = 0;
        ResultSet rs;
        /*
        if (Config.quick){
            Object[] data = {Config.dataTable, Config.species};
            rs = sqliteCall(String.format("SELECT DISTINCT phenotype_description FROM %s WHERE Species=\"%s\"", data));
        }
        else */
        rs = sqliteCall(String.format("SELECT DISTINCT Species,Gene_Identifier,phenotype_description FROM %s", Config.dataTable));
        
        
        while (rs.next()){
            chunkID++;
            String phenotypeDescription = rs.getString("phenotype_description");
            String geneIdentifier = rs.getString("Gene_Identifier");
            String speciesStr = rs.getString("Species");
            Species species = utils.Util.inferSpecies(speciesStr);
            
            List<Integer> atomIDs = new ArrayList<>();
            
            // is it okay to only look at gene_identifier here? should use phenotype description as well?
            // it was bad, fixed it.
            
            //Object[] data2 = {Config.dataTable, geneIdentifier};
            //ResultSet rs2 = sqliteCall(String.format("SELECT ppn_id FROM %s WHERE Gene_Identifier=\"%s\"", data2));
            Object[] data2 = {Config.dataTable, geneIdentifier, speciesStr, phenotypeDescription};
            ResultSet rs2 = sqliteCall(String.format("SELECT ppn_id FROM %s WHERE Gene_Identifier=\"%s\" AND Species=\"%s\" and phenotype_description=\"%s\"", data2));
            while (rs2.next()){
                atomIDs.add(rs2.getInt("ppn_id"));
            }
            
            Chunk chunk = formUntaggedPhenotype(chunkID, phenotypeDescription, species);
            chunks.add(chunk);
            
            // Updating the ID map.
            phenotypeIDtoAtomIDs.put(chunkID, (ArrayList<Integer>) atomIDs);
            // Updating the other ID map.
            for (int atomID: atomIDs){
                atomIDtoPhenotypeID.put(atomID, chunkID);
            }
            
            
            /* to be removed.
            if (atomIDs.isEmpty()){
                System.out.println("Phenotype "+chunkID+ " has no phenes associated with it.");
            }
            if (chunkID==2823){
                System.out.println("The number of atoms for phenotype 2823 is " + atomIDs.size());
                for (int atomID: atomIDs){
                    System.out.println("A " + atomID + " maps to P " + atomIDtoPhenotypeID.get(atomID));
                }
            }
            */
            
            
            
            
        }
        return chunks;
    }
    /*
    private ArrayList<Chunk> getUntaggedPhenotypes() throws SQLException{
        ArrayList<Chunk> chunks = new ArrayList<>();
        int chunkID = 0;
        ResultSet rs;
        if (Config.quick){
            Object[] data = {Config.dataTable, Config.species};
            rs = sqliteCall(String.format("SELECT DISTINCT phenotype_description FROM %s WHERE Species=\"%s\"", data));
        }
        else {
            rs = sqliteCall(String.format("SELECT DISTINCT phenotype_description FROM %s", Config.dataTable));
        }
        
        while (rs.next()){
            chunkID++;
            String phenotypeDescription = rs.getString("phenotype_description");
            
            List<Integer> atomIDs = new ArrayList<>();
            
            Object[] data2 = {Config.dataTable, phenotypeDescription};
            ResultSet rs2 = sqliteCall(String.format("SELECT ppn_id FROM %s WHERE phenotype_description=\"%s\"", data2));
            while (rs2.next()){
                atomIDs.add(rs2.getInt("ppn_id"));
            }
            
            Chunk chunk = formUntaggedPhenotype(chunkID, phenotypeDescription);
            chunks.add(chunk);
            
            // Updating the ID map.
            phenotypeIDtoAtomIDs.put(chunkID, (ArrayList<Integer>) atomIDs);
            // Updating the other ID map.
            for (int atomID: atomIDs){
                atomIDtoPhenotypeID.put(atomID, chunkID);
            }
        }
        return chunks;
    }
    */
    
    
    
    // Supporting method.
    private Chunk formUntaggedPhenotype(int chunkID, String phenotypeDescription, Species species){
        Chunk chunk = new Chunk(chunkID, TextDatatype.PHENOTYPE, phenotypeDescription, species);
        return chunk;
    }
        
    
    
    
    

    
    public Chunk getAtomChunkFromID(int atomID){
        return atChunkMap.get(atomID);
    }
    
    public Chunk getPhenotypeChunkFromID(int phenotypeID){
        return phChunkMap.get(phenotypeID);
    }
    
    public Chunk getSplitPhenotypeChunkFromID(int spID){
        return spChunkMap.get(spID);
    }
    
    
    public Chunk getChunkFromIDWithDType(int id, TextDatatype dt) throws Exception{
        switch(dt){
            case PHENOTYPE:
                return getPhenotypeChunkFromID(id);
            case PHENE:
                return getAtomChunkFromID(id);
            case SPLIT_PHENOTYPE:
                return getSplitPhenotypeChunkFromID(id);
            default:
                throw new Exception();
          
        }
    }
    
    public List<Chunk> getAtomChunksFromIDs(List<Integer> atomIDs){
        List<Chunk> chunks = new ArrayList<>();
        for (int atomID: atomIDs){
            chunks.add(getAtomChunkFromID(atomID));
        }
        return chunks;
    }
    
    public List<Chunk> getPhenotypeChunksFromIDs(List<Integer> phenotypeIDs){
        List<Chunk> chunks = new ArrayList<>();
                for (int phenID: phenotypeIDs){
            chunks.add(getPhenotypeChunkFromID(phenID));
        }
        return chunks;
    }
   
    
    /* check to verify that these are not used at all....
    public Chunk getDefaultChunkFromChunkID(int chunkID) throws Exception{
        switch(utils.Util.inferTextType(Config.format)){
        case ATOM:
            return getAtomChunkFromID(chunkID);
        case PHENOTYPE:
            return getPhenotypeChunkFromID(chunkID);
        default:
            throw new Exception();
        }
    }
    */
    /* chcck to verify that these are not used at all...
    public List<Chunk> getDefaultChunksFromChunkIDs(List<Integer> chunkIDs) throws Exception{
        switch(utils.Util.inferTextType(Config.format)){
        case ATOM:
            return getAtomChunksFromIDs(chunkIDs);
        case PHENOTYPE:
            return getPhenotypeChunksFromIDs(chunkIDs);
        default:
            throw new Exception();
        }
    }
    */
   
    
    
    public String getAtomizedStatementStr(int atomID){
        String atomizedStatement = atChunkMap.get(atomID).getRawText();
        String atomizedStatementNoCommas = atomizedStatement.replace(",","");
        return atomizedStatementNoCommas;
    }
    
    public String getPhenotypeDescStr(int phenotypeID){
        int onePossibleAtomID = phenotypeIDtoAtomIDs.get(phenotypeID).get(0);
        String phenotypeDesc = phChunkMap.get(phenotypeID).getRawText();
        String phenotypeDescNoCommas = phenotypeDesc.replace(",","");
        return phenotypeDescNoCommas;
    }
    
    public EQStatement getCuratedEQStatementFromAtomID(int atomID){
        return atomIDtoEQStatement.get(atomID);
    }
    
    public ArrayList<EQStatement> getCuratedEQStatementsFromAtomIDs(ArrayList<Integer> atomIDs){
        ArrayList<EQStatement> eqs = new ArrayList<>();
        for (int atomID: atomIDs){
            eqs.add(getCuratedEQStatementFromAtomID(atomID));
        }
        return eqs;
    }
    
    public ArrayList<EQStatement> getCuratedEQStatementsFromPhenotypeID(int phenotypeID){
        ArrayList<Integer> atomIDs = getAtomIDsFromPhenotypeID(phenotypeID);
        return getCuratedEQStatementsFromAtomIDs(atomIDs);
    }
    
    
   
    
    
    
    
    
    
    
    
    
    

    
    
    
    


    
}