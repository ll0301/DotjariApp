package com.example.wante.dotjariapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdatePasswordActivity extends AppCompatActivity {

    private Button update, cancel;
    private EditText updatePassword;
    private FirebaseUser firebaseUser;

    private FirebaseAuth firebaseAuth;
  private   String userUpdatePassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);


        update = (Button) findViewById(R.id.btn_updatePassword);
        cancel = (Button) findViewById(R.id.btn_updatePassword_cancel);
        updatePassword = (EditText) findViewById(R.id.et_updatePassword);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userUpdatePassword = updatePassword.getText().toString();
                firebaseUser.updatePassword(userUpdatePassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isComplete()) {
                            Toast.makeText(UpdatePasswordActivity.this, "비밀번호 변경완료", Toast.LENGTH_SHORT).show();
                            finish();
                        }else {
                            Toast.makeText(UpdatePasswordActivity.this, "비밀번호 변경실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            onBackPressed();
            finish();
            }
        });

    }
}
