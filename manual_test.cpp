#include <iostream>
#include <string>
#include <vector>
#include <algorithm>
using namespace std;

// Test 1: Hello World
class Solution1 {
public:
    string helloWorld() {
        return "Hello, World!";
    }
};

// Test 2: Add Two Numbers
class Solution2 {
public:
    int addTwoNumbers(int a, int b) {
        return a + b;
    }
};

// Test 3: Find Maximum
class Solution3 {
public:
    int findMaximum(vector<int>& nums) {
        return *max_element(nums.begin(), nums.end());
    }
};

// Test 4: Even or Odd
class Solution4 {
public:
    string evenOrOdd(int n) {
        return n % 2 == 0 ? "even" : "odd";
    }
};

int main() {
    // Test 1
    Solution1 sol1;
    string result1 = sol1.helloWorld();
    cout << "Test 1 - Hello World: " << result1 << endl;
    cout << "Expected: Hello, World!" << endl;
    cout << "Passed: " << (result1 == "Hello, World!") << endl;
    cout << endl;
    
    // Test 2
    Solution2 sol2;
    int result2 = sol2.addTwoNumbers(5, 3);
    cout << "Test 2 - Add Two Numbers: " << result2 << endl;
    cout << "Expected: 8" << endl;
    cout << "Passed: " << (result2 == 8) << endl;
    cout << endl;
    
    // Test 3
    Solution3 sol3;
    vector<int> nums = {1, 5, 3, 9, 2};
    int result3 = sol3.findMaximum(nums);
    cout << "Test 3 - Find Maximum: " << result3 << endl;
    cout << "Expected: 9" << endl;
    cout << "Passed: " << (result3 == 9) << endl;
    cout << endl;
    
    // Test 4
    Solution4 sol4;
    string result4 = sol4.evenOrOdd(4);
    cout << "Test 4 - Even or Odd: " << result4 << endl;
    cout << "Expected: even" << endl;
    cout << "Passed: " << (result4 == "even") << endl;
    cout << endl;
    
    cout << "=== C++ MANUAL TESTS COMPLETED ===" << endl;
    
    return 0;
}