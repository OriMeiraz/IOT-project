package com.example.tutorial6;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
//import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.github.mikephil.charting.utils.ColorTemplate;

public class TerminalFragment extends Fragment implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }

    private String deviceAddress;
    private SerialService service;
    String is_peak;
    Float peak;
    Integer sound;

    private TextView receiveText;
    //private TextView sendText;
    private TextUtil.HexWatcher hexWatcher;

    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean hexEnabled = false;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;

    //LineChart mpLineChart;
    LineDataSet hDataSet;
    LineDataSet zDataSet;
    float time = 0;
    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
    LineData data;
    Button buttonCsvShow;
    Button buttonSave;
    Button buttonStartStop;
    Button buttonReset;
    EditText name;
    CheckBox isRunning;
    ArrayList<String> hArray = new ArrayList<>();
    ArrayList<String> zArray = new ArrayList<>();
    ArrayList<String> TimeArray = new ArrayList<>();
    ArrayList<Integer> SoundArray = new ArrayList<>();
    MediaPlayer player;
    PyObject pyobj;
    int MAX_VOLUME = 50;

    File record;

    public void play_drum1(View v, float vol){
        MediaPlayer mp =  MediaPlayer.create(getContext(), R.raw.drum1);
        vol = (float) (1 - (Math.log(MAX_VOLUME - vol) / Math.log(vol)));
        mp.setVolume(vol, vol);
        mp.start();
    }
    public void play_drum2(View v, float vol){
        MediaPlayer mp =  MediaPlayer.create(getContext(), R.raw.drum2);
        vol = (float) (1 - (Math.log(MAX_VOLUME - vol) / Math.log(vol)));
        mp.setVolume(vol, vol);
        mp.start();
    }
    public void play_drum3(View v, float vol){
        MediaPlayer mp =  MediaPlayer.create(getContext(), R.raw.drum3);
        vol = (float) (1 - (Math.log(MAX_VOLUME - vol) / Math.log(vol)));
        mp.setVolume(vol, vol);
        mp.start();
    }


    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");

    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);
        receiveText = view.findViewById(R.id.receive_text);                          // TextView performance decreases with number of spans
        receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());

        //hexWatcher = new TextUtil.HexWatcher(sendText);
        //hexWatcher.enable(hexEnabled);
        //sendText.addTextChangedListener(hexWatcher);
        //sendText.setHint(hexEnabled ? "HEX mode" : "");

        buttonCsvShow = (Button) view.findViewById(R.id.button2);
        buttonSave = (Button) view.findViewById(R.id.save_button);
        buttonStartStop = (Button) view.findViewById(R.id.start_stop);
        buttonReset = (Button) view.findViewById(R.id.reset_btn);
        name = (EditText) view.findViewById(R.id.rec_name);

        //mpLineChart = (LineChart) view.findViewById(R.id.line_chart);

        hDataSet =  new LineDataSet(emptyDataValues(), "height");
        hDataSet.setColor(ColorTemplate.rgb("00ff00"));
        hDataSet.setCircleColor(ColorTemplate.rgb("00ff00"));

        zDataSet =  new LineDataSet(emptyDataValues(), "z_accel");
        zDataSet.setColor(ColorTemplate.rgb("0000ff"));
        zDataSet.setCircleColor(ColorTemplate.rgb("0000ff"));


        dataSets.add(hDataSet);
        dataSets.add(zDataSet);
        data = new LineData(dataSets);
        File file = new File("/sdcard/Documents/proj_dir/");
        file.mkdir();
        //mpLineChart.setData(data);
        //mpLineChart.invalidate();
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(getActivity()));
        }

        Python py =  Python.getInstance();
        pyobj = py.getModule("test");

        record = new File("record.txt");


        buttonCsvShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenLoadCSV();
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeArray = new ArrayList<>();
                hArray = new ArrayList<>();
                zArray = new ArrayList<>();
                SoundArray = new ArrayList<>();
                Toast.makeText(service, "Recording has been reset", Toast.LENGTH_SHORT).show();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (name.getText().toString().equals("")){
                    Toast.makeText(service, "Must specify name", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    String row[];
                    File file = new File("/sdcard/Documents/proj_dir/");
                    file.mkdir();

                    String csv = "/sdcard/Documents/proj_dir/" + name.getText().toString() + ".dtg";
                    CSVWriter csvWriter = new CSVWriter(new FileWriter(csv, false));

                    int n = SoundArray.size();
                    for (int i = 0; i < n; i++) {
                        row = new String[]{String.valueOf(SoundArray.get(i))};
                        csvWriter.writeNext(row);
                    }
                    csvWriter.close();
                    hArray = new ArrayList<>();
                    zArray = new ArrayList<>();
                    TimeArray = new ArrayList<>();
                    SoundArray = new ArrayList<>();
                    Toast.makeText(getActivity(), "Saved", Toast.LENGTH_LONG).show();

                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        buttonStartStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (buttonStartStop.getText().equals("START")){
                    buttonStartStop.setText("STOP");
                    buttonStartStop.setTextColor(Color.RED);
                }
                else {
                    buttonStartStop.setText("START");
                    buttonStartStop.setTextColor(Color.GREEN);
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_terminal, menu);
        menu.findItem(R.id.hex).setChecked(hexEnabled);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.clear) {
//            receiveText.setText("");
//            return true;
//        } else if (id == R.id.newline) {
//            String[] newlineNames = getResources().getStringArray(R.array.newline_names);
//            String[] newlineValues = getResources().getStringArray(R.array.newline_values);
//            int pos = java.util.Arrays.asList(newlineValues).indexOf(newline);
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            builder.setTitle("Newline");
//            builder.setSingleChoiceItems(newlineNames, pos, (dialog, item1) -> {
//                newline = newlineValues[item1];
//                dialog.dismiss();
//            });
//            builder.create().show();
//            return true;
//        } else if (id == R.id.hex) {
//            hexEnabled = !hexEnabled;
//            sendText.setText("");
//            hexWatcher.enable(hexEnabled);
//            sendText.setHint(hexEnabled ? "HEX mode" : "");
//            item.setChecked(hexEnabled);
//            return true;
//        } else {
//            return super.onOptionsItemSelected(item);
//        }
//    }

    /*
     * Serial + UI
     */
    private String[] clean_str(String[] stringsArr){
         for (int i = 0; i < stringsArr.length; i++)  {
             stringsArr[i]=stringsArr[i].replaceAll(" ","");
        }


        return stringsArr;
    }
    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        if(connected != Connected.True) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String msg;
            byte[] data;
            if(hexEnabled) {
                StringBuilder sb = new StringBuilder();
                TextUtil.toHexString(sb, TextUtil.fromHexString(str));
                TextUtil.toHexString(sb, newline.getBytes());
                msg = sb.toString();
                data = TextUtil.fromHexString(msg);
            } else {
                msg = str;
                data = (str + newline).getBytes();
            }
            SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            receiveText.append(spn);
            service.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(byte[] message) {
        if(hexEnabled) {
            receiveText.append(TextUtil.toHexString(message) + '\n');
        } else {
            String msg = new String(message);
            String[] msg_arr;
            float h, z, t;
            if(newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                // don't show CR as ^M if directly before LF
                String msg_to_save = msg;
                msg_to_save = msg.replace(TextUtil.newline_crlf, TextUtil.emptyString);
                // check message length
                if (msg_to_save.length() > 1){
                    // split message string by ',' char
                    //float distance = Float.valueOf(msg_to_save.substring(9));
                    msg_arr = msg_to_save.split(", ");
                    t = Float.parseFloat(msg_arr[0]);
                    z = Float.parseFloat(msg_arr[1]);
                    h = Float.parseFloat(msg_arr[2]);

                    // function to trim blank spaces

                    // saving data to csv


                    // add received values to line dataset for plotting the linechart
                    is_peak = pyobj.callAttr("add_accel", String.valueOf(z)).toString();
                    sound = 0;
                    if (! is_peak.equals("False")){
                        peak = Float.parseFloat(is_peak);
                        if (h<20) {
                            play_drum3(getView(), peak);
                            sound = 3;
                        }
                        else if (h<40) {
                            play_drum2(getView(), peak);
                            sound = 2;
                        }
                        else if (h<60) {
                            play_drum1(getView(), peak);
                            sound = 1;
                        }

                        time = t;
                    }
                    if (buttonStartStop.getText().equals("STOP"))
                    {
                        TimeArray.add(String.valueOf(t));
                        zArray.add(String.valueOf(z));
                        hArray.add(String.valueOf(h));
                        SoundArray.add(sound);
                    }
                    data.addEntry(new Entry(t, h),0);
                    data.addEntry(new Entry(t, z),1);
                    //data.addEntry(new Entry(Float.parseFloat(parts[3]),Float.parseFloat(parts[1])),1);
                    //data.addEntry(new Entry(Float.parseFloat(parts[3]),Float.parseFloat(parts[2])),2);

                    zDataSet.notifyDataSetChanged(); // let the data know a dataSet chang
                    hDataSet.notifyDataSetChanged(); // let the data know a dataSet changed
                    //zDataSet.notifyDataSetChanged(); // let the data know a dataSet changed
                    //mpLineChart.notifyDataSetChanged(); // let the chart know it's data changed
                    //mpLineChart.invalidate(); // refresh

            }

                msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
                // send msg to function that saves it to csv
                // special handling if CR and LF come in separate fragments
                if (pendingNewline && msg.charAt(0) == '\n') {
                    Editable edt = receiveText.getEditableText();
                    if (edt != null && edt.length() > 1)
                        edt.replace(edt.length() - 2, edt.length(), "");
                }
                pendingNewline = msg.charAt(msg.length() - 1) == '\r';
            }

            receiveText.append(TextUtil.toCaretString(msg, newline.length() != 0));
        }
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        receiveText.append(spn);
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        try {
        receive(data);}
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }

    private ArrayList<Entry> emptyDataValues()
    {
        ArrayList<Entry> dataVals = new ArrayList<Entry>();
        return dataVals;
    }

    private void OpenLoadCSV(){
        Intent intent = new Intent(getContext(),LoadCSV.class);
        startActivity(intent);
    }

}
