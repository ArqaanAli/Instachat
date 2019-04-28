package com.example.hp.instachat.Modules;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.instachat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    private CircleImageView mImage;
    private TextView mName;
    private TextView mStatus;

    private Button changeStatusbtn;
    private Button changeImagebtn;

    private static final int Gallery_Pick = 1;

    private ProgressDialog mProgressDialog;

    //Storage reference
    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mImage =(CircleImageView) findViewById(R.id.profile_image);
        mName  = findViewById(R.id.settings_profilename);
        mStatus = findViewById(R.id.settings_status);
        changeStatusbtn = findViewById(R.id.settings_changeStatus);
        changeImagebtn = findViewById(R.id.settings_changeImage);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        String current_uid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(current_uid);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {//used to retrive the data from databse
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {//add or retreive data

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if (!image.equals("default")) {//to remove invisibilty

                   // Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.cwisdefaultavatar).into(mImage);//placeholder is used to hold the default image rather than previously logged in image
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)//offline
                            .placeholder(R.drawable.cwisdefaultavatar).into(mImage, new Callback() {//callback is used to load image
                                                                                                     //online if due to some problem
                                                                                                     //it does not load offline
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {//this will download image online
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.cwisdefaultavatar).into(mImage);

                        }
                    });
                  //network policy will store image offline
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        changeStatusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status_value = mStatus.getText().toString();//getting value from textview
                Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra("status_value",status_value);//transfer status to StatusActivity
                startActivity(statusIntent);

            }
        });

        changeImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                // start picker to get image for cropping and then use the image in cropping activity
               /* CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);*/


                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),Gallery_Pick);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK){
            //get Uri from intent
            Uri imageUri = data.getData();

            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image....");
                mProgressDialog.setMessage("Please Wait for few minutes");
                mProgressDialog.setCanceledOnTouchOutside(true);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                //Filepath
                File thumb_filepath = new File(resultUri.getPath());

                String current_user = mCurrentUser.getUid();

                //convert to Bitmap
                final Bitmap thumb_bitmap = new Compressor(SettingsActivity.this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filepath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference filepath = mImageStorage.child("profile_images").child(current_user+".jpg");
                final StorageReference thumbfilepath = mImageStorage.child("profile_images").child("thumbs").child(current_user+".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                      if(task.isSuccessful()) {
                          final String download_url = task.getResult().toString();
                          UploadTask uploadTask = thumbfilepath.putBytes(thumb_byte);
                          uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {//for thumbnail
                              @Override
                              public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                  String downloadThumbUri = thumb_task.getResult().toString();
                                  if(thumb_task.isSuccessful()){
                                      Map updateHash = new HashMap<>();//set value of thumb and image together
                                      updateHash.put("image",download_url);//not updating the value of name and status
                                      updateHash.put("thumb_image",downloadThumbUri);
                                      mUserDatabase.updateChildren(updateHash).addOnCompleteListener(new OnCompleteListener<Void>() {
                                          @Override
                                          public void onComplete(@NonNull Task<Void> task) {
                                              if(task.isSuccessful()){
                                                  mProgressDialog.dismiss();
                                                  Toast.makeText(SettingsActivity.this,"Successfully Uploaded",Toast.LENGTH_LONG).show();
                                              }
                                          }
                                      });
                                  }
                                  else{
                                      Toast.makeText(SettingsActivity.this,"Error in uploading thumbnail",Toast.LENGTH_LONG).show();
                                      mProgressDialog.dismiss();

                                  }
                              }
                          });

                      }
                      else{
                          Toast.makeText(SettingsActivity.this,"Error in uploading",Toast.LENGTH_LONG).show();
                          mProgressDialog.dismiss();
                      }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }


}
