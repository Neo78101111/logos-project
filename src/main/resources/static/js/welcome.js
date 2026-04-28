/**
 * WELCOME.JS – инициализация страницы входа/регистрации.
 * Использует FormValidator из validation.js.
 */
document.addEventListener('DOMContentLoaded', function () {
    console.log('Welcome page loaded');

    initLifetimeCalculator();
    initFormValidation();
    initTabNavigation();
});

// ------------------------------------------------------------
// 1. Калькулятор продолжительности жизни
// ------------------------------------------------------------
function initLifetimeCalculator() {
    const birthInput = document.querySelector('input[name="birthDate"]');
    const deathInput = document.querySelector('input[name="deathDate"]');
    if (!birthInput || !deathInput) return;

    // Контейнер для вывода
    const display = document.createElement('div');
    display.className = 'alert alert-info mt-3';
    display.id = 'lifetimeDisplay';
    deathInput.parentNode.appendChild(display);

    // Ограничения дат
    const today = new Date();
    const maxBirth = new Date(today);
    maxBirth.setFullYear(today.getFullYear() - 13);
    birthInput.max = maxBirth.toISOString().split('T')[0];
    birthInput.min = '1900-01-01';

    // Значения по умолчанию
    const defaultBirth = new Date(today);
    defaultBirth.setFullYear(today.getFullYear() - 30);
    birthInput.value = defaultBirth.toISOString().split('T')[0];

    const defaultDeath = new Date(defaultBirth);
    defaultDeath.setFullYear(defaultBirth.getFullYear() + 80);
    deathInput.value = defaultDeath.toISOString().split('T')[0];

    // Первый расчёт
    updateLifetimeDisplay();

    // Слушатели изменений
    birthInput.addEventListener('change', updateLifetimeDisplay);
    deathInput.addEventListener('change', updateLifetimeDisplay);

    function updateLifetimeDisplay() {
        if (!birthInput.value || !deathInput.value) return;

        const birth = new Date(birthInput.value);
        const death = new Date(deathInput.value);
        const display = document.getElementById('lifetimeDisplay');
        if (!display) return;

        if (death <= birth) {
            display.innerHTML = '<i class="fas fa-exclamation-triangle"></i> Дата смерти должна быть позже даты рождения';
            display.className = 'alert alert-danger mt-3';
            return;
        }

        let years = death.getFullYear() - birth.getFullYear();
        let months = death.getMonth() - birth.getMonth();
        let days = death.getDate() - birth.getDate();

        if (days < 0) {
            months--;
            const lastDay = new Date(death.getFullYear(), death.getMonth(), 0).getDate();
            days += lastDay;
        }
        if (months < 0) {
            years--;
            months += 12;
        }

        const totalDays = Math.ceil((death - birth) / (1000 * 3600 * 24));
        let msg = `<i class="fas fa-hourglass-half"></i> Ожидаемая продолжительность жизни: `;
        if (years > 0) msg += `<strong>${years}</strong> лет `;
        if (months > 0) msg += `<strong>${months}</strong> месяцев `;
        if (days > 0 || (years === 0 && months === 0)) msg += `<strong>${days}</strong> дней`;
        msg += ` (всего <strong>${totalDays}</strong> дней)`;

        display.innerHTML = msg;
        display.className = 'alert alert-info mt-3';
    }
}

