
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
    public ArrayList<Term> termChain;
    private double[] dGScores;
    private double coverage;
    private double avgTermScore;
    private EQFormat format;
    private String[] componentStrings;
    private boolean fromCuratedDataSet;
    
    
    
    /**
     * Constructor used to generate objects for computationally predicted EQs.
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
            setAverageTermScore();
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
            setAverageTermScore();
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
            setAverageTermScore();
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
            setAverageTermScore();
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
            setAverageTermScore();
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
            setAverageTermScore();
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
            setAverageTermScore();
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
            setAverageTermScore();
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
            setAverageTermScore();
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
            setAverageTermScore();
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
            setAverageTermScore();
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
            setAverageTermScore();
            this.format = format;
            break;
            
        default:
            throw new Exception();
        }        
        
        fromCuratedDataSet = false;
        
    }
    
    

    
    
    
    
    
    
    /**
     * Construct used to generate objects for curated EQs from the corpus.
     * In the corpus file unused components are represented as blank strings, handled here.
     * Each term is assigned its known role. The different scores take on default values.
     * Assumes that the formatting for EQs is followed correctly, does not enforce that any
     * of the components are not missing for example.
     * @param components 
     */
    public EQStatement (String[] components) throws Exception{
        String qualityID = components[0];
        String qualifierID = components[1];
        String primaryEntity1ID = components[2];
        String primaryEntity2ID = components[3];
        String secondaryEntity1ID = components[4];
        String secondaryEntity2ID = components[5];
        String developmentalStageID = components[6];    
        componentStrings = components;
        termChain = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        
        
        // The StringBuilder keeps track of which components are here to find the right enumerated format type.
        // In order for that to work the characters must be added in the order specified for the EQFormat class.
        
        // Primary Entities
        if (!primaryEntity1ID.equals("")){
            primaryEntity1 = new Term(primaryEntity1ID, utils.Utils.inferOntology(primaryEntity1ID), Role.PRIMARY_ENTITY1_ID);
            termChain.add(primaryEntity1);
            sb.append("E");
        }
        if (!primaryEntity2ID.equals("")){
            primaryEntity2 = new Term(primaryEntity2ID, utils.Utils.inferOntology(primaryEntity2ID), Role.PRIMARY_ENTITY2_ID);
            termChain.add(primaryEntity2);
            sb.append("E");
        }
        
        // Quality
        if (!qualityID.equals("")){
            quality = new Term(qualityID, utils.Utils.inferOntology(qualityID), Role.QUALITY_ID);
            termChain.add(quality);
            sb.append("Q");
        }
        // Optional qualifier
        if (!qualifierID.equals("")){
            qualifier = new Term(qualifierID, utils.Utils.inferOntology(qualifierID), Role.QUALIFIER_ID);
            termChain.add(qualifier);
            sb.append("q");
        }
        
        // Secondary Entities
        if (!secondaryEntity1ID.equals("")){
            secondaryEntity1 = new Term(secondaryEntity1ID, utils.Utils.inferOntology(secondaryEntity1ID), Role.SECONDARY_ENTITY1_ID);
            termChain.add(secondaryEntity1);
            sb.append("E");
        }
        
        if (!secondaryEntity2ID.equals("")){
            secondaryEntity2 = new Term(secondaryEntity2ID, utils.Utils.inferOntology(secondaryEntity2ID), Role.SECONDARY_ENTITY2_ID);
            termChain.add(secondaryEntity2);
            sb.append("E");
        }
        
        // Developmental Stage
        /*
        if (!developmentalStageID.equals("")){
            developmentalStage = new Term(developmentalStageID, utils.Utils.inferOntology(developmentalStageID), Role.DEVELOPMENTAL_STAGE_ID);
            termChain.add(developmentalStage);
        }
        */
        fromCuratedDataSet = true;
        try{
            format = EQFormat.valueOf(sb.toString());
        }
        catch(IllegalArgumentException e){
            System.out.println(toIDText());
            System.out.println(sb.toString());
            format = EQFormat.UNKNOWN;
            throw new Exception();
        }
    }
    
    
    
    
    
    
    
    // Setters and getters for the different metrics to evaluate a single EQ statement.
    public void setDependencyGraphValues(double[] scores){
        dGScores = scores;
    }
    public double[] getDependencyGraphValues(){
        return dGScores;
    }
    
    public void setNodeOverlap(double overlap){
        coverage = overlap;
    }
    public double getNodeOverlap(){
        return coverage;
    }
    private void setAverageTermScore(){
        double sum=0.00;
        for (Term t: termChain){
            sum += t.probability;
        }
        avgTermScore = (double)sum / (double)termChain.size();
    }
    public double getAverageTermScore(){
        return avgTermScore;
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
    
    
    public EQFormat getFormat(){
        return format;
    }
    
    public String[] getComponentStrings(){
        return componentStrings;
    }
    
    /**
     * This is the representation that is used for both calculating overlap between
     * EQ statements and calculating Information Content of particular EQ statements 
     * in the corpus.
     * @return 
     */
    public String getStandardizedRepresentation(){
        return toIDText();
    }
    
    public boolean isFromCuratedDataSet(){
        return fromCuratedDataSet;
    }
    
    
    
    
 
}
