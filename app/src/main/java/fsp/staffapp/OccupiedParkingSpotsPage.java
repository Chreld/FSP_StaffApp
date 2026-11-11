package fsp.staffapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import fsp.staffapp.template.Template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OccupiedParkingSpotsPage extends Template {

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private EditText spotInput;
    private Button searchBtn;
    private Button reportBtn;
    private TextView result;

    @Override
    protected void onCreate(Bundle instance) {
        super.onCreate(instance);
        setContentView(R.layout.activity_occupied_parking_spots);

        spotInput = findViewById(R.id.spotInput);
        searchBtn = findViewById(R.id.searchBtn);
        result = findViewById(R.id.result);
        reportBtn = findViewById(R.id.reportBtn);

        reportBtn.setVisibility(View.GONE);

        searchBtn.setOnClickListener(view -> searchParkingSpot());

        reportBtn.setOnClickListener(view -> {
            String parkingSpot = spotInput.getText().toString().trim().toUpperCase();

            Intent intent = new Intent(OccupiedParkingSpotsPage.this, ReportIllegalParkingPage.class);

            intent.putExtra("parkingSpot", parkingSpot);
            startActivity(intent);
        });
    }

    private void searchParkingSpot() {
        String parkingSpot = spotInput.getText().toString().trim().toUpperCase();

        if (parkingSpot.isEmpty()) {
            result.setText(R.string.enter_parking_spot);
            result.setVisibility(View.VISIBLE);
            reportBtn.setVisibility(View.GONE);
            return;
        }

        result.setText("Checking parking spot...");
        result.setVisibility(View.VISIBLE);
        reportBtn.setVisibility(View.GONE);
        searchBtn.setEnabled(false);

        executor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL("http://localhost:9090/api/parking-spots/" + Uri.encode(parkingSpot));

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int status = connection.getResponseCode();

                if (status != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP " + status);
                }

                StringBuilder response = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream(),
                                StandardCharsets.UTF_8
                        ))) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                String licensePlate = response.toString().trim();

                runOnUiThread(() -> {
                    if (licensePlate.isEmpty()
                            || licensePlate.equalsIgnoreCase("null")) {

                        result.setText(R.string.no_car_expected);
                        reportBtn.setVisibility(View.VISIBLE);

                    } else {
                        result.setText(getString(
                                R.string.expected_license_plate,
                                licensePlate
                        ));

                        reportBtn.setVisibility(View.GONE);
                    }

                    searchBtn.setEnabled(true);
                });

            } catch (IOException e) {
                android.util.Log.e("OccupiedParkingSpots", "Parking spot lookup failed", e);

                String errorMessage = "Could not retrieve parking spot.";

                if (e.getMessage() != null) {
                    errorMessage += "\n" + e.getMessage();
                }

                String finalErrorMessage = errorMessage;

                runOnUiThread(() -> {
                    result.setText(finalErrorMessage);
                    result.setVisibility(View.VISIBLE);
                    reportBtn.setVisibility(View.GONE);
                    searchBtn.setEnabled(true);
                });

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
