package com.example.licenta;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class PaginaPrincipala extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagina_principala);

        mAuth = FirebaseAuth.getInstance();

        ImageView imageView1 = findViewById(R.id.imageView1);
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PaginaPrincipala.this, Activitatea.class);
                startActivity(intent);
            }
        });

        ImageView imageView2 = findViewById(R.id.imageView2);
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PaginaPrincipala.this, AdaugarePersoanaContact.class);
                startActivity(intent);
            }
        });

        ImageView imageView3 = findViewById(R.id.imageView3);
        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PaginaPrincipala.this, DetaliiCont.class);
                startActivity(intent);
            }
        });

        ImageView imageView4 = findViewById(R.id.imageView4);
        imageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignOutDialog();
            }
        });

        if (!isUidAvailable()) {
            Intent intent = new Intent(PaginaPrincipala.this, Login.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean isUidAvailable() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String uid = sharedPreferences.getString("uid", "");
        return uid != null && !uid.isEmpty();
    }

    private void showSignOutDialog() {
        String uid = mAuth.getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference utilizatoriRef = db.collection("Utilizatori");

        utilizatoriRef.whereEqualTo("uid", uid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        String prenume = document.getString("prenume");

                        AlertDialog.Builder builder = new AlertDialog.Builder(PaginaPrincipala.this);
                        builder.setTitle("Deconectare")
                                .setMessage("Ești sigur că vrei să ieși din cont, " + prenume + "?")
                                .setPositiveButton("Deconectează", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        signOut();
                                    }
                                })
                                .setNegativeButton("Anulează", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    } else {
                        // No document found with matching UID
                        Toast.makeText(PaginaPrincipala.this, "Nu s-a găsit documentul cu UID-ul corespunzător.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Error retrieving the document
                    Toast.makeText(PaginaPrincipala.this, "Eroare la obținerea documentului.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }





    private void signOut() {
        mAuth.signOut();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("uid");
        editor.apply();

        Intent intent = new Intent(PaginaPrincipala.this, Login.class);
        startActivity(intent);
        finish();
    }
}
