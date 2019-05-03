/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import composer.ComposerIO;
import composer.EQStatement;
import composer.Modifier;
import composer.Term;
import static composer.Utils.getInheritedEQs;
import config.Config;
import config.Connect;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraph;
import static edu.stanford.nlp.util.logging.RedwoodConfiguration.Handlers.file;
import enums.Ontology;
import enums.Role;
import enums.Species;
import enums.TextDatatype;
import infocontent.InfoContent;
import randomforest.index.FeatureIndices;
import java.io.File;
import randomforest.process.SimilarityFinder;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import main.Partitions;
import nlp.CoreNLP;
import nlp.MyAnnotation;
import nlp.MyAnnotation.Token;
import ontology.Onto;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import smile.classification.Maxent;
import objects.Attributes;
import objects.Chunk;
import objects.OntologyTerm;
import text.Text;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.NewOntologyException;
import uk.ac.ebi.brain.error.NonExistingEntityException;

/**
 *
 * @author irbraun
 */
public class NotCoverageTest {
    
    public NotCoverageTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    

    
    
    @Test
    public void testA() throws IOException, SQLException, FileNotFoundException, ParseException, OWLOntologyCreationException, NewOntologyException, ClassExpressionException, Exception{
 
        
        /*
        Config config = new Config("/Users/irbraun/NetBeansProjects/term-mapping/path/config/");
        Connect conn = new Connect();
        CoreNLP.setup(); 
        
        HashMap<Ontology,Onto> ontoObjects = utils.Utils.buildOntoObjects(Ontology.getPlantOntologies());
        Text text = new Text();
        
        EQStatement eq1 = text.getCuratedEQStatementFromAtomID(3);
        EQStatement eq2 = text.getCuratedEQStatementFromAtomID(4);

        InfoContent.setup(ontoObjects,text);

        
        System.out.println("similarity is");
        System.out.println(composer.Utils.getEQSimilarityNoWeighting(eq1, eq2, ontoObjects));
        
        
        //InfoContent.setup(ontoObjects,text);

        Config config = new Config("/Users/irbraun/NetBeansProjects/term-mapping/path/config/");
        Connect conn = new Connect();
        CoreNLP.setup(); 
        
        
        Text text = new Text();
        
        EQStatement eq = text.getCuratedEQStatementFromAtomID(3);
        
        
        HashMap<Ontology,Onto> ontoObjects = utils.Util.buildOntoObjects(Ontology.getSmallOntologies());
        for (EQStatement x: composer.Utils.getInheritedCuratedEQs(eq, ontoObjects)){
            System.out.println(x.toIDText());
            System.out.println(x.toLabelText(ontoObjects));
        }

        // sends all words from testing and training data to their own files.
        Config config = new Config("/Users/irbraun/NetBeansProjects/term-mapping/path/config/");
        Connect conn = new Connect();
        CoreNLP.setup(); 
        
        
        Text text = new Text();
        Partitions parts = new Partitions(text, TextType.ATOM, TextType.PHENOTYPE);
        List<Chunk> testingChunks = parts.getChunksInPartitionRangeInclusive(0, 4, text.getAllAtomChunks());
        List<Chunk> trainingChunks = parts.getChunksInPartitionRangeInclusive(0, 31, text.getAllAtomChunks());
        
       
        String testWordsFilename = "/Users/irbraun/NetBeansProjects/term-mapping/path/test_words.txt";
        String trainWordsFilename = "/Users/irbraun/NetBeansProjects/term-mapping/path/train_words.txt";
        File testWordsFile = new File(testWordsFilename);
        File trainWordsFile = new File(trainWordsFilename);
        PrintWriter testWordsPrinter = new PrintWriter(testWordsFile);
        PrintWriter trainWordsPrinter = new PrintWriter(trainWordsFile);
                
        
        HashSet<String> testWords = new HashSet<>();
        for (Chunk c: testingChunks){
            for (String word: c.getBagValues()){
                testWords.add(word);
            }
        }
        for (String word: testWords){
            testWordsPrinter.println(word);
        }
        
        HashSet<String> trainWords = new HashSet<>();
        for (Chunk c: trainingChunks){
            for (String word: c.getBagValues()){
                trainWords.add(word);
            }
        }
        for (String word: trainWords){
            trainWordsPrinter.println(word);
        }
        
        testWordsPrinter.close();
        trainWordsPrinter.close();
        
        
        
       
        Config config = new Config("/Users/irbraun/NetBeansProjects/term-mapping/");
        Connect conn = new Connect();
        CoreNLP.setup(); 
        
        
        Onto o = new Onto("/Users/irbraun/Desktop/owl/go.owl");
        
        OntologyTerm t1 = o.getTermFromTermID("GO_0048460"); // formation
        OntologyTerm t2 = o.getTermFromTermID("GO_0009908");
        OntologyTerm t3 = o.getTermFromTermID("GO_0042246"); // tissue regeneration
        OntologyTerm t4 = o.getTermFromTermID("GO_0031099"); //regeneration
        OntologyTerm t5 = o.getTermFromTermID("GO_0001890"); // placenta development
        
        
        
        
        System.out.println(o.getHierarchicalEvals(t1, t2)[3]);
        System.out.println(o.getHierarchicalEvals(t2, t1)[3]);
        System.out.println(o.getHierarchicalEvals(t2, t3)[3]);
        System.out.println(o.getHierarchicalEvals(t2, t4)[3]);
        System.out.println(o.getHierarchicalEvals(t2, t5)[3]);
        
        */
        
        
        
        
        
        
        
        
        
       /* 
       
        Config config = new Config("/Users/irbraun/NetBeansProjects/term-mapping/");
        Connect conn = new Connect();
        CoreNLP.setup();
        
        // Get a list of phenotype-related phrases from the plant trait ontology.
        {
        Onto o = new Onto("/Users/irbraun/Desktop/ie_results/to.owl");
        String filename = "/Users/irbraun/Desktop/phenotype-search/to_phrases.txt";
        File traitsFile = new File(filename);
        PrintWriter printer = new PrintWriter(traitsFile);
        for (OntologyTerm t: o.getTermList()){
            String label = t.label.replace("obsolete","").trim();
            printer.println(label);
            for (String s: t.exactSynonyms){
                String s_cleaned = s.replace("obsolete","").trim();
                printer.println(s_cleaned);
            }
            for (String s: t.relatedSynonyms){
                printer.println(s);
            }
            for (String s: t.broadSynonyms){
                printer.println(s);
            }
            for (String s: t.narrowSynonyms){
                printer.println(s);
            }
        }
        printer.close();
        }
        
        
        // Get a list of phenotype-related phrases from the phenotype and trait ontology.
        {
        Onto o = new Onto("/Users/irbraun/Desktop/owl/pato.owl");
        String filename = "/Users/irbraun/Desktop/phenotype-search/pato_phrases.txt";
        File traitsFile = new File(filename);
        PrintWriter printer = new PrintWriter(traitsFile);
        for (OntologyTerm t: o.getTermList()){
            String label = t.label.replace("obsolete","").trim();
            printer.println(label);
            for (String s: t.exactSynonyms){
                String s_cleaned = s.replace("obsolete","").trim();
                printer.println(s_cleaned);
            }
            for (String s: t.relatedSynonyms){
                printer.println(s);
            }
            for (String s: t.broadSynonyms){
                printer.println(s);
            }
            for (String s: t.narrowSynonyms){
                printer.println(s);
            }
        }
        printer.close();
        }
        
        
        
        
        
        Config config = new Config("/Users/irbraun/NetBeansProjects/term-mapping/");
        Connect conn = new Connect();
        CoreNLP.setup();
        
        Text text = new Text();
        System.out.println(text.getAllPhenotypeChunks().size()); //1952 previously now 2907.
        
        
        Config config = new Config("/Users/irbraun/NetBeansProjects/term-mapping/");
        Connect conn = new Connect();
        CoreNLP.setup();
        
        
        Text text = new Text();
        for (int i=1; i<200; i=i+10){
            Chunk c = text.getAtomChunkFromID(i);
            MyAnnotation a = Modifier.getAnnotation(c);
            System.out.println(c.getRawText());
            for (Token t: a.tokens){
                System.out.println(String.format("%s %s %s %s",t.nodeText, t.lemma, t.posTag, t.depIndex));
            }
        }

        Config config = new Config("/Users/irbraun/NetBeansProjects/term-mapping/");
        Connect conn = new Connect();
        CoreNLP.setup();
        
       
        System.out.println("testing combining class prob files to get ensemble performance");
        
        String f1 = "/Users/irbraun/Desktop/droplet/alpha2/nlp/nb/set2/outputs_test_ppn_pato/pato_test.fold1.classprobs.csv";
        String f2 = "/Users/irbraun/NetBeansProjects/term-mapping/dmom/NobleCoder/outputs_random_ppn_pato/pato_random.fold.classprobs.csv";
        
        ArrayList<String> l = new ArrayList<>();
        l.add(f1);
        l.add(f2);
        
        
        HashMap<Integer,ArrayList<Term>> chunkToTermsMap = ComposerIO.readClassProbFiles(l, 100);
        
        Text text = new Text();
        
        
        String s = "/Users/irbraun/Desktop/test.csv";
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
        System.out.println("done");
        
        
        
        
        
 
        Text text = new Text();
        Partitions p = new Partitions(text, TextType.ATOM, TextType.PHENOTYPE);  
        List<Chunk> cs = p.getChunksInPartitionRangeInclusive(0, 4, text.getAllAtomChunks());
        
        System.out.println(cs.size());
        
        HashSet<Chunk> phenotypes = new HashSet<>();
        for (Chunk c: cs){
            phenotypes.add(text.getPhenotypeChunkFromID(text.getPhenotypeIDfromAtomID(c.chunkID)));
        }
        System.out.println(phenotypes.size());
        
        
        int ctr = 0;
        for (Chunk c: cs){
            for (String id: text.getCuratedEQStatementFromAtomID(c.chunkID).getAllTermIDs()){
                if (utils.Util.inferOntology(id).equals(Ontology.PO)){
                    ctr++;
                }
            }
        }
        System.out.println(ctr);
        */
        
        
        
        
        
        
        
        
        /*
        System.out.println("maximum entropy model");
        
        int numSamples = 8;
        int p = 30; // dimension of the feature space
        int[][] x = new int[numSamples][p];
        int[] y = new int[numSamples];
        
        for (int i=0; i<numSamples; i++){
            for (int j=0; j<p; j++){
                x[i][j] = 0;
            }
        }
        y[0] = 1;
        y[1] = 1;
        y[2] = 1;
        
        // the 24th feature perfectly informs the outcome.
        x[0][24] = 1;
        x[1][24] = 1;
        x[2][24] = 1;
        Maxent m = new Maxent(p, x, y);
        
        System.out.println(m.predict(x[0]));
        System.out.println(m.predict(x[5]));
        */
        
    }
    
    
    /*
    @Test
    public void testGetExpectedAnnotationCounts() throws IOException, FileNotFoundException, SQLException, ParseException, Exception{
        Config config = new Config("/Users/irbraun/NetBeansProjects/term-mapping/");
        Connect conn = new Connect();
        CoreNLP.setup();
        
        Config.testingSet = "species";
        Text text = new Text();
        Partitions p = new Partitions(text, TextType.ATOM, TextType.ATOM);  // TOTO make sure that that second text type argument isn't actually being used....
        
        HashMap<Ontology,Integer> counts = new HashMap<>();
        for (Ontology o: Ontology.values()){
            counts.put(o, 0);
        }
        
        // Count the occurences of terms from each ontology in the training set.
        List<Chunk> trainingChunks = p.getChunksInPartitionRangeInclusive(0, 4, text.getAllAtomChunks());
        for (Chunk c: trainingChunks){
            for (String termID: text.getCuratedEQStatementFromAtomID(c.chunkID).getAllTermIDs()){
                int count = counts.get(utils.Util.inferOntology(termID));
                count++;
                counts.put(utils.Util.inferOntology(termID), count);
            }
        }
        
        // Output the ratios as term per chunk of text for each ontology.
        System.out.println("Expected ratios for each ontology");
        for (Ontology o: counts.keySet()){
            double ratio = (double) counts.get(o) / (double) trainingChunks.size();
            System.out.println(o.toString() + ": " + ratio);
        }
        
        
        
        
        
        
        
        
        

    }
    */
    
    
    
    
    
    
    
    
    
