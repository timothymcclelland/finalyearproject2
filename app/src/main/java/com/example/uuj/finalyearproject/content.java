package com.example.uuj.finalyearproject;

//android and google imports
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

//firebase imports
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

//import below commented out as unable to get image storage and download functionality working
//import com.squareup.picasso.Picasso;

public class content extends AppCompatActivity {

    /* followed videos 19-24 in the creation of the Firebase Recyclerview, PostView Holder and recycler adapter
    link to video series, https://www.youtube.com/playlist?list=PLxefhmF0pcPnTQ2oyMffo6QbWtztXu1W_
     */

    //Class member variables
    private LinearLayoutManager mLayoutManager;
    private SharedPreferences mSharedPref;
    private RecyclerView viewRecycler;
    private Button searchButton;
    private EditText searchInputText;
    Boolean LikeChecker = false;
    String currentUserID;

    //Firebase Authentication variable
    private FirebaseAuth mAuth;

    //Firebase Database variable
    private DatabaseReference databaseReference, likesRef;

    //Notification variables to display notification
    public static final String CHANNEL_ID = "notification_CHANNEL_ID";
    public static final String CHANNEL_NAME = "notification_CHANNEL_NAME";
    public static final String CHANNEL_DESCRIPTION = "notification_CHANNEL_DESCRIPTION";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content);

        //used https://www.youtube.com/watch?v=B-G9283Ssd4&list=PLk7v1Z2rk4hjM2NPKqtWQ_ndCuoqUj5Hh in the creation of all notification related methods


        //method to create notification channel
        //minimum SDK version must be 'O' to allow for notification channel to be created
        //channel will only be created for devices on Android Oreo (8) or newer
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESCRIPTION);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        //method to allow for users to sign up to for receiving Bible Verses notifications
        FirebaseMessaging.getInstance().subscribeToTopic("BibleVerses");

        //gets users device registration token and uses this token in saveToken method
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(task.isSuccessful()){
                            String token = task.getResult().getToken();
                            saveToken(token);
                        }else{

                        }
                    }
                });

        /*method below used to get instance of user that has just logged in
        from the Firebase Authentication system*/
        mAuth = FirebaseAuth.getInstance();

        //method below used to get user id of user logged in
        currentUserID = mAuth.getCurrentUser().getUid();

        //Shared Preferences used to store the users selected sort by preference
        mSharedPref = getSharedPreferences("SortSettings", MODE_PRIVATE);
        String mSorting = mSharedPref.getString("Sort", "Ascending");

        //Java referencing to XML items
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

        //setting the database node in Firebase to the likes node
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        //Referencing Java to XML resources
        //Reference toolbar as action bar and hiding title in toolbar
        Toolbar mytoolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mytoolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Floating action button reference
        /*Once user has clicked the postButton and input their data in the add_post screen, this data will be displayed in the content screen by
        calling DisplayPosts method*/
        //followed tutorial when implementing recyclerAdapter, https://www.youtube.com/watch?v=vD6Y_dVWJ5c
        FloatingActionButton postButton = findViewById(R.id.float_post);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(content.this, add_post.class));
            }
        });

        /*followed tutorial when implementing search facility to filter data based on category
        https://www.youtube.com/watch?v=sbOdwk4C_9s&t=1051s*/
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //converts input text to string to search data in DisplayPosts method
                String searchBoxInput = searchInputText.getText().toString();

                DisplayPosts(searchBoxInput);
            }
        });
    }

    //method to store users auto generated device registration token when they first use the app
    //used https://www.youtube.com/watch?v=6Od5PqDktGo&list=PLk7v1Z2rk4hjM2NPKqtWQ_ndCuoqUj5Hh&index=5 as guidance
    private void saveToken(String token) {
        String email = mAuth.getCurrentUser().getEmail();
        User user = new User(email, token);

        //stores token and email address in users node in Firebase RealTime Database
        DatabaseReference userTokenRef = FirebaseDatabase.getInstance().getReference("users");

        //retrieves unique id of currently logged in user
        userTokenRef.child(mAuth.getCurrentUser().getUid())
                .setValue(user);
    }

    /* DisplayPosts method calls recyclerview adapter to retrieve data from Firebase database and input into the cardview defined
    within post_view.xml
    followed */
    private void DisplayPosts(String searchBoxInput) {
        //database query to return results within recyclerview based on the category searched by the user
        Query categoryQuery = databaseReference.orderByChild("category").startAt(searchBoxInput).endAt(searchBoxInput);
        //RecyclerOptions set the options that the RecyclerAdapter will use to retrieve the data from the database based on the query defined above, categoryQuery
        FirebaseRecyclerOptions<post> options = new FirebaseRecyclerOptions.Builder<post>()
                .setQuery(categoryQuery, post.class)
                .build();



        /*RecyclerAdapter uses the post class and the getter and setter methods defined within to set the viewHolder data to the
        data retrieved from the database*/
        /* RecyclerAdapter is used to bind the data retrieved from the database for use by the PostViewHolder class to display it in the defined view*/
        FirebaseRecyclerAdapter<post, PostViewHolder> adapter = new FirebaseRecyclerAdapter<post, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder holder, final int position, @NonNull final post model)
            {
                //gets position of post user is interacting with from firebase database
                final String PostKey = getRef(position).getKey();

                //gets and displays the relevant data within the reyclerview item from model class that links to firebase database content
                holder.post_Text.setText(model.getPost());
                holder.category_Text.setText(model.getCategory());
                holder.date_Text.setText(model.getDate());
                holder.time_Text.setText(model.getTime());
                //code below commented out as unable to get image storage and download functionality working
                //holder.post_image(getApplicationContext(), model.getPostImage());
                holder.setLikeButton(PostKey);

                //used https://developer.android.com/training/sharing/send website in creation of share button functionality
                holder.shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //string that is share gets the firebase database Users Post child 'post' of the selected post item
                        String message = model.post;
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                        sendIntent.setType("text/plain");
                        startActivity(sendIntent);
                    }
                });

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
                onClickListener - when user clicks on likeButton, the method will check if the button has already been liked and if it has, it will be unliked and vice versa.
                PostKey used to retrieve data of specific post the user has selected to like.
                used tutorial from, https://www.youtube.com/watch?v=111GZQ0WsME&index=39&list=PLxefhmF0pcPnTQ2oyMffo6QbWtztXu1W_ in the creation of this onClickListener
                 */
                holder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeChecker = true;

                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(LikeChecker.equals(true))
                                {
                                    if(dataSnapshot.child(PostKey).hasChild(currentUserID))
                                    {
                                        likesRef.child(PostKey).child(currentUserID).removeValue();
                                        LikeChecker = false;
                                    }
                                    else
                                    {
                                        likesRef.child(PostKey).child(currentUserID).setValue(true);
                                        LikeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
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
        TextView numberOfLikes;
        //code below commented out as unable to get image storage and download functionality working
        //ImageView post_image;
        ImageButton commentButton, reportButton, likeButton, shareButton;
        int likeCounter;
        String currentUserId;
        DatabaseReference LikesRef;

        //referencing variables to xml
        public PostViewHolder(View itemView) {
            super(itemView);

            //code below commented out as unable to get image storage and download functionality working
            //String postimage;

            mView = itemView;

            post_Text = itemView.findViewById(R.id.post_text);
            category_Text = itemView.findViewById(R.id.post_category);
            date_Text = itemView.findViewById(R.id.post_date);
            time_Text = itemView.findViewById(R.id.post_time);
            //code below commented out as unable to get image storage and download functionality working
            //postimage = itemView.findViewById(R.id.post_image);
            //code below used to load image into xml item
            //Picasso.get(ctx).load(postimage).into(post_image);
            commentButton = mView.findViewById(R.id.comment_button);
            reportButton = mView.findViewById(R.id.report_button);
            likeButton = mView.findViewById(R.id.like_button);
            numberOfLikes = mView.findViewById(R.id.likeView);
            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            shareButton = mView.findViewById(R.id.share_button);
        }

        /*method to set the count on the number of likes based on number of users clicking on the like button
        and also change the like button to be filled or unfilled based on whether it has been liked or unliked by the current user*/
        //used tutorial from, https://www.youtube.com/watch?v=111GZQ0WsME&index=39&list=PLxefhmF0pcPnTQ2oyMffo6QbWtztXu1W_, in the implementation of this method
        public void setLikeButton(final String PostKey)
        {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(PostKey).hasChild(currentUserId))
                    {
                        //collects the number of likes on the post from the firebase database Post Key
                        likeCounter = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        //sets the like button to the dark heart to indicate the post being liked
                        likeButton.setImageResource(R.mipmap.ic_like);
                        //sets the likeCounter integer value to a string to be displayed to the user
                        numberOfLikes.setText(Integer.toString(likeCounter));
                    }
                    else
                    {
                        //collects the number of likes on the post from the firebase database Post Key
                        likeCounter = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        //sets the like button to the dark heart to indicate the post being liked
                        likeButton.setImageResource(R.mipmap.ic_unlike);
                        //sets the likeCounter integer value to a string to be displayed to the user
                        numberOfLikes.setText(Integer.toString(likeCounter));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
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
            case R.id.action_link:
                bibleLink();
                return true;
            case R.id.action_churchLocator:
                openChurchLocator();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //method to take user to the google maps activity
    private void openChurchLocator() {
        startActivity(new Intent(content.this, GoogleMapsActivity.class));
    }

    //method to take user to YouVersion App once they click on the Bible Icon
    //used https://developer.android.com/training/basics/intents/sending.html as guidance in creating this method
    private void bibleLink() {
        //package name for YouVersion Bible App
        //package name found using Package Name Viewer 2.0 App
        String app_name = "com.sirma.mobile.bible.android";
        Intent openBibleApp = getPackageManager().getLaunchIntentForPackage(app_name);

        //method to check if user has YouVersion Bibe App installed on device and open it if they do
        //if not then it will give them the option to go the the play store to install it
        if(openBibleApp == null) {
            openBibleApp = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+app_name));
        }
        startActivity(openBibleApp);
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