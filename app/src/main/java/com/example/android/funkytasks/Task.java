package com.example.android.funkytasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import io.searchbox.annotations.JestId;

import static java.util.Collections.*;

/**
 * Created by MonicaB on 2018-02-20.
 */

@SuppressWarnings("serial")
public class Task implements Serializable{
    // title, description, status, lowest bid,

    private String title;
    private String description;
    private User requester;
    private String status;
    private String[] statuses={"requested","bidded","assigned","done"};
    private ArrayList<Bid> bids;
    private double smallest;

    @JestId
    private String id;


    Task(String title, String description,User requester){
        // constructor for task object
        this.title = title;
        this.description = description;
        this.requester = requester;
        this.status = statuses[0];
        bids = new ArrayList<Bid>();

    }

//    public boolean equals(Object obj) {
//        if (obj == null) return false;
//        if (obj == this) return true;
//        if (!(obj instanceof Task)) return false;
//        Task o = (Task) obj;
//        return o.id.equals(this.id);
//    }

    public double getLowestBid() throws IllegalAccessException {
        if (bids.size() == 0){
            throw new IllegalAccessException();
        }
        smallest = bids.get(0).getAmount();
        for (int i = 0; i < bids.size(); i++){
             Bid bid = bids.get(i);
             if (smallest > bid.getAmount()){
                 smallest = bid.getAmount();
             }

        }
        return smallest;

    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public ArrayList<Bid> getBids() {
        return bids;
    }
    public void addBid(Bid newBidder){

        bids.add(newBidder);
    }
    public String getTitle(){
        return this.title;
    }
    public void setTitle(String newTitle){
        this.title = newTitle;
    }
    public String getDescription(){
        return this.description;
    }
    public void setDescription(String newDescription){
        this.description = newDescription;
    }
    public void setBidded(){
        this.status = statuses[1];
    }
    public void setAsigned(){
        this.status = statuses[2];
    }
    public void setDone(){
        this.status = statuses[3];
    }
    public String getStatus(){
        return this.status;
    }
    public void setRequester(User newRequester){
        this.requester=newRequester;
    }
    public User getRequester(){
        return this.requester;
    }

}
