// 📁 src/utils/pushManager.js
export async function subscribeUserToPush() {
    if (!("serviceWorker" in navigator)) {
        console.warn("❌ Service workers не поддерживаются в этом браузере");
        return null;
    }

    // 🔑 Проверяем, зарегистрирован ли SW
    const reg = await navigator.serviceWorker.ready;

    // 📡 Запрашиваем разрешение на уведомления
    const permission = await Notification.requestPermission();
    if (permission !== "granted") {
        console.warn("❌ Пользователь не разрешил уведомления");
        return null;
    }

    // 🔐 Подписываем пользователя
    const vapidKey = process.env.REACT_APP_VAPID_PUBLIC_KEY;
    if (!vapidKey) {
        console.error("⚠️ VAPID ключ не найден. Добавь его в .env как REACT_APP_VAPID_PUBLIC_KEY");
        return null;
    }

    const subscription = await reg.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(vapidKey),
    });

    console.log("✅ Подписка создана:", subscription);

    // 📦 Возвращаем данные для бэкенда
    return {
        endpoint: subscription.endpoint,
        keys: {
            p256dh: subscription.toJSON().keys.p256dh,
            auth: subscription.toJSON().keys.auth
        }
    };
}

// 📌 Вспомогательная функция для преобразования VAPID ключа
function urlBase64ToUint8Array(base64String) {
    const padding = "=".repeat((4 - (base64String.length % 4)) % 4);
    const base64 = (base64String + padding)
        .replace(/-/g, "+")
        .replace(/_/g, "/");

    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);

    for (let i = 0; i < rawData.length; ++i) {
        outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
}
