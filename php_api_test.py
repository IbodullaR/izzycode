#!/usr/bin/env python3

import requests
import json
import time

BASE_URL = "http://localhost:8080"

def test_problem(problem_id, code, language="php", expected_status="accepted"):
    """Test a single problem"""
    url = f"{BASE_URL}/api/problems/{problem_id}/run"
    payload = {
        "code": code,
        "language": language
    }
    
    try:
        response = requests.post(url, json=payload, timeout=10)
        if response.status_code == 200:
            result = response.json()
            status = result.get('status', 'unknown')
            passed = result.get('passed', False)
            output = result.get('output', '')
            expected_output = result.get('expectedOutput', '')
            error_message = result.get('errorMessage', '')
            
            print(f"Problem {problem_id}: {status.upper()}")
            print(f"  Expected: {expected_output}")
            print(f"  Got: {output}")
            print(f"  Passed: {passed}")
            if error_message:
                print(f"  Error: {error_message}")
            print()
            
            return status == expected_status and passed
        else:
            print(f"Problem {problem_id}: HTTP {response.status_code}")
            print(f"  Response: {response.text}")
            print()
            return False
    except Exception as e:
        print(f"Problem {problem_id}: ERROR - {str(e)}")
        print()
        return False

def main():
    print("=== PHP API TESTS ===")
    print()
    
    # Test cases
    test_cases = [
        {
            "id": 1,
            "name": "Hello World",
            "code": "class Solution { public function helloWorld() { return 'Hello, World!'; } }"
        },
        {
            "id": 2,
            "name": "Add Two Numbers",
            "code": "class Solution { public function addTwoNumbers($a, $b) { return $a + $b; } }"
        },
        {
            "id": 3,
            "name": "Find Maximum",
            "code": "class Solution { public function findMaximum($nums) { return max($nums); } }"
        },
        {
            "id": 4,
            "name": "Even or Odd",
            "code": "class Solution { public function isEven($n) { return $n % 2 == 0; } }"
        },
        {
            "id": 5,
            "name": "Count Digits",
            "code": "class Solution { public function countDigits($n) { return strlen(strval(abs($n))); } }"
        },
        {
            "id": 6,
            "name": "Array Sum",
            "code": "class Solution { public function arraySum($nums) { return array_sum($nums); } }"
        },
        {
            "id": 7,
            "name": "Reverse String",
            "code": "class Solution { public function reverseString($s) { return strrev($s); } }"
        },
        {
            "id": 8,
            "name": "Find Minimum",
            "code": "class Solution { public function findMin($nums) { return min($nums); } }"
        },
        {
            "id": 9,
            "name": "Count Vowels",
            "code": "class Solution { public function countVowels($s) { $vowels = ['a', 'e', 'i', 'o', 'u']; $count = 0; for ($i = 0; $i < strlen($s); $i++) { if (in_array(strtolower($s[$i]), $vowels)) { $count++; } } return $count; } }"
        },
        {
            "id": 10,
            "name": "Factorial",
            "code": "class Solution { public function factorial($n) { if ($n <= 1) return 1; $result = 1; for ($i = 2; $i <= $n; $i++) { $result *= $i; } return $result; } }"
        }
    ]
    
    passed_count = 0
    total_count = len(test_cases)
    
    for test_case in test_cases:
        print(f"Testing {test_case['name']} (ID: {test_case['id']})...")
        success = test_problem(test_case['id'], test_case['code'])
        if success:
            passed_count += 1
        time.sleep(0.5)  # Small delay between tests
    
    print("=== SUMMARY ===")
    print(f"Total tests: {total_count}")
    print(f"Passed: {passed_count}")
    print(f"Failed: {total_count - passed_count}")
    print(f"Success rate: {(passed_count/total_count)*100:.1f}%")

if __name__ == "__main__":
    main()