/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package nlp;


import config.Config;
import enums.TextDatatype;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static main.Main.logger;
import main.Partitions;
import structure.Chunk;
import text.Text;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.NewOntologyException;

/**
 * Handles all of the learning steps done for information extracted from the core NLP pipeline.
 * @author irbraun
 */
public class Learner {

    
    
    
    /**
     * This method currently learns distributions for dependencies on a set of atomized statements or 
     * phenotype descriptions. The text is annotated using the Stanford Core NLP pipeline, and then 
     * a distributions object is created and populated and written to files. 
     * @throws SQLException
     * @throws NewOntologyException
     * @throws ClassExpressionException
     * @throws IOException
     * @throws Exception 
     */
    public static void learn() throws SQLException, NewOntologyException, ClassExpressionException, IOException, Exception{
       
        logger.info("setting up and identifying text chunks to use for training");
        Text text = new Text();
        List<Chunk> chunks = text.getAllAtomChunks();


        // Training parts given where partitions 1 to 7 are used for testing.
        List<Integer> trainingParts = IntStream.rangeClosed(8, 31).boxed().collect(Collectors.toList());
        Partitions partitions = new Partitions(text, Config.typePartitions);
        List<Chunk> trainingChunks = partitions.getChunksFromPartitions(trainingParts, chunks);
        
        logger.info("parsing");
        Parser parser = new Parser();
        parser.learnDistributions(trainingChunks, ""); // hardcoding this blank string for the looptag here, don't need it at all really.
        
        
        
        
       
        
        /* OLD CODE THAT USES THE OUTER LOOP, new version that just does one split shown above.
        // Define the ranges of testing data on the partitions. These values and tags are hardcoded for now.
        Integer[] lowerBounds = {0,8,16,24};
        Integer[] upperBounds= {7,15,23,31};
        String[] cvTags = {"A", "B", "C", "D"};
        HashMap<String,List<Integer>> cvToPartMap = new HashMap<>();
        for (int i=0; i<cvTags.length; i++){
            cvToPartMap.put(cvTags[i], IntStream.rangeClosed(lowerBounds[i],upperBounds[i]).boxed().collect(Collectors.toList()));
        }
        
        
        // Inefficient way to do this, counting things three times. TODO fix this.
        // Also inefficient way to output the heatmaps during learning here as well, overwritten multiple times. TODO fix this.
        for (String testCVTag: cvTags){
            // Find all the partitions that can be used as training data in this cross-validation iteration.
            List<Integer> parts = new ArrayList<>();
            for (String trainCVTag: cvTags){
                if (!testCVTag.equals(trainCVTag)){
                    parts.addAll(cvToPartMap.get(trainCVTag));
                }
            }
            Partitions partitions = new Partitions(text, TextType.ATOM, TextType.PHENOTYPE);
            List<Chunk> trainingChunks = partitions.getChunksFromPartitions(parts, chunks);
            
            // Parsing.
            logger.info("parsing");
            Parser parser = new Parser();
            parser.learnDistributions(trainingChunks, testCVTag);
            parser.writeHeatMaps();
        }
        */
        

 
        
        
        /*
        // Dummy version for testing.
        List<Chunk> trainingChunks = new ArrayList<>();
        for (int i=200; i<300; i++){
            trainingChunks.add(chunks.get(i));
        }
        // Dummy version, testing ability to read distributions back in and calculate probabilities.
        Parser p2 = new Parser();
        p2.loadAnnotations(trainingChunks);
        p2.loadDistributions(Config.distributionsPath);
        for (Chunk chunk: trainingChunks){
            double prob = p2.getProbability(text.getCuratedEQStatementFromAtomID(chunk.chunkID), chunk);
        }
        */
        
        
        
        
        
    }
}
