// Test 1: Hello World
function helloWorld() {
    return "Hello, World!";
}

const result1 = helloWorld();
console.log("Test 1 - Hello World:", result1);
console.log("Expected: Hello, World!");
console.log("Passed:", result1 === "Hello, World!");
console.log();

// Test 2: Add Two Numbers
function addTwoNumbers(a, b) {
    return a + b;
}

const result2 = addTwoNumbers(5, 3);
console.log("Test 2 - Add Two Numbers:", result2);
console.log("Expected: 8");
console.log("Passed:", result2 === 8);
console.log();

// Test 3: Find Maximum
function findMaximum(nums) {
    return Math.max(...nums);
}

const result3 = findMaximum([1, 5, 3, 9, 2]);
console.log("Test 3 - Find Maximum:", result3);
console.log("Expected: 9");
console.log("Passed:", result3 === 9);
console.log();

// Test 4: Even or Odd
function evenOrOdd(n) {
    return n % 2 === 0 ? "even" : "odd";
}

const result4 = evenOrOdd(4);
console.log("Test 4 - Even or Odd:", result4);
console.log("Expected: even");
console.log("Passed:", result4 === "even");
console.log();

console.log("=== JAVASCRIPT MANUAL TESTS COMPLETED ===");