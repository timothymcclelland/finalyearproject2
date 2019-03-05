package com.example.uuj.finalyearproject;

//android and java imports
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

//import below commented out as unable to get image storage and download functionality working
//import android.widget.ImageButton;

//import below commented out as unable to get image storage and download functionality working
//import com.google.firebase.storage.StorageReference;

public class add_post extends AppCompatActivity {

        //Class member variables

        //final static variable commented out below commented out as unable to get image storage and download functionality working
        //private static final int Gallery_Pick = 1;
        private EditText addPost;
        private Spinner spinner;
        private Button buttonPost;
        private String date;
        private String time;
        private String currentUserID;

        //variables commented out below commented out as unable to get image storage and download functionality working
        //private String downloadURL, postRandomName;
        //private Uri ImageUri;
        //private ImageButton add_image;

        //Firebase Authentication variable
        private FirebaseAuth mAuth;

        //Firebase Database variable
        private DatabaseReference databaseReference;

        //Firebase Storage variable commented out as unable to get image storage and download functionality working
        //private StorageReference storageReference;

    //followed tutorial when implementing recyclerAdapter, https://www.youtube.com/watch?v=vD6Y_dVWJ5c
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_post);

        //methods below used to get current user ID from the Firebase Authentication system
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        //code below commented as unable to get image storage functionality working
        //add_image = findViewById(R.id.add_image);

        //Spinner used to select category of post
        spinner = (Spinner) findViewById(R.id.category_spinner);
        //spinner uses category array specified in strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_dropdown_item);
        //drop down style used
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //Referencing Java to XML variables in add_post.xml
        addPost = (EditText) findViewById(R.id.postEditText);
        buttonPost = (Button) findViewById(R.id.postButton);

        /*Referencing database variable to Firebase Realtime Database child "Users Posts" which will contain all user's posts*/
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users Posts");

        //storage reference commented as unable to get image storage functionality working
        //storageReference = FirebaseStorage.getInstance().getReference();

        /* code below commented out as unable to get downloadURL from image as image is storing too many layers down in the database
        add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                openGallery();
            }
        });*/

        //onClickListener method called to send data to the Firebase Realtime database
        buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //code below used to create date attribute in referenced child(randomly created when post to database is made)
                //code below taken from https://www.youtube.com/watch?v=LBiii5baeas&list=PLxefhmF0pcPnTQ2oyMffo6QbWtztXu1W_&index=21
                Calendar calendarDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
                date = currentDate.format(calendarDate.getTime());

                //code below used to create date attribute in referenced child(randomly created when post to database is made)
                //code below taken from https://www.youtube.com/watch?v=LBiii5baeas&list=PLxefhmF0pcPnTQ2oyMffo6QbWtztXu1W_&index=21
                Calendar calendarTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
                time = currentTime.format(calendarTime.getTime());

                //formatting EditText variable to String to input in Firebase Database
                String post_content = addPost.getText().toString();

                //formatting spinner item to get selected item and format it to string for input in Firebase Database
                String categorySelected = spinner.getSelectedItem().toString();

                //attempt at image storage method below. Code commented out as does not work. Unable to get downloadURL.
                //used https://firebase.google.com/docs/storage/android/download-files?authuser=0 &
                // https://www.youtube.com/watch?v=LBiii5baeas&list=PLxefhmF0pcPnTQ2oyMffo6QbWtztXu1W_&index=21 in attempting this functionality

                /*postRandomName = date + time;

                final StorageReference filePath = storageReference.child("Post Images").child(ImageUri + postRandomName + ".jpg");
                filePath.putFile(ImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri uri = task.getResult();
                            Toast.makeText(add_post.this, "Profile Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();

                            downloadURL = uri.toString();
                        }
                        else
                        {
                            String message = task.getException().getMessage();
                            Toast.makeText(add_post.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/

                /*Toast message that displays if post_content editText field is
                 left empty when user tries to add a new post*/
                if(TextUtils.isEmpty(post_content))
                {
                    Toast.makeText(add_post.this, "Enter post text", Toast.LENGTH_SHORT).show();
                }
                //code below commented out below commented out as unable to get image storage and download functionality working
                /*else if (ImageUri == null)
                {
                    Toast.makeText(add_post.this, "Please select a post image", Toast.LENGTH_SHORT).show();
                }*/
                else{
                    //used https://www.youtube.com/watch?v=tOn5HsQPhUY as basis of how I should send my data to my Firebase database
                    //Creates reference to auto-generated child location in Firebase database
                    DatabaseReference newPost = databaseReference.push();

                    //Creating children in referenced Firebase database child and set the value that will appear in the database
                    newPost.child("post").setValue(post_content);
                    newPost.child("category").setValue(categorySelected);
                    newPost.child("time").setValue(time);
                    newPost.child("date").setValue(date);
                    //code below commented out as unable to get image storage and downloading of URL working
                    //newPost.child("image").setValue(downloadURL);
                    newPost.child("uid").setValue(currentUserID);

                    //once OnClick method is completed, user will be taken back to the content activity screen
                    startActivity(new Intent(add_post.this, content.class));
                }
            }
        });
    }
    //code below commented as unable to get image downloadURL functionality to work correctly
    /*
    private void openGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            add_image.setImageURI(ImageUri);
        }
    }*/
}