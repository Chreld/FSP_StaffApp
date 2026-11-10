package fsp.staffapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import fsp.staffapp.template.Template;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class ReportIllegalParkingPage extends Template {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private EditText licensePlateInput;
    private EditText parkingSpotInput;
    private Button submitReportBtn;
    private TextView reportResult;

    @Override
    protected void onCreate(Bundle instance) {
        super.onCreate(instance);
        setContentView(R.layout.activity_report_illegal_parking);

        licensePlateInput = findViewById(R.id.licensePlateInput);
        parkingSpotInput = findViewById(R.id.parkingSpotInput);
        submitReportBtn = findViewById(R.id.submitReportBtn);
        reportResult = findViewById(R.id.reportResult);

        String parkingSpot = getIntent()
                .getStringExtra("parkingSpot");

        if (parkingSpot != null) {
            parkingSpotInput.setText(parkingSpot);
        }

        submitReportBtn.setOnClickListener(view -> submitReport());
    }

    private void submitReport() {
        String licensePlate = licensePlateInput.getText().toString().trim().toUpperCase();

        String parkingSpot = parkingSpotInput.getText().toString().trim().toUpperCase();

        if (licensePlate.isEmpty() || parkingSpot.isEmpty()) {
            reportResult.setText("Please enter a license plate and parking spot.");
            reportResult.setVisibility(View.VISIBLE);
            return;
        }

        JSONObject report = new JSONObject();

        try {
            report.put("licensePlate", licensePlate);
            report.put("parkingSpot", parkingSpot);
            report.put("reportedAt", OffsetDateTime.now().toString());

        } catch (JSONException e) {
            reportResult.setText("Could not prepare the report.");
            reportResult.setVisibility(View.VISIBLE);
            return;
        }

        submitReportBtn.setEnabled(false);
        reportResult.setText("Submitting report...");
        reportResult.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:9090/api/parking-spots/report");

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                byte[] requestBody = report.toString()
                        .getBytes(StandardCharsets.UTF_8);

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(requestBody);
                }

                int status = connection.getResponseCode();

                if (status < 200 || status >= 300) {
                    throw new IOException("HTTP " + status);
                }

                runOnUiThread(() -> {
                    reportResult.setText("Report submitted successfully.");

                    submitReportBtn.setEnabled(true);
                });

            } catch (IOException e) {
                android.util.Log.e("ReportIllegalParking", "Report submission failed", e);

                runOnUiThread(() -> {
                    reportResult.setText("Could not submit report: " + e.getMessage());
                    submitReportBtn.setEnabled(true);
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
