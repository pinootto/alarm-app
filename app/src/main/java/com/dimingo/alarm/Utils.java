package com.dimingo.alarm;

/**
 * Created by giovanni on 3/21/18.
 */

public class Utils {

    private static final String CLIENT_ID_PREFIX = "alarm";

    /**
     * Returns a randomly generated client identifier based on the fixed prefix (alarm)
     * and the system time.
     * <p>When cleanSession is set to false, an application must ensure it uses the
     * same client identifier when it reconnects to the server to resume state and maintain
     * assured message delivery.</p>
     * @return a generated client identifier
     * @see MqttConnectOptions#setCleanSession(boolean)
     */
    public static String generateClientId() {
        // length of nanoTime = 15, so total length = 20  < 65535(defined in spec)
        return CLIENT_ID_PREFIX + System.nanoTime();
    }


    /**
     * Returns a randomly generated client identifier based on the prefix (parameter)
     * and the system time.
     * <p>When cleanSession is set to false, an application must ensure it uses the
     * same client identifier when it reconnects to the server to resume state and maintain
     * assured message delivery.</p>
     * @return a generated client identifier
     * @see MqttConnectOptions#setCleanSession(boolean)
     */
    public static String generateClientId(String prefix) {
        return prefix + System.nanoTime();
    }

}
