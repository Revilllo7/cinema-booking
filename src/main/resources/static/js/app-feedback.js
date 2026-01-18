(() => {
    const STACK_ID = 'notificationStack';
    const DEFAULT_TIMEOUT = 6000;

    const TYPE_CLASS_MAP = {
        success: 'success',
        error: 'danger',
        danger: 'danger',
        warning: 'warning',
        info: 'info',
        primary: 'primary'
    };

    function ensureStack() {
        let stack = document.getElementById(STACK_ID);
        if (!stack) {
            stack = document.createElement('div');
            stack.id = STACK_ID;
            stack.className = 'notification-stack';
            stack.setAttribute('aria-live', 'polite');
            stack.setAttribute('aria-atomic', 'true');
            document.body.appendChild(stack);
        }
        return stack;
    }

    function sanitize(text) {
        if (text === null || text === undefined) {
            return '';
        }
        return String(text).trim();
    }

    function formatFieldName(name) {
        if (!name) return '';
        return name
            .replace(/([A-Z])/g, ' $1')
            .replace(/[_\-]+/g, ' ')
            .replace(/\s+/g, ' ')
            .replace(/^./, ch => ch.toUpperCase())
            .trim();
    }

    function buildDetailList(details) {
        if (!details || !details.length) return null;
        const list = document.createElement('ul');
        list.className = 'notification-details';
        details.forEach(item => {
            const li = document.createElement('li');
            li.textContent = sanitize(item);
            list.appendChild(li);
        });
        return list;
    }

    function autoDismiss(card, timeout) {
        if (!timeout || timeout < 0) return;
        setTimeout(() => dismiss(card), timeout);
    }

    function dismiss(card) {
        if (!card) return;
        card.classList.add('notification-hide');
        card.addEventListener('animationend', () => card.remove(), { once: true });
    }

    function showNotification({ type = 'info', title, message, details, timeout = DEFAULT_TIMEOUT, sticky = false, icon = null } = {}) {
        const stack = ensureStack();
        const alertClass = TYPE_CLASS_MAP[type] || TYPE_CLASS_MAP.info;
        const card = document.createElement('div');
        card.className = `notification-card alert alert-${alertClass}`;
        card.setAttribute('role', 'status');

        const header = document.createElement('div');
        header.className = 'notification-header';

        const titleEl = document.createElement('strong');
        titleEl.className = 'notification-title';
        titleEl.textContent = sanitize(title || type.toUpperCase());

        if (icon) {
            const iconEl = document.createElement('i');
            iconEl.className = `notification-icon ${icon}`;
            header.appendChild(iconEl);
        } else {
            const defaultIcon = document.createElement('i');
            defaultIcon.className = 'notification-icon bi bi-info-circle';
            header.appendChild(defaultIcon);
        }

        header.appendChild(titleEl);

        const closeBtn = document.createElement('button');
        closeBtn.type = 'button';
        closeBtn.className = 'btn-close notification-close';
        closeBtn.setAttribute('aria-label', 'Close notification');
        closeBtn.addEventListener('click', () => dismiss(card));
        header.appendChild(closeBtn);

        card.appendChild(header);

        if (message) {
            const body = document.createElement('div');
            body.className = 'notification-message';
            body.textContent = sanitize(message);
            card.appendChild(body);
        }

        const detailList = buildDetailList(details);
        if (detailList) {
            card.appendChild(detailList);
        }

        stack.appendChild(card);
        if (!sticky) {
            autoDismiss(card, timeout);
        }
        return card;
    }

    async function parseErrorResponse(response) {
        if (!response) {
            return { message: 'Request failed', status: 0 };
        }
        const status = response.status;
        const contentType = response.headers?.get('content-type') || '';
        try {
            if (contentType.includes('application/json')) {
                const data = await response.json();
                return {
                    status,
                    error: data.error,
                    message: data.message || data.error || `Request failed (${status})`,
                    errors: typeof data.errors === 'object' ? data.errors : null,
                    path: data.path,
                    timestamp: data.timestamp,
                    raw: data
                };
            }
            const text = await response.text();
            return {
                status,
                message: text?.trim() || `Request failed (${status})`
            };
        } catch (err) {
            console.error('Error parsing response body', err);
            return {
                status,
                message: `Request failed (${status})`
            };
        }
    }

    function createError(detail = {}) {
        const err = new Error(detail.message || 'Request failed');
        err.status = detail.status;
        err.error = detail.error;
        err.errors = detail.errors;
        err.timestamp = detail.timestamp;
        err.path = detail.path;
        err.raw = detail.raw;
        return err;
    }

    function validationList(errors) {
        if (!errors) return [];
        return Object.entries(errors).map(([field, msg]) => `${formatFieldName(field)}: ${msg}`);
    }

    function clearFieldErrors(form) {
        if (!form) return;
        form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
        form.querySelectorAll('[data-field-feedback]').forEach(el => {
            el.textContent = '';
        });
    }

    function setFieldErrors(form, errors) {
        if (!form || !errors) return;
        Object.entries(errors).forEach(([field, message]) => {
            const control = form.querySelector(`[name="${field}"]`);
            if (control) {
                control.classList.add('is-invalid');
            }
            const feedback = form.querySelector(`[data-field-feedback="${field}"]`);
            if (feedback) {
                feedback.textContent = message;
            }
        });
    }

    const AppFeedback = {
        notify: showNotification,
        success: (title, message, options = {}) => showNotification({ type: 'success', title, message, icon: 'bi bi-check-circle', ...options }),
        error: (title, message, options = {}) => showNotification({ type: 'error', title, message, icon: 'bi bi-exclamation-triangle', ...options }),
        info: (title, message, options = {}) => showNotification({ type: 'info', title, message, icon: 'bi bi-info-circle', ...options }),
        warning: (title, message, options = {}) => showNotification({ type: 'warning', title, message, icon: 'bi bi-exclamation-octagon', ...options }),
        parseErrorResponse,
        createError,
        validationList,
        clearFieldErrors,
        setFieldErrors
    };

    window.AppFeedback = AppFeedback;
})();
