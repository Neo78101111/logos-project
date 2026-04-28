// toast.js
class ToastManager {
    constructor() {
        this.container = document.getElementById('toastContainer');
        if (!this.container) {
            this.container = document.createElement('div');
            this.container.id = 'toastContainer';
            this.container.className = 'toast-container position-fixed top-0 end-0 p-3';
            document.body.appendChild(this.container);
        }
        // Задаем высокий z-index, чтобы тосты были поверх модальных окон
        this.container.style.zIndex = '2000';
    }

    show(message, type = 'success', duration = 3000) {
        const toast = document.createElement('div');
        toast.className = `toast align-items-center text-dark bg-${type} border-0`;
        toast.setAttribute('role', 'alert');
        toast.setAttribute('aria-live', 'assertive');
        toast.setAttribute('aria-atomic', 'true');
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;
        this.container.appendChild(toast);
        const bsToast = new bootstrap.Toast(toast, { autohide: true, delay: duration });
        bsToast.show();
        toast.addEventListener('hidden.bs.toast', () => toast.remove());
    }

    success(message, duration) { this.show(message, 'success', duration); }
    error(message, duration = 5000) { this.show(message, 'danger', duration); }
    warning(message, duration) { this.show(message, 'warning', duration); }
    info(message, duration) { this.show(message, 'info', duration); }
}
window.toast = new ToastManager();