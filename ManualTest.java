public class ManualTest {
    
    // Test 1: Hello World
    static class Solution1 {
        public String helloWorld() {
            return "Hello, World!";
        }
    }
    
    // Test 2: Add Two Numbers
    static class Solution2 {
        public int addTwoNumbers(int a, int b) {
            return a + b;
        }
    }
    
    // Test 3: Find Maximum
    static class Solution3 {
        public int findMaximum(int[] nums) {
            int max = nums[0];
            for (int num : nums) {
                if (num > max) {
                    max = num;
                }
            }
            return max;
        }
    }
    
    // Test 4: Even or Odd
    static class Solution4 {
        public String evenOrOdd(int n) {
            return n % 2 == 0 ? "even" : "odd";
        }
    }
    
    public static void main(String[] args) {
        // Test 1
        Solution1 sol1 = new Solution1();
        String result1 = sol1.helloWorld();
        System.out.println("Test 1 - Hello World: " + result1);
        System.out.println("Expected: Hello, World!");
        System.out.println("Passed: " + result1.equals("Hello, World!"));
        System.out.println();
        
        // Test 2
        Solution2 sol2 = new Solution2();
        int result2 = sol2.addTwoNumbers(5, 3);
        System.out.println("Test 2 - Add Two Numbers: " + result2);
        System.out.println("Expected: 8");
        System.out.println("Passed: " + (result2 == 8));
        System.out.println();
        
        // Test 3
        Solution3 sol3 = new Solution3();
        int[] nums = {1, 5, 3, 9, 2};
        int result3 = sol3.findMaximum(nums);
        System.out.println("Test 3 - Find Maximum: " + result3);
        System.out.println("Expected: 9");
        System.out.println("Passed: " + (result3 == 9));
        System.out.println();
        
        // Test 4
        Solution4 sol4 = new Solution4();
        String result4 = sol4.evenOrOdd(4);
        System.out.println("Test 4 - Even or Odd: " + result4);
        System.out.println("Expected: even");
        System.out.println("Passed: " + result4.equals("even"));
        System.out.println();
        
        System.out.println("=== JAVA MANUAL TESTS COMPLETED ===");
    }
}