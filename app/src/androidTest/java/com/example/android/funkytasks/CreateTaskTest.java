package com.example.android.funkytasks;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.test.filters.SmallTest;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;

import static com.example.android.funkytasks.CreateTaskActivity.REQUEST_IMAGE_CAPTURE;

/**
 * Created by jimi on 2018-03-15.
 */

public class CreateTaskTest extends ActivityInstrumentationTestCase2<LoginActivity> {
    private Solo solo;
    private ArrayList<Task>  allTask=new ArrayList<Task>();
    private Task result=new Task("CreatingTaskTest","test","qwerty123");
    private Task createdTask;

    public CreateTaskTest() {
        super(LoginActivity.class);

    }

    @Before
    public void setUp() throws Exception{
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void goToCreateTaskActivity(){
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        solo.enterText((EditText) solo.getView(R.id.editLoginName), "qwerty123");
        solo.clickOnButton("Login");
        solo.waitForActivity("MainMenuActivity.class");
        solo.assertCurrentActivity("Wrong activity", MainMenuActivity.class);
        solo.clickOnButton("create a funky task");
        solo.waitForActivity("CreateTaskActivity.class");
        solo.assertCurrentActivity("Wrong activity",CreateTaskActivity.class);
    }

    //test for US 01.01.01
    public void testCreateTask(){
        goToCreateTaskActivity();
        solo.enterText((EditText) solo.getView(R.id.AddTitle), result.getTitle());
        solo.enterText((EditText) solo.getView(R.id.AddDescription), result.getDescription());
        solo.clickOnView(solo.getView(R.id.fab));
        solo.waitForActivity("MainMenuActivity.class");
        solo.assertCurrentActivity("Wrong activity", MainMenuActivity.class);

    }


    //no test for camera since robotium doesnt support checking another application

    public void testAddLocation(){
        goToCreateTaskActivity();
        solo.clickOnButton("Add Location");
        solo.waitForActivity("DisplayMap.class");
        solo.assertCurrentActivity("Wrong activity", DisplayMap.class);
    }




    @After
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

}




