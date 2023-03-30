#include <iostream>
#include <array>
#include <cmath>

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


template<class T>
class DoubleHashSet : public ISet<T> {
    T* hashTable;
    const int capacity_t = 98317;
    bool* empty_cells;
    bool* deleted_elements;
    long long size_t = 0;
public:
    DoubleHashSet() {
        hashTable = new T[capacity_t];
        deleted_elements = new bool[capacity_t];
        empty_cells = new bool[capacity_t];
        for (long long k = 0; k < capacity_t; k++) {
            empty_cells[k] = true;
            deleted_elements[k] = false;
        }
    }

    void add(T item) override {
        long long k = 0;
        long long h = hash1(item);
        while(!empty_cells[h]) {
            if (deleted_elements[h]) {
                deleted_elements[h] = false;
                break;
            }
            k += 1;
            h = (hash1(item) + k * hash2(item)) % capacity_t;
        }
        hashTable[h] = item;
        empty_cells[h] = false;
        size_t++;
    }

    void remove(T item) override {
        if (isEmpty()) throw out_of_range("Cannot remove element from an empty set");
        long long index = search(item);
        if (index != -1) {
            deleted_elements[index] = true;
            size_t--;
        }
    }

    bool contains(T item) override {
        if (search(item) != -1) {
            return true;
        }
        return false;
    }

    int size() override {
        return size_t;
    }

    bool isEmpty() override {
        return size_t == 0;
    }

    long long hash1(T k) {
        hash<T> t;
        long long h = t(k);
        return abs(h) % capacity_t;
    }

    long long hash2(T k) {
        hash<T> t;
        long long h = t(k);
        return (12289 - (abs(h) % 12289));
    }

    long long search(T k) {
        long long h1 = hash1(k);
        int i = 0;
        while(true) {
            if (empty_cells[h1]) {
                return -1;
            } else if (hashTable[h1] == k && !deleted_elements[h1]) {
                return h1;
            } else {
                i++;
                h1 = (hash1(k) + hash2(k) * i) % capacity_t;
            }
        }

    }

    string list() {
        string env;
        for (long long i = 0; i < capacity_t; i++) {
            if (!empty_cells[i] && !deleted_elements[i]) {
                env += hashTable[i] + " ";
            }
        }
        return env;
    }
};


int main() {
    long long command_amount;
    cin >> command_amount;
    string full_command, command, file;
    string output;
    DoubleHashSet<string> environment;
    cin.ignore();
    while(command_amount > 0) {
        getline(cin, full_command);
        command_amount--;
        if (full_command != "LIST") {
            command = full_command.substr(0, full_command.find(' '));
            file = full_command.erase(0, full_command.find(' ') + 1);
            if (command == "NEW") {
                if (file.back() == '/') {
                    if (!environment.contains(file) && !environment.contains(file.substr(0, file.size() - 1))) {
                        environment.add(file);
                    } else {
                        cout << "ERROR: cannot execute " << command << " " << file << endl;
                    }
                } else {
                    if (!environment.contains(file) && !environment.contains(file + '/')) {
                        environment.add(file);
                    } else {
                        cout << "ERROR: cannot execute " << command << " " << file << endl;
                    }
                }
            } else if (command == "REMOVE") {
                if (environment.contains(file)) {
                    environment.remove(file);
                } else {
                    cout << "ERROR: cannot execute " << command << " " << file << endl;
                }
            }
        } else {
            cout << environment.list() << endl;
        }
    }

    return 0;
}