    /*
   
    @Test
    public void testDuplicates() throws IOException, FileNotFoundException, ParseException, SQLException, OWLOntologyCreationException, NewOntologyException, ClassExpressionException, NonExistingEntityException{
        Config config = new Config("/Users/irbraun/NetBeansProjects/term-mapping/");
        Connect conn = new Connect();
        CoreNLP.setup();
        
        //HashMap<Ontology,Onto> ontoObjects = new HashMap<>();
        //ontoObjects.put(Ontology.PATO, new Onto(Config.ontologyPaths.get(Ontology.PATO)));
        //ontoObjects.put(Ontology.PO, new Onto(Config.ontologyPaths.get(Ontology.PO)));
        //ontoObjects.put(Ontology.GO, new Onto(Config.ontologyPaths.get(Ontology.GO)));
        //ontoObjects.put(Ontology.CHEBI, new Onto(Config.ontologyPaths.get(Ontology.CHEBI)));
        
        
        //String outputFile = "/Users/irbraun/Desktop/duplicate_ids.csv";
        
        
        Config.typePartitions = "random";
        
        System.out.println("gathering text");
        Text text = new Text();
        System.out.println("done");
        // TODO make sure that second text type argument isn't actually being used....
        Partitions partitions = new Partitions(text, TextType.ATOM, TextType.ATOM); // no negative indices and no grouping by phenotypes.
        System.out.println("done partitioning");
        
        List<Integer> testParts = IntStream.rangeClosed(0,4).boxed().collect(Collectors.toList());
        List<Integer> trainParts = IntStream.rangeClosed(5,31).boxed().collect(Collectors.toList());

        List<Chunk> testingChunks = partitions.getChunksFromPartitions(testParts, text.getAllAtomChunks());
        List<Chunk> trainingChunks = partitions.getChunksFromPartitions(trainParts, text.getAllAtomChunks());
        
        HashSet<String> unique = new HashSet<>();
        ArrayList<Integer> duplicateChunkIDsInTestSet = new ArrayList<>();
        int ctr = 1;
        
        HashSet<Integer> phenotypeIDs = new HashSet<>();
        
        for (Chunk c: trainingChunks){
            String z = c.getRawText();
            String eq = text.getCuratedEQStatementFromAtomID(c.chunkID).toIDText();
            String instance = String.format("%s:%s",z,eq);
            unique.add(instance);
            ctr++;
            //System.out.println(String.format("%s/%s",ctr,trainingChunks.size()));
        }
        
        for (Chunk c: testingChunks){
            phenotypeIDs.add(text.getPhenotypeIDfromAtomID(c.chunkID));
            String z = c.getRawText();
            String eq = text.getCuratedEQStatementFromAtomID(c.chunkID).toIDText();
            String instance = String.format("%s:%s",z,eq);
            if (unique.contains(instance)){
                duplicateChunkIDsInTestSet.add(c.chunkID);
            }
        }
        
        System.out.println(String.format("%s of the %s testing instances were identical to training instances.",duplicateChunkIDsInTestSet.size(),testingChunks.size()));
        System.out.println(phenotypeIDs.size());
   
        try (PrintWriter writer = new PrintWriter(outputFile)) {
            for (int chunkID: duplicateChunkIDsInTestSet){
                writer.print(String.format("%s,",chunkID));
            }
            writer.close();
        }
        
        
        
        
        
    }
    
    */
    
    
    
