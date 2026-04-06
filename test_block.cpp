#include <iostream>
#include <string>
using namespace std;

int main() {
    string line;
    if (!getline(cin, line) || line.empty()) {
        cout << "No input" << endl;
        return 0;
    }
    cout << "Input: " << line << endl;
    return 0;
}
