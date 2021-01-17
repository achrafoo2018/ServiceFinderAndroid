package com.example.servicefinder.Models;

public class Comment {
    private int id;
    private int provider_id, rating;
    private String comment,commenterName,commentDate,commenterProfilePicture;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProvider_id() {
        return provider_id;
    }

    public void setProvider_id(int provider_id) {
        this.provider_id = provider_id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommenterName() {
        return commenterName;
    }

    public void setCommenterName(String commenterName) {
        this.commenterName = commenterName;
    }

    public String getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(String commentDate) {
        this.commentDate = commentDate;
    }

    public String getCommenterProfilePicture() {
        return commenterProfilePicture;
    }

    public void setCommenterProfilePicture(String commenterProfilePicture) {
        this.commenterProfilePicture = commenterProfilePicture;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
