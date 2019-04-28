package com.example.hp.instachat.Modules;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;


import com.example.hp.instachat.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String ChatUser;
    private android.support.v7.widget.Toolbar ChatToolbar;

    private DatabaseReference mRootRef;

    private FirebaseAuth mAuth;

    private String mCurrentUserId;


    private TextView mTitleName;
    private TextView mLastSeen;
    private CircleImageView mProfileImage;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter messageAdapter;

    private static  final  int TOTAL_NO_ITEM = 10;
    private int mCurrentPage = 1;

    private static final int GALLERY_PICK = 1;

    private StorageReference mImageStorage;

    //New Solution
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ChatToolbar = findViewById(R.id.chat_appbar);
        setSupportActionBar(ChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        ChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");

        getSupportActionBar().setTitle(userName);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(action_bar_view);

        //Custom Action bar item

        mTitleName = findViewById(R.id.textView_display_name);
        mProfileImage = findViewById(R.id.custom_bar_image);
        mLastSeen = findViewById(R.id.textView_lastSeen);

        mChatAddBtn = findViewById(R.id.chat_add_btn);
        mChatSendBtn = findViewById(R.id.chat_send_btn);
        mChatMessageView = findViewById(R.id.chat_message_view);

        messageAdapter = new MessageAdapter(messagesList);
        
        mMessagesList = findViewById(R.id.messagesList);
        mRefreshLayout = findViewById(R.id.swipe_refresh_message);

        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(messageAdapter);

        //firebase storage
        mImageStorage = FirebaseStorage.getInstance().getReference();
        
        loadMessages();

        mTitleName.setText(userName);

        mRootRef.child("User").child(ChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                if(online.equals("true")){
                    mLastSeen.setText("Online");
                }
                else{//Last seen property
                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long lastTime = Long.parseLong(online);

                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    mLastSeen.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Chat Child
        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(ChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp",ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"  + mCurrentUserId + "/" + ChatUser , chatAddMap);
                    chatUserMap.put("Chat/" + ChatUser + "/" + mCurrentUserId , chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError != null){
                                Log.d("CHAT LOG",databaseError.getMessage().toString());
                            }
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();
            }
        });

        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;

                itemPos = 0;

                loadMoreMessages();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode ==RESULT_OK){

            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + ChatUser;
            final String chat_user_ref = "messages/" + ChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(ChatUser).push();

            final String push_id = user_message_push.getKey();

            final StorageReference filePath = mImageStorage.child("message_images").child(push_id + ".jpg");

            filePath.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        Map messageMap = new HashMap();
                        messageMap.put("message",downloadUri);
                        messageMap.put("seen",false);
                        messageMap.put("type","image");
                        messageMap.put("time",ServerValue.TIMESTAMP);
                        messageMap.put("from",mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id,messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id,messageMap);

                        mChatMessageView.setText("");

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                if(databaseError != null){
                                    Log.d("CHAT LOG",databaseError.getMessage().toString());
                                }
                            }
                        });
                    } else {
                        Toast.makeText(ChatActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });


          /*  filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        //String download_url = task.getResult().getDownloadUrl.toString();

                        Map messageMap = new HashMap();
                        messageMap.put("message",download_url);
                        messageMap.put("seen",false);
                        messageMap.put("type","image");
                        messageMap.put("time",ServerValue.TIMESTAMP);
                        messageMap.put("from",mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id,messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id,messageMap);

                        mChatMessageView.setText("");

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                if(databaseError != null){
                                    Log.d("CHAT LOG",databaseError.getMessage().toString());
                                }
                            }
                        });
                    }
                }
            });*/

        }
    }

    private void loadMoreMessages(){
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(ChatUser);

        Query  messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages message = dataSnapshot.getValue(Messages.class);


                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++,message);
                }
                else {
                    mPrevKey = mLastKey;
                }

                if(itemPos == 1){


                    mLastKey = messageKey;
                }



                messageAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() - 1);

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10,0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }
    private void loadMessages() {//for single message
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(ChatUser);

        Query  messageQuery = messageRef.limitToLast(TOTAL_NO_ITEM);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;
                if(itemPos == 1){
                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;//for removing repetition of message
                }
                messagesList.add(message);
                messageAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() - 1);

                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage(){

        String message = mChatMessageView.getText().toString();
        if(!TextUtils.isEmpty(message)){

            String current_user_ref = "messages/" + mCurrentUserId + "/" + ChatUser;
            String chat_user_ref = "messages/" + ChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(ChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time" ,ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUserId);

            Map messageUserMap =new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mChatMessageView.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d("CHAT LOG",databaseError.getMessage().toString());
                    }
                }
            });


        }
    }
   /* public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(ChatUser != null){
            mRootRef.child("User").child(ChatUser).child("online").setValue("true");
        }

    }
    public void onStop(){
        super.onStop();

      //  FirebaseUser currentUser = mAuth.getCurrentUser();

        if(ChatUser != null) {
            mRootRef.child("User").child(ChatUser).child("online").setValue(ServerValue.TIMESTAMP);

        }
    }*/
}
