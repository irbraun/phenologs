/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
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
import structure.Chunk;
import text.Text;

/**
 *
 * @author irbraun
 */
public class DependencyParsing {
    
    
    private final HashMap<Integer,Integer> countsPE1toQ;
    private final HashMap<Integer,Integer> countsPE1toPE2;
    private final HashMap<Integer,Integer> countsPE1toSE1;
    private int sumPE1toQ;
    private int sumPE1toPE2;
    private int sumPE1toSE1;
    
    /**
     * Finds the frequencies of particular path lengths between terms in the curated
     * data. This only currently works using the phene descriptions because need to 
     * know which atomized statement is referring to which EQ statement and estimation
     * comes from these associations. The evaluation files that are specified in the 
     * config file should be from the semantic annotation of the phene descriptions.
     * @throws SQLException
     * @throws FileNotFoundException 
     */
    public DependencyParsing() throws SQLException, FileNotFoundException{
        

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
        
        countsPE1toQ = new HashMap<>();
        countsPE1toPE2 = new HashMap<>();
        countsPE1toSE1 = new HashMap<>();
        sumPE1toQ = 0;
        sumPE1toPE2 = 0;
        sumPE1toSE1 = 0;
        
        Text text = new Text();
        
        for (Chunk c: text.getAllAtomChunks()){
            EQStatement eq = text.getCuratedEQStatementFromAtomID(c.chunkID);
            HashSet<String> qTokens = new HashSet<>();
            HashSet<String> qlfrTokens = new HashSet<>();
            for (OwlSet p: patoSets){
                for (OwlClass oc: p.classes.getOrDefault(c.chunkID, new ArrayList<>())){
                    if (oc.termID.equals(eq.quality.id)){
                        qTokens.addAll(oc.nodes);
                    }
                    if (eq.qualifier!=null && oc.termID.equals(eq.qualifier.id)){
                        qlfrTokens.addAll(oc.nodes);
                    }
                }
            }
            
            HashSet<String> pe1Tokens = new HashSet<>();
            HashSet<String> pe2Tokens = new HashSet<>();
            HashSet<String> se1Tokens = new HashSet<>();
            HashSet<String> se2Tokens = new HashSet<>();
            for (OwlSet p: otherSets){
                for (OwlClass oc: p.classes.getOrDefault(c.chunkID, new ArrayList<>())){
                    if (oc.termID.equals(eq.primaryEntity1.id)){
                        pe1Tokens.addAll(oc.nodes);
                    }
                    if (eq.primaryEntity2!=null && oc.termID.equals(eq.primaryEntity2.id)){
                        pe2Tokens.addAll(oc.nodes);
                    }
                    if (eq.secondaryEntity1!=null && oc.termID.equals(eq.secondaryEntity1.id)){
                        se1Tokens.addAll(oc.nodes);
                    }
                    if (eq.secondaryEntity2!=null && oc.termID.equals(eq.secondaryEntity2.id)){
                        se2Tokens.addAll(oc.nodes);
                    }
                }
            }
            
            MyAnnotation a = Modifier.getAnnotation(c);
            
            int length;
            
            length = Modifier.getMinPathLength(qTokens, pe1Tokens, a);
            if (length != 100){
                int count = countsPE1toQ.getOrDefault(length, 0);
                count++;
                countsPE1toQ.put(length, count);
            }
            
            length = Modifier.getMinPathLength(pe1Tokens, pe2Tokens, a);
            if (length != 100){
                int count = countsPE1toPE2.getOrDefault(length, 0);
                count++;
                countsPE1toPE2.put(length, count); 
            }
            
            length = Modifier.getMinPathLength(pe1Tokens, se1Tokens, a);
            if (length != 100){
                int count = countsPE1toSE1.getOrDefault(length, 0);
                count++;
                countsPE1toSE1.put(length, count);            
            }
           
        }    
        

        for (int l: countsPE1toQ.keySet()){
            sumPE1toQ += countsPE1toQ.get(l);
            System.out.println(l + ":" + countsPE1toQ.get(l));
        }
        for (int l: countsPE1toPE2.keySet()){
            sumPE1toPE2 += countsPE1toPE2.get(l);
            System.out.println(l + ":" + countsPE1toPE2.get(l));
        }
        for (int l: countsPE1toSE1.keySet()){
            sumPE1toSE1 += countsPE1toSE1.get(l);
            System.out.println(l + ":" + countsPE1toSE1.get(l));
        }
        
        
        
        
    }
    


    
    
    public double getProbability(Role r1, Role r2, int length) throws Exception{
        double prob;
        if (r1.equals(Role.PRIMARY_ENTITY1_ID) && r2.equals(Role.QUALITY_ID)){
            prob = (double) countsPE1toQ.getOrDefault(length, 0) / (double) sumPE1toQ;
        }
        else if (r1.equals(Role.PRIMARY_ENTITY1_ID) && r2.equals(Role.PRIMARY_ENTITY2_ID)){
            prob = (double) countsPE1toPE2.getOrDefault(length, 0) / (double) sumPE1toPE2;
        }
        else if (r1.equals(Role.PRIMARY_ENTITY1_ID) && r2.equals(Role.SECONDARY_ENTITY1_ID)){
            prob = (double) countsPE1toSE1.getOrDefault(length, 0) / (double) sumPE1toSE1;
        }
        else {
            throw new Exception();
        }
        return prob;
    }
    
    
    
    
    
    
    
    
    
    
    
}
