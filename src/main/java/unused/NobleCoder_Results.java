/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package unused;

import config.Config;
import enums.Ontology;
import enums.Role;
import enums.TextDatatype;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import utils.DataGroup;
import static main.Main.logger;
import main.Partitions;
import nlp_annot.Utils;
import ontology.Onto;
import org.json.simple.parser.ParseException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import objects.Chunk;
import text.Text;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.NewOntologyException;

/**
 *
 * @author irbraun
 */
public class NobleCoder_Results {
    
    public static void routine() throws IOException, FileNotFoundException, ParseException, SQLException, OWLOntologyCreationException, NewOntologyException, ClassExpressionException, Exception{
        
        String dirBase = "/Users/irbraun/NetBeansProjects/term-mapping/dmom/NobleCoder/";

        String fold = "fold";
        List<Integer> all = utils.Utils.range(0, 31);
        List<Integer> testPartsSpecies = utils.Utils.range(0, 4);
        List<Integer> testPartsRandom = utils.Utils.range(0, 4);
        
        Text text = new Text();
        Partitions pRandom = new Partitions(text);   
        Partitions pSpecies = new Partitions(text);
        
        // Annotated data available in the Plant PhenomeNET.
        List<DataGroup> patoGroups = new ArrayList<>();
        patoGroups.add(new DataGroup("pato_all_copy", fold, all, String.format("%s%s",dirBase,"outputs_all_ppn_pato/"), pSpecies));
        patoGroups.add(new DataGroup("pato_species", fold, testPartsSpecies, String.format("%s%s",dirBase,"outputs_species_ppn_pato/"), pSpecies));
        patoGroups.add(new DataGroup("pato_random", fold, testPartsRandom, String.format("%s%s",dirBase,"outputs_random_ppn_pato/"), pRandom));
        nobleCoderHelper(text, Ontology.PATO, patoGroups, String.format("%s%s",dirBase,"outputs_all_ppn_pato/original_classprobs.csv"));

        List<DataGroup> poGroups = new ArrayList<>();
        poGroups.add(new DataGroup("po_all_copy", fold, all, String.format("%s%s",dirBase,"outputs_all_ppn_po/"), pSpecies));
        poGroups.add(new DataGroup("po_species", fold, testPartsSpecies, String.format("%s%s",dirBase,"outputs_species_ppn_po/"), pSpecies));
        poGroups.add(new DataGroup("po_random", fold, testPartsRandom, String.format("%s%s",dirBase,"outputs_random_ppn_po/"), pRandom));
        nobleCoderHelper(text, Ontology.PO, poGroups, String.format("%s%s",dirBase,"outputs_all_ppn_po/original_classprobs.csv"));

        
        List<DataGroup> goGroups = new ArrayList<>();
        goGroups.add(new DataGroup("go_all_copy", fold, all, String.format("%s%s",dirBase,"outputs_all_ppn_go/"), pSpecies));
        goGroups.add(new DataGroup("go_species", fold, testPartsSpecies, String.format("%s%s",dirBase,"outputs_species_ppn_go/"), pSpecies));
        goGroups.add(new DataGroup("go_random", fold, testPartsRandom, String.format("%s%s",dirBase,"outputs_random_ppn_go/"), pRandom));
        nobleCoderHelper(text, Ontology.GO, goGroups, String.format("%s%s",dirBase,"outputs_all_ppn_go/original_classprobs.csv"));
        
        //List<Group> chebiGroups = new ArrayList<>();
        //chebiGroups.add(new Group("chebi_all", fold, all, String.format("%s%s",dirBase,"outputs_all_ppn_chebi/"), pSpecies));
        //chebiGroups.add(new Group("chebi_species", fold, testPartsSpecies, String.format("%s%s",dirBase,"outputs_species_ppn_chebi/"), pSpecies));
        //chebiGroups.add(new Group("chebi_random", fold, testPartsRandom, String.format("%s%s",dirBase,"outputs_random_ppn_chebi/"), pRandom));
        //nobleCoderHelper(text, Ontology.CHEBI, chebiGroups, String.format("%s%s",dirBase,"outputs_all_ppn_chebi/original_classprobs.csv"));
        
    }
    
    
    
    
    /**
     * Populates the evaluation and class probability files for any groups that are passed in. This 
     * is done by looking at the single large class probability file that was obtained by converting
     * the results of running the tool on all the text data in the class probability file format,
     * then the results are interpreted for different sets of data so that the results from this
     * tool can be compared to other methods.
     * @param text
     * @param ontology
     * @param groups
     * @param inputFilename
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ParseException
     * @throws SQLException
     * @throws OWLOntologyCreationException
     * @throws NewOntologyException
     * @throws ClassExpressionException
     * @throws Exception 
     */
    public static void nobleCoderHelper(Text text, Ontology ontology, List<DataGroup> groups, String inputFilename) throws IOException, FileNotFoundException, ParseException, SQLException, OWLOntologyCreationException, NewOntologyException, ClassExpressionException, Exception{
        
        logger.info(String.format("working on terms from %s",ontology.toString()));
        
        List<Chunk> chunks = text.getAllAtomChunks();

        
        // Write the headers to the different output files.
        String evalHeader = "chunk,text,label,term,score,component,category,similarity,nodes";
        String classProbHeader = "chunk,term,prob,nodes";
        for (DataGroup g: groups){
            g.classProbsPrinter.println(classProbHeader);
            g.evalPrinter.println(evalHeader);
        }
        
        HashMap<Integer,ArrayList<Result>> nobleCoderTermsMap = new HashMap<>();

        // Open the input file, which contains all the class probabilities found using NOBLE-Coder.
        File classProbFile = new File(inputFilename);
        Scanner scanner = new Scanner(classProbFile);
        scanner.useDelimiter(",");
        scanner.nextLine();

        
        
        // Go through the class probability file that was generated from running NOBLE-Coder and parsing the output.
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] lineValues = line.split(",");
            String chunkIDStr = lineValues[0].replace("\"", "").trim();
            int chunkID = Integer.valueOf(chunkIDStr);
            String termID = lineValues[1].replace("\"","").trim();
            double probability = Double.valueOf(lineValues[2]);
            
