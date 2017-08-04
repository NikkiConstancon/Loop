package com.reva.loop301.reva;

import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
/**
 * Created by Hristian Vitrychenko on 04/07/2017.
 */

/**
 * Class designed to help scrolling from real time data to statistical
 * data to historical data. In short: tab page.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    /**
     * Overridden on create method, adapted to create a tabbed page
     * @param savedInstanceState used to create an instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }


    /**
     * Overridden onCreateOptionsMenu, adapted to display tabs
     * @param menu holds the menu for holding tabs
     * @return true to show success
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Overridden onOptionsItemSelected, adapted for tab purposes
     * to scroll through pages.
     * @param item holds the menu item of the time
     * @return the item that was selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

   //Deleted PlaceholderFragment class here
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Overridden getItem to return instances of either real time tab page,
         * statistical tab page or historical tab page
         * @param position holds the position of the current page
         * @return Nothing, simply meant to switch tabs
         */
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    tab1realtime tab1 = new tab1realtime();
                    return tab1;
                case 1:
                    tab2statistical tab2 = new tab2statistical();
                    return tab2;
            }
            return null;
        }

        /**
         * Overridden getCount to return the number of tabs
         * @return the number of tabs you can scroll through
         */
        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        /**
         * Overridden getPageTitle, adapted to return specific titles
         * @param position holds the position of the page
         * @return nothing
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Real-Time Data";
                case 1:
                    return "Statistical and Historical Data";
            }
            return null;
        }
    }
}
