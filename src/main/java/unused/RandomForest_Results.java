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
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import static main.Main.logger;
import main.Partitions;
import ontology.Onto;
import org.json.simple.parser.ParseException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import structure.Chunk;
import text.Text;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.NewOntologyException;

/**
 *
 * @author irbraun
 */


/**
 * Note about why threshold is done here.
 * 
 * # threshold the machine learning predictions.
d4[d4$category %in% c("FN"),"score"] <- NA
d4$score <- as.numeric(as.character(d4$score))
threshold = 0.5
d4 <- d4[(d4$score >= threshold | is.na(d4$score) | d4$category),]


# problem with threshold here....
# we want to get rid of predictions that have scores < threshold.
# but that might mean throwing out some TP, in which case we want to keep the row but change its category from TP to FN.

# take care of all the true positives.
d4[d4$category %in% c("TP"),]$category <- ifelse(d4$score >= threshold, "TP", "FN")

# this approach won't work, because the problem is that the similarity value for FN is calculated as a max of the FP sim values.
# so the threshold needs to be done in the java code that's actuall generating the evalution files.
 * 
 * 
 */



public class RandomForest_Results {
    
    public static void routine() throws IOException, FileNotFoundException, ParseException, SQLException, OWLOntologyCreationException, NewOntologyException, ClassExpressionException, Exception{
        
        String dirBase = "/Users/irbraun/Desktop/droplet/alpha2/rf/set3_ppn_spc_pato/";

        //rfHelper(Ontology.PATO, String.format("%s%s",dirBase,"outputs_all_ppn_pato/classprobs.csv"), String.format("%s%s",dirBase,"outputs_all_ppn_pato/eval.csv"));
        //rfHelper(Ontology.PO, String.format("%s%s",dirBase,"outputs_all_ppn_po/classprobs.csv"), String.format("%s%s",dirBase,"outputs_all_ppn_po/eval.csv"));
        //rfHelper(Ontology.GO, String.format("%s%s",dirBase,"outputs_all_ppn_go/classprobs.csv"), String.format("%s%s",dirBase,"outputs_all_ppn_go/eval.csv"));
        
        rfHelper(Ontology.PATO, String.format("%s%s",dirBase,"classprobs_training_fold1.csv"), String.format("%s%s",dirBase,"train_eval.csv"));
        //rfHelper(Ontology.PO, String.format("%s%s",dirBase,"outputs_train_ppn_po/classprobs.csv"), String.format("%s%s",dirBase,"outputs_train_ppn_po/eval.csv"));
        //rfHelper(Ontology.GO, String.format("%s%s",dirBase,"outputs_train_ppn_go/classprobs.csv"), String.format("%s%s",dirBase,"outputs_train_ppn_go/eval.csv"));
        
        rfHelper(Ontology.PATO, String.format("%s%s",dirBase,"classprobs_testing_fold1.csv"), String.format("%s%s",dirBase,"test_eval.csv"));
        //rfHelper(Ontology.PO, String.format("%s%s",dirBase,"outputs_test_ppn_po/classprobs.csv"), String.format("%s%s",dirBase,"outputs_test_ppn_po/eval.csv"));
        //rfHelper(Ontology.GO, String.format("%s%s",dirBase,"outputs_test_ppn_go/classprobs.csv"), String.format("%s%s",dirBase,"outputs_test_ppn_go/eval.csv"));    
    }
    
    
    
    
    private static void rfHelper(Ontology ontology, String inputFilename, String outputFilename) throws IOException, FileNotFoundException, ParseException, SQLException, OWLOntologyCreationException, NewOntologyException, ClassExpressionException, Exception{
        
        logger.info(String.format("working on terms from %s",ontology.toString()));
        Text text = new Text();
        
        
        
        // TODO don't hardcode this
        List<Chunk> allChunks = text.getAllAtomChunks();
        List<Chunk> chunks = new ArrayList<>();
        Partitions partitions = new Partitions(text);
        if (inputFilename.contains("all")){
            chunks.addAll(partitions.getChunksInPartitionRangeInclusive(0, 31, allChunks));
        }
        else if (inputFilename.contains("test")){
            chunks.addAll(partitions.getChunksInPartitionRangeInclusive(0, 4, allChunks));
        }
        else if (inputFilename.contains("train")){
            chunks.addAll(partitions.getChunksInPartitionRangeInclusive(5, 31, allChunks));
        }
        else {
            throw new Exception();
        }
        
        
        
        
        

        HashMap<Integer,ArrayList<Result>> nobleCoderTermsMap = new HashMap<>();
        
        File output = new File(outputFilename);
        PrintWriter writer = new PrintWriter(output);
        
        File classProbFile = new File(inputFilename);
        Scanner scanner = new Scanner(classProbFile);
        scanner.useDelimiter(",");
        scanner.nextLine();

        
        // TODO use the learned value for this.
        double threshold = 0.5;
        
        
        // Go through the class probability file that was generated from running NOBLE-Coder and parsing the output.
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] lineValues = line.split(",");
            String chunkIDStr = lineValues[0].replace("\"", "").trim();
            int chunkID = Integer.valueOf(chunkIDStr);
            String termID = lineValues[1].replace("\"","").trim();
            double probability = Double.valueOf(lineValues[2]);
            //String nodes = lineValues[3].trim();
            String nodes = "";
            
            
            Result r = new Result();
            r.termID = termID;
            r.score = probability;
            r.nodes = nodes;
            
