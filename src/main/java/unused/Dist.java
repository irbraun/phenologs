/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package unused;

import java.util.List;
import static main.Main.logger;

/**
 *
 * @author irbraun
 */
public class Dist {

    
    public List<Double> splits;
    public List<Double> probs;
    
    public Dist(List<Double> splits, List<Double> probs){
        this.splits = splits;
        this.probs = probs;
    }
    
    public double getProb(double value) throws Exception{
        // [ ) inclusive lower edge, excludes upper edges for first n-1 discrete windows.
        try {
            for (int i=0; i<splits.size()-2; i++){
                if (value >= splits.get(i) && value < splits.get(i+1)){
                    return probs.get(i);
                }
            }
            // [ ] includes the upper bound for last discrete window.
            if (value >= splits.get(splits.size()-2) && value <= splits.get(splits.size()-1)){
                return probs.get(splits.size()-2);
            } 
            throw new Exception();
        }
        catch(Exception e){
            logger.info("problem getting prob for " + value);
            return 0.00;
        }
    }
}

