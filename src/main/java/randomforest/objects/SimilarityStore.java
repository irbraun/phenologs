
package randomforest.objects;

import config.Config;
import static main.Main.logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.HashMap;


public class SimilarityStore {
    
    private final HashMap<String,Double> saved;
    String path;
    
    public SimilarityStore(String postfix) throws SQLException, FileNotFoundException, IOException, ClassNotFoundException{
        
        this.path = Config.savedPath+"."+postfix;
        
        File saveFile = new File(path);
        if (saveFile.exists()){
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            saved = (HashMap) in.readObject();
            in.close();
            fileIn.close();
        }
        else{
            saved = new HashMap<>();
        }
        Object[] data = {postfix, saved.keySet().size()};
        logger.info(String.format("saved file for %s opened with %d entries", data));
    }
    
    
    public void add(String from, String to, double score) throws IOException{
        
        Object[] data = {from, to};
        String entry = String.format("%s:%s", data);
        saved.put(entry, score);
    }
    
    
    public double get(String from, String to){
        Object[] data = {from, to};
        String entry = String.format("%s:%s", data);
        return saved.get(entry);
    }
    
    
    public void save() throws FileNotFoundException, IOException {
        FileOutputStream fileOut = new FileOutputStream(path);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(saved);
        out.close();
        fileOut.close();        
    }
    
    
}
