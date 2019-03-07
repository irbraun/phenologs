/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */
package composer;

import composer.Utils.TermComparatorByIC;
import composer.Utils.TermComparatorByLabelLength;
import composer.Utils.TermComparatorByProb;
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
import static main.Main.logger;
import nlp.MyAnnotation;
import nlp.MyAnnotation.Token;
import nlp.CoreNLP;
import structure.Chunk;

/**
 *
 * @author irbraun
 */
public class Modifier {
    
    

    
    // Assumes there is only one sentence or fragment in the chunk, uses the first if the parser finds multiple.
    public static MyAnnotation getAnnotation(Chunk chunk){
        String text = chunk.getRawText();
        CoreDocument doc = new CoreDocument(text);
        CoreNLP.getPipeline().annotate(doc);
        return new MyAnnotation(doc.sentences().get(0));
        
        
        
        
        
    }
    
    
    
    // TODO fix this method, not reliable. 
    // Goal is to detect when the subject is only implied using the NLP information rather
    // than just a lack of finding other entities. That might not be the right approach.
    // Maybe it's hard to do better than just checking to see if you can find anything else.
    // That could run into problems when the Q is relational but the primary entity is implied.
    // TODO add the other edges which imply the presence of a subject (complete sentence).
    public static boolean hasImpliedSubject(MyAnnotation annot){
        for (SemanticGraphEdge edge: annot.dependencyGraph.edgeListSorted()){
            if (edge.getRelation().getShortName().equals("nsubj")){
                return false;
            }
        }     
        return true;     
    }
    
    
    // Find the EQ statements that don't use the implied subject term as the single primary entity.
    public static List<EQStatement> getNonImpliedSubjEQs(List<EQStatement> eqs){
        List<EQStatement> toDelete = new ArrayList<>();
        for (EQStatement eq: eqs){
            if (EQFormat.hasComplexPrimaryEntity(eq.format)){
                toDelete.add(eq);
            }
            else if (!eq.primaryEntity1.id.equals("PO_0000003")){
                toDelete.add(eq);
            }
        }
        return toDelete;
    }
    
    
    
    
    
    
    /**
     * Return EQs that don't use the optional qualifier term.
     * @param eqs
     * @return 
     */
    public static List<EQStatement> getNonQualifierEQs(List<EQStatement> eqs){
        List<EQStatement> toDelete = new ArrayList<>();
        for (EQStatement eq: eqs){
            if (!EQFormat.hasOptionalQualifier(eq.format)){
                toDelete.add(eq);
            }
        }
        return toDelete;
    }
    
    
    
    
    
    
    
   
    public static List<EQStatement> getInvalidComplexEQs(List<EQStatement> eqs, MyAnnotation annot){
        List<EQStatement> toDelete = new ArrayList<>();
        for (EQStatement eq: eqs){
            if (EQFormat.hasComplexPrimaryEntity(eq.format)){
                if (!checkDependency(eq.primaryEntity1, eq.primaryEntity2, annot)){
                    toDelete.add(eq);
                }
            }
            if (EQFormat.hasComplexSecondaryEntity(eq.format)){
                if (checkDependency(eq.secondaryEntity1, eq.secondaryEntity2, annot)){
                    toDelete.add(eq);
                }
            }  
        }
        return toDelete;
    }
    
    
    
   
    
    
    
    private static boolean checkDependency(Term t1, Term t2, MyAnnotation annot){
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
        int minPathLength = 100;
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

    
    
    public static int getMinPathLength(HashSet<String> set1, HashSet<String> set2, MyAnnotation annot){
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
        int minPathLength = 100;
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
     * Return entity terms that overlap with another entity term in the list but
     * have lesser information content within the structure of their ontology.
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
                List<Term> conflicts = getConflictsOrderedByOntoThenProb(target,copyOther);
                toDelete.addAll(conflicts.subList(1,conflicts.size()));
                checked.addAll(conflicts);
            }
        }
        return toDelete;
    }
    private static List<Term> getConflictsOrderedByIC(Term target, List<Term> other){
        // Get the set of terms that is considered to be representing the same meaning as the target.
        List<Term> conflicts = getOverlappingTerms(target, other);
        conflicts.add(target);
        // Get the terms that had the lowest information content from the group.
        Collections.sort(conflicts, new TermComparatorByIC());
        
        return conflicts;
        //return conflicts.subList(1, conflicts.size());
    }
    private static List<Term> getConflictsOrderedByOntoThenProb(Term target, List<Term> other){
        
        
        
        List<Term> conflicts = getOverlappingTerms(target,other);
        conflicts.add(target);
        List<Term> conflictsSorted = new ArrayList<>();
        
        logger.info(String.format("conflict between %s terms", conflicts.size()));
        for (Term t: conflicts){
            System.out.println(t.id);
        }
        
        HashMap<Integer,Ontology> ontoOrder = new HashMap<>();
        ontoOrder.put(1,Ontology.PO);
        ontoOrder.put(2,Ontology.UBERON);
        ontoOrder.put(3,Ontology.GO);
        ontoOrder.put(4,Ontology.CHEBI);
        
        for (int order=1; order<=ontoOrder.keySet().size(); order++){
            List<Term> fromThisO = new ArrayList<>();
            for(Term t: conflicts){
                if (utils.Util.inferOntology(t.id).equals(ontoOrder.get(order))){
                    fromThisO.add(t);
                }
            }
            Collections.sort(fromThisO, new TermComparatorByProb());
            conflictsSorted.addAll(fromThisO);
        }
        
        
        logger.info(String.format("conflicts sorted has %s terms", conflictsSorted.size()));
        for (Term t: conflictsSorted){
            System.out.println(t.id);
        }
        
        
        
        
        return conflictsSorted;
    }
        
    
    
    
    
    
    // the comparison by label length should be replaced by a comparison of probabilty ,which takes into
    // account partial match characeteristics
    
    
    
    
    
    
    
    
    /**
     * Return the quality terms that overlap with another quality term in the list
     * but have short label strings.
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
        // Get the terms that had the least label length in that group.
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
            if (EQFormat.hasOptionalQualifier(eq.format)){
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
