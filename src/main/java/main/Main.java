
package main;


import randomforest.process.Run;
import composer.Composer;
import config.Config;
import config.Connect;
import preprocess.BNLPOutputReader;
import preprocess.InputDataPreparer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import nlp.CoreNLP;
import nlp.Learner;
import nlp_annot.AggregateResults;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.xml.sax.SAXException;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.NewOntologyException;
import uk.ac.ebi.brain.error.NonExistingEntityException;
import nlp_annot.NaiveBayes_Mapping;
import nlp_annot.OutsideAnnotationReader;
import text.TextUtils;



public class Main {
    
    public static Logger logger = Logger.getLogger(Main.class);
    
    // Main functions.
    @Option(name="-t", usage="produce text files of phenotype descriptions from tagged data")
    private boolean textprep;
    @Option(name="-a", usage="read charaparser output and write to annotations db")
    private boolean annotate;
    @Option(name="-f", usage="produce a csv file containing feature vectors")
    private boolean generate;
    @Option(name="-c", usage="compose eq statements from class probabilities")
    private boolean compose;
    @Option(name="-p", usage="running nlp to generate parse trees")
    private boolean parse;
        
    // The additional argument -thresh is used for both these with the default of 0.00.
    // The threshold applies towards to what degree word-embeddings are taken into account.
    // Smaller values mean more and more of the weaker information from the embeddings are used.
    @Option(name="-n1", usage="noble coder preprocessing")
    private String ncpre = "";
    @Option(name="-n3", usage="naive bayes classifier")
    private boolean naive;
    @Option(name="-thresh", usage="threshold for the synonyms file from word embeddings")
    private double threshold = 0.0;
    
    // Methods that have to do with using the semantic annotation tools and aggregating.
    @Option(name="-n2", usage="noble coder evaluation")
    private boolean nceval;
    @Option(name="-n22", usage="ncbo annot eval")
    private boolean naeval;
    @Option(name="-fuzzy", usage="use fuzzy matching to infer term scores on the fly")
    private boolean fuzzy;
    @Option(name="-name", usage="provides a name for a group object that is included in output files")
    private String name = "default";
    @Option(name="-agg", usage="agg")
    private String agg = "";
    @Option(name="-w", usage="word file")
    private String wordFiles = "";
    
    // Specify the type of text data if it's applicable.
    @Option(name="-d", usage="text datatype")
    private String dtype = "";
    
   
    @Argument
    private List<String> arguments = new ArrayList<String>();
    
     
     
    public static void main(String[] args) throws OWLOntologyCreationException, NewOntologyException, SQLException, ClassNotFoundException, IOException, ParserConfigurationException, SAXException, NonExistingEntityException, ClassExpressionException, CmdLineException, Exception{
        new Main().doMain(args);
    }
        

    
    public void doMain(String[] args) throws CmdLineException, SQLException, SAXException, IOException, ParserConfigurationException, NewOntologyException, OWLOntologyCreationException, NonExistingEntityException, ClassExpressionException, FileNotFoundException, ClassNotFoundException, Exception{
        
        // Parse the command line arguments.
        CmdLineParser parser = new CmdLineParser(this);
        parser.parseArgument(args); 
        String configPath = arguments.get(0);
        
        // Setup that needs to be run no matter what arguments are given.
        Config config = new Config(configPath);
        Connect conn = new Connect();
        CoreNLP.setup();
        
        
        
        // Anything that changes anything specified in the config objects.
        if (!dtype.equals("")){
            Config.format = dtype.trim();
        }
        else {
            throw new Exception();
        }
        Config.checkFuzzyScore = fuzzy;
        Config.passedInName = name;
        
        
        
        // Everything else.
        if (textprep){
            InputDataPreparer idp = new InputDataPreparer();
            idp.findAndWriteAtoms();
        }
        if (annotate){
            BNLPOutputReader outputReader = new BNLPOutputReader();
        }
        if (generate){
            Run dg = new Run();
            dg.run();
        }
        if (compose){
            Composer c = new Composer();
        }
        if (parse){
            Learner.learn();
        }
        if (!ncpre.equals("")){
            TextUtils.createTextFilesForAllChunks(ncpre, threshold, utils.Util.inferTextType(Config.format)); 
        }
        if (nceval){
            OutsideAnnotationReader.run("nc");
        }
        if (naeval){
            OutsideAnnotationReader.run("na");
        }
        if (naive){
            NaiveBayes_Mapping nb = new NaiveBayes_Mapping();
            nb.run(threshold);
        }
        if (!agg.equals("")){
            AggregateResults.run(agg);
        }
        if (!wordFiles.equals("")){
            TextUtils.createWordFiles(wordFiles);
        }
        logger.info("done");
    }
     
}
