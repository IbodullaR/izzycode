<?php

// Test 1: Hello World
class Solution1 {
    public function helloWorld() {
        return 'Hello, World!';
    }
}

$solution1 = new Solution1();
$result1 = $solution1->helloWorld();
echo "Test 1 - Hello World: " . $result1 . "\n";
echo "Expected: Hello, World!\n";
echo "Passed: " . ($result1 === 'Hello, World!' ? 'true' : 'false') . "\n\n";

// Test 2: Add Two Numbers
class Solution2 {
    public function addTwoNumbers($a, $b) {
        return $a + $b;
    }
}

$solution2 = new Solution2();
$result2 = $solution2->addTwoNumbers(5, 3);
echo "Test 2 - Add Two Numbers: " . $result2 . "\n";
echo "Expected: 8\n";
echo "Passed: " . ($result2 === 8 ? 'true' : 'false') . "\n\n";

// Test 3: Find Maximum
class Solution3 {
    public function findMaximum($nums) {
        return max($nums);
    }
}

$solution3 = new Solution3();
$result3 = $solution3->findMaximum([1, 5, 3, 9, 2]);
echo "Test 3 - Find Maximum: " . $result3 . "\n";
echo "Expected: 9\n";
echo "Passed: " . ($result3 === 9 ? 'true' : 'false') . "\n\n";

// Test 4: Even or Odd (boolean version)
class Solution4 {
    public function isEven($n) {
        return $n % 2 == 0;
    }
}

$solution4 = new Solution4();
$result4 = $solution4->isEven(4);
echo "Test 4 - Is Even: " . ($result4 ? 'true' : 'false') . "\n";
echo "Expected: true\n";
echo "Passed: " . ($result4 === true ? 'true' : 'false') . "\n\n";

// Test 5: Array Sum
class Solution5 {
    public function arraySum($nums) {
        return array_sum($nums);
    }
}

$solution5 = new Solution5();
$result5 = $solution5->arraySum([1, 2, 3, 4, 5]);
echo "Test 5 - Array Sum: " . $result5 . "\n";
echo "Expected: 15\n";
echo "Passed: " . ($result5 === 15 ? 'true' : 'false') . "\n\n";

// Test 6: Reverse String
class Solution6 {
    public function reverseString($s) {
        return strrev($s);
    }
}

$solution6 = new Solution6();
$result6 = $solution6->reverseString("hello");
echo "Test 6 - Reverse String: " . $result6 . "\n";
echo "Expected: olleh\n";
echo "Passed: " . ($result6 === 'olleh' ? 'true' : 'false') . "\n\n";

// Test 7: Count Vowels
class Solution7 {
    public function countVowels($s) {
        $vowels = ['a', 'e', 'i', 'o', 'u'];
        $count = 0;
        for ($i = 0; $i < strlen($s); $i++) {
            if (in_array(strtolower($s[$i]), $vowels)) {
                $count++;
            }
        }
        return $count;
    }
}

$solution7 = new Solution7();
$result7 = $solution7->countVowels("hello");
echo "Test 7 - Count Vowels: " . $result7 . "\n";
echo "Expected: 2\n";
echo "Passed: " . ($result7 === 2 ? 'true' : 'false') . "\n\n";

// Test 8: Factorial
class Solution8 {
    public function factorial($n) {
        if ($n <= 1) return 1;
        $result = 1;
        for ($i = 2; $i <= $n; $i++) {
            $result *= $i;
        }
        return $result;
    }
}

$solution8 = new Solution8();
$result8 = $solution8->factorial(5);
echo "Test 8 - Factorial: " . $result8 . "\n";
echo "Expected: 120\n";
echo "Passed: " . ($result8 === 120 ? 'true' : 'false') . "\n\n";

echo "=== PHP MANUAL TESTS COMPLETED ===\n";

?>