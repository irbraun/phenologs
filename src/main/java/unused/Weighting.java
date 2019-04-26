/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package unused;

import config.Config;
import enums.Metric;
import enums.Ontology;
import enums.Aspect;
import randomforest.index.FeatureIndices;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author irbraun
 */
public class Weighting {
    
    
    private final HashMap<Ontology,WeightSet> OntoToWeightMap;

    

    public Weighting(){
        OntoToWeightMap = new HashMap<>();
    }
    
    
    
    
    public void addOntology(String ontology) throws IOException{
        WeightSet weightSet = new WeightSet(utils.Utils.inferOntology(ontology));
        OntoToWeightMap.put(utils.Utils.inferOntology(ontology), weightSet); 
    }
    
    public void addOntology(Ontology ontology) throws IOException{
        WeightSet weightSet = new WeightSet(ontology);
        OntoToWeightMap.put(ontology, weightSet); 
    }
    
    
    
    
    
    
    public double getMetricWeight(String ontology, Metric metric){
        return OntoToWeightMap.get(utils.Utils.inferOntology(ontology)).metricWeights.get(metric);
    }
    public double getAspectWeight(String ontology, Aspect aspect){
        return OntoToWeightMap.get(utils.Utils.inferOntology(ontology)).aspectWeights.get(aspect);
    }
    
    
    
    
    
    
    public double getMetricWeight(Ontology ontology, Metric metric){
        return OntoToWeightMap.get(ontology).metricWeights.get(metric);
    }
    public double getAspectWeight(Ontology ontology, Aspect aspect){
        return OntoToWeightMap.get(ontology).aspectWeights.get(aspect);
    }
    
    
    
    public Set<Metric> getMetrics(String ontology){
        return OntoToWeightMap.get(utils.Utils.inferOntology(ontology)).metricWeights.keySet();
    }
    public Set<Aspect>  getAspects(String ontology){
        return OntoToWeightMap.get(utils.Utils.inferOntology(ontology)).aspectWeights.keySet();
    }
    
    
    
    
    
    
    public Set<Metric>  getMetrics(Ontology ontology){
        return OntoToWeightMap.get(ontology).metricWeights.keySet();
    }
    public Set<Aspect>  getAspects(Ontology ontology){
        return OntoToWeightMap.get(ontology).aspectWeights.keySet();
    }
    
    
    
    
    
  
    
    
    
    public class WeightSet {
        
        public HashMap<Metric,Double> metricWeights;
        public HashMap<Aspect,Double> aspectWeights;
        private final HashMap<Aspect,Double> aspectVarImpMap;
        private final HashMap<Metric,Double> metricVarImpMap;  
        
        
        public WeightSet(Ontology ontology) throws FileNotFoundException, IOException{
            metricVarImpMap = new HashMap<>();
            aspectVarImpMap = new HashMap<>();
            metricWeights = new HashMap<>();
            aspectWeights = new HashMap<>();

            findSumsOfVariableImportance(Config.varImpPaths.get(ontology));
            
            // Metrics
            double metricSum=0.000;
            for (Metric metric: metricVarImpMap.keySet()){
                metricSum += metricVarImpMap.get(metric);
            }
            for (Metric metric: metricVarImpMap.keySet()){
                metricWeights.put(metric, (double)metricVarImpMap.get(metric) / (double)metricSum);
            }
            
            // Term Aspects
            double aspectSum=0.000;
            for (Aspect aspect: aspectVarImpMap.keySet()){
                aspectSum += aspectVarImpMap.get(aspect);
            }
            for (Aspect aspect: aspectVarImpMap.keySet()){
                aspectWeights.put(aspect, (double)aspectVarImpMap.get(aspect) / (double)aspectSum);
            }
        }
        

        private void findSumsOfVariableImportance(String filename) throws FileNotFoundException, IOException{
            FeatureIndices featIdx = new FeatureIndices();
            Reader in = new FileReader(filename);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record: records){
                
                // Hardcoding for very specific formatting of the variable importance file, change this.
                String feature = record.get("Variable");
                double value = Double.valueOf(record.get("Positive"));
                Metric metric = FeatureIndices.mapAbbrevToMetric(feature.split("_")[0]);
                Aspect aspect = FeatureIndices.mapAbbrevToAspect(feature.split("_")[1]);
                
                metricVarImpMap.put(metric, metricVarImpMap.getOrDefault(metric, 0.0)+value);
                aspectVarImpMap.put(aspect, aspectVarImpMap.getOrDefault(aspect, 0.0)+value);             
            }
        }
    }
    
    
    
    
    
}
