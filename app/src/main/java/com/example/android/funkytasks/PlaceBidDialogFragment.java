/**
 * PlaceBidDialogFragment
 *
 * Version 1.0.0
 *
 * Create by Funky Tasks on March 8th
 *
 * Copyright information: https://github.com/CMPUT301W18T20/FunkyTasks/wiki/Reuse-Statement
 */

package com.example.android.funkytasks;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.funkytasks.ElasticSearchController;
import com.example.android.funkytasks.R;

import java.util.ArrayList;

/**
 * This fragment allows a user to place a bid on a task
 */
public class PlaceBidDialogFragment extends DialogFragment {

    private Double bidAmount;
    private String requester;
    private String bidder;
    private String taskID;
    private Task task;

    /**
     * Creates a dialogue popup that loads when a user wants to place a bid on a task
     *
     * @param savedInstanceState a bundle representing the most recent state of the view
     * @return returns a dialogue window
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater

        builder.setTitle("Place Bid");
        LayoutInflater inflater = getActivity().getLayoutInflater();

        requester = getArguments().getString("requester");
        bidder = getArguments().getString("bidder");
        taskID = getArguments().getString("id");

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.dialog_placebid, null);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Place Bid", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        EditText moneyPlaced = (EditText) view.findViewById(R.id.bidMoney);
                        bidAmount = Double.valueOf(moneyPlaced.getText().toString());

                        Log.e("requester in fragment", requester);
                        Log.e("bidder in fragment", bidder);
                        Log.e("id in fragment", taskID);
                        Log.e("amount in fragment", moneyPlaced.getText().toString());

                        ElasticSearchController.GetTask getTask = new ElasticSearchController.GetTask();
                        getTask.execute(taskID);
                        try {
                            task = getTask.get();
                            Log.e("Return task title", task.getTitle());
                        } catch (Exception e) {
                            Log.e("Error", "Task get not working");
                        }

                        if (task.getStatus().equals("requested")) {
                            task.setBidded();
                            ElasticSearchController.updateTask updateTask = new ElasticSearchController.updateTask();
                            updateTask.execute(task);
                        }

                        ElasticSearchController.PlaceBid placeBid = new ElasticSearchController.PlaceBid();
                        Bid bidToAdd = new Bid(bidder, requester, bidAmount, taskID);
                        placeBid.execute(bidToAdd);

                        Toast.makeText(getActivity(), "Successfully placed a bid", Toast.LENGTH_SHORT).show();

                        sendToSolveTaskActivity(bidder);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PlaceBidDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    /**
     * Loads the solve activity task view
     *
     * @param username the username of the user that has placed the bid, which is to be
     *                 passed to the new intent
     */
    public void sendToSolveTaskActivity(String username) {
        Intent intent = new Intent(getActivity(), SolveTaskActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

}