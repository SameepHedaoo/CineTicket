const listeners = new Set();
let nextNotificationId = 1;

const emit = (payload) => {
    listeners.forEach((listener) => listener(payload));
};

export const subscribeToNotifications = (listener) => {
    listeners.add(listener);
    return () => listeners.delete(listener);
};

export const notifySuccess = (message) => {
    emit({
        id: nextNotificationId++,
        type: "success",
        message: message || "Action completed successfully.",
    });
};
