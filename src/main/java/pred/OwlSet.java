
package pred;



import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


public class OwlSet {
    
    public HashMap<Integer,List<OwlClass>> classes;
    
    public OwlSet(String filename) throws FileNotFoundException{
        
        
        classes = new HashMap<>();
        File file = new File(filename);
        Scanner scanner = new Scanner(file);
            scanner.useDelimiter(",");
            // Account for the header in the class probability file.
            scanner.nextLine();
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] lineValues = line.split(",");
                // Get the values from each data line in the evaluation file.
                String chunkIDStr = lineValues[0].replace("\"", "").trim(); // (R scripts might add quotes to csv files).
                int chunkID = Integer.valueOf(chunkIDStr);
                String termID = lineValues[3].replace("\"","").trim();
 
                
                double value;
                try{
                    value = Double.valueOf(lineValues[4].trim());
                }
                catch(NumberFormatException e){
                    value = 0d;
                }
                
                // fix this later to use the enum.
                String componentStr = lineValues[5].replace("\"", "").trim();
                String role = componentStr;
                
                
                String category = lineValues[6].replace("\"", "").trim();
 
                double similarity;
                try{
                    similarity = Double.valueOf(lineValues[7].trim());
                }
                catch(NumberFormatException e){
                    similarity = 0d;
                }
                
                
                
                String nodes = "";
                if (lineValues.length > 8){
                    nodes = lineValues[8].trim();
                }
                OwlClass oc = new OwlClass();
                oc.role = role;
                oc.category = category;
                oc.chunkID = chunkID;
                oc.termID = termID;
                oc.similarity = similarity;
                oc.nodes = utils.Utils.getNodeSetFromString(nodes);   
                List l = classes.getOrDefault(chunkID, new ArrayList<>());
                l.add(oc);
                classes.put(chunkID, l);
            }  
    }
    
    
    public OwlSet(){
        classes = new HashMap<>();
    }
    
    
    
    
    
}
