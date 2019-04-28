package com.example.hp.instachat.Modules;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hp.instachat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {


       public static final int MSG_TYPE_LEFT = 0;
       public static final int MSG_TYPE_RIGHT = 1;
       private List<Messages> mMessageList;
       private FirebaseAuth mAuth;
       private DatabaseReference mUserDatabase;
       private FirebaseUser fuser;

       public MessageAdapter(List<Messages> mMessageList){
           this.mMessageList = mMessageList;
       }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

           View v;
        if (i == MSG_TYPE_RIGHT) {
             v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.message_single_layout2, viewGroup, false);
        }
        else{
             v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.message_single_layout, viewGroup, false);
        }

        return new MessageViewHolder(v);
    }

    public  class MessageViewHolder extends RecyclerView.ViewHolder{

           public TextView messageText;
           public CircleImageView profileImage;
           public ImageView messageImage;

           public MessageViewHolder(View view){
               super(view);

               messageText = view.findViewById(R.id.message_text_layout);
               profileImage = view.findViewById(R.id.message_profile_layout);
              // messageImage = (ImageView)view.findViewById(R.id.message_image_layout);

           }

    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if(mMessageList.get(position).getFrom().equals(fuser.getUid())){
           return MSG_TYPE_RIGHT;
        }
        else{
            return MSG_TYPE_LEFT;
        }
    }

    public void onBindViewHolder(final MessageViewHolder viewHolder , int i){

         // if(mAuth.getCurrentUser().getUid().equals(null)){
           //   String current_user_id = mAuth.getCurrentUser().getUid();

           Messages c = mMessageList.get(i);

           String from_user = c.getFrom();
           String messageType = c.getType();

           mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(from_user);

           mUserDatabase.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   String name = dataSnapshot.child("name").getValue().toString();
                   String image = dataSnapshot.child("thumb_image").getValue().toString();

                  /* Picasso.with(viewHolder.profileImage.getContext()).load(image)
                           .placeholder(R.drawable.cwisdefaultavatar)
                           .into(viewHolder.profileImage);*/
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });

           if(messageType.equals("text")){
               viewHolder.messageText.setText(c.getMessage());
           //    viewHolder.messageImage.setVisibility(View.INVISIBLE);
           }
           else{
               viewHolder.messageText.setVisibility(View.INVISIBLE);
               Picasso.with(viewHolder.profileImage.getContext()).load(c.getMessage())
               .placeholder(R.drawable.cwisdefaultavatar).into(viewHolder.messageImage);


              }

       /*    if(from_user.equals(current_user_id)) {
               viewHolder.messageText.setBackgroundColor(Color.WHITE);
               viewHolder.messageText.setTextColor(Color.BLACK);
           }
           else{

               viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
               viewHolder.messageText.setTextColor(Color.WHITE);
           }*/
    }
    public int getItemCount(){
           return mMessageList.size();

    }
}
