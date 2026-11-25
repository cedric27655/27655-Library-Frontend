package kigali.clinic.rw.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.service.DashboardService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping({"/api/dashboard", "/api/dashboards"})
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // D4: GET /api/dashboard/summary
    @GetMapping("/summary")
    public Map<String, Long> getSummary() {
        return dashboardService.getDashboardSummary();
    }
}
