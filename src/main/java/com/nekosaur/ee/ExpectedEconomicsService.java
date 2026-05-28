package com.nekosaur.ee;

import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
public class ExpectedEconomicsService {

    /**
     * 核心商業邏輯：計算專案成本、EEV (期望經濟價值) 與 ROI
     *
     * @param dto 前端傳入的專案參數
     * @return 包含計算結果的 Map，將直接序列化為 JSON
     */
    public Map<String, Object> calculate(ExpectedEconomicsDTO dto) {
        // 基礎費率計算 (每小時成本、每日成本)
        double hourlyRate = dto.monthly_cost() / (dto.work_days_per_month() * dto.work_hours_per_day());

        // 工時與工期推算
        double totalHours = dto.projects().stream().mapToDouble(p -> p.work_hours()).sum();
        double totalWorkDays = totalHours / dto.work_hours_per_day();

        // 總開發成本 (總工時 * 每小時薪資成本)
        double totalCost = totalHours * hourlyRate;

        // 推算預計完成日期：
        // extra_days_off 定義為「額外不工作的日曆天（例如國定假日）」
        // 先跳過週末推算工作天結束日，再往後推 extra_days_off 天
        LocalDate today = LocalDate.now();
        LocalDate finishDate = addWorkDays(today, (long) Math.ceil(totalWorkDays));
        finishDate = finishDate.plusDays(dto.extra_days_off()); // 再加額外假日

        // 總日曆天數（含週末與額外假日）
        long totalCalendarDays = java.time.temporal.ChronoUnit.DAYS.between(today, finishDate);

        // EEV 經濟效益評估計算
        // 期望收益 = 成功率 * 成功獲利
        double expGain = (dto.success_chance() / 100.0) * dto.if_success_earn();
        // 期望損失 = (1 - 成功率) * 失敗虧損（防呆：確保 if_fail_lose 為正數）
        double expLoss = ((100 - dto.success_chance()) / 100.0) * Math.abs(dto.if_fail_lose());

        // EEV 公式：Σ(Pi × Vi) - C (期望效益 - 期望損失 - 開發成本)
        double eev = expGain - expLoss - totalCost;

        // ROI 計算 (若成本為 0 則設為 0 以避免除以零錯誤)
        double roi = (totalCost > 0) ? (eev / totalCost) * 100 : 0;

        // 使用 %.0f 來顯示浮點數但不留小數點，這樣就不用擔心類型轉換問題
        String formula = String.format(
                "EEV = (成功率 %.0f%% * 成功收益 %.0f) - (失敗率 %.0f%% * 失敗損失 %.0f) - 開發成本 %.0f = %.0f",
                dto.success_chance(),
                dto.if_success_earn(),
                (100.0 - dto.success_chance()),
                Math.abs(dto.if_fail_lose()),
                totalCost,
                eev // 這裡是最終算出來的結果
        );

        // 各工項明細計算（依序累加工作天，finish_date 也一致加上 extra_days_off）
        List<Map<String, Object>> projectDetails = new ArrayList<>();
        double accumulatedWorkDaysDouble = 0.0;  // ✅ 改用 double 累加，避免提前 ceil 造成誤差
        for (var p : dto.projects()) {
            double days = p.work_hours() / dto.work_hours_per_day();
            accumulatedWorkDaysDouble += days;   // ✅ 先累加實際天數
            LocalDate projectFinish = addWorkDays(today, (long) Math.ceil(accumulatedWorkDaysDouble))
                    .plusDays(dto.extra_days_off());  // ✅ 最後才統一 ceil
            projectDetails.add(Map.of(
                    "name", p.name(),
                    "work_hours", p.work_hours(),
                    "work_days", days,
                    "cost", Math.round(p.work_hours() * hourlyRate),
                    "finish_date", projectFinish.toString()
            ));
        }

        // 在 service 內統計
        double total_estimated_hours = dto.projects().stream()
                .mapToDouble(p -> (double) p.work_hours())
                .sum();
        double total_estimated_dates = total_estimated_hours / dto.work_hours_per_day();
        double total_estimated_cost = projectDetails.stream()
                .mapToDouble(p -> ((Number) p.get("cost")).doubleValue())
                .sum();
        String final_finish_date = projectDetails.get(projectDetails.size() - 1).get("finish_date").toString();
        Map<String, Object> summary = Map.of(
                "total_estimated_hours", total_estimated_hours,
                "total_estimated_dates", total_estimated_dates,
                "total_estimated_cost", Math.round(total_estimated_cost),
                "final_finish_date", final_finish_date,
                "stage_count", projectDetails.size()
        );


        // 4. 封裝計算結果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("title", dto.title());
        result.put("note", dto.note());
        result.put("calculated_at", today.toString());
        result.put("total_work_hours", totalHours);
        result.put("total_work_days", totalWorkDays);
        result.put("total_calendar_days", totalCalendarDays);
        result.put("estimated_finish_date", finishDate.toString());
        // 成本彙總（保留原始精度，僅輸出時四捨五入）
        result.put("cost", Map.of(
                "hourly", Math.round(hourlyRate),
                "daily", Math.round(hourlyRate * dto.work_hours_per_day()),
                "total", Math.round(totalCost)
        ));
        // EEV 矩陣數據
        result.put("eev", Map.of(
                "success_chance", dto.success_chance(),
                "if_success_earn", dto.if_success_earn(),
                "if_fail_lose", Math.abs(dto.if_fail_lose()),
                "expected_gain", Math.round(expGain),
                "expected_loss", Math.round(expLoss),
                "dev_cost", Math.round(totalCost),
                "eev_value", Math.round(eev),
                "roi_percent", Math.round(roi),
                "recommendation", eev >= 0 ? "✅ 值得投入，EEV 為正 : " + eev : "❌ 不建議投入，EEV 為負 : " + eev,
                "formula_explanation", formula
        ));
        result.put("projects", projectDetails);
        result.put("summary", summary);

        return result;
    }

    /**
     * 從指定日期開始，往後推算指定的工作天數。
     * * 此計算會自動略過週六與週日。
     * 邏輯說明：
     * 1. 以給定日期為起點。
     * 2. 每天往後加 1 天。
     * 3. 檢查該日期是否為週六或週日，若為工作日則減少剩餘天數計數。
     * 4. 當剩餘天數歸零時，即為目標完成日。
     *
     * @param from      起始基準日期（不含今日，從隔天開始算）
     * @param workDays  需要推算的工作天數（非負整數）
     * @return          推算後的工作日結束日期
     */
    private LocalDate addWorkDays(LocalDate from, long workDays) {
        LocalDate date = from;
        long remaining = workDays;

        while (remaining > 0) {
            // 每次往後推一天
            date = date.plusDays(1);

            // 若該天不是週六也不是週日，則視為工作日，剩餘計數減一
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY
                    && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                remaining--;
            }
        }
        return date;
    }
}