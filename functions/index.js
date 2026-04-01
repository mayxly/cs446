const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

exports.sendPushNotification = onDocumentCreated(
  "users/{userId}/notifications/{notificationId}",
  async (event) => {
    const notification = event.data.data();
    const userId = event.params.userId;

    const userDoc = await getFirestore().doc(`users/${userId}`).get();
    const user = userDoc.data();

    if (!user || !user.pushNotificationsEnabled || !user.fcmToken) {
      return;
    }

    const message = {
      token: user.fcmToken,
      notification: {
        title: notification.fromUserName || "Align",
        body: notification.message || "You have a new notification",
      },
      data: {
        type: notification.type || "",
      },
    };

    try {
      await getMessaging().send(message);
    } catch (error) {
      console.error("Failed to send push notification:", error);
    }
  }
);
