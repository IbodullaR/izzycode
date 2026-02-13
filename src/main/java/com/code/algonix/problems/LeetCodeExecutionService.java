package com.code.algonix.problems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.code.algonix.problems.executors.JavaExecutor;
import com.code.algonix.problems.executors.JavaScriptExecutor;
import com.code.algonix.problems.executors.PhpExecutor;
import com.code.algonix.problems.executors.PythonExecutor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * LeetCode-style universal kod bajarish tizimi
 * - Har qanday til uchun universal wrapper
 * - Har qanday masala uchun ishlaydi
 * - Modular executor tizimi
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LeetCodeExecutionService implements CodeExecutionService {

    @Value("${judge.timeout-seconds:5}")
    private int timeoutSeconds;
    
    @Value("${judge.max-output-size:10240}")
    private int maxOutputSize;

    // Language executors - JavaScript, Java, Python va PHP
    private final JavaScriptExecutor javaScriptExecutor;
    private final JavaExecutor javaExecutor;
    private final PythonExecutor pythonExecutor;
    private final PhpExecutor phpExecutor;

    @Override
    public ExecutionResult executeCode(String code, String language, List<TestCase> testCases) {
        log.info("Starting universal execution for language: {}", language);
        
        // Kod validatsiya
        if (code == null || code.trim().isEmpty()) {
            return createErrorResult(ExecutionStatus.COMPILE_ERROR, "Code is empty");
        }
        
        if (code.length() > 50000) {
            return createErrorResult(ExecutionStatus.COMPILE_ERROR, "Code too long (max 50KB)");
        }
        
        // Funksiya kodini wrap qilish
        String wrappedCode = wrapFunctionCode(code, language);
        if (wrappedCode == null) {
            return createErrorResult(ExecutionStatus.COMPILE_ERROR, "Unsupported language: " + language);
        }
        
        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("leetcode-");
            log.debug("Created work directory: {}", workDir);
            
            return executeWithLanguage(wrappedCode, language, testCases, workDir);
            
        } catch (Exception e) {
            log.error("Execution error", e);
            return createErrorResult(ExecutionStatus.RUNTIME_ERROR, "Internal error: " + e.getMessage());
        } finally {
            if (workDir != null) {
                cleanupDirectory(workDir);
            }
        }
    }
    
    /**
     * Tilga qarab kod bajarish - JavaScript va Java
     */
    private ExecutionResult executeWithLanguage(String code, String language, List<TestCase> testCases, Path workDir) throws Exception {
        return switch (language.toLowerCase()) {
            case "javascript", "js", "node", "nodejs" -> executeWithExecutor(code, testCases, workDir, javaScriptExecutor::executeCode);
            case "java" -> executeWithExecutor(code, testCases, workDir, javaExecutor::executeCode);
            case "python", "py", "python3" -> executeWithExecutor(code, testCases, workDir, pythonExecutor::executeCode);
            case "php" -> executeWithExecutor(code, testCases, workDir, phpExecutor::executeCode);
            default -> createErrorResult(ExecutionStatus.COMPILE_ERROR, "Unsupported language: " + language + " (JavaScript, Java, Python and PHP supported)");
        };
    }
    
    /**
     * Executor bilan test case'larni bajarish
     */
    private ExecutionResult executeWithExecutor(String code, List<TestCase> testCases, Path workDir, 
                                              LanguageExecutor executor) throws Exception {
        List<TestCaseResult> results = new ArrayList<>();
        int passedCount = 0;
        long totalRuntime = 0;
        double totalMemory = 0.0;
        
        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            log.debug("Running test case {}/{}: input={}", i + 1, testCases.size(), testCase.getInput());
            
            long startTime = System.currentTimeMillis();
            
            try {
                Process process = executor.execute(code, workDir, timeoutSeconds);
                
                // Input berish
                try (PrintWriter writer = new PrintWriter(process.getOutputStream())) {
                    writer.print(testCase.getInput());
                    writer.flush();
                }
                
                // Timeout bilan kutish
                boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
                long runtime = System.currentTimeMillis() - startTime;
                
                if (!finished) {
                    process.destroyForcibly();
                    TestCaseResult result = TestCaseResult.builder()
                        .testCaseId(testCase.getId())
                        .status(ExecutionStatus.TIME_LIMIT_EXCEEDED)
                        .passed(false)
                        .input(testCase.getInput())
                        .expectedOutput(testCase.getExpectedOutput())
                        .actualOutput("")
                        .errorMessage(String.format("Time Limit Exceeded: %dms > %ds", runtime, timeoutSeconds))
                        .runtime((int) runtime)
                        .memory(0.0)
                        .build();
                    results.add(result);
                    break; // LeetCode style: stop on first failure
                }
                
                if (process.exitValue() != 0) {
                    String errorOutput = readProcessOutput(process.getErrorStream());
                    TestCaseResult result = TestCaseResult.builder()
                        .testCaseId(testCase.getId())
                        .status(ExecutionStatus.RUNTIME_ERROR)
                        .passed(false)
                        .input(testCase.getInput())
                        .expectedOutput(testCase.getExpectedOutput())
                        .actualOutput("")
                        .errorMessage("Runtime Error:\n" + errorOutput)
                        .runtime((int) runtime)
                        .memory(0.0)
                        .build();
                    results.add(result);
                    break; // LeetCode style: stop on first failure
                }
                
                String actualOutput = readProcessOutput(process.getInputStream());
                
                if (actualOutput.length() > maxOutputSize) {
                    TestCaseResult result = TestCaseResult.builder()
                        .testCaseId(testCase.getId())
                        .status(ExecutionStatus.RUNTIME_ERROR)
                        .passed(false)
                        .input(testCase.getInput())
                        .expectedOutput(testCase.getExpectedOutput())
                        .actualOutput(actualOutput.substring(0, Math.min(100, actualOutput.length())) + "...")
                        .errorMessage("Output Limit Exceeded")
                        .runtime((int) runtime)
                        .memory(0.0)
                        .build();
                    results.add(result);
                    break; // LeetCode style: stop on first failure
                }
                
                String expected = testCase.getExpectedOutput().trim();
                String actual = actualOutput.trim();
                double memoryUsage = Math.random() * 20 + 10; // Simulate memory usage
                
                if (compareOutputs(expected, actual)) {
                    passedCount++;
                    totalRuntime += runtime;
                    totalMemory += memoryUsage;
                    
                    TestCaseResult result = TestCaseResult.builder()
                        .testCaseId(testCase.getId())
                        .status(ExecutionStatus.ACCEPTED)
                        .passed(true)
                        .input(testCase.getInput())
                        .expectedOutput(expected)
                        .actualOutput(actual)
                        .errorMessage(null)
                        .runtime((int) runtime)
                        .memory(memoryUsage)
                        .build();
                    results.add(result);
                } else {
                    TestCaseResult result = TestCaseResult.builder()
                        .testCaseId(testCase.getId())
                        .status(ExecutionStatus.WRONG_ANSWER)
                        .passed(false)
                        .input(testCase.getInput())
                        .expectedOutput(expected)
                        .actualOutput(actual)
                        .errorMessage("Wrong Answer")
                        .runtime((int) runtime)
                        .memory(memoryUsage)
                        .build();
                    results.add(result);
                    break; // LeetCode style: stop on first failure
                }
                
            } catch (Exception e) {
                long runtime = System.currentTimeMillis() - startTime;
                TestCaseResult result = TestCaseResult.builder()
                    .testCaseId(testCase.getId())
                    .status(ExecutionStatus.RUNTIME_ERROR)
                    .passed(false)
                    .input(testCase.getInput())
                    .expectedOutput(testCase.getExpectedOutput())
                    .actualOutput("")
                    .errorMessage("Execution Error: " + e.getMessage())
                    .runtime((int) runtime)
                    .memory(0.0)
                    .build();
                results.add(result);
                break; // LeetCode style: stop on first failure
            }
        }
        
        ExecutionStatus finalStatus = passedCount == testCases.size() ? 
            ExecutionStatus.ACCEPTED : 
            (results.isEmpty() ? ExecutionStatus.RUNTIME_ERROR : results.get(results.size() - 1).getStatus());
            
        int avgRuntime = results.isEmpty() ? 0 : (int) (totalRuntime / results.size());
        double avgMemory = results.isEmpty() ? 0.0 : totalMemory / results.size();
        
        return ExecutionResult.builder()
            .status(finalStatus)
            .testResults(results)
            .totalTestCases(testCases.size())
            .passedTestCases(passedCount)
            .averageRuntime(avgRuntime)
            .averageMemory(avgMemory)
            .errorMessage(finalStatus == ExecutionStatus.ACCEPTED ? null : 
                String.format("Test case %d/%d failed", results.size(), testCases.size()))
            .build();
    }
    
    /**
     * Funksiya kodini wrap qilish - JavaScript va Java
     */
    private String wrapFunctionCode(String userCode, String language) {
        String functionName = extractFunctionName(userCode, language);
        if (functionName == null) {
            functionName = "solution"; // Default function name
        }
        
        return switch (language.toLowerCase()) {
            case "javascript", "js", "node", "nodejs" -> javaScriptExecutor.wrapFunction(userCode, functionName);
            case "java" -> javaExecutor.wrapFunction(userCode, functionName);
            case "python", "py", "python3" -> pythonExecutor.wrapFunction(userCode, functionName);
            case "php" -> phpExecutor.wrapFunction(userCode, functionName);
            default -> null;
        };
    }
    
    /**
     * Funksiya nomini aniqlash - JavaScript va Java
     */
    private String extractFunctionName(String code, String language) {
        return switch (language.toLowerCase()) {
            case "javascript", "js", "node", "nodejs" -> javaScriptExecutor.extractFunctionName(code);
            case "java" -> javaExecutor.extractFunctionName(code);
            case "python", "py", "python3" -> pythonExecutor.extractFunctionName(code);
            case "php" -> phpExecutor.extractFunctionName(code);
            default -> "solution";
        };
    }
    
    /**
     * Output'larni taqqoslash
     */
    private boolean compareOutputs(String expected, String actual) {
        return expected.equals(actual);
    }
    
    /**
     * Process output'ini o'qish
     */
    private String readProcessOutput(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString().trim();
    }
    
    /**
     * Xato natijasini yaratish
     */
    private ExecutionResult createErrorResult(ExecutionStatus status, String message) {
        return ExecutionResult.builder()
            .status(status)
            .errorMessage(message)
            .testResults(List.of())
            .totalTestCases(0)
            .passedTestCases(0)
            .averageRuntime(0)
            .averageMemory(0.0)
            .build();
    }
    
    /**
     * Papkani tozalash
     */
    private void cleanupDirectory(Path workDir) {
        try {
            Files.walk(workDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("Failed to delete: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.warn("Failed to cleanup directory: {}", workDir, e);
        }
    }
    
    /**
     * Language executor interface
     */
    @FunctionalInterface
    private interface LanguageExecutor {
        Process execute(String code, Path workDir, int timeoutSeconds) throws Exception;
    }
}