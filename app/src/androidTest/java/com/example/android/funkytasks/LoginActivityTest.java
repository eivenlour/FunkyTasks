package com.example.android.funkytasks;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.robotium.solo.Solo;


/**
 * Created by ${fc1} on 2018-03-11.
 */

public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {
    private Solo solo;

    public LoginActivityTest(){
        super(com.example.android.funkytasks.LoginActivity.class);
    }

    public void testStart() throws Exception{
        Activity activity = getActivity();

    }

    public void setUp() throws Exception{
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testFailedLogin(){
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        solo.enterText((EditText) solo.getView(R.id.editLoginName), "NonExistingUser");
        solo.clickOnButton("Login");
        solo.waitForText("Incorrect username");
        assertTrue(solo.searchText("Incorrect username"));
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);

    }

    public void testLogin() throws Exception{
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        //existing user
        User testingUser = new User("IntentTesting", "1234567890", "IT@ualbertac.ca");
        solo.enterText((EditText) solo.getView(R.id.editLoginName), testingUser.getUsername());
        solo.clickOnButton("Login");
        solo.waitForActivity("MainMenuActivity.class");
        solo.assertCurrentActivity("Wrong activity", MainMenuActivity.class);
    }

    public void testSignUp() throws Exception{
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        solo.clickOnText("Don't have an account?");
        solo.waitForActivity("SignUpActivity.class");
        solo.assertCurrentActivity("Wrong activity", SignUpActivity.class);
    }

    @Override
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }

}