package randomforest.process;

import config.Config;
import enums.Metric;
import enums.Ontology;
import enums.TextDatatype;
import randomforest.index.FeatureIndices;
import objects.OntologyTerm;
import randomforest.objects.FeatureArray;
import objects.Chunk;
import randomforest.index.PairIndices;
import infocontent.InfoContent;
import randomforest.objects.SimilarityStore;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import main.Partitions;
import static main.Main.logger;
import nlp.CoreNLP;
import ontology.Onto;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import objects.Attributes;
import randomforest.objects.FeatureVector;
import text.Text;
import uk.ac.ebi.brain.error.NewOntologyException;
import utils.Utils;




public class Run {
    
    private Text text;
    private HashMap<Ontology,Onto> ontoObjects;
    private int testCtr;
    private ArrayList<OntologyTerm> terms;
    private List<Chunk> chunks;
    private HashSet<String> unsupportedTerms;
    private Partitions partitions;
    private SimilarityFinder finder;
    private TextDatatype format;
    
   
    
    
    public void run() throws OWLOntologyCreationException, NewOntologyException, SQLException, Exception{

        testCtr = 0;
        unsupportedTerms = new HashSet<>();
        format = Utils.inferTextType(Config.format);

        logger.info("initial setup");
        logger.info("creating indexing structures");
        FeatureIndices featureIdx = new FeatureIndices();
        PairIndices pairIdx = new PairIndices();
        
        
        
        ontoObjects = utils.Utils.buildOntoObjects(Ontology.getAllOntologies());
        
        
        
        
        logger.info("setting up text data");
        //text = new Text();
        //updating the set up for text data so that different datasets can be used. 
        //TODO clean all this up.
        
        System.out.println("AAA" + Config.text + "AAA");
        
        if (Config.text.equals("ppn")){
            text = new Text();
            InfoContent.setup(ontoObjects, text);
            chunks = text.getAllChunksOfDType(format);

            terms = ontoObjects.get(utils.Utils.inferOntology(Config.ontologyName)).getTermList();
            // Currently hardcoded to group the chunks (of any kind) into partitions based on phenotype.
            logger.info("partitioning");
            partitions = new Partitions(text);
            finder = new SimilarityFinder();
            logger.info("processing text in each partition");
            for (int part=0; part<Config.numPartitions; part++){
                runPart(part);
            }
            outputUnsupportedTerms();
        }
        /*
        else if (Config.text.equals("gs_char")){
            text = new Text("char");
            InfoContent.setup(ontoObjects, text);
            chunks = text.getChunksOfKind(format);

            terms = ontoObjects.get(utils.Util.inferOntology(Config.ontologyName)).getTermList();
            // Currently hardcoded to group the chunks (of any kind) into partitions based on atomized statements.
            logger.info("partitioning");
            partitions = new Partitions(text, format, TextType.ATOM, terms);
            finder = new SimilarityFinder();
            logger.info("processing text in each partition");
            for (int part=0; part<Config.numPartitions; part++){
                runPart(part);
            }
            outputUnsupportedTerms();
        }
        else if (Config.text.equals("gs_state")){
            text = new Text("state");
            InfoContent.setup(ontoObjects, text);
            chunks = text.getChunksOfKind(format);

            terms = ontoObjects.get(utils.Util.inferOntology(Config.ontologyName)).getTermList();
            // Currently hardcoded to group the chunks (of any kind) into partitions based on atomized statements.
            logger.info("partitioning");
            partitions = new Partitions(text, format, TextType.ATOM, terms);
            finder = new SimilarityFinder();
            logger.info("processing text in each partition");
            for (int part=0; part<Config.numPartitions; part++){
                runPart(part);
            }
            outputUnsupportedTerms();
        }
        */
        else {
            throw new Exception();
        }
        

    }
    
    
    
    
    

    
    
