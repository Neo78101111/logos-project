/**
 * CALENDAR.JS – работа с календарём, модальными окнами, API.
 * Все стили вынесены в CSS.
 */

// ------------------------------------------------------------
// Базовый API-клиент (без изменений)
// ------------------------------------------------------------
async function apiRequest(url, method = 'GET', data = null) {
    try {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

        const options = {
            method,
            headers: {'Content-Type': 'application/json'},
            credentials: 'same-origin'
        };

        if (csrfToken && method !== 'GET') {
            options.headers[csrfHeader] = csrfToken;
        }

        if (data && ['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
            options.body = JSON.stringify(data);
        }

        const response = await fetch(url, options);
        if (!response.ok) {
            const err = await response.text();
            throw new Error(`HTTP ${response.status}: ${err}`);
        }

        return response.status === 204 ? null : await response.json();
    } catch (error) {
        console.error('API Request failed:', error);
        throw error;
    }
}

function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return String(unsafe)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// ------------------------------------------------------------
// Основной класс – управление календарём
// ------------------------------------------------------------
class CalendarManager {
    constructor() {
        this.selectedDate = null;
        this.currentViewDate = null; // для хранения даты открытой модалки просмотра
        this.currentYear = document.getElementById('currentYear')?.value;
        this.currentMonth = document.getElementById('currentMonth')?.value;
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.setupModalHandlers();
    }

    // ----- События -----
    setupEventListeners() {
        document.addEventListener('click', (e) => this.handleButtonClick(e));
    }

    handleButtonClick(e) {
        const taskBtn = e.target.closest('.add-task-btn');
        if (taskBtn) {
            e.preventDefault();
            this.openAddTaskModal(taskBtn.dataset.date);
            return;
        }

        const evalBtn = e.target.closest('.evaluate-btn');
        if (evalBtn) {
            e.preventDefault();
            this.openEvaluateModal(evalBtn.dataset.date);
            return;
        }

        const viewBtn = e.target.closest('.view-day-btn');
        if (viewBtn) {
            e.preventDefault();
            this.openViewModal(viewBtn.dataset.date);
            return;
        }

        const completeBtn = e.target.closest('.complete-task-btn');
        if (completeBtn) {
            e.preventDefault();
            const taskId = completeBtn.dataset.taskId;
            const date = completeBtn.dataset.date;
            this.completeTask(date, taskId);
            return;
        }

        const deleteBtn = e.target.closest('.delete-task-btn');
        if (deleteBtn) {
            e.preventDefault();
            const taskId = deleteBtn.dataset.taskId;
            const date = deleteBtn.dataset.date;
            if (confirm('Удалить задачу?')) {
                this.deleteTask(date, taskId);
            }
            return;
        }
    }

    // ----- Модальные окна -----
    setupModalHandlers() {
        const saveTask = document.getElementById('saveTask');
        if (saveTask) saveTask.addEventListener('click', () => this.saveTask());

        const saveEval = document.getElementById('saveEvaluation');
        if (saveEval) saveEval.addEventListener('click', () => this.saveEvaluation());

        this.setupSatisfactionCalculator();
        this.setupModalCleanup();
    }

    setupSatisfactionCalculator() {
        document.querySelectorAll('.evaluation-score').forEach(input => {
            input.addEventListener('input', () => this.calculateSatisfaction());
        });
    }

    setupModalCleanup() {
        const taskModal = document.getElementById('addTaskModal');
        if (taskModal) {
            taskModal.addEventListener('hidden.bs.modal', () => {
                document.getElementById('taskForm')?.reset();
                this.selectedDate = null;
            });
        }

        const evalModal = document.getElementById('evaluateDayModal');
        if (evalModal) {
            evalModal.addEventListener('hidden.bs.modal', () => {
                document.getElementById('evaluationForm')?.reset();
                this.selectedDate = null;
            });
        }
    }

    // ----- Добавление задачи -----
    openAddTaskModal(date) {
        if (this.isPastDate(date)) {
            toast.warning('Нельзя добавлять задачи на прошедшие дни');
            return;
        }
        this.selectedDate = date;
        const input = document.getElementById('taskDate');
        if (input) input.value = date;

        const modalEl = document.getElementById('addTaskModal');
        if (modalEl) new bootstrap.Modal(modalEl).show();
    }

    async saveTask() {
        if (!this.selectedDate) {
            toast.warning('Ошибка: не указана дата');
            return;
        }

        const title = document.getElementById('taskTitle')?.value.trim();
        if (!title) {
            toast.warning('Пожалуйста, введите название задачи');
            return;
        }

        const priority = document.getElementById('taskPriority')?.value || 'MEDIUM';

        const taskData = {
            title,
            description: document.getElementById('taskDescription')?.value.trim() || '',
            priority: priority,
            completed: false
        };

        try {
            this.showLoading('saveTask', true);

            await apiRequest(`/api/calendar/days/${this.selectedDate}/tasks`, 'POST', taskData);

            bootstrap.Modal.getInstance(document.getElementById('addTaskModal'))?.hide();
            toast.success('Задача успешно добавлена!');
            await this.refreshMonth();
        } catch (error) {
            console.error(error);
            let msg = 'Ошибка при сохранении задачи';
            if (error.message.includes('400')) msg = 'Некорректные данные задачи';
            else if (error.message.includes('403')) msg = 'Недостаточно прав';
            else if (error.message.includes('409')) msg = 'Задача уже существует?';
            toast.error(msg);
        } finally {
            this.showLoading('saveTask', false);
        }
    }

    async completeTask(date, taskId) {
        let loadingShown = false;
        try {
            this.showInlineLoading(taskId, true); //
            loadingShown = true;
            await apiRequest(`/api/calendar/days/${date}/tasks/${taskId}`, 'PATCH');
            toast.success('Задача выполнена!');
            this.showInlineLoading(taskId, false);
            loadingShown = false;
            if (this.currentViewDate === date) {
                await this.refreshViewModal();
            }
            await this.refreshMonth();
        } catch (error) {
            console.error(error);
            let msg = 'Ошибка при выполнении задачи';
            if (error.message.includes('400')) msg = 'Некорректный запрос';
            else if (error.message.includes('403')) msg = 'Недостаточно прав';
            else if (error.message.includes('404')) msg = 'Задача не найдена';
            else if (error.message.includes('409')) msg = 'Конфликт данных';
            toast.error(msg);
            if (loadingShown) {
                this.showInlineLoading(taskId, false);
            }
        }
    }

    async deleteTask(date, taskId) {
        let loadingShown = false;
        try {
            this.showInlineLoading(taskId, true);
            loadingShown = true;
            await apiRequest(`/api/calendar/days/${date}/tasks/${taskId}`, 'DELETE');
            toast.success('Задача удалена');
            this.showInlineLoading(taskId, false);
            loadingShown = false;
            if (this.currentViewDate === date) {
                await this.refreshViewModal();
            }
            await this.refreshMonth();
        } catch (error) {
            console.error(error);
            let msg = 'Ошибка при удалении задачи';
            if (error.message.includes('403')) msg = 'Недостаточно прав';
            else if (error.message.includes('404')) msg = 'Задача не найдена';
            toast.error(msg);
        } finally {
            if (loadingShown) {
                this.showInlineLoading(taskId, false);
            }
        }
    }

    // Простой индикатор загрузки
    showInlineLoading(taskId, show) {
        const btn = document.querySelector(`[data-task-id="${taskId}"]`);
        if (!btn) return;

        if (show) {
            if (!btn.dataset.originalHtml) {
                btn.dataset.originalHtml = btn.innerHTML;
            }
            btn.disabled = true;
            btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
        } else {
            if (btn.dataset.originalHtml && document.body.contains(btn)) {
                btn.disabled = false;
                btn.innerHTML = btn.dataset.originalHtml;
                delete btn.dataset.originalHtml;
            }
        }
    }

    // ----- Оценка дня -----
    openEvaluateModal(date) {
        const today = new Date().toISOString().split('T')[0];
        if (date !== today) {
            toast.warning('Можно оценить только сегодняшний день');
            return;
        }

        this.selectedDate = date;
        const input = document.getElementById('evaluationDate');
        if (input) input.value = date;

        const modalEl = document.getElementById('evaluateDayModal');
        if (modalEl) new bootstrap.Modal(modalEl).show();
    }

    calculateSatisfaction() {
        const scores = ['wisdomScore', 'courageScore', 'temperanceScore', 'justiceScore']
            .map(name => parseInt(document.querySelector(`input[name="${name}"]`)?.value) || 0);
        const isValid = scores.every(s => s >= 1 && s <= 10);
        if (isValid) {
            const avg = Math.round(scores.reduce((a, b) => a + b, 0) / scores.length);
            const satInput = document.getElementById('satisfactionScore');
            if (satInput) satInput.value = avg;
        }
    }

    async saveEvaluation() {
        if (!this.selectedDate) {
            toast.warning('Ошибка: не указана дата');
            return;
        }

        const formData = {
            daySatisfactionScore: parseInt(document.getElementById('satisfactionScore')?.value) || 0,
            wisdomScore: parseInt(document.querySelector('input[name="wisdomScore"]')?.value) || 0,
            courageScore: parseInt(document.querySelector('input[name="courageScore"]')?.value) || 0,
            temperanceScore: parseInt(document.querySelector('input[name="temperanceScore"]')?.value) || 0,
            justiceScore: parseInt(document.querySelector('input[name="justiceScore"]')?.value) || 0,
            comment: document.querySelector('textarea[name="comment"]')?.value || ''
        };

        const scores = [
            formData.daySatisfactionScore,
            formData.wisdomScore,
            formData.courageScore,
            formData.temperanceScore,
            formData.justiceScore
        ];

        if (scores.some(s => s < 1 || s > 10)) {
            toast.warning('Все оценки должны быть от 1 до 10');
            return;
        }

        try {
            this.showLoading('saveEvaluation', true);
            await apiRequest(`/api/calendar/days/${this.selectedDate}/evaluation`, 'PATCH', formData);

            bootstrap.Modal.getInstance(document.getElementById('evaluateDayModal'))?.hide();
            toast.success('День успешно оценён!');
            await this.refreshMonth();
        } catch (error) {
            console.error(error);
            let msg = 'Ошибка при сохранении оценки';
            if (error.message.includes('400')) msg = 'Ошибка валидации';
            else if (error.message.includes('403')) msg = 'Недостаточно прав';
            else if (error.message.includes('409')) msg = 'Этот день уже оценён';
            toast.error(msg);
        } finally {
            this.showLoading('saveEvaluation', false);
        }
    }

    async openViewModal(date) {
        try {
            this.currentViewDate = date;
            this.selectedDate = date;
            const modalEl = document.getElementById('viewDayModal');
            if (!modalEl) return;

            const dayData = await apiRequest(`/api/calendar/days/${date}`, 'GET');

            document.getElementById('viewDate').textContent =
                new Date(date).toLocaleDateString('ru-RU', {
                    day: 'numeric', month: 'long', year: 'numeric'
                });

            this.renderViewTasks(dayData.tasks || []);

            this.renderViewEvaluation(dayData.evaluation);

            const modal = new bootstrap.Modal(modalEl);
            modal.show();
        } catch (error) {
            console.error('Error loading day details:', error);
            toast.error('Не удалось загрузить информацию о дне');
        }
    }

    /**
     * Отображение списка задач в модалке просмотра
     */
    renderViewTasks(tasks) {
        const container = document.getElementById('viewTasksList');
        if (!container) return;

        if (!tasks || tasks.length === 0) {
            container.innerHTML = '<p class="text-muted">Нет задач</p>';
            return;
        }

        let html = '<ul class="list-unstyled">';
        tasks.forEach(task => {
            const statusClass = task.completed ? 'text-success' : 'text-secondary';
            const statusIcon = task.completed ? 'fa-check-circle' : 'fa-circle';

            let priorityIcon = '';
            if (task.priority === 'HIGH') priorityIcon = '<i class="fas fa-exclamation-circle text-danger me-1" title="Высокий"></i>';
            else if (task.priority === 'MEDIUM') priorityIcon = '<i class="fas fa-minus-circle text-warning me-1" title="Средний"></i>';
            else priorityIcon = '<i class="fas fa-arrow-circle-down text-info me-1" title="Низкий"></i>';

            html += `<li class="mb-3 d-flex align-items-start">
                    <i class="fas ${statusIcon} ${statusClass} me-2 mt-1"></i>
                    ${priorityIcon}
                    <div class="flex-grow-1">
                        <strong>${escapeHtml(task.title)}</strong>
                        ${task.description ? `<br><small class="text-muted">${escapeHtml(task.description)}</small>` : ''}
                    </div>
                    <div class="ms-2">
                        ${!task.completed ?
                `<button class="btn btn-sm btn-outline-success complete-task-btn me-1" 
                                     data-task-id="${task.id}" 
                                     data-date="${this.selectedDate}"
                                     title="Выполнить">
                                <i class="fas fa-check"></i>
                             </button>` : ''}
                        <button class="btn btn-sm btn-outline-danger delete-task-btn" 
                                data-task-id="${task.id}" 
                                data-date="${this.selectedDate}"
                                title="Удалить">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                 </li>`;
        });
        html += '</ul>';
        container.innerHTML = html;
    }

    /**
     * Отображение оценки дня в модалке просмотра
     */
    renderViewEvaluation(evaluation) {
        const container = document.getElementById('viewEvaluation');
        if (!container) return;

        if (!evaluation) {
            container.innerHTML = '<p class="text-muted">День не оценён</p>';
            return;
        }

        const scores = [
            {label: 'Общая удовлетворённость', value: evaluation.daySatisfactionScore},
            {label: 'Мудрость', value: evaluation.wisdomScore},
            {label: 'Мужество', value: evaluation.courageScore},
            {label: 'Умеренность', value: evaluation.temperanceScore},
            {label: 'Справедливость', value: evaluation.justiceScore}
        ];

        let html = '<div class="row">';
        scores.forEach(score => {
            html += `<div class="col-md-6 mb-2">
                        <span class="fw-bold">${score.label}:</span>
                        <span class="badge bg-primary">${score.value}/10</span>
                     </div>`;
        });
        html += '</div>';

        if (evaluation.comment) {
            html += `<div class="mt-3">
                        <span class="fw-bold">Комментарий:</span>
                        <p class="mt-1 p-2 bg-light rounded">${escapeHtml(evaluation.comment)}</p>
                     </div>`;
        }

        container.innerHTML = html;
    }

    // ----- Вспомогательные методы -----
    isPastDate(dateString) {
        return dateString < new Date().toISOString().split('T')[0];
    }

    showLoading(buttonId, show) {
        const btn = document.getElementById(buttonId);
        if (!btn) return;
        if (show) {
            btn.dataset.originalText = btn.innerHTML;
            btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Сохранение...';
            btn.disabled = true;
        } else {
            if (btn.dataset.originalText) {
                btn.innerHTML = btn.dataset.originalText;
                delete btn.dataset.originalText;
            }
            btn.disabled = false;
        }
    }

    async refreshViewModal() {
        if (!this.currentViewDate) return;
        try {
            const dayData = await apiRequest(`/api/calendar/days/${this.currentViewDate}`, 'GET');
            this.renderViewTasks(dayData.tasks || []);
            this.renderViewEvaluation(dayData.evaluation);
        } catch (error) {
            console.error('Failed to refresh view modal:', error);
            toast.error('Не удалось обновить данные дня');
        }
    }

    async refreshMonth() {
        try {
            const data = await apiRequest(`/api/calendar/months/${this.currentYear}/${this.currentMonth}`, 'GET');
            this.updateCalendarUI(data);
            document.getElementById('currentYear').value = data.year;
            document.getElementById('currentMonth').value = data.month;
            this.currentYear = data.year;
            this.currentMonth = data.month;
        } catch (error) {
            console.error('Failed to refresh month:', error);
            toast.error('Не удалось обновить календарь');
        }
    }

    updateCalendarUI(monthData) {
        document.querySelector('.calendar-header h2').textContent = monthData.monthName;
        this.updateStats(monthData);

        const prevLink = document.getElementById('prevMonthLink');
        const nextLink = document.getElementById('nextMonthLink');
        if (prevLink && monthData.previousMonth) {
            prevLink.href = `/calendar?date=${monthData.previousMonth}`;
        }
        if (nextLink && monthData.nextMonth) {
            nextLink.href = `/calendar?date=${monthData.nextMonth}`;
        }

        this.renderCalendarGrid(monthData.days);
    }

    updateStats(monthData) {
        document.getElementById('evaluatedDaysCount').textContent = monthData.evaluatedDays || 0;
        const avg = (monthData.averageSatisfaction || 0).toFixed(2);
        document.getElementById('averageSatisfaction').textContent = avg;
        document.getElementById('completedTasksCount').textContent = `${monthData.completedTasks}/${monthData.totalTasks}`;
        document.getElementById('totalDaysCount').textContent = monthData.totalDays || 0;
    }

    renderCalendarGrid(days) {
        const container = document.getElementById('calendarDays');
        if (!container) return;
        if (!days || days.length === 0) {
            container.innerHTML = '<div class="alert alert-warning">Нет данных</div>';
            return;
        }

        const firstDay = days[0];
        const firstDate = new Date(firstDay.date + 'T00:00:00');
        let firstDayOfWeek = firstDate.getDay();
        firstDayOfWeek = firstDayOfWeek === 0 ? 7 : firstDayOfWeek;

        let html = '';
        for (let i = 1; i < firstDayOfWeek; i++) {
            html += '<div class="calendar-day-card empty border-0"></div>';
        }

        const todayStr = new Date().toISOString().split('T')[0];
        days.forEach(day => {
            const dateStr = day.date;
            const dayNumber = new Date(dateStr + 'T00:00:00').getDate();
            const statusClass = `day-${day.temporalStatus.toLowerCase()}`;

            let tasksHtml = '';
            if (!day.tasks || day.tasks.length === 0) {
                tasksHtml = '<div class="text-muted small"><i class="fas fa-bullseye"></i> Нет задач</div>';
            } else {
                const highTasks = day.tasks.filter(t => t.priority === 'HIGH');
                const highCount = highTasks.length;
                const totalCount = day.tasks.length;

                highTasks.slice(0, 3).forEach(task => {
                    const icon = task.completed ? 'fa-check-circle text-success' : 'far fa-circle text-secondary';
                    tasksHtml += `<div class="task-item d-flex align-items-start mb-1">
                    <i class="fas ${icon} me-1 mt-1"></i>
                    <span class="flex-grow-1 small">${escapeHtml(task.title)}</span>
                </div>`;
                });

                if (highCount > 3) {
                    tasksHtml += `<div class="small text-muted"><i class="fas fa-ellipsis-h"></i> ещё ${highCount - 3} важных</div>`;
                }
                if (totalCount > highCount) {
                    tasksHtml += `<div class="small text-muted"><i class="fas fa-bullseye"></i> всего ${totalCount} задач</div>`;
                }
            }

            let actionsHtml = '';
            if (day.temporalStatus !== 'PAST') {
                actionsHtml += `<button class="btn btn-sm btn-outline-primary add-task-btn" data-date="${dateStr}"><i class="fas fa-bullseye"></i> Задача</button> `;
            }
            if (dateStr === todayStr && !day.evaluation) {
                actionsHtml += `<button class="btn btn-sm btn-outline-success evaluate-btn ms-1" data-date="${dateStr}"><i class="fas fa-star"></i> Оценить</button> `;
            }
            actionsHtml += `<button class="btn btn-sm btn-outline-info view-day-btn ms-1" data-date="${dateStr}"><i class="fas fa-eye"></i> Просмотр</button>`;

            html += `<div class="calendar-day-card p-2 ${statusClass}">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="fw-bold">${dayNumber}</span>
                <small class="text-muted">${new Date(dateStr + 'T00:00:00').toLocaleDateString('ru-RU', { weekday: 'short' })}</small>
            </div>
            <div class="day-tasks mb-2" style="min-height: 60px;">${tasksHtml}</div>
            <div class="day-actions mt-2">${actionsHtml}</div>
        </div>`;
        });

        container.innerHTML = html;
    }
}

// ------------------------------------------------------------
// Утилиты для дат
// ------------------------------------------------------------
const CalendarUtils = {
    formatDate(dateString) {
        if (!dateString) return '';
        return new Date(dateString).toLocaleDateString('ru-RU', {
            day: 'numeric', month: 'long', year: 'numeric'
        });
    },
    isToday(dateString) {
        return dateString === new Date().toISOString().split('T')[0];
    }
};

// ------------------------------------------------------------
// Инициализация
// ------------------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
    console.log('Calendar page loaded');
    window.calendarManager = new CalendarManager();
    checkRequiredElements();
});

function checkRequiredElements() {

    ['#calendarDays', '.calendar-day-card', '#addTaskModal', '#evaluateDayModal', '#viewDayModal'].forEach(selector => {
        if (!document.querySelector(selector)) {
            console.warn(`Element not found: ${selector}`);
        }
    });
}