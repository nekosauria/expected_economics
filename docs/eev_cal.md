# 💰 人工成本計算器 使用說明書

## 📌 這個工具是做什麼的？

這是一個幫你**估算軟體開發成本**，並判斷**這個專案值不值得做**的計算工具。

你只需要告訴它：
- 月成本是多少
- 這個專案有哪些工作要做、各需要幾小時
- 如果成功能賺多少、失敗會損失多少

它會自動幫你算出：
- 總開發成本
- 預計完成日期
- 這個專案的經濟效益（值不值得做）

---

## 🚀 兩種使用方式

| 方式 | 適合誰 | 入口 |
|------|--------|------|
| 網頁介面 | PM、HR、非技術人員 | `http://localhost:8080` |
| REST API | 工程師、系統整合 | `http://localhost:8080/api/eev/calculate` |

---

## 1️⃣ 網頁介面使用說明

### Step 1：開啟網頁
用瀏覽器打開 `http://localhost:8080`

### Step 2：填寫基本資訊

| 欄位       | 說明       | 範例 |
|----------|----------|------|
| 職稱       | 選填，備忘用   | Senior Engineer |
| 備註       | 選填，備忘用   | Q3 專案估算 |
| 月成本（NT$） | 合併輸入多種成本 | 60000 |
| 每月工作天    | 通常填 22   | 22 |
| 每天工作小時   | 通常填 8    | 8 |
| 額外假期天數   | 國定假日、特休等 | 5 |

### Step 3：填寫 EEV 效益評估

| 欄位 | 說明 | 範例 |
|------|------|------|
| 成功機率（%） | 你覺得這專案成功的機率 | 60 |
| 成功可以賺多少（NT$） | 如果做成功，預期增加的營收或節省的成本 | 1,000,000 |
| 失敗會損失多少（NT$） | 如果失敗，沉沒成本或機會成本 | 200,000 |

### Step 4：新增工項

每個工項填：
- **工項名稱**：例如「需求分析」、「API 開發」
- **工作工時（小時）**：這個工項純工作需要幾小時

可以新增多個工項，點「＋ 新增工項」繼續加。

### Step 5：送出計算

點「**開始計算**」按鈕，結果會顯示在下方。

---

## 2️⃣ REST API 使用說明

### 端點

```
POST http://localhost:8080/api/eev/calculate
Content-Type: application/json
```

### 輸入欄位說明

| 欄位名稱                    | 類型 | 必填 | 說明        |
|-------------------------|------|------|-----------|
| `title`                 | 字串 | 否 | 職稱        |
| `note`                  | 字串 | 否 | 備註        |
| `monthly_cost`          | 數字 | ✅ | 月成本（NT$）  |
| `work_days_per_month`   | 整數 | ✅ | 每月工作天     |
| `work_hours_per_day`    | 整數 | ✅ | 每天工作小時    |
| `extra_days_off`        | 整數 | ✅ | 額外假期天數    |
| `success_chance`        | 數字 0~100 | ✅ | 成功機率（%）   |
| `if_success_earn`       | 數字 | ✅ | 成功效益（NT$） |
| `if_fail_lose`          | 數字 | ✅ | 失敗損失（NT$） |
| `projects`              | 陣列 | ✅ | 工項清單      |
| `projects[].name`       | 字串 | ✅ | 工項名稱      |
| `projects[].work_hours` | 數字 | ✅ | 工項工時（小時）  |

### 輸出欄位說明

| 欄位名稱 | 說明 |
|----------|------|
| `calculated_at` | 計算日期 |
| `total_work_hours` | 所有工項加總工時 |
| `total_work_days` | 換算工作天數 |
| `total_calendar_days` | 含假期的實際天數 |
| `estimated_finish_date` | 預計完成日（從今天起算） |
| `cost.hourly` | 每小時成本（NT$） |
| `cost.daily` | 每日成本（NT$） |
| `cost.total` | 總開發成本（NT$） |
| `eev.expected_gain` | 成功期望值（成功機率 × 成功效益） |
| `eev.expected_loss` | 失敗期望損失（失敗機率 × 失敗損失） |
| `eev.eev_value` | EEV = 期望收益 - 期望損失 - 開發成本 |
| `eev.roi_percent` | 投資報酬率（%） |
| `eev.recommendation` | 系統建議（值不值得做） |
| `projects[]` | 各工項明細（工時、工作天、成本、完成日） |

---

## 📖 範例一：一般後端功能開發

