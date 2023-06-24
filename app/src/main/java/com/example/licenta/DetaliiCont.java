package com.example.licenta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.licenta.ui.Utilizator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class DetaliiCont extends AppCompatActivity {

    private TextView numeTextView;
    private TextView prenumeTextView;
    private TextView varstaTextView;
    private TextView afectiuniTextView;
    private TextView telefonTextView;
    private TextView emailTextView;
    private TextView nCTextView;
    private TextView pCTextView;
    private TextView tCTextView;
    private Button incarcaDateButton;

    private FirebaseFirestore db;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalii_cont);

        // Initialize views
        numeTextView = findViewById(R.id.valoareNumeTextView);
        prenumeTextView = findViewById(R.id.valoarePrenumeTextView);
        varstaTextView = findViewById(R.id.valoareVarstaTextView);
        afectiuniTextView = findViewById(R.id.valoareAfectiuniTextView);
        telefonTextView = findViewById(R.id.valoareTelefonTextView);
        emailTextView = findViewById(R.id.valoareEmailTextView);
        nCTextView = findViewById(R.id.valoareNCTextView);
        pCTextView = findViewById(R.id.valoarePCTextView);
        tCTextView = findViewById(R.id.valoareTCTextView);
        incarcaDateButton = findViewById(R.id.incarcaDateButton);


        db = FirebaseFirestore.getInstance();

        // Get the current user's UID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUid = currentUser.getUid();
        }

        incarcaDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieveUserData();
            }
        });
        TextView backToMainTextView = findViewById(R.id.backToMainTextView);

        backToMainTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(DetaliiCont.this, PaginaPrincipala.class);
                startActivity(intent);
                finish();
            }
        });
    }


    private void retrieveUserData() {
        CollectionReference usersCollection = db.collection("Utilizatori");
        Query query = usersCollection.whereEqualTo("uid", currentUid);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {

                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        Utilizator user = document.toObject(Utilizator.class);
                        if (user != null) {
                            numeTextView.setText(user.getNume());
                            prenumeTextView.setText(user.getPrenume());
                            varstaTextView.setText(String.valueOf(user.getVarsta()));
                            afectiuniTextView.setText(user.getAfectiuni());
                            telefonTextView.setText(user.getTelefon());
                            emailTextView.setText(user.getEmail());
                            nCTextView.setText(user.getNumeContact());
                            pCTextView.setText(user.getPrenumeContact());
                            tCTextView.setText(user.getTelefonContact());
                        }
                    } else {
                        Toast.makeText(DetaliiCont.this, "Documentul nu exista.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DetaliiCont.this, "Eroare la preluarea datelor..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}