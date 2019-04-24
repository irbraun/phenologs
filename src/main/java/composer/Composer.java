
package composer;

import composer.Utils.EQComparatorByAllMetricsAlternateOrder;
import enums.Ontology;
import config.Config;
import enums.Role;
import infocontent.InfoContent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import static main.Main.logger; 
import main.Partitions;
import nlp.MyAnnotation;
import nlp_annot.DependencyParsing;
import ontology.Onto;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import structure.Chunk;
import structure.OntologyTerm;
import text.Text;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.NewOntologyException;
import uk.ac.ebi.brain.error.NonExistingEntityException;



public class Composer {
    
    
    private final Text text;
    private final List<Integer> chunkIDs;
    private final HashMap<Ontology,Onto> ontoObjects;
    private final RulesForPATO patoInfo;
    private final HashMap<Integer,ArrayList<Term>> qTermProbabilityTable;
    private final HashMap<Integer,ArrayList<Term>> eTermProbabilityTable;
    private final HashMap<Integer,ArrayList<EQStatement>> predictedEQsPheneMap;
    private final HashMap<Integer,ArrayList<EQStatement>> predictedEQsPhenotypeMap;
    private final DependencyParsing dpg;
    
    
    
    public Composer() throws FileNotFoundException, ClassExpressionException, SQLException, NonExistingEntityException, OWLOntologyCreationException, NewOntologyException, Exception{


        logger.info("reading in the text data");
        text = new Text();
        Partitions parts = new Partitions(text);
        //chunkIDs = parts.getChunkIDsFromPartitionRangeInclusive(0, 31, text.getAllChunksOfDType(Config.format));
        //chunkIDs = parts.getChunkIDsFromPartitionRangeInclusive(0,1,text.getAllChunksOfDType(Config.format));
        chunkIDs = utils.Util.range(3,4);
        
        logger.info("building ontology representations");
        ontoObjects = utils.Util.buildOntoObjects(Ontology.getPlantOntologies());
        //ontoObjects = utils.Util.buildOntoObjects(Ontology.getAllOntologies());
        InfoContent.setup(ontoObjects,text);
       

        // Rules about which terms are optional qualifiers or relational qualities.
        patoInfo = new RulesForPATO(ontoObjects.get(Ontology.PATO));
        
        // Read in annotation files produced from the previous steps.
        logger.info("reading in concept mapping outputs");
        List<String> entityClassProbPaths = new ArrayList<>();
        List<String> qualityClassProbPaths = new ArrayList<>();
        entityClassProbPaths.addAll(Config.classProbsPaths.get(Ontology.PO));
        entityClassProbPaths.addAll(Config.classProbsPaths.get(Ontology.GO));
        entityClassProbPaths.addAll(Config.classProbsPaths.get(Ontology.CHEBI));
        qualityClassProbPaths.addAll(Config.classProbsPaths.get(Ontology.PATO));
        

        // Initialize mappings to produce the gene network files.
        predictedEQsPheneMap = new HashMap<>();
        predictedEQsPhenotypeMap = new HashMap<>();
        

        
        // Build the dependency graphs for the input text data use for scoring.
        dpg = new DependencyParsing();
        
        // Produce an output file for the input set of descriptions.
        logger.info("populating output table");
        eTermProbabilityTable = ComposerIO.readClassProbFiles(entityClassProbPaths, 6);
        qTermProbabilityTable = ComposerIO.readClassProbFiles(qualityClassProbPaths, 4);
        makeTable();
        
        
        System.out.println("MAKING NETWORK");
        
        
        // Look at network similarity.
        logger.info("building gene network files");
        if (Config.buildNetworks){
            makeNetwork();
        }
    }
    
    
    
    
    
