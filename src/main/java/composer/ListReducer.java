
package composer;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import enums.EQFormat;
import enums.Ontology;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import nlp.MyAnnotation;
import nlp.MyAnnotation.Token;
import nlp.CoreNLP;
import objects.Chunk;
import utils.Comparators.TermComparatorByLabelLength;
import utils.Comparators.TermComparatorByScore;


public class ListReducer {
    
    
    
    
    
    
    
    
    /**
     * Obtain the annotations available from running the Stanford CoreNLP pipeline on this 
     * text chunk, which includes the parts of speech of each of the tokens in the chunk and 
     * the dependency graph of edges connecting these tokens. Default parameters are used.
     * Assumes there is only one sentence or fragment in the chunk, uses the first if the 
     * parser finds multiple. This method should only receive as input chunks thats are related 
     * to phenes or phenotypes that have already been split into multiple atomized statements 
     * so that only one sentence is retrieved from Stanford pipeline. TODO add a check to see
     * how many times this parse is falling to retrieve less than two sentences.
     * @param chunk
     * @return 
     */
    public static MyAnnotation getAnnotation(Chunk chunk){
        String text = chunk.getRawText();
        CoreDocument doc = new CoreDocument(text);
        CoreNLP.getPipeline().annotate(doc);
        return new MyAnnotation(doc.sentences().get(0));
    }
    
    


    
    
    
    
    
    
    /**
     * Unused, not working as intended. Goal was to detect when the subject is only implied using 
     * the NLP information rather than just a lack of finding other entities. That might not be 
     * the right approach because it's much more difficult that just seeing what else was found
     * during annotation and haven't found a reliable way to get that information out of the dG.
     * TODO figure out which edge types (if any) reliably imply the presence of a subject (enables
     * it to be a complete sentence as well).
     * @param annot
     * @return 
     */
    public static boolean hasImpliedSubject(MyAnnotation annot){
        for (SemanticGraphEdge edge: annot.dependencyGraph.edgeListSorted()){
            if (edge.getRelation().getShortName().equals("nsubj")){
                return false;
            }
        }     
        return true;     
    }
    
    
    
    
    
    
    
    /**
     * Finds the EQ statements that are not using the term specified as the default primary
     * entity. For the plant dataset currently used in this work that is whole plant which
     * is a term in PO. A sublist of the passed in list of EQ statements is returned, which
     * only includes the elements which match this criteria so that they can be removed.
     * @param eqs
     * @return 
     */
    public static List<EQStatement> getNonImpliedSubjEQs(List<EQStatement> eqs){
        List<EQStatement> toDelete = new ArrayList<>();
        for (EQStatement eq: eqs){
            if (EQFormat.hasComplexPrimaryEntity(eq.getFormat())){
                toDelete.add(eq);
            }
            else if (!eq.primaryEntity1.id.equals("PO_0000003")){
                toDelete.add(eq);
            }
        }
        return toDelete;
    }
    
    
   
    
    
    
    
    /**
     * Finds the EQ statements that are not using an optional qualifier term. These terms
     * are specified in the class containing additional hard coded information about which
     * terms in PATO are treated with which rules. A sublist of the passed in list of EQ 
     * statements is returned, which only includes the elements which match this criteria 
     * so that they can be removed.
     * @param eqs
     * @return 
     */
    public static List<EQStatement> getNonQualifierEQs(List<EQStatement> eqs){
        List<EQStatement> toDelete = new ArrayList<>();
        for (EQStatement eq: eqs){
            if (!EQFormat.hasOptionalQualifier(eq.getFormat())){
                toDelete.add(eq);
            }
        }
        return toDelete;
    }
    
    




    
    
