package dsa;

import java.text.ParseException;
import java.util.*;

import static java.util.Collections.swap;

class TaskB {
    public static void main(String[] args) throws ParseException {

        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        PriorityQueue<Integer, String> branches = new PriorityQueue<>();

        for (int i = 0; i <= n; i++) {

            String line = sc.nextLine();
            if (Objects.equals(line, "")) continue;
            String[] tmp = line.split(" ");

            if (tmp.length == 3) {
                int penalty = Integer.parseInt(tmp[2]);
                String branchName = tmp[1];
                PriorityQueue.BNode branch = branches.new BNode(penalty, branchName);
                branches.insert(branch);
            } else if (tmp.length == 1) {
                System.out.println(branches.extractMin().value);
            }

        }
    }
}

class PriorityQueue<K extends Comparable<K>, V extends Comparable <V>> implements IPriorityQueue<K, V, PriorityQueue<K, V>.BNode> {
    private final ArrayList<BNode> minHeap;

    public class BNode extends Node<K, V> {
        BNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public PriorityQueue() {
        this.minHeap = new ArrayList<>();
    }

    private int parent(int i) {
        // Retrieving parent of current element
        return ((i - 1) / 2);
    }

    private int left(int i) {
        // Retrieving left child of current element
        return (2 * i) + 1;
    }

    private int right(int i) {
        // Retrieving right child of current element
        return (2 * i) + 2;
    }

    private void minHeapify(int i) {
        int left = left(i);
        int right = right(i);

        int smallest = i;

        if (left < size()) {
            BNode currentElement = minHeap.get(i);
            BNode leftElement = minHeap.get(left);
            if (less(leftElement.key, currentElement.key)) {
                smallest = left;
            } else if (equal(leftElement.key, currentElement.key)) {
                // The same as in minHeapify, those checks are needed to compare by values if keys are equal
                if (lessValue(leftElement.value, currentElement.value)) {
                    smallest = left;
                }
            }
        }

        if (right < size()) {
            BNode rightElement = minHeap.get(right);
            if (less(rightElement.key, minHeap.get(smallest).key)) {
                smallest = right;
            } else if (equal(rightElement.key, minHeap.get(smallest).key)) {
                if (lessValue(rightElement.value, minHeap.get(smallest).value)) {
                    smallest = right;
                }
            }
        }

        if (smallest != i) {
            // Swapping current element with the next found smaller element
            swap(minHeap, i, smallest);
            minHeapify(smallest);
        }
    }

    public void insert(BNode item) {
        minHeap.add(item);
        int i = size() - 1;
        // Swapping current and parent elements if current element key is lower than parent's
        // in case if those elements are equal, we compare their values
        while (i > 0 && ((less(minHeap.get(i).key, minHeap.get(parent(i)).key)) ||
                        (equal(minHeap.get(i).key, minHeap.get(parent(i)).key)) &&
                        (lessValue(minHeap.get(i).value, minHeap.get(parent(i)).value)))) {
            swap(minHeap, i, parent(i));
            i = parent(i);
        }
    }

    public BNode findMin() {
        return minHeap.get(0);
    }

    public BNode extractMin() {
        BNode min = findMin();
        // Retrieving min element, which is located in the root
        minHeap.set(0, minHeap.get(size() - 1));
        // Replacing min element with the last one, to then restore the heap
        minHeap.remove(size() - 1);
        minHeapify(0);

        return min;
    }

    public void decreaseKey(BNode item, K newKey) {
        // Deleting the previous occurrence of current item
        delete(item);
        // Creating new item with the new key and previous item value
        BNode newItem = new BNode(newKey, item.value);
        insert(newItem);
    }

    public void delete(BNode item) {
        // Finding the index of item in the heap
        int i = findIndexOfItem(item);

        // Deleting the item
        BNode tmp = minHeap.get(i);
        minHeap.set(i, minHeap.get(size() - 1));
        minHeap.set(size() - 1, tmp);
        minHeap.remove(size() - 1);

        // Restoring heap by performing multiple minHeapify
        for (int j = (size() / 2) - 1; j >= 0; j--) {
            minHeapify(j);
        }
    }

    private int findIndexOfItem(BNode item) {
        int i;
        // Retrieving the index of item we need to remove
        for (i = 0; i < size(); i++) {
            if (item.key.equals(minHeap.get(i).key) && item.value.equals(minHeap.get(i).value)) {
                break;
            }
        }
        return i;
    }

    public void union(Object anotherQueue) {
        PriorityQueue<K, V> queue = (PriorityQueue) anotherQueue;

        // Adding elements from another queue to our queue
        for (int i = 0; i < queue.size(); i++) {
            minHeap.add(queue.extractMin());
        }

        // Restoring heap after merging two queues
        for (int i = ((size() + queue.size()) / 2) - 1; i >= 0; i--) {
            minHeapify(i);
        }
    }

    private int size() {
        return minHeap.size();
    }

    // 3 methods below are used to compare keys or values of the heap
    private boolean less(K key1, K key2) {
        return key1.compareTo(key2) < 0;
    }

    private boolean equal(K key1, K key2) {
        return key1.compareTo(key2) == 0;
    }

    private boolean lessValue(V value1, V value2) {
        return value1.compareTo(value2) < 0;
    }

}

class Node<K, V> {
    K key;
    V value;
    Node() {}
    Node(K key, V value) {
        this.key = key;
        this.value = value;
    }
}

interface IPriorityQueue<K, V, N extends Node<K, V>> {
    void insert(N item);

    N findMin();

    N extractMin();

    void decreaseKey(N item, K newKey);

    void delete(N item);

    void union(Object anotherQueue);
}