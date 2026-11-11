package fsp.staffapp.network;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import fsp.staffapp.config.ApiConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Callback for returning API results
    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    // GET: Retrieve the expected license plate for a parking spot
    public static void getLicensePlate(String parkingSpot, ApiCallback callback) {

        String endpoint = "/api/parking-spots/" + Uri.encode(parkingSpot);

        makeRequest("GET", endpoint, null, callback);
    }

    // POST: Submit an illegal parking report
    public static void reportIllegalParking(JSONObject report, ApiCallback callback) {

        makeRequest("POST", "/api/parking-spots/report", report, callback);
    }

    // Handle the actual HTTP connection
    private static void makeRequest(
            String method,
            String endpoint,
            JSONObject requestBody,
            ApiCallback callback) {

        executor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(ApiConfig.BASE_URL + endpoint);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(method);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                // Send JSON if a request body exists
                if (requestBody != null) {

                    connection.setDoOutput(true);

                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                    byte[] data = requestBody.toString().getBytes(StandardCharsets.UTF_8);

                    try (OutputStream output = connection.getOutputStream()) {
                        output.write(data);
                    }
                }

                int status = connection.getResponseCode();

                InputStream inputStream;

                if (status >= 200 && status < 300) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                }

                StringBuilder response = new StringBuilder();

                if (inputStream != null) {

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    inputStream,
                                    StandardCharsets.UTF_8
                            ))) {

                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }
                }

                if (status < 200 || status >= 300) {
                    throw new IOException("HTTP " + status + ": " + response);
                }

                String result = response.toString().trim();

                // Return the result on the main Android thread
                mainHandler.post(() -> callback.onSuccess(result));

            } catch (IOException e) {

                android.util.Log.e("ApiClient", "API request failed", e);

                mainHandler.post(() ->
                        callback.onError(e.getMessage())
                );

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
}
