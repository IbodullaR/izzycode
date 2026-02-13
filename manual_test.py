#!/usr/bin/env python3

# Test 1: Hello World
class Solution:
    def hello_world(self) -> str:
        return 'Hello, World!'

solution = Solution()
result = solution.hello_world()
print(f"Test 1 - Hello World: {result}")
print(f"Expected: Hello, World!")
print(f"Passed: {result == 'Hello, World!'}")
print()

# Test 2: Add Two Numbers
class Solution2:
    def add_two_numbers(self, a: int, b: int) -> int:
        return a + b

solution2 = Solution2()
result2 = solution2.add_two_numbers(5, 3)
print(f"Test 2 - Add Two Numbers: {result2}")
print(f"Expected: 8")
print(f"Passed: {result2 == 8}")
print()

# Test 3: Find Maximum
class Solution3:
    def find_maximum(self, nums: list) -> int:
        return max(nums)

solution3 = Solution3()
result3 = solution3.find_maximum([1, 5, 3, 9, 2])
print(f"Test 3 - Find Maximum: {result3}")
print(f"Expected: 9")
print(f"Passed: {result3 == 9}")
print()

# Test 4: Even or Odd
class Solution4:
    def even_or_odd(self, n: int) -> str:
        return "even" if n % 2 == 0 else "odd"

solution4 = Solution4()
result4 = solution4.even_or_odd(4)
print(f"Test 4 - Even or Odd: {result4}")
print(f"Expected: even")
print(f"Passed: {result4 == 'even'}")
print()

print("=== PYTHON MANUAL TESTS COMPLETED ===")