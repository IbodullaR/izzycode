package com.code.algonix.problems.executors;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Java kod bajaruvchi
 */
@Component
@Slf4j
public class JavaExecutor {

    /**
     * Java kodini bajarish
     */
    public Process executeCode(String wrappedCode, Path workDir, int timeoutSeconds) throws Exception {
        Path sourceFile = workDir.resolve("Main.java");
        Files.writeString(sourceFile, wrappedCode);
        
        // Compile
        ProcessBuilder compileBuilder = new ProcessBuilder("javac", sourceFile.toString());
        compileBuilder.directory(workDir.toFile());
        compileBuilder.redirectErrorStream(true);
        
        Process compileProcess = compileBuilder.start();
        boolean compileFinished = compileProcess.waitFor(10, TimeUnit.SECONDS);
        
        if (!compileFinished || compileProcess.exitValue() != 0) {
            return compileProcess; // Return compile process for error handling
        }
        
        // Execute
        ProcessBuilder pb = new ProcessBuilder("java", "-cp", workDir.toString(), "Main");
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(false);
        
        return pb.start();
    }
    
    /**
     * Java funksiyasini universal wrapper bilan o'rash - Array support bilan
     */
    public String wrapFunction(String userCode, String functionName) {
        // User kodini Solution class ichiga o'rash
        String wrappedUserCode = wrapUserCodeInSolutionClass(userCode);
        
        return """
            import java.util.*;
            import java.util.stream.*;
            
            %s
            
            public class Main {
                public static void main(String[] args) {
                    Scanner sc = new Scanner(System.in);
                    Solution solution = new Solution();
                    
                    if (!sc.hasNext()) {
                        // No input
                        Object result = callMethod(solution, "%s", new Object[0]);
                        System.out.println(formatOutput(result));
                    } else {
                        String line = sc.nextLine().trim();
                        Object result = parseInputAndCall(line, solution, "%s");
                        System.out.println(formatOutput(result));
                    }
                    sc.close();
                }
                
                private static Object parseInputAndCall(String input, Solution solution, String methodName) {
                    try {
                        // Array format: "[1,2,3] 5" yoki "[1,2,3]"
                        if (input.contains("[") && input.contains("]")) {
                            List<Object> params = new ArrayList<>();
                            StringBuilder current = new StringBuilder();
                            boolean inArray = false;
                            int bracketCount = 0;
                            
                            for (int i = 0; i < input.length(); i++) {
                                char ch = input.charAt(i);
                                if (ch == '[') {
                                    inArray = true;
                                    bracketCount++;
                                    current.append(ch);
                                } else if (ch == ']') {
                                    bracketCount--;
                                    current.append(ch);
                                    if (bracketCount == 0) {
                                        inArray = false;
                                    }
                                } else if (ch == ' ' && !inArray) {
                                    if (current.length() > 0) {
                                        params.add(parseParameter(current.toString().trim()));
                                        current = new StringBuilder();
                                    }
                                } else {
                                    current.append(ch);
                                }
                            }
                            if (current.length() > 0) {
                                params.add(parseParameter(current.toString().trim()));
                            }
                            
                            return callMethod(solution, methodName, params.toArray());
                        } else if (input.contains(" ")) {
                            // Multiple simple parameters
                            String[] parts = input.split(" ");
                            Object[] params = new Object[parts.length];
                            for (int i = 0; i < parts.length; i++) {
                                params[i] = parseParameter(parts[i]);
                            }
                            return callMethod(solution, methodName, params);
                        } else {
                            // Single parameter
                            Object param = parseParameter(input);
                            return callMethod(solution, methodName, new Object[]{param});
                        }
                    } catch (Exception e) {
                        System.err.println("Parse error: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                }
                
                private static Object parseParameter(String param) {
                    if (param.startsWith("[") && param.endsWith("]")) {
                        // Array parse qilish - oddiy JSON parser
                        String content = param.substring(1, param.length() - 1);
                        if (content.trim().isEmpty()) {
                            return new int[0];
                        }
                        String[] elements = content.split(",");
                        int[] array = new int[elements.length];
                        for (int i = 0; i < elements.length; i++) {
                            array[i] = Integer.parseInt(elements[i].trim());
                        }
                        return array;
                    } else {
                        try {
                            return Integer.parseInt(param);
                        } catch (NumberFormatException e) {
                            // String bo'lishi mumkin - qo'shtirnoqlarni olib tashlash
                            String cleaned = param;
                            if (cleaned.startsWith("\\\"") && cleaned.endsWith("\\\"")) {
                                cleaned = cleaned.substring(1, cleaned.length() - 1);
                            }
                            return cleaned;
                        }
                    }
                }
                
                private static Object callMethod(Solution solution, String methodName, Object[] params) {
                    try {
                        Class<?> clazz = solution.getClass();
                        
                        // Method signature'larni sinab ko'rish
                        if (params.length == 0) {
                            return clazz.getMethod(methodName).invoke(solution);
                        } else if (params.length == 1) {
                            if (params[0] instanceof int[]) {
                                return clazz.getMethod(methodName, int[].class).invoke(solution, params[0]);
                            } else if (params[0] instanceof Integer) {
                                return clazz.getMethod(methodName, int.class).invoke(solution, params[0]);
                            } else {
                                return clazz.getMethod(methodName, String.class).invoke(solution, params[0]);
                            }
                        } else if (params.length == 2) {
                            if (params[0] instanceof int[] && params[1] instanceof Integer) {
                                return clazz.getMethod(methodName, int[].class, int.class).invoke(solution, params[0], params[1]);
                            } else if (params[0] instanceof int[] && params[1] instanceof int[]) {
                                // Ikki array parametr (masalan: findMedianSortedArrays)
                                return clazz.getMethod(methodName, int[].class, int[].class).invoke(solution, params[0], params[1]);
                            } else if (params[0] instanceof Integer && params[1] instanceof Integer) {
                                return clazz.getMethod(methodName, int.class, int.class).invoke(solution, params[0], params[1]);
                            } else if (params[0] instanceof String && params[1] instanceof String) {
                                // Ikki String parametr (masalan: isMatch)
                                return clazz.getMethod(methodName, String.class, String.class).invoke(solution, params[0], params[1]);
                            }
                        }
                        
                        return null;
                    } catch (Exception e) {
                        System.err.println("Method call error: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                }
                
                private static String formatOutput(Object result) {
                    if (result == null) return "null";
                    if (result instanceof int[]) {
                        int[] arr = (int[]) result;
                        return "[" + Arrays.stream(arr).mapToObj(String::valueOf).collect(Collectors.joining(",")) + "]";
                    } else if (result instanceof boolean[]) {
                        boolean[] arr = (boolean[]) result;
                        StringBuilder sb = new StringBuilder("[");
                        for (int i = 0; i < arr.length; i++) {
                            if (i > 0) sb.append(",");
                            sb.append(arr[i] ? "true" : "false");
                        }
                        sb.append("]");
                        return sb.toString();
                    } else if (result instanceof Boolean) {
                        return result.toString().toLowerCase();
                    } else if (result instanceof String) {
                        return result.toString();  // String'larni qo'shtirnoqsiz qaytarish
                    } else {
                        return result.toString();
                    }
                }
            }
            """.formatted(wrappedUserCode, functionName, functionName);
    }
    
    /**
     * Java funksiya nomini aniqlash
     */
    public String extractFunctionName(String code) {
        // Regex pattern bilan method nomini topish - return type'da array bo'lishi mumkin
        Pattern pattern = Pattern.compile("public\\s+[\\w\\[\\]]+\\s+(\\w+)\\s*\\(");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Agar public bo'lmasa, boshqa access modifier'larni ham qidirish
        pattern = Pattern.compile("(private|protected)\\s+[\\w\\[\\]]+\\s+(\\w+)\\s*\\(");
        matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(2);
        }
        
        return "solution"; // Default
    }
    
    /**
     * User kodini Solution class ichiga o'rash
     */
    private String wrapUserCodeInSolutionClass(String userCode) {
        // Agar user kodi allaqachon class ichida bo'lsa, uni o'zgartirmaslik
        if (userCode.contains("class Solution") || userCode.contains("public class")) {
            return userCode;
        }
        
        // Aks holda, Solution class ichiga o'rash
        return """
            class Solution {
                %s
            }
            """.formatted(userCode);
    }
}