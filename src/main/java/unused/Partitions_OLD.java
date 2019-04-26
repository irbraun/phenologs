/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package unused;

import config.Config;
import enums.Species;
import enums.TextDatatype;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import objects.Chunk;
import objects.OntologyTerm;
import text.Text;

/**
 *
 * @author irbraun
 */
public class Partitions_OLD {

    /*
    
    
    private final HashMap<Chunk,Integer> partitionMap;
    private final ArrayList<Integer> termUseIndices;
    private final Text text;
    private final String split;
    
    

    public Partitions_OLD(Text text, TextDatatype whatToPartition, TextDatatype withRespectTo, List<OntologyTerm> terms) throws SQLException{
        this.text = text;
        split = Config.typePartitions;
        partitionMap = new HashMap<>();
        termUseIndices = new ArrayList<>();
        generatePartitions(whatToPartition, withRespectTo);
        generateNegIndices(terms);
        
    }
    
    
    
    
    public Partitions_OLD(Text text, TextDatatype whatToPartition, TextDatatype withRespectTo) throws SQLException{
        this.text = text;
        split = Config.typePartitions;
        partitionMap = new HashMap<>();
        termUseIndices = new ArrayList<>();
        generatePartitions(whatToPartition, withRespectTo);
    }
    
    
    public Partitions_OLD(Text text, TextDatatype whatToPartition, TextDatatype withRespectTo, String splitStr) throws SQLException{
        this.text = text;
        split = splitStr;
        partitionMap = new HashMap<>();
        termUseIndices = new ArrayList<>();
        generatePartitions(whatToPartition, withRespectTo);
    }
    
    
    
    
    
    
    
    
    // Check which partition a chunk belongs to.
    public int getPartitionNumber(Chunk c){
        return partitionMap.get(c);
    }
    
    // Check decision to keep or discard a sample pair in the list for this partition.
    public boolean keepExample(int index){
        return termUseIndices.get(index) == 1;
    }
    
    
    
    
    
     * Should shuffle these indices when changing to a new chunk within a partition. 
     * All chunks within a partition are retained, but up to k of the negative samples (<c,t> pairs)
     * containing that chunk can be thrown away through undersampling.
     * NOTE: this one does NOT have a seed value, because we want random negative examples to be removed each time.
     * this is in contrast to the randomization of which chunks go in each partition, which needs to be 
     * reproduced across runs because different ontologies are used each time.
    public void shuffleNegIndices(){
        Collections.shuffle(termUseIndices, new Random());
    }
   
    
    
    

    private void generatePartitions(TextDatatype whatToPartition, TextDatatype withRespectTo) throws SQLException{
        
        // partitions 0 to 4 will have other species in them.
        if (split.equals("species")){
            generateSpeciesPartitions();
            return;
        }
        // random splist but with testing portion size consistent with above.
        else if (split.equals("random")){
            generateConsistentPartitions();
            return;
        }
        
        
        // other, random splits with equal sizes.
        List<Chunk> chunks;
        switch (withRespectTo) {
        case PHENOTYPE:
            chunks = text.getAllPhenotypeChunks();
            break;
        case PHENE:
            chunks = text.getAllAtomChunks();
            break;
        default:
            chunks = text.getAllAtomChunks();
            break;
        }
        
        // Create a part list that has one number in it for each chunk of the type we want to partition with respect to.
        ArrayList<Integer> partList = new ArrayList<>();
        int partSize = (int) chunks.size() / Config.numPartitions;
        for (int part=0; part<Config.numPartitions; part++){
            for (int i=0; i<partSize; i++){
                partList.add(part);
            }
        }
        for (int part=0; part<Config.numPartitions; part++){
            partList.add(part);
            if (partList.size() == chunks.size()){
                break;
            }
        }
        

        // If we want to use chunks of the same type that we're partitioning with respect to.
        if (whatToPartition.equals(withRespectTo)){
            Collections.shuffle(partList, new Random(Config.seedValue));
            for (int j=0; j<chunks.size(); j++){
                partitionMap.put(chunks.get(j), partList.get(j));
            }
        }
        
        // If we want to use atomized statement chunks but partition on them with respect to the phenotype they belong to.
        if (whatToPartition.equals(TextDatatype.PHENE) && withRespectTo.equals(TextDatatype.PHENOTYPE)){
            Collections.shuffle(partList, new Random(Config.seedValue));
            for (int j=0; j<chunks.size(); j++){
                // All the atomized statements belonging to this phenotype get assigned this phenotype's partition.
                for (int atomID: text.getAtomIDsFromPhenotypeID(chunks.get(j).chunkID)){
                    partitionMap.put(text.getAtomChunkFromID(atomID), partList.get(j));       
                }
                partitionMap.put(chunks.get(j), partList.get(j)); //TODO why is this here? why add a phenotype chunk to the partition map in this case?
            }
            
            
            
            
        }
        
        // If...
        // (other cases)
        
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    // Right now these are not being split up such that phenotypes remain within  a continuous partition.
    // That shouldn't be a problem because phenotypes in this case are always going to be within the same
    // training or testing group. Make sure this is right.
    private void generateSpeciesPartitions() throws SQLException{
        
        // Arabidopsis phenotype chunks.
        // TODO this is actually not doing with respect to phenotypes, make sure this is right.
        List<Chunk> araChunks = text.getChunksOfKindAndSpecies(TextDatatype.PHENE, Species.ARABIDOPSIS_THALIANA);
        
        // Create a list holding partition numbers ranging 5 to 31 that's as long as the list of arabidopsis atomized statement chunks.
        ArrayList<Integer> partList = new ArrayList<>();
        int partSize = (int) araChunks.size() / (Config.numPartitions-5);
        for (int part=5; part<Config.numPartitions; part++){
            for (int i=0; i<partSize; i++){
                partList.add(part);
            }
        }
        for (int part=5; part<Config.numPartitions; part++){
            partList.add(part);
            if (partList.size() == araChunks.size()){
                break;
            }
        }
        
        // Associate those partition numbers with specific chunks.
        for (int i=0; i<araChunks.size(); i++){
            partitionMap.put(araChunks.get(i), partList.get(i));
        }
        
        // Do the rest of the species, using partitions 0 to 4.
        int part = 0;
        for (Species species: Species.values()){
            if (!species.equals(Species.ARABIDOPSIS_THALIANA)){
                for (Chunk c: text.getChunksOfKindAndSpecies(TextDatatype.PHENE, species)){
                    partitionMap.put(c,part);
                }   
                part++;
            }
        }
    }
    

    private void generateConsistentPartitions() throws SQLException{
        
        // Split criteria is random but phenotypes should group and number of testing
        // phenotypes should be consisten with what happens when we group by species.
        int numTestingPhenotypes = 394;
        int numTestingPartitions = 5;
        int numTrainingPartitions = 27;
        List<Chunk> pChunks = text.getAllPhenotypeChunks();
        Collections.shuffle(pChunks, new Random(Config.seedValue));
        List<Chunk> testChunks = pChunks.subList(0, numTestingPhenotypes);
        List<Chunk> trainChunks = pChunks.subList(numTestingPhenotypes,pChunks.size());
        
        // Create a list of partition numbers for the testing phenotypes.
        ArrayList<Integer> testPartList = new ArrayList<>();
        int testPartSize = (int) testChunks.size() / (numTestingPartitions);
        for (int part=0; part<numTestingPartitions; part++){
            for (int i=0; i<testPartSize; i++){
                testPartList.add(part);
            }
        }
        for (int part=0; part<numTestingPartitions; part++){
            testPartList.add(part);
            if (testPartList.size() == testChunks.size()){
                break;
            }
        }
        
        // Create a list of partition numbers for the training phenotypes.
        ArrayList<Integer> trainPartList = new ArrayList<>();
        int trainPartSize = (int) trainChunks.size() / (numTrainingPartitions);
        for (int part=5; part<Config.numPartitions; part++){
            for (int i=0; i<trainPartSize; i++){
                trainPartList.add(part);
            }
        }
        for (int part=5; part<Config.numPartitions; part++){
            trainPartList.add(part);
            if (trainPartList.size() == trainChunks.size()){
                break;
            }
        }
        
        // Associate atomized statement text chunks to specific test partitions.
        for (int j=0; j<testChunks.size(); j++){
            for (Chunk c: text.getAtomChunksFromIDs(text.getAtomIDsFromPhenotypeID(testChunks.get(j).chunkID))){
                partitionMap.put(c, testPartList.get(j));
            }
        }
        // Associate atomized statement text chunks to specific training partitions.
        for (int j=0; j<trainChunks.size(); j++){
            for (Chunk c: text.getAtomChunksFromIDs(text.getAtomIDsFromPhenotypeID(trainChunks.get(j).chunkID))){
                partitionMap.put(c, trainPartList.get(j));
            }
        }
        
    }    
        
        
        
        
    
    private void generateNegIndices(List<OntologyTerm> terms){
        for (int i=0; i<Config.maxNegRetain; i++){
            termUseIndices.add(1);
        }
        while (termUseIndices.size() < terms.size()){
            termUseIndices.add(0);
        }
    }
    
    
    
    
    
    
    // TODO: These are pretty inefficient, the method that actually assigns the partitions should just make a hash
    // map where the keys are partition numbers and the value are lists of chunks, then this can just return the 
    // list of chunks. This doesn't matter much for the small dataset, might matter more when using a really large
    // one.

    public List<Chunk> getChunksFromPartitions(int part, List<Chunk> chunks){
        List<Chunk> partitionChunks = new ArrayList<>();
        for (Chunk c: chunks){
            if (getPartitionNumber(c) == part){
                partitionChunks.add(c);
            }
        }
        return partitionChunks;
    }
    
    public List<Chunk> getChunksFromPartitions(List<Integer> parts, List<Chunk> chunks){
        List<Chunk> partitionChunks = new ArrayList<>();
        for (Chunk c: chunks){
            if (parts.contains(getPartitionNumber(c))){
                partitionChunks.add(c);
            }
        }
        return partitionChunks;
    }
    
    public List<Chunk> getChunksInPartitionRangeInclusive(int lower, int upper, List<Chunk> chunks){
        List<Integer> parts = IntStream.rangeClosed(lower, upper).boxed().collect(Collectors.toList());
        return getChunksFromPartitions(parts, chunks);
    }
    
    
    
    
    
    
    // Get the chunk IDs that are present in a specific range of partitions.
    public List<Integer> getChunkIDsFromPartitionRangeInclusive(int lower, int upper, List<Chunk> chunks){
        List<Integer> parts = IntStream.rangeClosed(lower, upper).boxed().collect(Collectors.toList());
        List<Integer> partitionChunkIDs = new ArrayList<>();
        for (Chunk c: chunks){
            if (parts.contains(getPartitionNumber(c))){
                partitionChunkIDs.add(c.chunkID);
            }
        }
        return partitionChunkIDs;
    }
    
    
    
    
    */
    
    
    
    
    
    
    
    
    
    
    

}
