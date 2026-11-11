package fsp.staffapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle instance) {
        super.onCreate(instance);
        setContentView(R.layout.activity_main);

        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Button loginBtn = findViewById(R.id.loginButton);
        TextView loginMessageException = findViewById(R.id.loginMessage);

        loginBtn.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            // Temp data to test login.
            if (username.equals("staff") && password.equals("1234")) {
                Intent intent = new Intent(LoginPage.this, MainMenuPage.class);
                startActivity(intent);

            } else {
                loginMessageException.setText(R.string.login_error);
            }
        });
    }
}
