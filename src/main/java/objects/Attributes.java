
package objects;

public class Attributes {
    
    public int chunkID;
    public String termID;
    public boolean match;
    
    public double hPrecision;
    public double hRecall;
    public double hF1;
    public double hJac;
    
    public String hpMaxer;
    public String hrMaxer;
    public String hfMaxer;
    public String hjMaxer;
   
    public String role;
    public int partition;
    
    public Attributes(int chunkID, String termID){
        this.chunkID = chunkID;
        this.termID = termID;
        this.match = false;
    }

    
}
