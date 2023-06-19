package com.example.socialmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    EditText mEmailEt ,mPasswordEt;
    Button mRegisterBtn;
    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
   // private lateinit var auth: FirebaseAuth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar =getSupportActionBar();
        actionBar.setTitle("Create Accoutnt");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);



        mEmailEt=findViewById(R.id.emailEt);
        mPasswordEt=findViewById(R.id.passwordEt);
         mRegisterBtn=findViewById(R.id.registerBtn);
                 progressDialog =new ProgressDialog(this);

           mAuth= FirebaseAuth.getInstance();

        progressDialog.setMessage("Registerig User");

                 mRegisterBtn.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         String email=mEmailEt.getText().toString().trim();
                         String password=mPasswordEt.getText().toString().trim();
                         if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                         {
                             mEmailEt.setError("Invalid Email");
                             mEmailEt.setFocusable(true);
                         }
                         else if(password.length()<6)
                         {
                             mEmailEt.setError("Password Length At Least 6 Characters");
                             mPasswordEt.setFocusable(true);

                         }
                         else
                         {
                             registerUser(email,password);
                         }

                     }
                 });


    }

    private void registerUser(String email, String password) {
        progressDialog.show();


                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    progressDialog.dismiss();
                                    FirebaseUser user=mAuth.getCurrentUser();
                                    Toast.makeText(RegisterActivity.this,"Register ..\n"+user.getEmail(),Toast.LENGTH_SHORT).show();
                                     startActivity(new Intent(RegisterActivity.this,ProfileActivity.class));
                                     finish();
                                }
                                else
                                {
                                    progressDialog.dismiss();
                                    Toast.makeText(RegisterActivity.this,"Authentocation Failed",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();


                            }
                        });




    }


    public boolean onSupportNavigateUp ()
    {

        onBackPressed();
        return super.onSupportNavigateUp();
    }
}