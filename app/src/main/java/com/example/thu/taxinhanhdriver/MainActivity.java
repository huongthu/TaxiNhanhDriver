package com.example.thu.taxinhanhdriver;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.thu.fragments.BookFragment;
import com.example.thu.fragments.ChatFragment;
import com.example.thu.fragments.HistoryFragment;
import com.example.thu.fragments.ProfileFragment;
import com.example.thu.utils.Utils;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.URISyntaxException;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressDialog mProgressDialog;

    private Class currentClass = null;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://thesisk13.ddns.net:3001/");
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginHandle();

        if (!isLogin()) {
            return;
        }

        mSocket.connect();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loadFragment(BookFragment.class);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.nav_taxi) {
            loadFragment(BookFragment.class);
        } else if (id == R.id.nav_support) {
            loadFragment(ChatFragment.class);
        } else if (id == R.id.nav_history) {
            loadFragment(HistoryFragment.class);

        } else if (id == R.id.nav_promotion) {
            new GetContentFromUrl().execute(getResources().getString(R.string.url_nofitication));
        } else if (id == R.id.nav_user_info) {
            loadFragment(ProfileFragment.class);
        } else if (id == R.id.nav_logout) {
            doSignOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class GetContentFromUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return Utils.getContentFromUrl(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            ((LinearLayout)findViewById(R.id.llLoading)).setVisibility(View.GONE);
            //https://stackoverflow.com/questions/17939760/how-to-solve-android-os-networkonmainthreadexception-in-json
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();

                StrictMode.setThreadPolicy(policy);
            }

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Thông tin khuyến mãi")
                    .setMessage(result)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    }).create().show();
        }

        @Override
        protected void onPreExecute() {
            ((LinearLayout)findViewById(R.id.llLoading)).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) { }
    }

    private void loadFragment(Class classObject) {
        Fragment fragment = null;
        Class fragmentClass = classObject;

        if (classObject == currentClass) {
            return;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
            currentClass = classObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isLogin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return true;
        } else {
            return false;
        }
    }

    private void doSignOut() {
        if (null != FirebaseAuth.getInstance().getCurrentUser()) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
            MainActivity.this.finish();
        }
    }

    private void loginHandle() {
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    // Update user into to navigation
                    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                    navigationView.setNavigationItemSelectedListener(MainActivity.this);
                    View header =navigationView.getHeaderView(0);

                    TextView tvFullName = (TextView) header.findViewById(R.id.tvHeaderMainName);
                    TextView tvEmail = (TextView) header.findViewById(R.id.tvHeaderMainEmail);
                    tvFullName.setText(user.getDisplayName());
                    tvEmail.setText(user.getEmail());

                } else {
                    // User is signed out
                    // show Login Activity
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    MainActivity.this.finish();
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}