package com.code.algonix.problems;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeExecutionServiceSelector {

    private final LeetCodeExecutionService leetCodeExecutionService;

    public CodeExecutionService.ExecutionResult executeCode(String code, String language, List<TestCase> testCases) {
        // Faqat LeetCodeExecutionService ishlatamiz - universal tizim
        return leetCodeExecutionService.executeCode(code, language, testCases);
    }

    public String getExecutionMethod() {
        return "LeetCode Style Universal (Function-only execution with auto-wrapping for any problem)";
    }
}