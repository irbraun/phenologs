package randomforest.index;


import config.Config;
import structure.Feature;
import enums.Aspect;
import enums.Context;
import enums.Metric;
import enums.Side;
import java.util.HashMap;
import java.util.Map;


public class FeatureIndices {
    
    private static Map<String, Integer> featureIndexMap;
    private static Map<Integer, String> featureAbbrevMap;
    private static Map<String, String> abb;
    private static HashMap<String,Metric> abbToMetric;
    private static HashMap<String,Aspect> abbToAspect;
    private static HashMap<String,Context> abbToContext;
    private static HashMap<String,Side> abbToSide;
    private static int featureNumber = 0;
    
    public FeatureIndices(){   
        
        featureIndexMap = new HashMap<>();
        featureAbbrevMap = new HashMap<>();
        abb = new HashMap<>();
        abbToMetric = new HashMap<>();
        abbToAspect = new HashMap<>();
        abbToContext = new HashMap<>();
        abbToSide = new HashMap<>();
        populateAbbreviations();
        
        
        // Context-free features.
        for (Context context: Context.getContextFreeSubtypes()){
            for (Aspect aspect: Config.normalAspects){
                for (Metric metric: Config.normalFunctions){
                    String featureKey = Feature.createUniqueName(metric.toString(), aspect.toString(), Config.numEdges.toString(), Config.weighting.toString(), context.toString());
                    String featureAbbrev = FeatureIndices.getAbbreviation(metric.toString(), aspect.toString(), Config.numEdges.toString(), Config.weighting.toString(), context.toString());
                    FeatureIndices.addFeatureToIndexing(featureKey, featureAbbrev);
                }                
            }
        }
        
        // Context-based features.
        if (!Config.contextFunctions.isEmpty()){
            for (Context context: Context.getContextSubtypes()){
                for (Aspect aspect: Config.contextAspects){
                    for (Metric metric: Config.contextFunctions){
                        String featureKey = Feature.createUniqueName(metric.toString(), aspect.toString(), Config.numEdges.toString(), Config.weighting.toString(), context.toString());
                        String featureAbbrev = FeatureIndices.getAbbreviation(metric.toString(), aspect.toString(), Config.numEdges.toString(), Config.weighting.toString(), context.toString());
                        FeatureIndices.addFeatureToIndexing(featureKey, featureAbbrev);
                    }
                }
            }
        }
        
        // Adding in any other arbitrary features here.
        //FeatureIndices.addFeatureToIndexing("dummy_key","dummy_abbrev");
        
    }
    
    
    
    
    
    public static int getIndex(Feature feature){
        String featureKey = feature.name;
        return featureIndexMap.get(featureKey);
    }
    
    
    public static int getSize(){
        return featureNumber;
    }
    
    
    public static String getAbbreviation(int index){
        return(featureAbbrevMap.get(index));
    }
    
    
    private static void addFeatureToIndexing(String featureKey, String featureAbbrev){
        featureIndexMap.put(featureKey, featureNumber);
        featureAbbrevMap.put(featureNumber, featureAbbrev);                            
        featureNumber++; 
    }
    
    
    
    private static void populateAbbreviations(){
        // Term Aspects
        abb.put("LABEL","lb");
        abb.put("EXACT_SYN","ex");
        abb.put("RELATED_SYN","rl");
        abb.put("NARROW_SYN","nr");
        abb.put("BROAD_SYN","br");
        abb.put("DESCRIPTION","ds");
        // Contexts
        abb.put("ROOTPATH","cr");
        abb.put("SIBLING","cs");
        abb.put("NO", "cn");
        // Metrics
        abb.put("WU_PALMER","wup");
        abb.put("LIN","lin");
        abb.put("PATH","path");
        abb.put("LESK","lesk");
        abb.put("RESNIK","res");
        abb.put("LEACOCK_CHODOROW","lch");
        abb.put("HIRST_ST_ONGE","hso");
        abb.put("JIANG_CONRATH","jcn");
        abb.put("COSINE_DIST","cos");
        abb.put("LEVENSCHTEIN_DIST","lev");
        abb.put("JACCARD_SIM","jac");
        abb.put("LCS_DIST","lcs");
        abb.put("JAROWINKLER_DIST","jar");
        // Sides
        abb.put("TEXT","x");
        abb.put("TERM","m");
        abb.put("NONE","n");
        
        
        // Mappings from abbreviations back to objects.
        abbToMetric.put("wup", Metric.WU_PALMER);
        abbToMetric.put("lin", Metric.LIN);
        abbToMetric.put("path", Metric.PATH);
        abbToMetric.put("lesk", Metric.LESK);
        abbToMetric.put("res", Metric.RESNIK);
        abbToMetric.put("lch", Metric.LEACOCK_CHODOROW);
        abbToMetric.put("hso", Metric.HIRST_ST_ONGE);
        abbToMetric.put("jcn", Metric.JIANG_CONRATH);
        abbToMetric.put("cos", Metric.COSINE_DIST);
        abbToMetric.put("lev", Metric.LEVENSCHTEIN_DIST);
        abbToMetric.put("jac", Metric.JACCARD_SIM);
        abbToMetric.put("lcs", Metric.LCS_DIST);
        abbToMetric.put("jar", Metric.JAROWINKLER_DIST);
        abbToAspect.put("lb", Aspect.LABEL);
        abbToAspect.put("ex", Aspect.EXACT_SYN);
        abbToAspect.put("rl", Aspect.RELATED_SYN);
        abbToAspect.put("nr", Aspect.NARROW_SYN);
        abbToAspect.put("br", Aspect.BROAD_SYN);
        abbToAspect.put("ds", Aspect.DESCRIPTION);
        abbToContext.put("cn",Context.NONE);
        abbToContext.put("cr",Context.ROOTPATH);
        abbToContext.put("cs",Context.SIBLING);
        abbToSide.put("x",Side.TEXT);
        abbToSide.put("m",Side.TERM);
        abbToSide.put("n",Side.NONE);
    }
    
    
    
    private static String getAbbreviation(String metric, String aspect, String edgeSide, String weightSide, String context){
        String metAbb = abb.get(metric);
        String conAbb = abb.get(context);
        String aspAbb = abb.get(aspect);
        String esiAbb = abb.get(edgeSide);
        String wsiAbb = abb.get(weightSide);
        Object[] data = {metAbb,aspAbb,esiAbb,wsiAbb,conAbb};
        return String.format("%s_%s_%s_%s_%s",data);
    }
    
    private static String getAbbreviation(String metric, String aspect, String context){
        String metAbb = abb.get(metric);
        String conAbb = abb.get(context);
        String aspAbb = abb.get(aspect);
        Object[] data = {metAbb,aspAbb,conAbb};
        return String.format("%s_%s_%s",data);
    }
    
    
    
    public static Metric mapAbbrevToMetric(String abbreviation){
        return abbToMetric.get(abbreviation);
    }
       
    public static Aspect mapAbbrevToAspect(String abbreviation){
        return abbToAspect.get(abbreviation);
    }
    
    public static Context mapAbbrevToContext(String abbreviation){
        return abbToContext.get(abbreviation);
    }
    
    public static Side mapAbbrevToSide(String abbreviation){
        return abbToSide.get(abbreviation);
    }
    
    
    
}
