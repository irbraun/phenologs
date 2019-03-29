
package composer;

import enums.EQFormat;
import enums.Ontology;
import enums.Role;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import ontology.Onto;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.brain.error.NonExistingEntityException;




public class EQStatement {

    public Term quality;
    public Term qualifier;
    public Term primaryEntity1;
    public Term primaryEntity2;
    public Term secondaryEntity1;
    public Term secondaryEntity2;
    public Term developmentalStage;
    public double termScore;
    public String dGraphScore;
    public double coverage;
    public ArrayList<Term> termChain;
    public EQFormat format;
    
    
    
    /**
     * Used to generate objects for computationally predicted EQs.
     * @param terms
     * @param format
     * @throws Exception 
     */
    public EQStatement(List<Term> terms, EQFormat format) throws Exception{
        switch(format){
        case EQ:
            primaryEntity1 = new Term(terms.get(0), Role.PRIMARY_ENTITY1_ID);
            quality = new Term(terms.get(1), Role.QUALITY_ID);
            termChain = new ArrayList<>();
            termChain.add(primaryEntity1);
            termChain.add(quality);
            termScore = getAverageTermProbability();
            this.format = format;
            break;
            
        case EQq:
            primaryEntity1 = new Term(terms.get(0), Role.PRIMARY_ENTITY1_ID);
            quality = new Term(terms.get(1), Role.QUALITY_ID);
            qualifier = new Term(terms.get(2), Role.QUALIFIER_ID);
            termChain = new ArrayList<>();
            termChain.add(primaryEntity1);
            termChain.add(quality);
            termChain.add(qualifier);
            termScore = getAverageTermProbability();
            this.format = format;
            break;
            
        case EEQ:
            primaryEntity1 = new Term(terms.get(0), Role.PRIMARY_ENTITY1_ID);
            primaryEntity2 = new Term(terms.get(1), Role.SECONDARY_ENTITY2_ID);
            quality = new Term(terms.get(2), Role.QUALITY_ID);
            termChain = new ArrayList<>();
            termChain.add(primaryEntity1);
            termChain.add(primaryEntity2);
            termChain.add(quality);
            termScore = getAverageTermProbability();
            this.format = format;
            break;
            
        case EEQq:
            primaryEntity1 = new Term(terms.get(0), Role.PRIMARY_ENTITY1_ID);
            primaryEntity2 = new Term(terms.get(1), Role.PRIMARY_ENTITY2_ID);
            quality = new Term(terms.get(2), Role.QUALITY_ID);
            qualifier = new Term(terms.get(3), Role.QUALIFIER_ID);
            termChain = new ArrayList<>();
            termChain.add(primaryEntity1);
            termChain.add(primaryEntity2);
            termChain.add(quality);
            termChain.add(qualifier);
            termScore = getAverageTermProbability();
            this.format = format;
            break;
            
        case EQE:
            primaryEntity1 = new Term(terms.get(0), Role.PRIMARY_ENTITY1_ID);
            quality = new Term(terms.get(1), Role.QUALITY_ID);
            secondaryEntity1 = new Term(terms.get(2), Role.SECONDARY_ENTITY1_ID);
            termChain = new ArrayList<>();
            termChain.add(primaryEntity1);
            termChain.add(quality);
            termChain.add(secondaryEntity1);
            termScore = getAverageTermProbability();
            this.format = format;
            break;
            
        case EQqE:
            primaryEntity1 = new Term(terms.get(0), Role.PRIMARY_ENTITY1_ID);
            quality = new Term(terms.get(1), Role.QUALITY_ID);
            qualifier = new Term(terms.get(2), Role.QUALIFIER_ID);
            secondaryEntity1 = new Term(terms.get(3), Role.SECONDARY_ENTITY1_ID);
            termChain = new ArrayList<>();
            termChain.add(primaryEntity1);
            termChain.add(quality);
            termChain.add(qualifier);
            termChain.add(secondaryEntity1);
            termScore = getAverageTermProbability();
            this.format = format;
            break;
            
        case EEQE:
            primaryEntity1 = new Term(terms.get(0), Role.PRIMARY_ENTITY1_ID);
            primaryEntity2 = new Term(terms.get(1), Role.PRIMARY_ENTITY2_ID);
            quality = new Term(terms.get(2), Role.QUALITY_ID);
            secondaryEntity1 = new Term(terms.get(3), Role.SECONDARY_ENTITY1_ID);
            termChain = new ArrayList<>();
            termChain.add(primaryEntity1);
            termChain.add(primaryEntity2);
            termChain.add(quality);
            termChain.add(secondaryEntity1);
            termScore = getAverageTermProbability();
            this.format = format;
            break;
            
        case EEQqE:
            primaryEntity1 = new Term(terms.get(0), Role.PRIMARY_ENTITY1_ID);
            primaryEntity2 = new Term(terms.get(1), Role.PRIMARY_ENTITY2_ID);
            quality = new Term(terms.get(2), Role.QUALITY_ID);
            qualifier = new Term(terms.get(3), Role.QUALIFIER_ID);
            secondaryEntity1 = new Term(terms.get(4), Role.SECONDARY_ENTITY1_ID);
            termChain = new ArrayList<>();
            termChain.add(primaryEntity1);
            termChain.add(primaryEntity2);
            termChain.add(quality);
            termChain.add(qualifier);
            termChain.add(secondaryEntity1);
            termScore = getAverageTermProbability();
            this.format = format;
            break;
            
        case EQEE:
            primaryEntity1 = new Term(terms.get(0), Role.PRIMARY_ENTITY1_ID);
            quality = new Term(terms.get(1), Role.QUALITY_ID);
            secondaryEntity1 = new Term(terms.get(2), Role.SECONDARY_ENTITY1_ID);
            secondaryEntity2 = new Term(terms.get(3), Role.SECONDARY_ENTITY2_ID);
            termChain = new ArrayList<>();
            termChain.add(primaryEntity1);
            termChain.add(quality);
            termChain.add(secondaryEntity1);
            termChain.add(secondaryEntity2);
            termScore = getAverageTermProbability();
            this.format = format;
            break;
            
        
        case EQqEE:
            primaryEntity1 = new Term(terms.get(0), Role.PRIMARY_ENTITY1_ID);
            quality = new Term(terms.get(1), Role.QUALITY_ID);
            qualifier = new Term(terms.get(2), Role.QUALIFIER_ID);
            secondaryEntity1 = new Term(terms.get(3), Role.SECONDARY_ENTITY1_ID);
            secondaryEntity2 = new Term(terms.get(4), Role.SECONDARY_ENTITY2_ID);
            termChain = new ArrayList<>();
            termChain.add(primaryEntity1);
            termChain.add(quality);
            termChain.add(qualifier);
            termChain.add(secondaryEntity1);
            termChain.add(secondaryEntity2);
            termScore = getAverageTermProbability();
            this.format = format;
            break;

        case EEQEE:
            primaryEntity1 = new Term(terms.get(0), Role.PRIMARY_ENTITY1_ID);
            primaryEntity2 = new Term(terms.get(1), Role.PRIMARY_ENTITY2_ID);
            quality = new Term(terms.get(2), Role.QUALITY_ID);
            secondaryEntity1 = new Term(terms.get(3), Role.SECONDARY_ENTITY1_ID);
            secondaryEntity2 = new Term(terms.get(4), Role.SECONDARY_ENTITY2_ID);
            termChain = new ArrayList<>();
            termChain.add(primaryEntity1);
            termChain.add(primaryEntity2);
            termChain.add(quality);
            termChain.add(secondaryEntity1);
            termChain.add(secondaryEntity2);
            termScore = getAverageTermProbability();
            this.format = format;
            break;
            
        case EEQqEE:
            primaryEntity1 = new Term(terms.get(0), Role.PRIMARY_ENTITY1_ID);
            primaryEntity2 = new Term(terms.get(1), Role.PRIMARY_ENTITY2_ID);
            quality = new Term(terms.get(2), Role.QUALITY_ID);
            qualifier = new Term(terms.get(3), Role.QUALIFIER_ID);
            secondaryEntity1 = new Term(terms.get(4), Role.SECONDARY_ENTITY1_ID);
            secondaryEntity2 = new Term(terms.get(5), Role.SECONDARY_ENTITY2_ID);
            termChain = new ArrayList<>();
            termChain.add(primaryEntity1);
            termChain.add(primaryEntity2);
            termChain.add(quality);
            termChain.add(qualifier);
            termChain.add(secondaryEntity1);
            termChain.add(secondaryEntity2);
            termScore = getAverageTermProbability();
            this.format = format;
            break;
            
        default:
            throw new Exception();
        }        
    }
    
    

    
    
    
    
    
    
