package org.fdroid.fdroid.authorisation;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import org.fdroid.fdroid.Preferences;
import org.fdroid.fdroid.net.SelfSignSslFactory;
import org.fdroid.fdroid.receiver.DeviceIdReceiver;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class Authorisation {

    private final static String urlPostfix = "/api/v1";
    private final static String urlPrefix = "auth.";
    private final static String TAG = "Authorisation";
    public final static String DNS_ERROR = "-1";
    public final static String IO_ERROR = "-2";
    public final static String JSON_ERROR = "-3";
    public final static String NO_TOKEN = "-4";
    public final static String FORBIDDEN = "403";
    public final static String INTERNAL_ERROR = "500";
    private final static int TIMEOUT = 5000;
    public final static String RECEIVER_INTENT = "AUTH_FAILED";
    public final static String MESSAGE_ERROR = "Token error";


    public static void repoInit(final Context context){
        final InitDialog initDialog = new InitDialog(context);
        final AsyncTask<Void, Void, JSONObject> repoInitTask = new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                initDialog.show();
            }

            @Override
            protected JSONObject doInBackground(Void... objects) {
                Preferences instance = Preferences.get();
                String token = instance.getAccessToken();
                String hostname = instance.getHostName();
                try {
                    if (token.equals("")) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("err", NO_TOKEN);
                        return jsonObject;
                    }
                    Uri uri = Uri.parse("https://" + urlPrefix + hostname + urlPostfix + "/get_repo_list");
                    URL url = new URL(uri.toString());
                    HttpURLConnection httpURLConnection = certification((HttpURLConnection) url.openConnection(), context);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setConnectTimeout(TIMEOUT);
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
                    int responseCode = httpURLConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        StringBuilder stringBuffer = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuffer.append(line);
                        }
                        return new JSONObject(stringBuffer.toString());
                    }
                    else if(responseCode == HttpURLConnection.HTTP_FORBIDDEN || responseCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("err", FORBIDDEN);
                        return jsonObject;
                    }
                    else{
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("err", String.valueOf(responseCode));
                        return jsonObject;
                    }
                }
                catch(IOException e){
                    Log.e(TAG, e.getMessage());
                    return new JSONObject();
                }
                catch(JSONException e){
                    Log.e(TAG, e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                if(jsonObject == null){
                    initDialog.error(JSON_ERROR);
                }
                else if(jsonObject.toString().equals("{}")){
                    initDialog.error(IO_ERROR);
                }
                else if(jsonObject.has("err")){
                    try {
                        String err = jsonObject.getString("err");
                        if(err.equals(FORBIDDEN))
                            initDialog.forbidden();
                        else
                            initDialog.error(err);
                    }
                    catch(JSONException e){
                        Log.e(TAG, e.getMessage());
                        initDialog.error(JSON_ERROR);
                    }
                }
                else {
                    initDialog.setRepo(jsonObject);
                    Preferences.get().setAuthRun(false);
                }
            }
        };
        repoInitTask.execute();
    }

    public static void validationOtp(final String otp,final Context context){
        final JwtDialog jwtDialog = new JwtDialog(context);
        AsyncTask<String, Void, String> jwtRequestTask = new AsyncTask<String, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                jwtDialog.show();
            }

            @Override
            protected String doInBackground(String... values) {
                String otp = values[0];
                Preferences instance = Preferences.get();
                final String hostname = instance.getHostName();
                final String deviceId = instance.getDeviceID();
                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("client_id", deviceId);
                    jsonObject.put("client_otp", otp);
                    Uri uri = Uri.parse("https://" + urlPrefix + hostname + urlPostfix + "/login");
                    URL url = new URL(uri.toString());
                    HttpURLConnection httpURLConnection = certification((HttpURLConnection) url.openConnection(), context);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setConnectTimeout(TIMEOUT);
                    httpURLConnection.setRequestProperty("Content-Type","application/json");
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    outputStream.write(jsonObject.toString().getBytes());
                    outputStream.flush();
                    int responseCode = httpURLConnection.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_CREATED) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        StringBuilder stringBuffer = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null){
                            stringBuffer.append(line);
                        }
                        return new JSONObject(stringBuffer.toString()).getString(
                                "access_token");
                    }
                    return String.valueOf(responseCode);
                }
                catch(UnknownHostException e){
                    Log.e(TAG, e.getMessage());
                    return DNS_ERROR;
                }
                catch(IOException e){
                    Log.e(TAG, e.getMessage());
                    return IO_ERROR;
                }
                catch(JSONException e){
                    Log.e(TAG, e.getMessage());
                    return JSON_ERROR;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if(s.length() > 3){
                    jwtDialog.setToken(s);
                }
                else {
                    jwtDialog.error(s);
                }
            }
        };
        jwtRequestTask.execute(otp);
    }

    public static void requestOtp(final Context context){

        final OtpDialog otpDialog = new OtpDialog(context);
        final DeviceIdReceiver deviceIdReceiver = new DeviceIdReceiver();
        final AsyncTask<Void, Void, Integer> otpRequestTask = new AsyncTask<Void, Void, Integer>() {
            /**
             * Request OTP code from server
             * Custom responses:
             */

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Integer doInBackground(Void... preferences) {
                Preferences instance = Preferences.get();
                final String hostname = instance.getHostName();
                final String deviceId = instance.getDeviceID();
                final String jsonStr = "{\"client_id\": \"" + deviceId + "\"}";

                Uri uri = Uri.parse("https://" + urlPrefix + hostname + urlPostfix + "/otp_request");

                try {
                    URL url = new URL(uri.toString());
                    HttpURLConnection httpURLConnection = certification((HttpURLConnection) url.openConnection(), context);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setConnectTimeout(TIMEOUT);
                    httpURLConnection.setRequestProperty("Content-Type","application/json");
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    outputStream.write(jsonStr.getBytes());
                    outputStream.flush();
                    return httpURLConnection.getResponseCode();
                }
                catch(UnknownHostException e){
                    Log.e(TAG, e.getMessage());
                    return Integer.parseInt(Authorisation.DNS_ERROR);
                }
                catch(IOException e){
                    Log.e(TAG, e.getMessage());
                    return Integer.parseInt(Authorisation.IO_ERROR);
                }
            }

            @Override
            protected void onPostExecute(Integer reposeCode) {
                super.onPostExecute(reposeCode);
                if(reposeCode == HttpURLConnection.HTTP_OK){
                    otpDialog.correctDialog();
                }
                else if(reposeCode == HttpURLConnection.HTTP_NOT_FOUND || reposeCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT){
                    otpDialog.notFoundDialog();
                }
                else if(reposeCode == HttpURLConnection.HTTP_FORBIDDEN){
                    otpDialog.forbiddenDialog();
                }
                else {
                    otpDialog.error(String.valueOf(reposeCode));
                }


            }
        };
        final AsyncTask<Void, Void, Integer> deviceIdTask = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Preferences.get().setDeviceID("");
            }

            @Override
            protected Integer doInBackground(Void... voids) {
                /**
                Returned codes:
                    0: OK
                    1: Interrupted
                    2: Timeout
                 */
                Intent intent = new Intent(DeviceIdReceiver.ODIN_ACTION);
                intent.putExtra(DeviceIdReceiver.EXTRA_REQUEST, DeviceIdReceiver.DEVICE_ID_MESSAGE);
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                context.getApplicationContext().sendBroadcast(intent);
                //Time duration for broadcast response
                final int maxTime = 20000;
                int count = 0;
                final int timeInterval = 100;
                while (Preferences.get().getDeviceID().equals("") && count < maxTime){
                    try {
                        count += timeInterval;
                        Thread.sleep(timeInterval);
                    }
                    catch(InterruptedException e){
                        return 1;
                    }
                }
                if(count >= timeInterval)
                {
                    return 2;
                }
                return 0;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                final String deviceId = Preferences.get().getDeviceID();
                if(!deviceId.equals(""))
                    otpRequestTask.execute();
                else{
                    otpDialog.noDeviceIdDialog();
                }
                otpDialog.setDeviceIdTextView(deviceId);
                context.getApplicationContext().unregisterReceiver(deviceIdReceiver);
            }
        };
        context.getApplicationContext().registerReceiver(deviceIdReceiver, new IntentFilter(DeviceIdReceiver.INTENT_FILTER));
        otpDialog.show();
        deviceIdTask.execute();
    }

    /**
     * Returned {@link HttpsURLConnection} with self sign SSL factory {@link SelfSignSslFactory} for privet CA
     */
    private static HttpURLConnection certification(HttpURLConnection connection, Context context){
        if(connection instanceof HttpsURLConnection){
            SelfSignSslFactory selfSignSslFactory = new SelfSignSslFactory(context);
            SSLSocketFactory factory = selfSignSslFactory.getSocketFactory();
            if(factory != null){
                ((HttpsURLConnection)connection).setSSLSocketFactory(factory);
            }
        }
        return connection;
    }

    public static void check(@NonNull Context context){
         if(Preferences.get().getAccessToken().equals("")){
            requestOtp(context);
         }
         else {
            repoInit(context);
         }
    }
}
