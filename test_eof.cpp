#include <iostream>
#include <string>
using namespace std;
int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);
    string line;
    if (cin.peek() == EOF || !getline(cin, line) || line.empty()) {
        cout << "No input" << endl;
        return 0;
    }
    cout << "Input: " << line << endl;
    return 0;
}
