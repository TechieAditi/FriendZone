package com.elab.friendzone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settingsactivity extends AppCompatActivity {
    private Button update;
    private EditText username,status;
    private CircleImageView userimage;
    private String currentuserid;
    private FirebaseAuth mauth;
    private DatabaseReference rootref;
    private static final int gallerypic=1;
    private StorageReference userprofileimages;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settingsactivity);
        initializefiels();
        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();
        rootref=FirebaseDatabase.getInstance().getReference();
        userprofileimages= FirebaseStorage.getInstance().getReference().child("Profile images");
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatesettings();
            }
        });
        Retrivieuserinfo();
        userimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                Intent galleryintent=new Intent();
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                galleryintent.setType("image/*");
                startActivityForResult(galleryintent,gallerypic);
            }
        });
    }



    private void initializefiels() {
        update=(Button)findViewById(R.id.update);
        username=(EditText)findViewById(R.id.name);
        status=(EditText)findViewById(R.id.status);
        userimage=(CircleImageView)findViewById(R.id.profile_image);
        loadingbar= new ProgressDialog(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((requestCode==gallerypic)&&(resultCode==RESULT_OK)&& data!=null)
        {
            Uri ImageUri= data.getData();
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);


        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK)
            {
                Uri resulturi=result.getUri();
                loadingbar.setTitle("Uploading Image");
                loadingbar.setMessage("Please wait while image is  being uploaded");
                loadingbar.show();
                loadingbar.setCanceledOnTouchOutside(false);
                StorageReference filepath=userprofileimages.child(currentuserid+".jpg");
                filepath.putFile(resulturi).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(Settingsactivity.this,"Profile image uploaded successfully",Toast.LENGTH_LONG).show();
                            final String downloadurl=task.getResult().getDownloadUrl().toString();
                            rootref.child("Users").child(currentuserid).child("image")
                                    .setValue(downloadurl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(Settingsactivity.this,"Image seaved in database successfully",Toast.LENGTH_LONG).show();
                                                loadingbar.dismiss();

                                            }
                                            else
                                            {
                                                String msg=task.getException().toString();
                                                Toast.makeText(Settingsactivity.this,msg,Toast.LENGTH_LONG).show();
                                                loadingbar.dismiss();
                                            }

                                        }
                                    });
                        }
                        else
                        {
                            String msg= task.getException().toString();
                            Toast.makeText(Settingsactivity.this,msg,Toast.LENGTH_LONG).show();
                            loadingbar.dismiss();

                        }

                    }
                });
            }


        }

    }

    private void updatesettings() {

        String setusername=username.getText().toString();
        String setstatus=status.getText().toString();
        if(TextUtils.isEmpty(setusername))
        {
            Toast.makeText(Settingsactivity.this,"please Writa a user name",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(setstatus))
        {
            Toast.makeText(Settingsactivity.this,"please Writa a status",Toast.LENGTH_LONG).show();
        }
        else
        {
            HashMap<String,String> profilemap=new HashMap<>();
            profilemap.put("uid",currentuserid);
            profilemap.put("name",setusername);
            profilemap.put("status",setstatus);
            rootref.child("Users").child(currentuserid).setValue(profilemap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Settingsactivity.this, "Profile updated successfully", Toast.LENGTH_LONG).show();
                        sendUsertoMainactivity();
                    } else {
                        String msg = task.getException().toString();
                        Toast.makeText(Settingsactivity.this, "Error" + msg, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }


    }
    private void sendUsertoMainactivity() {

        Intent mainintent=new Intent(Settingsactivity.this,MainActivity.class);

        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();


    }
    private void Retrivieuserinfo() {
        rootref.child("Users").child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))&&(dataSnapshot.hasChild("image")))
                {
                    String retrieveusername=dataSnapshot.child("name").getValue().toString();
                    String retrieveuserstatus=dataSnapshot.child("status").getValue().toString();
                    String profileimage=dataSnapshot.child("image").getValue().toString();
                    username.setText(retrieveusername);
                    status.setText(retrieveuserstatus);
                    Picasso.get().load(profileimage).into(userimage);

                }
                else if(((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))))
                {
                    String retrieveusername=dataSnapshot.child("name").getValue().toString();
                    String retrieveuserstatus=dataSnapshot.child("status").getValue().toString();
                    username.setText(retrieveusername);
                    status.setText(retrieveuserstatus);

                }
                else
                {
                    username.setVisibility(View.VISIBLE);
                    Toast.makeText(Settingsactivity.this,"Please set and update your profile info",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
