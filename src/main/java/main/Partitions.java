
package main;

import config.Config;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import objects.Chunk;
import text.Text;


public class Partitions {
    
    
    private final HashMap<Chunk,Integer> phenotypePartitionMap;
    private final HashMap<Chunk,Integer> phenePartitionMap;
    private final Text text;
    
    
    /**
     * Note that split phenotypes are never accounted for when generating partitions.
     * Partition numbers for those are done by sending either them or their associated
     * phenotype here and then the partition number of the phenotype is always checked.
     * @param text
     * @throws SQLException
     * @throws Exception 
     */
    public Partitions(Text text) throws SQLException, Exception{
        this.text = text;
        phenePartitionMap = new HashMap<>();
        phenotypePartitionMap = new HashMap<>();
        generatePartitions();
    }
    
    
    
    // Check which partition a text chunk belongs to.
    public int getPartitionNumber(Chunk c) throws Exception{
        switch(c.textType){
        case PHENOTYPE:
            //System.out.println("looking for phenotype with id " + c.chunkID);
            return phenotypePartitionMap.get(c);
        case PHENE:
            return phenePartitionMap.get(c);
        case SPLIT_PHENOTYPE:
            return phenotypePartitionMap.get(text.getPhenotypeChunkFromID(text.getPhenotypeIDfromSplitPhenotypeID(c.chunkID)));
        default:
            throw new Exception();
        }
    }
       
    
    
    private void generatePartitions() throws SQLException, Exception{
       
        List<Chunk> pChunks = text.getAllPhenotypeChunks();
        Collections.shuffle(pChunks, new Random(Config.seedValue));
        
        // Create a randomized list of partition numbers of the same length as the list of phenotype chunksk.
        ArrayList<Integer> partList = new ArrayList<>();
        int partSize = (int) pChunks.size() / Config.numPartitions;
        for (int part=0; part<Config.numPartitions; part++){
            for (int i=0; i<partSize; i++){
                partList.add(part);
            }
        }
        for (int part=0; part<Config.numPartitions; part++){
            partList.add(part);
            if (partList.size() == pChunks.size()){
                break;
            }
        }
       
        // Associate atomized statement text chunks to specific partitions.
        for (int j=0; j<pChunks.size(); j++){
            for (Chunk c: text.getAtomChunksFromIDs(text.getAtomIDsFromPhenotypeID(pChunks.get(j).chunkID))){
                phenePartitionMap.put(c, partList.get(j));
            }
        }
        
        // Assign the phenotype chunks to partitions as well.
        for (Chunk atomChunk: text.getAllAtomChunks()){
            Chunk phenotypeChunk = text.getPhenotypeChunkFromID(text.getPhenotypeIDfromAtomID(atomChunk.chunkID));
            phenotypePartitionMap.put(phenotypeChunk, getPartitionNumber(atomChunk));
        }
        
        
        
        
        
   
                
                
        // ----------------------- code for generating more specific testing sets, not currently used --------------------------------- //  
                
        /*
        // partitions 0 to 4 will have other species in them.
        if (split.equals("species")){
            generateSpeciesPartitions();
        }
        // random splist but with testing portion size consistent with above.
        else if (split.equals("random")){
            generateConsistentPartitions();
        }
        else{
            throw new Exception();
        }
        */
                  
        /*
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
        */
    }
    
    
    
    
    
    
    
    // Right now these are not being split up such that phenotypes remain within  a continuous partition.
    // That shouldn't be a problem because phenotypes in this case are always going to be within the same
    // training or testing group. Make sure this is right.
    /*
    private void generateSpeciesPartitions() throws SQLException, Exception{
        
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
            phenePartitionMap.put(araChunks.get(i), partList.get(i));
        }
        
        // Do the rest of the species, using partitions 0 to 4.
        int part = 0;
        for (Species species: Species.values()){
            if (!species.equals(Species.ARABIDOPSIS_THALIANA)){
                for (Chunk c: text.getChunksOfKindAndSpecies(TextDatatype.PHENE, species)){
                    phenePartitionMap.put(c,part);
                }   
                part++;
            }
        }
        
        // does calling getPartitionNumber work okay here? it should..
        // can change this to iterate through the phenotype chunks instead, but this should work so figure out why it doesnt.
        // Assign the phenotype chunks parts as well. (NEW)
        for (Chunk atomChunk: text.getAllAtomChunks()){
            Chunk phenotypeChunk = text.getPhenotypeChunkFromID(text.getPhenotypeIDfromAtomID(atomChunk.chunkID));
            phenotypePartitionMap.put(phenotypeChunk, getPartitionNumber(atomChunk));
        }
    }
    */

    /*
    private void generateConsistentPartitions() throws SQLException, Exception{
        
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
                phenePartitionMap.put(c, testPartList.get(j));
            }
        }
        // Associate atomized statement text chunks to specific training partitions.
        for (int j=0; j<trainChunks.size(); j++){
            for (Chunk c: text.getAtomChunksFromIDs(text.getAtomIDsFromPhenotypeID(trainChunks.get(j).chunkID))){
                phenePartitionMap.put(c, trainPartList.get(j));
            }
        }
        
        
        
        // Assign the phenotype chunks parts  as well. (NEW)
        for (Chunk atomChunk: text.getAllAtomChunks()){
            Chunk phenotypeChunk = text.getPhenotypeChunkFromID(text.getPhenotypeIDfromAtomID(atomChunk.chunkID));
            phenotypePartitionMap.put(phenotypeChunk, getPartitionNumber(atomChunk));
        }
    }    
    */
        
       
    /* 
    Notes: These work even if datatype is unknown because the list of text chunks are provided as an argument.
    So if you pass in only phenes you're guaranteed to only get phenes back.
    TODO pretty inefficient, the method that actually assigns the partitions should just make a hash
    map where the keys are partition numbers and the value are lists of chunks, then this can just return 
    the  list of chunks. This doesn't matter much for the small dataset, might matter more when using a 
    really large one.
    */


    public List<Chunk> getChunksFromPartitions(int part, List<Chunk> chunks) throws Exception{
        List<Chunk> partitionChunks = new ArrayList<>();
        for (Chunk c: chunks){
            if (getPartitionNumber(c) == part){
                partitionChunks.add(c);
            }
        }
        return partitionChunks;
    }
    
    public List<Chunk> getChunksFromPartitions(List<Integer> parts, List<Chunk> chunks) throws Exception{
        List<Chunk> partitionChunks = new ArrayList<>();
        for (Chunk c: chunks){
            if (parts.contains(getPartitionNumber(c))){
                partitionChunks.add(c);
            }
        }
        return partitionChunks;
    }
    
    public List<Chunk> getChunksInPartitionRangeInclusive(int lower, int upper, List<Chunk> chunks) throws Exception{
        List<Integer> parts = IntStream.rangeClosed(lower, upper).boxed().collect(Collectors.toList());
        return getChunksFromPartitions(parts, chunks);
    }
    
    public List<Integer> getChunkIDsFromPartitionRangeInclusive(int lower, int upper, List<Chunk> chunks) throws Exception{
        List<Integer> parts = IntStream.rangeClosed(lower, upper).boxed().collect(Collectors.toList());
        List<Integer> partitionChunkIDs = new ArrayList<>();
        for (Chunk c: chunks){
            if (parts.contains(getPartitionNumber(c))){
                partitionChunkIDs.add(c.chunkID);
            }
        }
        return partitionChunkIDs;
    }
    
    
    
    
    
    
    
    
    

}
