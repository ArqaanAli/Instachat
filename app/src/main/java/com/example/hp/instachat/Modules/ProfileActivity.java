package com.example.hp.instachat.Modules;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.instachat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

   private ImageView mProfileImage;
    private TextView mProfileName,mProfileStatus,mFriendCount;
    private Button mProfileSendRequest,mProfileDeclineRequest;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendreqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

    private String mCurrent_State ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(user_id);
        mFriendreqDatabase = FirebaseDatabase.getInstance().getReference().child("Friends_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = findViewById(R.id.profile_image1);
        mProfileName = findViewById(R.id.profile_name);
        mProfileStatus = findViewById(R.id.profile_status);
        mFriendCount = findViewById(R.id.profile_countfriends);
        mProfileSendRequest = findViewById(R.id.sendrequestbtn);
        mProfileDeclineRequest = findViewById(R.id.declinerequestbtn);

        mCurrent_State = "not_friends";

        mProgressDialog = new ProgressDialog(ProfileActivity.this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please Wait while we load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(name);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.cwisdefaultavatar).into(mProfileImage);

                //Friends List/Request Feature
                mFriendreqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received")){

                                mCurrent_State = "req_received";
                                mProfileSendRequest.setText("Accept Friend Request");

                                mProfileDeclineRequest.setVisibility(View.VISIBLE);
                                mProfileDeclineRequest.setEnabled(true);

                            }
                            else if(req_type.equals("sent")){

                                mCurrent_State = "req_sent";
                                mProfileSendRequest.setText("Cancel Friend Request");

                                mProfileDeclineRequest.setVisibility(View.INVISIBLE);
                                mProfileDeclineRequest.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        }
                        else{
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        mCurrent_State = "friends";
                                        mProfileSendRequest.setText("Unfriend this person");

                                        mProfileDeclineRequest.setVisibility(View.INVISIBLE);
                                        mProfileDeclineRequest.setEnabled(false);
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgressDialog.dismiss();

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProfileSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendRequest.setEnabled(false);

                //Not Friends State
                if(mCurrent_State.equals("not_friends")) {

                    DatabaseReference newNotificationref = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friends_req/" + mCurrentUser.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friends_req/" + user_id + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" +newNotificationId,notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                           if(databaseError != null){
                               Toast.makeText(ProfileActivity.this,"There is some error",Toast.LENGTH_SHORT).show();
                           }
                           mProfileSendRequest.setEnabled(true);

                            mCurrent_State = "req_sent";
                            mProfileSendRequest.setText("Cancel Friend Request");

                        }
                            });
                        }


                //Cancel Request state
                if(mCurrent_State.equals("req_sent")){
                    mFriendreqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendreqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendRequest.setEnabled(true);
                                    mCurrent_State = "not_friends";
                                    mProfileSendRequest.setText("Send Friend Request");

                                    mProfileDeclineRequest.setVisibility(View.INVISIBLE);
                                    mProfileDeclineRequest.setEnabled(false);

                                }

                            });
                        }
                    });//This will remove only user_id child
                    //because we want only (other user id)child to delete so that our other sent req should delete.If we delete our id
                    //with other user_id as a child then all the request u have send will be delete.
                }
                //Req Received State
                if(mCurrent_State.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendMap = new HashMap();
                    friendMap.put("Friends/" + mCurrentUser.getUid()+ "/" + user_id + "/date",currentDate);
                    friendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date" ,currentDate);

                    friendMap.put("Friends_req/" + mCurrentUser.getUid() + "/" + user_id  , null);
                    friendMap.put("Friends_req/" + user_id + "/" + mCurrentUser.getUid()  , null);

                    mRootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference){
                           if(databaseError != null){
                               mProfileSendRequest.setEnabled(true);
                               mCurrent_State = "friends";
                               mProfileSendRequest.setText("Unfriend this person");

                               mProfileDeclineRequest.setVisibility(View.INVISIBLE);
                               mProfileDeclineRequest.setEnabled(false);
                               }
                           else{

                               String error = databaseError.getMessage();
                               Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                           }
                        }


                    });
                }
                //Friends State
                if(mCurrent_State.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid()+ "/" + user_id ,null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid()  ,null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference){
                            if(databaseError != null){

                                mCurrent_State = "not_friends";
                                mProfileSendRequest.setText("Send Friend Request");

                                mProfileDeclineRequest.setVisibility(View.INVISIBLE);
                                mProfileDeclineRequest.setEnabled(false);
                            }
                            else{

                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }
                            mProfileSendRequest.setEnabled(true);
                        }


                    });

                }

            }
        });
    }
}
