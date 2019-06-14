
package nlp_annot;

import java.util.ArrayList;
import java.util.HashMap;


public class MinPathCounter {
    
    private final HashMap<Integer,Integer> countsOfEachLength;
    private final int defaultMaxLength = 100;
    
    
    public MinPathCounter(){
        countsOfEachLength = new HashMap<>();
    }
    
    
    public void addLengthValue(int l){
        if (l!=defaultMaxLength && l!=0){
            int count = countsOfEachLength.getOrDefault(l, 0);
            count++;
            countsOfEachLength.put(l, count);
        }
    }
    
    
    
    public double getProbability(int l){
        int sum = utils.Utils.sum(new ArrayList<>(countsOfEachLength.values()));
        double prob = (double) countsOfEachLength.getOrDefault(l,0) / (double) sum;
        if (Double.isNaN(prob)){
            System.out.println("found NaN value for length probability");
            return 0.000;
        }
        return prob;
    }
    
    public int getCount(int l){
        return countsOfEachLength.getOrDefault(l,0);
    }
    
    public int getN(){
        return utils.Utils.sum(new ArrayList<>(countsOfEachLength.values()));
    }
    
    public ArrayList<Integer> getAllValuesInPathDistribution(){
        return new ArrayList<>(countsOfEachLength.keySet());
    }
    
    
    
    
}
