let projCounter = 0;
let lastResult = null; // 保存最後一次計算結果，供 CSV 使用

function addProjectRow(name = '', hours = '') {
    const id = projCounter++;
    const list = document.getElementById('proj-list');
    const div = document.createElement('div');
    div.id = 'proj-row-' + id;
    div.className = 'd-flex gap-2 mb-2 align-items-center';
    div.innerHTML = `
        <input type="text" class="form-control form-control-sm flex-grow-1"
               placeholder="例如：需求分析" value="${name}" data-field="name" />
        <input type="number" class="form-control form-control-sm text-end"
               style="width:130px" placeholder="int" value="${hours}"
               min="0" data-field="hours" />
        <button type="button" class="btn btn-outline-danger btn-sm"
                onclick="removeProjectRow(${id})">✕</button>
    `;
    list.appendChild(div);
}

function removeProjectRow(id) {
    document.getElementById('proj-row-' + id)?.remove();
}

function getProjects() {
    return [...document.querySelectorAll('#proj-list [data-field="name"]')]
        .map(nameEl => {
            const row = nameEl.parentElement;
            const name = nameEl.value.trim();
            const hours = parseFloat(row.querySelector('[data-field="hours"]').value);
            return (name && hours > 0) ? { name, work_hours: hours } : null;
        })
        .filter(Boolean);
}

function downloadCSV() {
    if (!lastResult) {
        alert('請先執行計算後再下載。');
        return;
    }

    const d = lastResult;
    const rows = [];

    // 基本資訊
    rows.push(['專案名稱', d.title]);
    rows.push(['備註', d.note ?? '']);
    rows.push(['計算日期', d.calculated_at]);
    rows.push(['預計完成日', d.estimated_finish_date]);
    rows.push(['總工時 (小時)', d.total_work_hours]);
    rows.push(['總工作天', d.total_work_days]);
    rows.push(['總日曆天', d.total_calendar_days]);
    rows.push([]);

    // 成本
    rows.push(['── 成本 ──']);
    rows.push(['每小時成本 (NT$)', d.cost.hourly]);
    rows.push(['每日成本 (NT$)', d.cost.daily]);
    rows.push(['總成本 (NT$)', d.cost.total]);
    rows.push([]);

    // EEV
    rows.push(['── EEV 經濟效益 ──']);
    rows.push(['成功機率 (%)', d.eev.success_chance]);
    rows.push(['成功預期收益 (NT$)', d.eev.if_success_earn]);
    rows.push(['失敗預期損失 (NT$)', d.eev.if_fail_lose]);
    rows.push(['期望收益 (NT$)', d.eev.expected_gain]);
    rows.push(['期望損失 (NT$)', d.eev.expected_loss]);
    rows.push(['開發成本 (NT$)', d.eev.dev_cost]);
    rows.push(['EEV 預期經濟價值 (NT$)', d.eev.eev_value]);
    rows.push(['ROI 投資報酬率 (%)', d.eev.roi_percent]);
    rows.push(['計算公式', d.eev.formula_explanation]);
    rows.push(['建議', d.eev.recommendation]);
    rows.push([]);

    // 工項明細
    rows.push(['── 工項明細 ──']);
    rows.push(['工項名稱', '預估工時 (小時)', '預估工作天', '預估成本 (NT$)', '預計完成日']);
    d.projects.forEach(p => {
        rows.push([p.name, p.work_hours, p.work_days.toFixed(1), p.cost, p.finish_date]);
    });

    // CSV 階段統計
    rows.push(['── 專案統計摘要 ──']);
    rows.push(['階段總數', d.summary.stage_count]);
    rows.push(['預估總工時', d.summary.total_estimated_hours]);
    rows.push(['預估總工作天', d.summary.total_estimated_dates]);
    rows.push(['預估總成本', d.summary.total_estimated_cost]);
    rows.push(['最終完成日', d.summary.final_finish_date]);
    rows.push([]);

    // 轉換為 CSV 字串（加 BOM 讓 Excel 正確顯示中文）
    const csv = '\uFEFF' + rows.map(r =>
        r.map(cell => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(',')
    ).join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `EEV_${d.title || 'report'}_${d.calculated_at}.csv`;
    a.click();
    URL.revokeObjectURL(url);
}

document.getElementById('calcForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const projects = getProjects();
    if (projects.length === 0) {
        alert('請至少新增一筆工項，並確認名稱與工時皆有填寫。');
        return;
    }

    const payload = {
        title: document.getElementById('title').value,
        monthly_cost: parseFloat(document.getElementById('monthly_cost').value),
        work_days_per_month: parseInt(document.getElementById('work_days_per_month').value),
        work_hours_per_day: parseInt(document.getElementById('work_hours_per_day').value),
        extra_days_off: parseInt(document.getElementById('extra_days_off').value),
        success_chance: parseFloat(document.getElementById('success_chance').value),
        if_success_earn: parseFloat(document.getElementById('if_success_earn').value),
        if_fail_lose: parseFloat(document.getElementById('if_fail_lose').value),
        note: document.getElementById('note').value,
        projects
    };

    try {
        const response = await fetch('/api/eev/calculate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await response.json();
        lastResult = data; // 儲存結果供 CSV 使用

        document.getElementById('resultContainer').style.display = 'block';
        document.getElementById('res_formula').textContent = data.eev.formula_explanation;
        document.getElementById('res_title').textContent = data.title;
        document.getElementById('res_hours').textContent = data.total_work_hours;
        document.getElementById('res_days').textContent = data.total_work_days;
        document.getElementById('res_cost').textContent = data.cost.total.toLocaleString();
        document.getElementById('res_roi').textContent = data.eev.roi_percent;

        const eevBox = document.getElementById('res_eev_box');
        eevBox.className = data.eev.eev_value > 0 ? 'alert alert-success' : 'alert alert-danger';
        eevBox.innerHTML = `<strong>${data.eev.recommendation}</strong> (EEV: ${data.eev.eev_value.toLocaleString()})`;

        // 渲染表格內容
        const tbody = document.getElementById('res_projects_body');
        tbody.innerHTML = '';
        data.projects.forEach(p => {
            tbody.innerHTML += `<tr>
                <td>${p.name}</td>
                <td>${p.work_hours} 小時 / ${p.work_days} 天</td>
                <td>${p.cost.toLocaleString()} NT$</td>
                <td>${p.finish_date}</td>
            </tr>`;
        });

        // 渲染表格底部的總計資訊 (tfoot)
        document.getElementById('res_summary_count').textContent = data.summary.stage_count;
        // 更新 HTML 顯示
        document.getElementById('res_summary_hours').textContent = data.summary.total_estimated_hours +" 小時 / "+ data.summary.total_estimated_dates + "天";
        document.getElementById('res_summary_cost').textContent = data.summary.total_estimated_cost.toLocaleString() + ' NT$';
        document.getElementById('res_summary_date').textContent = data.summary.final_finish_date;

        document.getElementById('result').textContent = JSON.stringify(data, null, 2);

    } catch (err) {
        alert('計算失敗：' + err.message);
    }
});

// 頁面載入完成後，自動新增一組預設欄位
document.addEventListener('DOMContentLoaded', () => {
    addProjectRow();
});