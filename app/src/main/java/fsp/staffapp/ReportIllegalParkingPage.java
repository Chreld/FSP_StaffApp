package fsp.staffapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import fsp.staffapp.network.ApiClient;
import fsp.staffapp.template.Template;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;

public class ReportIllegalParkingPage extends Template {

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

        String parkingSpot = getIntent().getStringExtra("parkingSpot");

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

        // Ask ApiClient to submit the report
        ApiClient.reportIllegalParking(report, new ApiClient.ApiCallback() {

            @Override
            public void onSuccess(String response) {

                reportResult.setText("Report submitted successfully.");
                submitReportBtn.setEnabled(true);
            }

            @Override
            public void onError(String error) {

                reportResult.setText("Could not submit report: " + error);
                submitReportBtn.setEnabled(true);
            }
        });
    }
}
