
package text;

import composer.EQStatement;
import config.Config;
import enums.Species;
import enums.TextDatatype;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import objects.Chunk;
import static utils.Utils.sqliteCall;



/**
 * Class for managing all the information relating to the text data.
 * Text data is read in from the SQLite database and stored by this object which assigns 
 * relevant identifiers and provides methods for relating parts of the data to each other 
 * and accessing it.
 * @author irbraun
 */
public class Text {
    
    // Mapping between different types in the tagged data.
    private final HashMap<Integer,ArrayList<Integer>> phenotypeIDtoAtomIDs;
    private final HashMap<Integer,ArrayList<Integer>> phenotypeIDtoSplitIDs;
    private final HashMap<Integer,Integer> atomIDtoPhenotypeID;
    private final HashMap<Integer,Integer> splitIDtoPhenotypeID;
    private final HashMap<Integer,EQStatement> atomIDtoEQStatement;
    private final HashMap<String,Integer> geneIDtoPhenotypeID;
    private final HashMap<Integer,String> phenotypeIDtoGeneID;

    // Lists holding the entirety of data for each type.
    private ArrayList<Chunk> phChunks;
    private ArrayList<Chunk> atChunks;
    private ArrayList<Chunk> spChunks;
    
    // Mapping between the ID field of each chunk and the entire object.
    private final HashMap<Integer,Chunk> phChunkMap;
    private final HashMap<Integer,Chunk> atChunkMap;
    private final HashMap<Integer,Chunk> spChunkMap;
    private final HashMap<Integer,String> atSpeciesMap;
    
    
    
    
    // Initialize all the maps and call methods to populate them.
    public Text() throws SQLException, Exception{
        phenotypeIDtoAtomIDs = new HashMap<>();
        phenotypeIDtoSplitIDs = new HashMap<>();
        atomIDtoPhenotypeID = new HashMap<>();
        atomIDtoEQStatement = new HashMap<>();
        splitIDtoPhenotypeID = new HashMap<>();
        geneIDtoPhenotypeID = new HashMap<>();
        phenotypeIDtoGeneID = new HashMap<>();
        phChunkMap = new HashMap<>();
        atChunkMap = new HashMap<>();
        spChunkMap = new HashMap<>();
        atSpeciesMap = new HashMap<>();
        populateCuratedEQStmts();
        buildChunks();
    }


    
    
    // Functions to convert between IDs of different text datatypes.
    public ArrayList<Integer> getAtomIDsFromPhenotypeID(int phenotypeID){
        return phenotypeIDtoAtomIDs.get(phenotypeID);
    }
    public ArrayList<Integer> getSplitPhenotypeIDsFromPhenotypeID(int phenotypeID){
        return phenotypeIDtoSplitIDs.get(phenotypeID);
    }
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
    
   
    
    /**
     * Uses the SQL table and assigns new IDS to each phenotype (those IDs don't come
     * from the table. This is different than the atomized statements which get their
     * IDs read directly from the SQL table.
     * @return
     * @throws SQLException 
     */
    private ArrayList<Chunk> buildPhenotypeChunks() throws SQLException{
        ArrayList<Chunk> chunks = getUntaggedPhenotypes();
        return chunks;
    } 
    
