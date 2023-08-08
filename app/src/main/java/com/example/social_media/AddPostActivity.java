package com.example.social_media;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    //ActionBar actionBar;
    Toolbar toolbar;

    //permission constants
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE=300;
    private static final int IMAGE_PICK_GALLERY_CODE=400;



    //permission array
    String[] cameraPermissions;
    String[] storagePermissions;



    //views
    EditText titleEt, descriptionEt;
    ImageView imageIv;
    Button uploadBtn;

    //user info
    String name, email, uid,dp;
   // String actionBar;
    String editTitle ,editDescription,editImage;

    //image picked wil be samed in this uri
    Uri image_rui=null;

    //progress bar
    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add New Post");

        cameraPermissions = new String[] {android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd= new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();


        Intent intent=getIntent();
        String isUpdateKey=""+intent.getStringExtra("key");
        String editPostId=""+intent.getStringExtra("editPostId");


        if(isUpdateKey.equals("editPost"))
        {
            //Change??
            getActionBar().setTitle("Update Post");
            //actionBar.setTitle("Update post");
            uploadBtn.setText("Update");
            loadPostData(editPostId);

        }
        else {

            //changed??
            getActionBar().setTitle("Add New Post");
            //actionBar.setTitle("Update post");
            uploadBtn.setText("Upload");
        }



        getSupportActionBar().setSubtitle(email);

        //get some info of current user to include in post
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot ds: datasnapshot.getChildren()){
                    name = ""+ds.child("name").getValue();
                    email = ""+ds.child("email").getValue();
                    dp = ""+ds.child("image").getValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //init views
        titleEt= findViewById(R.id.pTitleEt);
        descriptionEt= findViewById(R.id.pDescriptionEt);
        imageIv= findViewById(R.id.pImageIv);
        uploadBtn= findViewById(R.id.pUploadBtn);

        //get image from camera/gallery on click
        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialog
                showImagePickDialog();
            }
        });

        //upload button click listener
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get data (title,description from EditTexts)
                String title= titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();
                if (TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "Enter title...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this, "Enter description...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isUpdateKey.equals("editPost"))
                {
                    beingUpdate(title ,description,editPostId);
                }
                else {
                    uploadData(title,description, "noImage");
                }


                if (image_rui==null){
                    //post without image
                    uploadData(title,description,"noImage");
                }
                else {
                    uploadData(title,description,String.valueOf(image_rui));
                    //post with image
                }
            }

            private void beingUpdate(String title, String description, String editPostId) {

                pd.setMessage("Updating Post...");
                pd.show();


                if(!editImage.equals("noImage"))
                {
                  UpdateWasWithImage(title,description,editPostId);
                }
                else if(imageIv.getDrawable()!=null) {


                    UpdateWithNowImage(title ,description,editPostId);

                }
                else {
                    UpdateWithoutImage(title,description,editPostId);
                    
                }
            }

            private void UpdateWithoutImage(String title, String description, String editPostId) {
                HashMap<String ,Object> hashMap=new HashMap<>();
                hashMap.put("uid",uid);
                hashMap.put("uName",name);
                hashMap.put("uEmail",email);
                hashMap.put("uDp",dp);
                hashMap.put("pTitle",title);
                hashMap.put("pDescr",description);
                hashMap.put("pImage","noImage");

                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                ref.child(editPostId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this, "Update", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                            }
                        });

            }

            private void UpdateWithNowImage(String title, String description, String editPostId) {

                String timeStamp =String.valueOf(System.currentTimeMillis());
                String filePathAndName ="Posts/"+ "post_"+timeStamp;

                Bitmap bitmap= ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
                ByteArrayInputStream baos=new ByteArrayInputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG , 100,baos);

                byte[] data = baos.toByteArray();


                StorageReference ref =FirebaseStorage.getInstance().getReference().child(filePathAndName);

                ref.putBytes(data)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                                Task<Uri> uriTask =taskSnapshot.getStorage().getDownloadUrl();
                                while(!uriTask.isSuccessful());
                                String downloadUri =uriTask.getResult().toString();
                                if(uriTask.isSuccessful())
                                {
                                    HashMap<String ,Object> hashMap=new HashMap<>();
                                    hashMap.put("uid",uid);
                                    hashMap.put("uName",name);
                                    hashMap.put("uEmail",email);
                                    hashMap.put("uDp",dp);
                                    hashMap.put("pTitle",title);
                                    hashMap.put("pDescr",description);
                                    hashMap.put("pImage",downloadUri);

                                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                                    ref.child(editPostId)
                                            .updateChildren(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    pd.dismiss();
                                                    Toast.makeText(AddPostActivity.this, "Update", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    pd.dismiss();
                                                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                                                }
                                            });



                                }

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                            }
                        });

            }

            private void UpdateWasWithImage(String title, String description, String editPostId) {

                StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
                mPictureRef.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                String timeStamp =String.valueOf(System.currentTimeMillis());
                                String filePathAndName ="Posts/"+ "post_"+timeStamp;

                                Bitmap bitmap= ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
                                ByteArrayInputStream baos=new ByteArrayInputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG , 100,baos);

                                byte[] data = baos.toByteArray();


                                StorageReference ref =FirebaseStorage.getInstance().getReference().child(filePathAndName);

                                ref.putBytes(data)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                                                Task<Uri> uriTask =taskSnapshot.getStorage().getDownloadUrl();
                                                while(!uriTask.isSuccessful());
                                                String downloadUri =uriTask.getResult().toString();
                                                if(uriTask.isSuccessful())
                                                {
                                                    HashMap<String ,Object> hashMap=new HashMap<>();
                                                  hashMap.put("uid",uid);
                                                  hashMap.put("uName",name);
                                                  hashMap.put("uEmail",email);
                                                  hashMap.put("uDp",dp);
                                                  hashMap.put("pTitle",title);
                                                  hashMap.put("pDescr",description);
                                                  hashMap.put("pImage",downloadUri);

                                                  DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                                                  ref.child(editPostId)
                                                          .updateChildren(hashMap)
                                                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                              @Override
                                                              public void onSuccess(Void unused) {
                                                                  pd.dismiss();
                                                                  Toast.makeText(AddPostActivity.this, "Update", Toast.LENGTH_SHORT).show();
                                                              }
                                                          })
                                                          .addOnFailureListener(new OnFailureListener() {
                                                              @Override
                                                              public void onFailure(@NonNull Exception e) {
                                                                  pd.dismiss();
                                                                  Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                                                              }
                                                          });



                                                }

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                                            }
                                        });


                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });

            }
        });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Posts");
        Query fquery=reference.orderByChild("pId").equalTo(editPostId);

         fquery.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 for(DataSnapshot ds : dataSnapshot.getChildren())
                 {
                     editTitle=""+ds.child("pTitle").getValue();
                     editDescription=""+ds.child("pDesor").getValue();
                     editImage=""+ds.child("pImage").getValue();

                     titleEt.setText(editTitle);
                     descriptionEt.setText(editDescription);

                     if(!(editImage).equals("noImage"))
                     {
                        try {
                            Picasso.get().load(editImage).into(imageIv);

                        }
                        catch (Exception e)
                        {

                        }

                     }


                 }



             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
    }

    private void uploadData(String title, String description, String noImage) {
        pd.setMessage("Publishing post...");
        pd.show();

        //for post-image name, post-id, post-publish-time
        String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post_" + timeStamp;

        Bitmap bitmap= ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
        ByteArrayInputStream baos=new ByteArrayInputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG , 100,baos);

        byte[] data = baos.toByteArray();



        if (imageIv.getDrawable()!=null){
            //post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image is uploaded to firebase storage, now get it's url
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());

                            String downloadUri = uriTask.getResult().toString();
                            if (uriTask.isSuccessful()){
                                //url is received upload post to firebase database
                                HashMap<Object, String>hashMap = new HashMap<>();
                                //put post info
                                hashMap.put("uid",uid);
                                hashMap.put("uName",name);
                                hashMap.put("uEmail",email);
                                hashMap.put("uDp",dp);
                                hashMap.put("pId",timeStamp);
                                hashMap.put("pTitle",title);
                                hashMap.put("pDescr",description);
                                hashMap.put("pImage",downloadUri);
                                hashMap.put("pTime",timeStamp);

                                //path to stop post data
                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
                                //put data in this ref
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //added in database
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();
                                                titleEt.setText("");
                                                descriptionEt.setText("");
                                                imageIv.setImageURI(null);
                                                image_rui = null;
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed adding post in database
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed uploading image
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });


        }
        else {
            //post without image
            HashMap<Object, String>hashMap = new HashMap<>();
            //put post info
            hashMap.put("uid",uid);
            hashMap.put("uName",name);
            hashMap.put("uEmail",email);
            hashMap.put("uDp",dp);
            hashMap.put("pId",timeStamp);
            hashMap.put("pTitle",title);
            hashMap.put("pDescr",description);
            hashMap.put("pImage","noImage");
            hashMap.put("pTime",timeStamp);

            //path to stop post data
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
            //put data in this ref
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //added in database
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();
                            //reset views
                            titleEt.setText("");
                            descriptionEt.setText("");
                            imageIv.setImageURI(null);
                            image_rui = null;

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed adding post in database
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }

    }

    private void showImagePickDialog() {
        //option (camera, gallery) to show in dialog
        String[] options = {"Camera", "Gallery"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image from");
        //set options to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //item click handle
                if (which ==0){
                    //camera clicked
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                if (which ==1){
                    //gallery clicked
                    if (!checkCameraPermission()){
                        requestStoragePermission();
                    }
                    else{
                        pickFromGallery();
                    }
                }

            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void pickFromGallery() {
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //intent to pick image from camera
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        //check if storage permission is enabled or not
        //return true if enabled
        //return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }
    private void requestStoragePermission(){
        //request runtime storage permission
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        //check if camera permission is enabled or not
        //return true if enabled
        //return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
        //request runtime camera permission
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            //user  is signed in, stay
            email = user.getEmail();
            uid = user.getUid();
        }
        else{
            //user not signed in then, go to main activity
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();    //goto previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //this method is called when user press Allow or Deny from permission
        //here we will handle permission cases (Allowed and denied)

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //both permission are granted
                        pickFromCamera();
                    }
                    else {
                        //camera or gallery or both permissions were denied
                        Toast.makeText(this, "Camera & Storage both permission are necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //storage permission granted
                        pickFromGallery();
                    }
                    else {
                        //camera or gallery or both permissions were denied
                        Toast.makeText(this, "Storage permission necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                }

            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method will be called after picking image from camera or gallery
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //image is picked from gallery, get uri of image
                image_rui = data.getData();

                //set to imageview
                imageIv.setImageURI(image_rui);

            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //image is picked from camera, get uri of image
                imageIv.setImageURI(image_rui);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}