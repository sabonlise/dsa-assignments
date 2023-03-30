#include <iostream>

using namespace std;

template<class T>
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

template<class T>
struct Node {
    T data;
    Node<T> *next{};
};

template<class T>
class LinkedList {
    Node<T> *head;
    int size_t;
public:
    LinkedList() {
        head = nullptr;
        size_t = 0;
    }

    void addFirst(T value) {
        auto *node = new Node<T>;
        node->data = value;
        node->next = nullptr;
        if (head != nullptr) node->next = head;
        head = node;
        size_t++;
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


//int main() {
//    int n, k;
//    cin >> n >> k;
//    CircularBoundedQueue<string> commands;
//    commands.setCapacity(k);
//    string command;
//    for (int c = 0; c < n; c++) {
//        getline(cin, command);
//        if (c >= (n - k)) {
//            commands.offer(command);
//        }
//    }
//    getline(cin, command);
//    commands.offer(command);
//
//    for (int i = 0; i < k; i++) {
//        cout << commands.poll() << endl;
//    }
//
//    return 0;
//}

