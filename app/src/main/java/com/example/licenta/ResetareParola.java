package com.example.licenta;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class ResetareParola extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;

    private FirebaseFirestore db;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetare_parola);

        emailEditText = findViewById(R.id.emailEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                resetPassword(email);
            }
        });

        TextView backToLoginTextView = findViewById(R.id.backToLoginTextView);
        backToLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResetareParola.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void resetPassword(String email) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Introduceți adresa de email", Toast.LENGTH_SHORT).show();
            return;
        }

        checkIfEmailExists(email);
    }

    private void checkIfEmailExists(final String email) {
        CollectionReference usersRef = db.collection("Utilizatori");
        Query query = usersRef.whereEqualTo("email", email);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (!querySnapshot.isEmpty()) {
                        sendResetPasswordEmail(email);
                    } else {
                        Toast.makeText(ResetareParola.this, "Emailul introdus nu se află în baza de date", Toast.LENGTH_SHORT).show();
                        resetPasswordButton.setBackgroundColor(Color.RED);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                resetPasswordButton.setBackgroundColor(getResources().getColor(R.color.green_button));
                            }
                        }, 5000);
                    }
                } else {
                    Toast.makeText(ResetareParola.this, "Eroare la accesarea bazei de date", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendResetPasswordEmail(String email) {
        fAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ResetareParola.this, "Emailul de resetare a parolei a fost trimis.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ResetareParola.this, Login.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ResetareParola.this, "Nu s-a găsit această adresă de email în baza de date.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
