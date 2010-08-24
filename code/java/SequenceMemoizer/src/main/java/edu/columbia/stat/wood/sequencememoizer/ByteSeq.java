/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.sequencememoizer;

/**
 *
 * @author nicholasbartlett
 */
public class ByteSeq {
    private int nodeSize, index;
    private ByteSeqNode first, last;

    public ByteSeq(int nodeSize){
        this.nodeSize = nodeSize;
        
        first = new ByteSeqNode(null, null);
        last = first;
        index = nodeSize - 1;
    }

    public void append(byte b){
        if(index < 0){
            last.next = new ByteSeqNode(last, null);
            last = last.next;
            index = nodeSize - 1;
        }

        last.byteChunk[index--] = b;
    }

    public void shorten(){
        throw new UnsupportedOperationException("Not supported yet.");
        //first = first.next;
        //first.previous = null;
    }

    public BackwardsIterator backwardsIterator(){
        return new BackwardsIterator();
    }

    public class ByteSeqNode {
        private byte[] byteChunk;
        private ByteSeqNode previous, next;

        public ByteSeqNode(ByteSeqNode previous, ByteSeqNode next){
            this.previous = previous;
            this.next = next;
            
            byteChunk = new byte[nodeSize];
        }
    }

    public class BackwardsIterator{
        private ByteSeqNode node;
        private int ind;

        public BackwardsIterator(){
            node = last;
            ind = index + 1;
        }

        public byte peek(){
            if(ind >= nodeSize){
                node = node.previous;
                ind = 0;
            }
            return node.byteChunk[ind];
        }
        
        public byte next(){
            if(ind >= nodeSize){
                node = node.previous;
                ind = 0;
            }
            return node.byteChunk[ind++];
        }

        public boolean hasNext(){
            if(ind >= nodeSize){
                node = node.previous;
                ind = 0;
            }

            return node != null;
        }

        public int overlap(ByteSeqNode edgeNode, int edgeIndex, int edgeLength){
            ByteSeqNode ln;
            int overlap, li;

            ln = edgeNode;
            li = edgeIndex;

            overlap = 0;
            while(ln.byteChunk[li] == node.byteChunk[ind] && overlap < edgeLength){
                li++;
                ind++;
                overlap++;

                if(li >= nodeSize){
                    ln = ln.previous;
                    li = 0;
                    if(ln == null){
                        break;
                    }
                }

                if(ind >= nodeSize){
                    node = node.previous;
                    ind = 0;
                }
            }

            return overlap;
        }
    }

    public static void main(String[] args){
        ByteSeq bs = new ByteSeq(1024);

        for(int i = 0; i < 1050; i++){
            bs.append((byte) i);
        }

        BackwardsIterator bi = bs.backwardsIterator();

        System.out.println(bi.overlap(bs.first, 998, 1000));

        while(bi.hasNext()){
            System.out.println(bi.next());
        }

    }
}
