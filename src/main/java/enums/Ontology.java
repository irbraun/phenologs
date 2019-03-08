
package enums;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public enum Ontology implements Serializable {
    PATO,
    PO,
    GO,
    CHEBI,
    UBERON,
    UNSUPPORTED;
    
    public static List<Ontology> getPlantOntologies(){
        return new ArrayList<>(EnumSet.of(PATO, PO));
    }
    
    public static List<Ontology> getVertebrateOntologies(){
        return new ArrayList<>(EnumSet.of(PATO, UBERON));
    }
    
    public static List<Ontology> getAllOntologies(){
        return new ArrayList<>(EnumSet.of(PATO, PO, GO, CHEBI, UBERON));
    }
    
    public static List<Ontology> getSmallOntologies(){
        return new ArrayList<>(EnumSet.of(PATO, PO, GO));
    }
    
}
