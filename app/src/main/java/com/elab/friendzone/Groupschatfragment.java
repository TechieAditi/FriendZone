package com.elab.friendzone;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class Groupschatfragment extends Fragment {
    private View grpview;
    private ListView list_view;
    private ArrayAdapter<String> arrayadapter;
    private ArrayList<String> listofgrps=new ArrayList<>();
    private DatabaseReference groupref;


    public Groupschatfragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        grpview= inflater.inflate(R.layout.fragment_groupchatsfragment, container, false);

        groupref= FirebaseDatabase.getInstance().getReference().child("Groups");
        initializefileds();
        retriveanddisplaygroup();
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentgrpname=parent.getItemAtPosition(position).toString();
                Intent grpchatintent=new Intent(getContext(),GroupchatActivity.class);
                grpchatintent.putExtra("groupname",currentgrpname);
                startActivity(grpchatintent);


            }
        });
        return grpview;

    }



    private void initializefileds() {
        list_view=(ListView)grpview.findViewById(R.id.list_view);
        arrayadapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,listofgrps);
        list_view.setAdapter(arrayadapter);
    }
    private void retriveanddisplaygroup() {
        groupref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set=new HashSet<>();
                Iterator iterator=dataSnapshot.getChildren().iterator();
                while(iterator.hasNext())
                {
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }
                listofgrps.clear();
                listofgrps.addAll(set);
                arrayadapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
