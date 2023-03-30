#include <iostream>
#include <array>
using namespace std;

template<class T>
class ISet {
public:
    virtual void add(T item) = 0;
    virtual void remove(T item) = 0;
    virtual bool contains(T item) = 0;
    virtual int size() = 0;
    virtual bool isEmpty() = 0;
};

template <class T>
class ICircularBoundedQueue {
public:
    virtual void offer(T value) = 0;
    virtual T poll() = 0;
    virtual T peek() = 0;
    virtual bool isEmpty() = 0;
    virtual bool isFull() = 0;
    virtual int size() = 0;
    virtual int capacity() = 0;
};

template <class T>
class IBoundedStack {
public:
    virtual void push(T value) = 0;
    virtual T pop() = 0;
    virtual T top() = 0;
    virtual void flush() = 0;
    virtual bool isEmpty() = 0;
    virtual bool isFull() = 0;
    virtual int size() = 0;
    virtual int capacity() = 0;
};

template<class T>
struct Node {
    T data;
    Node<T> *next;
};

// Implementation of Linked List
template<class T>
class LinkedList {
    Node<T> *head;
    int size_t;
public:
    LinkedList() {
        head = nullptr;
        size_t = 0;
    }

    void addLast(T value) {
        auto *node = new Node<T>;
        node->data = value;
        node->next = nullptr;
        if (size_t == 0) {
            head = node;
        } else {
            auto *temp = head;
            while(temp->next != nullptr) {
                temp = temp->next;
            }
            temp->next = node;
        }
        size_t++;
    }

    T removeFirst() {
        auto temp = head->data;
        auto *node = head;
        head = head->next;
        delete node;
        size_t--;
        return temp;
    }

    T getFirst() {
        return head->data;
    }

    void display() {
        auto *node = head;
        while(node != nullptr) {
            cout << node->data << endl;
            node = node->next;
        }
    }

    int size() {
        return size_t;
    }

};

template<class T>
class CircularBoundedQueue : public ICircularBoundedQueue<T> {
    LinkedList<T> items;
    int capacity_t{};
public:
    void setCapacity(int cap) {
        capacity_t = cap;
    }

    // Worst case = average case = amortized = O(n)
    void offer(T value) {
        while (isFull()) {   // if is full, item is being removed only once
            items.removeFirst();   // O(1)
        }
        items.addLast(value);   // O(n)
    }

    // Worst case = average case = amortized = O(1)
    T poll() {
        if (isEmpty()) throw out_of_range("Cannot poll from empty queue");
        T item = items.removeFirst();
        return item;
    }

    // Worst case = average case = amortized = O(1)
    T peek() {
        if (isEmpty()) throw out_of_range("Cannot peek from empty queue");
        T item = items.getFirst();
        return item;
    }

    // Worst case = average case = amortized = O(n)
    void flush() {
        while(items.size() != 0) {
            items.removeFirst();
        }
    }

    // Worst case = average case = amortized = O(1)
    bool isEmpty() {
        return items.size() <= 0;
    }

    // Worst case = average case = amortized = O(1)
    bool isFull() {
        return size() >= capacity();
    }

    // Worst case = average case = amortized = O(1)
    int size() {
        return items.size();
    }

    // Worst case = average case = amortized = O(1)
    int capacity() {
        return capacity_t;
    }

};

template<class T>
class QueueBoundedStack : public IBoundedStack<T> {
    CircularBoundedQueue<T> first_queue;
    CircularBoundedQueue<T> second_queue;
public:
    void setCapacity(int cap) {
        first_queue.setCapacity(cap);
        second_queue.setCapacity(cap);
    }

    // Worst case = average case = amortized = O(n)

    // The main idea of this approach is to use second queue
    // as a temporal storage for stack elements when we are adding new element
    void push(T value) {
        if (first_queue.isEmpty()) {
            first_queue.offer(value);
        } else {
            int size_t = size();
            for(int i = 0; i < size_t; i++) {
                second_queue.offer(first_queue.poll());
            }
            first_queue.offer(value);
            for(int k = 0; k < size_t; k++) {
                first_queue.offer(second_queue.poll());
            }
        }
    }

