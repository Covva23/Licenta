package com.example.licenta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView newUserTextView, forgotPasswordTextView;

    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        newUserTextView = findViewById(R.id.newUserTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        newUserTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to registration activity
                Intent intent = new Intent(Login.this, Inregistrare.class);
                startActivity(intent);
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, ResetareParola.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || !validareEmail(email)) {
            Toast.makeText(Login.this, "Introduceți un email valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(Login.this, "Introduceți o parolă.", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                            if (currentUser != null) {
                                String uid = currentUser.getUid();

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("uid", uid);
                                editor.apply();

                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                CollectionReference usersCollection = db.collection("Utilizatori");
                                Query query = usersCollection.whereEqualTo("uid", uid);

                                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                                String nume = document.getString("nume");
                                                String prenume = document.getString("preunume");
                                                String telefon = document.getString("telefon");
                                                int varsta = document.getLong("varsta").intValue();
                                                String afectiuni = document.getString("afectiuni");
                                                String email = document.getString("email");
                                                String parola = document.getString("parola");

                                            }
                                        } else {
                                            Toast.makeText(Login.this, "Eroare la obținerea detaliilor utilizatorului.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                            Intent intent = new Intent(Login.this, PaginaPrincipala.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Login.this, "Email sau parolă incorecte.", Toast.LENGTH_SHORT).show();
                            loginButton.setBackgroundColor(getResources().getColor(R.color.red_button));

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loginButton.setBackgroundColor(getResources().getColor(R.color.green_button));
                                }
                            }, 5000);
                        }
                    }
                });
    }

    // Email validation using regular expressions
    private boolean validareEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }
}
