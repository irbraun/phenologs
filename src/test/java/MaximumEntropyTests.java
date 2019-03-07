/*
 * Ian Braun
 * irbraun@iastate.edu
 * term-mapping 
 */

import java.sql.SQLException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author irbraun
 */
public class MaximumEntropyTests {
    
    public MaximumEntropyTests() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    
    @Test
    public void shortRoutineTest() throws SQLException, Exception{
        /*
        Text text = new Text();
        List<Integer> r = IntStream.rangeClosed(55, 1275).boxed().collect(Collectors.toList());
        List<Chunk> chunks = text.getAtomChunksFromIDs(r);
        
        int maxIter = 200;
        MaxEntClassifier mec = new MaxEntClassifier(maxIter);
        mec.train(text, chunks);
        
        Chunk c = text.getAtomChunkFromID(55);
        String t1 = text.getCuratedEQStatementFromAtomID(55).getAllTermIDs().get(0);
        String t2 = text.getCuratedEQStatementFromAtomID(56).getAllTermIDs().get(0);
        System.out.println(mec.getProb(c.getBagValues(), t1));
        System.out.println(mec.getProb(c.getBagValues(), t2));
        System.out.println("-----------");
        
        
        c = text.getAtomChunkFromID(110);
        t1 = text.getCuratedEQStatementFromAtomID(110).getAllTermIDs().get(0);
        t2 = text.getCuratedEQStatementFromAtomID(111).getAllTermIDs().get(0);
        System.out.println(mec.getProb(c.getBagValues(), t1));
        System.out.println(mec.getProb(c.getBagValues(), t2));
        System.out.println("-----------");
        
        
        c = text.getAtomChunkFromID(210);
        t1 = text.getCuratedEQStatementFromAtomID(210).getAllTermIDs().get(0);
        t2 = text.getCuratedEQStatementFromAtomID(211).getAllTermIDs().get(0);
        System.out.println(mec.getProb(c.getBagValues(), t1));
        System.out.println(mec.getProb(c.getBagValues(), t2));
        System.out.println("-----------");
        */
    }
    
    
}
