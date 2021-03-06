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

/*
Note: made it so that the ontology term's potentially use two different identifiers.
One that is used when the brain is called and one that is used everywhere else.
*/




/**
 *
 * @author irbraun
 */
public class OutsideAnnotationReaderOld {
    
    /*
    public static void run(String source) throws IOException, FileNotFoundException, ParseException, SQLException, OWLOntologyCreationException, NewOntologyException, ClassExpressionException, Exception{
        
        
        
        // Pick between the available methods.
        String dir;
        String patosrc;
        String posrc;
        String gosrc;
        String chebisrc;

        switch(source){
            case("na"):
                dir = Config.naPath;
                patosrc = Config.naSourceFilePATO;
                posrc = Config.naSourceFilePO;
                gosrc = Config.naSourceFileGO;
                chebisrc = Config.naSourceFileChEBI;
                break;
            case("nc"):
                dir = Config.ncPath;
                patosrc = Config.ncSourceFilePATO;
                posrc = Config.ncSourceFilePO;
                gosrc = Config.ncSourceFileGO;
                chebisrc = Config.ncSourceFileChEBI;
                break;
            default:
                throw new Exception();
        }
       
        
        
        
        
        String baseDirectory = dir;
        
        // Strings identifying aspects of the testing sets of phenotypes.
        String fold = "fold";
        String set1 = Config.set1Name;
        String set2 = Config.set2Name;
        String all = "all";
       
        // Partition numbers for each testing set.
        List<Integer> allPartitionNumbers = utils.Util.range(0, 31);
        List<Integer> set1PartitionNumbers = utils.Util.range(0, 4);
        List<Integer> set2PartitionNumbers = utils.Util.range(0, 4);
        
        // Text data and partition objects for each testing set.
        Text text = new Text();
        Partitions set1PartitionObj = new Partitions(text, Config.set1Name);
        Partitions set2PartitionObj = new Partitions(text, Config.set2Name);   

        // Annotated data available in the Plant PhenomeNET.

        String dataset = "ppn";
        List<Group> patoGroups = new ArrayList<>();
        patoGroups.add(new Group("name", fold, allPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", all, dataset, "pato/"), set1PartitionObj));
        patoGroups.add(new Group("name", fold, set1PartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", set1, dataset, "pato/"), set1PartitionObj));
        patoGroups.add(new Group("name", fold, set2PartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", set2, dataset, "pato/"), set2PartitionObj));
        populateFilesForTestSets(text, Ontology.PATO, patoGroups, patosrc);

        List<Group> poGroups = new ArrayList<>();
        poGroups.add(new Group("name", fold, allPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", all, dataset, "po/"), set1PartitionObj));
        poGroups.add(new Group("name", fold, set1PartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", set1, dataset, "po/"), set1PartitionObj));
        poGroups.add(new Group("name", fold, set2PartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", set2, dataset, "po/"), set2PartitionObj));
        populateFilesForTestSets(text, Ontology.PO, poGroups, posrc);

        List<Group> goGroups = new ArrayList<>();
        goGroups.add(new Group("name", fold, allPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", all, dataset, "go/"), set1PartitionObj));
        goGroups.add(new Group("name", fold, set1PartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", set1, dataset, "go/"), set1PartitionObj));
        goGroups.add(new Group("name", fold, set2PartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", set2, dataset, "go/"), set2PartitionObj));
        populateFilesForTestSets(text, Ontology.GO, goGroups, gosrc);
        
        if (!source.equals("nc")){
            List<Group> chebiGroups = new ArrayList<>();
            chebiGroups.add(new Group("name", fold, allPartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", all, dataset, "chebi/"), set1PartitionObj));
            chebiGroups.add(new Group("name", fold, set1PartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", set1, dataset, "chebi/"), set1PartitionObj));
            chebiGroups.add(new Group("name", fold, set2PartitionNumbers, String.format("%s%s_%s_%s_%s",baseDirectory,"outputs", set2, dataset, "chebi/"), set2PartitionObj));
            populateFilesForTestSets(text, Ontology.CHEBI, chebiGroups, chebisrc);
        }

        
        // Annotated data available in the Plant PhenomeNET.    // trying new stuff to make the naming better.
        String dataset = "ppn";
        String outputPath = String.format("%s/%s",baseDirectory,"output_pato");
        List<Group> patoGroups = new ArrayList<>();
        patoGroups.add(new Group("group1", fold, allPartitionNumbers, outputPath, set1PartitionObj));
        patoGroups.add(new Group("group2", fold, set1PartitionNumbers, outputPath, set1PartitionObj));
        patoGroups.add(new Group("group3", fold, set2PartitionNumbers, outputPath, set2PartitionObj));
        populateFilesForTestSets(text, Ontology.PATO, patoGroups, patosrc);

        List<Group> poGroups = new ArrayList<>();
        outputPath = String.format("%s/%s",baseDirectory,"output_po");
        poGroups.add(new Group("group1", fold, allPartitionNumbers, outputPath, set1PartitionObj));
        poGroups.add(new Group("group2", fold, set1PartitionNumbers, outputPath, set1PartitionObj));
        poGroups.add(new Group("group3", fold, set2PartitionNumbers, outputPath, set2PartitionObj));
        populateFilesForTestSets(text, Ontology.PO, poGroups, posrc);

        List<Group> goGroups = new ArrayList<>();
        outputPath = String.format("%s/%s",baseDirectory,"output_go");
        goGroups.add(new Group("group1", fold, allPartitionNumbers, outputPath, set1PartitionObj));
        goGroups.add(new Group("group2", fold, set1PartitionNumbers, outputPath, set1PartitionObj));
        goGroups.add(new Group("group3", fold, set2PartitionNumbers, outputPath, set2PartitionObj));
        populateFilesForTestSets(text, Ontology.GO, goGroups, gosrc);
        
        if (!source.equals("nc")){
            List<Group> chebiGroups = new ArrayList<>();
            outputPath = String.format("%s/%s",baseDirectory,"output_chebi");
            chebiGroups.add(new Group("group1", fold, allPartitionNumbers, outputPath, set1PartitionObj));
            chebiGroups.add(new Group("group2", fold, set1PartitionNumbers, outputPath, set1PartitionObj));
            chebiGroups.add(new Group("group3", fold, set2PartitionNumbers, outputPath, set2PartitionObj));
            populateFilesForTestSets(text, Ontology.CHEBI, chebiGroups, chebisrc);
        }
        
        
        
        
        
    }
    
    
    

    public static void populateFilesForTestSets(Text text, Ontology ontology, List<Group> groups, String inputFilename) throws IOException, FileNotFoundException, ParseException, SQLException, OWLOntologyCreationException, NewOntologyException, ClassExpressionException, Exception{
        
        logger.info(String.format("working on terms from %s",ontology.toString()));
        
        List<Chunk> chunks = text.getAllAtomChunks();

        
        // Write the headers to the different output files.
        String evalHeader = "chunk,text,label,term,score,component,category,similarity,nodes";
        String classProbHeader = "chunk,term,prob,nodes";
        for (Group g: groups){
            g.classProbsPrinter.println(classProbHeader);
            g.evalPrinter.println(evalHeader);
        }
        
        HashMap<Integer,ArrayList<Result>> toolFoundTermsMap = new HashMap<>();

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
            String nodes = "";
            if (lineValues.length>3){
                nodes = lineValues[3].trim();
            }
            
            Result r = new Result();
            r.termID = termID;
            r.score = probability;
            r.nodes = nodes;
            
            ArrayList<Result> a = toolFoundTermsMap.getOrDefault(chunkID, new ArrayList<>());
            a.add(r);
            toolFoundTermsMap.put(chunkID, a);
        }
        
        Onto onto = new Onto(Config.ontologyPaths.get(ontology));
        
       
                
                
                
        
        
        for (Chunk c: chunks){
            ArrayList<String> curatedTermIDs = text.getCuratedEQStatementFromAtomID(c.chunkID).getAllTermIDs();
            ArrayList<Role> curatedTermRoles = text.getCuratedEQStatementFromAtomID(c.chunkID).getAllTermRoles();
            
            // What are the ID's of the terms that the tool found for this chunk of text?
            List<String> allTermIDsFoundBySearching = new ArrayList<>();
            for (Result result: toolFoundTermsMap.getOrDefault(c.chunkID, new ArrayList<>())){
                // Checking to verify the term predicted by the tool is on the owl file used.
                if (onto.getTermFromTermID(result.termID)!=null){
                    allTermIDsFoundBySearching.add(result.termID);
                }
                else{
                    logger.info(String.format("%s is in the predicted annotations but not in the owl file",result.termID));
                }
            }
            
            // Iterate through the curated terms for this chunk of text.
            for (int i=0; i<curatedTermIDs.size(); i++){
                String id = curatedTermIDs.get(i);
                Role role = curatedTermRoles.get(i);
                
                
                // Check if this curated term is from the ontology we're looking at right now and
                // that is also found in the ontology file that's being used for the rest of the pipeline.
                if (utils.Util.inferOntology(id).equals(ontology)){
                    if ( onto.getTermFromTermID(id)!=null){

                        // Find the false negatives.
                        if (!allTermIDsFoundBySearching.contains(id)){
                            String label = onto.getTermFromTermID(id).label;
                            // need to find the predicted term that is most similar to this FN curated term. (similarity = 0 for no predictions).
                            double simD = utils.Util.getMaxSimJac(id, allTermIDsFoundBySearching, onto);
                            String sim = String.format("%.3f",simD);
                            Object[] line = {c.chunkID, c.getRawText().replace(",", ""), label, id, "none", Role.getAbbrev(role), "FN", sim, "none"};
                            Utils.writeToEvalFiles(line, c, groups);
                        }
                        // Find the true positives.
                        else {
                            String joined = toolFoundTermsMap.get(c.chunkID).get(allTermIDsFoundBySearching.indexOf(id)).nodes;
                            double score = toolFoundTermsMap.get(c.chunkID).get(allTermIDsFoundBySearching.indexOf(id)).score;
                            String scoreStr = String.format("%.3f",score);
                            Object[] line = {c.chunkID, c.getRawText().replace(",", ""), onto.getTermFromTermID(id).label, id, scoreStr, Role.getAbbrev(role), "TP", "1.000", joined};
                            Utils.writeToEvalFiles(line, c, groups);
                        }
                    }
                    else {
                        logger.info(String.format("%s is in the curated annotations but not in the owl file",id));
                    }
                }
                //else {
                //  this term is from the wrong ontology or vocabulary.
                //}
            }
            
            
            // Iterate through all the terms found by the tool for this chunk. 
            for (Result result: toolFoundTermsMap.getOrDefault(c.chunkID, new ArrayList<>())){
                String id = result.termID;
                // Check if this ID passed the checks already done above.
                if (allTermIDsFoundBySearching.contains(id)){
                    if(!curatedTermIDs.contains(id) && utils.Util.inferOntology(id).equals(ontology)){
                        // Find the false positives.

                        // New code for this tool because it's using a version of the ontology not locally stored.
                        // Double check that the predicted term is actually present in the ontology file, can't use it if it's not.
                        if (onto.getTermFromTermID(id)!=null){
                            double simD = utils.Util.populateAttributes(c, onto.getTermFromTermID(id), text, onto, ontology).hJac;
                            String sim = String.format("%.3f",simD);
                            double score = toolFoundTermsMap.get(c.chunkID).get(allTermIDsFoundBySearching.indexOf(id)).score;
                            String scoreStr = String.format("%.3f",score);
                            String joined = result.nodes;
                            Object[] line = {c.chunkID, c.getRawText().replace(",",""), onto.getTermFromTermID(id).label, id, scoreStr, Role.getAbbrev(Role.UNKNOWN), "FP", sim, joined};
                            Utils.writeToEvalFiles(line, c, groups);
                        }
                        else {
                            logger.info(String.format("%s should never see this, already checking for this above",id));
                        }


                    }

                    // Note, also have to check here to make sure terms are only sent to the class probability file if they are present in the owl file.
                    // Put these terms in the class probabilities file, regardless of whether they are TP or FP, just include all mapped terms.
                    if (onto.getTermFromTermID(id)!=null){
                        String joined = result.nodes;
                        double score = toolFoundTermsMap.get(c.chunkID).get(allTermIDsFoundBySearching.indexOf(id)).score;
                        String scoreStr = String.format("%.3f",score);
                        Object[] line = {c.chunkID, id, scoreStr,joined};
                        Utils.writeToClassProbFiles(line, c, groups);
                    }
                    else {
                        logger.info(String.format("%s should never see this, already checking for this above",id));
                    }
                }
            }

            
        }
        
        // Close all the files.
        for (Group g: groups){
            g.classProbsPrinter.close();
            g.evalPrinter.close();
        }
 
    }
    
    
    
    
    
    
    public static class Result{
        public String termID;
        public String nodes;
        public double score;
    }
    
*/
    
}
