/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package utils;


import config.Config;
import enums.Ontology;
import config.Connect;
import enums.Species;
import enums.TextDatatype;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import main.Partitions;
import ontology.Onto;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import objects.Attributes;
import objects.Chunk;
import objects.OntologyTerm;
import text.Text;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.NewOntologyException;

/**
 *
 * @author irbraun
 */
public class Utils {
    
    
    
    
    public static List<Integer> range(int min, int max){
        return IntStream.rangeClosed(min, max).boxed().collect(Collectors.toList());
    }
    public static List<Integer> range(int min1, int max1, int min2, int max2){
        ArrayList<Integer> r = new ArrayList<>();
        r.addAll(IntStream.rangeClosed(min1,max1).boxed().collect(Collectors.toList()));
        r.addAll(IntStream.rangeClosed(min2,max2).boxed().collect(Collectors.toList()));
        return r;
    }
    public static int product(ArrayList<Integer> l){
        int product = 1;
        for (int i: l){
            product = product*i;
        }
        return product;
    }
    public static double mean(ArrayList<Double> l){
        double sum = 0.00;
        for (double d: l){
            sum = sum + d;
        }
        double mean = sum / (double) l.size();
        return mean;
    }
    
    
    
    
   
    public static ArrayList<Integer> getNumericList(String cs){
        List<String> csl = Arrays.asList(cs.split(","));
        ArrayList<Integer> csln = new ArrayList<>();
        for (String s: csl){
            csln.add(Integer.valueOf(s));
        }
        return csln;
    }
    
    
    public static ResultSet sqliteCall(String stmtStr) throws SQLException{
        Statement stmt = Connect.conn.createStatement();
        stmt.execute(stmtStr);
        ResultSet rs = stmt.getResultSet();
        return rs;
    }
    
    
    public static Ontology inferOntology(String s){
        if (s.toLowerCase().contains("pato")){
            return Ontology.PATO;
        }
        else if (s.toLowerCase().contains("po")){
            return Ontology.PO;
        }
        else if (s.toLowerCase().contains("go")){
            return Ontology.GO;
        }
        else if (s.toLowerCase().contains("chebi")){
            return Ontology.CHEBI;
        }
        else if (s.toLowerCase().contains("uberon")){
            return Ontology.UBERON;
        }
        else {
            return Ontology.UNSUPPORTED;
        }
    }
    
    
    public static TextDatatype inferTextType(String s){
        switch (s) {
        case "atomized":
        case "atom":
        case "phenes":
        case "phene":
            return TextDatatype.PHENE;
        case "phenotype":
        case "phenotypes":
            return TextDatatype.PHENOTYPE;
        case "split":
        case "splitphenotypes":
        case "splitphenotype":
        case "split_phenotype":
        case "split_phenotypes":
            return TextDatatype.SPLIT_PHENOTYPE;
        default:
            return null;
        }
    }
    
    
    public static Species inferSpecies(String s){
        if (s.toLowerCase().contains("arabidopsis")){
            return Species.ARABIDOPSIS_THALIANA;
        }
        else if (s.toLowerCase().contains("zea")){
            return Species.ZEA_MAYS;
        }
        else if (s.toLowerCase().contains("solanum")){
            return Species.SOLANUM_LYCOPERSICUM;
        }
        else if (s.toLowerCase().contains("oryza")){
            return Species.ORYZA_SATIVA;
        }
        else if (s.toLowerCase().contains("medicago")){
            return Species.MEDICAGO_TRUNCATULA;
        }
        else if (s.toLowerCase().contains("glycine")){
            return Species.GLYCINE_MAX;
        }
        else {
            return Species.UNKNOWN;
        }
    }
    
    
    
    public static String pickOntologyPath(String ontologyName) throws Exception{
        Ontology ontology = inferOntology(ontologyName);
        return Config.ontologyPaths.get(ontology);
    }
    
    
    
    public static List<Double> normalize(List<Double> list){
        double sum = 0.00;
        for (double element: list){
            sum += element;
        }    
        for (int i=0; i<list.size(); i++){
            list.set(i, (double) list.get(i) / (double) sum);
        }
        return list;
    }
    
    
    