    private void makeTable() throws FileNotFoundException, ClassExpressionException, SQLException, NonExistingEntityException, Exception{
        File outputFile = new File(Config.predictedStmtsPath);
        PrintWriter writer = new PrintWriter(outputFile);
        switch(utils.Util.inferTextType(Config.format)){
            case PHENOTYPE:
                produceOutputEQTableForPhenotypes(writer);
                break;
            case SPLIT_PHENOTYPE:
                produceOutputEQTableForSplitPhenotypes(writer);
                break;
            case PHENE:
                produceOutputEQTableForPhenes(writer);
                break;
            default:
                throw new Exception();
        }
        writer.close();
    }
    
        
       
    
    
    
    /**
     * Produces the output table when the class probability tables were generated using atomized statement IDs mapped
     * to ontology terms and probabilities. The source of the predictions made for the candidate ontology terms came from
     * the atomized statements that were curated and already available in the tagged data set used for training and testing.
     * Therefore, the similarities between the curated EQ statements and the predicted EQ statements are very relevant
     * because they are one to one to each other. Those EQ statements come from the exact same text that was used to predict
     * the ontology terms. The phenotype to phenotype similarity is still included, in cases where all of the curated atomized
     * statements for a given phenotype were used as input for this testing set. To achieve this, the original testing/training
     * split has to be drawn from random phenotype (keep all atomized statements together), so that all or none of the
     * atomized statements for a given phenotype wind up in the testing set from which the class probability file is drawn.
     * 
     * Important assumption for this case:
     * Class probabilities must be specified using atomized statement IDs in the input files.
     * 
     * @param writer
     * @throws SQLException
     * @throws NonExistingEntityException
     * @throws ClassExpressionException 
     */
    private void produceOutputEQTableForPhenes(PrintWriter writer) throws SQLException, NonExistingEntityException, ClassExpressionException, Exception{
        
        String header = "species,"
                + "phenotype_id,"
                + "phenotype,"
                + "phene_id,"
                + "phene,"
                + "num_pred,"
                + "eq_labels,"
                + "eq_ids,"
                + "pred_eq_labels,"
                + "pred_eq_ids, "
                + "terms,"
                + "dgraph,"
                + "nodes,"
                + "phene_sim,"
                + "phenotype_sim";
        
        int numColumns = header.split(",").length;
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<numColumns-2; i++){
            sb.append("%s,");
        }
        sb.append("%s");
        String formattedLineMinusOne = sb.toString();
        writer.println(header);

        
        
        // Get the relevant phenotype description IDs so that they can be iterated over.
        HashSet<Integer> atomIDs = new HashSet<>(chunkIDs);
        HashSet<Integer> phenotypeIDs = new HashSet<>();
        for (int atomID: atomIDs){
            int phenotypeID = text.getPhenotypeIDfromAtomID(atomID);
            phenotypeIDs.add(phenotypeID);
        }
        
        
        int ctr=0;

