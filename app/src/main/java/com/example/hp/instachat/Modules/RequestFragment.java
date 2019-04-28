package com.example.hp.instachat.Modules;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.instachat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private RecyclerView mFriendList;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mUserDatabase;

    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mainView;

    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_request, container, false);

        mFriendList = mainView.findViewById(R.id.request_fragment);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friends_req").child(mCurrent_user_id);
        mFriendReqDatabase.keepSynced(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        mUserDatabase.keepSynced(true);

        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mainView;
    }
    public void onStart(){
        super.onStart();

        FirebaseRecyclerAdapter<Users,CallsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, CallsViewHolder>(
                Users.class,
                R.layout.friend_request,
                CallsViewHolder.class,
                mUserDatabase
        ) {
            @Override
            protected void populateViewHolder(final CallsViewHolder viewHolder, Users model, int position) {
                //  viewHolder.setName(model.getName());
                  //viewHolder.setUserThumb(model.getThumb_image(), getContext());

               /* viewHolder.setRequest_type(model.getRequest_Type);
                if (model.getRequest_type() == "received"){

                final String list_user_id = getRef(position).getKey();

                mUserDatabase.child("User").child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        String name = dataSnapshot.child("name").getValue().toString();
                        final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        viewHolder.setName(name);
                        viewHolder.setUserThumb(userThumb, getContext());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }*/
            }
        };
        mFriendList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CallsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public CallsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }
        public void setRequest_type(String request_type){
            TextView userStatusView = mView.findViewById(R.id.user_single_reqtype1);
            userStatusView.setText("Friend Request");
        }
       public void setName(String name){
            TextView userNameView = mView.findViewById(R.id.user_single_name1);
            userNameView.setText(name);
       }
       public void setUserThumb(String thumb_image, Context context){
           CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
           Picasso.with(context).load(thumb_image).placeholder(R.drawable.cwisdefaultavatar).into(userImageView);
       }
       public void setAccept(final String mCurrentUser, final String user_id){
           ImageButton accept = mView.findViewById(R.id.check_btn);
           accept.setEnabled(true);
           accept.setVisibility(View.VISIBLE);
           accept.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                   Map friendMap = new HashMap();
                   friendMap.put("Friends/" + mCurrentUser+ "/" + user_id + "/date",currentDate);
                   friendMap.put("Friends/" + user_id + "/" + mCurrentUser + "/date" ,currentDate);

                   friendMap.put("Friends_req/" + mCurrentUser + "/" + user_id  , null);
                   friendMap.put("Friends_req/" + user_id + "/" + mCurrentUser  , null);

               }
           });
       }
        public void setReject(){
            ImageButton reject = mView.findViewById(R.id.cancel_btn);
        }
    }
}
