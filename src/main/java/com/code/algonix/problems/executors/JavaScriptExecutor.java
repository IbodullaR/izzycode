package com.code.algonix.problems.executors;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * JavaScript/Node.js kod bajaruvchi
 */
@Component
@Slf4j
public class JavaScriptExecutor {

    /**
     * JavaScript kodini bajarish
     */
    public Process executeCode(String wrappedCode, Path workDir, int timeoutSeconds) throws Exception {
        Path sourceFile = workDir.resolve("solution.js");
        Files.writeString(sourceFile, wrappedCode);
        
        ProcessBuilder pb = new ProcessBuilder("node", sourceFile.toString());
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(false);
        
        return pb.start();
    }
    
    /**
     * JavaScript funksiyasini universal wrapper bilan o'rash - Array support bilan
     */
    public String wrapFunction(String userCode, String functionName) {
        boolean isClassMethod = userCode.trim().startsWith("class ");
        
        return isClassMethod ? 
            """
            const fs = require('fs');
            const input = fs.readFileSync(0, 'utf8').trim();
            
            %s
            
            const solution = new Solution();
            
            // Input formatini aniqlash va funksiyani chaqirish
            if (input === '') {
                // No input
                const result = solution.%s();
                console.log(result);
            } else {
                // Parse input - array va boshqa parametrlarni qo'llab-quvvatlash
                const result = parseInputAndCall(input, (parsedArgs) => {
                    return solution.%s(...parsedArgs);
                });
                console.log(Array.isArray(result) ? JSON.stringify(result) : result);
            }
            
            function parseInputAndCall(input, callback) {
                try {
                    // Array format: "[1,2,3] 5" yoki "[1,2,3]"
                    if (input.includes('[') && input.includes(']')) {
                        const parts = [];
                        let current = '';
                        let inArray = false;
                        let bracketCount = 0;
                        
                        for (let i = 0; i < input.length; i++) {
                            const char = input[i];
                            if (char === '[') {
                                inArray = true;
                                bracketCount++;
                                current += char;
                            } else if (char === ']') {
                                bracketCount--;
                                current += char;
                                if (bracketCount === 0) {
                                    inArray = false;
                                }
                            } else if (char === ' ' && !inArray) {
                                if (current.trim()) {
                                    parts.push(current.trim());
                                    current = '';
                                }
                            } else {
                                current += char;
                            }
                        }
                        if (current.trim()) {
                            parts.push(current.trim());
                        }
                        
                        // Parse har bir qismni
                        const parsedArgs = parts.map(part => {
                            if (part.startsWith('[') && part.endsWith(']')) {
                                // Array parse qilish
                                return JSON.parse(part);
                            } else if (!isNaN(part)) {
                                // Number
                                return Number(part);
                            } else {
                                // String
                                return part;
                            }
                        });
                        
                        return callback(parsedArgs);
                    } else if (input.includes(' ')) {
                        // Multiple simple parameters
                        const params = input.split(' ').map(s => isNaN(s) ? s : Number(s));
                        return callback(params);
                    } else if (!isNaN(input)) {
                        // Single number
                        return callback([Number(input)]);
                    } else {
                        // Single string
                        return callback([input]);
                    }
                } catch (error) {
                    console.error('Parse error:', error);
                    return null;
                }
            }
            """.formatted(userCode, functionName, functionName) :
            """
            const fs = require('fs');
            const input = fs.readFileSync(0, 'utf8').trim();
            
            %s
            
            // Input formatini aniqlash va funksiyani chaqirish
            if (input === '') {
                // No input
                const result = %s();
                console.log(result);
            } else {
                // Parse input - array va boshqa parametrlarni qo'llab-quvvatlash
                const result = parseInputAndCall(input, (parsedArgs) => {
                    return %s(...parsedArgs);
                });
                console.log(Array.isArray(result) ? JSON.stringify(result) : result);
            }
            
            function parseInputAndCall(input, callback) {
                try {
                    // Array format: "[1,2,3] 5" yoki "[1,2,3]"
                    if (input.includes('[') && input.includes(']')) {
                        const parts = [];
                        let current = '';
                        let inArray = false;
                        let bracketCount = 0;
                        
                        for (let i = 0; i < input.length; i++) {
                            const char = input[i];
                            if (char === '[') {
                                inArray = true;
                                bracketCount++;
                                current += char;
                            } else if (char === ']') {
                                bracketCount--;
                                current += char;
                                if (bracketCount === 0) {
                                    inArray = false;
                                }
                            } else if (char === ' ' && !inArray) {
                                if (current.trim()) {
                                    parts.push(current.trim());
                                    current = '';
                                }
                            } else {
                                current += char;
                            }
                        }
                        if (current.trim()) {
                            parts.push(current.trim());
                        }
                        
                        // Parse har bir qismni
                        const parsedArgs = parts.map(part => {
                            if (part.startsWith('[') && part.endsWith(']')) {
                                // Array parse qilish
                                return JSON.parse(part);
                            } else if (!isNaN(part)) {
                                // Number
                                return Number(part);
                            } else {
                                // String
                                return part;
                            }
                        });
                        
                        return callback(parsedArgs);
                    } else if (input.includes(' ')) {
                        // Multiple simple parameters
                        const params = input.split(' ').map(s => isNaN(s) ? s : Number(s));
                        return callback(params);
                    } else if (!isNaN(input)) {
                        // Single number
                        return callback([Number(input)]);
                    } else {
                        // Single string
                        return callback([input]);
                    }
                } catch (error) {
                    console.error('Parse error:', error);
                    return null;
                }
            }
            """.formatted(userCode, functionName, functionName);
    }
    
    /**
     * JavaScript funksiya nomini aniqlash
     */
    public String extractFunctionName(String code) {
        String[] lines = code.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            
            // ES6 class method: methodName(params) { ... }
            if (line.matches("\\s*\\w+\\s*\\([^)]*\\)\\s*\\{?.*") && 
                !line.startsWith("class ") && 
                !line.startsWith("function ") &&
                !line.contains("=") &&
                line.contains("(")) {
                
                String methodName = line.split("\\(")[0].trim();
                if (!methodName.isEmpty() && !methodName.equals("constructor")) {
                    return methodName;
                }
            }
            
            // var/let/const functionName = function(...) pattern
            if ((line.startsWith("var ") || line.startsWith("let ") || line.startsWith("const ")) 
                && (line.contains("= function(") || line.contains("=>"))) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    return parts[1].replace("=", "").trim();
                }
            }
            
            // function functionName(...) pattern
            if (line.startsWith("function ") && line.contains("(")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    return parts[1].split("\\(")[0].trim();
                }
            }
        }
        
        return "solution"; // Default
    }
}