    private void runPart(int part) throws Exception{
        
        // Check if undersampling should be used on this partition or not.
        boolean fullPartition = isFullPartition(part);
        
        // Create a list of the chunks specific to this partition.
        logger.info(String.format("partition %s", part));
        List<Chunk> partitionChunks = partitions.getChunksFromPartitions(part, chunks);
        
        
        // Add pairs that will be used to the pair indexing structure. 
        // Every chunk will be represented, but if undersampling is used then some random terms will not be, with respect to parameters.
        addPairIndices(partitionChunks, fullPartition);        
        logger.info(String.format("%s pairs", PairIndices.getSize()));
        logger.info(String.format("%s features", FeatureIndices.getSize()));
        FeatureArray bigArray = new FeatureArray();
        
        
        // Add attributes for all the featues in this partition.
        logger.info("adding attributes");
        for (Chunk chunk: partitionChunks){  
            for (OntologyTerm term : terms){
                // If this <text,term> pair was dropped during undersampling, don't process it.
                if (PairIndices.getIndex(chunk.chunkID, term.termID) != -1){
                    Attributes attrib = populateAttributes(chunk, term);
                    bigArray.addAttributes(attrib);
                    if (testLimit()){                  
                        break;
                    }  
                }
            }
        }
        
        
        
        
        
        /*
        After this point any feature can be added as long as it's contained within a vector object (partial feature vector),
        because that feature vector object has the ID values necessary to place the contained feature values within the output.
        Organization of how the loops are set up is arbitrary, only done this way because it makes some memory saving steps easier,
        the features in feature vectors can actually be added to the output in any order and the full vectors can be done in as many
        partial steps as necessary. 
        */
        
        
        /*
        logger.info("adding dummy feature");
        for (Chunk chunk: partitionChunks){
            for (OntologyTerm term: terms){
                if (PairIndices.getIndex(chunk.chunkID, term.termID) != -1){
                    FeatureVector vector = PairEater.getDummyVector(chunk, term);
                    cube.addVector(vector);
                    if (testLimit()){
                        break;
                    }  
                }
            }
        }
        */
        
        
        
        
        /*
        Note, in the case were undersampling is used...
        Looping through the entire |c|*|t| space and then just processing the ones that happen to be found in the PairIndices hashmap
        is a bad way to do it when the partition is undersampled and the number of pairs that are used is rare. A better way to do it 
        would be to create a list used tuples in the PairIndices class that can be simply iterated through here instead of doing a
        nested loop through the whole search space. Add that below to the addPairIndices() function if it becomes necessary to speed up
        subsampled partitions.
        */
        
        
       
        // Add all the features that are based on semantic or syntactic similarity.
        logger.info("adding semantic and syntactic features");
        for (Metric metric: Config.normalFunctions){
            if (Config.useSaveStore){
                String storePostfix = String.format("%s.%s", Config.ontologyName.toLowerCase(), metric.toString().toLowerCase());
                SimilarityStore store = new SimilarityStore(storePostfix);
                finder.swapSaveStore(store);
            }
            for (Chunk chunk: partitionChunks){
                for (OntologyTerm term: terms){
                    if (PairIndices.getIndex(chunk.chunkID, term.termID) != -1){
                        FeatureVector vector = PairEater.getVector(finder, metric, chunk, term);
                        bigArray.addVector(vector);
                        if (testLimit()){
                            break;
                        }
                    }
                }
            }
        }
       
        
        /*
        Note, for the context-based features...
        These features are irrelevant when the entirety of the matrix is not produced for each chunk. In order
        to look at the value of other features along the path to the root from any given ontology term the entire
        fully sampled matrix needs to have been produced (every ontology term) for the text chunk in question.
        Therefore including context features only makes sense when undersampling is not done (full partitions) at
        least during the feature vector creation step, undersampling can always be done later.
        */
        
        
        // Add all the features that are based on context in the ontology.
        logger.info("adding context-based features");
        if (!Config.contextFunctions.isEmpty()){
            for (Chunk chunk: partitionChunks){
                for (OntologyTerm term: terms){
                    if (PairIndices.getIndex(chunk.chunkID, term.termID) != -1){
                        // Path from term to the root.
                        FeatureVector vectorRootPath = PairEater.getRootPathContextVector(chunk, term, bigArray);
                        bigArray.addVector(vectorRootPath);
                        // Sibling terms of the term.
                        FeatureVector vectorSiblings = PairEater.getSiblingContextVector(chunk, term, bigArray);
                        bigArray.addVector(vectorSiblings); 
                        if (testLimit()){
                            break;
                        }
                    }
                }
            }
        }
        

        
        
       
        
        
        
        
        
        
        
        
        
        
        
        
        /*
        logger.info("adding semantic features");
        for (MetricSemantic metric : UseFeatures.metricNamesSemantic){
            
            if (Config.useSaveStore){
                String storePostfix = String.format("%s.%s", Config.ontologyName.toLowerCase(), metric.toString().toLowerCase());
                SimilarityStore store = new SimilarityStore(storePostfix);
                finder.swapSaveStore(store);
            }
            
            Looping through the entire |c|*|t| space and then just processing the ones that happen to be found in the PairIndices hashmap
            is a bad way to do it when the partition is undersampled and the number of pairs that are used is rare. A better way to do it 
            would be to create a list used tuples in the PairIndices class that can be simply iterated through here instead of doing a
            nested loop through the whole search space. Add that below to the addPairIndices() function if it becomes necessary to speed up
            subsampled partitions.
            
            for (Chunk chunk: partitionChunks){ 

                for (OntologyTerm term : terms){
                    
                    if (PairIndices.getIndex(chunk.chunkID, term.termID) != -1){
                        FeatureVector vector = PairEater.getVector(finder, metric, chunk, term);
                        cube.addVector(vector);
                        if (testLimit()){
                            break;
                        }  
                    }
                }
            }            
        }
        */
        
        
        
        
        /*
        logger.info("adding syntactic features");
        for (MetricSyntactic metric : UseFeatures.metricNamesSyntactic){
            
            if (Config.useSaveStore){
                String storePostfix = String.format("%s.%s", Config.ontologyName.toLowerCase(), metric.toString().toLowerCase());
                SimilarityStore store = new SimilarityStore(storePostfix);
                finder.swapSaveStore(store);
            }
            
            for (Chunk chunk: partitionChunks){
                for (OntologyTerm term: terms){
                    if (PairIndices.getIndex(chunk.chunkID, term.termID) != -1){
                        FeatureVector vector = PairEater.getVector(finder, metric, chunk, term);
                        cube.addVector(vector);
                        if (testLimit()){
                            break;
                        } 
                    }
                }
            }
        }
        */
        
       
       
        /*
        These features are irrelevant when the entirety of the matrix is not produced for each chunk. In order
        to look at the value of other features along the path to the root from any given ontology term the entire
        fully sampled matrix needs to have been produced (every ontology term) for the text chunk in question.
        Therefore including context features only makes sense when undersampling is not done (full partitions) at
        least during the feature vector creation step, undersampling can always be done later.
        */
        /*
        logger.info("adding context features");
        if (doRootPathFeatures() || doSiblingFeatures()){
            for (Chunk chunk: partitionChunks){
                for (OntologyTerm term: terms){
                    if (PairIndices.getIndex(chunk.chunkID, term.termID) != -1){

                        // Inherited terms along the path to root.
                        if (doRootPathFeatures()){
                            FeatureVector vector = PairEater.getRootPathContextVector(chunk, term, cube);
                            cube.addVector(vector);
                        }
                        
                        // Sibling terms (share an immediate common parent).
                        if (doSiblingFeatures()){
                            FeatureVector vector2 = PairEater.getSiblingContextVector(chunk, term, cube);
                            cube.addVector(vector2);             
                        }
                        
                        if (testLimit()){
                            break;
                        }   
                    }
                }
            } 
        }
        */

        
        bigArray.writeData(part, fullPartition);
        PairIndices.clear();
        
    }
    
    
    
    
    private boolean testLimit(){
        testCtr++;
        if (Config.quick && (testCtr >= Config.quickLimit)){
            testCtr=0;
            return true;
        }
        return false;
    }
    
    
    private boolean isFullPartition(int part){
        if (Config.undersample){
            return Config.fullPartitions.contains(part);
        }
        else {
            return true;
        }
    }
    
    
    private void outputUnsupportedTerms() throws FileNotFoundException{
        Object[] data = {unsupportedTerms.size(), Config.ontologyName};
        logger.info(String.format("%s curated %s terms were not supported by provided ontology file", data));
        for (String unsuppTerm: unsupportedTerms){
            logger.info(unsuppTerm);
        }
    }
    
    
    private void addPairIndices(List<Chunk> partitionChunks, boolean testingPart){
        for (Chunk chunk: partitionChunks){
            // Each chunk should see a different ordering of pairs (different terms with this chunk) to keep as negative examples.
            //partitions.shuffleNegIndices();
            int index = 0;
            for (OntologyTerm term : terms){
                // Always include data which is either a match or is in a full partition.
                if (testingPart || text.getAllTermIDs(chunk.chunkID, chunk.textType).contains(term.termID)){
                    PairIndices.add(chunk.chunkID, term.termID);
                }
                // For any other data points, check whether they should be kept by looking at the randomized indices.
                // Only up to some maximum number of those will indicate the data point should be kept.
                /*
                else if (partitions.keepExample(index)){
                    PairIndices.add(chunk.chunkID, term.termID);
                }
                index++;
                */
            }
        }
    }
    
    
    private Attributes populateAttributes(Chunk chunk, OntologyTerm term){
        
        Attributes attrib = new Attributes(chunk.chunkID, term.termID);
        
        // Some initital attributes having to do with target values.
        attrib.match = text.getAllTermIDs(chunk.chunkID, chunk.textType).contains(term.termID);
        //attrib.partition = partitions.getPartitionNumber(chunk);
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
            if (curatedTermID.contains(Config.ontologyName)){   
                OntologyTerm curatedTerm = ontoObjects.get(utils.Utils.inferOntology(curatedTermID)).getTermFromTermID(curatedTermID);
                if (curatedTerm != null){
                    double[] hierVals = ontoObjects.get(utils.Utils.inferOntology(curatedTerm.termID)).getHierarchicalEvals(term, curatedTerm);
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
                else {
                    unsupportedTerms.add(curatedTermID);
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
    
    
    
    
    
}
