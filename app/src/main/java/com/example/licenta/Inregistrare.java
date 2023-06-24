package com.example.licenta;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.licenta.ui.Utilizator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Inregistrare extends AppCompatActivity {

    private EditText numeEditText, prenumeEditText, telefonEditText, varstaEditText, afectiuniEditText,
            emailEditText, parolaEditText;
    private Button registerButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inregistrare);

        numeEditText = findViewById(R.id.numeEditText);
        prenumeEditText = findViewById(R.id.prenumeEditText);
        telefonEditText = findViewById(R.id.telefonEditText);
        varstaEditText = findViewById(R.id.varstaEditText);
        afectiuniEditText = findViewById(R.id.afectiuniEditText);
        emailEditText = findViewById(R.id.emailEditText);
        parolaEditText = findViewById(R.id.parolaEditText);
        registerButton = findViewById(R.id.registerButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obținere valorile introduse în câmpurile de înregistrare
                String nume = numeEditText.getText().toString().trim();
                String prenume = prenumeEditText.getText().toString().trim();
                String telefon = telefonEditText.getText().toString().trim();
                String varstaStr = varstaEditText.getText().toString().trim();
                String afectiuni = afectiuniEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String parola = parolaEditText.getText().toString().trim();

                if (!isValidEmail(email)) {
                    emailEditText.setError("Introduceți un email valid");
                    emailEditText.requestFocus();
                    return;
                }

                if (!isValidPassword(parola)) {
                    parolaEditText.setError("Introduceți o parolă validă");
                    parolaEditText.requestFocus();
                    showPasswordRequirementsDialog();
                    return;
                }

                if (!isValidPhoneNumber(telefon)) {
                    telefonEditText.setError("Număr de telefon invalid");
                    telefonEditText.requestFocus();
                    return;
                }

                int varsta = Integer.parseInt(varstaStr);
                if (!isValidAge(varsta)) {
                    varstaEditText.setError("Vârstă invalidă");
                    varstaEditText.requestFocus();
                    return;
                }

                Utilizator utilizator = new Utilizator(nume, prenume, telefon, varsta, afectiuni, email, parola);
                utilizator.setNumeContact("");
                utilizator.setPrenumeContact("");
                utilizator.setTelefonContact("");

                createNewUser(utilizator);
            }
        });

        TextView backToLoginTextView = findViewById(R.id.backToLoginTextView);
        backToLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Inregistrare.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        parolaEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordRequirementsDialog();
            }
        });
    }

    private boolean isValidEmail(CharSequence email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";
        return password.matches(regex);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        String regex = "^07\\d{8}$";
        return phoneNumber.matches(regex);
    }

    private boolean isValidAge(int age) {
        return age >= 0 && age <= 120;
    }

    private void showPasswordRequirementsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerințe pentru parolă");
        builder.setMessage("• Cel puțin o literă mare\n" +
                "• Cel puțin o literă mică\n" +
                "• Cel puțin o cifră\n" +
                "• Minim 8 caractere");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void createNewUser(Utilizator utilizator) {
        mAuth.createUserWithEmailAndPassword(utilizator.getEmail(), utilizator.getParola())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            String uid = mAuth.getCurrentUser().getUid();

                            utilizator.setUid(uid);

                            CollectionReference utilizatoriRef = db.collection("Utilizatori");

                            utilizatoriRef.add(utilizator)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(Inregistrare.this, "Înregistrare realizată cu succes", Toast.LENGTH_SHORT).show();
                                                finish(); // Încheiere activitate după înregistrare reușită
                                            } else {
                                                Toast.makeText(Inregistrare.this, "Eroare la înregistrare", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(Inregistrare.this, "Eroare la înregistrare: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUtilizatorDocument(DocumentReference documentReference, Utilizator utilizator) {
        documentReference.set(utilizator)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Inregistrare.this, "Înregistrare realizată cu succes", Toast.LENGTH_SHORT).show();
                            finish(); // Încheiere activitate după înregistrare reușită
                        } else {
                            Toast.makeText(Inregistrare.this, "Eroare la înregistrare", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
