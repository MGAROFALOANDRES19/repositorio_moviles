package com.access.espol.marco77713.espolaccess.views.fragments;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.access.espol.marco77713.espolaccess.R;
import com.access.espol.marco77713.espolaccess.adapter.PositionAdapter;
import com.access.espol.marco77713.espolaccess.adapter.SearcherAdapter;
import com.access.espol.marco77713.espolaccess.model.BuildRow;
import com.access.espol.marco77713.espolaccess.model.Edificio;
import com.access.espol.marco77713.espolaccess.model.Posicion;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment  implements SearchView.OnQueryTextListener{


    private List<BuildRow> buildRowsList = new ArrayList<>();
    private List<Edificio> edificioList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SearcherAdapter sAdapter;
    boolean isConnected;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("edificios");


    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.builds);
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            // Read from the database
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    edificioList.clear();
                    buildRowsList.clear();
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        Edificio edificio = snap.getValue(Edificio.class);
                        edificioList.add(edificio);
                        System.out.println("Value is: " + edificioList);
                    }
                    prepareBuilsData();

                    sAdapter = new SearcherAdapter(buildRowsList, getContext());
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(sAdapter);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    System.out.println("Failed to read value." + error.toException());
                }
            });
        }
        else
        {
            Toast.makeText(getContext(), "No tienes acceso a internet, lo necesitas para hacer uso de la app", Toast.LENGTH_LONG).show();
        }
        setHasOptionsMenu(true);

        return view;
    }

    private void prepareBuilsData() {
        System.out.println("PILAAAAAAAAS");
        for(Edificio edificio: edificioList){
            buildRowsList.add(new BuildRow(edificio.getNombre(), String.valueOf(edificio.getResultado_accesibilidad())));
            System.out.println(buildRowsList);
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem.OnActionExpandListener onActionExpandListener = new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        };

        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);

        menuItem.setOnActionExpandListener(onActionExpandListener);
//        menuItem.expandActionView();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {


        String userInput = newText.toLowerCase();
        List<BuildRow> newList = new ArrayList<>();

        for (BuildRow builRow : buildRowsList)
        {
            if(builRow.getNombreEdificio().toLowerCase().contains(userInput))
            {
                newList.add(builRow);
            }
        }

        System.out.println(buildRowsList);
        if(isConnected){
            sAdapter.updateList(newList);

        }

        return true;
    }

}
