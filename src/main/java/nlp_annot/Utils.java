/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package nlp_annot;

import java.util.List;
import main.Group;
import static main.Main.logger;
import structure.Chunk;

/**
 *
 * @author irbraun
 */
public class Utils {
    
    
    
    public static void writeToEvalFiles(Object[] line, int part, List<Group> groups){
        for (Group g: groups){
            if (g.partitionNumbers.contains(part)){
                g.evalPrinter.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",line));
            }
        }
    }
    public static void writeToClassProbFiles(Object[] line, int part, List<Group> groups){
        for (Group g: groups){
            if (g.partitionNumbers.contains(part)){
                g.classProbsPrinter.println(String.format("%s,%s,%s,%s",line));
            }
        }
    }

    
    

    public static void writeToEvalFiles(Object[] line, Chunk c, List<Group> groups) throws Exception{
        for (Group g: groups){
            if (g.partitionNumbers.contains(g.p.getPartitionNumber(c))){
                g.evalPrinter.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",line));
            }
        }
    }
    public static void writeToClassProbFiles(Object[] line, Chunk c, List<Group> groups) throws Exception{
        for (Group g: groups){
            if (g.partitionNumbers.contains(g.p.getPartitionNumber(c))){
                g.classProbsPrinter.println(String.format("%s,%s,%s,%s",line));
            }
        }
    }
    
    
    
    
    public static void updateLog(int ctr, int step){
        if (ctr%step==0){
            logger.info(String.format("%s chunks processed",ctr));
        }
    }
    
    
}
