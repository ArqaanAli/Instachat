package com.example.hp.instachat.Modules;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.instachat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendList;

    private DatabaseReference mFriendDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    private String mCurrent_user_id;
    private View mainView;
    private static final int REQUEST_CALL = 1;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendList = (RecyclerView)mainView.findViewById(R.id.friends_fragment);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendDatabase.keepSynced(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        mUserDatabase.keepSynced(true);

        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mainView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CALL){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){}
            else{
                Toast.makeText( getContext(),"Permission Denied" ,Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void onStart(){
        super.onStart();

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> friendsRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(

                Friends.class,
                R.layout.user_single_layout,
                FriendsViewHolder.class,
                mFriendDatabase
        ) {

            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                    viewHolder.setDate(model.getDate());

                    final String list_user_id = getRef(position).getKey();


                    mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                            final String phone = dataSnapshot.child("phone").getValue().toString();
                            //String userOnline = dataSnapshot.child("online").getValue().toString();

                            if(dataSnapshot.hasChild("online")) {
                                String userOnline =  dataSnapshot.child("online").getValue().toString();
                                viewHolder.setUserOnline(userOnline);
                            }
                            viewHolder.setName(userName);
                            viewHolder.setUserImage(userThumb,getContext());

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    CharSequence options[] = new CharSequence[]{"Open Profile","Send Message","Call"};

                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Select Options");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                           //Click Event
                                            if(which == 0){
                                                Intent profileintent = new Intent(getContext(),ProfileActivity.class);
                                                profileintent.putExtra("user_id",list_user_id);
                                                startActivity(profileintent);
                                            }
                                            if(which == 1){
                                                Intent chatintent = new Intent(getContext(),ChatActivity.class);
                                                chatintent.putExtra("user_id",list_user_id);
                                                chatintent.putExtra("user_name",userName);
                                                startActivity(chatintent);
                                            }
                                            if(which == 2){
                                                if(phone.trim().length() > 0){
                                                    if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE)
                                                  != PackageManager.PERMISSION_GRANTED){
                                                        ActivityCompat.requestPermissions(getActivity(),
                                                                new String[]{Manifest.permission.CALL_PHONE},REQUEST_CALL);
                                                    }
                                                    else{
                                                        String dial = "tel:" + phone;
                                                        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
                                                    }
                                                }
                                                else{
                                                    Toast.makeText(getContext(),"Enter phone number",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
            }
        };
       mFriendList.setAdapter(friendsRecyclerAdapter);
    }
   public  static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;
       public FriendsViewHolder( View itemView) {
           super(itemView);

           mView = itemView;
       }
       public void setDate(String date){
           TextView userStatusView = mView.findViewById(R.id.user_single_status);
           userStatusView.setText(date);
       }
       public void setName(String name){
           TextView userNameView = mView.findViewById(R.id.user_single_name);
           userNameView.setText(name);
       }
       public void setUserImage(String thumb_image, Context context){

           CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
           Picasso.with(context).load(thumb_image).placeholder(R.drawable.cwisdefaultavatar).into(userImageView);
       }

       public void setUserOnline(String online_status){

           ImageView userOnlineView = mView.findViewById(R.id.user_single_online);

           if(online_status.equals("true")){
               userOnlineView.setVisibility(View.VISIBLE);
           }
           else{
               userOnlineView.setVisibility(View.INVISIBLE);
           }
       }
   }
}
