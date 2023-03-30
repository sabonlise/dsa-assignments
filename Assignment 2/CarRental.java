package dsa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static java.util.Collections.swap;

public class CarRental {
    public static void main(String[] args) {
        Graph<Pair<String, Double>, Double, String, Double> graph = new Graph<>();
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        for (int i = 0; i <= n; i++) {
            String[] tmp = sc.nextLine().split(" ");
            switch (tmp[0]) {
                case "PRINT_MIN":
                    graph.msf();
                    System.out.println();
                    break;
                case "ADD":
                    graph.insertVertex(new Pair<>(tmp[1], Double.parseDouble(tmp[2])));
                    break;
                case "CONNECT":
                    // Retrieving vertices hashmap to get vertices by their name and insert corresponding edge
                    var vertices = graph.getVertices();
                    graph.insertEdge(vertices.get(tmp[1]), vertices.get(tmp[2]), Double.parseDouble(tmp[3]));
                    break;
            }
        }
    }
}

// Generic class to work with Vertex object
class Pair<V, P extends Number & Comparable<P>> {
    private final V value;
    private final P penalty;

    public Pair(V value, P penalty) {
        this.value = value;
        this.penalty = penalty;
    }

    public V getValue() {
        return value;
    }
    public P getPenalty() {
        return penalty;
    }
}

class Graph<V extends Pair<K, P>, E extends Number, K, P extends Number & Comparable<P>> implements IGraph<V, E> {
    // Primary adjacency matrix
    private final ArrayList<ArrayList<Edge<V, E>>> adjacencyMatrix;
    // Hashmap to get Vertex object by its index
    private final HashMap<Integer, Vertex<V>> verticesIndex = new HashMap<>();
    // Hashmap to get Vertex object by its name
    private final HashMap<K, Vertex<V>> vertices = new HashMap<>();
    // Variable to assign each Vertex its own index and to track current amount of vertices
    private int currentIndex;

    public Graph() {
        currentIndex = 0;
        adjacencyMatrix = new ArrayList<>();
    }

    public Vertex<V> insertVertex(V v) {
        // Creating Vertex object from key-value pair and assigning new vertex its index
        Vertex<V> vertex = new Vertex<>(v, currentIndex);
        // Filling corresponding hashmaps
        verticesIndex.put(currentIndex, vertex);
        vertices.put(v.getValue(), vertex);
        // Incrementing current vertex index
        currentIndex++;
        return vertex;
    }

    public Edge<V, E> insertEdge(Vertex<V> from, Vertex<V> to, E w) {
        // Calculating weight by provided formula
        Double weight = w.doubleValue() / (from.getVertex().getPenalty().doubleValue() + to.getVertex().getPenalty().doubleValue());
        // Creating edges from both sides, since the graph is undirected
        Edge<V, E> edge1 = new Edge(from, to, weight);
        Edge<V, E> edge2 = new Edge(to, from, weight);

        /*
         Determining to which size we should resize our matrix

         Note: adjacency matrix is not always a square matrix, since we do not resize matrix each time to
         current amount of vertices, but rather to the current needed size
         */
        int newSize = Math.max(from.getIndex(), to.getIndex()) + 1;

        // If current amount of rows is less than needed size, we insert empty lists
        if (adjacencyMatrix.size() < newSize) {
            while (adjacencyMatrix.size() != newSize) {
                adjacencyMatrix.add(new ArrayList<>());
            }
        }

        // If current row is not filled enough to insert first edge, we resize it appropriately
        if (adjacencyMatrix.get(from.getIndex()).size() < to.getIndex() + 1) {
            while (adjacencyMatrix.get(from.getIndex()).size() != to.getIndex() + 1) {
                adjacencyMatrix.get(from.getIndex()).add(null);
            }
        }
        // If current row is not filled enough to insert second edge, we resize it appropriately
        if (adjacencyMatrix.get(to.getIndex()).size() < from.getIndex() + 1) {
            while (adjacencyMatrix.get(to.getIndex()).size() != from.getIndex() + 1) {
                adjacencyMatrix.get(to.getIndex()).add(null);
            }
        }

        // Inserting edges from both sides
        adjacencyMatrix.get(from.getIndex()).set(to.getIndex(), edge1);
        adjacencyMatrix.get(to.getIndex()).set(from.getIndex(), edge2);
        // Incrementing degree of both vertices
        to.degree++;
        from.degree++;

        return edge1;
    }

    // Allowing to retrieve hashmap of vertices to insert new edges by getting vertex object from vertex name
    public HashMap<K, Vertex<V>> getVertices() {
        return vertices;
    }

    // Removing vertex references from hashmaps
    public void removeVertex(Vertex<V> v) {
        vertices.remove(v.getVertex().getValue());
        verticesIndex.remove(v.getIndex());
    }

