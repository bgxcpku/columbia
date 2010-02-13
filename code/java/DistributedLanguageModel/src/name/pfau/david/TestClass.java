package name.pfau.david;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author davidpfau
 */
public class TestClass {

    public void changeArray(int[] in) {
        in[0]  = 1;
    }
    
    public TestClass() {
        
    }
    
    public static void main(String[] args) {
        /*int[] words = {1,2,4,6,5,4,4,0,3,0,3,3,1,1,7};
        ArrayList<Integer> wordlist = new ArrayList<Integer>(15);
        for(int i : words) {
            wordlist.add(i);
        }
        Document doc = new Document(wordlist,2,4);
        
        for(Observation o : doc.getObservations()) {
            System.out.println(o.toString());
        }*/
        
        int[] test = new int[10];
        TestClass tc = new TestClass();
        System.out.println(test[0]);
        tc.changeArray(test);
        System.out.println(test[0]);
    }
}
