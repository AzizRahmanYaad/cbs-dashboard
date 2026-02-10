package com.example.CBS.Dashboard.controller.training;

import com.example.CBS.Dashboard.dto.training.CfoTrainingDashboardDto;
import com.example.CBS.Dashboard.service.training.CfoTrainingAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * CFO-only training analytics endpoints.
 *
 * This controller exposes a read-only, aggregated dashboard API for the CFO role.
 */
@RestController
@RequestMapping("/api/training/cfo")
@RequiredArgsConstructor
public class CfoTrainingAnalyticsController {

    private final CfoTrainingAnalyticsService cfoTrainingAnalyticsService;

    /**
     * Executive CFO dashboard.
     *
     * @param from Optional start date (ISO). Defaults to 90 days ago when omitted together with {@code to}.
     * @param to   Optional end date (ISO). Defaults to today when omitted together with {@code from}.
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ROLE_CFO')")
    public ResponseEntity<CfoTrainingDashboardDto> getDashboard(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        CfoTrainingDashboardDto dto = cfoTrainingAnalyticsService.getDashboard(from, to);
        return ResponseEntity.ok(dto);
    }
}

