package it.mahd.taxi.database;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import it.mahd.taxi.R;
import it.mahd.taxi.activity.ReclamationChat;
import it.mahd.taxi.util.Controllers;

/**
 * Created by salem on 4/1/16.
 */
public class ServiceAdapterList extends BaseAdapter {
    Controllers conf = new Controllers();
    LayoutInflater inflater;
    Context context;
    List<ServicesDB> data;
    Fragment fragment;
    int note;

    public ServiceAdapterList(Context context, List<ServicesDB> data, Fragment fragment) {
        this.context = context;
        this.data = data;
        this.fragment = fragment;
    }

    @Override
    public int getCount() { return data.size(); }

    @Override
    public Object getItem(int position) { return data.get(position); }

    @Override
    public long getItemId(int position) { return data.indexOf(getItem(position)); }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ServiceHolder holder = new ServiceHolder();
        if (v == null) {
            inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.booknow_dialog_list, null);
            holder.Name_cbx = (CheckBox) v.findViewById(R.id.name_cbx);
            note = 0;
            v.setTag(holder);
        } else {
            holder = (ServiceHolder) v.getTag();
        }
        holder.Name_cbx.setText(data.get(position).getName().toString());
        holder.Name_cbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    note += data.get(position).getValue();
                } else {
                    note -= data.get(position).getValue();
                }
            }
        });
        return v;
    }

    public int getNote() { return (note / getTotalNote()) * 20; }

    public int getTotalNote() {
        int total = 0;
        for (int i = 0; i<getCount(); i++){
            total += data.get(i).getValue();
        }
        return total;
    }

    class ServiceHolder {
        CheckBox Name_cbx;
    }
}
