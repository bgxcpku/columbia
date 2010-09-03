/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer.v1;

import edu.columbia.stat.wood.util.MutableInt;
import gnu.trove.set.hash.THashSet;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author nicholasbartlett
 */
public class ByteSeq implements Serializable {

    static final long serialVersionUID = 1;
    
    private int nodeSize, index, length;
    private ByteSeqNode first, last;

    public ByteSeq(int nodeSize) {
        this.nodeSize = nodeSize;

        first = new ByteSeqNode(null, null, nodeSize);
        last = first;
        index = nodeSize - 1;
        length = 0;
    }

    public int blockSize() {
        return nodeSize;
    }

    public void append(byte b) {

        if (index < 0) {
            last.next = new ByteSeqNode(last, null, nodeSize);
            last = last.next;
            index = nodeSize - 1;
        }

        length++;
        last.byteChunk[index--] = b;
    }

    public int length() {
        return length;
    }

    public void shorten() {
        for (ByteRestaurant r : first) {
            r.removeFromTree();
        }

        length -= nodeSize;
        first = first.next;
        first.previous = null;
    }

    public BackwardsIterator backwardsIterator() {
        return new BackwardsIterator();
    }

    public int restaurantCount() {
        int nodes = 0;
        ByteSeqNode bsn = last;
        while (bsn != null) {
            nodes++;
            bsn = bsn.previous;
        }

        bsn = last;
        int restaurantCount = 0;
        while (bsn != null) {
            restaurantCount += bsn.size();

            System.out.print(nodes-- + ", ");
            System.out.println(bsn.size());

            bsn = bsn.previous;
        }

        return restaurantCount;
    }

    public int overlap(ByteSeqNode edgeNode, int edgeIndex, int edgeLength, byte[] context, int index) {
        int overlap = 0;
        while (edgeNode != null && overlap < edgeLength && index > -1 && edgeNode.byteChunk[edgeIndex] == context[index]) {
            overlap++;
            index--;
            edgeIndex++;

            if (edgeIndex >= nodeSize) {
                edgeNode = edgeNode.previous;
                if (edgeNode == null) {
                    break;
                }
                edgeIndex = 0;
            }
        }

        return overlap;
    }

    public ByteSeqNode get(int ind){
        ByteSeqNode node = first;
        for(int i = 0; i < ind; i++){
            node = node.next;
        }
        return node;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        int nodes = 0;
        ByteSeqNode node = first;
        while (node != null) {
            nodes++;
            node = node.next;
        }

        out.writeInt(nodes);
        out.writeInt(nodeSize);
        out.writeInt(index);
        out.writeInt(length);

        node = first;
        while (node != null) {
            THashSet<ByteRestaurant> s = new THashSet<ByteRestaurant>(node);
            out.writeObject(s);
            out.writeObject(node.byteChunk);

            node = node.next;
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int nodes = in.readInt();
        nodeSize = in.readInt();
        index = in.readInt();
        length = in.readInt();

        THashSet<ByteRestaurant> s = (THashSet<ByteRestaurant>) in.readObject();

        ByteSeqNode nextNode;
        ByteSeqNode node = new ByteSeqNode(s);
        node.byteChunk = (byte[]) in.readObject();
        first = node;
        nodes--;
        while (nodes > 0) {
            s = (THashSet<ByteRestaurant>) in.readObject();
            nextNode = new ByteSeqNode(s);
            node.next = nextNode;
            nextNode.previous = node;
            nextNode.byteChunk = (byte[]) in.readObject();

            node = nextNode;
            nodes--;
        }
        last = node;
    }

    public class ByteSeqNode extends THashSet<ByteRestaurant> {

        private byte[] byteChunk;
        private ByteSeqNode previous, next;

        public ByteSeqNode(ByteSeqNode previous, ByteSeqNode next, int nodeSize) {
            this.previous = previous;
            this.next = next;

            byteChunk = new byte[nodeSize];
        }

        public ByteSeqNode(Collection<ByteRestaurant> collection){
            super(collection);
        }

        public ByteSeqNode(){};

        public ByteSeqNode previous() {
            return previous;
        }

        public byte[] byteChunk() {
            return byteChunk;
        }

        public int getIndex(){
            int ind = -1;
            ByteSeqNode node = this;
            while(node != null){
                ind++;
                node = node.previous;
            }
            return ind;
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

    public static void main(String[] args) throws IOException {

        ByteSeq bs = new ByteSeq(1024);

        for (int i = 0; i < 200000; i++) {
            bs.append((byte) i);
        }


        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream("/Users/nicholasbartlett/Documents/np_bayes/data/test/sm_object"));
            oos.writeObject(bs);
        } catch (Exception e) {
            e.printStackTrace();
            if (oos != null) {
                oos.close();
            }
        }

        System.out.println(bs.get(100).getIndex());

        ObjectInputStream ois = null;
        ByteSeq bs1 = null;
        //THashSet<Integer> bs1 = null;
        try {
            ois = new ObjectInputStream(new FileInputStream("/Users/nicholasbartlett/Documents/np_bayes/data/test/sm_object"));
            bs1 = (ByteSeq) ois.readObject();
            //bs1 = (THashSet<Integer>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            if (ois != null) {
                ois.close();
            }
        }


        assert Arrays.equals(bs1.first.byteChunk, bs.first.byteChunk);
        assert Arrays.equals(bs1.last.byteChunk, bs.last.byteChunk);

        ByteSeqNode node1 = bs.first;
        ByteSeqNode node2 = bs1.first;
        while (node1 != null) {
            assert Arrays.equals(node1.byteChunk, node2.byteChunk);
            node1 = node1.next;
            node2 = node2.next;
        }
    }
}
