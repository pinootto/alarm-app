package com.dimingo.alarm;


public class Constants {

    public static final String MQTT_BROKER_URL = "tcp://xxx:1883";

    public static final String MQTT_BROKER_USERNAME = "xxx";
    public static final String MQTT_BROKER_PASSWORD = "xxx";

    public static final String PUBLISH_TOPIC = "alarm";
    public static final String SUBSCRIBE_TOPIC = "alarm";

    public static final int QOS = 0;

    public interface NOTIFICATION_ID {
        int MESSAGE = 100;
        int FOREGROUND_SERVICE = 1337;
    }

}

