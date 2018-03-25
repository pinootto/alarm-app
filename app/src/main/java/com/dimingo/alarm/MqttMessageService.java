package com.dimingo.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MqttMessageService extends Service {

    private static final String TAG = "MqttMessageService";
    private boolean checkBoxAudio;
    private TextToSpeech tts;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private String clientId;

    public MqttMessageService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
//        EventBus.getDefault().register(this);

        EventBus.getDefault().register(this);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });

        clientId = Utils.generateClientId("service");
        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, clientId);

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String s) {
                if (reconnect) {
//                    setMessageNotification("debug", "service: reconnected");
                    Log.i(TAG, "service: reconnected");
                }
                try {
                    pahoMqttClient.subscribe(mqttAndroidClient, Constants.SUBSCRIBE_TOPIC, Constants.QOS);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
//                setMessageNotification("debug", "service: connection lost");
                Log.i(TAG, "service: connection lost");
                try {
                    mqttAndroidClient.connect(pahoMqttClient.getMqttConnectionOption());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                String message = new String(mqttMessage.getPayload());
                Log.i(TAG, "service: message received");
                Log.i(TAG, "service: topic = " + topic + ", message = " + message);
                EventBus.getDefault().post(new MessageEvent(message));
                if (!message.equals("0")) {
                    setMessageNotification(topic, message);
                    if (checkBoxAudio) {
//                    setMessageNotification("debug", "service: checkBoxAudio = true");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Red Alarm APP")
                .setTicker("Production Red Alarm")
                .setContentText("monitoring...")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build();

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);

        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        EventBus.getDefault().unregister(this);
        stopForeground(true);
        super.onDestroy();
    }


    private void setMessageNotification(@NonNull String topic, @NonNull String msg) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_message_black_24dp)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setCategory(Notification.CATEGORY_MESSAGE)
                        .setContentTitle("Red Alarm")
//                        .setContentText(topic + ": " + msg);
                        .setContentText(msg);
        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Constants.NOTIFICATION_ID.MESSAGE, mBuilder.build());
    }


    // This method will be called when a BooleanEvent is posted
    @Subscribe
    public void onBooleanEvent(BooleanEvent event) {
        checkBoxAudio = event.value;
    }


}
