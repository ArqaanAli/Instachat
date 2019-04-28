package com.example.hp.instachat.Modules;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.hp.instachat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionPagerAdapter;

    private DatabaseReference mUserRef;

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("InstaChat");

        mViewPager = (ViewPager) findViewById(R.id.main_pager);
        mSectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        if(mAuth.getCurrentUser() != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("User").child(mAuth.getCurrentUser().getUid());
        }
           mViewPager.setAdapter(mSectionPagerAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            sendToStart();
        }
        else {
           mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            sendToStart();
        }
        else {
            mUserRef.child("online").setValue("true");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }

    public void onStop(){
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {
           mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }
    private void sendToStart(){
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu) ;

    return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout){
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        if(item.getItemId() == R.id.main_settings){
            Intent setting_intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(setting_intent);
        }
        if(item.getItemId() == R.id.main_users){
            Intent user_intent = new Intent(MainActivity.this,UsersActivity.class);
            startActivity(user_intent);
        }
        return true;
    }
}
