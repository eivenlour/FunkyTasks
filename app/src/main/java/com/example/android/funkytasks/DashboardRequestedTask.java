/**
 * DashboardRequestedTask
 *
 * Version 1.0.0
 *
 * Created on March 8th by Funky Tasks
 *
 * Copyright information: https://github.com/CMPUT301W18T20/FunkyTasks/wiki/Reuse-Statement
 */
package com.example.android.funkytasks;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static java.security.AccessController.getContext;

/**
 * This class displays the tasks and their bids to a requester. It allows the requester to
 * view the details of the task as well as the details of the bids that were placed on the task.
 * The requester can accept or decline tasks from this page.
 */
public class DashboardRequestedTask extends BaseActivity {

    private TextView titleValue;
    private TextView descriptionValue;
    private TextView statusValue;
    private ListView bidListView;
    private TextView providerName;
    private TextView providerEmail;
    private TextView providerPhone;
    private TextView provideByShow;
    private String id;// id of the detailed task
    private Button updateStatus;
    private Button reassign;
    private ArrayList<Task> taskList;

    private GoogleMap mMap;
    MapView mapView;

    private ArrayList<Bitmap> images;

    private String username; // username of the person who logged in
    private Task task;
    private int index;
    private int EDIT_CODE = 1;
    private User bidder;
    private User requester;
    ListViewAdapter listViewAdapter;
    ArrayList<Bid> bidList = new ArrayList<>();

    private int photoLength;


