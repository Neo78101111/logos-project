$(document).ready(function() {
    loadDreams();

    // ----- Новая мечта: сброс формы -----
    $('#newDreamBtn').click(function() {
        // Очищаем идентификатор и поля ДО того, как модалка откроется
        $('#dreamId').val('');
        $('#dreamForm')[0].reset();
        $('#currentDreamImage').html('');
        $('#dreamModalTitle').text('Новая мечта');
        // Модалка откроется автоматически через data-bs-toggle
    });

    // ----- Обработчик открытия модального окна мечты -----
    $('#dreamModal').on('show.bs.modal', function() {
        var dreamId = $('#dreamId').val();
        if (dreamId) {
            $('#dreamModalTitle').text('Редактировать мечту');
        } else {
            // Дополнительная страховка: если dreamId пуст, убедимся, что заголовок "Новая мечта"
            $('#dreamModalTitle').text('Новая мечта');
        }
    });

    // ----- Сохранение мечты (создание или обновление) -----
    $('#saveDreamBtn').click(function() {
        const dreamId = $('#dreamId').val();
        const data = {
            title: $('#dreamTitle').val(),
            description: $('#dreamDescription').val()
        };
        const method = dreamId ? 'PUT' : 'POST';
        let url = '/api/dreams';
        if (dreamId) url += '/' + dreamId;

        $.ajax({
            url: url,
            type: method,
            contentType: 'application/json',
            data: JSON.stringify(data),
            beforeSend: addCsrfToken,
            success: function(dream) {
                const file = $('#dreamImageFile')[0].files[0];
                if (file) {
                    uploadDreamImage(dream.id, file, function() {
                        $('#dreamModal').modal('hide');
                        toast.success('Мечта сохранена');
                        loadDreams();
                        // После сохранения сбрасываем dreamId для следующего создания
                        $('#dreamId').val('');
                    });
                } else {
                    $('#dreamModal').modal('hide');
                    toast.success('Мечта сохранена');
                    loadDreams();
                    $('#dreamId').val('');
                }
            },
            error: function() {
                toast.error('Ошибка сохранения мечты');
            }
        });
    });

    // ----- Сохранение цели (создание или обновление) -----
    $('#saveGoalBtn').click(function() {
        const goalId = $('#goalId').val();
        const dreamId = $('#goalDreamId').val();
        const data = {
            title: $('#goalTitle').val(),
            deadline: $('#goalDeadline').val()
        };
        let url, method;
        if (goalId) {
            url = '/api/dreams/' + dreamId + '/goals/' + goalId;
            method = 'PUT';
        } else {
            url = '/api/dreams/' + dreamId + '/goals';
            method = 'POST';
        }
        $.ajax({
            url: url,
            type: method,
            contentType: 'application/json',
            data: JSON.stringify(data),
            beforeSend: addCsrfToken,
            success: function() {
                $('#goalModal').modal('hide');
                toast.success('Цель сохранена');
                loadDreams();
            },
            error: function() {
                toast.error('Ошибка сохранения цели');
            }
        });
    });

    // ----- Достижение мечты -----
    $('#confirmAchieveBtn').click(function() {
        const dreamId = $('#achieveDreamId').val();
        const formData = new FormData();
        const file = $('#resultImage')[0].files[0];
        if (file) formData.append('resultImage', file);
        formData.append('achievementComment', $('#achievementComment').val());

        $.ajax({
            url: '/api/dreams/' + dreamId + '/achieve',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            beforeSend: addCsrfToken,
            success: function() {
                $('#achieveModal').modal('hide');
                toast.success('Мечта достигнута! Поздравляем!');
                loadDreams();
            },
            error: function() {
                toast.error('Ошибка при достижении мечты');
            }
        });
    });
});

// Загрузка списка мечт
function loadDreams() {
    $.get('/api/dreams', function(dreams) {
        const container = $('#dreamsList');
        container.empty();
        if (dreams.length === 0) {
            container.html('<div class="text-center"><p class="text-muted">У вас пока нет мечт. Создайте первую!</p></div>');
            return;
        }
        dreams.forEach(dream => {
            const card = buildDreamCard(dream);
            container.append(card);
        });
        bindDreamEvents();
    });
}

