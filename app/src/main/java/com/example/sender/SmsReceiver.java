package com.example.sender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");

            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                    String sender = sms.getDisplayOriginatingAddress();
                    String message = sms.getMessageBody();

                    Log.d("SMS_RECEIVED", "From: " + sender + ", Message: " + message);

                    if (containsLatitudeAndLongitude(message)) {
                        Log.d("SMS_MATCH", "Message contains latitude and longitude");
                        String latitude = null;
                        String longitude = null;

                        String pattern = "Latitude:\\s*(-?\\d+\\.?\\d*),\\s*Longitude:\\s*(-?\\d+\\.?\\d*)";
                        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
                        java.util.regex.Matcher m = r.matcher(message);

                        if (m.find()) {
                            latitude = m.group(1);  // First capturing group is latitude
                            longitude = m.group(2); // Second capturing group is longitude
                        }
                        Intent i = new Intent(context, MainActivity.class);
                        i.putExtra("latitude", latitude);
                        i.putExtra("longitude", longitude);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
            }
        }
    }

    public static boolean containsLatitudeAndLongitude(String message) {
        // Define a regular expression pattern for Latitude and Longitude with coordinates
        String pattern = "(?i).*Latitude:\\s*-?\\d+\\.?\\d*,\\s*Longitude:\\s*-?\\d+\\.?\\d*.*";

        // Check if the message matches the pattern
        return message.matches(pattern);
    }
}

