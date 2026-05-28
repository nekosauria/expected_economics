package com.nekosaur.ee;

import java.util.List;

public record ExpectedEconomicsDTO(
        String title,
        String note,
        double monthly_cost,
        int work_days_per_month,
        int work_hours_per_day,
        int extra_days_off,
        double success_chance,
        double if_success_earn,
        double if_fail_lose,
        List<ProjectItem> projects
) {
    public record ProjectItem(String name, double work_hours) {}
}