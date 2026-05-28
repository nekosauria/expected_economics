# 💰 人工成本計算器（Java 25 LTS + Spring Boot + Thymeleaf）

## 📌 專案簡介

本專案使用 **Java 25 LTS + Spring Boot + Thymeleaf**，
提供一個簡單的 Web Demo 與 REST API，用來計算軟體工程人力成本。

目標是讓開發者、PM、或 HR 能快速估算：

- 人力成本（依工時 / 人月）
- 專案開發成本
- 可行性評估

---

## ⚙️ 技術架構

- Java 25 LTS
- Spring Boot
- Spring MVC (REST API)
- Thymeleaf (Web UI)
- Maven
- Bootstrap

---

# 📊 期望值（Expected Value, EV）公式
後端根據上述實作 expected economics 公式
## EV = Σ (Pi × Vi) - C


## 🚀 功能說明
提供 REST API 與 Web 輸入兩種選項

### 2️⃣ REST API
輸入 JSON Sample 
```
{
  // ── 人力基本資訊 ──────────────────────────
  "title": "影像辨識開發/RD",          // 職稱（選填）
  "note": "測試 API",                  // 備註（選填）

  // ── 成本設定 ──────────────────────────────
  "monthly_cost": 100000,              // 一個月的成本 NT$, ( e.g. 自行換算後專案投入人總月薪, 固定成本 )
  // ── 工時設定 ──────────────────────────────
  "work_days_per_month": 22,           // 每月工作天
  "work_hours_per_day": 8,             // 每天工作小時
  "extra_days_off": 0,                 // 額外假期（國定假日等，整個專案期間）

  // ── EEV 經濟效益評估 ───────────────────────
  "success_chance": 50,                // 成功機率（0~100）
  "if_success_earn": 300000,           // 成功的話，預期可以賺多少（NT$）
  "if_fail_lose": 60000,               // 失敗的話，預期會損失多少（NT$）

  // ── 專案工項 ──────────────────────────────
  "projects": [
    {
      "name": "需求分析",               // 工項名稱
      "work_hours": 16                 // 這個工項的工作工時（小時）
    },
    {
      "name": "開發",
      "work_hours": 40
    },
    {
      "name": "測試",
      "work_hours": 8
    },
    {
      "name": "部署",
      "work_hours": 16
    },
    {
      "name": "維護",
      "work_hours": 40
    }
  ]
}
```

用 Expected Economic 經濟效益評估矩陣寫一個 java controller 程式
計算後輸出 JSON Sample

```
{
  // ── 基本資訊 ──────────────────────────────
  "title": "影像辨識開發/RD",                    // 職稱
  "note": "測試 API",                            // 備註
  "calculated_at": "2026-05-28",                // 計算日期

  // ── 工時彙總 ──────────────────────────────
  "total_work_hours": 120.0,                    // 總工時（小時）
  "total_work_days": 15.0,                      // 總工作天
  "total_calendar_days": 21,                    // 含假期的實際天數
  "estimated_finish_date": "2026-06-18",        // 預計完成日期（今天起算）

  // ── 成本彙總 ──────────────────────────────
  "cost": {
    "hourly": 568,                              // 每小時成本（NT$）
    "daily": 4545,                             // 每日成本（NT$）
    "total": 68182                             // 總成本（NT$）
  },

  // ── EEV 經濟效益評估 ───────────────────────
  "eev": {
    "success_chance": 50.0,                    // 成功機率（%）
    "if_success_earn": 300000.0,               // 成功效益（NT$）
    "if_fail_lose": 60000.0,                   // 失敗損失（NT$）
    "expected_gain": 150000,                   // 成功期望值 = 50% × 300,000
    "expected_loss": 30000,                    // 失敗期望損失 = 50% × 60,000
    "dev_cost": 68182,                         // 開發成本
    "eev_value": 51818,                        // EEV = 期望收益 - 期望損失 - 開發成本
    "roi_percent": 76,                         // ROI（%）
    "formula_explanation": "EEV = (成功率 50% * 成功收益 300000) - (失敗率 50% * 失敗損失 60000) - 開發成本 68182 = 51818",
    "recommendation": "✅ 值得投入，EEV 為正 : 51818.18181818182"  // 建議
  },

  // ── 各工項明細 ────────────────────────────
  "projects": [
    {
      "name": "需求分析",
      "work_hours": 16.0,                      // 工時
      "work_days": 2.0,                        // 換算工作天
      "cost": 9091,                            // 工項成本（NT$）
      "finish_date": "2026-06-01"              // 預計完成日
    },
    {
      "name": "開發",
      "work_hours": 40.0,
      "work_days": 5.0,
      "cost": 22727,
      "finish_date": "2026-06-08"
    },
    {
      "name": "測試",
      "work_hours": 8.0,
      "work_days": 1.0,
      "cost": 4545,
      "finish_date": "2026-06-09"
    },
    {
      "name": "部署",
      "work_hours": 16.0,
      "work_days": 2.0,
      "cost": 9091,
      "finish_date": "2026-06-11"
    },
    {
      "name": "維護",
      "work_hours": 40.0,
      "work_days": 5.0,
      "cost": 22727,
      "finish_date": "2026-06-18"
    }
  ],

  // ── 彙總 ──────────────────────────────────
  "summary": {
    "stage_count": 5,                          // 工項數量
    "total_estimated_hours": 120.0,            // 總工時
    "total_estimated_dates": 15.0,             // 總工作天
    "total_estimated_cost": 68181,             // 總成本
    "final_finish_date": "2026-06-18"          // 最終完成日
  }
}
```

### 1️⃣ Web Demo（Thymeleaf）
👉 前端有簡單 CSS with Bootstrap & RWD

👉 前端提供簡單操作說明書

👉 前端提供簡單表單對應上述 REST API 欄位

👉 前端輸出網頁報告並提供 CSV 下載選項

👉 前端簡單渲染輸出 JSON 資料

---


#### CURL Sample
```
curl -X POST http://localhost:8080/api/eev/calculate \
-H "Content-Type: application/json" \
-d '{
  "title": "影像辨識開發/RD",
  "note": "測試 API",
  "monthly_cost": 100000,
  "work_days_per_month": 22,
  "work_hours_per_day": 8,
  "extra_days_off": 0,
  "success_chance": 50,
  "if_success_earn": 300000,
  "if_fail_lose": 60000,
  "projects": [
    { "name": "需求分析", "work_hours": 16 },
    { "name": "開發",    "work_hours": 40 },
    { "name": "測試",    "work_hours": 8  },
    { "name": "部署",    "work_hours": 16 },
    { "name": "維護",    "work_hours": 40 }
  ]
}'
```