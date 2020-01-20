package com.elab.friendzone;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GroupchatActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private ImageButton sendbtn;
    private EditText usermsg;
    private ScrollView mscroll;
    private TextView displaytextmsgs;
    private FirebaseAuth mauth;
    private DatabaseReference userref, grpnameref, grpmsgkeyref;
    private String currentgroupname, currentuserid, currentusername, currentdate, currenttime;
    private RequestQueue queue;
    private String url="https://fcm.googleapis.com/fcm/send";
    private String userid="AxnQuYi7bfdO1f0XL4WF26yb6Vu2";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupchat2);
        currentgroupname = getIntent().getExtras().get("groupname").toString();
        mauth = FirebaseAuth.getInstance();
        currentuserid = mauth.getCurrentUser().getUid();
        queue= Volley.newRequestQueue(this);
       FirebaseMessaging.getInstance().subscribeToTopic(userid);
        userref = FirebaseDatabase.getInstance().getReference().child("Users");
        grpnameref = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentgroupname);
        initializefields();
        getusetrinfo();
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendmsgtodatabse();
                usermsg.setText("");
                mscroll.fullScroll(ScrollView.FOCUS_DOWN);
                sendNotification();

            }


        });

        Toast.makeText(GroupchatActivity.this, currentgroupname, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        grpnameref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Displaymsgs(dataSnapshot);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Displaymsgs(dataSnapshot);

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void initializefields() {
        mtoolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        //currentgroupname=getIntent().getExtras().get("groupname").toString();
        //Toast.makeText(GroupchatActivity.this,currentgroupname,Toast.LENGTH_LONG).show();
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentgroupname);
        sendbtn = (ImageButton) findViewById(R.id.sendbutton);
        usermsg = (EditText) findViewById(R.id.input_group_msg);
        displaytextmsgs = (TextView) findViewById(R.id.group_chat_text_display);
        mscroll = (ScrollView) findViewById(R.id.myscroll_view);
    }

    private void getusetrinfo() {
        userref.child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentusername = dataSnapshot.child("name").getValue().toString();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendmsgtodatabse() {
        String msg = usermsg.getText().toString();
        String msgkey = grpnameref.push().getKey();
        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(GroupchatActivity.this, "please write message first", Toast.LENGTH_LONG).show();
        } else {
            Calendar calfordate = Calendar.getInstance();
            SimpleDateFormat currentdateformat = new SimpleDateFormat(" dd MMM,yyyy");
            currentdate = currentdateformat.format(calfordate.getTime());
            Calendar calfortime = Calendar.getInstance();
            SimpleDateFormat currenttimeformat = new SimpleDateFormat("hh:mm a");
            currenttime = currenttimeformat.format(calfortime.getTime());
            HashMap<String, Object> grpmsgkey = new HashMap<>();
            grpnameref.updateChildren(grpmsgkey);
            grpmsgkeyref = grpnameref.child(msgkey);
            HashMap<String, Object> msginfomap = new HashMap<>();
            msginfomap.put("ukey",msgkey);
            msginfomap.put("name", currentusername);
            msginfomap.put("message", msg);

            msginfomap.put("Date", currentdate);

            msginfomap.put("Time", currenttime);
            grpmsgkeyref.updateChildren(msginfomap);


        }
    }
    private void Displaymsgs(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        Calendar calfordate = Calendar.getInstance();
        SimpleDateFormat currentdateformat = new SimpleDateFormat(" dd MMM,yyyy");
      String  currentdate = currentdateformat.format(calfordate.getTime());
        String key = dataSnapshot.getKey();
        while (iterator.hasNext()) {
            String chatdate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chattime = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatmsg = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatname = (String) ((DataSnapshot) iterator.next()).getValue();
            String keyid = (String) ((DataSnapshot) iterator.next()).getValue();

            String date = chatdate.substring(1, 3);
            int dateint = Integer.parseInt(date);
            String systemdates = currentdate.substring(1, 3);
            int systemdatte = Integer.parseInt(systemdates);
            systemdatte -= dateint;

            if (systemdatte >=1) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Groupus").child(currentgroupname).child(key);
                //reference.child("Date").removeValue();
                //reference.child("Time").removeValue();
                //reference.child("message").removeValue();
                //reference.child("name").removeValue();
                //reference.child("ukey").removeValue();
                dataSnapshot.getRef().removeValue();
            } else {
                displaytextmsgs.append(chatname + ":\n" + chatmsg + "\n" + chattime + "  " + chatdate + "\n\n\n");
                mscroll.fullScroll(ScrollView.FOCUS_DOWN);

            }
        }

    }
    private void sendNotification()
    {
        JSONObject mainobj=new JSONObject();
        try {
            mainobj.put("to","/topic/"+userid);
            JSONObject notification=new JSONObject();
            notification.put("title","amy title");
            notification.put("body","any body");
            mainobj.put("notification",notification);
            JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, url,
                    mainobj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                  //code for successful


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //code on error
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> header=new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AIzaSyAakQbiwjhxp3ERA4Tpm8SMdRq0ssaAOqc");
                    return  header;
                }
            };
            queue.add(request);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }
}