    /**
     * Overrides the default onCreate class and starts the activity with the proper view
     *
     * @param savedInstanceState a bundle storing the last state the app was in before it
     *                           was last closed
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_requested_task);
        Toolbar myToolbar = findViewById(R.id.DashboardRequestedTasktoolbar);
        setSupportActionBar(myToolbar);

//        loadMap(savedInstanceState); // load and display the map

        // set bids listview
        bidListView = findViewById(R.id.bidlistView);
        descriptionValue = findViewById(R.id.textDescription);
        titleValue = findViewById(R.id.taskName);
        statusValue = findViewById(R.id.taskStatus);
        providerName = findViewById(R.id.taskProviderUsername);
        providerEmail = findViewById(R.id.taskProviderEmail);
        providerPhone = findViewById(R.id.taskProviderPhone);
        provideByShow = findViewById(R.id.TextView);
        updateStatus= findViewById(R.id.setToDone);
        reassign= findViewById(R.id.reasignTask);



        final Intent intent = getIntent();
        username = intent.getExtras().getString("username");
        username = LoginActivity.username;

        index = intent.getExtras().getInt("position");
        id = intent.getExtras().getString("id");

        if (isNetworkAvailable()) {
            getUpdatedTask();
        } else {
            //task = (Task) intent.getSerializableExtra("task");
            LocalRequestedTaskController localController = new LocalRequestedTaskController(getApplicationContext(),username);
            taskList = localController.loadRequestedTask();
            for (Task eachTask: taskList) {
                if ((eachTask.getId() != null) && (eachTask.getId().equals(id))){
                    task = eachTask;
                    break;

                }
                if ((eachTask.getOfflineId() != null) && (eachTask.getOfflineId().equals(id))){
                    task = eachTask;
                    break;
                }
            }
        }


        setTaskDetails(task); // set the contents of the screen to the task details
        photoLength = task.getImages().size();

        if (isNetworkAvailable()){
            setBids(); // grab the associated bids of the task
            setAdapter(); // set adapter ot the list view for bids
        }
        setStatusDone();
        ressignTask();


        bidListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // if we click on a bid, create a pop up window to display bid details

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final AlertDialog.Builder Builder = new AlertDialog.Builder(DashboardRequestedTask.this);
                View View=getLayoutInflater().inflate(R.layout.bids_dialog,null);
                Builder.setView(View);
                final AlertDialog dialog=Builder.create();
                dialog.show();

                TextView bidderTextView = View.findViewById(R.id.bidderTextView);
                TextView contactTextViewPhone = View.findViewById(R.id.contactTextView);
                TextView contactTextViewEmail = View.findViewById(R.id.contactTextViewEmail);
                TextView amountTextView = View.findViewById(R.id.amountTextView);
                TextView ratingView = View.findViewById(R.id.ratingView);
                Button acceptBTN = View.findViewById(R.id.acceptButton);
                Button declineBTN = View.findViewById(R.id.declineButton);


                String bidderName = bidList.get(i).getBidder();
                ElasticSearchController.GetUser getUser = new ElasticSearchController.GetUser();
                getUser.execute(bidderName);

                try{
                    bidder = getUser.get();
                    double rating = bidder.getRating();
                    Log.e("Success",bidder.getUsername());
                    contactTextViewPhone.setText("PHONE: "+bidder.getPhonenumber());
                    contactTextViewEmail.setText("EMAIL: "+bidder.getEmail());
                    ratingView.setText(Double.toString(rating) + "/5 \uD83C\uDF4C");
                }
                catch (Exception e){
                    Log.e("Error","Unable to get the bidder's username");
                }
                // set the provider's contact info
                bidderTextView.setText(bidderName);
                Double bidAmount = bidList.get(i).getAmount();
                amountTextView.setText("$"+bidAmount.toString());




                final int target=i;
                //accept or decline bids

                if(!task.getStatus().equals("bidded")){
                    acceptBTN.setVisibility(View.GONE);
                    declineBTN.setVisibility(View.GONE);
                }
                if(!bidList.get(i).getStatus().equals("")){
                    acceptBTN.setVisibility(View.GONE);
                    declineBTN.setVisibility(View.GONE);
                }

                acceptBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (task.getStatus().equals("assigned") || task.getStatus().equals("done")){
                            Toast.makeText(DashboardRequestedTask.this,
                                    "Already accepted the bid", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        acceptBid(target);
                        dialog.dismiss();
                        statusValue.setText("assigned");
                        setAdapter();
                    }
                });
                declineBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (task.getStatus().equals("assigned") || task.getStatus().equals("done")){
                            Toast.makeText(DashboardRequestedTask.this,
                                    "Cannot decline a bid with current task status",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        declineBids(target);
                        dialog.dismiss();
                        setAdapter();
                    }
                });

            }

        });

        Button locationBtn = this.findViewById(R.id.viewLocation);

        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    LatLng checkLtn = task.getLocation();
                    if (checkLtn == null){
                        Toast.makeText(DashboardRequestedTask.this, "No location to show", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                catch(Exception e){
                    Toast.makeText(DashboardRequestedTask.this, "No location to show", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent mapIntent = new Intent(DashboardRequestedTask.this, DisplayMap.class);
                String taskID = task.getId();
                String activityName = "Request";
                mapIntent.putExtra("task", taskID);
                mapIntent.putExtra("name", activityName);
                startActivity(mapIntent);
            }
        });
    }

    /**
     * Creates the options menu on a given screen using the provided menu
     *
     * @param menu a menu item representing the menu to be displayed by the method
     * @return returns a boolean stating whether or not the menu was successfully instantiated
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dashboard_requested, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Dictates what happens when an item in the options menu is selected.
     *
     * @param item a MenuItem representing one item in the list of items
     * @return returns a boolean value stating whether an item was successfully selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteActionBar: // if clicked on the delete button
                if (!isNetworkAvailable()){
                    Toast.makeText(DashboardRequestedTask.this,
                            "Can't delete task offline", Toast.LENGTH_SHORT).show();
                    return false;
                }
                Intent intent = getIntent();
                onDeleteTask(); // delete the task and any bids along with it
                intent.putExtra("id", id);
                setResult(RESULT_OK, intent);
                finish();

                break;
            case R.id.editRequestedTask: // if clicked on the edit button
                if (task.getStatus().equals("requested")) {
                    Intent editIntent = new Intent(DashboardRequestedTask.this,
                            EditDashboardRequestedTask.class);
                    editIntent.putExtra("username", username);
                    editIntent.putExtra("id", id);
                    editIntent.putExtra("index", index);
                    //editIntent.putExtra("task", task);
                    startActivityForResult(editIntent, EDIT_CODE); // go to activity to edit the task
                }
                else{
                    Toast.makeText(DashboardRequestedTask.this,
                            "Task cannot be edited", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.picdetails:
                if (photoLength > 0){
                    Intent picIntent = new Intent(DashboardRequestedTask.this,ImageDetails.class);
                    picIntent.putExtra("username", username);
                    picIntent.putExtra("id", id);
                    startActivity(picIntent);
                }
                else{
                    Toast.makeText(DashboardRequestedTask.this,
                            "No images to show", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    /**
     * States what happens when an item in the menu list is selected. It starts an activity
     * with the given intent and sets variables to their proper values.
     *
     * @param requestCode an integer stating what type of activity should be performed
     * @param resultCode an integer that is returned by the activity stating
     *                   whether the activity was successfully completed or otherwise
     * @param intent a passed intent that states which activity should be loaded
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == EDIT_CODE && resultCode == RESULT_OK) { // returning from editing the task, update screen contents
            //task = (Task) intent.getSerializableExtra("updatedTask");
            String title = intent.getExtras().getString("title");
            String des = intent.getExtras().getString("des");
            Integer imagesize = intent.getExtras().getInt("size");
            LatLng point = intent.getParcelableExtra("location");
            ArrayList<String> images = (ArrayList<String>)intent.getStringArrayListExtra("pics");

            titleValue.setText(title);
            descriptionValue.setText(des);
            photoLength = imagesize;
            Log.e("phonelen",String.valueOf(photoLength));

            task.setTitle(title);
            task.setDescription(des);
            task.setImagesList(images);
            task.setLocation(point);

            if (isNetworkAvailable()){
                Log.d("Network", "available");
                ElasticSearchController.updateTask updateTask = new ElasticSearchController.updateTask();
                updateTask.execute(task);

                getUpdatedTask();
            }



        }
    }

    private void getUpdatedTask(){
        ElasticSearchController.GetTask getTask = new ElasticSearchController.GetTask();
        getTask.execute(id);
        try{
            task = getTask.get();
            Log.e("Task title",task.getTitle());
        } catch (Exception e) {
            Log.e("ERROR","not working get task");
        }
    }

    /**
     *Reasign a task, clear all the bids placed and change status to requested.
     */

