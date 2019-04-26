/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package unused;

import enums.Role;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author irbraun
 */
public class Bayesian {
    
    // Mappings for the different distributions.
    private final HashMap<DepFeatureCategories, HashMap<List<Role>,List<Double>>> samples;
    private final HashMap<DepFeatureCategories, HashMap<List<Role>,Dist>> distributions;
    private final List<List<Role>> rolePairs;

    
    // Counters for where data points cannot be included in the distributions.
    private final HashMap<List<Role>,Integer> componentNotPresentMap = new HashMap<>();
    private final HashMap<List<Role>,Integer> noPathFoundMap = new HashMap<>();
    private final HashMap<List<Role>,Integer> targetSourceOverlapMap = new HashMap<>();
    

    
    public Bayesian(){
        
        samples = new HashMap<>();
        samples.put(DepFeatureCategories.DIRECTION, new HashMap<>());
        samples.put(DepFeatureCategories.LENGTH, new HashMap<>());
        distributions = new HashMap<>();
        rolePairs = new ArrayList<>();
        
        Role[] dep1 = {Role.PRIMARY_ENTITY1_ID, Role.PRIMARY_ENTITY2_ID};
        Role[] dep2 = {Role.SECONDARY_ENTITY1_ID, Role.SECONDARY_ENTITY2_ID};
        Role[] dep3 = {Role.PRIMARY_ENTITY1_ID, Role.QUALITY_ID};
        Role[] dep4 = {Role.PRIMARY_ENTITY1_ID, Role.QUALIFIER_ID};
        Role[] dep5 = {Role.PRIMARY_ENTITY1_ID, Role.SECONDARY_ENTITY1_ID};
        Role[] dep6 = {Role.QUALITY_ID, Role.QUALIFIER_ID};
        
        rolePairs.add(Arrays.asList(dep1));
        rolePairs.add(Arrays.asList(dep2));
        rolePairs.add(Arrays.asList(dep3));
        rolePairs.add(Arrays.asList(dep4));
        rolePairs.add(Arrays.asList(dep5));
        rolePairs.add(Arrays.asList(dep6));
        for (List<Role> rolePair: rolePairs){
            samples.get(DepFeatureCategories.DIRECTION).put(rolePair, new ArrayList<>());
            samples.get(DepFeatureCategories.LENGTH).put(rolePair, new ArrayList<>());
            componentNotPresentMap.put(rolePair,0);
            noPathFoundMap.put(rolePair, 0);
            targetSourceOverlapMap.put(rolePair,0);
        }
    }
    
   
    
    
    
    /**
     * Constructor for when the distributions/counts have already been previously found.
     * After calling this, the populate counts method should be called next.
     * Ideally this would be fixed so that the counts could be stored in a file
     * instead of just the individual lists but there is too much information being additionally
     * read from the map of lists to do that right now, fix this sometime.
     * @param distPath
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public Bayesian(String distPath, String distFile) throws IOException, ClassNotFoundException{
        distributions = new HashMap<>();
        String distFilename = String.format("%s%s",distPath, distFile);
        File dirFile = new File(distFilename);
        if (dirFile.exists()){
            FileInputStream fileIn = new FileInputStream(distFilename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            samples = (HashMap) in.readObject();
            in.close();
            
            //TODO dont hardcode this, just testing for now.
            rolePairs = new ArrayList<>();
            List<Role> rolePair = new ArrayList<>();
            Role[] dep1 = {Role.PRIMARY_ENTITY1_ID, Role.PRIMARY_ENTITY2_ID};
            Role[] dep2 = {Role.SECONDARY_ENTITY1_ID, Role.SECONDARY_ENTITY2_ID};
            Role[] dep3 = {Role.PRIMARY_ENTITY1_ID, Role.QUALITY_ID};
            Role[] dep4 = {Role.PRIMARY_ENTITY1_ID, Role.QUALIFIER_ID};
            Role[] dep5 = {Role.PRIMARY_ENTITY1_ID, Role.SECONDARY_ENTITY1_ID};
            Role[] dep6 = {Role.QUALITY_ID, Role.QUALIFIER_ID};
            rolePairs.add(Arrays.asList(dep1));
            rolePairs.add(Arrays.asList(dep2));
            rolePairs.add(Arrays.asList(dep3));
            rolePairs.add(Arrays.asList(dep4));
            rolePairs.add(Arrays.asList(dep5));
            rolePairs.add(Arrays.asList(dep6));
        }
        else {
            throw new FileNotFoundException();
        }
    }

    
    
    
    

    /**
     * Adds new examples of each features to the distributions keeping track of their counts.
     * @param featureMap Mapping between feature and their values.
     * @param rolePair The pair of roles that these features were calculated for.
     * @throws java.lang.Exception
     */
    public void addFeatures(HashMap<DepFeatureCategories,Double> featureMap, List<Role> rolePair) throws Exception{
        if (checkForError(featureMap, rolePair) == false){
            for (Map.Entry<DepFeatureCategories,Double> feature : featureMap.entrySet()) {
                samples.get(feature.getKey()).get(rolePair).add(feature.getValue());           
            }
        }
    }
    
    
    
    private boolean checkForError(HashMap<DepFeatureCategories,Double> featureMap, List<Role> rolePair) throws Exception{
        if (featureMap.containsValue(-1.0)){
            incrementErrorCounter(rolePair, 1);
            return true;
        }
        else if (featureMap.containsValue(-2.0)){
            incrementErrorCounter(rolePair, 2);
            return true;
        }
        else if (featureMap.containsValue(-3.0)){
            incrementErrorCounter(rolePair, 3);
            return true;
        }     
        return false;
    }
    
