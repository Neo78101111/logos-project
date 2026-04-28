// validation.js
/**
 * Единый класс для валидации форм.
 * Не зависит от конкретных страниц – переиспользуется в welcome и других формах.
 */
class FormValidator {
    /**
     * Валидирует всю форму по правилам.
     * @param {string} formId - ID формы
     * @param {Array} rules - массив правил для полей
     * @returns {Object|null} - объект с данными формы или null, если есть ошибки
     */
    static validateForm(formId, rules) {
        const form = document.getElementById(formId);
        if (!form) return null;

        let isValid = true;
        const data = {};

        // Очищаем предыдущие ошибки
        this.clearAllErrors(form);

        rules.forEach(rule => {
            const field = form.querySelector(`[name="${rule.field}"]`);
            if (!field) return;

            const value = field.value.trim();
            data[rule.field] = value;

            const error = this.validateField(rule, value);
            if (error) {
                this.showError(field, error);
                isValid = false;
            }
        });

        return isValid ? data : null;
    }

    /**
     * Валидация одного поля.
     */
    static validateField(rule, value) {
        // Обязательное поле
        if (rule.required && !value) {
            return rule.messages?.required || 'Поле обязательно для заполнения';
        }
        if (!rule.required && !value) return null;

        // Email
        if (rule.type === 'email') {
            const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
            if (!emailRegex.test(value)) {
                return rule.messages?.email || 'Введите корректный email';
            }
            if (/[а-яА-Я]/.test(value)) {
                return 'Email не должен содержать русские буквы';
            }
        }

        // Длина
        if (rule.minLength && value.length < rule.minLength) {
            return rule.messages?.minLength || `Минимум ${rule.minLength} символов`;
        }
        if (rule.maxLength && value.length > rule.maxLength) {
            return rule.messages?.maxLength || `Максимум ${rule.maxLength} символов`;
        }

        return null;
    }

    /**
     * Показать ошибку под полем (Bootstrap-стили).
     */
    static showError(field, message) {
        field.classList.add('is-invalid');

        let errorDiv = field.parentNode.querySelector('.invalid-feedback');
        if (!errorDiv) {
            errorDiv = document.createElement('div');
            errorDiv.className = 'invalid-feedback';
            field.parentNode.appendChild(errorDiv);
        }
        errorDiv.textContent = message;
    }

    /**
     * Убрать ошибку с поля.
     */
    static clearError(field) {
        field.classList.remove('is-invalid');
        const errorDiv = field.parentNode.querySelector('.invalid-feedback');
        if (errorDiv) errorDiv.textContent = '';
    }

    /**
     * Очистить все ошибки в форме.
     */
    static clearAllErrors(form) {
        form.querySelectorAll('.is-invalid').forEach(field => this.clearError(field));
    }
}