import { useEffect, useRef, useState } from "react";
import { subscribeToNotifications } from "./notificationBus";

const TOAST_TIMEOUT_MS = 3200;

function ToastProvider({ children }) {
    const [toasts, setToasts] = useState([]);
    const timers = useRef(new Map());

    useEffect(() => {
        const unsubscribe = subscribeToNotifications((toast) => {
            setToasts((prev) => [...prev, toast]);
            const timeoutId = setTimeout(() => {
                setToasts((prev) => prev.filter((item) => item.id !== toast.id));
                timers.current.delete(toast.id);
            }, TOAST_TIMEOUT_MS);
            timers.current.set(toast.id, timeoutId);
        });

        return () => {
            unsubscribe();
            timers.current.forEach((timeoutId) => clearTimeout(timeoutId));
            timers.current.clear();
        };
    }, []);

    const dismiss = (id) => {
        const timeoutId = timers.current.get(id);
        if (timeoutId) {
            clearTimeout(timeoutId);
            timers.current.delete(id);
        }
        setToasts((prev) => prev.filter((item) => item.id !== id));
    };

    return (
        <>
            {children}
            <div className="toast-stack" aria-live="polite" aria-atomic="true">
                {toasts.map((toast) => (
                    <div key={toast.id} className={`toast-card toast-${toast.type}`}>
                        <div className="toast-title">Success</div>
                        <div className="toast-message">{toast.message}</div>
                        <button
                            type="button"
                            className="toast-close"
                            onClick={() => dismiss(toast.id)}
                            aria-label="Dismiss notification"
                        >
                            x
                        </button>
                    </div>
                ))}
            </div>
        </>
    );
}

export default ToastProvider;
