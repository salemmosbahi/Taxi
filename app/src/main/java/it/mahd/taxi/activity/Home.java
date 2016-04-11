package it.mahd.taxi.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import it.mahd.taxi.Main;
import it.mahd.taxi.R;
import it.mahd.taxi.database.Notify;
import it.mahd.taxi.util.Controllers;

/**
 * Created by salem on 2/13/16.
 */
public class Home extends Fragment {
    SharedPreferences pref;
    Controllers conf = new Controllers();

    public Home() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home, container, false);
        pref = getActivity().getSharedPreferences(conf.app, Context.MODE_PRIVATE);

        //socket
        //new Notify().reclamationNotify(Home.this);

        Button Now_btn = (Button) rootView.findViewById(R.id.btn_now);
        Now_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pref.getString(conf.tag_token, "").equals("")){
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_body, new Login());
                    ft.addToBackStack(null);
                    ft.commit();
                    ((Main) getActivity()).getSupportActionBar().setTitle(getString(R.string.login));
                }else{
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_body, new BookNow());
                    ft.addToBackStack(null);
                    ft.commit();
                    ((Main) getActivity()).getSupportActionBar().setTitle(getString(R.string.now));
                }
            }
        });

        Button Advance_btn = (Button) rootView.findViewById(R.id.btn_advance);
        Advance_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pref.getString(conf.tag_token, "").equals("")){
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_body, new Login());
                    ft.commit();
                    ((Main) getActivity()).getSupportActionBar().setTitle(getString(R.string.login));
                }else{
                    Fragment fr = new BookAdvance();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Bundle args = new Bundle();
                    args.putDouble(conf.tag_latitude, 0);
                    args.putDouble(conf.tag_longitude, 0);
                    fr.setArguments(args);
                    ft.replace(R.id.container_body, fr);
                    ft.addToBackStack(null);
                    ft.commit();
                    ((Main) getActivity()).getSupportActionBar().setTitle(getString(R.string.advance));
                }
            }
        });

        Button Reclamation_btn = (Button) rootView.findViewById(R.id.btn_reclamation);
        Reclamation_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pref.getString(conf.tag_token, "").equals("")){
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_body, new Login());
                    ft.commit();
                    ((Main) getActivity()).getSupportActionBar().setTitle(getString(R.string.login));
                }else{
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_body, new Reclamation());
                    ft.addToBackStack(null);
                    ft.commit();
                    ((Main) getActivity()).getSupportActionBar().setTitle(getString(R.string.reclamation));
                }
            }
        });

        Button Profile_btn = (Button) rootView.findViewById(R.id.btn_profile);
        Profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pref.getString(conf.tag_token, "").equals("")){
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_body, new Login());
                    ft.commit();
                    ((Main) getActivity()).getSupportActionBar().setTitle(getString(R.string.login));
                }else{
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_body, new Profile());
                    ft.addToBackStack(null);
                    ft.commit();
                    ((Main) getActivity()).getSupportActionBar().setTitle(getString(R.string.profile));
                }
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().finish();
    }
}