**情境：** PM 希望新增一個會員登入功能，工程師評估如下。

### CURL 範例

```bash
curl -X POST http://localhost:8080/api/eev/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Backend Engineer",
    "note": "會員登入功能",
    "monthly_cost": 60000,
    "work_days_per_month": 22,
    "work_hours_per_day": 8,
    "extra_days_off": 3,
    "success_chance": 80,
    "if_success_earn": 500000,
    "if_fail_lose": 50000,
    "projects": [
      { "name": "需求分析", "work_hours": 8 },
      { "name": "資料庫設計", "work_hours": 16 },
      { "name": "API 開發", "work_hours": 40 },
      { "name": "測試", "work_hours": 16 }
    ]
  }'
```

### 預期輸出

```json
{
  "title": "Backend Engineer",
  "note": "會員登入功能",
  "calculated_at": "2026-05-28",
  "total_work_hours": 80.0,
  "total_work_days": 10.0,
  "total_calendar_days": 17,
  "estimated_finish_date": "2026-06-14",
  "cost": {
    "daily": 2727,
    "hourly": 341,
    "total": 27273
  },
  "eev": {
    "if_success_earn": 500000.0,
    "success_chance": 80.0,
    "eev_value": 362727,
    "recommendation": "✅ 值得投入，EEV 為正 : 362727.2727272727",
    "expected_gain": 400000,
    "dev_cost": 27273,
    "if_fail_lose": 50000.0,
    "roi_percent": 1330,
    "expected_loss": 10000,
    "formula_explanation": "EEV = (成功率 80% * 成功收益 500000) - (失敗率 20% * 失敗損失 50000) - 開發成本 27273 = 362727"
  },
  "projects": [
    {
      "name": "需求分析",
      "cost": 2727,
      "finish_date": "2026-06-01",
      "work_hours": 8.0,
      "work_days": 1.0
    },
    {
      "name": "資料庫設計",
      "cost": 5455,
      "finish_date": "2026-06-05",
      "work_hours": 16.0,
      "work_days": 2.0
    },
    {
      "name": "API 開發",
      "cost": 13636,
      "finish_date": "2026-06-12",
      "work_hours": 40.0,
      "work_days": 5.0
    },
    {
      "name": "測試",
      "cost": 5455,
      "finish_date": "2026-06-14",
      "work_hours": 16.0,
      "work_days": 2.0
    }
  ],
  "summary": {
    "total_estimated_cost": 27273,
    "final_finish_date": "2026-06-14",
    "total_estimated_dates": 10.0,
    "total_estimated_hours": 80.0,
    "stage_count": 4
  }
}
```

---

## 📖 範例二：高風險 AI 功能

**情境：** PM 想做一個 AI 推薦系統，成功效益高但不確定性也高。

```bash
curl -X POST http://localhost:8080/api/eev/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "title": "ML Engineer",
    "note": "AI 推薦系統",
    "monthly_cost": 800000,
    "work_days_per_month": 22,
    "work_hours_per_day": 8,
    "extra_days_off": 10,
    "success_chance": 35,
    "if_success_earn": 3000000,
    "if_fail_lose": 1000000,
    "projects": [
      { "name": "資料收集與清洗", "work_hours": 80 },
      { "name": "模型訓練", "work_hours": 120 },
      { "name": "API 整合", "work_hours": 40 },
      { "name": "A/B Testing", "work_hours": 40 }
    ]
  }'
```

結果 `eev.recommendation` 會顯示 `⚠️ 高風險高報酬，建議先做 POC 驗證`。

---

## ❓ 常見問題

**Q：額外假期要填幾天？**
A：填整個專案期間會遇到的國定假日 + 特休天數。例如專案跑 1 個月，遇到 3 個國定假日就填 3。

**Q：成功機率怎麼評估？**
A：沒有標準答案，參考下表：

| 情境 | 建議機率 |
|------|---------|
| 需求明確、技術成熟 | 70~90% |
| 需求大致清楚、有些技術風險 | 50~70% |
| 需求模糊、或有新技術 | 30~50% |
| 純實驗性質 | 10~30% |

**Q：EEV 是負的代表不能做嗎？**
A：不一定。EEV 為負代表純經濟效益不划算，但有時有策略性考量（例如技術債償還、品牌形象）仍值得做，需要人工判斷。

**Q：工時要填純工作時間還是含開會？**
A：如果不想當卡車司機，手握方向盤（才算工時）的話，填工作與此案子相關的時間加總即可。