// Построение HTML карточки мечты (с data-dream-id для целей)
function buildDreamCard(dream) {
    let goalsHtml = '';
    dream.goals.forEach(goal => {
        const remainingDays = getRemainingDays(goal.deadline);
        const isExpired = remainingDays < 0;
        const urgent = !isExpired && remainingDays <= 3;
        let timerText = '';
        if (isExpired) timerText = 'просрочено';
        else if (remainingDays === 0) timerText = 'сегодня';
        else timerText = `${remainingDays} дн.`;

        goalsHtml += `
            <div class="goal-item" data-goal-id="${goal.id}" data-dream-id="${dream.id}">
                <div class="goal-info">
                    <div class="goal-title ${goal.completed ? 'completed' : ''}">${escapeHtml(goal.title)}</div>
                    <div class="goal-deadline"><i class="far fa-calendar-alt"></i> ${goal.deadline}</div>
                </div>
                <div class="goal-timer ${urgent ? 'urgent' : ''}">${timerText}</div>
                <div class="goal-actions">
                    <button class="btn btn-sm btn-outline-success toggle-goal-btn" data-goal-id="${goal.id}" data-dream-id="${dream.id}" data-completed="${goal.completed}">
                        <i class="fas ${goal.completed ? 'fa-undo' : 'fa-check'}"></i> ${goal.completed ? 'Отменить' : 'Выполнить'}
                    </button>
                    <button class="btn btn-sm btn-outline-secondary edit-goal-btn" data-goal-id="${goal.id}" data-dream-id="${dream.id}" data-title="${escapeHtml(goal.title)}" data-deadline="${goal.deadline}">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger delete-goal-btn" data-goal-id="${goal.id}" data-dream-id="${dream.id}">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        `;
    });

    let actionButtons = '';
    if (dream.status === 'IN_PROGRESS') {
        actionButtons = `
            <div class="mt-3 d-flex justify-content-between">
                <button class="btn btn-outline-primary edit-dream-btn" data-dream-id="${dream.id}" data-title="${escapeHtml(dream.title)}" data-description="${escapeHtml(dream.description || '')}" data-image="${dream.dreamImageUrl || ''}">
                    <i class="fas fa-edit"></i> Редактировать
                </button>
                <button class="btn btn-outline-danger delete-dream-btn" data-dream-id="${dream.id}">
                    <i class="fas fa-trash"></i> Удалить
                </button>
                <button class="btn btn-success achieve-dream-btn" data-dream-id="${dream.id}">
                    <i class="fas fa-trophy"></i> Достичь мечты
                </button>
            </div>
        `;
    } else {
        actionButtons = `
            <div class="alert alert-success mt-3">
                <i class="fas fa-check-circle"></i> Мечта достигнута!
                ${dream.resultImageUrl ? `<br><img src="${dream.resultImageUrl}" class="result-image img-fluid">` : ''}
                ${dream.achievementComment ? `<br><em>${escapeHtml(dream.achievementComment)}</em>` : ''}
            </div>
        `;
    }

    return `
        <div class="card dream-card">
            ${dream.dreamImageUrl ? `<img src="${dream.dreamImageUrl}" class="dream-image" alt="Фото-ожидание">` : ''}
            <div class="card-body">
                <h3 class="card-title">${escapeHtml(dream.title)}</h3>
                <p class="card-text">${escapeHtml(dream.description) || '—'}</p>
                <hr>
                <h5><i class="fas fa-bullseye"></i> Цели:</h5>
                <div class="goals-list mt-2">${goalsHtml || '<p class="text-muted">Нет целей. Добавьте первую!</p>'}</div>
                <button class="btn btn-sm btn-primary add-goal-btn mt-2" data-dream-id="${dream.id}">
                    <i class="fas fa-plus"></i> Добавить цель
                </button>
                ${actionButtons}
            </div>
        </div>
    `;
}

