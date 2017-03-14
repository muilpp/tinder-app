package com.tinderapp.view;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HomeActivityTest {

    @Rule
    public ActivityTestRule<HomeActivity> mActivityRule = new ActivityTestRule<>(HomeActivity.class);

    @Test
    public void usersFragmentIsDisplayedWhenAddPersonIconIsClicked() {
//        onView(withId(R.id.add_person_layout)).perform(click());
//
//        onView(withId(R.id.users_fragment_layout)).check(matches(isDisplayed()));
    }
}