    /**
     * Uses the SQL table and reads the IDs of each atomized statement directly from
     * the table.
     * @return
     * @throws SQLException 
     */
    private ArrayList<Chunk> buildAtomChunks() throws SQLException{
        ArrayList<Chunk> chunks = getUntaggedAtoms();
        return chunks;
    } 

    
    
    
    /**
     * Tries out the assumption that periods and semi-colons are used to deliminate 
     * different phenes in the phenotype description. Simplest possible assumption to
     * check performance of splitting phenotypes into atomized statements without 
     * trying to infer any new information.
     * @return
     * @throws SQLException 
     */
    private ArrayList<Chunk> buildSplitChunks() throws SQLException{
        ArrayList<Chunk> chunks = new ArrayList<>();
        int splitID=1;
        for (Chunk c: phChunks){
            ArrayList<Integer> splitChunkIDsForThisPhenotype = new ArrayList<>();
            String phenotype_desc = c.getRawText();
            for (String s1: phenotype_desc.split("\\.")){
                for (String s2: s1.split(";")){
                    s2 = s2.trim();
                    if (!s2.equals("")){
                        Chunk spChunk = new Chunk(splitID, TextDatatype.SPLIT_PHENOTYPE, s2, c.species, c.geneIdentifier);
                        chunks.add(spChunk);
                        splitIDtoPhenotypeID.put(splitID, c.chunkID);
                        splitChunkIDsForThisPhenotype.add(spChunk.chunkID);
                        splitID++;
                    }
                }
            }
            phenotypeIDtoSplitIDs.put(c.chunkID, splitChunkIDsForThisPhenotype);
        }
        return chunks;
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
    
    
    
    /**
     * Return a set of text chunks based on a description of their datatype.
     * @param dtype_str
     * @return
     * @throws Exception 
     */
    public ArrayList<Chunk> getAllChunksOfDType(String dtype_str) throws Exception{
        TextDatatype dtype = utils.Utils.inferTextType(dtype_str);
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
    public ArrayList<Chunk> getAllChunksOfDType(TextDatatype dtype) throws Exception{
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
    
    
    
    
    
    /* TO BE DELETED.
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
    */
    
    
    
    
    
 
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
    
    
    
    
    // Functions to get the term IDs and their roles in the curated EQ statements
    // associated with either an atomized statement or a phenotype description.
    // Importantly these two methods return lists of the same size with the same
    // ordering. This is used later so should be enforced here rather than just
    // assumed ideally.
    public List getAllTermIDs(int id, TextDatatype textType){
        
        switch(textType){
        case PHENE:
            return getAllTermIDsAtom(id);
        case PHENOTYPE:
            return getAllTermIDsPhen(id);
        case SPLIT_PHENOTYPE:
            return getAllTermIDsSplit(id);
        default:
            return null;
        }
       
    }
    public List getAllTermRoles(int id, TextDatatype textType){
        switch(textType){
        case PHENE:
            return getAllTermRolesAtom(id);
        case PHENOTYPE:
            return getAllTermRolesPhen(id);
        case SPLIT_PHENOTYPE:
            return getAllTermRolesSplit(id);
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
    private List getAllTermIDsSplit(int splitID){
        int phenotypeID = splitIDtoPhenotypeID.get(splitID);
        ArrayList<Integer> atomIDs = phenotypeIDtoAtomIDs.get(phenotypeID);
        ArrayList<String> allTermsIDs = new ArrayList<>();
        for (int atomID: atomIDs){
            allTermsIDs.addAll(getAllTermIDsAtom(atomID));
        }
        return allTermsIDs;
    }
    private List getAllTermRolesSplit(int splitID){
        int phenotypeID = splitIDtoPhenotypeID.get(splitID);
        ArrayList<Integer> atomIDs = phenotypeIDtoAtomIDs.get(phenotypeID);
        ArrayList<String> allTermRoles = new ArrayList<>();
        for (int atomID: atomIDs){
            allTermRoles.addAll(getAllTermRolesAtom(atomID));
        }
        return allTermRoles;
    }

    
    
    
  
    /**
     * Specific to the formatting of the csv file that was read into the SQL table.
     * Changes need to be made here is standardizing the way the input should look
     * in the csv file.
     * @throws SQLException 
     */
    private void populateCuratedEQStmts() throws SQLException, Exception{
        
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
    
  
    
    /**
     * Specific to the formatting of the csv file that was read into the SQL table.
     * Changes need to be made here is standardizing the way the input should look
     * in the csv file. Note untagged means that the words in the atomized statements 
     * are not marked up, as they could be by a BioNLP parser.
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
            rs = sqliteCall(String.format("SELECT Species,Gene_Identifier,ppn_id,atomized_statement FROM %s", Config.dataTable));
        }
        
        while (rs.next()){
            int chunkID = rs.getInt("ppn_id");
            String atomizedStatement = rs.getString("atomized_statement");
            String geneIdentifier = rs.getString("Gene_Identifier");
            String speciesStr = rs.getString("Species");
            Species species = utils.Utils.inferSpecies(speciesStr);
            Chunk chunk = formUntaggedAtom(chunkID, atomizedStatement, species, geneIdentifier);
            chunks.add(chunk);
        }
        return chunks;
        
    }
    private Chunk formUntaggedAtom(int chunkID, String atomizedStatement, Species species, String geneIdentifier){
        Chunk chunk = new Chunk(chunkID, TextDatatype.PHENE, atomizedStatement, species, geneIdentifier);
        return chunk;
        
    }
    
    
    
    
    /**
     * Specific to the formatting of the csv file that was read into the SQL table.
     * Changes need to be made here is standardizing the way the input should look
     * in the csv file. Note untagged means that the words in the atomized statements 
     * are not marked up, as they could be by a BioNLP parser.
     * @return
     * @throws SQLException 
     */
    private ArrayList<Chunk> getUntaggedPhenotypes() throws SQLException{
        ArrayList<Chunk> chunks = new ArrayList<>();
        int chunkID = 0;
        ResultSet rs;
        rs = sqliteCall(String.format("SELECT DISTINCT Species,Gene_Identifier,phenotype_description FROM %s", Config.dataTable));

        while (rs.next()){
            chunkID++;
            String phenotypeDescription = rs.getString("phenotype_description");
            String geneIdentifier = rs.getString("Gene_Identifier");
            String speciesStr = rs.getString("Species");
            Species species = utils.Utils.inferSpecies(speciesStr);
            
            // The requirement for a phenotype in the input table is that is has to be unique in all these columns.
            List<Integer> atomIDs = new ArrayList<>();
            Object[] data2 = {Config.dataTable, geneIdentifier, speciesStr, phenotypeDescription};
            ResultSet rs2 = sqliteCall(String.format("SELECT ppn_id FROM %s WHERE Gene_Identifier=\"%s\" AND Species=\"%s\" and phenotype_description=\"%s\"", data2));
            while (rs2.next()){
                atomIDs.add(rs2.getInt("ppn_id"));
            }
            
            Chunk chunk = formUntaggedPhenotype(chunkID, phenotypeDescription, species, geneIdentifier);
            chunks.add(chunk);
            
            // Updating the ID maps.
            geneIDtoPhenotypeID.put(geneIdentifier, chunkID);
            phenotypeIDtoGeneID.put(chunkID, geneIdentifier);
            phenotypeIDtoAtomIDs.put(chunkID, (ArrayList<Integer>) atomIDs);
            for (int atomID: atomIDs){
                atomIDtoPhenotypeID.put(atomID, chunkID);
            }
        }
        return chunks;
    }
    private Chunk formUntaggedPhenotype(int chunkID, String phenotypeDescription, Species species, String geneIdentifier){
        Chunk chunk = new Chunk(chunkID, TextDatatype.PHENOTYPE, phenotypeDescription, species, geneIdentifier);
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
   



    
    public String getAtomizedStatementStr(int atomID){
        String atomizedStatement = atChunkMap.get(atomID).getRawText();
        String atomizedStatementNoCommas = atomizedStatement.replace(",","");
        return atomizedStatementNoCommas;
    }
    public String getPhenotypeDescStr(int phenotypeID){
        String phenotypeDesc = phChunkMap.get(phenotypeID).getRawText();
        String phenotypeDescNoCommas = phenotypeDesc.replace(",","");
        return phenotypeDescNoCommas;
    }
    public String getSplitPhenotypeDescStr(int splitPhenotypeID){
        String desc = spChunkMap.get(splitPhenotypeID).getRawText();
        String descNoCommas = desc.replace(",","");
        return descNoCommas;
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
    public ArrayList<EQStatement> getCuratedEQStatementsFromSplitPhenotypeID(int splitID){
        int phenotypeID = getPhenotypeIDfromSplitPhenotypeID(splitID);
        return getCuratedEQStatementsFromPhenotypeID(phenotypeID);
    }
    
    public ArrayList<EQStatement> getAllCuratedEQStatements(){
        return new ArrayList<>(atomIDtoEQStatement.values());
    }
    
    
    
    
    
    public String getGeneIDFromPhenotypeID(int phenotypeID){
        return phenotypeIDtoGeneID.get(phenotypeID);
    }
    public int getPhenotypeIDFromGeneID(String geneID){
        return geneIDtoPhenotypeID.get(geneID);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    private String getConcatenatedPheneText(int phenotypeID){
        StringBuilder sb = new StringBuilder();
        for (int atomID: this.getAtomIDsFromPhenotypeID(phenotypeID)){
            String desc = this.getAtomChunkFromID(atomID).getRawText();
            sb.append(desc);
            if (desc.charAt(desc.length()-1) != '.'){
                sb.append(". ");
            }
        }
        return sb.toString();
    }
    
    public ArrayList<Chunk> getAllPhenotypeChunksWithConcatenatedPheneText() throws SQLException{
        ArrayList<Chunk> concatPhenotypeChunks = new ArrayList<>();
        for (Chunk c: this.getAllPhenotypeChunks()){
            String concatendatedPheneDescriptions = getConcatenatedPheneText(c.chunkID); 
            Chunk modified = new Chunk(c.chunkID, c.textType, concatendatedPheneDescriptions, c.species, c.geneIdentifier);
            concatPhenotypeChunks.add(modified);
        }
        return concatPhenotypeChunks;
    }
    
    
    
    
    
    
    
    
    
    
    
}