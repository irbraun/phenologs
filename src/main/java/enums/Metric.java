package enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

public enum Metric {
    WU_PALMER,
    LIN,
    PATH,
    LESK,
    RESNIK,
    LEACOCK_CHODOROW,
    HIRST_ST_ONGE,
    JIANG_CONRATH,
    COSINE_DIST,
    LEVENSCHTEIN_DIST,
    JACCARD_SIM,
    LCS_DIST,
    JAROWINKLER_DIST;
    
    
   
    public static boolean isDistanceMetric(Metric metric) throws Exception{
        return EnumSet.of(COSINE_DIST, LEVENSCHTEIN_DIST, LCS_DIST).contains(metric);
    } 
        
    public static boolean isSemanticMetric(Metric metric){
        return EnumSet.of(WU_PALMER, LIN, PATH, LESK, RESNIK, LEACOCK_CHODOROW, HIRST_ST_ONGE, JIANG_CONRATH).contains(metric);
    }
        
    public static List<String> names() {
        return Arrays.asList(Stream.of(Metric.values()).map(Metric::name).toArray(String[]::new));
    }
    
    
}
