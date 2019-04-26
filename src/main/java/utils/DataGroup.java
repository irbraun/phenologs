
package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import main.Partitions;



public class DataGroup {
    public String name;
    public String fold;
    public String outputPath;
    public File evalFile;
    public PrintWriter evalPrinter;
    public File classProbsFile;
    public PrintWriter classProbsPrinter;
    public List<Integer> partitionNumbers;
    public Partitions p;
    
    
    // The output path should not include the trailing slash, the name can be anything.
    // TODO just use libraries that join paths instead of assuming their formatted correctly.
    public DataGroup(String name, String fold, List<Integer> parts, String outputPath) throws FileNotFoundException{
        this.name = name;
        this.fold = fold;
        this.partitionNumbers = parts;
        this.outputPath = outputPath;
        this.classProbsFile = new File(String.format("%s/%s_classprobs.csv",this.outputPath,this.name));
        this.classProbsPrinter = new PrintWriter(this.classProbsFile);
        this.evalFile = new File(String.format("%s/%s_eval.csv",this.outputPath,this.name));
        this.evalPrinter = new PrintWriter(this.evalFile);
    }
    
    
    // Version for when the different groups might actually be using different partition objects rather than different partition numbers.
    public DataGroup(String name, String fold, List<Integer> parts, String outputPath, Partitions p) throws FileNotFoundException{
        this.name = name;
        this.fold = fold;
        this.partitionNumbers = parts;
        this.outputPath = outputPath;
        this.classProbsFile = new File(String.format("%s/%s_classprobs.csv",this.outputPath,this.name));
        this.classProbsPrinter = new PrintWriter(this.classProbsFile);
        this.evalFile = new File(String.format("%s/%s_eval.csv",this.outputPath,this.name));
        this.evalPrinter = new PrintWriter(this.evalFile);
        this.p = p;
    }
    
    
    
    
}
