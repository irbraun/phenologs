/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package composer;

import enums.EQFormat;
import enums.Ontology;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import static main.Main.logger;
import ontology.Onto;
import structure.OntologyTerm;
import uk.ac.ebi.brain.error.ClassExpressionException;

/**
 *
 * @author irbraun
 */
public class EQBuilder {
    
    /**
     * Returns a list of candidate EQ statements based on the candidate terms which were provided in the input files,
     * and the average probability assigned to each of the terms for this text. These candidate terms are generated
     * ignoring any other potential source of information like structural similarity to the input text, etc. The 
     * provided chunkID can be of any type like a phenotype ID, atomized statement ID, or predicted atomized statement
     * ID, but it has to correspond with what type of ID was used in the input files.
     * @param ontoObjects
     * @param eTerms
     * @param qTermsSimple
     * @param qTermsRelational
     * @param qlfrTerms
     * @return
     * @throws ClassExpressionException
     * @throws Exception 
     */
    public static ArrayList<EQStatement> getAllPermutations(HashMap<Ontology,Onto> ontoObjects, List<Term> eTerms, List<Term> qTermsSimple, List<Term> qTermsRelational, List<Term> qlfrTerms) throws ClassExpressionException, Exception{
        
        
        ArrayList<EQStatement> predictedEQs = new ArrayList<>();
        
        logger.info("get all permutations is looking at " + eTerms.size() + " entities");
        
                
        // using simple qualities
        for (Term predictedQ: qTermsSimple){
            for (Term predictedE: eTerms){

                
                // Primary entity 1 can't be a ChEBI term.
                if (predictedE.ontology.equals(Ontology.CHEBI)){
                    break;
                }
                
                // If primary entity 1 is a GO:BP or GO:MF, the quality must be a child of process quality in PATO.
                if (predictedE.ontology.equals(Ontology.GO)){
                    OntologyTerm pe1 = ontoObjects.get(Ontology.GO).getTermFromTermID(predictedE.id);
                    OntologyTerm q = ontoObjects.get(Ontology.PATO).getTermFromTermID(predictedQ.id);
                    if ((pe1.allNodes.contains("GO_0008150") || pe1.allNodes.contains("GO_0003674")) && (!q.allNodes.contains("PATO_0001236"))){
                        break;
                    }
                }    
                   
                // Add EQ Statements in the different formats that all use non-relational qualities.
                addEQ(predictedEQs, Arrays.asList(predictedE, predictedQ), EQFormat.EQ);
                for (Term predictedQlfr: qlfrTerms){
                    addEQ(predictedEQs, Arrays.asList(predictedE, predictedQ, predictedQlfr), EQFormat.EQq);
                    for (Term predictedPrimaryE2: eTerms){
                        addEQ(predictedEQs, Arrays.asList(predictedE, predictedPrimaryE2, predictedQ, predictedQlfr), EQFormat.EEQq);
                    }
                }
                for (Term predictedPrimaryE2: eTerms){
                    addEQ(predictedEQs, Arrays.asList(predictedE, predictedPrimaryE2, predictedQ), EQFormat.EEQ);
                }
                
            }
        }

        
        
        // using relational qualities.
        for (Term predictedQ: qTermsRelational){
            for (Term predictedPrE1: eTerms){
                
                // Primary entity 1 can't be a ChEBI term.
                if (predictedPrE1.ontology.equals(Ontology.CHEBI)){
                    break;
                }
                // If primary entity 1 is a GO:BP or GO:MF, the quality must be a child of process quality in PATO.
                if (predictedPrE1.ontology.equals(Ontology.GO)){
                    OntologyTerm pe1 = ontoObjects.get(Ontology.GO).getTermFromTermID(predictedPrE1.id);
                    OntologyTerm q = ontoObjects.get(Ontology.PATO).getTermFromTermID(predictedQ.id);
                    if ((pe1.allNodes.contains("GO_0008150") || pe1.allNodes.contains("GO_0003674")) && (!q.allNodes.contains("PATO_0001236"))){
                        break;
                    }
                    
                }    
                
                
                // Add EQ Statements in the different format that all use relational qualities.
                for (Term predictedSecE1: eTerms){
                    addEQ(predictedEQs, Arrays.asList(predictedPrE1, predictedQ, predictedSecE1), EQFormat.EQE);
                    for (Term predictedQlfr: qlfrTerms){
                        addEQ(predictedEQs, Arrays.asList(predictedPrE1, predictedQ, predictedQlfr, predictedSecE1), EQFormat.EQqE);
                    }
                    // All the formats that add a primary entity 2.
                    for (Term predictedPrE2: eTerms){
                        addEQ(predictedEQs, Arrays.asList(predictedPrE1, predictedPrE2, predictedQ, predictedSecE1), EQFormat.EEQE);
                        for (Term predictedQlfr: qlfrTerms){
                            addEQ(predictedEQs, Arrays.asList(predictedPrE1, predictedPrE2, predictedQ, predictedQlfr, predictedSecE1), EQFormat.EEQqE);
                        }
                        
                        // All the formats that also add a secondary entity 2.
                        for (Term predictedSecE2: eTerms){
                            for (Term predictedQlfr: qlfrTerms){
                                addEQ(predictedEQs, Arrays.asList(predictedPrE1, predictedPrE2, predictedQ, predictedQlfr, predictedSecE1, predictedSecE2), EQFormat.EEQqEE);
                            }
                        }
                    }

                    // All the formats that add a secondary entity 2.
                    for (Term predictedSecE2: eTerms){
                        addEQ(predictedEQs, Arrays.asList(predictedPrE1,  predictedQ, predictedSecE1, predictedSecE2), EQFormat.EQEE);
                        for (Term predictedQlfr: qlfrTerms){
                            addEQ(predictedEQs, Arrays.asList(predictedPrE1, predictedQ, predictedQlfr, predictedSecE1, predictedSecE2), EQFormat.EQqEE);
                        }
                    }
                }
            }
        }
        
        logger.info("there were " + predictedEQs.size() + " eqs sent back from EQ builder");
        return predictedEQs;
    }
    
    
    

    // Checks to make sure terms aren't used twice in the EQ statement and then add its to the list of EQs.
    private static ArrayList<EQStatement> addEQ(ArrayList<EQStatement> predictedEQs, List<Term> terms, EQFormat format) throws Exception{
        HashSet<Term> termSet = new HashSet<>(terms);
        if (termSet.size() == terms.size()){
            EQStatement eq = new EQStatement(terms, format);
            predictedEQs.add(eq);
        }
        return predictedEQs;
    }
    
    
    
    
    
    
    
}