    // To remove edge, we set its position in adjacency matrix to null from both sides, so we ignore it later
    public void removeEdge(Edge<V, E> e) {
        Vertex<V> from = e.getVertexFrom();
        Vertex<V> to = e.getVertexFrom();
        adjacencyMatrix.get(from.getIndex()).set(to.getIndex(), null);
        adjacencyMatrix.get(to.getIndex()).set(from.getIndex(), null);
    }

    // If vertices are adjacent, the appropriate position in adjacency matrix is not null
    public boolean areAdjacent(Vertex<V> v, Vertex<V> u) {
        return adjacencyMatrix.get(v.getIndex()).get(u.getIndex()) != null;
    }

    public int degree(Vertex<V> v) {
        return v.degree;
    }

    // Method to run prims' algorithm with each unvisited vertex, in order to find minimum spanning forest
    public void msf() {
        boolean[] visited = new boolean[currentIndex + 1];
        for (int i = 0; i < currentIndex; i++) {
            if (!visited[i]) prim(i, visited);
        }
    }

    // Prims' algorithm based on priority queue of edges
    private void prim(int currentVertex, boolean[] visited) {
        // Marking current vertex as visited
        visited[currentVertex] = true;
        PriorityQueue<Double, Edge<V, E>> queue = new PriorityQueue<>();
        // Adding incident edges of vertex in priority queue
        insertInQueue(currentVertex, queue);

        while (!queue.isEmpty()) {
            // Extracting minimum edge from priority queue
            Edge<V, E> edge = queue.extractMin().value;
            Vertex<V> from = edge.getVertexFrom();
            Vertex<V> to = edge.getVertexTo();
            // Switching to the next vertex to find its incident edges
            int nextVertex = to.getIndex();
            if (!visited[nextVertex]) {
                System.out.print(from.getVertex().getValue() + ":" + to.getVertex().getValue() + " ");
                visited[nextVertex] = true;
                int count = 0;
                for (int i = 0; count < to.degree && i < currentIndex; i++) {
                    Edge<V, E> e = adjacencyMatrix.get(nextVertex).get(i);
                    if (e != null) {
                        count++;
                        queue.insert(queue.new BNode(e.getWeight().doubleValue(), e));
                    }
                }
            }
        }

    }

    // Method to insert incident edges of vertex in priority queue
    private void insertInQueue(int index, PriorityQueue<Double, Edge<V, E>> queue) {
        int count = 0;
        for (int i = 0; count < verticesIndex.get(index).degree && i < currentIndex; i++) {
            Edge<V, E> e = adjacencyMatrix.get(index).get(i);
            if (e != null) {
                count++;
                queue.insert(queue.new BNode(e.getWeight().doubleValue(), e));
            }
        }
    }
}

// Vertex class, containing its name, penalty (which are located in vertex object) and degree
class Vertex<V> {
    private final V vertex;
    private final int index;
    public int degree;
    public Vertex(V vertex, int index) {
        this.vertex = vertex;
        this.index = index;
        this.degree = 0;
    }
    public V getVertex() {
        return vertex;
    }
    public int getIndex() {
        return index;
    }
}

// Edge class, containing its weight and both vertices
class Edge<V, E extends Number> implements Comparable<Edge> {
    private final E weight;
    private final Vertex<V> vertexFrom;
    private final Vertex<V> vertexTo;

    public Edge(Vertex<V> vertexFrom, Vertex<V> vertexTo, E weight) {
        this.vertexFrom = vertexFrom;
        this.vertexTo = vertexTo;
        this.weight = weight;
    }

    public Vertex<V> getVertexFrom() {
        return vertexFrom;
    }

    public Vertex<V> getVertexTo() {
        return vertexTo;
    }

    public E getWeight() {
        return weight;
    }

    @Override
    public int compareTo(Edge o) {
        return Double.compare(weight.doubleValue(), o.getWeight().doubleValue());
    }
}

interface IGraph<V, E extends Number> {
    Vertex<V> insertVertex(V v);

    Edge<V, E> insertEdge(Vertex<V> from, Vertex<V> to, E w);

    void removeVertex(Vertex<V> v);

    void removeEdge(Edge<V, E> e);

    boolean areAdjacent(Vertex<V> v, Vertex<V> u);

    int degree(Vertex<V> v);

}

// PriorityQueue from previous task, slightly modified version (removed comparisons of values if keys are equal)
class PriorityQueue<K extends Comparable<K>, V> implements IPriorityQueue<K, V, PriorityQueue<K, V>.BNode> {
    private final ArrayList<BNode> minHeap = new ArrayList<>();

    public class BNode extends Node<K, V> {
        BNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public PriorityQueue() {
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
            }
        }

        if (right < size()) {
            BNode rightElement = minHeap.get(right);
            if (less(rightElement.key, minHeap.get(smallest).key)) {
                smallest = right;
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
        while (i > 0 && less(minHeap.get(i).key, minHeap.get(parent(i)).key)) {
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

    public boolean isEmpty() {
        return size() == 0;
    }

    // 2 methods below are used to compare keys or values of the heap
    private boolean less(K key1, K key2) {
        return key1.compareTo(key2) < 0;
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
