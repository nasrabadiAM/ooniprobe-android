package org.openobservatory.ooniprobe.fragment;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

import org.openobservatory.ooniprobe.R;
import org.openobservatory.ooniprobe.activity.MainActivity;
import org.openobservatory.ooniprobe.model.OONITests;
import org.openobservatory.ooniprobe.utils.NotificationHandler;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class SettingsFragment extends Fragment {
    private MainActivity mActivity;
    SharedPreferences preferences;
    RelativeLayout collector_addressLayout;
    RelativeLayout local_notifications_timeLayout;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivity = (MainActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement onViewSelected");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.setTitle(mActivity.getString(R.string.settings));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        final SwitchCompat include_ipButton = (SwitchCompat) v.findViewById(R.id.ck_include_ip);
        include_ipButton.setChecked(preferences.getBoolean("include_ip", false));
        include_ipButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                if (isChecked) {
                    editor.putBoolean("include_ip", true);
                } else {
                    editor.putBoolean("include_ip", false);
                }
                editor.commit();
            }
        });

        final SwitchCompat include_asnButton = (SwitchCompat) v.findViewById(R.id.ck_include_asn);
        include_asnButton.setChecked(preferences.getBoolean("include_asn", true));
        include_asnButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                if (isChecked) {
                    editor.putBoolean("include_asn", true);
                } else {
                    editor.putBoolean("include_asn", false);
                }
                editor.commit();
            }
        });

        final SwitchCompat include_ccButton = (SwitchCompat) v.findViewById(R.id.ck_include_country);
        include_ccButton.setChecked(preferences.getBoolean("include_cc", true));
        include_ccButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                if (isChecked) {
                    editor.putBoolean("include_cc", true);
                } else {
                    editor.putBoolean("include_cc", false);
                }
                editor.commit();
            }
        });

        final SwitchCompat send_crashButton = (SwitchCompat) v.findViewById(R.id.send_crash);
        send_crashButton.setChecked(preferences.getBoolean("send_crash", true));
        send_crashButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                if (isChecked) {
                    editor.putBoolean("send_crash", true);
                } else {
                    editor.putBoolean("send_crash", false);
                }
                editor.commit();
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
                final Boolean send_crash = preferences.getBoolean("send_crash", true);
                CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(send_crash).build();
                Fabric.with(mActivity, new Crashlytics.Builder().core(core).build());
            }
        });

        TextView collector_address = (TextView) v.findViewById(R.id.collector_address_subText);
        collector_address.setText(preferences.getString("collector_address", OONITests.COLLECTOR_ADDRESS));

        collector_addressLayout = (RelativeLayout) v.findViewById(R.id.collector_addressLayout);
        collector_addressLayout.setOnClickListener(new RelativeLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });
        final TextView max_runtime = (TextView) v.findViewById(R.id.max_runtimeEditText);
        String max_runtime_str = preferences.getString("max_runtime", OONITests.MAX_RUNTIME);
        max_runtime.setText(max_runtime_str);
        max_runtime.setImeOptions(EditorInfo.IME_ACTION_DONE);
        max_runtime.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String new_max_runtime = v.getText().toString();
                    if (Integer.parseInt(new_max_runtime) < 10){
                        new_max_runtime = "10";
                        Toast toast = Toast.makeText(mActivity, mActivity.getText(R.string.max_runtime_low), Toast.LENGTH_LONG);
                        View view = toast.getView();
                        TextView text = (TextView) view.findViewById(android.R.id.message);
                        text.setGravity(Gravity.CENTER);
                        toast.show();
                    }
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("max_runtime", new_max_runtime);
                    editor.commit();
                }
                return false;
            }
        });
        max_runtime.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        final EditText local_notifications_timeEditText = (EditText) v.findViewById(R.id.local_notifications_timeEditText);
        String local_notifications_time = preferences.getString("local_notifications_time", "18:00");
        String[] separated = local_notifications_time.split(":");
        int hours = Integer.valueOf(separated[0]);
        int minutes = Integer.valueOf(separated[1]);
        local_notifications_timeEditText.setText(String.format("%02d", hours) + ":" +String.format("%02d", minutes));
        InputMethodManager im = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(local_notifications_timeEditText.getWindowToken(), 0);
        local_notifications_timeLayout = (RelativeLayout) v.findViewById(R.id.local_notifications_timeLayout);
        SwitchCompat local_notificationsButton = (SwitchCompat) v.findViewById(R.id.local_notifications);
        local_notificationsButton.setChecked(preferences.getBoolean("local_notifications", false));
        local_notificationsButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                if (isChecked) {
                    local_notifications_timeLayout.setVisibility(View.VISIBLE);
                    editor.putBoolean("local_notifications", true);
                    NotificationHandler.setRecurringAlarm(mActivity.getApplicationContext());
                } else {
                    local_notifications_timeLayout.setVisibility(View.GONE);
                    editor.putBoolean("local_notifications", false);
                    NotificationHandler.cancelRecurringAlarm(mActivity.getApplicationContext());
                }
                editor.commit();
            }
        });

        local_notifications_timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(mActivity, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        Locale en_locale = new Locale("en","EN");
                        NumberFormat nf = NumberFormat.getInstance(en_locale);
                        int hour = Integer.parseInt(nf.format(selectedHour));
                        int minute = Integer.parseInt(nf.format(selectedMinute));
                        String time = String.format(en_locale, "%02d", hour) + ":" + String.format(en_locale, "%02d", minute);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("local_notifications_time", time);
                        editor.commit();
                        local_notifications_timeEditText.setText(String.format("%02d", selectedHour) + ":" + String.format("%02d", selectedMinute));
                        NotificationHandler.setRecurringAlarm(mActivity.getApplicationContext());
                    }
                }, hour, minute, true);
                mTimePicker.show();
            }
        });

        SwitchCompat upload_resultsButton = (SwitchCompat) v.findViewById(R.id.ck_upload_results);
        upload_resultsButton.setChecked(preferences.getBoolean("upload_results", true));
        upload_resultsButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                if (isChecked) {
                    collector_addressLayout.setVisibility(View.VISIBLE);
                    include_asnButton.setVisibility(View.VISIBLE);
                    include_ccButton.setVisibility(View.VISIBLE);
                    include_ipButton.setVisibility(View.VISIBLE);
                    editor.putBoolean("upload_results", true);
                } else {
                    collector_addressLayout.setVisibility(View.GONE);
                    include_asnButton.setVisibility(View.GONE);
                    include_ccButton.setVisibility(View.GONE);
                    include_ipButton.setVisibility(View.GONE);
                    editor.putBoolean("upload_results", false);
                }
                editor.commit();
            }
        });

        if (preferences.getBoolean("upload_results", true)){
            collector_addressLayout.setVisibility(View.VISIBLE);
            include_asnButton.setVisibility(View.VISIBLE);
            include_ccButton.setVisibility(View.VISIBLE);
            include_ipButton.setVisibility(View.VISIBLE);
        }
        else{
            collector_addressLayout.setVisibility(View.GONE);
            include_asnButton.setVisibility(View.GONE);
            include_ccButton.setVisibility(View.GONE);
            include_ipButton.setVisibility(View.GONE);
        }

        if (preferences.getBoolean("local_notifications", false))
            local_notifications_timeLayout.setVisibility(View.VISIBLE);
        else
            local_notifications_timeLayout.setVisibility(View.GONE);

        return v;
    }

    private void showPopup(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(getString(R.string.collector_address));

        final EditText input = new EditText(mActivity);
        input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setText(preferences.getString("collector_address", OONITests.COLLECTOR_ADDRESS));
        builder.setView(input);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("collector_address", input.getText().toString());
                editor.commit();
                //Workaround to reload settings
                TextView collector_address = (TextView)mActivity.findViewById(R.id.collector_address_subText);
                collector_address.setText(input.getText().toString());
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        AlertDialog d = builder.create();
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        d.show();
    }
}