    // Worst case = average case = amortized = O(1)
    T pop() {
        if (isEmpty()) throw out_of_range("Cannot pop from empty stack");
        return first_queue.poll();
    }

    // Worst case = average case = amortized = O(1)
    T top() {
        if (isEmpty()) throw out_of_range("Cannot retrieve top element from empty stack");
        return first_queue.peek();
    }

    // Worst case = average case = amortized = O(n)
    void flush() {
        first_queue.flush();
    }

    // Worst case = average case = amortized = O(1)
    bool isEmpty() {
        return first_queue.isEmpty();
    }

    // Worst case = average case = amortized = O(1)
    bool isFull() {
        return first_queue.isFull();
    }
    // Worst case = average case = amortized = O(1)
    int size() {
        return first_queue.size();
    }
    // Worst case = average case = amortized = O(1)
    int capacity() {
        return first_queue.capacity();
    }
};


template<class T>
class DoubleHashSet : public ISet<T> {
    T* hashtable;
    int capacity_t = 98317;
    bool* empty_cells;
    bool* deleted_elements;
    int table_size = 0;
public:
    // Time complexity depends on load factor
    DoubleHashSet() {
        hashtable = new T[capacity_t];
        deleted_elements = new bool[capacity_t];
        empty_cells = new bool[capacity_t];
        for (int k = 0; k < capacity_t; k++) {
            empty_cells[k] = true;
            deleted_elements[k] = false;
        }
    }

    DoubleHashSet(DoubleHashSet& dhs) {
        hashtable = new T[capacity_t];
        deleted_elements = new bool[capacity_t];
        empty_cells = new bool[capacity_t];

        hashtable = dhs.hashtable;
        deleted_elements = dhs.deleted_elements;
        empty_cells = dhs.empty_cells;
    }

    // Worst case = O(n)
    // Average case = O(1)
    // Amortized = O(1)
    void add(T item) override {
        int k = 0;
        int h = hash1(item);
        while(!empty_cells[h]) {
            if (deleted_elements[h]) {
                deleted_elements[h] = false;
                break;
            }
            k += 1;
            h = (hash1(item) + k * hash2(item)) % capacity_t;
        }
        hashtable[h] = item;
        empty_cells[h] = false;
        table_size++;
    }

    // Worst case = O(n)
    // Average case = O(1)
    // Amortized = O(1)
    void remove(T item) override {
        if (isEmpty()) throw out_of_range("Cannot remove element from an empty set");
        int index = search(item);
        if (index != -1) {
            deleted_elements[index] = true;
            table_size--;
        }
    }

    // Worst case = O(n)
    // Average case = O(1)
    // Amortized = O(1)
    bool contains(T item) override {
        if (search(item) != -1) {
            return true;
        }
        return false;
    }

    // Worst case = O(1)
    // Average case = O(1)
    // Amortized = O(1)
    int size() override {
        return table_size;
    }

    // Worst case = O(1)
    // Average case = O(1)
    // Amortized = O(1)
    bool isEmpty() override {
        return table_size == 0;
    }

    // Worst case = O(1)
    // Average case = O(1)
    // Amortized = O(1)
    int hash1(T k) {
        hash<T> t;
        int h = t(k);
        return abs(h) % capacity_t;
    }

    // Worst case = O(1)
    // Average case = O(1)
    // Amortized = O(1)
    int hash2(T k) {
        hash<T> t;
        int h = t(k);
        return (12289 - (abs(h) % 12289));
    }

    // Worst case = O(n)
    // Average case = O(1)
    // Amortized = O(1)
    int search(T k) {
        int h1 = hash1(k);
        int i = 0;
        while(true) {
            if (empty_cells[h1]) {
                return -1;
            } else if (hashtable[h1] == k && !deleted_elements[h1]) {
                return h1;
            } else {
                i++;
                h1 = (hash1(k) + hash2(k) * i) % capacity_t;
            }
        }
    }

    // An additional method to retrieve items of set
    // Worst case = O(n)
    // Average case = O(n)
    // Amortized = O(n)
    string list() {
        string env;
        for (int i = 0; i < capacity_t; i++) {
            if (!empty_cells[i] && !deleted_elements[i]) {
                if (hashtable[i].length() > 0) {
                    env += hashtable[i] + " ";
                }
            }
        }
        return env;
    }
};
