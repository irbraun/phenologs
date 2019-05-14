  
package config;

import enums.Metric;
import enums.Side;
import enums.Aspect;
import enums.Ontology;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import static main.Main.logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Config {
    
    public static String text;
    public static String format;
    public static String connPath;
    public static String stopWordsPath;
    public static String allWordsPath;
    public static String allPairsPath;
    public static String ontologyName;
    public static String savedPath;
    public static String csvPath;
    public static String csvName;
    public static int seedValue;
    public static boolean useSaveStore;
    public static int numPartitions;
    public static boolean useStemmer;
    public static boolean removeStopWords;
    public static String subsetsInputPath;
    public static String subsetsOutputPath;
    public static String predefinedSimilaritiesPath;
    
    public static boolean checkFuzzyScore;
    public static String passedInName;
    
    public static boolean undersample;
    public static int maxNegRetain;
    public static ArrayList<Integer> fullPartitions;
    
    public static String predictedStmtsPath;
    public static int maxTermsUsedPerOntology;
    
    public static String nbPath;
    public static String ncPath;
    public static String mePath;
    public static String naPath;
    public static String ncSourceFilePATO;
    public static String ncSourceFilePO;
    public static String ncSourceFileGO;
    public static String ncSourceFileChEBI;
    public static String naSourceFilePATO;
    public static String naSourceFilePO;
    public static String naSourceFileGO;
    public static String naSourceFileChEBI;
    
    public static boolean usePrior;
    public static boolean useLemmas;
    public static boolean useEmbeddings;
    public static double maxentTol;
    public static int maxFeaturesPerTerm;
    public static boolean buildNetworks;
    public static String pheneNetworkPath;
    public static String phenotypeNetworkPath;
    public static String pheneNodesPath;
    public static String phenotypeNodesPath;

    public static HashMap<Ontology,String> varImpPaths;
    public static HashMap<Ontology,String> ontologyPaths;
    public static HashMap<Ontology,List<String>> classProbsPaths;
    public static List<String> qFilesForDependencyParsing;
    public static List<String> otherFilesForDependencyParsing;

    public static String distributionsPath;
    public static String distributionsSerialName;
    
    public static boolean quick;
    public static int quickLimit;
    public static String species;
    
    // Stuff that has defaults, doesn't need to be in the config files.
    public static String dataTable;
    public static String annotCharTable;
    public static String annotEntTable;
    public static String convTable;
    public static String tempCharTable;
    public static String tempEntTable;
    public static String charparTextPath;
    public static String charparOutputPath;
    public static boolean removeDuplicates;
    public static boolean dynamicOverlaps;
    
    // From the JSON containing information about features.
    public static Side numEdges;
    public static Side weighting;
    public static List<Metric> normalFunctions;
    public static List<Metric> contextFunctions;
    public static List<Aspect> normalAspects;
    public static List<Aspect> contextAspects;
    
    
    
    
    
    
    public Config(String path) throws FileNotFoundException, IOException, ParseException, Exception{
        String generalPropertiesFilename = String.format("%sconfig.properties", path);
        String featureJSONFilename = String.format("%sconfig.json", path);
        readGeneralProperties(generalPropertiesFilename);
        readJSON(featureJSONFilename);
    }
    
    
    
    
    private void readGeneralProperties(String filename) throws FileNotFoundException, IOException{
        
        File file = new File(filename);
        
        Properties properties = new Properties();
        FileInputStream in = new FileInputStream(file);
        properties.load(in);

        // General properties.
        text = properties.getProperty("text").trim();
        //format = properties.getProperty("format").trim();
        connPath = properties.getProperty("connPath").trim();
        stopWordsPath = properties.getProperty("stopPath").trim();
        allWordsPath = properties.getProperty("allWordsPath").trim();
        allPairsPath = properties.getProperty("allPairsPath").trim();
        numPartitions = Integer.valueOf(properties.getProperty("numPartitions"));
        seedValue = Integer.valueOf(properties.getProperty("seedValue"));
        subsetsInputPath = properties.getProperty("categories_path").trim();
        predefinedSimilaritiesPath = properties.getProperty("predefined_path").trim();
        
        
        // Files containing the ontologies to be used.
        String owlPath = properties.getProperty("owlPath");
        ontologyPaths = new HashMap<>();
        ontologyPaths.put(Ontology.PATO, String.format("%s%s",owlPath,properties.getProperty("patoFilename")).trim());
        ontologyPaths.put(Ontology.PO, String.format("%s%s",owlPath,properties.getProperty("poFilename")).trim());
        ontologyPaths.put(Ontology.GO, String.format("%s%s",owlPath,properties.getProperty("goFilename")).trim());
        ontologyPaths.put(Ontology.CHEBI, String.format("%s%s",owlPath,properties.getProperty("chebiFilename")).trim());
        ontologyPaths.put(Ontology.UBERON, String.format("%s%s", owlPath,properties.getProperty("uberonFilename")).trim());
        
        // Semantic annotation tools.
        String baseDir = properties.getProperty("baseDir").trim();
        nbPath = String.format("%s%s",baseDir,properties.getProperty("naivebayesPath").trim());
        mePath = String.format("%s%s",baseDir,properties.getProperty("maxentropyPath").trim());
        ncPath = String.format("%s%s",baseDir,properties.getProperty("noblecoderPath").trim());
        naPath = String.format("%s%s",baseDir,properties.getProperty("ncboannotatorPath").trim());
        
        // Class probability files from running outside services on the entirety of the data.
        naSourceFilePATO = String.format("%s%s",baseDir,properties.getProperty("ncboannotatorSourceFilePATO").trim());
        naSourceFilePO = String.format("%s%s",baseDir,properties.getProperty("ncboannotatorSourceFilePO").trim());
        naSourceFileGO = String.format("%s%s",baseDir,properties.getProperty("ncboannotatorSourceFileGO").trim());
        naSourceFileChEBI = String.format("%s%s",baseDir,properties.getProperty("ncboannotatorSourceFileChEBI").trim());
        ncSourceFilePATO = String.format("%s%s",baseDir,properties.getProperty("noblecoderSourceFilePATO").trim());
        ncSourceFilePO = String.format("%s%s",baseDir,properties.getProperty("noblecoderSourceFilePO").trim());
        ncSourceFileGO = String.format("%s%s",baseDir,properties.getProperty("noblecoderSourceFileGO").trim());
        ncSourceFileChEBI = String.format("%s%s",baseDir,properties.getProperty("noblecoderSourceFileChEBI").trim());
        
               
        // Optional, some not currently used, check about removing these.
        dataTable = properties.getProperty("dataTable", "ppndata");
        tempCharTable = properties.getProperty("tempCharTable", "char_temp");
        tempEntTable = properties.getProperty("tempEntTable", "ent_temp");
        annotCharTable = properties.getProperty("annotCharTable", "char_annotations");
        annotEntTable = properties.getProperty("annotEntTable", "ent_annotations");
        convTable = properties.getProperty("convTable", "conversions");
        charparTextPath = properties.getProperty("charparTextPath", "doesnt matter");
        charparOutputPath = properties.getProperty("charparOutputPath", "doesnt matter");
        removeDuplicates = Boolean.parseBoolean(properties.getProperty("removeDuplicates", "false"));
        dynamicOverlaps = Boolean.parseBoolean(properties.getProperty("dynamicOverlaps", "true"));
    }
    
    
    
    
    private void readJSON(String filename) throws FileNotFoundException, IOException, ParseException, Exception{
        
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(filename));
        JSONObject json = (JSONObject) obj;
       
        
        // Look for optional parts of the config json file used in older experiments.
        try{
            // Feature architecture and other options for features.
            JSONObject features = (JSONObject) json.get("features");
            ontologyName = features.get("ontology").toString();
            useSaveStore = Boolean.parseBoolean(features.get("use_saved").toString());
            savedPath = features.get("saved_path").toString();
            useStemmer = Boolean.parseBoolean(features.get("use_stemmer").toString());
            removeStopWords = Boolean.parseBoolean(features.get("remove_stopwords").toString());
            csvPath = features.get("data_path").toString();
            csvName = features.get("data_name").toString();
            
            JSONObject undersampling = (JSONObject) features.get("undersampling");
            undersample = Boolean.parseBoolean(undersampling.get("undersample").toString());
            maxNegRetain = Integer.valueOf(undersampling.get("max_neg_retain").toString());
            fullPartitions = utils.Utils.getNumericList(undersampling.get("full_partitions").toString());

            JSONObject featureArchitecture = (JSONObject) features.get("feature_architecture");
            numEdges = Side.valueOf(featureArchitecture.get("num_edges").toString());
            weighting = Side.valueOf(featureArchitecture.get("weighting").toString());

            JSONObject normal = (JSONObject) features.get("normal");
            JSONArray normalSemanticFunctionsArr = (JSONArray) normal.get("semantic_functions");
            JSONArray normalSyntacticFunctionsArr = (JSONArray) normal.get("syntactic_functions");
            JSONArray normalAspectsArr = (JSONArray) normal.get("aspects");       
            normalFunctions = new ArrayList<>();
            normalFunctions.addAll(fillFunctionListFromIter(normalSemanticFunctionsArr.iterator()));
            normalFunctions.addAll(fillFunctionListFromIter(normalSyntacticFunctionsArr.iterator()));
            normalAspects = new ArrayList<>();
            normalAspects.addAll(fillAspectListFromIter(normalAspectsArr.iterator()));

            JSONObject context = (JSONObject) features.get("context");
            JSONArray contextSemanticFunctionsArr = (JSONArray) context.get("semantic_functions");
            JSONArray contextSyntacticFunctionsArr = (JSONArray) context.get("syntactic_functions");
            JSONArray contextAspectsArr = (JSONArray) context.get("aspects");
            contextFunctions = new ArrayList<>();
            contextFunctions.addAll(fillFunctionListFromIter(contextSemanticFunctionsArr.iterator()));
            contextFunctions.addAll(fillFunctionListFromIter(contextSyntacticFunctionsArr.iterator()));
            contextAspects = new ArrayList<>();
            contextAspects.addAll(fillAspectListFromIter(contextAspectsArr.iterator()));
            
            JSONObject testingOptions = (JSONObject) json.get("testing");
            quick = Boolean.parseBoolean(testingOptions.get("quick").toString());
            quickLimit = Integer.valueOf(testingOptions.get("quick_limit").toString());
            species = testingOptions.get("species").toString();
        }
        catch (Exception e){
            logger.info("some options specific to random forests were not provided");
        }
        

        // Options for the composer step.
        JSONObject composer = (JSONObject) json.get("composer");
        maxTermsUsedPerOntology = Integer.valueOf(composer.get("max_terms_per_ontology").toString());
        predictedStmtsPath = composer.get("output_path").toString();
        
        classProbsPaths = new HashMap<>();
        JSONObject classProbs = (JSONObject) composer.get("class_probs");
        JSONArray patoClassProbs = (JSONArray) classProbs.get("pato");
        JSONArray poClassProbs = (JSONArray) classProbs.get("po");
        JSONArray goClassProbs = (JSONArray) classProbs.get("go");
        JSONArray chebiClassProbs = (JSONArray) classProbs.get("chebi");        
        classProbsPaths.put(Ontology.PATO, fillStringListFromIter(patoClassProbs.iterator()));
        classProbsPaths.put(Ontology.PO, fillStringListFromIter(poClassProbs.iterator()));
        classProbsPaths.put(Ontology.GO, fillStringListFromIter(goClassProbs.iterator()));
        classProbsPaths.put(Ontology.CHEBI, fillStringListFromIter(chebiClassProbs.iterator()));
        
        JSONObject dgFiles = (JSONObject) composer.get("dg_files");
        JSONArray patoDGFiles = (JSONArray) dgFiles.get("q");
        JSONArray otherDGFiles = (JSONArray) dgFiles.get("other");
        qFilesForDependencyParsing = new ArrayList<>();
        qFilesForDependencyParsing.addAll(fillStringListFromIter(patoDGFiles.iterator()));
        otherFilesForDependencyParsing = new ArrayList<>();
        otherFilesForDependencyParsing.addAll(fillStringListFromIter(otherDGFiles.iterator()));
        
        JSONObject network = (JSONObject) composer.get("network");
        buildNetworks = Boolean.parseBoolean(network.get("network_values").toString());
        pheneNetworkPath = network.get("phene_network").toString();
        phenotypeNetworkPath = network.get("phenotype_network").toString();   
        pheneNodesPath = network.get("phene_nodes_path").toString().trim();
        phenotypeNodesPath = network.get("phenotype_nodes_path").toString().trim();
        
        
        
        // Options relevant to using the NLP pipeline. Note, also used for the composer.  
        JSONObject nlp = (JSONObject) json.get("nlp");
                
        usePrior = Boolean.parseBoolean(nlp.get("use_prior").toString());
        useLemmas = Boolean.parseBoolean(nlp.get("use_lemmas").toString());
        useEmbeddings = Boolean.parseBoolean(nlp.get("use_embeddings").toString());
        String maxentTolStr = nlp.get("maxent_tol").toString().trim();
        switch (maxentTolStr) {
            case "neg_four":
                maxentTol = 1E-4;
                break;
            case "neg_five":
                maxentTol = 1E-5;
                break;
            case "neg_six":
                maxentTol = 1E-6;
                break;
            case "neg_seven":
                maxentTol = 1E-7;
                break;
            case "neg_eight":
                maxentTol = 1E-8;
                break;
            default:
                throw new Exception();
        }
        maxFeaturesPerTerm = Integer.valueOf(nlp.get("max_features_per_term").toString());
    }
    
    
    
   
    private ArrayList<Metric> fillFunctionListFromIter(Iterator itr){
        ArrayList<Metric> list = new ArrayList<>();
        while (itr.hasNext()){
            list.add(Metric.valueOf(itr.next().toString()));
        }
        return list;
    }
    
    private ArrayList<Aspect> fillAspectListFromIter(Iterator itr){
        ArrayList<Aspect> list = new ArrayList<>();
        while (itr.hasNext()){
            list.add(Aspect.valueOf(itr.next().toString()));
        }
        return list;
    }
    
    private ArrayList<String> fillStringListFromIter(Iterator itr){
        ArrayList<String> list = new ArrayList<>();
        while (itr.hasNext()){
            list.add(itr.next().toString());
        }
        return list;
    }
    
    private ArrayList<Double> fillDoubleListFromIter(Iterator itr){
        ArrayList<Double> list = new ArrayList<>();
        while (itr.hasNext()){
            list.add(Double.valueOf(itr.next().toString()));
        }
        return list;
    }
    
    
    
    
    
}
