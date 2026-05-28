# 💰 EEV Economics Calculator

一個基於 **Java 25 LTS** 與 **Spring Boot** 的決策輔助工具，旨在透過「預期經濟價值 (EEV)」公式，協助團隊將開發需求數據化，優化 ROI (投資報酬率) 並有效過濾無效需求。

> **" 不再憑感覺接需求，用數據做出合理決策。 "**



## 📊 決策邏輯 (The Formula)
本系統核心基於期望值公式，衡量專案投入後的淨獲利可能性：

$$EEV = \sum (P_i \times V_i) - C$$

* **$P_i$ (Probability)**：預期成功機率。
* **$V_i$ (Value)**：成功帶來的經濟價值。
* **$C$ (Cost)**：專案開發總成本（包含人力與時間成本）。

透過此公式，當 **EEV > 0** 時，代表專案在統計上具備投入價值；透過 ROI 指標並列比較，可精確排列專案先後順序。



## 🛠 技術堆疊
* **Backend**: Java 25 LTS, Spring Framework
* **Frontend**: Thymeleaf, JavaScript
* **Architecture**: MVC Pattern with RESTful API

## 💡 功能亮點
* **專案階段拆解**：自動計算各階段（ e.g. 分析、開發、測試、部署 ）的成本分佈與預計完成日。
* **精確排程預估**：內建工作日邏輯，自動排除週末影響。
* **決策輔助報表**：一鍵生成 EEV 評估結果與 ROI 指標，便於向 Stakeholder 溝通。
* **數據統計摘要**：提供總階段數、總工時及總成本匯總，提升專案透明度。



## 🌐 線上體驗
歡迎試用：[https://eev.nekosaur.com/](https://eev.nekosaur.com/)

> ***" 開發註記：本專案為 8 小時內完成之快速原型 (PoC)，旨在透過實作深化對 EEV 模型的理解，並驗證數據輔助決策的核心邏輯。 "***
