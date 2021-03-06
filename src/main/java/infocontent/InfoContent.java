

package infocontent;

import enums.Ontology;
import enums.TextDatatype;
import java.sql.SQLException;
import java.util.HashMap;
import ontology.Onto;
import text.Text;
import uk.ac.ebi.brain.error.ClassExpressionException;


public class InfoContent {
    
    
    
    private static CorpusWordIC cwic;
    private static CorpusTermIC ctic;    
    private static CorpusEQIC ceqic;
    private static HashMap<Ontology,OntologyWordIC> owicMap;
    private static HashMap<Ontology,OntologyTermIC> oticMap;
    private static HashMap<Ontology,Onto> ontoObjects;

    
    
    
    public static void setup(HashMap<Ontology,Onto> ontoObjects, Text text) throws SQLException, ClassExpressionException, Exception{
        InfoContent.ontoObjects = ontoObjects;
        cwic = new CorpusWordIC(text, TextDatatype.PHENE);
        ctic = new CorpusTermIC(ontoObjects);
        ceqic = new CorpusEQIC(text, ontoObjects);
        owicMap = new HashMap<>();
        oticMap = new HashMap<>();
        for (Ontology ontology: ontoObjects.keySet()){
            owicMap.put(ontology, new OntologyWordIC(ontoObjects.get(ontology).getTermList()));
            oticMap.put(ontology, new OntologyTermIC(ontoObjects.get(ontology).getTermList(), ontoObjects.get(ontology).getBrain()));
        }
    }
    
    
    public static double getICofWordInCorpus(String word){
        return cwic.getIC(word);
    }
    
    public static double getICofTermFromCorpus(String termID){
        return ctic.getIC(termID);
    }
    
    public static double getICofTermInOntology(Ontology o, String termID){
        return oticMap.get(o).getIC(termID);
    }
    
    
    public static double getICofWordInOntology(Ontology o, String word){
        return owicMap.get(o).getIC(word);
    }
    
    public static double getICofEQInCorpus(String eqStr) throws Exception{
        return ceqic.getIC(eqStr);
    }
    
    
    
    
    
    
    
    public static int getLabelLength(String termID){
        return ontoObjects.get(utils.Utils.inferOntology(termID)).getTermFromTermID(termID).label.length();
    }
    
    
    
    
    
    
}
