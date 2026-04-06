package com.code.algonix.problems.executors;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * C++ kod bajaruvchi - universal input/output support bilan
 */
@Component
@Slf4j
public class CppExecutor {

    /**
     * C++ kodini compile va bajarish
     */
    public Process executeCode(String wrappedCode, Path workDir, int timeoutSeconds) throws Exception {
        Path sourceFile = workDir.resolve("solution.cpp");
        Files.writeString(sourceFile, wrappedCode);

        Path executableFile = workDir.resolve("solution.exe");

        // g++ path - MSYS2 ucrt64
        String gppPath = findGppPath();

        // Compile
        ProcessBuilder compileBuilder = new ProcessBuilder(
                gppPath, "-o", executableFile.toString(),
                sourceFile.toString(), "-std=c++17", "-O2", "-lm", "-static"
        );
        compileBuilder.directory(workDir.toFile());
        compileBuilder.redirectErrorStream(true);
        // PATH qo'shish
        compileBuilder.environment().put("PATH",
                "C:\\msys64\\ucrt64\\bin;" + System.getenv("PATH"));

        Process compileProcess = compileBuilder.start();
        boolean compileFinished = compileProcess.waitFor(30, TimeUnit.SECONDS);

        if (!compileFinished || compileProcess.exitValue() != 0) {
            log.error("C++ compile failed. Exit: {}, Finished: {}",
                compileProcess.exitValue(), compileFinished);
            return compileProcess;
        }

        log.debug("C++ compiled successfully: {}", executableFile);

        // Execute
        ProcessBuilder pb = new ProcessBuilder(executableFile.toString());
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(false);

        return pb.start();
    }

    private String findGppPath() {
        String[] candidates = {
            "C:\\msys64\\ucrt64\\bin\\g++.exe",
            "C:\\msys64\\mingw64\\bin\\g++.exe",
            "C:\\msys64\\usr\\bin\\g++.exe",
            "g++"
        };
        for (String path : candidates) {
            if (new java.io.File(path).exists()) {
                return path;
            }
        }
        return "g++"; // fallback - PATH'dan topadi
    }

