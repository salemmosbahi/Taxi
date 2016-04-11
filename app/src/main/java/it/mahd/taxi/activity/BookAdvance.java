package it.mahd.taxi.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.mahd.taxi.Main;
import it.mahd.taxi.R;
import it.mahd.taxi.util.Calculator;
import it.mahd.taxi.util.Controllers;
import it.mahd.taxi.util.Encrypt;
import it.mahd.taxi.util.ServerRequest;

/**
 * Created by salem on 2/13/16.
 */
public class BookAdvance extends Fragment {
    Calculator c = new Calculator();
    Controllers conf = new Controllers();
    ServerRequest sr = new ServerRequest();

    private TextView Latitude_txt, Longitude_txt,Date_txt, Time_txt;
    private FloatingActionButton Position_btn;
    private Button Send_btn;
    private Switch Repeat_switch;
    private ToggleButton Mon_btg,Tue_btg, Wed_btg, Thu_btg, Fri_btg, Sat_btg, Sun_btg;
    private HorizontalScrollView Repeat_hsv;
    private TextInputLayout Description_input;
    private EditText Description_etxt;
    private long[] tab;
    private int year, month, day, hour, minute;
    private String datetime = "";
    private double latitude, longitude = 0;
    private boolean Repeat, Mon, Tue, Wed, Thu, Fri, Sat, Sun;

