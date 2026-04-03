package com.code.algonix.problems.executors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * C# kod bajaruvchi - universal input/output support bilan
 */
@Component
@Slf4j
public class CSharpExecutor {

    private static final String[] DOTNET_COMMANDS = {"dotnet-script", "dotnet", "csc", "mcs"};

    /**
     * C# kodini bajarish - dotnet yoki mono bilan
     */
    public Process executeCode(String wrappedCode, Path workDir, int timeoutSeconds) throws Exception {
        Path sourceFile = workDir.resolve("Program.cs");
        Files.writeString(sourceFile, wrappedCode);

        // dotnet-script (eng oson) yoki mcs (Mono) yoki csc
        String compiler = findCompiler();

        if (compiler.equals("dotnet-script")) {
            ProcessBuilder pb = new ProcessBuilder("dotnet-script", sourceFile.toString());
            pb.directory(workDir.toFile());
            pb.redirectErrorStream(false);
            return pb.start();
        }

        if (compiler.equals("mcs")) {
            // Mono compile + run
            Path executableFile = workDir.resolve("Program.exe");
            ProcessBuilder compileBuilder = new ProcessBuilder(
                    "mcs", "-out:" + executableFile.toString(), sourceFile.toString()
            );
            compileBuilder.directory(workDir.toFile());
            compileBuilder.redirectErrorStream(true);
            Process compileProcess = compileBuilder.start();
            boolean compileFinished = compileProcess.waitFor(15, TimeUnit.SECONDS);
            if (!compileFinished || compileProcess.exitValue() != 0) {
                return compileProcess;
            }
            ProcessBuilder pb = new ProcessBuilder("mono", executableFile.toString());
            pb.directory(workDir.toFile());
            pb.redirectErrorStream(false);
            return pb.start();
        }

        if (compiler.equals("csc")) {
            // Microsoft CSC
            Path executableFile = workDir.resolve("Program.exe");
            ProcessBuilder compileBuilder = new ProcessBuilder(
                    "csc", "/out:" + executableFile.toString(), sourceFile.toString()
            );
            compileBuilder.directory(workDir.toFile());
            compileBuilder.redirectErrorStream(true);
            Process compileProcess = compileBuilder.start();
            boolean compileFinished = compileProcess.waitFor(15, TimeUnit.SECONDS);
            if (!compileFinished || compileProcess.exitValue() != 0) {
                return compileProcess;
            }
            ProcessBuilder pb = new ProcessBuilder(executableFile.toString());
            pb.directory(workDir.toFile());
            pb.redirectErrorStream(false);
            return pb.start();
        }

        throw new RuntimeException("C# compiler not found. Install dotnet-script, mono, or .NET SDK.");
    }

    private String findCompiler() {
        for (String cmd : DOTNET_COMMANDS) {
            try {
                Process p = new ProcessBuilder(cmd, "--version").start();
                p.waitFor(3, TimeUnit.SECONDS);
                if (p.exitValue() == 0) return cmd;
            } catch (Exception ignored) {}
        }
        return "mcs"; // default to mono
    }

