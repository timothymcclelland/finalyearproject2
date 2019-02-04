package com.example.uuj.finalyearproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class content extends AppCompatActivity {

    //Class member variables
    private LinearLayoutManager mLayoutManager;
    private SharedPreferences mSharedPref;
    private RecyclerView viewRecycler;
    private String currentUser;

    private List<post> postList;

    //Firebase Authentication variable
    private FirebaseAuth mAuth;

    //Firebase Database variable
    private DatabaseReference databaseReference, categoryRef;
    private Button searchButton;
    private EditText searchInputText;
    public static final String CHANNEL_ID = "notification_CHANNEL_ID";
    public static final String CHANNEL_NAME = "notification_CHANNEL_NAME";
    public static final String CHANNEL_DESCRIPTION = "notification_CHANNEL_DESCRIPTION";
    private TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESCRIPTION);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        textView = findViewById(R.id.token);

        FirebaseMessaging.getInstance().subscribeToTopic("BibleVerses");

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(task.isSuccessful()){
                            String token = task.getResult().getToken();
                            saveToken(token);
                        }else{
                            textView.setText(task.getException().getMessage());
                        }
                    }
                });
        /*method below used to get instance of user that has just logged in
        from the Firebase Authentication system*/
        mAuth = FirebaseAuth.getInstance();

        //Shared Preferences used to store the users selected sort by preference
        mSharedPref = getSharedPreferences("SortSettings", MODE_PRIVATE);
        String mSorting = mSharedPref.getString("Sort", "Ascending");

        searchButton = findViewById(R.id.searchButton);
        searchInputText = findViewById(R.id.searchEditText);

        /*sort the posts in the content screen in ascending order by reversing the layout and setting the stack of the contents to
        start from the end*/
        if(mSorting.equals("Ascending")){
            mLayoutManager = new LinearLayoutManager(this);
            mLayoutManager.setReverseLayout(true);
            mLayoutManager.setStackFromEnd(true);
            /*sort the posts in the content screen in descending order by not reversing the layout and not setting the stack of the contents to
        start from the end*/
        }else if(mSorting.equals("Descending")){
            mLayoutManager = new LinearLayoutManager(this);
            mLayoutManager.setReverseLayout(false);
            mLayoutManager.setStackFromEnd(false);
        }

        /*assigning java RecyclerView instance to xml item and setting to fixed size so
        that width or height does not change based on the content in it
         */
        viewRecycler = findViewById(R.id.recyclerView);
        viewRecycler.setHasFixedSize(true);
        viewRecycler.setLayoutManager(mLayoutManager);

        //setting the database node in Firebase to Users Posts
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users Posts");

        //Referencing Java to XML resources
        //Reference toolbar as action bar and hiding title in toolbar
        Toolbar mytoolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mytoolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Floating action button reference
        /*Once user has clicked the postButton and input their data in the add_post screen, this data will be displayed in the content screen by
        calling DisplayPosts method*/
        //followed tutorial when implementing recyclerAdapter, https://www.youtube.com/watch?v=vD6Y_dVWJ5c
        FloatingActionButton postButton = (FloatingActionButton)findViewById(R.id.float_post);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(content.this, add_post.class));
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchBoxInput = searchInputText.getText().toString();

                DisplayPosts(searchBoxInput);
            }
        });
    }

    private void saveToken(String token) {
        String email = mAuth.getCurrentUser().getEmail();
        User user = new User(email, token);

        DatabaseReference userTokenRef = FirebaseDatabase.getInstance().getReference("User Token");

        userTokenRef.child(mAuth.getCurrentUser().getUid())
                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(content.this, "Token Saved", Toast.LENGTH_LONG).show();
                }
            }
        });
    }



    /* DisplayPosts method calls recyclerview adapter to retrieve data from Firebase database and input into the cardview defined
    within post_view.xml*/
    private void DisplayPosts(String searchBoxInput) {
        Query categoryQuery = databaseReference.orderByChild("category").startAt(searchBoxInput).endAt(searchBoxInput);
        //RecyclerOptions set the options that the RecyclerAdapter will use to retrieve the data from the database
        FirebaseRecyclerOptions<post> options = new FirebaseRecyclerOptions.Builder<post>()
                .setQuery(categoryQuery, post.class)
                .build();

        /*RecyclerAdapter uses the post class and the getter and setter methods defined within to set the viewHolder data to the
        data retrieved from the database*/
        /* RecyclerAdapter is used to bind the data retrieved from the database for use by the PostViewHolder class to display it in the defined view*/
        FirebaseRecyclerAdapter<post, PostViewHolder> adapter = new FirebaseRecyclerAdapter<post, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostViewHolder holder, final int position, @NonNull post model)
            {
                final String PostKey = getRef(position).getKey();

                holder.post_Text.setText(model.getPost());
                holder.category_Text.setText(model.getCategory());
                holder.date_Text.setText(model.getDate());
                holder.time_Text.setText(model.getTime());

                /*
                onClickListener - when user clicks on post_Text they are sent to edit_delete_post screen.
                PostKey used to retrieve data of specific post the user has selected to edit/delete.
                 */
                holder.post_Text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent postIntent = new Intent(content.this, edit_delete_post.class);
                        postIntent.putExtra("PostKey", PostKey);
                        startActivity(postIntent);
                    }
                });

                /*
                onClickListener - when user clicks on commentButton they are sent to commentScreen screen.
                PostKey used to retrieve data of specific post the user has selected to comment on.
                 */
                holder.commentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(content.this, commentScreen.class);
                        commentIntent.putExtra("PostKey", PostKey);
                        startActivity(commentIntent);
                    }
                });

                /*
                onClickListener - when user clicks on reportButton they are sent to reportScreen screen.
                PostKey used to retrieve data of specific post the user has selected to report on.
                 */
                holder.reportButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent reportIntent = new Intent(content.this, reportScreen.class);
                        reportIntent.putExtra("PostKey", PostKey);
                        startActivity(reportIntent);
                    }
                });
            }

            @NonNull
            @Override
            //method used to take data from database and display it in the post_view.xml cardview holder
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_view, viewGroup, false);
                PostViewHolder viewHolder = new PostViewHolder(view);
                return viewHolder;
            }
        };
        //sets the recyclerview to the recycleradapter defined above
        viewRecycler.setAdapter(adapter);
        //initiates the recycleradapter to pull the data from the database
        adapter.startListening();
    }

    //ViewHolder used to reference each post_view xml resource and allow repetition of these resources as required by the RecyclerAdapter
    public static class PostViewHolder extends RecyclerView.ViewHolder {


        View mView;

        TextView post_Text, category_Text;
        TextView date_Text, time_Text;
        ImageButton commentButton, reportButton;

        public PostViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            post_Text = itemView.findViewById(R.id.post_text);
            category_Text = itemView.findViewById(R.id.post_category);
            date_Text = itemView.findViewById(R.id.post_date);
            time_Text = itemView.findViewById(R.id.post_time);
            commentButton = mView.findViewById(R.id.comment_button);
            reportButton = mView.findViewById(R.id.report_button);
        }
    }

    //create menu items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.toolbar_menu,menu);
        return true;
    }

    //Toolbar menu items corresponding method calls
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort:
                sort();
                return true;
            case R.id.action_sign_out:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Sign out method taken directly from FirebaseAuth methods
    public void signOut(){
        mAuth.signOut();
        startActivity(new Intent(this, log_in.class));
        finish();
    }

    //sort content based on date in ascending or descending order
    //followed youtube tutorial https://www.youtube.com/watch?v=fmkjH7tIyao for sorting content in recyclerview
    public void sort(){
        String[] sortOptions = {"Ascending", "Descending"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort by Order")
                .setItems(sortOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 0 means Ascending and 1 means descending
                        if (which==0){
                            //sort ascending
                            //Edit Shared Preferences
                            SharedPreferences.Editor editor = mSharedPref.edit();
                            editor.putString("Sort", "Ascending"); //where sort is key & ascending is value
                            editor.apply(); //apply/save value in Shared Preferences
                            recreate();
                        }
                        else if(which==1){
                            //sort descending
                            //Edit Shared Preferences
                            SharedPreferences.Editor editor = mSharedPref.edit();
                            editor.putString("Sort", "Descending"); //where sort is key & descending is value
                            editor.apply(); //apply/save value in Shared Preferences
                            recreate();
                        }
                    }
                });
        builder.show();
    }
}