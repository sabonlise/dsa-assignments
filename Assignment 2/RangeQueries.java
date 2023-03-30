package dsa;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Main {
    public static void main(String[] args) throws ParseException {

        RangeQueries<String, Integer> history = new RangeQueries<>(20);  // initial degree of BTree
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();

        for (int i = 0; i <= n; i++) {
            String line = sc.nextLine();
            String[] tmp = line.split(" ");

            if (tmp.length == 3) {
                String date = tmp[0];
                String command = tmp[1];
                Integer amount = Integer.parseInt(tmp[2]);

                if (command.equals("DEPOSIT")) {
                    history.add(date, amount);
                } else if (command.equals("WITHDRAW")) {
                    history.add(date, -amount);
                }

            } else if (tmp.length == 5) {
                int sum = 0;
                String fromDate = tmp[2];
                String toDate = tmp[4];
                List<Integer> balance = history.lookupRange(fromDate, toDate);
                if (balance != null && !balance.isEmpty()) {
                    for (int transaction : balance) {
                        sum += transaction;
                    }
                    System.out.println(sum);
                } else {
                    System.out.println(0);
                }
            }
        }
    }
}

class RangeQueries<K extends Comparable<K>, V> implements RangeMap<K, V> {

    private final int degree;
    private Node<K, V> root;
    private int numberOfKeys = 0;

    private static class Node<K, V> {
        int n;
        boolean leaf = true;
        Node<K, V>[] child;
        Entry<K, V>[] entries;
        private Node(int deg) {
            this.entries = new Entry[2 * deg - 1];
            this.child = new Node[2 * deg];
        }
    }

    private static class Entry<K, V> {
        private final K key;
        private final V value;
        private Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public RangeQueries(int t) {
        root = new Node<>(t);
        root.leaf = true;
        root.n = 0;    // initial amount of keys is 0
        degree = t;
    }


    // Searching for a value by key
    private V search(Node<K, V> x, K key) {
        int i = 0;

        // Finding the smallest index "i"
        while (i < x.n && less(x.entries[i].key, key)) {
            i++;
        }
        // Returning the value if we have discovered the key
        if (i < x.n && equal(key, x.entries[i].key)) {
            return x.entries[i].value;
        }

        // If "x" is not a leaf, we should recurse to search the appropriate subtree of "x"
        if (!x.leaf) return search(x.child[i], key);
        return null;
    }

    // Splitting a node
    private void split(Node<K, V> x, int position) {
        Node<K, V> newNode = new Node<>(degree);
        /* oldNode originally has "2 * degree" children ("2 * degree - 1" keys)
           after executing split it is reduced to "degree" children ("degree - 1" keys) */
        Node<K, V> oldNode = x.child[position];
        newNode.leaf = oldNode.leaf;
        newNode.n = degree - 1;

        /* Here we create "newNode" and give it the largest "degree - 1" keys
           and corresponding "degree" children of "oldNode" */
        if (degree - 1 >= 0) System.arraycopy(oldNode.entries, degree, newNode.entries, 0, degree - 1);
        if (!oldNode.leaf) {
            System.arraycopy(oldNode.child, degree, newNode.child, 0, degree);
        }

        // Adjusting the key count for "oldNode"
        oldNode.n = degree - 1;
        // Inserting "newNode" as a child of "x"
        // Moving the median key from "oldNode" up to "x" in order
        // to separate "oldNode" from "newNode" and adjust key count for "x"
        if ((x.n - position) >= 0)
            System.arraycopy(x.child, position + 1, x.child, position + 2, x.n - position);
        x.child[position + 1] = newNode;

        if (x.n - position >= 0)
            System.arraycopy(x.entries, position, x.entries, position + 1, x.n - position);

        x.entries[position] = oldNode.entries[degree - 1];
        x.n = x.n + 1;
    }

    // Inserting a key-value entry in tree
    public void add(K key, V value) {
        numberOfKeys++;
        Node<K, V> r = root;
        Entry<K, V> e = new Entry<>(key, value);
        if (r.n == 2 * degree - 1) {
            Node<K, V> s = new Node<>(degree);
            root = s;
            s.leaf = false;
            s.n = 0;
            s.child[0] = r;
            /*
            Since we cannot insert a key into a leaf node that is full
            we need to split a node into two nodes around the median key
             */
            split(s, 0);
            //  Inserting entry into the tree rooted at the non-full root node
            insertNonFull(s, e);
        } else {
            insertNonFull(r, e);
        }
    }

    // Handling the case when we are inserting into non-full root node
    private void insertNonFull(Node<K, V> x, Entry<K, V> e) {
        int i = x.n - 1;
        if (x.leaf) {
            // Inserting entry "e" into "x" if "x" is a leaf
            for (; i >= 0 && less(e.key, x.entries[i].key); i--) {
                x.entries[i + 1] = x.entries[i];
            }
            x.entries[i + 1] = e;
            x.n = x.n + 1;
        } else {
            /*
            If "x" is not a leaf node, we have to insert entry "e"
            into the appropriate leaf node in the subtree rooted at internal node "x"
             */
            while(i >= 0 && less(e.key, x.entries[i].key)) i--;
            i++;
            Node<K, V> nextNode = x.child[i];
            if (nextNode.n == 2 * degree - 1) {
                /*
                 If the recursion descends to a full child,
                 we need to split that child into two non-full children
                 */
                split(x, i);
                if (less(x.entries[i].key, e.key)) i++;  // determining to which children we should descend to
            }
            insertNonFull(x.child[i], e);
        }
    }

    public V lookup(K key) {
        // Getting the value by key, by searching from the root node
        return search(root, key);
    }

    public List<V> lookupRange(K from, K to) {
        // Executing privateLookupRange by starting to search from the root node
        return privateLookupRange(root, from, to);
    }

    private ArrayList<V> privateLookupRange(Node<K, V> x, K from, K to) {
        if (x == null) return null;
        ArrayList<V> result = new ArrayList<>();
        int entry;

        for (entry = 0; entry < x.n; entry++) {
            K entryKey = x.entries[entry].key;
            if (less(to, entryKey)) break;  // if we exceed interval, we have to immediately stop traversing

            if ((less(from, entryKey) || equal(from, entryKey)) && (less(entryKey, to) || equal(entryKey, to))) {
                // If the current entry key is within bounds, we check if we have to traverse its children
                // if it's not leaf, or to simply add current value to result
                if (!x.leaf) result.addAll(privateLookupRange(x.child[entry], from, to));
                result.add(x.entries[entry].value);
            }
        }

        // If the current node is leaf, we have to perform search in the interval for its children
        if (!x.leaf) result.addAll(privateLookupRange(x.child[entry], from, to));
        return result;
    }

    public int size() {
        return numberOfKeys;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    private boolean equal(K key1, K key2) {
        return key1.compareTo(key2) == 0;
    }

    private boolean less(K key1, K key2) {
        return key1.compareTo(key2) < 0;
    }

    public boolean contains(K k) {
        return lookup(k) != null;
    }
}

interface RangeMap<K, V> {
    int size();
    boolean isEmpty();
    void add(K key, V value);
    boolean contains(K key);
    V lookup(K key);
    List<V> lookupRange(K from, K to);
}

