package dev.nicolas.firebasedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameEt, fullnameEt, emailEt, passwordEt;
    private DatabaseReference dbRef;
    private AppCompatButton registerBtn;
    private TextView loginUserTxt;
    private FirebaseAuth auth;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEt = findViewById(R.id.username_et);
        fullnameEt = findViewById(R.id.fullname_et);
        emailEt = findViewById(R.id.email_et);
        passwordEt = findViewById(R.id.password_et);
        loginUserTxt = findViewById(R.id.login_user_txt);
        registerBtn = findViewById(R.id.register_btn);
        pd = new ProgressDialog(this);

        dbRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        loginUserTxt.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
        registerBtn.setOnClickListener(v -> {
            String username = usernameEt.getText().toString();
            String fullname = fullnameEt.getText().toString();
            String email = emailEt.getText().toString();
            String password = passwordEt.getText().toString();
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(fullname)
                    || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(RegisterActivity.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Password too short", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(username, fullname, email, password);
            }
        });
    }

    private void registerUser(String username, String fullname, String email, String password) {
        pd.setMessage("Creating user...");
        pd.setIndeterminate(true);
        pd.show();

        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            HashMap<String, Object> map = new HashMap<>();
            map.put("fullname", fullname);
            map.put("email", email);
            map.put("username", username);
            map.put("id", auth.getCurrentUser().getUid());
            map.put("bio", "");
            map.put("imgUrl", "default");

            dbRef.child("Users").child(auth.getCurrentUser().getUid()).setValue(map)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            Toast.makeText(RegisterActivity.this, "Update the profile for better experience",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            finish();
                        }
                    }).addOnFailureListener(e -> {
                        pd.dismiss();
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}