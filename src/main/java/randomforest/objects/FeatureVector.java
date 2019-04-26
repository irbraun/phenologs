package randomforest.objects;

import randomforest.objects.Feature;
import java.util.ArrayList;
import java.util.List;


public class FeatureVector {
    
    public int chunkID;
    public String termID;
    public List<Feature> features;
    
    public FeatureVector(int chunkID, String termID){
        features = new ArrayList<>();
        this.chunkID =  chunkID;
        this.termID = termID;
    }
    
    public void add(Feature feature){
        features.add(feature);
    }
    
}
