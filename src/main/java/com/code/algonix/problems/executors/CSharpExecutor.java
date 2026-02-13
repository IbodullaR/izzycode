package com.code.algonix.problems.executors;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * C# kod bajaruvchi
 */
@Component
@Slf4j
public class CSharpExecutor {

    /**
     * C# kodini bajarish
     */
    public Process executeCode(String wrappedCode, Path workDir, int timeoutSeconds) throws Exception {
        Path sourceFile = workDir.resolve("Program.cs");
        Files.writeString(sourceFile, wrappedCode);
        
        Path executableFile = workDir.resolve("program.exe");
        
        // Compile
        ProcessBuilder compileBuilder = new ProcessBuilder(
            "csc", "/out:" + executableFile.toString(), sourceFile.toString()
        );
        compileBuilder.directory(workDir.toFile());
        compileBuilder.redirectErrorStream(true);
        
        Process compileProcess = compileBuilder.start();
        boolean compileFinished = compileProcess.waitFor(10, TimeUnit.SECONDS);
        
        if (!compileFinished || compileProcess.exitValue() != 0) {
            return compileProcess; // Return compile process for error handling
        }
        
        // Execute
        ProcessBuilder pb = new ProcessBuilder(executableFile.toString());
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(false);
        
        return pb.start();
    }
    
    /**
     * C# funksiyasini universal wrapper bilan o'rash
     */
    public String wrapFunction(String userCode, String functionName) {
        return """
            using System;
            using System.Collections.Generic;
            using System.Linq;
            
            %s
            
            class Program {
                static void Main() {
                    Solution solution = new Solution();
                    
                    string input = Console.ReadLine();
                    if (string.IsNullOrEmpty(input)) {
                        // No input
                        Console.WriteLine(solution.%s());
                    } else if (input.Contains(" ")) {
                        // Multiple parameters
                        string[] parts = input.Split(' ');
                        if (parts.Length == 2) {
                            if (int.TryParse(parts[0], out int a) && int.TryParse(parts[1], out int b)) {
                                Console.WriteLine(solution.%s(a, b));
                            } else {
                                Console.WriteLine(solution.%s(parts[0], parts[1]));
                            }
                        }
                    } else {
                        // Single parameter
                        if (int.TryParse(input, out int num)) {
                            Console.WriteLine(solution.%s(num));
                        } else {
                            Console.WriteLine(solution.%s(input));
                        }
                    }
                }
            }
            """.formatted(userCode, functionName, functionName, functionName, functionName, functionName);
    }
    
    /**
     * C# funksiya nomini aniqlash
     */
    public String extractFunctionName(String code) {
        String[] lines = code.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.contains("public ") && line.contains("(") && !line.contains("class")) {
                String[] parts = line.split("\\s+");
                for (String part : parts) {
                    if (part.contains("(")) {
                        return part.split("\\(")[0];
                    }
                }
            }
        }
        
        return "Solution"; // Default for C#
    }
}