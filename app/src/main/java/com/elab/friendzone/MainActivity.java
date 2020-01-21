package com.elab.friendzone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private tabaccsesseradapter tabsaccesseradapter;
    private FirebaseAuth mauth;
    private DatabaseReference rootref;

    private FirebaseUser currentuser;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mtoolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        mauth = FirebaseAuth.getInstance();
        currentuser = mauth.getCurrentUser();
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("FRIENDZONE");
        viewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        rootref = FirebaseDatabase.getInstance().getReference();
        tabsaccesseradapter = new tabaccsesseradapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsaccesseradapter);

        tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentuser==null)
        {
            sendUsertologinactivity();
            finish();
        }
        else
        {
            verifyexistence();
        }

    }

    private void verifyexistence() {
        String currentuserid = mauth.getCurrentUser().getUid();

        rootref.child("Users").child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists()))
                {
                    Toast.makeText(MainActivity.this,"Welcome Back",Toast.LENGTH_LONG).show();
                }
                else
                {
                    sendUsertosettings();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUsertologinactivity() {
        Intent loginintent=new Intent(MainActivity.this,Groupchat.class);
        loginintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginintent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout)
        {
            mauth.signOut();
            sendUsertologinactivity();
            finish();
        }
        if(item.getItemId()==R.id.main_settings)
        {
            sendUsertosettings();


        }
        if(item.getItemId()==R.id.main_groupid)
        {

            requestnewgroup();
        }
        return true;
    }

    private void requestnewgroup() {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name");
        final EditText grpname=new EditText(MainActivity.this);
        grpname.setHint(" eg.Debate box");
        builder.setView(grpname);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String grp=grpname.getText().toString();
                if(TextUtils.isEmpty(grp))
                {
                    Toast.makeText(MainActivity.this,"Please enter a group name",Toast.LENGTH_LONG).show();
                }
                else
                {

                    createnewgroup(grp);
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void createnewgroup(final String grp) {
        rootref.child("Groups").child(grp).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this,grp+"Created successfully !!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void sendUsertosettings() {
        Intent settingintent=new Intent(MainActivity.this,Settingsactivity.class);
        startActivity(settingintent);
        settingintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingintent);
        finish();

    }

}