    private void incrementErrorCounter(List<Role> rolePair, int error) throws Exception{
        switch(error){
        case(1):
            componentNotPresentMap.put(rolePair, componentNotPresentMap.get(rolePair)+1);
            break;
        case(2):
            noPathFoundMap.put(rolePair, noPathFoundMap.get(rolePair)+1);
            break;
        case(3):
            targetSourceOverlapMap.put(rolePair, targetSourceOverlapMap.get(rolePair)+1);
            break;
        default:
            throw new Exception();
        }
    }
    
    
    
    
    public List<List<Role>> getDependencyRolePairs(){
        return rolePairs;
    }
    

    
    
    
    // TODO
    // dont like the way this is set up where its a nested hashmap, makes it easy to wreck when
    // adding stuff to it in the wrong order, change it so that the whole feature including 
    // which nodes are used (roles) and the type of feature (len, direction, etc) are included
    // in a single feature template object when changing this stuff in the future.
    
    
    
    public void populateCounts(int numBins){
        
        for (DepFeatureCategories feature: DepFeatureCategories.values()){
            HashMap<List<Role>,Dist> map = new HashMap<>();
            for (List<Role> rolePair: rolePairs){
                List<Double> featureSamples = this.samples.get(feature).get(rolePair);
                List<Double> splits = new ArrayList<>();
                splits.add(0.000);
                
                // Accounting for the fact that the maximum value could be anything, not necessarily 1 (for lenght).
                double maxValue = 0.00;
                for (double sample: featureSamples){
                    if (sample>=maxValue){
                        maxValue = sample;
                    }
                }
                double binWidth = (double)maxValue/(double)numBins;
                for (int i=1; i<numBins; i++){
                    splits.add((double)i*binWidth);
                }
                splits.add(maxValue);
                
                
                int[] counts = new int[numBins];
                for (double sample: featureSamples){
                    // [ )
                    for (int i=0; i<splits.size()-2; i++){
                        if (sample >= splits.get(i) && sample < splits.get(i+1)){
                            counts[i]++;
                        }
                    }
                    // [ ]
                    if (sample >= splits.get(splits.size()-2) && sample <= splits.get(splits.size()-1)){
                        counts[splits.size()-2]++;
                    }
                }
                List<Double> probs = new ArrayList<>();
                for (int count: counts){
                    double prob = (double)count / (double)featureSamples.size();
                    probs.add(prob);
                }
                map.put(rolePair, new Dist(splits, probs));
            }
            distributions.put(feature, map);
        }
 
    }
    
    
    

    
    
    
    public double getProbability(List<Role> rolePair, DepFeatureCategories feature, double value) throws Exception{
        return distributions.get(feature).get(rolePair).getProb(value);
    }
    
    
    
    
    
   
    public void summarize(String path, String loopTag) throws FileNotFoundException{
        String filename = String.format("%ssummary_%s.txt",path,loopTag);
        File outputFile = new File(filename);
        try (PrintWriter writer = new PrintWriter(outputFile)) {
            int rolePairNumber = 1;
            for (List<Role> rolePair: rolePairs){
                writer.println(rolePair.get(0).toString() + " --> " + rolePair.get(1).toString() + " (distribution" + rolePairNumber + ")");
                // Could have used any dependency feature, should be equivalent for length and direction.
                int n = samples.get(DepFeatureCategories.DIRECTION).get(rolePair).size();
                int error1 = componentNotPresentMap.get(rolePair);
                int error2 = noPathFoundMap.get(rolePair);
                int error3 = targetSourceOverlapMap.get(rolePair);
                int N = n+error1+error2+error3;
                writer.println("n=" + n);
                writer.println("N=" + N + " ("+error1+" missing component, " + error2 + " missing path, " + error3 + " target/source overlap)");
                writer.println();
                rolePairNumber++;
            }
        }
    }
    


    
    public void writeToTextFiles(String path,String loopTag) throws FileNotFoundException{
        // Write to human readable files.
        int rolePairNumber = 1;
        for (List<Role> rolePair: rolePairs){
            Object[] data = {path, rolePairNumber, loopTag};
            File outputFile = new File(String.format("%sdist%s_%s.csv", data));
            try (PrintWriter writer = new PrintWriter(outputFile)) {
                writer.println("direction,length");
                // Could have used any dependency feature to get the size, should be equivalent for length and direction.
                for (int i=0; i<samples.get(DepFeatureCategories.DIRECTION).get(rolePair).size(); i++){
                    double direction = samples.get(DepFeatureCategories.DIRECTION).get(rolePair).get(i);
                    double length = samples.get(DepFeatureCategories.LENGTH).get(rolePair).get(i);
                    Object[] values = {direction, length};
                    writer.println(String.format("%s,%s", values));
                }
            }
            rolePairNumber++;
        }
    }
    
    

    
    public void writeToSerializedFile(String path, String loopTag) throws FileNotFoundException, IOException{
        // Write to serialize object files.
        FileOutputStream distFileOut = new FileOutputStream(String.format("%sdist_%s.ser",path,loopTag));
        ObjectOutputStream distOut = new ObjectOutputStream(distFileOut);
        distOut.writeObject(samples);
        distOut.close();
        distFileOut.close();    
    }
    

   
}
