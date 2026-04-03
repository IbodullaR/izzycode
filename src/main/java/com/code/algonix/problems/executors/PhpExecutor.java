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

    private static final String HELPER_FUNCTIONS =
        "function parseInputAndCall($input, $methodName, $solution) {\n" +
        "    $args = parseArgs($input);\n" +
        "    return call_user_func_array([$solution, $methodName], $args);\n" +
        "}\n" +
        "function parseInputAndCallFunc($input, $funcName) {\n" +
        "    $args = parseArgs($input);\n" +
        "    return call_user_func_array($funcName, $args);\n" +
        "}\n" +
        "function parseArgs($input) {\n" +
        "    $input = trim($input);\n" +
        "    if (strpos($input, '[') !== false && strpos($input, ']') !== false) {\n" +
        "        $parts = []; $current = ''; $inArray = false; $bracketCount = 0;\n" +
        "        for ($i = 0; $i < strlen($input); $i++) {\n" +
        "            $char = $input[$i];\n" +
        "            if ($char == '[') { $inArray = true; $bracketCount++; $current .= $char; }\n" +
        "            elseif ($char == ']') { $bracketCount--; $current .= $char; if ($bracketCount == 0) $inArray = false; }\n" +
        "            elseif ($char == ' ' && !$inArray) { if (trim($current) !== '') { $parts[] = trim($current); $current = ''; } }\n" +
        "            else { $current .= $char; }\n" +
        "        }\n" +
        "        if (trim($current) !== '') $parts[] = trim($current);\n" +
        "        return array_map('parseSingleArg', $parts);\n" +
        "    } elseif (strpos($input, ' ') !== false) {\n" +
        "        return array_map('parseSingleArg', explode(' ', $input));\n" +
        "    } else {\n" +
        "        return [parseSingleArg($input)];\n" +
        "    }\n" +
        "}\n" +
        "function parseSingleArg($part) {\n" +
        "    if (strpos($part, '[') === 0) return json_decode($part, true);\n" +
        "    if (is_numeric($part)) return strpos($part, '.') !== false ? floatval($part) : intval($part);\n" +
        "    return $part;\n" +
        "}\n" +
        "function formatOutput($result) {\n" +
        "    if (is_bool($result)) return $result ? 'true' : 'false';\n" +
        "    if (is_null($result)) return 'null';\n" +
        "    if (is_array($result)) return json_encode($result, JSON_UNESCAPED_SLASHES);\n" +
        "    if (is_float($result)) {\n" +
        "        $str = rtrim(sprintf('%.10f', $result), '0');\n" +
        "        if (substr($str, -1) === '.') $str .= '0';\n" +
        "        return $str;\n" +
        "    }\n" +
        "    return $result;\n" +
        "}\n";

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
        boolean isClass = userCode.contains("class Solution");

        // <?php va ?> tag'larini olib tashlash
        String cleanCode = userCode.trim();
        if (cleanCode.startsWith("<?php")) cleanCode = cleanCode.substring(5).trim();
        if (cleanCode.endsWith("?>")) cleanCode = cleanCode.substring(0, cleanCode.length() - 2).trim();

        StringBuilder sb = new StringBuilder();
        sb.append("<?php\n");
        sb.append(cleanCode).append("\n\n");

        if (isClass) {
            sb.append("$solution = new Solution();\n");
            sb.append("ob_start();\n");
            sb.append("$input = trim(fgets(STDIN));\n");
            sb.append("if (empty($input)) {\n");
            sb.append("    $result = $solution->").append(functionName).append("();\n");
            sb.append("} else {\n");
            sb.append("    $result = parseInputAndCall($input, '").append(functionName).append("', $solution);\n");
            sb.append("}\n");
        } else {
            sb.append("ob_start();\n");
            sb.append("$input = trim(fgets(STDIN));\n");
            sb.append("if (empty($input)) {\n");
            sb.append("    $result = ").append(functionName).append("();\n");
            sb.append("} else {\n");
            sb.append("    $result = parseInputAndCallFunc($input, '").append(functionName).append("');\n");
            sb.append("}\n");
        }

        sb.append("$captured = ob_get_clean();\n");
        sb.append("if ($captured !== '') {\n");
        sb.append("    echo trim($captured) . \"\\n\";\n");
        sb.append("} elseif ($result !== null) {\n");
        sb.append("    echo formatOutput($result) . \"\\n\";\n");
        sb.append("}\n\n");
        sb.append(HELPER_FUNCTIONS);

        return sb.toString();
    }

    /**
     * PHP funksiya nomini aniqlash
     */
    public String extractFunctionName(String code) {
        for (String line : code.split("\n")) {
            line = line.trim();
            if (line.contains("function ") && line.contains("(")) {
                for (String part : line.split("\\s+")) {
                    if (part.contains("(")) {
                        return part.split("\\(")[0];
                    }
                }
            }
        }
        return "solution";
    }
}