        for (int phenotypeID: phenotypeIDs){
            
            ArrayList<String> dataRowsForThisPhenotype = new ArrayList<>();
            ArrayList<Integer> curatedAtomIDs = text.getAtomIDsFromPhenotypeID(phenotypeID);
            
            // Sanity check to ensure all atomized statements for this phenotype were predicted on.
            if (atomIDs.containsAll(curatedAtomIDs)){
                ArrayList<EQStatement> allCuratedEQsForThisPhenotype = new ArrayList<>();
                ArrayList<EQStatement> allPredictedEQsForThisPhenotype = new ArrayList<>();
                for (int curatedAtomID: curatedAtomIDs){
                    
                    // Get the curated EQ statement.
                    EQStatement curatedEQ = text.getCuratedEQStatementFromAtomID(curatedAtomID);
                    
                    // Get the predicted EQ statement(s).
                    ArrayList<EQStatement> predictedEQs = getPredictedEQs(curatedAtomID);
                    ctr++;
                    updateLog(ctr,100);
                    
                   
                    // When there are no predicted EQs, the row looks like this.
                    if (predictedEQs.isEmpty()){
                        Object[] data = {text.getAtomChunkFromID(curatedAtomID).species.toString().toLowerCase(),
                                        phenotypeID, 
                                        text.getPhenotypeDescStr(phenotypeID).replace(",", ""),
                                        curatedAtomID,
                                        text.getAtomizedStatementStr(curatedAtomID).replace(",", ""), 
                                        predictedEQs.size(),
                                        text.getCuratedEQStatementFromAtomID(curatedAtomID).toLabelText(ontoObjects).replace(",", ""),
                                        text.getCuratedEQStatementFromAtomID(curatedAtomID).toIDText().replace(",", ""),
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                        };          
                        if (data.length!=(numColumns-1)){
                            throw new Exception(String.format("%s %s",data.length,numColumns));
                        }
                        dataRowsForThisPhenotype.add(String.format(formattedLineMinusOne, data));
                        
                        
                        
                        
                    }
                    
                    
                    
                    
                    // Iterate through the predicted EQs when they exist.
                    for (EQStatement predictedEQ: predictedEQs){
                        double similarity = Utils.getTermSetSimilarity(predictedEQ, text.getCuratedEQStatementFromAtomID(curatedAtomID), ontoObjects);
                        // Things that belong in the table (minus phenotype similarity).
                        Object[] data = {text.getAtomChunkFromID(curatedAtomID).species.toString().toLowerCase(),
                                        phenotypeID, 
                                        text.getPhenotypeDescStr(phenotypeID).replace(",", ""),
                                        curatedAtomID,
                                        text.getAtomizedStatementStr(curatedAtomID).replace(",", ""), 
                                        predictedEQs.size(),
                                        text.getCuratedEQStatementFromAtomID(curatedAtomID).toLabelText(ontoObjects).replace(",", ""),
                                        text.getCuratedEQStatementFromAtomID(curatedAtomID).toIDText().replace(",", ""),
                                        predictedEQ.toLabelText(ontoObjects).replace(",", ""),
                                        predictedEQ.toIDText().replace(",", ""),
                                        predictedEQ.termScore,
                                        predictedEQ.dGraphScore,
                                        predictedEQ.coverage,
                                        similarity,
                        };          
                        if (data.length!=(numColumns-1)){
                            throw new Exception(String.format("%s %s",data.length,numColumns));
                        }
                        dataRowsForThisPhenotype.add(String.format(formattedLineMinusOne, data));





                        
                        // Remember this EQ for looking at network similarity later.
                        ArrayList<EQStatement> eqs = predictedEQsPheneMap.getOrDefault(curatedAtomID, new ArrayList<>());
                        eqs.add(predictedEQ);
                        predictedEQsPheneMap.put(curatedAtomID, eqs);
                        ArrayList<EQStatement> eqs2 = predictedEQsPhenotypeMap.getOrDefault(phenotypeID, new ArrayList<>());
                        eqs2.add(predictedEQ);
                        predictedEQsPhenotypeMap.put(phenotypeID, eqs2);
                        
                        
                    }

                    // The growing lists of all the EQs for this phenotype as a whole.
                    allPredictedEQsForThisPhenotype.addAll(predictedEQs);
                    allCuratedEQsForThisPhenotype.add(curatedEQ);

                }  

                // Done with all the atomized statements in this phenotype. Find the phenotype similarity
                // Output all the rows for each of the phenes that were included within this phenotypes, they have all values now.
                double phenotypeSimilarity = Utils.getTermSetSimilarity(allPredictedEQsForThisPhenotype, allCuratedEQsForThisPhenotype, ontoObjects);   
                for (String dataRow: dataRowsForThisPhenotype){
                    writer.println(String.format("%s,%s",dataRow,phenotypeSimilarity));
                }
            }
            
            else {
                // This message should never be seen. Not throwing an exception here only for testing purposes.
                // This means that the partitioning of the corpus isn't working how it's expected to because
                // the atomized statements are supposed to go with their phenotype to whatever partition, so all
                // of them would be included whenever the phenotype is iterated over in a testing set.
                logger.info("some atomized statements missing for phenotype ID=" + phenotypeID);
            }  
        } 
    }
    

    private void produceOutputEQTableForPhenotypes(PrintWriter writer) throws SQLException, NonExistingEntityException, ClassExpressionException, Exception{
        
        String header = "species,"
                + "phenotype_id,"
                + "phenotype,"
                + "num_pred,"
                + "pred_eq_labels,"
                + "pred_eq_ids, "
                + "terms,"
                + "dgraph,"
                + "nodes,"
                + "phenotype_sim";
        
        int numColumns = header.split(",").length;
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<numColumns-2; i++){
            sb.append("%s,");
        }
        sb.append("%s");
        String formattedLineMinusOne = sb.toString();
        writer.println(header);

        
        // Get the relevant phenotype description IDs so that they can be iterated over.
        HashSet<Integer> phenotypeIDs = new HashSet<>(chunkIDs);
        
        
        /*
        for (int atomID: atomIDs){
            int phenotypeID = text.getPhenotypeIDfromAtomID(atomID);
            phenotypeIDs.add(phenotypeID);
        }
        */
        
        int ctr=0;

        for (int phenotypeID: phenotypeIDs){
            
            ArrayList<String> dataRowsForThisPhenotype = new ArrayList<>();

            // Get the curated and predicted EQs for this phenotype description.
            ArrayList<EQStatement> allCuratedEQsForThisPhenotype = text.getCuratedEQStatementsFromPhenotypeID(phenotypeID);
            ArrayList<EQStatement> allPredictedEQsForThisPhenotype = getPredictedEQs(phenotypeID);
            ArrayList<EQStatement> predictedEQs = new ArrayList<>(allPredictedEQsForThisPhenotype);
            
            ctr++;
            updateLog(ctr,100);
            
            // When there are no predicted EQs for the phenotype, the row looks like this.
            if (predictedEQs.isEmpty()){
                Object[] data = {text.getPhenotypeChunkFromID(phenotypeID).species.toString().toLowerCase(),
                                phenotypeID, 
                                text.getPhenotypeDescStr(phenotypeID).replace(",", ""),
                                predictedEQs.size(),
                                "",
                                "",
                                "",
                                "",
                                "",
                };          
                if (data.length!=(numColumns-1)){
                    throw new Exception(String.format("%s %s",data.length,numColumns));
                }
                dataRowsForThisPhenotype.add(String.format(formattedLineMinusOne, data));
            }
            
            
            // Iterate through the predicted EQ statements for this phenotype if there are any.
            for (EQStatement predictedEQ: predictedEQs){
                // Things that belong in the table (minus phenotype similarity).
                Object[] data = {text.getAtomChunkFromID(phenotypeID).species.toString().toLowerCase(),
                                phenotypeID, 
                                text.getPhenotypeDescStr(phenotypeID).replace(",", ""),
                                predictedEQs.size(),
                                predictedEQ.toLabelText(ontoObjects).replace(",", ""),
                                predictedEQ.toIDText().replace(",", ""),
                                predictedEQ.termScore,
                                predictedEQ.dGraphScore,
                                predictedEQ.coverage,
                };          
                if (data.length!=(numColumns-1)){
                    throw new Exception(String.format("%s %s",data.length,numColumns));
                }
                dataRowsForThisPhenotype.add(String.format(formattedLineMinusOne, data));
            }  
            
            // Remember these EQs for looking at network similarity later.
            ArrayList<EQStatement> eqs2 = predictedEQsPhenotypeMap.getOrDefault(phenotypeID, new ArrayList<>());
            eqs2.addAll(predictedEQs);
            predictedEQsPhenotypeMap.put(phenotypeID, eqs2);

            // Add the phenotype similarity value to the lines and write them to the file.
            double phenotypeSimilarity = Utils.getTermSetSimilarity(allPredictedEQsForThisPhenotype, allCuratedEQsForThisPhenotype, ontoObjects);   
            for (String dataRow: dataRowsForThisPhenotype){
                writer.println(String.format("%s,%s",dataRow,phenotypeSimilarity));
            }

        } 
    }
    

    private void produceOutputEQTableForSplitPhenotypes(PrintWriter writer) throws SQLException, NonExistingEntityException, ClassExpressionException, Exception{
        
        String header = "species,"
                + "phenotype_id,"
                + "phenotype,"
                + "phene_id,"
                + "phene,"
                + "num_pred,"
                + "pred_eq_labels,"
                + "pred_eq_ids, "
                + "terms,"
                + "dgraph,"
                + "nodes,"
                + "phenotype_sim";
        
        int numColumns = header.split(",").length;
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<numColumns-2; i++){
            sb.append("%s,");
        }
        sb.append("%s");
        String formattedLineMinusOne = sb.toString();
        writer.println(header);

        
        
        // Get the relevant phenotype description IDs so that they can be iterated over.
        HashSet<Integer> splitIDs = new HashSet<>(chunkIDs);
        HashSet<Integer> phenotypeIDs = new HashSet<>();
        for (int splitID: splitIDs){
            int phenotypeID = text.getPhenotypeIDfromSplitPhenotypeID(splitID);
            phenotypeIDs.add(phenotypeID);
        }
        
        
        int ctr=0;

        for (int phenotypeID: phenotypeIDs){
            
            ArrayList<String> dataRowsForThisPhenotype = new ArrayList<>();
            ArrayList<Integer> splitDescIDs = text.getSplitPhenotypeIDsFromPhenotypeID(phenotypeID);
           
            ArrayList<EQStatement> allCuratedEQsForThisPhenotype = text.getCuratedEQStatementsFromPhenotypeID(phenotypeID);
            ArrayList<EQStatement> allPredictedEQsForThisPhenotype = new ArrayList<>();
            for (int sID: splitDescIDs){

                // Get the predicted EQ statement(s).
                ArrayList<EQStatement> predictedEQs = getPredictedEQs(sID);
                ctr++;
                updateLog(ctr,100);

                // When there are no predicted EQs, the row looks like this.
                if (predictedEQs.isEmpty()){
                    Object[] data = {text.getAtomChunkFromID(text.getAtomIDsFromPhenotypeID(phenotypeID).get(0)).species.toString().toLowerCase(),
                                    phenotypeID, 
                                    text.getPhenotypeDescStr(phenotypeID).replace(",", ""),
                                    sID,
                                    text.getSplitPhenotypeChunkFromID(sID).getRawText().replace(",", ""),
                                    predictedEQs.size(),
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                    };          
                    if (data.length!=(numColumns-1)){
                        throw new Exception(String.format("%s %s",data.length,numColumns));
                    }
                    dataRowsForThisPhenotype.add(String.format(formattedLineMinusOne, data));




                }

                // Iterate through the predicted EQs when they exist.
                for (EQStatement predictedEQ: predictedEQs){
                    //double similarity = Utils.getSimilarity(predictedEQ, text.getCuratedEQStatementFromAtomID(curatedAtomID), ontoObjects);
                    // Things that belong in the table (minus phenotype similarity).
                    Object[] data = {text.getAtomChunkFromID(text.getAtomIDsFromPhenotypeID(phenotypeID).get(0)).species.toString().toLowerCase(),
                                    phenotypeID, 
                                    text.getPhenotypeDescStr(phenotypeID).replace(",", ""),
                                    sID,
                                    text.getSplitPhenotypeChunkFromID(sID).getRawText().replace(",", ""),
                                    predictedEQs.size(),
                                    predictedEQ.toLabelText(ontoObjects).replace(",", ""),
                                    predictedEQ.toIDText().replace(",", ""),
                                    predictedEQ.termScore,
                                    predictedEQ.dGraphScore,
                                    predictedEQ.coverage,
                    };          
                    if (data.length!=(numColumns-1)){
                        throw new Exception(String.format("%s %s",data.length,numColumns));
                    }
                    dataRowsForThisPhenotype.add(String.format(formattedLineMinusOne, data));



                    // Remember this EQ for looking at network similarity later.
                    //ArrayList<EQStatement> eqs = predictedEQsPheneMap.getOrDefault(curatedAtomID, new ArrayList<>());
                    //eqs.add(predictedEQ);
                    //predictedEQsPheneMap.put(curatedAtomID, eqs);
                    ArrayList<EQStatement> eqs2 = predictedEQsPhenotypeMap.getOrDefault(phenotypeID, new ArrayList<>());
                    eqs2.add(predictedEQ);
                    predictedEQsPhenotypeMap.put(phenotypeID, eqs2);


                }

                // The growing lists of all the EQs for this phenotype as a whole.
                allPredictedEQsForThisPhenotype.addAll(predictedEQs);

            }  

            // Done with all the atomized statements in this phenotype. Find the phenotype similarity
            // Output all the rows for each of the phenes that were included within this phenotypes, they have all values now.
            double phenotypeSimilarity = Utils.getTermSetSimilarity(allPredictedEQsForThisPhenotype, allCuratedEQsForThisPhenotype, ontoObjects);   
            for (String dataRow: dataRowsForThisPhenotype){
                writer.println(String.format("%s,%s",dataRow,phenotypeSimilarity));
            }

        } 
    }
    
    


    
    
    
    
    
    
    /**
     * Generate the edges and nodes files for network analysis for graphs built using the annotations.
     * Edge and node file with relevant attributes for each link or node built separately to be used
     * with the igraph package. Note this does not currently work when using the phenotype text data, 
     * assumes that the text chunks are atomized statements.
     * @throws NonExistingEntityException
     * @throws FileNotFoundException
     * @throws Exception 
     */
    private void makeNetwork() throws NonExistingEntityException, FileNotFoundException, Exception{

        
        // Phenotype and phene networks.
        File pheneEdgeValuesFile = new File(Config.pheneNetworkPath);
        File phenotypeEdgeValuesFile = new File(Config.phenotypeNetworkPath);
        PrintWriter pheneWriter = new PrintWriter(pheneEdgeValuesFile);
        PrintWriter phenotypeWriter = new PrintWriter(phenotypeEdgeValuesFile);
        pheneWriter.println("phene_1, phene_2, phenotype_1, phenotype_2, p_edge, c_edge");
        phenotypeWriter.println("phenotype_1, phenotype_2, p_edge, c_edge");
        
        
        /*
        Modification so that the network that is shown includes all of the available nodes
        instead of just the ones that have atleast one predicted EQ statement. Would just need to make
        the p_edge column NA for that entry in the output file. Then these could be included in the 
        calculations for table to make the values that are reported accurate but could still be removed
        from the visualization to save space if necessary.
        Making some other changes to make sure that this works with all the methods of reading the text.
        */
        HashSet<Integer> atomIDsSet = new HashSet<>();
        HashSet<Integer> phenotypeIDsSet = new HashSet<>();
        switch (utils.Util.inferTextType(Config.format)) {
            case PHENE:
                for (int chunkID: chunkIDs){
                    int phenotypeID = text.getPhenotypeIDfromAtomID(chunkID);
                    atomIDsSet.add(chunkID);
                    phenotypeIDsSet.add(phenotypeID);
                }   break;
            case PHENOTYPE:
                for (int chunkID: chunkIDs){
                    int phenotypeID = chunkID;
                    phenotypeIDsSet.add(phenotypeID);
                }   break;
            case SPLIT_PHENOTYPE:
                for (int chunkID: chunkIDs){
                    int phenotypeID = text.getPhenotypeIDfromSplitPhenotypeID(chunkID);
                    phenotypeIDsSet.add(phenotypeID);
                }   break;
            default:
                throw new Exception();
        }
        
        ArrayList<Integer> atomIDs = new ArrayList<>(atomIDsSet);
        ArrayList<Integer> phenotypeIDs = new ArrayList<>(phenotypeIDsSet);
        
        
        // Phene network.
        for (int i=0; i<atomIDs.size(); i++){
            for (int j=i+1; j<atomIDs.size(); j++){
                int atomID1 = atomIDs.get(i);
                int atomID2 = atomIDs.get(j);
                int associatedPhenotypeID1 = text.getPhenotypeIDfromAtomID(atomID1);
                int associatedPhenotypeID2 = text.getPhenotypeIDfromAtomID(atomID2);
                // Don't need to use try catch here because the -1 is already returned if there is an acceptable problem.
                double predictedSim = Utils.getEQSimilarity(predictedEQsPheneMap.get(atomID1), predictedEQsPheneMap.get(atomID2), ontoObjects);
                // Don't need to use try catch here for the same reason.
                double curatedSim = Utils.getEQSimilarity(text.getCuratedEQStatementFromAtomID(atomID1), text.getCuratedEQStatementFromAtomID(atomID2), ontoObjects);
                Object[] items = {atomID1, atomID2, associatedPhenotypeID1, associatedPhenotypeID2, predictedSim, curatedSim};
                pheneWriter.println(String.format("%s,%s,%s,%s,%.3f,%.3f",items));
            }
        }

        // Phenotype network.
        for (int i=0; i<phenotypeIDs.size(); i++){
            for (int j=i+1; j<phenotypeIDs.size(); j++){
                int phenotypeID1 = phenotypeIDs.get(i);
                int phenotypeID2 = phenotypeIDs.get(j);
                double predictedSim = Utils.getEQSimilarity(predictedEQsPhenotypeMap.get(phenotypeID1), predictedEQsPhenotypeMap.get(phenotypeID2), ontoObjects);
                double curatedSim = Utils.getEQSimilarity(text.getCuratedEQStatementsFromAtomIDs(text.getAtomIDsFromPhenotypeID(phenotypeID1)), text.getCuratedEQStatementsFromAtomIDs(text.getAtomIDsFromPhenotypeID(phenotypeID2)), ontoObjects);
                Object[] items = {phenotypeID1, phenotypeID2, predictedSim, curatedSim};
                phenotypeWriter.println(String.format("%s,%s,%.3f,%.3f",items));
            }
        }
        pheneWriter.close();
        phenotypeWriter.close();


        // Generate files that have the ID's for all the phenes and phenotypes that form the nodes 
        // in the networks. This way can save attributes about them such as the species they pertain
        // to that can be used in the plots. Is this necessary anymore after changing the R script?
        File pheneNodeFile = new File(Config.pheneNodesPath);
        File phenotypeNodeFile = new File(Config.phenotypeNodesPath);
        PrintWriter pheneNodeWriter = new PrintWriter(pheneNodeFile);
        PrintWriter phenotypeNodeWriter = new PrintWriter(phenotypeNodeFile);
        pheneNodeWriter.println("id, species");
        phenotypeNodeWriter.println("id, species");
        for (int atomID: atomIDs){
            String species = text.getAtomChunkFromID(atomID).species.toString().toLowerCase().trim();
            Object[] items = {atomID, species};
            pheneNodeWriter.println(String.format("%s,%s",items));
        }   
        for (int phenotypeID: phenotypeIDs){
            String species = text.getPhenotypeChunkFromID(phenotypeID).species.toString().toLowerCase().trim();
            Object[] items = {phenotypeID, species};
            phenotypeNodeWriter.println(String.format("%s,%s", items)); 
        }
        pheneNodeWriter.close();
        phenotypeNodeWriter.close();
        
    }
    
    
    
    /**
     * Rule-based approach to constructing EQ statements from provided terms. See the document where
     * the steps used in this approach are described one by one. Make sure this is always written in
     * a way where it's easy to remove or add steps and the pipeline for creating EQs from the sets
     * of candidate terms still always works and all the metrics and scoring still apply the same way.
     * @param chunkID
     * @return
     * @throws ClassExpressionException
     * @throws Exception 
     */
    private ArrayList<EQStatement> getPredictedEQs(int chunkID) throws ClassExpressionException, Exception{

        // The starting lists of candidate EQs.
        ArrayList<Term> predictedQs = (ArrayList<Term>) qTermProbabilityTable.getOrDefault(chunkID, new ArrayList<>());
        ArrayList<Term> predictedEs = (ArrayList<Term>) eTermProbabilityTable.getOrDefault(chunkID, new ArrayList<>());

        // Run the NLP pipeline to get depedencies, constituencies, POS, any other information.
        Chunk chunk = text.getChunkFromIDWithDType(chunkID, utils.Util.inferTextType(Config.format));
        MyAnnotation annot = Modifier.getAnnotation(chunk);

        // Retain only the most specific terms.
        //predictedQs = collapse(predictedQs);
        //predictedEs = collapse(predictedEs);

        // Categorizing PATO terms broadly into those belonging to the relational slim, optional qualifiers, and other.
        ArrayList<Term> predictedQsSimple = new ArrayList<>();
        ArrayList<Term> predictedQsRelational = new ArrayList<>();
        ArrayList<Term> predictedQualifiers = new ArrayList<>();
        for (Term t: predictedQs){
            if (patoInfo.relationalQualityIDs.contains(t.id)){
                predictedQsRelational.add(t);
            }
            else if (patoInfo.qualifierIDs.contains(t.id)){
                predictedQualifiers.add(t);
            }
            else {
                predictedQsSimple.add(t);
            }
        }

        // Optional removal steps that try to pair down the possible terms.
        logger.info(String.format("processing chunk %s (%s)\n",chunkID,text.getAtomChunkFromID(chunkID).getRawText()));
        logger.info(String.format("%s candidate E",predictedEs.size()));
        for (Term t: predictedEs){
            logger.info(String.format("%s (%s)",t.id, ontoObjects.get(utils.Util.inferOntology(t.id)).getTermFromTermID(t.id).label));
        }
        logger.info(String.format("%s candidate Q",predictedQs.size()));
        for (Term t: predictedQs){
            logger.info(String.format("%s (%s)",t.id, ontoObjects.get(utils.Util.inferOntology(t.id)).getTermFromTermID(t.id).label));
        }


        // Optional removal step checking for redundant entities.
        logDeleteTerms("removing %s redundant entity terms", Modifier.findRedundantEntities(predictedEs));
        //predictedEs.removeAll(Modifier.findRedundantEntities(predictedEs));
        //predictedQsSimple.removeAll(Modifier.findRedundantQualities(predictedQsSimple));
        //predictedQsRelational.removeAll(Modifier.findRedundantQualities(predictedQsRelational));
        //predictedQualifiers.removeAll(Modifier.findRedundantQualities(predictedQualifiers));

        // If no entity that could be the first primary entity is predicted at all, insert the term used as an implied subject.
        int numEligiblePrimE1 = 0;
        for (Term t: predictedEs){
            if (!t.ontology.equals(Ontology.CHEBI) && !t.ontology.equals(Ontology.UNSUPPORTED)){
                numEligiblePrimE1++;
                break;
            }
        }
        if (numEligiblePrimE1 == 0){
            predictedEs.add(new Term("PO_0000003", 1.000,Ontology.PO, new HashSet<>()));
            logger.info("default entity was added");
        }

        // If atleast one of the entities is a GO process, include 'process quality' as a possible Q. 
        for (Term t: predictedEs){
            if(t.ontology.equals(Ontology.GO)){
                OntologyTerm term = ontoObjects.get(Ontology.GO).getTermFromTermID(t.id);
                if (term.allNodes.contains("GO_0008150") || term.allNodes.contains("GO_0003674")){
                    predictedQs.add(new Term("PATO_0001236",1.000,Ontology.PATO,new HashSet<>()));
                }
            }
        }

        // Get all possible permutations of the terms within an acceptable EQ statement structure.
        ArrayList<EQStatement> predictedEQs = EQBuilder.getAllPermutations(ontoObjects, predictedEs, predictedQsSimple, predictedQsRelational, predictedQualifiers);

        logger.info(String.format("there are %s candidate EQs",predictedEQs.size()));
        for (EQStatement eq: predictedEQs){
            logger.info(eq.toLabelText(ontoObjects));
        }
        
        // If an optional qualifier term is present always use it.
        if (!predictedQualifiers.isEmpty()){
            logDeleteEQs("removing %s EQs that are missing a found qualifier", Modifier.getNonQualifierEQs(predictedEQs));
            predictedEQs.removeAll(Modifier.getNonQualifierEQs(predictedEQs));
        }
        
        // Check the dependencies between terms involved in post-composed (complex) entities, and remove problems.
        logDeleteEQs("removing %s EQs with bad complex entities", Modifier.getInvalidComplexEQs(predictedEQs, annot));
        //predictedEQs.removeAll(Modifier.getInvalidComplexEQs(predictedEQs, annot));


        // Check for cases where one of the entity terms is overlapping with either the quality term or optional qualifier.
        logDeleteEQs("removing %s EQs where an E overlaps with a Q", Modifier.getRedundantEQs(predictedEQs));
        //predictedEQs.removeAll(Modifier.getRedundantEQs(predictedEQs));


        logger.info(String.format("there are %s accepted EQs\n",predictedEQs.size()));

        // Assign some score values to the predicted EQ statements based on other information.
        checkCoverage(chunkID, predictedEQs);
        checkDepGraph(annot, predictedEQs);

        // Only take a maximum of k EQ statements as output for each description input.
        int threshold = 4;
        predictedEQs.sort(new EQComparatorByAllMetricsAlternateOrder());
        predictedEQs = new ArrayList<>(predictedEQs.subList(0, Math.min(threshold, predictedEQs.size())));


        return predictedEQs;

    }
    
    
    
    
    
    
    
    private void logDeleteTerms(String message, List<Term> terms){
        logger.info(String.format(message,terms.size()));
        for (Term t: terms){
            logger.info(String.format("%s",t.id));
        }
    }
       
    private void logDeleteEQs(String message, List<EQStatement> eqs) throws NonExistingEntityException{
        logger.info(String.format(message,eqs.size()));
        for (EQStatement eq: eqs){
            logger.info(eq.toLabelText(ontoObjects));
        }
    }
    
    
    
    /**
     * Coverage is defined as the fraction of nodes in dG that the contents of the ontology term is
     * related or mapped to.
     * TODO add check to throw exception if nodes appear in the EQ statement that don't appear in
     * the text chunk, because that should never occur.
     * @param chunkID
     * @param eqs 
     */
    private void checkCoverage(int chunkID, ArrayList<EQStatement>eqs){
        HashSet<String> chunkNodes = new HashSet<>(text.getAtomChunkFromID(chunkID).getBagValues());
        for (EQStatement eq: eqs){
            HashSet<String> eqNodes = new HashSet<>();
            for (Term t: eq.termChain){
                eqNodes.addAll(t.nodes);
            }
            eq.coverage = (double) eqNodes.size() / (double) chunkNodes.size();
        }
    }
    
    
    
    
    /**
     * Finds the joint probability of the path lengths between nodes corresponding to certain pairs
     * of ontology terms in the EQs. Note that there are only three minimal paths that are currently
     * looked at because this was the limit of the examples available in the training data, could be
     * changed when more tagged descriptions are available for the length frequencies to be counted
     * on. When the terms for some of the paths are not present in the EQ, then the probability for
     * that path is assumed to be 1. In other words, there is no penalty for not included added
     * complexity in the EQ statement for this score (other scores, like coverage, might suffer). 
     * If the terms are present, but a minimal path cannot be found (nearly always because no nodes
     * are associated with the term) then the length is treated as a number for which the probability
     * is 0, so the joint is automatically sent all the way to 0. This way we get a numerical score
     * for every EQ statement.
     * @param annot
     * @param eqs
     * @throws Exception 
     */
    private void checkDepGraph(MyAnnotation annot, ArrayList<EQStatement>eqs) throws Exception{
        for (EQStatement eq: eqs){
            // Path from nodes for the first primary entity to the quality.
            int len1 = Modifier.getMinPathLength(eq.primaryEntity1.nodes, eq.quality.nodes, annot);
            double p1 = dpg.getProbability(Role.PRIMARY_ENTITY1_ID, Role.QUALITY_ID, len1);
            // Path from nodes for the first primary entity to the second primary entity.
            double p2 = 1.00;
            if (eq.primaryEntity2!=null){
                int len2 = Modifier.getMinPathLength(eq.primaryEntity1.nodes, eq.primaryEntity2.nodes, annot);
                p2 = dpg.getProbability(Role.PRIMARY_ENTITY1_ID, Role.PRIMARY_ENTITY2_ID, len2);
            }
            // Path from nodes for the first primary entity to the first secondary entity.
            double p3 = 1.00;
            if (eq.secondaryEntity2!=null){
                int len3 = Modifier.getMinPathLength(eq.primaryEntity1.nodes, eq.secondaryEntity1.nodes, annot);
                p3 = dpg.getProbability(Role.PRIMARY_ENTITY1_ID, Role.SECONDARY_ENTITY1_ID, len3);
                
            }
            // Get the combined probability.
            double score = p1*p2*p3;
            eq.dGraphScore = String.valueOf(score);
        }
    }
    
    
    
    
    /**
     * Takes a list of predicted terms and reduces it by removing any term T1 where some T2 exists that is a
     * descendant of T1 but where T2 has a high class probability associated with it than T1 does. In other
     * words, less specific terms are removed if and only if the list includes some more specific term that 
     * also has a higher probability of mapping to the input text.
     * @param predictedTerms
     * @return
     * @throws ClassExpressionException 
     */
    private ArrayList<Term> collapse(ArrayList<Term> predictedTerms) throws ClassExpressionException{
        
        //List of the string ontology IDs of the predicted terms.
        ArrayList<String> predictedTermIDs = new ArrayList<>();
        for (Term t: predictedTerms){
            predictedTermIDs.add(t.id);
        }
        
        //Remove any inherited terms that have lower probability than their descendants.
        ArrayList<String> savePredictedTermIDs = new ArrayList<>(predictedTermIDs);
        ArrayList<Term> savePredictedTerms = new ArrayList<>(predictedTerms);
        for (Term pt: predictedTerms){
            List<String> inheritedTermIDs = ontoObjects.get(pt.ontology).getTermFromTermID(pt.id).inheritedNodes;
            for (String inheritedNodeID: inheritedTermIDs){
                for (Term ptOther: predictedTerms){
                    if (inheritedNodeID.equals(ptOther.id) && (ptOther.probability <= pt.probability)){
                        int indexToDelete = savePredictedTermIDs.indexOf(ptOther.id);
                        if (indexToDelete != -1){
                            savePredictedTermIDs.remove(indexToDelete);
                            savePredictedTerms.remove(indexToDelete);
                        }
                        
                    }
                }
            }
        }
        return savePredictedTerms;
    }
   
   
    
    
    

    
    private void updateLog(int ctr, int step){
        if (ctr%step==0){
            logger.info(String.format("%s chunks processed",ctr));
        }
    }
    
    
    
    
}
    
    
    
    
   
    
    
    
    
    
   
    
    
    
