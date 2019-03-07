/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package unused;

import enums.Aspect;
import enums.Context;
import enums.Metric;
import index.FeatureIndices;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author irbraun
 */
public class GroupVariableImportance {
    
    
    
    
 
    
    public static void getImps() throws IOException{
        
        FeatureIndices f = new FeatureIndices();

        String patoPath = "/Users/irbraun/Desktop/droplet/forests/pato_atom_xm_wr/";
        String poPath = "/Users/irbraun/Desktop/droplet/forests/po_atom_xm_wr/";
        
        ArrayList<String> patoFiles = new ArrayList<>();
        patoFiles.add(String.format("%svariables_A.csv",patoPath));
        patoFiles.add(String.format("%svariables_B.csv",patoPath));
        patoFiles.add(String.format("%svariables_C.csv",patoPath));
        patoFiles.add(String.format("%svariables_D.csv",patoPath));
        
        ArrayList<String> poFiles = new ArrayList<>();
        poFiles.add(String.format("%svariables_A.csv",poPath));
        poFiles.add(String.format("%svariables_B.csv",poPath));
        poFiles.add(String.format("%svariables_C.csv",poPath));
        poFiles.add(String.format("%svariables_D.csv",poPath));
        
        HashMap<Aspect,Double> aspectVarImpMap = new HashMap<>();
        HashMap<Metric,Double> metricVarImpMap = new HashMap<>();
        HashMap<Context,Double> contextVarImpMap = new HashMap<>();
        HashMap<Aspect,Integer> aspectVarNumMap = new HashMap<>();
        HashMap<Metric,Integer> metricVarNumMap = new HashMap<>();
        HashMap<Context,Integer> contextVarNumMap = new HashMap<>();
        

        
        int numFeatures = 0;
        for (String file: patoFiles){
            Reader in = new FileReader(file);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            
            for (CSVRecord record: records){
                numFeatures++;
                // Hardcoding for very specific formatting of the variable importance file, change this.
                String feature = record.get("Variable");
                double value = Double.valueOf(record.get("Positive"));
                Metric metric = FeatureIndices.mapAbbrevToMetric(feature.split("_")[0]);
                Aspect aspect = FeatureIndices.mapAbbrevToAspect(feature.split("_")[1]);
                Context context = FeatureIndices.mapAbbrevToContext(feature.split("_")[4]);
                // Add the values to a map.
                metricVarImpMap.put(metric, metricVarImpMap.getOrDefault(metric, 0.0)+value);
                aspectVarImpMap.put(aspect, aspectVarImpMap.getOrDefault(aspect, 0.0)+value);      
                contextVarImpMap.put(context, contextVarImpMap.getOrDefault(context, 0.0)+value);
                metricVarNumMap.put(metric, metricVarNumMap.getOrDefault(metric, 0)+1);
                aspectVarNumMap.put(aspect, aspectVarNumMap.getOrDefault(aspect, 0)+1);      
                contextVarNumMap.put(context, contextVarNumMap.getOrDefault(context, 0)+1);
            }
        }
        StringBuilder sbHeaders = new StringBuilder();
        StringBuilder sbValues = new StringBuilder();
        StringBuilder sbNumbers = new StringBuilder();
        
        int numVariables;

        
        double semanticAvgImp = 0.00;
        numVariables = 0;
        for (Metric metric: Metric.values()){
            if (Metric.isSemanticMetric(metric)){
                semanticAvgImp += metricVarImpMap.getOrDefault(metric, 0.0);
                numVariables += metricVarNumMap.getOrDefault(metric, 0);
                
            }
        }
        sbHeaders.append("Semantic,");
        sbValues.append(String.format("%.3f,",semanticAvgImp / (double) numVariables));
        sbNumbers.append(String.format("%d,",numVariables));

        double syntacticAvgImp = 0.00;
        numVariables = 0;
        for (Metric metric: Metric.values()){
            if (!Metric.isSemanticMetric(metric)){
                syntacticAvgImp += metricVarImpMap.getOrDefault(metric, 0.0);
                numVariables += metricVarNumMap.getOrDefault(metric, 0);
                
            }
        }
        sbHeaders.append("Syntactic,");
        sbValues.append(String.format("%.3f,",syntacticAvgImp / (double) numVariables));
        sbNumbers.append(String.format("%d,",numVariables));
        
        
        
        double labelAvgImp = 0.00;
        numVariables = 0;
        for (Aspect aspect: Aspect.values()){
            if (aspect.equals(Aspect.LABEL)){
                labelAvgImp += aspectVarImpMap.getOrDefault(aspect, 0.0);
                numVariables += aspectVarNumMap.getOrDefault(aspect, 0);
                
            }
        }
        sbHeaders.append("Label,");
        sbValues.append(String.format("%.3f,",labelAvgImp / (double) numVariables));
        sbNumbers.append(String.format("%d,",numVariables));
        
        double synonymAvgImp = 0.00;
        numVariables = 0;
        for (Aspect aspect: Aspect.values()){
            if (aspect.equals(Aspect.BROAD_SYN) || aspect.equals(Aspect.EXACT_SYN) || aspect.equals(Aspect.NARROW_SYN) || aspect.equals(Aspect.RELATED_SYN)){
                synonymAvgImp += aspectVarImpMap.getOrDefault(aspect, 0.0);
                numVariables += aspectVarNumMap.getOrDefault(aspect, 0);
                
            }
        }
        sbHeaders.append("Synonyms,");
        sbValues.append(String.format("%.3f,",synonymAvgImp / (double) numVariables));
        sbNumbers.append(String.format("%d,",numVariables));
        
        double descAvgImp = 0.00;
        numVariables = 0;
        for (Aspect aspect: Aspect.values()){
            if (aspect.equals(Aspect.DESCRIPTION)){
                descAvgImp += aspectVarImpMap.getOrDefault(aspect, 0.0);
                numVariables += aspectVarNumMap.getOrDefault(aspect, 0);
                
            }
        }
        sbHeaders.append("Description,");
        sbValues.append(String.format("%.3f,",descAvgImp / (double) numVariables));
        sbNumbers.append(String.format("%d,",numVariables));
        
       
        
        
        double noneAvgImp = 0.00;
        numVariables = 0;
        for (Context context: Context.values()){
            if (context.equals(Context.NONE)){
                noneAvgImp += contextVarImpMap.getOrDefault(context, 0.0);
                numVariables += contextVarNumMap.getOrDefault(context, 0);
                
            }
        }
        sbHeaders.append("None,");
        sbValues.append(String.format("%.3f,",noneAvgImp / (double) numVariables));
        sbNumbers.append(String.format("%d,",numVariables));
        
        double rootAvgImp = 0.00;
        numVariables = 0;
        for (Context context: Context.values()){
            if (context.equals(Context.ROOTPATH)){
                rootAvgImp += contextVarImpMap.getOrDefault(context, 0.0);
                numVariables += contextVarNumMap.getOrDefault(context, 0);
                
            }
        }
        sbHeaders.append("Root,");
        sbValues.append(String.format("%.3f,",rootAvgImp / (double) numVariables));
        sbNumbers.append(String.format("%d,",numVariables));
        
        double sibingAvgImp = 0.00;
        numVariables = 0;
        for (Context context: Context.values()){
            if (context.equals(Context.SIBLING)){
                sibingAvgImp += contextVarImpMap.getOrDefault(context, 0.0);
                numVariables += contextVarNumMap.getOrDefault(context, 0);
                
            }
        }
        sbHeaders.append("Siblings");
        sbValues.append(String.format("%.3f",sibingAvgImp / (double) numVariables));
        sbNumbers.append(String.format("%d",numVariables));
        
        System.out.println(numFeatures + " features across four random forests.");
        System.out.println(sbHeaders.toString());
        System.out.println(sbValues.toString());
        System.out.println(sbNumbers.toString());
        
        
    }
    
    
    
    
    
}
