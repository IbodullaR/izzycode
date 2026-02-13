package com.code.algonix.problems.executors;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Python kod bajaruvchi
 */
@Component
@Slf4j
public class PythonExecutor {

    /**
     * Python kodini bajarish
     */
    public Process executeCode(String wrappedCode, Path workDir, int timeoutSeconds) throws Exception {
        Path sourceFile = workDir.resolve("solution.py");
        Files.writeString(sourceFile, wrappedCode);
        
        ProcessBuilder pb = new ProcessBuilder("python", sourceFile.toString());
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(false);
        
        return pb.start();
    }
    
    /**
     * Python funksiyasini universal wrapper bilan o'rash - Array support bilan
     */
    public String wrapFunction(String userCode, String functionName) {
        // User kodini Solution class ichiga o'rash
        String wrappedUserCode = wrapUserCodeInSolutionClass(userCode);
        
        return """
            import sys
            import json
            
            %s
            
            solution = Solution()
            
            def parse_input_and_call(input_line, method_name):
                try:
                    # Array format: "[1,2,3] 5" yoki "[1,2,3]"
                    if '[' in input_line and ']' in input_line:
                        parts = []
                        current = ''
                        in_array = False
                        bracket_count = 0
                        
                        for char in input_line:
                            if char == '[':
                                in_array = True
                                bracket_count += 1
                                current += char
                            elif char == ']':
                                bracket_count -= 1
                                current += char
                                if bracket_count == 0:
                                    in_array = False
                            elif char == ' ' and not in_array:
                                if current.strip():
                                    parts.append(current.strip())
                                    current = ''
                            else:
                                current += char
                        
                        if current.strip():
                            parts.append(current.strip())
                        
                        # Parse har bir qismni
                        parsed_args = []
                        for part in parts:
                            if part.startswith('[') and part.endswith(']'):
                                # Array parse qilish
                                parsed_args.append(json.loads(part))
                            elif part.startswith('"') and part.endswith('"'):
                                # String
                                parsed_args.append(part[1:-1])
                            else:
                                try:
                                    # Number
                                    parsed_args.append(int(part))
                                except ValueError:
                                    # String without quotes
                                    parsed_args.append(part)
                        
                        return getattr(solution, method_name)(*parsed_args)
                    elif ' ' in input_line:
                        # Multiple simple parameters
                        params = []
                        for part in input_line.split():
                            if part.startswith('"') and part.endswith('"'):
                                params.append(part[1:-1])
                            else:
                                try:
                                    params.append(int(part))
                                except ValueError:
                                    params.append(part)
                        return getattr(solution, method_name)(*params)
                    else:
                        # Single parameter
                        if input_line.startswith('"') and input_line.endswith('"'):
                            param = input_line[1:-1]
                        else:
                            try:
                                param = int(input_line)
                            except ValueError:
                                param = input_line
                        return getattr(solution, method_name)(param)
                except Exception as e:
                    print(f"Parse error: {e}", file=sys.stderr)
                    return None
            
            def format_output(result):
                if result is None:
                    return "null"
                elif isinstance(result, bool):
                    return str(result).lower()
                elif isinstance(result, str):
                    return result  # String'larni qo'shtirnoqsiz qaytarish
                elif isinstance(result, list):
                    return json.dumps(result, separators=(',', ':'))
                else:
                    return str(result)
            
            try:
                input_line = input().strip()
                result = parse_input_and_call(input_line, '%s')
                print(format_output(result))
            except EOFError:
                # No input
                result = getattr(solution, '%s')()
                print(format_output(result))
            """.formatted(wrappedUserCode, functionName, functionName);
    }
    
    /**
     * User kodini Solution class ichiga o'rash
     */
    private String wrapUserCodeInSolutionClass(String userCode) {
        // Agar user kodi allaqachon class ichida bo'lsa, uni o'zgartirmaslik
        if (userCode.contains("class Solution") || userCode.contains("class ")) {
            return userCode;
        }
        
        // Aks holda, Solution class ichiga o'rash - har bir qatorni 4 ta space bilan indent qilish
        String[] lines = userCode.split("\n");
        StringBuilder indentedCode = new StringBuilder();
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                indentedCode.append("    ").append(line).append("\n");
            } else {
                indentedCode.append("\n");
            }
        }
        
        return """
            class Solution:
            %s""".formatted(indentedCode.toString());
    }
    
    /**
     * Python funksiya nomini aniqlash
     */
    public String extractFunctionName(String code) {
        String[] lines = code.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("def ") && line.contains("(")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    return parts[1].split("\\(")[0].trim();
                }
            }
        }
        
        return "solution"; // Default
    }
}