    /**
     * C++ funksiyasini universal wrapper bilan o'rash
     * - int, long, double, string, vector, bool turlarini qo'llab-quvvatlaydi
     * - Bir yoki bir nechta parametrlarni qo'llab-quvvatlaydi
     * - Array/vector inputni qo'llab-quvvatlaydi
     */
    public String wrapFunction(String userCode, String functionName) {
        // return statement yo'q bo'lsa, return type'ni void'ga o'zgartirish
        String processedCode = convertToVoidIfNeeded(userCode, functionName);
        
        return """
            #include <iostream>
            #include <vector>
            #include <string>
            #include <sstream>
            #include <algorithm>
            #include <stdexcept>
            using namespace std;

            %s

            // ============ Universal Input Parser ============

            bool isNumber(const string& s) {
                if (s.empty()) return false;
                size_t start = (s[0] == '-') ? 1 : 0;
                if (start == s.size()) return false;
                for (size_t i = start; i < s.size(); i++) {
                    if (!isdigit(s[i]) && s[i] != '.') return false;
                }
                return true;
            }

            bool isDouble(const string& s) {
                return s.find('.') != string::npos && isNumber(s);
            }

            vector<string> splitTokens(const string& line) {
                vector<string> tokens;
                string current;
                bool inArray = false;
                int bracketCount = 0;
                for (char ch : line) {
                    if (ch == '[') { inArray = true; bracketCount++; current += ch; }
                    else if (ch == ']') { bracketCount--; current += ch; if (bracketCount == 0) inArray = false; }
                    else if (ch == ' ' && !inArray) {
                        if (!current.empty()) { tokens.push_back(current); current.clear(); }
                    } else { current += ch; }
                }
                if (!current.empty()) tokens.push_back(current);
                return tokens;
            }

            vector<int> parseIntArray(const string& s) {
                vector<int> result;
                string content = s.substr(1, s.size() - 2);
                if (content.empty()) return result;
                stringstream ss(content);
                string token;
                while (getline(ss, token, ',')) {
                    result.push_back(stoi(token));
                }
                return result;
            }

            vector<long long> parseLongArray(const string& s) {
                vector<long long> result;
                string content = s.substr(1, s.size() - 2);
                if (content.empty()) return result;
                stringstream ss(content);
                string token;
                while (getline(ss, token, ',')) {
                    result.push_back(stoll(token));
                }
                return result;
            }

            vector<double> parseDoubleArray(const string& s) {
                vector<double> result;
                string content = s.substr(1, s.size() - 2);
                if (content.empty()) return result;
                stringstream ss(content);
                string token;
                while (getline(ss, token, ',')) {
                    result.push_back(stod(token));
                }
                return result;
            }

            vector<string> parseStringArray(const string& s) {
                vector<string> result;
                string content = s.substr(1, s.size() - 2);
                if (content.empty()) return result;
                stringstream ss(content);
                string token;
                while (getline(ss, token, ',')) {
                    // Remove quotes if present
                    if (token.size() >= 2 && token.front() == '"' && token.back() == '"')
                        token = token.substr(1, token.size() - 2);
                    result.push_back(token);
                }
                return result;
            }

            void printResult(const auto& result) {
                if constexpr (is_same_v<decay_t<decltype(result)>, bool>) {
                    cout << (result ? "true" : "false") << endl;
                } else if constexpr (is_same_v<decay_t<decltype(result)>, vector<int>>) {
                    cout << "[";
                    for (size_t i = 0; i < result.size(); i++) {
                        if (i > 0) cout << ",";
                        cout << result[i];
                    }
                    cout << "]" << endl;
                } else if constexpr (is_same_v<decay_t<decltype(result)>, vector<long long>>) {
                    cout << "[";
                    for (size_t i = 0; i < result.size(); i++) {
                        if (i > 0) cout << ",";
                        cout << result[i];
                    }
                    cout << "]" << endl;
                } else if constexpr (is_same_v<decay_t<decltype(result)>, vector<string>>) {
                    cout << "[";
                    for (size_t i = 0; i < result.size(); i++) {
                        if (i > 0) cout << ",";
                        cout << result[i];
                    }
                    cout << "]" << endl;
                } else {
                    cout << result << endl;
                }
            }

            // ============ Main ============

            int main() {
                ios::sync_with_stdio(false);
                cin.tie(nullptr);
                Solution solution;
                string line;

                // EOF yoki bo'sh input tekshirish
                if (cin.peek() == EOF || !getline(cin, line) || line.empty()) {
                    // No input - stdout capture qilish
                    ostringstream captured;
                    streambuf* oldBuf = cout.rdbuf(captured.rdbuf());
                    try { printResult(solution.%s()); }
                    catch (...) { solution.%s(); }
                    cout.rdbuf(oldBuf);
                    string out = captured.str();
                    if (!out.empty()) cout << out;
                    else if (out.empty()) {} // void function - already printed
                    return 0;
                }

                auto tokens = splitTokens(line);

                try {
                    if (tokens.size() == 1) {
                        auto& t = tokens[0];
                        if (t[0] == '[') {
                            // Array input - try int first
                            try {
                                auto arr = parseIntArray(t);
                                printResult(solution.%s(arr));
                            } catch (...) {
                                auto arr = parseStringArray(t);
                                printResult(solution.%s(arr));
                            }
                        } else if (isDouble(t)) {
                            printResult(solution.%s(stod(t)));
                        } else if (isNumber(t)) {
                            try { printResult(solution.%s((long long)stoll(t))); }
                            catch (...) { printResult(solution.%s(stoi(t))); }
                        } else if (t == "true" || t == "false") {
                            printResult(solution.%s(t == "true"));
                        } else {
                            printResult(solution.%s(t));
                        }
                    } else if (tokens.size() == 2) {
                        auto& a = tokens[0];
                        auto& b = tokens[1];
                        bool aIsArr = !a.empty() && a[0] == '[';
                        bool bIsArr = !b.empty() && b[0] == '[';

                        if (aIsArr && !bIsArr) {
                            auto arr = parseIntArray(a);
                            if (isNumber(b)) printResult(solution.%s(arr, stoi(b)));
                            else printResult(solution.%s(arr, b));
                        } else if (aIsArr && bIsArr) {
                            auto arr1 = parseIntArray(a);
                            auto arr2 = parseIntArray(b);
                            printResult(solution.%s(arr1, arr2));
                        } else if (isNumber(a) && isNumber(b)) {
                            printResult(solution.%s(stoi(a), stoi(b)));
                        } else {
                            printResult(solution.%s(a, b));
                        }
                    } else {
                        // 3+ params - all as ints or strings
                        vector<int> intParams;
                        bool allInts = true;
                        for (auto& t : tokens) {
                            if (isNumber(t)) intParams.push_back(stoi(t));
                            else { allInts = false; break; }
                        }
                        if (allInts && tokens.size() == 3) {
                            printResult(solution.%s(intParams[0], intParams[1], intParams[2]));
                        } else {
                            cerr << "Unsupported input format: " << tokens.size() << " tokens" << endl;
                        }
                    }
                } catch (const exception& e) {
                    cerr << "Error: " << e.what() << endl;
                    return 1;
                }

                return 0;
            }
            """.formatted(
                processedCode,
                functionName, functionName,   // no-arg calls
                functionName, functionName,   // single array
                functionName,                 // double
                functionName, functionName,   // long/int
                functionName,                 // bool
                functionName,                 // string
                functionName, functionName,   // array + scalar
                functionName,                 // two arrays
                functionName,                 // two ints
                functionName,                 // two strings
                functionName                  // three ints
        );
    }

    /**
     * C++ funksiya nomini aniqlash
     */
    public String extractFunctionName(String code) {
        String[] lines = code.split("\n");
        for (String line : lines) {
            line = line.trim();
            // Skip class definitions, includes, access modifiers
            if (line.startsWith("#") || line.startsWith("//") ||
                    line.startsWith("class ") || line.contains("public:") ||
                    line.contains("private:") || line.contains("protected:")) continue;

            if (line.contains("(") && !line.contains("class") &&
                    !line.startsWith("if") && !line.startsWith("for") &&
                    !line.startsWith("while") && !line.startsWith("switch")) {

                // Match: returnType functionName(
                String[] parts = line.split("\\s+");
                for (String part : parts) {
                    if (part.contains("(") && !part.startsWith("(")) {
                        String name = part.split("\\(")[0].trim();
                        if (!name.isEmpty() && name.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                            return name;
                        }
                    }
                }
            }
        }
        return "solution";
    }
    
    /**
     * return statement yo'q bo'lsa, return type'ni void'ga o'zgartirish
     */
    private String convertToVoidIfNeeded(String code, String functionName) {
        // Agar return statement bo'lsa, o'zgartirmaslik
        if (code.contains("return ")) {
            return code;
        }
        
        // return type'ni void'ga o'zgartirish
        // Pattern: "returnType functionName(" -> "void functionName("
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(\\w+(?:<[^>]+>)?(?:\\s*\\*)?\\s+)" + java.util.regex.Pattern.quote(functionName) + "\\s*\\("
        );
        java.util.regex.Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return code.substring(0, matcher.start()) + "void " + functionName + "(" + code.substring(matcher.end());
        }
        
        return code;
    }
}
