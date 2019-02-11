package com.access.espol.marco77713.espolaccess.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.access.espol.marco77713.espolaccess.R;
import com.access.espol.marco77713.espolaccess.adapter.AccesibilityAdapter;
import com.access.espol.marco77713.espolaccess.adapter.SearcherAdapter;
import com.access.espol.marco77713.espolaccess.model.Accesibility;
import com.access.espol.marco77713.espolaccess.model.Edificio;
import com.access.espol.marco77713.espolaccess.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class BuildingActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    DatabaseReference myRef;

    TextView txtResultado;
    ImageView building;

    String edificio;
    AccesibilityAdapter accesibilityAdapter;
    RecyclerView recyclerView;

    Map<String, Drawable> imageBuildings = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building);

        Intent intent = getIntent();
        edificio = intent.getStringExtra("edificio");

        txtResultado = (TextView) findViewById(R.id.resultado_accesibilidad);
        building = (ImageView) findViewById(R.id.logo);

        recyclerView = (RecyclerView) findViewById(R.id.accesibility_building);
        this.setViews();


        imageBuildings.put("EDCOM", getResources().getDrawable(R.drawable.edcom_png));
        imageBuildings.put("FIEC", getResources().getDrawable(R.drawable.fiec_png));
        imageBuildings.put("UBEP", getResources().getDrawable(R.drawable.ubep_png));

        building.setBackground(imageBuildings.get(edificio));

        //DATABASE CALL AND WRITTING
        ConnectivityManager cm = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            new AsyncCaller().execute();
        }
        else{
            Toast.makeText(getBaseContext(), "No tienes acceso a internet, lo necesitas para hacer uso de la app", Toast.LENGTH_LONG).show();
        }
        showToolbar(edificio, true);
    }

    private void setViews() {
        this.txtResultado = (TextView) findViewById(R.id.resultado_accesibilidad);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
 finish();
    }


    public void  showToolbar(String title, boolean upButton){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        final Drawable upArrow = getResources().getDrawable(R.drawable.upbutton);


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'></font>"));
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
    }


    private class AsyncCaller extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void...voids) {

            myRef = database.getReference("edificios/" + edificio);
            myRef.addValueEventListener(new ValueEventListener() {

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    Edificio edificio = dataSnapshot.getValue(Edificio.class);

                    edificio.context = getBaseContext();

                    System.out.println(edificio.setResultViewAccesibility());

                    txtResultado.setText(edificio.getNombre_completo());

                    accesibilityAdapter = new AccesibilityAdapter(edificio.setResultViewAccesibility());
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getBaseContext());
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(accesibilityAdapter);

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    System.out.println("Failed to read value." + error.toException());
                }
            });

            return null;
        }

    }
}