    /*
    @Test
    public void testGS() throws IOException, FileNotFoundException, ParseException, SQLException, OWLOntologyCreationException, NewOntologyException, ClassExpressionException{
        Config config = new Config("/Users/irbraun/NetBeansProjects/term-mapping/");
        Connect conn = new Connect();
        //Text text1 = new Text();
        Text text2 = new Text("state");
        Text text3 = new Text("char");
        
        
        
        System.out.println(text2.getAtomChunkFromID(2).getRawText());
        System.out.println(text2.getAtomChunkFromID(229).getRawText());
        System.out.println(text3.getAtomChunkFromID(2).getRawText());
        System.out.println(text3.getAtomChunkFromID(229).getRawText());
        
        HashMap<Ontology,Onto> ontoObjects = new HashMap<>();
        ontoObjects.put(Ontology.UBERON, new Onto(Config.ontologyPaths.get(Ontology.UBERON)));
        System.out.println(ontoObjects.get(Ontology.UBERON).getTermListSize());
        
        
        
        
         
        
        System.out.println("done");
    }
    

    @Test
    public void testCNLP() throws IOException{
        CoreNLP.setup();
        CoreDocument doc = new CoreDocument("the plants are all reduced in height isn't that weird.");
        CoreNLP.getPipeline().annotate(doc);
        CoreSentence s = doc.sentences().get(0);
        
        SemanticGraph dG = s.dependencyParse();
        List<String> posTags = s.posTags();
        
        for (int i=0; i<dG.size(); i++){
            String node = dG.getNodeByIndex(i+1).originalText();
            String pos = posTags.get(i);
            System.out.println(pos + ":" + node);
        }
       
        
        

    }
    */
    
    
    
    
    
    
    
