package com.example.hp.instachat.Modules;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.example.hp.instachat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mToolbar;
    private TextInputLayout mStatus;

    //FireBase
    private  FirebaseUser mCurrentUser;
    private Button mUpdateStatus;

    //Progress
    private ProgressDialog mProgressDialog;

    private DatabaseReference mStatusDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(current_uid);

        //Toolbar
        mToolbar = (android.support.v7.widget.Toolbar )findViewById(R.id.status_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value = getIntent().getStringExtra("status_value");//retrieve value from SettingsActivity


        mProgressDialog = new ProgressDialog(this);


        //layout
        mStatus = findViewById(R.id.status_input);
        mUpdateStatus = findViewById(R.id.status_button);

        mStatus.getEditText().setText(status_value);//set value in the input textfield of status

        mUpdateStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Progress
                mProgressDialog = new ProgressDialog(StatusActivity.this);//if we dismiss it should create again
                mProgressDialog.setTitle("Changing Status");
                mProgressDialog.setMessage("Please Wait.Status updation is in progress");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();


                String status = mStatus.getEditText().getText().toString();

                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            mProgressDialog.dismiss();
                        }
                        else {
                            Toast.makeText(StatusActivity.this,"Error! while saving changes",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }
}
