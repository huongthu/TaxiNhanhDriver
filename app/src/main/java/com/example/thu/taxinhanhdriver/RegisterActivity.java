package com.example.thu.taxinhanhdriver;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thu.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    RegisterActivity.this.finish();
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);

        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = ((TextView) findViewById(R.id.etEmail)).getText().toString();
                String password = ((TextView) findViewById(R.id.etPassword)).getText().toString();
                String password2 = ((TextView) findViewById(R.id.etPassword2)).getText().toString();
                String fullName = ((TextView) findViewById(R.id.etFullname)).getText().toString();
                String phoneNumber = ((TextView) findViewById(R.id.etPhoneNumber)).getText().toString();

                if (!password.equals(password2)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this, getResources().getText(R.string.password_not_match),Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                registerWithFirebase(email, password, fullName, phoneNumber);
            }
        });
    }

    private void registerWithFirebase(String email, String password, final String name, final String phoneNumber) {
        if (Utils.isNullOrEmpty(email, password, name, phoneNumber)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(RegisterActivity.this, getResources().getText(R.string.plz_provide_full_register_info),Toast.LENGTH_LONG).show();
                }
            });

            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                DatabaseReference myRef = database.getReference
                                                        (getResources().getString(R.string.db_child_phone_number) + "/" + user.getUid());
                                                myRef.setValue(phoneNumber);

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(RegisterActivity.this, getResources().getText(R.string.register_successful),Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        }
                                    });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RegisterActivity.this, getResources().getText(R.string.register_failed),Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
    }
}
