#include <iostream>
#include <vector>
#include <string>
#include <sstream>
using namespace std;

class Solution {
public:
    string helloWorld() {
        cout << "Hello, World!" << endl;
        // no return
    }
};

void printResult(const auto& result) {
    cout << result << endl;
}

int main() {
    Solution solution;
    string line;

    ostringstream captured;
    streambuf* oldBuf = cout.rdbuf(captured.rdbuf());
    try { printResult(solution.helloWorld()); }
    catch (...) { solution.helloWorld(); }
    cout.rdbuf(oldBuf);
    string out = captured.str();
    if (!out.empty()) cout << out;

    return 0;
}
