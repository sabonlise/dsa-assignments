#include <iostream>
#include <memory>
#include <array>
#include <cmath>
#include <cstdint>
#include <cstring>

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
    Node<T> *next{};
};

template<class T>
class LinkedList {
    Node<T> *head;
    int table_size;
public:
    LinkedList() {
        head = nullptr;
        table_size = 0;
    }

    void addLast(T value) {
        auto *node = new Node<T>;
        node->data = value;
        node->next = nullptr;
        if (table_size == 0) {
            head = node;
        } else {
            auto *temp = head;
            while(temp->next != nullptr) {
                temp = temp->next;
            }
            temp->next = node;
        }
        table_size++;
    }

    T removeFirst() {
        auto temp = head->data;
        auto *node = head;
        head = head->next;
        delete node;
        table_size--;
        return temp;
    }

    T getFirst() {
        return head->data;
    }

    int size() {
        return table_size;
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

    void offer(T value) {
        while (isFull()) {
            items.removeFirst();
        }
        items.addLast(value);
    }

    T poll() {
        if (isEmpty()) throw out_of_range("Cannot poll from empty queue");
        T item = items.removeFirst();
        return item;
    }

    T peek() {
        if (isEmpty()) throw out_of_range("Cannot peek from empty queue");
        T item = items.getFirst();
        return item;
    }

    void flush() {
        while(items.size() != 0) {
            items.removeFirst();
        }
    }

    bool isEmpty() {
        return items.size() <= 0;
    }

    bool isFull() {
        return size() >= capacity();
    }

    int size() {
        return items.size();
    }

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

    void push(T value) {
        if (first_queue.isEmpty()) {
            first_queue.offer(value);
        } else {
            int table_size = size();
            for(int i = 0; i < table_size; i++) {
                second_queue.offer(first_queue.poll());
            }
            first_queue.offer(value);
            for(int k = 0; k < table_size; k++) {
                first_queue.offer(second_queue.poll());
            }
        }
    }

    T pop() {
        if (isEmpty()) throw out_of_range("Cannot pop from empty stack");
        return first_queue.poll();
    }

    T top() {
        if (isEmpty()) throw out_of_range("Cannot retrieve top element from empty stack");
        return first_queue.peek();
    }

    void flush() {
        first_queue.flush();
    }

    bool isEmpty() {
        return first_queue.isEmpty();
    }

    bool isFull() {
        return first_queue.isFull();
    }

    int size() {
        return first_queue.size();
    }

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

    void remove(T item) override {
        if (isEmpty()) throw out_of_range("Cannot remove element from an empty set");
        int index = search(item);
        if (index != -1) {
            deleted_elements[index] = true;
            table_size--;
        }
    }

    bool contains(T item) override {
        if (search(item) != -1) {
            return true;
        }
        return false;
    }

    int size() override {
        return table_size;
    }

    bool isEmpty() override {
        return table_size == 0;
    }

    int hash1(T k) {
        hash<T> t;
        int h = t(k);
        return abs(h) % capacity_t;
    }

    int hash2(T k) {
        hash<T> t;
        int h = t(k);
        return (12289 - (abs(h) % 12289));
    }

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


int main() {
    int command_amount, stack_size, rollbacks;
    bool error;
    cin >> command_amount >> stack_size;
    string full_command, command, file;
    string output;

    auto environment = new QueueBoundedStack<DoubleHashSet<string>>;
    environment->setCapacity(stack_size);
    auto initial_state = new DoubleHashSet<string>;
    initial_state->add("");
    environment->push(*initial_state);

    cin.ignore();
    while(command_amount > 0) {
        getline(cin, full_command);
        command_amount--;
        if (full_command == "UNDO" || full_command == "UNDO 1") {
            if (environment->size() >= 1) {
                environment->pop();
            } else {
                cout << "ERROR: cannot execute " << full_command << endl;
            }
        } else if (full_command == "LIST") {
            for (int i = 0; i < environment->size(); i++) {
                cout << environment->pop().list() << endl;
            }
        } else {
            command = full_command.substr(0, full_command.find(' '));
            if (command == "UNDO") {
                rollbacks = stoi(full_command.erase(0, full_command.find(' ') + 1));
                if (environment->size() - rollbacks >= 0) {
                    for (int rb = 0; rb < rollbacks; rb++) {
                        environment->pop();
                    }
                } else {
                    cout << "ERROR: cannot execute " << command << " " << rollbacks << endl;
                }
            } else if (command == "NEW" || command == "REMOVE") {
                file = full_command.erase(0, full_command.find(' ') + 1);
                error = false;
                auto last_state = new DoubleHashSet(environment->top());    // copying the last state set
                if (command == "NEW") {
                    if (file.back() == '/') {
                        if (!last_state->contains(file) && !last_state->contains(file.substr(0, file.size() - 1))) {
                            last_state->add(file);
                        } else {
                            cout << "ERROR: cannot execute " << command << " " << file << endl;
                            error = true;
                        }
                    } else {
                        if (!last_state->contains(file) && !last_state->contains(file + '/')) {
                            last_state->add(file);
                        } else {
                            cout << "ERROR: cannot execute " << command << " " << file << endl;
                            error = true;
                        }
                    }
                } else if (command == "REMOVE") {
                    if (last_state->contains(file)) {
                        last_state->remove(file);
                    } else {
                        cout << "ERROR: cannot execute " << command << " " << file << endl;
                        error = true;
                    }
                }
                cout << last_state->list() << endl;
                if (!error) {
                    environment->push(*last_state);
                }
            }
        }
    }
    return 0;
}

