/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */

public class MemoizedSequence {

    private int[][] sequence;
    private int currentSequence = 0;
    private int currentLength = 0;

    public MemoizedSequence(int initialSize){
        sequence = new int[1][initialSize];
    }

    public MemoizedSequence(){
        this(100000);
    }

    public int get(int seqIndex, int index){
        return sequence[seqIndex][index];
    }

    public int get(int index){
        return this.get(currentSequence, index);
    }

    public int getCurrentSeq(){
        return currentSequence;
    }

    public void incrementSeq(){
        int[][] newSequence = new int[sequence.length + 1][];
        System.arraycopy(sequence, 0, newSequence, 0, sequence.length);
        newSequence[sequence.length] = new int[100000];
        sequence = newSequence;
        currentLength = 0;
        currentSequence++;
    }

    public void deleteSeq(){
        int[][] newSequence = new int[sequence.length - 1][];
        System.arraycopy(sequence, 0, newSequence, 0, sequence.length-1);
        sequence = newSequence;
        currentSequence--;
    }

    public void add(int[] newSeq){
        for(int i = 0; i<newSeq.length; i++){
            this.add(newSeq[i]);
        }
    }

    public void add(int newObs) {
        //if possible, just tack on most recent obs
        if (currentLength < sequence[currentSequence].length) {
            sequence[currentSequence][currentLength++] = newObs;
        } //else will need to make array longer for this sequence index
        else {
            int[] newThisSeq = new int[sequence[currentSequence].length + 100000];
            System.arraycopy(sequence[currentSequence], 0, newThisSeq, 0, currentLength);
            sequence[currentSequence] = newThisSeq;
            sequence[currentSequence][currentLength++] = newObs;
        }
    }

    public int getLastElementIndex(){
        return currentLength - 1;
    }

    public int compareContexts(int[] parentPath, int ind){
        //walk through the parent path backwards, comparing it to the sequence
        //from l, going backwards.

        int parentSeq = parentPath[2];

        int maxDistanceWalk = ((ind+1)<(parentPath[1] - parentPath[0]))?(ind+1):(parentPath[1] - parentPath[0]);

        for (int j = 0; j < maxDistanceWalk; j++) {
            if (sequence[currentSequence][ind - j] != sequence[parentSeq][parentPath[1] - j - 1]){

                if(j <= 0){
                    System.out.println("INSIDE HERE");
                    System.out.println(sequence[currentSequence][ind - j] + "current seq");
                    System.out.println(sequence[parentSeq][parentPath[1] - j -1] + "old seq");
                }

                return j;
            }
        }

        return maxDistanceWalk;
    }            
}
