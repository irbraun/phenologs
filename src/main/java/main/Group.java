/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

/**
 *
 * @author irbraun
 */
public class Group {
    public String name;
    public String fold;
    public String outputPath;
    public File evalFile;
    public PrintWriter evalPrinter;
    public File classProbsFile;
    public PrintWriter classProbsPrinter;
    public List<Integer> partitionNumbers;
    public Partitions p;
    
    
    
    /**
     * The paths should not include the file names but include trailing slash.
     * The filenames will be taken from the name and fold fields and extensions added.
     * @param name
     * @param fold
     * @param parts
     * @param outputPath
     * @throws FileNotFoundException 
     */
    public Group(String name, String fold, List<Integer> parts, String outputPath) throws FileNotFoundException{
        this.name = name;
        this.fold = fold;
        this.partitionNumbers = parts;
        this.outputPath = outputPath;
        /**
        this.classProbsFile = new File(String.format("%s%s.%s.classprobs.csv",this.outputPath,this.name,this.fold));
        this.classProbsPrinter = new PrintWriter(this.classProbsFile);
        this.evalFile = new File(String.format("%s%s.%s.eval.csv",this.outputPath,this.name,this.fold));
        this.evalPrinter = new PrintWriter(this.evalFile);
        **/
        this.classProbsFile = new File(String.format("%s/%s_classprobs.csv",this.outputPath,this.name));
        this.classProbsPrinter = new PrintWriter(this.classProbsFile);
        this.evalFile = new File(String.format("%s/%s_eval.csv",this.outputPath,this.name));
        this.evalPrinter = new PrintWriter(this.evalFile);
    }
    
    
    
    // Version for when the different groups might actually be using different partition objects rather than different partition numbers.
    public Group(String name, String fold, List<Integer> parts, String outputPath, Partitions p) throws FileNotFoundException{
        this.name = name;
        this.fold = fold;
        this.partitionNumbers = parts;
        this.outputPath = outputPath;
        
        /*
        this.classProbsFile = new File(String.format("%s%s.%s.classprobs.csv",this.outputPath,this.name,this.fold));
        this.classProbsPrinter = new PrintWriter(this.classProbsFile);
        this.evalFile = new File(String.format("%s%s.%s.eval.csv",this.outputPath,this.name,this.fold));
        this.evalPrinter = new PrintWriter(this.evalFile);
        */
        
        
        this.classProbsFile = new File(String.format("%s/%s_classprobs.csv",this.outputPath,this.name));
        this.classProbsPrinter = new PrintWriter(this.classProbsFile);
        this.evalFile = new File(String.format("%s/%s_eval.csv",this.outputPath,this.name));
        this.evalPrinter = new PrintWriter(this.evalFile);
        this.p = p;
    }
    
    
    
    
}
