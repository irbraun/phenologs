
package composer;

import static composer.Utils.normalizeTermID;
import enums.Ontology;
import enums.Role;
import java.util.HashSet;


public class Term {
    public String id;
    public double probability;
    public Ontology ontology;
    public Role role;
    public HashSet<String> nodes;
    
    
    // Allows for creating a term with a role within the context of an EQ statement.
    // Used by the constructors for the EQStatement class.
    public Term(Term singleTerm, Role role){
        this.id = normalizeTermID(singleTerm.id);
        this.probability = singleTerm.probability;
        this.ontology = singleTerm.ontology;
        this.nodes = singleTerm.nodes;
        this.role = role;
    }
    
    public Term(String id, double probability, Ontology ontology){
        this.id = normalizeTermID(id);
        this.probability = probability;
        this.ontology = ontology;
    }
    public Term(String id, double probability, String ontology){
        this.id = normalizeTermID(id);
        this.probability = probability;
        this.ontology = Ontology.valueOf(ontology);   
    }
    
    
    
    public Term(String id, Ontology ontology)  {
        this.id = normalizeTermID(id);
        this.ontology = ontology;
    }
    
    public Term(String id, String ontology)  {
        this.id = normalizeTermID(id);
        this.ontology = Ontology.valueOf(ontology);
    }
    
    
    
    public Term(String id, double probability, Ontology ontology, Role role)  {
        this.id = normalizeTermID(id);
        this.probability = probability;
        this.ontology = ontology;
        this.role = role;
    }
    public Term(String id, double probability, String ontology, Role role)  {
        this.id = normalizeTermID(id);
        this.probability = probability;
        this.ontology = Ontology.valueOf(ontology);   
        this.role = role;
    }
    
    
    
    public Term(String id, Ontology ontology, Role role)  {
        this.id = normalizeTermID(id);
        this.ontology = ontology;
        this.role = role;
    }
    
    public Term(String id, String ontology, Role role)  {
        this.id = normalizeTermID(id);
        this.ontology = Ontology.valueOf(ontology);
        this.role = role;
    }
    
    
    
    
    
    public Term(String id, double probability, Ontology ontology, HashSet<String> nodes)  {
        this.id = normalizeTermID(id);
        this.probability = probability;
        this.ontology = ontology;
        this.nodes = nodes;
    }
    
    
    
}