    /**
     * Not currently used. The probabilities of the minimal path lengths between terms in a 
     * complex entity as well as those used within the context of relational entity are instead
     * used to rank EQ statements rather than directly removing them from the list of viable 
     * EQ statements.
     * @param eqs
     * @param annot
     * @return 
     */
    public static List<EQStatement> getInvalidComplexEQs(List<EQStatement> eqs, MyAnnotation annot){
        List<EQStatement> toDelete = new ArrayList<>();
        for (EQStatement eq: eqs){
            if (EQFormat.hasComplexPrimaryEntity(eq.getFormat())){
                if (!checkDependency(eq.primaryEntity1, eq.primaryEntity2, annot)){
                    toDelete.add(eq);
                }
            }
            if (EQFormat.hasComplexSecondaryEntity(eq.getFormat())){
                if (checkDependency(eq.secondaryEntity1, eq.secondaryEntity2, annot)){
                    toDelete.add(eq);
                }
            }  
        }
        return toDelete;
    }
    
    
    
    
    
    
    
    
    /**
     * Not currently used. The probabilities of the minimal path lengths between terms in a 
     * complex entity as well as those used within the context of relational entity are instead
     * used to rank EQ statements rather than directly removing them from the list of viable 
     * EQ statements.
     * @param eqs
     * @param annot
     * @return 
     */
    public static List<EQStatement> getInvalidRelationalEQs(List<EQStatement> eqs, MyAnnotation annot){
        List<EQStatement> toDelete = new ArrayList<>();
        for (EQStatement eq: eqs){
            if (EQFormat.hasComplexPrimaryEntity(eq.getFormat())){
                if (!checkDependency(eq.primaryEntity1, eq.primaryEntity2, annot)){
                    toDelete.add(eq);
                }
            }
            if (EQFormat.hasComplexSecondaryEntity(eq.getFormat())){
                if (checkDependency(eq.secondaryEntity1, eq.secondaryEntity2, annot)){
                    toDelete.add(eq);
                }
            }  
        }
        return toDelete;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * Looks at the minimal undirected path lengths between the nodes mapping
     * to the two input terms. This method is specific to looking for a path
     * length of one between the relevant nodes and just returns false when this
     * is not the case. This is for cases where the path length of one is an 
     * enforced requirement such as between two terms in the same entity.
     * @param t1
     * @param t2
     * @param annot
     * @return true when there is directed path length of 1 from t1 to t2, false when anything else.
     */
    private static boolean checkDependency(Term t1, Term t2, MyAnnotation annot){
        int arbitrarilyLargePathLength = 100;
        List<IndexedWord> nodesTerm1 = new ArrayList<>();
        List<IndexedWord> nodesTerm2 = new ArrayList<>();
        SemanticGraph dG = annot.dependencyGraph;
        for (Token token: annot.tokens){
            if (t1.nodes.contains(token.nodeText)){
                nodesTerm1.add(token.idxWord);
            }
            if (t2.nodes.contains(token.nodeText)){
                nodesTerm2.add(token.idxWord);
            }
        }
        int minPathLength = arbitrarilyLargePathLength;
        boolean correctDirection = false;
        for (IndexedWord w1: nodesTerm1){
            for (IndexedWord w2: nodesTerm2){
                try{
                    List<IndexedWord> path = dG.getShortestUndirectedPathNodes(w1,w2);
                    int pathLength = path.size()-1;
                    
                    if (pathLength < minPathLength){
                        minPathLength = pathLength;
                        try {
                            List<SemanticGraphEdge> edges = dG.getShortestDirectedPathEdges(w1,w2);
                            correctDirection = true;
                        }
                        catch (Exception e){
                            correctDirection = false;
                        }
                    }
                }
                catch(Exception e){    
                }
            }
        }
        if (minPathLength == 1){
            return correctDirection;
        }
        return false;
    }

    
    
    /**
     * Find the minimal path length between any tokens in the dependency graph which
     * match nodes from the first set and any that match nodes in the second set. The
     * length obtained can then be used to find the probability of observing this path
     * length in a high quality EQ statement as estimated from the training data.
     * @param set1
     * @param set2
     * @param annot
     * @return 
     */
    public static int getMinPathLength(HashSet<String> set1, HashSet<String> set2, MyAnnotation annot){
        int arbitrarilyLargePathLength = 100;
        set1 = CoreNLP.removeStopWords(set1);
        set2 = CoreNLP.removeStopWords(set2);
        List<IndexedWord> nodesTerm1 = new ArrayList<>();
        List<IndexedWord> nodesTerm2 = new ArrayList<>();
        SemanticGraph dG = annot.dependencyGraph;
        for (Token token: annot.tokens){
            if (set1.contains(token.nodeText)){
                nodesTerm1.add(token.idxWord);
            }
            if (set2.contains(token.nodeText)){
                nodesTerm2.add(token.idxWord);
            }
        }
        int minPathLength = arbitrarilyLargePathLength;
        for (IndexedWord w1: nodesTerm1){
            for (IndexedWord w2: nodesTerm2){
                try{
                    List<IndexedWord> path = dG.getShortestUndirectedPathNodes(w1,w2);
                    int pathLength = path.size()-1;
                    if (pathLength < minPathLength){
                        minPathLength = pathLength;
                    }
                }
                catch(Exception e){    
                }
            }
        }
        return minPathLength;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * Returns terms from a list of terms that share nodes (tokens) with the target
     * term. This counts all the nodes that are reported to be aligned with the 
     * provided terms. Note that this should probably be changed to disregard either
     * stop words or tokens that appear more than once in this particular piece of 
     * text.
     * @param target
     * @param other
     * @return 
     */
    private static List<Term> getOverlappingTerms(Term target, List<Term> other){
        List<Term> overlappingTerms = new ArrayList<>();
        for (Term e: other){
            HashSet intersection = new HashSet<>(target.nodes);
            intersection.retainAll(e.nodes);
            if (!intersection.isEmpty()){
                overlappingTerms.add(e);
            }
        }
        return overlappingTerms;
    }
    
    
    
    
    

    /**
     * Return entity terms that overlap with another entity term but are ranked lower.
     * @param terms
     * @return 
     */
    public static List<Term> findRedundantEntities(List<Term> terms){
        HashSet<Term> checked = new HashSet<>();
        List<Term> toDelete = new ArrayList<>();
        for (Term target: terms){
            if (!checked.contains(target)){
                List<Term> copyOther = new ArrayList<>(terms);
                copyOther.remove(target);
                List<Term> conflicts = getConflictsOrderedByOntologyAndScore(target,copyOther);
                toDelete.addAll(conflicts.subList(1,conflicts.size()));
                checked.addAll(conflicts);
            }
        }
        return toDelete;
    }

    
    
    
    
    
    
    
    private static List<Term> getConflictsOrderedByOntologyAndScore(Term target, List<Term> other){
        // Figure out which terms are overlapping so that conflicts can be resolved.
        List<Term> conflicts = getOverlappingTerms(target,other);
        conflicts.add(target);
        List<Term> conflictsSorted = new ArrayList<>();
        // Define default order for how ontology terms should be retained.
        HashMap<Integer,Ontology> ontoOrder = new HashMap<>();
        ontoOrder.put(1,Ontology.PO);
        ontoOrder.put(2,Ontology.UBERON);
        ontoOrder.put(3,Ontology.GO);
        ontoOrder.put(4,Ontology.CHEBI);
        // Do the sorting based on those preferences.
        for (int order=1; order<=ontoOrder.keySet().size(); order++){
            List<Term> fromThisO = new ArrayList<>();
            for(Term t: conflicts){
                if (utils.Utils.inferOntology(t.id).equals(ontoOrder.get(order))){
                    fromThisO.add(t);
                }
            }
            Collections.sort(fromThisO, new TermComparatorByScore());
            conflictsSorted.addAll(fromThisO);
        }        
        return conflictsSorted;
    }
        
    
    
    
    
    
    /**
     * Return the quality terms that overlap with another quality term in the list.
     * @param terms
     * @return 
     */
    public static List<Term> findRedundantQualities(List<Term> terms){
        HashSet<Term> checked = new HashSet<>();
        List<Term> toDelete = new ArrayList<>();
        for (Term target: terms){
            if (!checked.contains(target)){
                List<Term> copyOther = new ArrayList<>(terms);
                copyOther.remove(target);
                toDelete.addAll(getTermsToDeleteByLength(target,copyOther));
                checked.add(target);
                checked.addAll(toDelete);
            }
        }
        return toDelete;
    }
    private static List<Term> getTermsToDeleteByLength(Term target, List<Term> other){
        // Get the set of terms that is considered to be representing the same meaning as the target.
        List<Term> conflicts = getOverlappingTerms(target, other);
        conflicts.add(target);
        // Get the terms which had the minimal label length in that set.
        Collections.sort(conflicts, new TermComparatorByLabelLength());
        return conflicts.subList(1, conflicts.size());
    }
    
    
    
    
    
    
    
    
    
    
    /**
     * Return EQs where an entity term is overlapping with either the quality term or 
     * a term being used as an optional qualifier.
     * @param eqs
     * @return 
     */
    public static List<EQStatement> getRedundantEQs(List<EQStatement> eqs){
        List<EQStatement> toDelete = new ArrayList<>();
        for (EQStatement eq: eqs){
            if (EQFormat.hasOptionalQualifier(eq.getFormat())){
                Term q = eq.quality;
                Term ql = eq.qualifier;
                List<Term> other = new ArrayList<>(eq.termChain);
                other.remove(q);
                other.remove(ql);
                if (!getOverlappingTerms(q,other).isEmpty() || !getOverlappingTerms(ql,other).isEmpty()){
                    toDelete.add(eq);
                }
            }
            else {
                Term q = eq.quality;
                List<Term> other = new ArrayList<>(eq.termChain);
                other.remove(q);
                if (!getOverlappingTerms(q,other).isEmpty()){
                    toDelete.add(eq);
                }
            }
        }
        return toDelete;
    }
    
    
    
    
   
    
  
    
}