    /**
     * Used to generate objects for curated EQs from the corpus.
     * In the corpus file unused components are represented as blank strings, handled here.
     * Each term is assigned its known role. The different scores take on default values.
     * @param components 
     */
    public EQStatement (String[] components){
        String qualityID = components[0];
        String qualifierID = components[1];
        String primaryEntity1ID = components[2];
        String primaryEntity2ID = components[3];
        String secondaryEntity1ID = components[4];
        String secondaryEntity2ID = components[5];
        String developmentalStageID = components[6];    
        termChain = new ArrayList<>();
        
        // Primary Entities
        if (!primaryEntity1ID.equals("")){
            primaryEntity1 = new Term(primaryEntity1ID, utils.Util.inferOntology(primaryEntity1ID), Role.PRIMARY_ENTITY1_ID);
            termChain.add(primaryEntity1);
        }
        if (!primaryEntity2ID.equals("")){
            primaryEntity2 = new Term(primaryEntity2ID, utils.Util.inferOntology(primaryEntity2ID), Role.PRIMARY_ENTITY2_ID);
            termChain.add(primaryEntity2);
        }
        
        // Quality
        if (!qualityID.equals("")){
            quality = new Term(qualityID, utils.Util.inferOntology(qualityID), Role.QUALITY_ID);
            termChain.add(quality);
        }
        
        // Secondary Entities
        if (!secondaryEntity1ID.equals("")){
            secondaryEntity1 = new Term(secondaryEntity1ID, utils.Util.inferOntology(secondaryEntity1ID), Role.SECONDARY_ENTITY1_ID);
            termChain.add(secondaryEntity1);
        }
        if (!secondaryEntity2ID.equals("")){
            secondaryEntity2 = new Term(secondaryEntity2ID, utils.Util.inferOntology(secondaryEntity2ID), Role.SECONDARY_ENTITY2_ID);
            termChain.add(secondaryEntity2);
        }
        
        // Optional qualifier
        if (!qualifierID.equals("")){
            qualifier = new Term(qualifierID, utils.Util.inferOntology(qualifierID), Role.QUALIFIER_ID);
            termChain.add(qualifier);
        }
        
        // Developmental Stage
        if (!developmentalStageID.equals("")){
            developmentalStage = new Term(developmentalStageID, utils.Util.inferOntology(developmentalStageID), Role.DEVELOPMENTAL_STAGE_ID);
            termChain.add(developmentalStage);
        }
        termScore = 1.00;
        dGraphScore = "1.00";
        
    }
    
    
    
