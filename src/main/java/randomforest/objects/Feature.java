package randomforest.objects;

import enums.Aspect;
import enums.Context;
import enums.Metric;
import enums.Side;

public class Feature {
        
    public String name;
    public double value;
    
    // Creates a feature with identifying characteristics and a numerical value.
    public Feature(Metric metric, Aspect aspect, Side edgeSide, Side weightSide, Context context, double value){
        this.name = Feature.createUniqueName(metric, aspect, edgeSide, weightSide, context);
        this.value = value;
    }
        
    // Creates a feature with identifying characeteristics and a numerical value.
    public Feature(Metric metric, Aspect aspect, Context context, double value){
        this.name = Feature.createUniqueName(metric, aspect, context);
        this.value = value; 
    }
    
    // Creates a pre-named feature and a numerical value.
    public Feature(String name, double value){
        this.name = name;
        this.value = value;
    }
    
    public Feature(){
    }
    
    
    /*
    Generates unique names for this particular feature given the type of information that was passed to the constructor
    when the feature was created. Not the responsibility of this class to make sure the name is unique, this just creates
    the name. The loop that generates the different types of features has to make sure that each feature that will have
    a specific name is only created once.
    */
    
    public static String createUniqueName(Metric metric, Aspect aspect, Context context){
        Object[] data = {metric.toString(), aspect.toString(), context.toString()};
        String featureName = String.format("%s%s%s",data);
        return featureName;
    }
    public static String createUniqueName(String metric, String aspect, String context){
        Object[] data = {metric, aspect, context};
        String featureName = String.format("%s%s%s",data);
        return featureName;
    }
    
    public static String createUniqueName(String metric, String aspect, String edgeSide, String weightSide, String context){
        Object[] data = {metric, aspect, edgeSide, weightSide, context};
        String featureName = String.format("%s%s%s%s%s", data);
        return featureName;
    }
    public static String createUniqueName(Metric metric, Aspect aspect, Side edgeSide, Side weightSide, Context context){
        Object[] data = {metric.toString(), aspect.toString(), edgeSide.toString(), weightSide.toString(), context.toString()};
        String featureName = String.format("%s%s%s%s%S", data);
        return featureName;
    }
    
    
}