            // this method is used by the aggregate one too, and that might not included nodes. make this more consistent.
            String nodes = "";
            if (lineValues.length>3){
                nodes = lineValues[3].trim();
            }

            Result r = new Result();
            r.termID = termID;
            r.score = probability;
            r.nodes = nodes;
            
            ArrayList<Result> a = nobleCoderTermsMap.getOrDefault(chunkID, new ArrayList<>());
            a.add(r);
            nobleCoderTermsMap.put(chunkID, a);
        }
        
        Onto onto = new Onto(Config.ontologyPaths.get(ontology));
        
        for (Chunk c: chunks){
            ArrayList<String> termIDs = text.getCuratedEQStatementFromAtomID(c.chunkID).getAllTermIDs();
            ArrayList<Role> termRoles = text.getCuratedEQStatementFromAtomID(c.chunkID).getAllTermRoles();
            
            // What are the ID's of the terms that NOBLE-Coder found for this chunk of text?
            List<String> allTermIDsFoundBySearching = new ArrayList<>();
            for (Result result: nobleCoderTermsMap.getOrDefault(c.chunkID, new ArrayList<>())){
                allTermIDsFoundBySearching.add(result.termID);
            }
            
            for (int i=0; i<termIDs.size(); i++){
                String id = termIDs.get(i);
                Role role = termRoles.get(i);
                if (utils.Utils.inferOntology(id).equals(ontology)){
                    try{
                        // Find the false negatives.
                        if (!allTermIDsFoundBySearching.contains(id)){
                            String label = onto.getTermFromTermID(id).label;
                            
                            // need to find the predicted term that is most similar to this FN curated term. (similarity = 0 for no predictions).
                            double simD = utils.Utils.getMaxSimJac(id, allTermIDsFoundBySearching, onto);
                            String sim = String.format("%.3f",simD);
                            Object[] line = {c.chunkID, c.getRawText().replace(",", ""), label, id, "none", Role.getAbbrev(role), "FN", sim, "none"};
                            Utils.writeToEvalFiles(line, c, groups);
                        }
                        // Find the true positives.
                        else {
                            String joined = nobleCoderTermsMap.get(c.chunkID).get(allTermIDsFoundBySearching.indexOf(id)).nodes;
                            double score = nobleCoderTermsMap.get(c.chunkID).get(allTermIDsFoundBySearching.indexOf(id)).score;
                            String scoreStr = String.format("%.3f",score);
                            Object[] line = {c.chunkID, c.getRawText().replace(",", ""), onto.getTermFromTermID(id).label, id, scoreStr, Role.getAbbrev(role), "TP", "1.000", joined};
                            Utils.writeToEvalFiles(line, c, groups);
                        }
                    }
                    catch (NullPointerException e){
                        logger.info(String.format("%s was found in the annotations but not in the ontology file",id));
                    }
                }
            }
            
            // Don't need to include the try catch for unsupported ontology terms here because these all come directly from the owl file. 
            for (Result result: nobleCoderTermsMap.getOrDefault(c.chunkID, new ArrayList<>())){
                String id = result.termID;
                if(!termIDs.contains(id) && utils.Utils.inferOntology(id).equals(ontology)){
                    // Find the false positives.
                    double simD = utils.Utils.populateAttributes(c, onto.getTermFromTermID(id), text, onto, ontology).hJac;
                    String sim = String.format("%.3f",simD);
                    double score = nobleCoderTermsMap.get(c.chunkID).get(allTermIDsFoundBySearching.indexOf(id)).score;
                    String scoreStr = String.format("%.3f",score);
                    String joined = result.nodes;
                    Object[] line = {c.chunkID, c.getRawText().replace(",",""), onto.getTermFromTermID(id).label, id, scoreStr, Role.getAbbrev(Role.UNKNOWN), "FP", sim, joined};
                    Utils.writeToEvalFiles(line, c, groups);
                }
                
                // Put these terms in the class probabilities file, regardless of whether they are TP or FP, just include all mapped terms.
                String joined = result.nodes;
                double score = nobleCoderTermsMap.get(c.chunkID).get(allTermIDsFoundBySearching.indexOf(id)).score;
                String scoreStr = String.format("%.3f",score);
                Object[] line = {c.chunkID,id, scoreStr,joined};
                Utils.writeToClassProbFiles(line, c, groups);
            }
        }
 
        
        // Close all the files.
        for (DataGroup g: groups){
            g.classProbsPrinter.close();
            g.evalPrinter.close();
        }
 
    }
    
    
    
    
    
    
    public static class Result{
        public String termID;
        public String nodes;
        public double score;
    }
    
    
    
}