    public ArrayList<String> getAllTermIDs(){
        ArrayList<String> termIDs = new ArrayList<>();
        for (Term t: termChain){
            termIDs.add(t.id);
        }
        return termIDs;
    }
    
    
    public ArrayList<Role> getAllTermRoles(){
        ArrayList<Role> termRoles = new ArrayList<>();
        for (Term t: termChain){
            termRoles.add(t.role);
        }
        return termRoles;
    }
    
   
        
    
    public String toIDText(){
        ArrayList<String> idChain = new ArrayList<>();
        for (Term t: (ArrayList<Term>) termChain){
            idChain.add(t.id);
        }
        String text = StringUtils.join(idChain,"+");
        return text;
    }
    
    
    
    public String toLabelText(HashMap<Ontology,Onto> ontoObjects) throws NonExistingEntityException{
        ArrayList<String> labelChain = new ArrayList<>();
        for (Term t: (ArrayList<Term>) termChain){
            try{
                String label = ontoObjects.get(t.ontology).getTermFromTermID(t.id).label;
                labelChain.add(label);
            }
            catch(NullPointerException e){
                String label = "unsupported term";
                labelChain.add(label);
            }
        }
        String text = StringUtils.join(labelChain,"+");
        return text;
    }
    
    
    
    public boolean isSupported(){
        for (Term t: (ArrayList<Term>) termChain){
            if (t.ontology.equals(Ontology.UNSUPPORTED)){
                return false;
            }
        }
        return true;
    }
    
    
    
    public ArrayList<Term> getSupportedTermChain(){
        ArrayList<Term> supportedTermChain = new ArrayList<>();
        for (Term t: termChain){
            if (t.ontology != Ontology.UNSUPPORTED){
                supportedTermChain.add(t);
            }
        }
        return supportedTermChain;
    }
    
    
    
    private double getAverageTermProbability(){
        double sum=0.00;
        for (Term t: termChain){
            sum += t.probability;
        }
        return (double)sum / (double)termChain.size();
    } 
    
    public EQFormat getFormat(){
        return format;
    }
    
    
    
    
    
 
}