    public BookAdvance() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bookadvance, container, false);
        latitude = getArguments().getDouble(conf.tag_latitude);
        longitude = getArguments().getDouble(conf.tag_longitude);

        Latitude_txt = (TextView) rootView.findViewById(R.id.latitude_txt);
        Longitude_txt = (TextView) rootView.findViewById(R.id.longitude_txt);
        Date_txt = (TextView) rootView.findViewById(R.id.date_txt);
        Time_txt = (TextView) rootView.findViewById(R.id.time_txt);
        Repeat_switch = (Switch) rootView.findViewById(R.id.repeat_swit);
        Repeat_hsv = (HorizontalScrollView) rootView.findViewById(R.id.repeat_hsv);
        Mon_btg = (ToggleButton) rootView.findViewById(R.id.mon_btg);
        Tue_btg = (ToggleButton) rootView.findViewById(R.id.tue_btg);
        Wed_btg = (ToggleButton) rootView.findViewById(R.id.wed_btg);
        Thu_btg = (ToggleButton) rootView.findViewById(R.id.thu_btg);
        Fri_btg = (ToggleButton) rootView.findViewById(R.id.fri_btg);
        Sat_btg = (ToggleButton) rootView.findViewById(R.id.sat_btg);
        Sun_btg = (ToggleButton) rootView.findViewById(R.id.sun_btg);
        Description_input = (TextInputLayout) rootView.findViewById(R.id.input_description);
        Description_etxt = (EditText) rootView.findViewById(R.id.description_etxt);
        Repeat = Sat = Sun = false;
        Mon = Tue = Wed = Thu = Fri = true;

        Calendar d = c.getCurrentTime();
        year = d.get(Calendar.YEAR);
        month  = d.get(Calendar.MONTH);
        day  = d.get(Calendar.DAY_OF_MONTH);
        hour = d.get(Calendar.HOUR_OF_DAY);
        minute = d.get(Calendar.MINUTE);

        if (latitude != 0){
            Latitude_txt.setText(getString(R.string.latitude) + " " + latitude);
            Longitude_txt.setText(getString(R.string.longitude) + " " + longitude);
        }
        Date_txt.setText(new SimpleDateFormat("d MMM yyyy").format(d.getTime()));
        Date_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(), R.style.MyMaterialDesignTheme, dateSetListener, year, month, day).show();
            }
        });

        Time_txt.setText(new SimpleDateFormat("K:mma").format(d.getTime()));
        Time_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(getActivity(), R.style.MyMaterialDesignTheme, timeSetListener, hour, minute, true).show();
            }
        });

        Position_btn = (FloatingActionButton) rootView.findViewById(R.id.position_btn);
        Position_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container_body, new BookAdvancePosition());
                ft.addToBackStack(null);
                ft.commit();
                ((Main) getActivity()).getSupportActionBar().setTitle(getString(R.string.advance));
            }
        });

        Repeat_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Repeat_hsv.setVisibility(View.VISIBLE);
                    Repeat = true;
                } else {
                    Repeat_hsv.setVisibility(View.GONE);
                    Repeat = false;
                }
            }
        });

        Mon_btg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Mon = true;
                } else {
                    Mon = false;
                }
            }
        });

        Tue_btg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Tue = true;
                } else {
                    Tue = false;
                }
            }
        });

        Wed_btg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Wed = true;
                } else {
                    Wed = false;
                }
            }
        });

        Thu_btg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Thu = true;
                } else {
                    Thu = false;
                }
            }
        });

        Fri_btg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Fri = true;
                } else {
                    Fri = false;
                }
            }
        });

        Sat_btg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Sat = true;
                } else {
                    Sat = false;
                }
            }
        });

        Sun_btg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Sun = true;
                } else {
                    Sun = false;
                }
            }
        });

        Send_btn = (Button) rootView.findViewById(R.id.send_btn);
        Send_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(conf.NetworkIsAvailable(getActivity())){
                    submitForm();
                }else{
                    Toast.makeText(getActivity(), R.string.networkunvalid, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return rootView;
    }

    private void submitForm(){
        SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy K:mma");
        try {
            Date date = format.parse(Date_txt.getText().toString() + " " + Time_txt.getText().toString());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            datetime = year + "/" + month + "/" + day + " " + hour + ":" + minute;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Encrypt algo = new Encrypt();
        int x = algo.keyVirtual();
        String key = algo.key(x);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(conf.tag_date, algo.dec2enc(datetime, key)));
        params.add(new BasicNameValuePair(conf.tag_latitude, algo.dec2enc(latitude + "", key)));
        params.add(new BasicNameValuePair(conf.tag_longitude, algo.dec2enc(longitude + "", key)));
        params.add(new BasicNameValuePair(conf.tag_repeat, algo.dec2enc(Boolean.toString(Repeat), key)));
        if (Repeat) {
            params.add(new BasicNameValuePair(conf.tag_mon, algo.dec2enc(Boolean.toString(Mon), key)));
            params.add(new BasicNameValuePair(conf.tag_tue, algo.dec2enc(Boolean.toString(Tue), key)));
            params.add(new BasicNameValuePair(conf.tag_wed, algo.dec2enc(Boolean.toString(Wed), key)));
            params.add(new BasicNameValuePair(conf.tag_thu, algo.dec2enc(Boolean.toString(Thu), key)));
            params.add(new BasicNameValuePair(conf.tag_fri, algo.dec2enc(Boolean.toString(Fri), key)));
            params.add(new BasicNameValuePair(conf.tag_sat, algo.dec2enc(Boolean.toString(Sat), key)));
            params.add(new BasicNameValuePair(conf.tag_sun, algo.dec2enc(Boolean.toString(Sun), key)));
        }
        params.add(new BasicNameValuePair(conf.tag_description, algo.dec2enc(Description_etxt.getText().toString(), key)));
        params.add(new BasicNameValuePair(conf.tag_key, x + ""));
        JSONObject json = sr.getJSON(conf.url_addBookAdvance, params);
        if(json != null){
            try{
                String jsonstr = json.getString(conf.response);
                Toast.makeText(getActivity(),jsonstr,Toast.LENGTH_LONG).show();
                if(json.getBoolean(conf.res)){
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_body, new Home());
                    ft.addToBackStack(null);
                    ft.commit();
                    ((Main) getActivity()).getSupportActionBar().setTitle(getString(R.string.home));
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }else{
            Toast.makeText(getActivity(),"App server is unavailable!",Toast.LENGTH_SHORT).show();
        }
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;
            Calendar c1 = Calendar.getInstance();
            c1.set(Calendar.YEAR, year);
            c1.set(Calendar.MONTH, month);
            c1.set(Calendar.DAY_OF_MONTH, day);

            Calendar d = c.getCurrentTime();
            int yearNow = d.get(Calendar.YEAR);
            int monthNow  = d.get(Calendar.MONTH);
            int dayNow  = d.get(Calendar.DAY_OF_MONTH);
            int hourNow = d.get(Calendar.HOUR_OF_DAY);
            int minuteNow = d.get(Calendar.MINUTE);
            int secondNow = d.get(Calendar.SECOND);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");
            try {
                Date startDate = simpleDateFormat.parse(dayNow + "/" + monthNow + "/" + yearNow + " " + hourNow + ":" + minuteNow + ":" + secondNow);
                Date endDate = simpleDateFormat.parse(day + "/" + month + "/" + year + " " + hour + ":" + minute + ":00");
                tab = c.getDifference2Dates(startDate, endDate);
            }catch (ParseException e){
                e.printStackTrace();
            }
            if (tab[0] >= 3600000 && tab[0] <= 604800000){
                Toast.makeText(getActivity(),"The remaining time to process the reservation is " + tab[1] + "day, " + tab[2] + "hour, "
                        + tab[3] + "minute, " + tab[4] + "second",Toast.LENGTH_SHORT).show();
                Send_btn.setEnabled(true);
            }else{
                Toast.makeText(getActivity(),"Error, The time is not between 1 hour and 7 days!! " + tab[1] + "day, " + tab[2] + "hour, "
                        + tab[3] + "minute, " + tab[4] + "second",Toast.LENGTH_SHORT).show();
                Send_btn.setEnabled(false);
            }
            Date_txt.setText(new SimpleDateFormat("d MMM yyyy").format(c1.getTime()));
        }
    };

    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int min) {
            hour = hourOfDay;
            minute = min;
            Calendar c1 = Calendar.getInstance();
            c1.set(Calendar.HOUR_OF_DAY, hour);
            c1.set(Calendar.MINUTE, minute);

            Calendar d = c.getCurrentTime();
            int yearNow = d.get(Calendar.YEAR);
            int monthNow  = d.get(Calendar.MONTH);
            int dayNow  = d.get(Calendar.DAY_OF_MONTH);
            int hourNow = d.get(Calendar.HOUR_OF_DAY);
            int minuteNow = d.get(Calendar.MINUTE);
            int secondNow = d.get(Calendar.SECOND);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");
            try {
                Date startDate = simpleDateFormat.parse(dayNow + "/" + monthNow + "/" + yearNow + " " + hourNow + ":" + minuteNow + ":" + secondNow);
                Date endDate = simpleDateFormat.parse(day + "/" + month + "/" + year + " " + hour + ":" + minute + ":00");
                tab = c.getDifference2Dates(startDate, endDate);
            }catch (ParseException e){
                e.printStackTrace();
            }
            if (tab[0] >= 3600000 && tab[0] <= 604800000){
                Toast.makeText(getActivity(),"The remaining time to process the reservation is " + tab[1] + "day, " + tab[2] + "hour, "
                        + tab[3] + "minute, " + tab[4] + "second",Toast.LENGTH_SHORT).show();
                Send_btn.setEnabled(true);
            }else{
                Toast.makeText(getActivity(),"Error, The time is not between 1 hour and 7 days!! " + tab[1] + "day, " + tab[2] + "hour, "
                        + tab[3] + "minute, " + tab[4] + "second",Toast.LENGTH_SHORT).show();
                Send_btn.setEnabled(false);
            }
            Time_txt.setText(new SimpleDateFormat("K:mma").format(c1.getTime()));
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        //socket.close();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.container_body, new Home());
        ft.addToBackStack(null);
        ft.commit();
        ((Main) getActivity()).getSupportActionBar().setTitle(getString(R.string.home));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
