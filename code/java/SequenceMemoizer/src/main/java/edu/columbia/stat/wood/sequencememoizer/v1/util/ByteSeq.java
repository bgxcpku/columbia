/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.sequencememoizer.v1;

import edu.columbia.stat.wood.util.MutableInt;
import gnu.trove.set.hash.THashSet;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