// ------------------------------------------------------------
// 2. Валидация и отправка форм
// ------------------------------------------------------------
function initFormValidation() {
    // ----- Правила для регистрации -----
    const registerRules = [
        {
            field: 'username', required: true, minLength: 3, maxLength: 50,
            messages: {
                required: 'Введите имя пользователя',
                minLength: 'Минимум 3 символа',
                maxLength: 'Максимум 50 символов'
            }
        },
        {
            field: 'email', required: true, type: 'email',
            messages: {required: 'Введите email', email: 'Введите корректный email'}
        },
        {
            field: 'password', required: true, minLength: 8,
            messages: {required: 'Введите пароль', minLength: 'Пароль должен быть не менее 8 символов'}
        },
        {
            field: 'confirmPassword', required: true,
            messages: {
                required: 'Подтвердите пароль'
            }
        },
        {field: 'birthDate', required: true, messages: {required: 'Выберите дату рождения'}},
        {field: 'deathDate', required: true, messages: {required: 'Выберите дату смерти'}}
    ];

    // ----- Правила для входа -----
    const loginRules = [
        {field: 'username', required: true, messages: {required: 'Введите имя пользователя'}},
        {field: 'password', required: true, messages: {required: 'Введите пароль'}}
    ];

    // ----- Форма регистрации (AJAX) -----
    const regForm = document.getElementById('registerForm');
    if (regForm) {
        regForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const data = FormValidator.validateForm('registerForm', registerRules);
            if (!data) {
                const firstError = document.querySelector('.is-invalid + .invalid-feedback')?.textContent;
                alert(firstError || 'Пожалуйста, исправьте ошибки в форме');
                return;
            }

            const dateErrors = validateRegistrationDates(data);
            if (dateErrors.length > 0) {
                alert(dateErrors[0]); // простое уведомление
                return;
            }

            await submitRegistration(data);
        });

        // Валидация в реальном времени
        setupRealTimeValidation(regForm, registerRules);
    }

    // ----- Форма входа (Spring Security, валидация перед отправкой) -----
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', (e) => {
            const data = FormValidator.validateForm('loginForm', loginRules);
            if (!data) {
                e.preventDefault();
                const firstError = document.querySelector('.is-invalid + .invalid-feedback')?.textContent;
                alert(firstError || 'Заполните все обязательные поля');
                return;
            }
        });

        setupRealTimeValidation(loginForm, loginRules);
    }
}

/**
 * Дополнительная валидация дат (возраст, логика).
 */
function validateRegistrationDates(data) {
    const errors = [];
    const birth = new Date(data.birthDate);
    const death = new Date(data.deathDate);
    const today = new Date();

    const minBirth = new Date(today);
    minBirth.setFullYear(today.getFullYear() - 13);
    if (birth > minBirth) errors.push('Для регистрации необходимо быть старше 13 лет');

    if (death <= birth) errors.push('Дата смерти должна быть позже даты рождения');

    const maxLifetime = new Date(birth);
    maxLifetime.setFullYear(birth.getFullYear() + 120);
    if (death > maxLifetime) errors.push('Максимальная продолжительность жизни – 120 лет');

    return errors;
}

/**
 * Отправка регистрации на сервер (AJAX).
 */
async function submitRegistration(data) {
    const btn = document.querySelector('#registerForm button[type="submit"]');
    const originalText = btn.innerHTML;

    try {
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Регистрация...';
        btn.disabled = true;

        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [document.querySelector('meta[name="_csrf_header"]')?.content]:
                document.querySelector('meta[name="_csrf"]')?.content
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (result.success) {
            alert('Регистрация успешна! Теперь вы можете войти.');

            // Переключаем таб на "Вход"
            document.getElementById('login-tab').click();

            // Автозаполнение
            const loginUser = document.querySelector('#login input[name="username"]');
            const loginPass = document.querySelector('#login input[name="password"]');
            if (loginUser) loginUser.value = data.username;
            if (loginPass) loginPass.value = data.password;
        } else {
            if (result.errors) {
                Object.entries(result.errors).forEach(([field, msg]) => {
                    const input = document.querySelector(`[name="${field}"]`);
                    if (input) FormValidator.showError(input, msg);
                });
            } else {
                alert(result.message || 'Ошибка регистрации');
            }
        }
    } catch (error) {
        console.error(error);
        alert('Ошибка соединения с сервером');
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

/**
 * Валидация в реальном времени (input – очистка, blur – проверка).
 */
function setupRealTimeValidation(form, rules) {
    rules.forEach(rule => {
        const field = form.querySelector(`[name="${rule.field}"]`);
        if (!field) return;

        field.addEventListener('input', () => FormValidator.clearError(field));
        field.addEventListener('blur', () => {
            const error = FormValidator.validateField(rule, field.value.trim());
            if (error) FormValidator.showError(field, error);
        });
    });
}

// ------------------------------------------------------------
// 3. Управление табами (запоминание активной вкладки)
// ------------------------------------------------------------
function initTabNavigation() {
    const tabs = document.getElementById('authTabs');
    if (!tabs) return;

    const saved = localStorage.getItem('activeAuthTab');
    if (saved) {
        const btn = document.querySelector(`[data-bs-target="${saved}"]`);
        if (btn) new bootstrap.Tab(btn).show();
    }

    tabs.addEventListener('show.bs.tab', (e) => {
        localStorage.setItem('activeAuthTab', e.target.getAttribute('data-bs-target'));
    });
}