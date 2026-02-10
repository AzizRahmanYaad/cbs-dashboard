package com.example.CBS.Dashboard.service.training;

import com.example.CBS.Dashboard.entity.TrainingProgram;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.repository.TrainingProgramRepository;
import com.example.CBS.Dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Centralized access control for training programs, sessions and materials.
 *
 * Rule:
 * - ROLE_ADMIN and ROLE_TRAINING_ADMIN can see all content.
 * - A user can see a program's sessions/materials if:
 *   - They are the program instructor, OR
 *   - They are enrolled in the program (active enrollment).
 */
@Service
@RequiredArgsConstructor
public class TrainingAccessService {

    private final UserRepository userRepository;
    private final TrainingProgramRepository trainingProgramRepository;

    /**
     * Throws if the given user is NOT allowed to see content for the given program.
     */
    public void assertCanAccessProgramContent(String username, Long programId) {
        if (!canAccessProgramContent(username, programId)) {
            throw new RuntimeException("Access denied: you are not assigned to or enrolled in this training program.");
        }
    }

    /**
     * Returns true if the user can see content for the given program.
     */
    public boolean canAccessProgramContent(String username, Long programId) {
        if (username == null || programId == null) {
            return false;
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Global override: admins and training admins see everything.
        if (hasAnyRole(user, "ROLE_ADMIN", "ROLE_TRAINING_ADMIN")) {
            return true;
        }

        TrainingProgram program = trainingProgramRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Training program not found"));

        // Program instructor (teacher) has access.
        if (program.getInstructor() != null && program.getInstructor().getId().equals(user.getId())) {
            return true;
        }

        // Active student enrollment provides access. Repository already ignores WITHDRAWN/CANCELLED.
        return trainingProgramRepository.findByStudentId(user.getId())
                .stream()
                .anyMatch(p -> programId.equals(p.getId()));
    }

    private boolean hasAnyRole(User user, String... roleNames) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return false;
        }
        return user.getRoles().stream()
                .map(role -> role.getName() != null ? role.getName().trim() : "")
                .anyMatch(userRole ->
                        java.util.Arrays.stream(roleNames)
                                .anyMatch(required -> required.equalsIgnoreCase(userRole)));
    }
}

