/*
followed the following tutorials in the creation of this java activity:
https://www.youtube.com/watch?v=uJJY8LkXx0I&list=PLxefhmF0pcPnTQ2oyMffo6QbWtztXu1W_&index=26
https://www.youtube.com/watch?v=x0XIYqf4zuw&list=PLxefhmF0pcPnTQ2oyMffo6QbWtztXu1W_&index=27
https://www.youtube.com/watch?v=3xvtMYT4mmw&index=28&list=PLxefhmF0pcPnTQ2oyMffo6QbWtztXu1W_
https://www.youtube.com/watch?v=Ris408wl9E0&list=PLxefhmF0pcPnTQ2oyMffo6QbWtztXu1W_&index=29
https://www.youtube.com/watch?v=cEr-xRsSlP8&index=30&list=PLxefhmF0pcPnTQ2oyMffo6QbWtztXu1W_
 */

package com.example.uuj.finalyearproject;

//android and java imports
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

//firebase imports

//import below commented out as unable to get image storage and download functionality working
//import com.squareup.picasso.Picasso;

public class edit_delete_post extends AppCompatActivity {

    //Class member variables
    private TextView editPostContent, editPostCategory;
    private Button editButton, deleteButton;
    private String PostKey, currentUserID, userID;
    //code below commented out as unable to get image storage and download functionality working
    //private ImageView editPostImage;

    //Firebase Database variable
    private DatabaseReference databaseReference;

    //Firebase Authentication variable
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_delete_post);

        //methods below used to get current user ID from the Firebase Authentication system
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        //used to get position of specific post that a user has selected to edit/delete
        PostKey = getIntent().getExtras().get("PostKey").toString();

        /*Referencing database variable to Firebase Realtime Database child "Users Posts" which will contain all the information needed for the
        specific post that the user has selected to either edit or delete*/
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users Posts").child(PostKey);

        //Referencing Java to XML variables in activity_edit_delete_post.xml
        //code below commented out as unable to get image storage and download functionality working
        //editPostImage = findViewById(R.id.edit_delete_post_image);
        editPostContent = findViewById(R.id.edit_delete_post_text);
        editPostCategory = findViewById(R.id.edit_delete_post_category);
        editButton = findViewById(R.id.editPostButton);
        deleteButton = findViewById(R.id.deletePostButton);

        /*Edit and Delete buttons set to invisible initially so that
         only those posted the post originally can edit or delete it*/
        editButton.setVisibility(View.INVISIBLE);
        deleteButton.setVisibility(View.INVISIBLE);

        //method to retrieve data of post that user that has selected to edit or delete
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){

                if(dataSnapshot.exists())
                {
                    final String post = dataSnapshot.child("post").getValue().toString();
                    final String category = dataSnapshot.child("category").getValue().toString();
                    userID = dataSnapshot.child("user id").getValue().toString();
                    //code below commented out as unable to get image storage and download functionality working
                    //image = dataSnapshot.child("image").getValue().toString();

                    //code below commented out as unable to get image storage and download functionality working
                    //Picasso.get(edit_delete_post.this).load(image).into(editPostImage);
                    editPostContent.setText(post);
                    editPostCategory.setText(category);

                    /* method to check if the currentUserID equals the UserID of the post.
                    Then sets buttons to visible to allow user to edit or delete post*/
                    if(currentUserID.equals(userID))
                    {
                        editButton.setVisibility(View.VISIBLE);
                        deleteButton.setVisibility(View.VISIBLE);
                    }

                    editButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editPost(post);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete post from database method
                databaseReference.removeValue();
                //once OnClick method is completed, user will be taken back to the content activity screen
                Intent deletePostIntent = new Intent(edit_delete_post.this, content.class);
                deletePostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(deletePostIntent);
                Toast.makeText(edit_delete_post.this, "Post deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editPost(String post)
    {
        //Alert dialog box appears when user selects edit button to allow user to edit their post
        AlertDialog.Builder builder = new AlertDialog.Builder(edit_delete_post.this);
        //Referencing Java to XML variables in edit_dialog.xml
        View mView = getLayoutInflater().inflate(R.layout.edit_dialog, null);
        final EditText editPost = mView.findViewById(R.id.post_edit_text);
        editPost.setText(post);
        final Spinner editSpinner = mView.findViewById(R.id.post_edit_category);
        //spinner uses category array specified in strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_dropdown_item);
        //drop down style used
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editSpinner.setAdapter(adapter);
        // Get the layout inflater

        //code below used to create date attribute in referenced child(randomly created when post to database is made)
        //code below taken from https://www.youtube.com/watch?v=LBiii5baeas&list=PLxefhmF0pcPnTQ2oyMffo6QbWtztXu1W_&index=21
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        final String date = currentDate.format(calendarDate.getTime());

        //code below used to create date attribute in referenced child(randomly created when post to database is made)
        //code below taken from https://www.youtube.com/watch?v=LBiii5baeas&list=PLxefhmF0pcPnTQ2oyMffo6QbWtztXu1W_&index=21
        Calendar calendarTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        final String time = currentTime.format(calendarTime.getTime());

        //onClickListener method called when user selects "Update" to send data to the Firebase Realtime database
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseReference.child("category").setValue(editSpinner.getSelectedItem().toString());
                databaseReference.child("post").setValue(editPost.getText().toString());
                databaseReference.child("time").setValue(time);
                databaseReference.child("date").setValue(date);
                Toast.makeText(edit_delete_post.this, "Post Updated", Toast.LENGTH_SHORT).show();
            }
        });
        //onClickListener method called when user selects "Cancel" to close the dialog box
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        //opens dialog box and sets the view to the view created called mView
        builder.setView(mView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
