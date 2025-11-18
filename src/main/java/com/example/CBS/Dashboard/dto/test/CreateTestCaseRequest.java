package com.example.CBS.Dashboard.dto.test;

import com.example.CBS.Dashboard.entity.TestCase.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestCaseRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String preconditions;
    @NotNull(message = "Steps are required")
    private List<String> steps = new ArrayList<>();
    private String expectedResult;
    @NotNull(message = "Priority is required")
    private Priority priority;
    private Long moduleId;
    private Long assignedToId;
}