    /*
    
    @Test
    public void testSemanticSimilarity() throws SQLException, IOException, FileNotFoundException, ClassNotFoundException{
        SimilarityFinder finder = new SimilarityFinder();
        System.out.println("Semantic similarity functions");
        for (MetricSemantic metric: MetricSemantic.values()){
            double perfectSimilarity = finder.getSimilarity(metric, "dog", "dog");
            double highSimilarity = finder.getSimilarity(metric, "dog", "cat");
            double lowerSimilarity = finder.getSimilarity(metric, "dog", "table");
            System.out.println(metric.toString() + " " + perfectSimilarity + " " + highSimilarity + " " + lowerSimilarity);
        }
    }
    
    
    @Test
    public void testSyntacticSimilarity() throws SQLException, IOException, FileNotFoundException, ClassNotFoundException{
        SimilarityFinder finder = new SimilarityFinder();
        System.out.println("Syntactic similarity functions");
        for (MetricSyntactic metric: MetricSyntactic.getSimilarityMetrics()){
            double perfectSimilarity = finder.getSimilarity(metric, "dog", "dog");
            double highSimilarity = finder.getSimilarity(metric, "dog", "cat");
            double lowerSimilarity = finder.getSimilarity(metric, "dog", "table");
            System.out.println(metric.toString() + " " + perfectSimilarity + " " + highSimilarity + " " + lowerSimilarity);
        }
    }
    
    
    @Test
    public void testSyntacticDistance() throws SQLException, IOException, FileNotFoundException, ClassNotFoundException{
        SimilarityFinder finder = new SimilarityFinder();
        System.out.println("Syntactic distance functions");
        for (MetricSyntactic metric: MetricSyntactic.getDistanceMetrics()){
            double perfectSimilarity = finder.getSimilarity(metric, "dog", "dog");
            double highSimilarity = finder.getSimilarity(metric, "dog", "cat");
            double lowerSimilarity = finder.getSimilarity(metric, "dog", "table");
            System.out.println(metric.toString() + " " + perfectSimilarity + " " + highSimilarity + " " + lowerSimilarity);
        }
    }
    
    
    
    
   
    
    
    

    @Test
    public void testListKeys(){
        // Making sure using lists of strings as keys works as intended.
        // (Recreating the list even if its in a different object does recreate the valid key).
        Map<List<String>, Integer> featureIndexMap = new HashMap<>();
        List featureKey1 = Arrays.asList("a", "b", "c");
        featureIndexMap.put(featureKey1, 1);
        List featureKey2 = Arrays.asList("a", "b", "c");
        List featureKey3 = Arrays.asList("a", "c", "c");
        System.out.println(featureIndexMap.get(featureKey1));
        System.out.println(featureIndexMap.get(featureKey2));
        System.out.println(featureIndexMap.get(featureKey3));
    }
    
    
    
    
    
    
    @Test
    public void testRegex(){        
        String s = " And heres_ the String okay  -a_ ";
        String c = s.trim();   
        String[] tokensMethod1 = c.split("\\s+|_|-|,");
        String[] tokensMethod2 = c.split("[\\s+|_|-|,]+");
        String[] tokensMethod3 = c.replaceAll("\\s+|_|-|,", " ").split("\\s+");
        System.out.println("Tokens of method 1");
        for (String token: tokensMethod1){
            System.out.print(token + " ");
        }
        System.out.println();
        System.out.println("Tokens from method 2");
        for (String token: tokensMethod2){
            System.out.print(token + " ");
        }
        System.out.println();
        System.out.println("Tokens from method 3");
        for (String token: tokensMethod3){
            System.out.print(token + " ");
        }
        System.out.println();
    }
   

    */
    
    
}
