package com.code.algonix.problems.executors;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * PHP kod bajaruvchi
 */
@Component
@Slf4j
public class PhpExecutor {

    /**
     * PHP kodini bajarish
     */
    public Process executeCode(String wrappedCode, Path workDir, int timeoutSeconds) throws Exception {
        Path sourceFile = workDir.resolve("solution.php");
        Files.writeString(sourceFile, wrappedCode);
        
        ProcessBuilder pb = new ProcessBuilder("php", sourceFile.toString());
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(false);
        
        return pb.start();
    }
    
    /**
     * PHP funksiyasini universal wrapper bilan o'rash
     */
    public String wrapFunction(String userCode, String functionName) {
        return """
            <?php
            %s
            
            $solution = new Solution();
            
            $input = trim(fgets(STDIN));
            if (empty($input)) {
                // No input
                $result = $solution->%s();
                echo formatOutput($result) . "\\n";
            } else {
                // Parse input
                $result = parseInputAndCall($input, '%s', $solution);
                echo formatOutput($result) . "\\n";
            }
            
            function parseInputAndCall($input, $methodName, $solution) {
                // Array format: "[1,2,3] 5" yoki "[1,2,3]"
                if (strpos($input, '[') !== false && strpos($input, ']') !== false) {
                    $parts = [];
                    $current = '';
                    $inArray = false;
                    $bracketCount = 0;
                    
                    for ($i = 0; $i < strlen($input); $i++) {
                        $char = $input[$i];
                        if ($char == '[') {
                            $inArray = true;
                            $bracketCount++;
                            $current .= $char;
                        } elseif ($char == ']') {
                            $bracketCount--;
                            $current .= $char;
                            if ($bracketCount == 0) {
                                $inArray = false;
                            }
                        } elseif ($char == ' ' && !$inArray) {
                            if (trim($current) !== '') {
                                $parts[] = trim($current);
                                $current = '';
                            }
                        } else {
                            $current .= $char;
                        }
                    }
                    
                    if (trim($current) !== '') {
                        $parts[] = trim($current);
                    }
                    
                    // Parse har bir qismni
                    $parsedArgs = [];
                    foreach ($parts as $part) {
                        if (strpos($part, '[') === 0 && strrpos($part, ']') === strlen($part) - 1) {
                            // Array parse qilish
                            $parsedArgs[] = json_decode($part, true);
                        } elseif (strpos($part, '"') === 0 && strrpos($part, '"') === strlen($part) - 1) {
                            // String
                            $parsedArgs[] = substr($part, 1, -1);
                        } else {
                            if (is_numeric($part)) {
                                $parsedArgs[] = intval($part);
                            } else {
                                $parsedArgs[] = $part;
                            }
                        }
                    }
                    
                    return call_user_func_array([$solution, $methodName], $parsedArgs);
                } elseif (strpos($input, ' ') !== false) {
                    // Multiple simple parameters
                    $params = [];
                    foreach (explode(' ', $input) as $part) {
                        if (strpos($part, '"') === 0 && strrpos($part, '"') === strlen($part) - 1) {
                            $params[] = substr($part, 1, -1);
                        } else {
                            if (is_numeric($part)) {
                                $params[] = intval($part);
                            } else {
                                $params[] = $part;
                            }
                        }
                    }
                    return call_user_func_array([$solution, $methodName], $params);
                } else {
                    // Single parameter
                    if (strpos($input, '"') === 0 && strrpos($input, '"') === strlen($input) - 1) {
                        $param = substr($input, 1, -1);
                    } else {
                        if (is_numeric($input)) {
                            $param = intval($input);
                        } else {
                            $param = $input;
                        }
                    }
                    return call_user_func([$solution, $methodName], $param);
                }
            }
            
            function formatOutput($result) {
                if (is_bool($result)) {
                    return $result ? 'true' : 'false';
                } elseif (is_null($result)) {
                    return 'null';
                } elseif (is_array($result)) {
                    return json_encode($result, JSON_UNESCAPED_SLASHES);
                } else {
                    return $result;
                }
            }
            ?>
            """.formatted(userCode, functionName, functionName);
    }
    
    /**
     * PHP funksiya nomini aniqlash
     */
    public String extractFunctionName(String code) {
        String[] lines = code.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.contains("function ") && line.contains("(")) {
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