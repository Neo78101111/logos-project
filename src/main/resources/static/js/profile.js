/**
 * profile.js – управление профилем пользователя
 */

document.addEventListener('DOMContentLoaded', function() {
    loadUserProfile();
    setupProfileForm();
    setupPasswordForm();
});

/**
 * Загрузка данных пользователя через API
 */
async function loadUserProfile() {
    try {
        const response = await fetch('/api/users/me', {
            method: 'GET',
            credentials: 'same-origin',
            headers: getCsrfHeaders()
        });
        if (!response.ok) throw new Error('Ошибка загрузки профиля');
        const user = await response.json();
        fillProfileForm(user);
    } catch (error) {
        console.error(error);
        toast.error('Не удалось загрузить данные профиля');
    }
}

function fillProfileForm(user) {
    document.getElementById('username').value = user.username || '';
    document.getElementById('email').value = user.email || '';
    document.getElementById('birthDate').value = user.birthDate || '';
    document.getElementById('deathDate').value = user.deathDate || '';
}

/**
 * Настройка формы редактирования профиля
 */
function setupProfileForm() {
    const form = document.getElementById('profileForm');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        // Валидация через FormValidator
        const rules = [
            { field: 'username', required: true, minLength: 3, maxLength: 50,
                messages: { required: 'Введите имя пользователя', minLength: 'Минимум 3 символа', maxLength: 'Максимум 50 символов' } },
            { field: 'email', required: true, type: 'email',
                messages: { required: 'Введите email', email: 'Введите корректный email' } },
            { field: 'birthDate', required: true, messages: { required: 'Выберите дату рождения' } },
            { field: 'deathDate', required: true, messages: { required: 'Выберите дату смерти' } }
        ];

        const data = FormValidator.validateForm('profileForm', rules);
        if (!data) {
            const firstError = document.querySelector('.is-invalid + .invalid-feedback')?.textContent;
            toast.warning(firstError || 'Пожалуйста, исправьте ошибки в форме');
            return;
        }

        // Дополнительная проверка дат (deathDate > birthDate)
        if (new Date(data.deathDate) <= new Date(data.birthDate)) {
            toast.warning('Дата смерти должна быть позже даты рождения');
            return;
        }

        const btn = document.getElementById('saveProfileBtn');
        const originalText = btn.innerHTML;
        try {
            btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Сохранение...';
            btn.disabled = true;

            const response = await fetch('/api/users/me', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    ...getCsrfHeaders()
                },
                credentials: 'same-origin',
                body: JSON.stringify(data)
            });

            // Случай смены username (401 Unauthorized)
            if (response.status === 401) {
                const errorData = await response.json();
                toast.warning(errorData.message || 'Имя пользователя изменено. Войдите заново.');
                setTimeout(() => window.location.href = '/welcome', 2000);
                return;
            }

            // Успешное обновление (200 OK)
            if (response.ok) {
                const updatedUser = await response.json();
                fillProfileForm(updatedUser);
                toast.success('Профиль успешно обновлён');
                setTimeout(() => location.reload(), 1500);
                return;
            }

            // Обработка ошибок валидации и бизнес-ошибок (400, 409 и т.д.)
            const errorData = await response.json();
            if (errorData.errors) {
                // Ошибки валидации полей (от MethodArgumentNotValidException)
                Object.entries(errorData.errors).forEach(([field, message]) => {
                    const input = document.querySelector(`[name="${field}"]`);
                    if (input) FormValidator.showError(input, message);
                });
                toast.warning('Пожалуйста, исправьте ошибки в форме');
            } else if (errorData.message) {
                toast.error(errorData.message);
            } else {
                toast.error('Ошибка при сохранении профиля');
            }
        } catch (error) {
            console.error(error);
            toast.error(error.message || 'Ошибка соединения с сервером');
        } finally {
            btn.innerHTML = originalText;
            btn.disabled = false;
        }
    });
}

/**
 * Настройка формы смены пароля
 */
function setupPasswordForm() {
    const form = document.getElementById('changePasswordForm');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const rules = [
            { field: 'oldPassword', required: true, messages: { required: 'Введите текущий пароль' } },
            { field: 'newPassword', required: true, minLength: 8,
                messages: { required: 'Введите новый пароль', minLength: 'Пароль должен быть не менее 8 символов' } },
            { field: 'confirmPassword', required: true, messages: { required: 'Подтвердите новый пароль' } }
        ];

        const data = FormValidator.validateForm('changePasswordForm', rules);
        if (!data) {
            toast.warning('Заполните все поля корректно');
            return;
        }

        if (data.newPassword !== data.confirmPassword) {
            toast.warning('Новый пароль и подтверждение не совпадают');
            return;
        }

        const btn = document.getElementById('changePasswordBtn');
        const originalText = btn.innerHTML;
        try {
            btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Смена...';
            btn.disabled = true;

            const payload = {
                oldPassword: data.oldPassword,
                newPassword: data.newPassword,
                confirmPassword: data.confirmPassword
            };

            const response = await fetch('/api/users/me/password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...getCsrfHeaders()
                },
                credentials: 'same-origin',
                body: JSON.stringify(payload)
            });

            const result = await response.json();
            if (!response.ok) {
                throw new Error(result.message || 'Ошибка смены пароля');
            }

            toast.success('Пароль успешно изменён');
            form.reset();
        } catch (error) {
            console.error(error);
            toast.error(error.message);
        } finally {
            btn.innerHTML = originalText;
            btn.disabled = false;
        }
    });
}

/**
 * Получение CSRF-заголовков из мета-тегов
 */
function getCsrfHeaders() {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    return csrfToken ? { [csrfHeader]: csrfToken } : {};
}