    /**
     * C# funksiyasini universal wrapper bilan o'rash
     */
    public String wrapFunction(String userCode, String functionName) {
        return """
            using System;
            using System.Collections.Generic;
            using System.Linq;
            using System.Text.Json;

            %s

            class Program {
                static void Main() {
                    Solution solution = new Solution();
                    string input = Console.ReadLine() ?? "";
                    input = input.Trim();

                    if (string.IsNullOrEmpty(input)) {
                        // No input
                        try {
                            var r = InvokeNoArg(solution);
                            Console.WriteLine(FormatOutput(r));
                        } catch { }
                        return;
                    }

                    var tokens = SplitTokens(input);

                    try {
                        if (tokens.Count == 1) {
                            string t = tokens[0];
                            if (t.StartsWith("[")) {
                                // Array input
                                var arr = ParseIntArray(t);
                                Console.WriteLine(FormatOutput(solution.%s(arr)));
                            } else if (t == "true" || t == "false") {
                                Console.WriteLine(FormatOutput(solution.%s(t == "true")));
                            } else if (long.TryParse(t, out long lv)) {
                                try { Console.WriteLine(FormatOutput(solution.%s((int)lv))); }
                                catch { Console.WriteLine(FormatOutput(solution.%s(lv))); }
                            } else if (double.TryParse(t, out double dv)) {
                                Console.WriteLine(FormatOutput(solution.%s(dv)));
                            } else {
                                Console.WriteLine(FormatOutput(solution.%s(t)));
                            }
                        } else if (tokens.Count == 2) {
                            string a = tokens[0], b = tokens[1];
                            bool aArr = a.StartsWith("["), bArr = b.StartsWith("[");
                            if (aArr && !bArr && int.TryParse(b, out int bi)) {
                                Console.WriteLine(FormatOutput(solution.%s(ParseIntArray(a), bi)));
                            } else if (aArr && bArr) {
                                Console.WriteLine(FormatOutput(solution.%s(ParseIntArray(a), ParseIntArray(b))));
                            } else if (int.TryParse(a, out int ai) && int.TryParse(b, out int bi2)) {
                                Console.WriteLine(FormatOutput(solution.%s(ai, bi2)));
                            } else {
                                Console.WriteLine(FormatOutput(solution.%s(a, b)));
                            }
                        } else {
                            // 3+ int params
                            var ints = tokens.Select(t => int.Parse(t)).ToArray();
                            if (tokens.Count == 3)
                                Console.WriteLine(FormatOutput(solution.%s(ints[0], ints[1], ints[2])));
                        }
                    } catch (Exception ex) {
                        Console.Error.WriteLine("Error: " + ex.Message);
                    }
                }

                static object InvokeNoArg(Solution s) {
                    var m = typeof(Solution).GetMethod("%s");
                    return m?.Invoke(s, null);
                }

                static List<string> SplitTokens(string line) {
                    var tokens = new List<string>();
                    string current = "";
                    bool inArray = false;
                    int bracketCount = 0;
                    foreach (char ch in line) {
                        if (ch == '[') { inArray = true; bracketCount++; current += ch; }
                        else if (ch == ']') { bracketCount--; current += ch; if (bracketCount == 0) inArray = false; }
                        else if (ch == ' ' && !inArray) {
                            if (!string.IsNullOrEmpty(current)) { tokens.Add(current); current = ""; }
                        } else { current += ch; }
                    }
                    if (!string.IsNullOrEmpty(current)) tokens.Add(current);
                    return tokens;
                }

                static int[] ParseIntArray(string s) {
                    string content = s.Substring(1, s.Length - 2);
                    if (string.IsNullOrEmpty(content)) return Array.Empty<int>();
                    return content.Split(',').Select(x => int.Parse(x.Trim())).ToArray();
                }

                static string FormatOutput(object result) {
                    if (result == null) return "null";
                    if (result is bool b) return b.ToString().ToLower();
                    if (result is int[] arr) return "[" + string.Join(",", arr) + "]";
                    if (result is long[] larr) return "[" + string.Join(",", larr) + "]";
                    if (result is double[] darr) return "[" + string.Join(",", darr) + "]";
                    if (result is string[] sarr) return "[" + string.Join(",", sarr) + "]";
                    if (result is IList<int> list) return "[" + string.Join(",", list) + "]";
                    if (result is IList<string> slist) return "[" + string.Join(",", slist) + "]";
                    return result.ToString();
                }
            }
            """.formatted(
                userCode,
                functionName, functionName, functionName, functionName,
                functionName, functionName,
                functionName, functionName, functionName, functionName,
                functionName,
                functionName
        );
    }

    /**
     * C# funksiya nomini aniqlash
     */
    public String extractFunctionName(String code) {
        String[] lines = code.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("//") || line.startsWith("class ") ||
                    line.startsWith("using ") || line.contains("public class")) continue;

            if (line.contains("public ") && line.contains("(") && !line.contains("class")) {
                String[] parts = line.trim().split("\\s+");
                for (String part : parts) {
                    if (part.contains("(") && !part.startsWith("(")) {
                        String name = part.split("\\(")[0].trim();
                        if (!name.isEmpty() && name.matches("[a-zA-Z_][a-zA-Z0-9_]*")
                                && !name.equals("public") && !name.equals("void")) {
                            return name;
                        }
                    }
                }
            }
        }
        return "Solution";
    }
}
