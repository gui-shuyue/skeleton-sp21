package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> L2 = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 3);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                L2.addLast(randVal);

            } else if (L.size() == 0){
                continue;
            } else if (operationNumber == 1) {

                int last = L.getLast();
                int last2 = L2.getLast();
                assertEquals(last, last2);
            } else if (operationNumber == 2) {

                int r1 = L.removeLast();
                int r2 = L2.removeLast();
                assertEquals(r1, r2);
            }

        }
    }
}



