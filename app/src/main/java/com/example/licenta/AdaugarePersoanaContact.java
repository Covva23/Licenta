package com.example.licenta;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AdaugarePersoanaContact extends AppCompatActivity {

    private EditText emailEditText, numeContactEditText, prenumeContactEditText, telefonContactEditText;
    private Button adaugareButton;
    private TextView backToMainTextView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String currentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adaugare_persoana_contact);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserUid = currentUser.getUid();
        } else {
            Toast.makeText(this, "Utilizatorul nu este autentificat.", Toast.LENGTH_SHORT).show();
            return;
        }

        emailEditText = findViewById(R.id.emailEditText);
        numeContactEditText = findViewById(R.id.numeContactEditText);
        prenumeContactEditText = findViewById(R.id.prenumeContactEditText);
        telefonContactEditText = findViewById(R.id.telefonContactEditText);
        adaugareButton = findViewById(R.id.adaugareButton);
        backToMainTextView = findViewById(R.id.backToMainTextView);

        adaugareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                afiseazaDialogConfirmare();
            }
        });

        backToMainTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdaugarePersoanaContact.this, PaginaPrincipala.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void afiseazaDialogConfirmare() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmare")
                .setMessage("Sunteți sigur că doriți să adăugați persoana de contact sau să actualizați datele acesteia?")
                .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adaugaPersoanaContact();
                    }
                })
                .setNegativeButton("Nu", null)
                .show();
    }

    private void adaugaPersoanaContact() {
        String email = emailEditText.getText().toString().trim();
        String numeContact = numeContactEditText.getText().toString().trim();
        String prenumeContact = prenumeContactEditText.getText().toString().trim();
        String telefonContact = telefonContactEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(numeContact) || TextUtils.isEmpty(prenumeContact) || TextUtils.isEmpty(telefonContact)) {
            Toast.makeText(this, "Completați toate câmpurile.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!telefonContact.startsWith("07") || telefonContact.length() != 10 || !TextUtils.isDigitsOnly(telefonContact)) {
            Toast.makeText(this, "Numărul de telefon trebuie să înceapă cu '07' și să conțină exact 10 cifre.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Utilizatori")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot.isEmpty()) {
                                Toast.makeText(AdaugarePersoanaContact.this, "Utilizatorul nu a fost găsit.", Toast.LENGTH_SHORT).show();
                            } else {
                                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                String utilizatorId = document.getId();

                                DocumentReference documentReference = db.collection("Utilizatori").document(utilizatorId);

                                documentReference.update("numeContact", numeContact,
                                                "prenumeContact", prenumeContact,
                                                "telefonContact", telefonContact)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(AdaugarePersoanaContact.this, "Datele au fost actualizate cu succes", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(AdaugarePersoanaContact.this, PaginaPrincipala.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                String errorMessage = "Eroare la actualizarea datelor: " + e.toString();
                                                AlertDialog.Builder builder = new AlertDialog.Builder(AdaugarePersoanaContact.this);
                                                builder.setTitle("Eroare")
                                                        .setMessage(errorMessage)
                                                        .setPositiveButton("OK", null)
                                                        .show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(AdaugarePersoanaContact.this, "Eroare la obținerea datelor utilizatorului.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

