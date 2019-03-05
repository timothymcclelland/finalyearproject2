package com.example.uuj.finalyearproject;

//followed tutorial on youtube in producing this class, https://www.youtube.com/watch?v=vD6Y_dVWJ5c

//model class used to retrieve the data from the database and set it to the referenced variables for use by the RecyclerAdapter method
public class post {

    //class variables referenced in Firebase Database
    public String post;
    public String date;
    public String time;
    public String category;
    //code below commented out as unable to get image storage and download functionality working
    //public String postImage;

    //default constructor
    public post(){

    }

    //post constructor class
    public post(String post, String date, String time, String category/*String postImage*,
    //code below commented out as unable to get image storage and download functionality working*/) {
        this.post = post;
        this.date = date;
        this.time = time;
        this.category = category;

        //code below commented out as unable to get image storage and download functionality working
        //this.postImage = postImage;
    }

    //getter method for post variable
    public String getPost() {
        return post;
    }

    //setter method for post variable
    public void setPost(String post) {
        this.post = post;
    }

    //getter method for date variable
    public String getDate() {
        return date;
    }

    //setter method for date variable
    public void setDate(String date) {
        this.date = date;
    }

    //getter method for time variable
    public String getTime() {
        return time;
    }

    //setter method for time variable
    public void setTime(String time) {
        this.time = time;
    }

    //getter method for category variable
    public String getCategory() {
        return category;
    }

    //setter method for category variable
    public void setCategory(String category) {
        this.category = category;
    }

    /* methods below are commented out as unable to get image storage and download functionality working

    //getter method for postImage variable
    public String getPostImage() {
        return postImage;
    }

    //setter method for postImage variable
    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }*/
}