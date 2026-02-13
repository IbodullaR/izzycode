package com.code.algonix.problems.executors;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * C++ kod bajaruvchi
 */
@Component
@Slf4j
public class CppExecutor {

    /**
     * C++ kodini bajarish
     */
    public Process executeCode(String wrappedCode, Path workDir, int timeoutSeconds) throws Exception {
        Path sourceFile = workDir.resolve("solution.cpp");
        Files.writeString(sourceFile, wrappedCode);
        
        Path executableFile = workDir.resolve("solution.exe");
        
        // Compile
        ProcessBuilder compileBuilder = new ProcessBuilder(
            "g++", "-o", executableFile.toString(), 
            sourceFile.toString(), "-std=c++17", "-O2"
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
     * C++ funksiyasini universal wrapper bilan o'rash
     */
    public String wrapFunction(String userCode, String functionName) {
        return """
            #include <iostream>
            #include <vector>
            #include <string>
            #include <sstream>
            using namespace std;
            
            %s
            
            int main() {
                Solution solution;
                string line;
                
                if (getline(cin, line)) {
                    if (line.find(' ') != string::npos) {
                        // Multiple parameters
                        istringstream iss(line);
                        string first, second;
                        iss >> first >> second;
                        
                        try {
                            int a = stoi(first);
                            int b = stoi(second);
                            cout << solution.%s(a, b) << endl;
                        } catch (...) {
                            cout << solution.%s(first, second) << endl;
                        }
                    } else {
                        // Single parameter
                        try {
                            int num = stoi(line);
                            auto result = solution.%s(num);
                            if (typeid(result) == typeid(bool)) {
                                cout << (result ? "true" : "false") << endl;
                            } else {
                                cout << result << endl;
                            }
                        } catch (...) {
                            cout << solution.%s(line) << endl;
                        }
                    }
                } else {
                    // No input
                    cout << solution.%s() << endl;
                }
                
                return 0;
            }
            """.formatted(userCode, functionName, functionName, functionName, functionName, functionName);
    }
    
    /**
     * C++ funksiya nomini aniqlash
     */
    public String extractFunctionName(String code) {
        String[] lines = code.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if ((line.contains("public:") || line.contains("private:")) && 
                lines.length > 1) {
                // Next line should contain function
                continue;
            }
            if (line.contains("(") && !line.contains("class") && !line.contains("#include")) {
                String[] parts = line.split("\\s+");
                for (String part : parts) {
                    if (part.contains("(")) {
                        return part.split("\\(")[0];
                    }
                }
            }
        }
        
        return "solution"; // Default
    }
}