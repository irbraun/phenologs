
package nlp_annot;

import composer.EQStatement;
import composer.Modifier;
import config.Config;
import enums.Role;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import nlp.MyAnnotation;
import pred.OwlClass;
import pred.OwlSet;
import objects.Chunk;
import text.Text;
import static utils.Utils.toRoundedString;


public class DependencyParsing {
    
   
    
    private final HashMap<String,MinPathCounter> tagToDistObjectMap;
    private final HashMap<String,MinPathCounter> tagToMergedDistObjectMap;
    
    
    /**
     * Finds the frequencies of particular path lengths between terms in the curated
     * data. This only currently works using the phene descriptions because need to 
     * know which atomized statement is referring to which EQ statement and estimation
     * comes from these associations. The evaluation files that are specified in the 
     * config file should be from the semantic annotation of the phene descriptions.
     * @param print
     * @throws SQLException
     * @throws FileNotFoundException 
     */
    public DependencyParsing(boolean print) throws SQLException, FileNotFoundException, Exception{
        

        // Establish a list of files to be used as training data for word to term associations.
        List<OwlSet> patoSets = new ArrayList<>();
        for (String filepath: Config.qFilesForDependencyParsing){
            patoSets.add(new OwlSet(filepath));
        }

        // Establish a list of files to be used as training data for word to term associations.
        List<OwlSet> otherSets = new ArrayList<>();
        for (String filepath: Config.otherFilesForDependencyParsing){
            otherSets.add(new OwlSet(filepath));
        }
        

        // Build text data object.
        Text text = new Text();
              

        
        // Pairs that define a path between the primary entity and the quality.
        ArrayList<RolePair> pairsEtoQ = new ArrayList<>();
        pairsEtoQ.add(new RolePair(Role.PRIMARY_ENTITY1_ID, Role.QUALITY_ID));
        pairsEtoQ.add(new RolePair(Role.PRIMARY_ENTITY2_ID, Role.QUALITY_ID));
        
        // Pairs that define a path between to terms in a post composed entity.
        ArrayList<RolePair> pairsPostComposed = new ArrayList<>();
        pairsPostComposed.add(new RolePair(Role.PRIMARY_ENTITY1_ID, Role.PRIMARY_ENTITY2_ID));
        pairsPostComposed.add(new RolePair(Role.SECONDARY_ENTITY1_ID, Role.SECONDARY_ENTITY2_ID));
        
        // Pairs that define a path between entities related through a quality.
        ArrayList<RolePair> pairsRelational = new ArrayList<>();
        pairsRelational.add(new RolePair(Role.PRIMARY_ENTITY1_ID, Role.SECONDARY_ENTITY1_ID));
        pairsRelational.add(new RolePair(Role.PRIMARY_ENTITY1_ID, Role.SECONDARY_ENTITY2_ID));
        pairsRelational.add(new RolePair(Role.PRIMARY_ENTITY2_ID, Role.SECONDARY_ENTITY1_ID));
        pairsRelational.add(new RolePair(Role.PRIMARY_ENTITY2_ID, Role.SECONDARY_ENTITY2_ID));
  
        // Define which pairs of roles will be used to look for path lengths.
        ArrayList<RolePair> pairs = new ArrayList<>();
        pairs.addAll(pairsEtoQ);
        pairs.addAll(pairsPostComposed);
        pairs.addAll(pairsRelational);
        
        // Create mapping between each tag and its own distribution.
        tagToDistObjectMap = new HashMap<>();
        for (RolePair rp: pairs){
            tagToDistObjectMap.put(rp.tag, rp.distCounter);
        }
        
        

        
        
        
        
        
        // Iterate through each text chunk in the dataset.
        for (Chunk c: text.getAllAtomChunks()){
            EQStatement eq = text.getCuratedEQStatementFromAtomID(c.chunkID);
            
            // Create empty lists of nodes values for each term role.
            HashMap<Role,HashSet<String>> tokenMap = new HashMap<>();
            for (Role role: Role.values()){
                tokenMap.put(role, new HashSet<>());
            }
            
            // Find all nodes related to qualities.
            for (OwlSet p: patoSets){
                for (OwlClass oc: p.classes.getOrDefault(c.chunkID, new ArrayList<>())){
                    if (oc.termID.equals(eq.quality.id)){
                        tokenMap.put(Role.QUALITY_ID, oc.nodes);
                    }
                    if (eq.qualifier!=null && oc.termID.equals(eq.qualifier.id)){
                        tokenMap.put(Role.QUALIFIER_ID, oc.nodes);
                    }
                }
            }
            
            // Find all nodes related to entities.
            for (OwlSet p: otherSets){
                for (OwlClass oc: p.classes.getOrDefault(c.chunkID, new ArrayList<>())){
                    if (oc.termID.equals(eq.primaryEntity1.id)){
                        tokenMap.put(Role.PRIMARY_ENTITY1_ID, oc.nodes);
                    }
                    if (eq.primaryEntity2!=null && oc.termID.equals(eq.primaryEntity2.id)){
                        tokenMap.put(Role.PRIMARY_ENTITY2_ID, oc.nodes);
                    }
                    if (eq.secondaryEntity1!=null && oc.termID.equals(eq.secondaryEntity1.id)){
                        tokenMap.put(Role.SECONDARY_ENTITY1_ID, oc.nodes);
                    }
                    if (eq.secondaryEntity2!=null && oc.termID.equals(eq.secondaryEntity2.id)){
                        tokenMap.put(Role.SECONDARY_ENTITY2_ID, oc.nodes);
                    }
                }
            }
            
            // Get the dependency graph using Stanford CoreNLP libraries.
            MyAnnotation a = Modifier.getAnnotation(c);
            
            
            // Update the the distributions of minimal path lengths.
            for (RolePair rp: pairs){
                int length = Modifier.getMinPathLength(tokenMap.get(rp.r1), tokenMap.get(rp.r2), a);
                rp.distCounter.addLengthValue(length);
            }
        }    
        
        
        
        // Create mappings between multiple tags from a group and their merged distributions.
        tagToMergedDistObjectMap = new HashMap<>();
        MinPathCounter mergedEtoQ = mergeDistributions(pairsEtoQ);
        for (RolePair rp: pairsEtoQ){
            tagToMergedDistObjectMap.put(rp.tag,mergedEtoQ);
        }
        MinPathCounter mergedPostComposed = mergeDistributions(pairsPostComposed);
        for (RolePair rp: pairsPostComposed){
            tagToMergedDistObjectMap.put(rp.tag, mergedPostComposed);
        }
        MinPathCounter mergedRelational = mergeDistributions(pairsRelational);
        for (RolePair rp: pairsRelational){
            tagToMergedDistObjectMap.put(rp.tag, mergedRelational);
        }
        
       
        if (print){
            
            // The individual distributions for each path type.
            for (RolePair rp: pairs){
                System.out.println(String.format("\nDistribution of paths from %s to %s (n=%s)", rp.r1, rp.r2, rp.distCounter.getN()));
                for (int l: rp.distCounter.getAllValuesInPathDistribution()){
                    System.out.println(String.format("Length=%s, Probability=%s", l, toRoundedString(rp.distCounter.getProbability(l),3)));
                }
            }
            
            // The merged distributions that included multiple path types.
            System.out.println(String.format("\nDistribution of paths in merged E to Q (n=%s)", mergedEtoQ.getN()));
            for (int l: mergedEtoQ.getAllValuesInPathDistribution()){
                System.out.println(String.format("Length=%s, Probability=%s", l, toRoundedString(mergedEtoQ.getProbability(l),3)));
            }
            System.out.println(String.format("\nDistribution of paths in merged post composed entities (n=%s)", mergedPostComposed.getN()));
            for (int l: mergedPostComposed.getAllValuesInPathDistribution()){
                System.out.println(String.format("Length=%s, Probability=%s", l, toRoundedString(mergedPostComposed.getProbability(l),3)));
            }
            System.out.println(String.format("\nDistribution of paths in merged relational qualities (n=%s)", mergedRelational.getN()));
            for (int l: mergedRelational.getAllValuesInPathDistribution()){
                System.out.println(String.format("Length=%s, Probability=%s", l, toRoundedString(mergedRelational.getProbability(l),3)));
            }

        }
        

    }
    

   

    
    
    
    
   
    public class RolePair{
        public Role r1;
        public Role r2;
        public String tag;
        public MinPathCounter distCounter;
        public RolePair(Role r1, Role r2){
            this.r1 = r1;
            this.r2 = r2;
            this.tag = getTag(r1,r2);
            this.distCounter = new MinPathCounter();
        } 
    }
    
    
    public double getProbability(Role r1, Role r2, int length){
        return tagToDistObjectMap.get(getTag(r1,r2)).getProbability(length);
        
    }
    
    private String getTag(Role r1, Role r2){
        return String.format("%s:%s", r1.toString(), r2.toString());
    }    
    
    
    
    private MinPathCounter mergeDistributions(ArrayList<RolePair> pairObjects){
        MinPathCounter newDistCounter = new MinPathCounter();
        for (RolePair rp: pairObjects){
            for (int length: rp.distCounter.getAllValuesInPathDistribution()){
                for (int i=0; i<rp.distCounter.getCount(length); i++){
                    newDistCounter.addLengthValue(length);
                }
            }
        }
        return newDistCounter;
    }
    
    
    
    
    
    
    
    
    
    
}
