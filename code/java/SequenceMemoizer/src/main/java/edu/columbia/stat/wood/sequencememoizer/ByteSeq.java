/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer;

import edu.columbia.stat.wood.util.MutableInt;
import java.util.HashSet;

/**
 *
 * @author nicholasbartlett
 */

public class ByteSeq {

    private int nodeSize, index, length;
    private ByteSeqNode first, last;

    public ByteSeq(int nodeSize) {
        this.nodeSize = nodeSize;

        first = new ByteSeqNode(null, null);
        last = first;
        index = nodeSize - 1;
        length = 0;
    }

    public int blockSize() {
        return nodeSize;
    }

    public void append(byte b) {

        if (index < 0) {
            last.next = new ByteSeqNode(last, null);
            last = last.next;
            index = nodeSize - 1;
        }

        length++;
        last.byteChunk[index--] = b;
    }

    public int length(){
        return length;
    }

    public void shorten() {
        for(ByteRestaurant r : first){
            r.removeFromTree();
        }

        length -= nodeSize;
        first = first.next;
        first.previous = null;
    }

    public BackwardsIterator backwardsIterator() {
        return new BackwardsIterator();
    }

    public int restaurantCount(){
        int nodes = 0;
        ByteSeqNode bsn = last;
        while(bsn != null){
            nodes++;
            bsn = bsn.previous;
        }

        bsn = last;
        int restaurantCount = 0;
        while(bsn != null){
            restaurantCount += bsn.size();

            System.out.print(nodes-- + ", ");
            System.out.println(bsn.size());

            bsn = bsn.previous;
        }

        return restaurantCount;
    }

    public int overlap(ByteSeqNode edgeNode, int edgeIndex, int edgeLength, byte[] context, int index){
        int overlap = 0;
        while(edgeNode != null && overlap < edgeLength && index > -1 && edgeNode.byteChunk[edgeIndex] == context[index]){
            overlap++;
            index--;
            edgeIndex++;

            if(edgeIndex >= nodeSize){
                edgeNode = edgeNode.previous;
                if(edgeNode == null){
                    break;
                }
                edgeIndex = 0;
            }
        }
        
        return overlap;
    }

    public class ByteSeqNode extends HashSet<ByteRestaurant>{

        private byte[] byteChunk;
        private ByteSeqNode previous, next ;

        public ByteSeqNode(ByteSeqNode previous, ByteSeqNode next) {
            this.previous = previous;
            this.next = next;

            byteChunk = new byte[nodeSize];
        }

        public ByteSeqNode previous() {
            return previous;
        }

        public byte[] byteChunk(){
            return byteChunk;
        }

        /*
        @Override
        public final boolean add(ByteRestaurant r){
            return true;
        }

        @Override
        public final boolean remove(Object r){
            return true;
        }

        @Override
        public final boolean contains(Object o){
            return true;
        }*/
    }

    public class BackwardsIterator {

        public ByteSeqNode node;
        public int ind;

        public BackwardsIterator() {
            node = last;
            ind = index + 1;
        }

        public byte peek() {
            if (ind >= nodeSize) {
                node = node.previous;
                ind = 0;
            }
            return node.byteChunk[ind];
        }

        public byte next() {
            if (ind >= nodeSize) {
                node = node.previous;
                ind = 0;
            }
            return node.byteChunk[ind++];
        }

        public boolean hasNext() {
            if (ind >= nodeSize) {
                node = node.previous;
                ind = 0;
            }

            return node != null;
        }

        public int overlap(ByteSeqNode edgeNode, int edgeIndex, int edgeLength, MutableInt newKey) {
            ByteSeqNode ln;
            int overlap, li;

            ln = edgeNode;
            li = edgeIndex;

            overlap = 0;
            while (ln.byteChunk[li] == node.byteChunk[ind] && overlap < edgeLength) {
                li++;
                ind++;
                overlap++;

                if (li >= nodeSize) {
                    ln = ln.previous;
                    if (ln == null) {
                        break;
                    }
                    li = 0;
                }

                if (ind >= nodeSize) {
                    node = node.previous;
                    ind = 0;
                }
            }

            if (ln != null) {
                newKey.set((int) ln.byteChunk[li] & 0xFF);
            } else {
                newKey.set(-1);
            }

            return overlap;
        }

        public int available(int l) {
            int available = nodeSize - ind;

            if (available > l) {
                return l;
            } else {
                ByteSeqNode n = node.previous;
                while (n != null) {
                    available += nodeSize;
                    if (available > l) {
                        return l;
                    }
                    n = n.previous;
                }

                return available;
            }
        }
    }
}