    public static double[] normalize(double[] arr){
        double sum = 0.00;
        for (int i=0; i<arr.length; i++){
            sum += arr[i];
        }    
        for (int i=0; i<arr.length; i++){
            arr[i] = arr[i] / (double) sum;
        }
        return arr;
    }
    
    
    
    
    // This version of the methods doesn't have any of the partition information.
    public static Attributes populateAttributes(Chunk chunk, OntologyTerm term, Text text, Onto onto, Ontology ontology){
        
       
        
        Attributes attrib = new Attributes(chunk.chunkID, term.termID);
        
        // Some initital attributes having to do with target values.
        attrib.match = text.getAllTermIDs(chunk.chunkID, chunk.textType).contains(term.termID);
        if (attrib.match){
            int index = text.getAllTermIDs(chunk.chunkID, chunk.textType).indexOf(term.termID);
            attrib.role = text.getAllTermRoles(chunk.chunkID, chunk.textType).get(index).toString();
        }
        else {
            attrib.role = "none";
        }
                
        // Some mathematical attributes having to do with target values.
        double hPrecisionMax = 0.00;
        double hRecallMax = 0.00;
        double hJacMax = 0.00;
        double hF1Max = 0.00;
        String hpMaxer = "none";
        String hrMaxer = "none";
        String hjMaxer = "none";
        String hfMaxer = "none";
        for (String curatedTermID: (List<String>) text.getAllTermIDs(chunk.chunkID, chunk.textType)){
            // Check to make sure this curated term a) applies to this ontology b) is supported.
            if (inferOntology(curatedTermID).equals(ontology)){
                OntologyTerm curatedTerm = onto.getTermFromTermID(curatedTermID);
                if (curatedTerm != null){
                    double[] hierVals = onto.getHierarchicalEvals(term, curatedTerm);
                    double hPrec = hierVals[0];
                    double hRec = hierVals[1];
                    double hF1 = hierVals[2];
                    double hJac = hierVals[3];
                    if (hPrec >= hPrecisionMax){
                        hPrecisionMax = hPrec;
                        hpMaxer = curatedTermID;
                    }
                    if (hRec >= hRecallMax){
                        hRecallMax = hRec;
                        hrMaxer = curatedTermID;
                    }
                    if (hJac >= hJacMax){
                        hJacMax = hJac;
                        hjMaxer = curatedTermID;
                    }
                    if (hF1 >= hF1Max){
                        hF1Max = hF1;
                        hfMaxer = curatedTermID;
                    }
                }
            }
        }
        attrib.hPrecision = hPrecisionMax;
        attrib.hRecall = hRecallMax;
        attrib.hF1 = hF1Max;
        attrib.hJac = hJacMax;
        
        attrib.hpMaxer = hpMaxer;
        attrib.hrMaxer = hrMaxer;
        attrib.hfMaxer = hfMaxer;
        attrib.hjMaxer = hjMaxer;
        
        return attrib;
        
    }
    
    
    
    public static double getMaxSimJac(String termID1, List<String>termIDs, Onto onto){
        double maxSim = 0.000;
        OntologyTerm t1 = onto.getTermFromTermID(termID1);
        for (String termID2: termIDs){
            OntologyTerm t2 = onto.getTermFromTermID(termID2);
            double sim = onto.getHierarchicalEvals(t1, t2)[3];
            maxSim = Math.max(sim, maxSim);
        }
        return maxSim;
    }
    
    
    
    
    // Used with node representation that use a bar delimiter.
    // Using a system where nodes matching is not case sensitive.
    public static HashSet<String> getNodeSetFromString(String nodes){
        HashSet<String> nodeSet = new HashSet<>();
        String[] nodesArr = nodes.toLowerCase().trim().split("\\|");
        nodeSet.addAll(Arrays.asList(nodesArr));
        nodeSet.remove(""); // new make sure this works.
        return nodeSet;
    }
    
    
    
    
    // Make a mapping between ontology enums and their full objects.
    public static HashMap<Ontology,Onto> buildOntoObjects(List<Ontology> ontologies) throws OWLOntologyCreationException, NewOntologyException, ClassExpressionException{
        HashMap<Ontology,Onto> ontoObjects = new HashMap<>();
        for (Ontology o: ontologies){
            ontoObjects.put(o, new Onto(Config.ontologyPaths.get(o)));
        }
        return ontoObjects;
    }
    
    
    
    
    
    
    
    
    // The lower and upper partition numbers passed in are meant to be inclusive.
    public static List<Chunk> getChunksGivenPartitionRange(int lowerPartNum, int upperPartNum, List<Chunk> chunks, Partitions partitions) throws Exception{
        List<Integer> parts = IntStream.rangeClosed(lowerPartNum,upperPartNum).boxed().collect(Collectors.toList());
        List<Chunk> relevantChunks = partitions.getChunksFromPartitions(parts, chunks);
        return relevantChunks;
    }
    
    
    
    
    
    
}
