const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendWelcomeNotification = functions.auth.user()
    .onCreate(async (user) => {
        let notification = await admin.firestore().doc('/notification/welcome').get();

        var payload = {
            notification: {
                title: notification.data().title,
                body: notification.data().body,
                sound: "default",

            },
        };

        let tokenRef = await admin.firestore().collection('token').doc(user.uid).get();
        let fcmToken = tokenRef.data().token;
        const response = await admin.messaging().sendToDevice(fcmToken, payload);

    });


exports.updateTransactions = functions.firestore.document('user/{userID}/note/{noteID}')
    .onWrite(async (change, context) => {

        // let transaction = await admin.firestore().doc('/admin/admin').get();
        // transactions.data().transactions

        // let transaction = await admin.firestore().doc('/admin/admin').se;

        const transaction = await admin.firestore().collection('admin').doc('admin');

        const res = await transaction.update({
            transactions: admin.firestore.FieldValue.increment(1)
        });

    });


exports.sendMidnightNotification = functions.firestore.document('notification/midnight')
    .onUpdate(async (change, context) => {

        const newValue = change.after.data();

        var payload = {
            notification: {
                title: change.after.data().title,
                body: change.after.data().body,
                image: change.after.data().image,
                sound: "default",

            },
        };

        const response = await admin.messaging().sendToTopic("allUsers", payload);

    });

exports.sendMorningNotification = functions.firestore.document('notification/morning')
    .onUpdate(async (change, context) => {

        const newValue = change.after.data();

        var payload = {
            notification: {
                title: change.after.data().title,
                body: change.after.data().body,
                image: change.after.data().image,
                sound: "default",
            },
        };

        const response = await admin.messaging().sendToTopic("allUsers", payload);

    });

exports.sendRandomNotification = functions.firestore.document('notification/random')
    .onUpdate(async (change, context) => {

        const newValue = change.after.data();

        var payload = {
            notification: {
                title: change.after.data().title,
                body: change.after.data().body,
                image: change.after.data().image,
                pushType: "banner",
                vibrate: "true",
                alert : "You have a notification",
                sound: "default",
                priority: "high",

            },
        };

        const response = await admin.messaging().sendToTopic("allUsers", payload);

    });