// Привязка событий к динамическим элементам
function bindDreamEvents() {
    // Редактирование мечты
    $('.edit-dream-btn').click(function() {
        const id = $(this).data('dream-id');
        const title = $(this).data('title');
        const description = $(this).data('description');
        $('#dreamId').val(id);
        $('#dreamTitle').val(title);
        $('#dreamDescription').val(description);
        $('#currentDreamImage').html('');
        if ($(this).data('image')) {
            $('#currentDreamImage').html(`<img src="${$(this).data('image')}" style="max-height:100px"> <small>Текущее фото</small>`);
        }
        $('#dreamModal').modal('show');
    });

    // Удаление мечты
    $('.delete-dream-btn').click(function() {
        const id = $(this).data('dream-id');
        if (confirm('Удалить мечту и все её цели?')) {
            $.ajax({
                url: '/api/dreams/' + id,
                type: 'DELETE',
                beforeSend: addCsrfToken,
                success: function() {
                    loadDreams();
                    toast.success('Мечта удалена');
                },
                error: function() {
                    toast.error('Ошибка удаления');
                }
            });
        }
    });

    // Добавление цели
    $('.add-goal-btn').click(function() {
        const dreamId = $(this).data('dream-id');
        $('#goalId').val('');
        $('#goalDreamId').val(dreamId);
        $('#goalTitle').val('');
        $('#goalDeadline').val('');
        $('#goalModal').modal('show');
    });

    // Редактирование цели
    $('.edit-goal-btn').click(function() {
        const goalId = $(this).data('goal-id');
        const dreamId = $(this).data('dream-id');
        const title = $(this).data('title');
        const deadline = $(this).data('deadline');
        $('#goalId').val(goalId);
        $('#goalDreamId').val(dreamId);
        $('#goalTitle').val(title);
        $('#goalDeadline').val(deadline);
        $('#goalModal').modal('show');
    });

    // Удаление цели
    $('.delete-goal-btn').click(function() {
        const goalId = $(this).data('goal-id');
        const dreamId = $(this).data('dream-id');
        if (confirm('Удалить цель?')) {
            $.ajax({
                url: '/api/dreams/' + dreamId + '/goals/' + goalId,
                type: 'DELETE',
                beforeSend: addCsrfToken,
                success: function() {
                    loadDreams();
                    toast.success('Цель удалена');
                },
                error: function() {
                    toast.error('Ошибка удаления');
                }
            });
        }
    });

    // Переключение выполнения цели
    $('.toggle-goal-btn').click(function() {
        const goalId = $(this).data('goal-id');
        const dreamId = $(this).data('dream-id');
        $.ajax({
            url: '/api/dreams/' + dreamId + '/goals/' + goalId + '/toggle',
            type: 'PATCH',
            beforeSend: addCsrfToken,
            success: function() {
                loadDreams();
            },
            error: function() {
                toast.error('Ошибка изменения статуса');
            }
        });
    });

    // Открытие модалки достижения мечты
    $('.achieve-dream-btn').click(function() {
        const dreamId = $(this).data('dream-id');
        $('#achieveDreamId').val(dreamId);
        $('#achieveForm')[0].reset();
        $('#achieveModal').modal('show');
    });
}

// Загрузка фото-ожидания
function uploadDreamImage(dreamId, file, callback) {
    const formData = new FormData();
    formData.append('image', file);
    $.ajax({
        url: '/api/dreams/' + dreamId + '/upload-dream-image',
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        beforeSend: addCsrfToken,
        success: callback,
        error: function() {
            toast.error('Ошибка загрузки фото');
            if (callback) callback();
        }
    });
}

// Вспомогательные функции
function getRemainingDays(deadline) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const deadlineDate = new Date(deadline);
    deadlineDate.setHours(0, 0, 0, 0);
    const diffTime = deadlineDate - today;
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}

function addCsrfToken(xhr) {
    const token = $('meta[name="_csrf"]').attr('content');
    const header = $('meta[name="_csrf_header"]').attr('content');
    if (token && header) xhr.setRequestHeader(header, token);
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}