    public void ressignTask(){
        if(task.getStatus().equals("assigned")){
            reassign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(bidList.size()>=2){
                        task.setBidded();
                        ElasticSearchController.updateTask update=new ElasticSearchController.updateTask();
                        update.execute(task);
                        statusValue.setText("bidded");
                    }
                    if(bidList.size()==1){
                        task.setRequested();
                        ElasticSearchController.updateTask update=new ElasticSearchController.updateTask();
                        update.execute(task);
                        statusValue.setText("requested");
                    }
                    for (int index = 0; index < bidList.size(); index++) {
                        ElasticSearchController.updateBid updateBid= new ElasticSearchController.updateBid();
                        Bid toUpdate=bidList.get(index);
                        toUpdate.setDeclined();
                        updateBid.execute(toUpdate);
                    }
                    setAdapter();
                    updateStatus.setVisibility(View.GONE);
                    reassign.setVisibility(View.GONE);

                }
            });
        }
        else{
            reassign.setVisibility(View.GONE);
        }

    }

    /**
     * Set the task status to "done" and rate solver
     */

    public void setStatusDone(){
        if(task.getStatus().equals("assigned")){
            updateStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Beginning of rating testing
                    Intent intent = new Intent(DashboardRequestedTask.this, RateActivity.class);
                    String uname = bidder.getUsername();
                    intent.putExtra("username", uname);
                    startActivity(intent);

                    // Enn of rating testing

                    task.setDone();
                    ElasticSearchController.updateTask done=new ElasticSearchController.updateTask();
                    done.execute(task);
                    statusValue.setText("done");
                    Toast.makeText(DashboardRequestedTask.this,
                            "Task status: done", Toast.LENGTH_SHORT).show();
                    updateStatus.setVisibility(View.GONE);
                    reassign.setVisibility(View.GONE);
                }
            });


        }
        else{
            updateStatus.setVisibility(View.GONE);
        }

    }

    /**
     * Set the proper adapter for the bid list view
     */

    public void setAdapter(){
        BidListViewAdapter adpater = new BidListViewAdapter(DashboardRequestedTask.this,
                android.R.layout.simple_list_item_1, bidList) ;

        bidListView.setAdapter(adpater);
        adpater.notifyDataSetChanged();
    }

    /**
     * Get the bids from the server in order to display them
     */
    public void setBids(){
        // get all bids associated with the task
        ElasticSearchController.GetBidsByTaskID getBids = new ElasticSearchController.GetBidsByTaskID();
        getBids.execute(id);
        try{
            bidList=getBids.get();
            Log.e("Got bids",bidList.toString());

        }
        catch(Exception e){
            Log.e("Bid get","not workng");
        }

    }


    /**
     * Retrieve the task details in order to be displayed by the activity
     */
    public void setTaskDetails(Task task){
        // get the task details to place on screen

        titleValue.setText(task.getTitle());
        descriptionValue.setText(task.getDescription());
        statusValue.setText(task.getStatus());

        if(task.getStatus().equals("assigned")){

            providerName.setText(task.getProvider());
            ElasticSearchController.GetUser getProvider= new ElasticSearchController.GetUser();
            getProvider.execute(task.getProvider());
            User provider=new User("","","");
            try{
                provider = getProvider.get();
                Log.e("Return requester",provider.getUsername());
            }
            catch(Exception e){
                Log.e("Requester name get","not workng");
            }
            providerPhone.setText(provider.getPhonenumber());
            providerEmail.setText(provider.getEmail());
            provideByShow.setText("Provided By: ");
        }
    }

    /**
     * States what should happen when a task is deleted.
     * This function searches for the task and deletes it from the server upon the
     *  user's prompting.
     */
    public void onDeleteTask(){
        // delete the task along with any other associated bids with it
        if (isNetworkAvailable()){
            if (!task.getStatus().equals("requested")) {
                // to delete any bids associated with the task
                ArrayList<Bid> bids;
                ElasticSearchController.GetBidsByTaskID taskBids = new ElasticSearchController.GetBidsByTaskID();
                taskBids.execute(task.getId());
                try {
                    bids = taskBids.get();
                    Log.e("It works", "got list of bids");

                    ElasticSearchController.deleteBid deleteBids = new ElasticSearchController.deleteBid();
                    for (Bid bid : bids) {
                        deleteBids.execute(bid.getId());
                    }
                    Log.e("It works", "Deleted all related bids to this task");
                } catch (Exception e) {
                    Log.e("Error", "With getting bids by task id");
                }
                Toast.makeText(DashboardRequestedTask.this,
                        "Deleted associated bids with task", Toast.LENGTH_SHORT).show();

            }
            ElasticSearchController.deleteTask deleteTask = new ElasticSearchController.deleteTask();
            deleteTask.execute(id);
            Log.e("deleted","task");
        }
        else{
            Toast.makeText(DashboardRequestedTask.this,
                    "Can't delete task offline", Toast.LENGTH_SHORT).show();
        }



    }

    /**
     * Allows a task requester to accept a bid. This clears all other bids from the bid list
     * and changes the task status to "accepted"
     *
     * @param target an integer representing the index of the bid that is retrieved
     *               by the activity
     */
    public void acceptBid(int target){
        //accpet a bid by changing the status of the bid to accepted and declining all other bids;
        if (task.getStatus().equals("accepted")){
            Toast.makeText(DashboardRequestedTask.this,
                    "Task has already been assigned", Toast.LENGTH_SHORT).show();
            return;
        }
        Bid acceptedBid = bidList.get(target);
        acceptedBid.setAccepted();
        ElasticSearchController.updateBid bid= new ElasticSearchController.updateBid();
        bid.execute(acceptedBid);
        Log.e("Accept bid","success");


        //decline all other bids by setting their status to declined;

        for (int index = 0; index < bidList.size(); index++) {
            if(!acceptedBid.getId().equals(bidList.get(index).getId())){
                ElasticSearchController.updateBid declineAllBids = new ElasticSearchController.updateBid();
                Bid declined=bidList.get(index);
                declined.setDeclined();
                declineAllBids.execute(declined);
            }
        }


        //change task status to assigned and set provider field of the task to the bidder
        task.setAssigned();
        task.setProvider(bidder.getUsername());
        ElasticSearchController.updateTask assigned = new ElasticSearchController.updateTask();
        assigned.execute(task);
        updateStatus.setVisibility(View.VISIBLE);
        reassign.setVisibility(View.VISIBLE);
        ressignTask();
        setStatusDone();
        Toast.makeText(DashboardRequestedTask.this,
                "Task has been assigned", Toast.LENGTH_SHORT).show();
    }

    /**
     * Declines a bid placed on a task. This will change the status of the bid to declined;
     *
     * @param target an integer representing which list item was selected and should be
     *               treated by the method
     */
    public void declineBids(int target){
        if (task.getStatus().equals("bidded")){
            ElasticSearchController.updateBid updateBid=new ElasticSearchController.updateBid();
            Bid toUpdate=bidList.get(target);
            toUpdate.setDeclined();
            updateBid.execute(toUpdate);
            setAdapter();
        }


    }

    /**
     * Function goes back to the activity we want when we click on the back button
     */

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MyTasksActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    /**
     * Checks for internet connection
     * @return boolean if we are connected to the internet or not
     */


    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }
}
