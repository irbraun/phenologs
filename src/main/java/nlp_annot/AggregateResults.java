/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package nlp_annot;

import composer.ComposerIO;
import composer.Term;
import config.Config;
import enums.Ontology;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import main.Group;
import main.Partitions;
import static nlp_annot.OutsideAnnotationReader.populateFilesForTestSets;
import org.json.simple.parser.ParseException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import text.Text;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.NewOntologyException;

/**
 *
 * @author irbraun
 */
public class AggregateResults {
    
    

    
    
    
    /**
     * Produce ensemble versions of the complete class probability files.
     * @param dir
     * @throws SQLException
     * @throws FileNotFoundException 
     */
    public static void makeCombinedFilesSet(String dir) throws SQLException, FileNotFoundException{
                
        List<String> patoFiles = new ArrayList<>();
        patoFiles.add(dir+"/noble/output_pato/group1_"+Config.format+"_classprobs.csv");
        patoFiles.add(dir+"/noble/output_pato/group2_"+Config.format+"_classprobs.csv");
        patoFiles.add(dir+"/naive/output_pato/merged_"+Config.format+"_classprobs.csv");
        patoFiles.add(dir+"/ncbo/output_pato/group1_"+Config.format+"_classprobs.csv");
        mergeFiles(patoFiles, dir+"/aggregate/output_pato/classprobs.csv");

        List<String> poFiles = new ArrayList<>();
        poFiles.add(dir+"/noble/output_po/group1_"+Config.format+"_classprobs.csv");
        poFiles.add(dir+"/noble/output_po/group2_"+Config.format+"_classprobs.csv");
        poFiles.add(dir+"/naive/output_po/merged_"+Config.format+"_classprobs.csv");
        poFiles.add(dir+"/ncbo/output_po/group1_"+Config.format+"_classprobs.csv");
        mergeFiles(poFiles, dir+"/aggregate/output_po/classprobs.csv");
        
        List<String> goFiles = new ArrayList<>();
        goFiles.add(dir+"/noble/output_go/group1_"+Config.format+"_classprobs.csv");
        goFiles.add(dir+"/noble/output_go/group2_"+Config.format+"_classprobs.csv");
        goFiles.add(dir+"/naive/output_go/merged_"+Config.format+"_classprobs.csv");
        goFiles.add(dir+"/ncbo/output_go/group1_"+Config.format+"_classprobs.csv");
        mergeFiles(goFiles, dir+"/aggregate/output_go/classprobs.csv");
        
        List<String> chebiFiles = new ArrayList<>();
        //chebiFiles.add(dir+"/noble/output_chebi/group1_"+Config.format+"_classprobs.csv");
        //chebiFiles.add(dir+"/noble/output_chebi/group2_"+Config.format+"_classprobs.csv");
        chebiFiles.add(dir+"/naive/output_chebi/merged_"+Config.format+"_classprobs.csv");
        chebiFiles.add(dir+"/ncbo/output_chebi/group1_"+Config.format+"_classprobs.csv");
        mergeFiles(chebiFiles, dir+"/aggregate/output_chebi/classprobs.csv");
    }
    
    
    
    
    
    private static void mergeFiles(List<String> files, String newFilename) throws FileNotFoundException{
        HashMap<Integer,ArrayList<Term>> chunkToTermsMap = ComposerIO.readClassProbFiles(files, 100);
        String s = newFilename;
        File classProbsFile = new File(s);
        String classProbHeader = "chunk,term,prob,nodes";
        PrintWriter cpPrinter = new PrintWriter(classProbsFile);
        cpPrinter.println(classProbHeader);
        for (int chunkID: chunkToTermsMap.keySet()){
            ArrayList<Term> ts = chunkToTermsMap.get(chunkID);
            for (Term t: ts){
                HashSet<String> nodes = t.nodes;
                String joinedNodes = nodes.stream().collect(Collectors.joining("|"));
                double prob = t.probability;
                String probStr = String.format("%.3f",prob);
                Object[] line = {chunkID,t.id,probStr,joinedNodes};
                cpPrinter.println(String.format("%s,%s,%s,%s",line));
            }   
        }
        cpPrinter.close();
    }
    
    
    
    
    
    
    
    
    /**
     * Create class probability and evaluation files for the testing sets.
     * Hardcoded to run both of the testing sets with hardcoded files specified for each of the 
     * different methods of semantic annotation and ontologies. To include more files just specify
     * them where the other ones are for now.
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ParseException
     * @throws SQLException
     * @throws OWLOntologyCreationException
     * @throws NewOntologyException
     * @throws ClassExpressionException
     * @throws Exception 
     */
    public static void run(String annotDir) throws IOException, FileNotFoundException, ParseException, SQLException, OWLOntologyCreationException, NewOntologyException, ClassExpressionException, Exception{
        
        
        makeCombinedFilesSet(annotDir);
        
        String baseDirectory = annotDir+"aggregate/";

        // Strings identifying aspects of the testing sets of phenotypes.
        String fold = "fold";
       
        // Partition numbers for each testing set.
        List<Integer> allPartitionNumbers = utils.Util.range(0, 31);
        List<Integer> set1PartitionNumbers = utils.Util.range(0, 4);
        
        // Text data and partition objects for each testing set.
        Text text = new Text();
        Partitions set1PartitionObj = new Partitions(text); 
       
        String outputPath = String.format("%s/%s",baseDirectory,"output_pato");
        List<Group> patoGroups = new ArrayList<>();
        patoGroups.add(new Group("group1", fold, allPartitionNumbers, outputPath, set1PartitionObj));
        populateFilesForTestSets(text, Ontology.PATO, patoGroups, outputPath+"/classprobs.csv");

        List<Group> poGroups = new ArrayList<>();
        outputPath = String.format("%s/%s",baseDirectory,"output_po");
        poGroups.add(new Group("group1", fold, allPartitionNumbers, outputPath, set1PartitionObj));
        populateFilesForTestSets(text, Ontology.PO, poGroups, outputPath+"/classprobs.csv");

        List<Group> goGroups = new ArrayList<>();
        outputPath = String.format("%s/%s",baseDirectory,"output_go");
        goGroups.add(new Group("group1", fold, allPartitionNumbers, outputPath, set1PartitionObj));
        populateFilesForTestSets(text, Ontology.GO, goGroups, outputPath+"/classprobs.csv");

        List<Group> chebiGroups = new ArrayList<>();
        outputPath = String.format("%s/%s",baseDirectory,"output_chebi");
        chebiGroups.add(new Group("group1", fold, allPartitionNumbers, outputPath, set1PartitionObj));
        populateFilesForTestSets(text, Ontology.CHEBI, chebiGroups, outputPath+"/classprobs.csv");
         
    }
    
   
}
