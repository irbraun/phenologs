
package nlp_annot;

import java.util.List;
import utils.DataGroup;
import static main.Main.logger;
import objects.Chunk;


public class Utils {
    
    
    
    public static void writeToEvalFiles(Object[] line, int part, List<DataGroup> groups){
        for (DataGroup g: groups){
            if (g.partitionNumbers.contains(part)){
                g.evalPrinter.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",line));
            }
        }
    }
    public static void writeToClassProbFiles(Object[] line, int part, List<DataGroup> groups){
        for (DataGroup g: groups){
            if (g.partitionNumbers.contains(part)){
                g.classProbsPrinter.println(String.format("%s,%s,%s,%s",line));
            }
        }
    }

    
    

    public static void writeToEvalFiles(Object[] line, Chunk c, List<DataGroup> groups) throws Exception{
        for (DataGroup g: groups){
            if (g.partitionNumbers.contains(g.p.getPartitionNumber(c))){
                g.evalPrinter.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",line));
            }
        }
    }
    public static void writeToClassProbFiles(Object[] line, Chunk c, List<DataGroup> groups) throws Exception{
        for (DataGroup g: groups){
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
