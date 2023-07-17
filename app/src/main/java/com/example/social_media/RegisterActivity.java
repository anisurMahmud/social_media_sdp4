package com.example.social_media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    //views
    EditText mEmailEt, mPasswordEt;
    Button mRegisterBtn;

    //progressbar to display while registering user
    ProgressDialog progressDialog;

    //declare an instance of firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //actionbar and its title
       // ActionBar actionbar = getActionBar();
        //actionbar.setTitle("Create Account");
        //enable back button
        //actionbar.setDisplayHomeAsUpEnabled(true);
        //actionbar.setDisplayShowHomeEnabled(true);

        //init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mRegisterBtn = findViewById(R.id.register_Btn);

        //in the onCreate() method, initiate the firebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");

        //handle register btn click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input email, password
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                //validate
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set error and focus to email edittext
                    mEmailEt.setError("invalid email");
                    mEmailEt.setFocusable(true);
                }
                else if (password.length()<6){
                    //set error and focus to password edittext
                    mPasswordEt.setError("Password length at least 6 characters");
                    mPasswordEt.setFocusable(true);
                }
                else {
                    registerUser(email, password); //regiser the user
                }

            }
        });
    }
            private void registerUser(String email, String password) {
                //email and password patter is valid, show progress dialog and start registering the user whoosh
                progressDialog.show();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(RegisterActivity.this, "Registered...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, ProfileActivity.class));
                                    finish();
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(RegisterActivity.this, "Authentication failed. ", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });





            }


    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed(); //go previous activity
        return super.onSupportNavigateUp();
    }
}