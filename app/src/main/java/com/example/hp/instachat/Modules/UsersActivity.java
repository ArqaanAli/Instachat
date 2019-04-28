package com.example.hp.instachat.Modules;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hp.instachat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
     private Toolbar mToolbar;

     private RecyclerView mUserList;

     private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = findViewById(R.id.user_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");//retrieve all data from users object

        mUserList= findViewById(R.id.user_recyclerview);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();



        //add User.class and UserViewHolder in FirebaseAdapter
      FirebaseRecyclerAdapter<Users,UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(
              Users.class,
              R.layout.user_single_layout,
              UserViewHolder.class,
              mUserDatabase
      ) {
          @Override
          protected void populateViewHolder(UserViewHolder viewHolder, Users model, int position) {

              viewHolder.setProfileName(model.getName());//return the name to UserViewHolder
              viewHolder.setStatus(model.getStatus());
              viewHolder.setUserImage(model.getThumb_image(),getApplicationContext());

              final String user_id = getRef(position).getKey();//key

              viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {

                      Intent profileintent = new Intent(UsersActivity.this,ProfileActivity.class);
                      profileintent.putExtra("user_id",user_id);
                      startActivity(profileintent);


                  }
              });
          }
      };
      mUserList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{//viewholder will get the name  from populateVH and it will store in the user_single_layout file
        //we have to create a view which will used by the adapter

         View mView;

        public UserViewHolder(@NonNull View itemView) {

            super(itemView);
            mView = itemView;

        }
        public void setProfileName(String name){//get value from populate
            TextView userName = mView.findViewById(R.id.user_single_name);
            userName.setText(name);
        }
        public void setStatus(String status){//get value from populate
            TextView userName = mView.findViewById(R.id.user_single_status);
            userName.setText(status);
        }

        public void setUserImage(String thumb_image, Context context){

            CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.cwisdefaultavatar).into(userImageView);
        }
    }
}