            // This is where the thresholding is done.
            if (r.score >= threshold){
                ArrayList<Result> a = nobleCoderTermsMap.getOrDefault(chunkID, new ArrayList<>());
                a.add(r);
                nobleCoderTermsMap.put(chunkID, a);
            }

        }
        
        
        
        
        Onto onto = new Onto(Config.ontologyPaths.get(ontology));
        
        writer.println("chunk,text,label,term,score,component,category,similarity,nodes");
        
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
                if (utils.Util.inferOntology(id).equals(ontology)){
                    try{
                        // Find the false negatives.
                        if (!allTermIDsFoundBySearching.contains(id)){
                            String label = onto.getTermFromTermID(id).label;
                            
                            // need to find the predicted term that is most similar to this FN curated term. (similarity = 0 for no predictions).
                            double simD = utils.Util.getMaxSimJac(id, allTermIDsFoundBySearching, onto);
                            String sim = String.format("%.3f",simD);
                            Object[] line = {c.chunkID, c.getRawText().replace(",", ""), label, id, "none", Role.getAbbrev(role), "FN", sim, "none"};
                            writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",line));
                        }
                        // Find the true positives.
                        else {
                            String joined = nobleCoderTermsMap.get(c.chunkID).get(allTermIDsFoundBySearching.indexOf(id)).nodes;
                            double score = nobleCoderTermsMap.get(c.chunkID).get(allTermIDsFoundBySearching.indexOf(id)).score;
                            String scoreStr = String.format("%.3f",score);
                            Object[] line = {c.chunkID, c.getRawText().replace(",", ""), onto.getTermFromTermID(id).label, id, scoreStr, Role.getAbbrev(role), "TP", "1.000", joined};
                            writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",line));
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
                if(!termIDs.contains(id) && utils.Util.inferOntology(id).equals(ontology)){
                    // Find the false positives.
                    double simD = utils.Util.populateAttributes(c, onto.getTermFromTermID(id), text, onto, ontology).hJac;
                    String sim = String.format("%.3f",simD);
                    double score = nobleCoderTermsMap.get(c.chunkID).get(allTermIDsFoundBySearching.indexOf(id)).score;
                    String scoreStr = String.format("%.3f",score);
                    String joined = result.nodes;
                    Object[] line = {c.chunkID, c.getRawText().replace(",",""), onto.getTermFromTermID(id).label, id, scoreStr, Role.getAbbrev(Role.UNKNOWN), "FP", sim, joined};
                    writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",line));
                }
            }
        }
 
        writer.close();
 
    }
    
    
    
    
    
    
    public static class Result{
        public String termID;
        public String nodes;
        public double score;
    }
 
    
    
    
}