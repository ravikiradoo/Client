package com.example.student.client;

import android.app.ActivityManager;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private String SERVICE_NAME = "Client Device";
    private String SERVICE_TYPE = "_http._tcp.";
    private String Host = "";
    private Boolean Found = false;
    String user="";
    private InetAddress hostAddress;
    private int hostPort;

    Socket socket;
    DataOutputStream outputStream = null;
    private NsdManager mNsdManager;
    private HashMap<String, serv_Info> hashmap = new HashMap<String, serv_Info>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        mNsdManager.discoverServices(SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);


    }

    public void ConnectToService(View v) throws IOException {
        EditText editText = (EditText) findViewById(R.id.editText);
        Host = editText.getText().toString();
        final serv_Info s = hashmap.get(Host);
        if (s != null) {

           clientThread ch = new clientThread(s.Ip,s.Port);
            ch.start();



                    }




        v.setEnabled(false);

    }
    public void SetUserName(View v)
    {
        EditText editText = (EditText)findViewById(R.id.editText1);
        user=editText.getText().toString();

        if(user.equals(""))
        {
            Toast.makeText(this,"Enter a valid user name",Toast.LENGTH_LONG).show();
        }
        else {
            Button button =(Button)findViewById(R.id.button);
            button.setEnabled(true);
            v.setEnabled(false);
        }
        }


    NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {

        // Called as soon as service discovery begins.
        @Override
        public void onDiscoveryStarted(String regType) {
            Toast.makeText(MainActivity.this, "Discovering devices", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceFound(NsdServiceInfo service) {
            // A service was found! Do something with it.


            Toast.makeText(MainActivity.this, "service found" + service.getPort(), Toast.LENGTH_SHORT).show();

            if (!service.getServiceType().equals(SERVICE_TYPE)) {
                // Service type is the string containing the protocol and
                // transport layer for this service.
                Log.d("nsdservice", "Unknown Service Type: " + service.getServiceType());
            } else if (service.getServiceName().equals(SERVICE_NAME)) {
                //Name of the service
                Log.d("nsdservice", "Same machine: " + SERVICE_NAME);
            } else {
                Log.d("nsdservice", "Diff Machine : " + service.getServiceName());
                // connect to the service and obtain serviceInfo
                mNsdManager.resolveService(service, mResolveListener);
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo service) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Toast.makeText(MainActivity.this, "Service lost", Toast.LENGTH_SHORT).show();
            Log.e("nsdserviceLost", "service lost" + service);
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Toast.makeText(MainActivity.this, "Discovering devices stopped", Toast.LENGTH_SHORT).show();
            Log.i("nsdserviceDstopped", "Discovery stopped: " + serviceType);
        }

        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.e("nsdServiceSrartDfailed", "Discovery failed: Error code:" + errorCode);
            Toast.makeText(MainActivity.this, "Discover start failed", Toast.LENGTH_SHORT).show();
            mNsdManager.stopServiceDiscovery(this);
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.e("nsdserviceStopDFailed", "Discovery failed: Error code:" + errorCode);
            Toast.makeText(MainActivity.this, "Discover stop failed", Toast.LENGTH_SHORT).show();
            mNsdManager.stopServiceDiscovery(this);
        }
    };

    NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Toast.makeText(MainActivity.this, "Resolve failed", Toast.LENGTH_SHORT).show();
            // Called when the resolve fails. Use the error code to debug.
            Log.e("nsdservicetag", "Resolve failed " + errorCode);
            Log.e("nsdservicetag", "serivce = " + serviceInfo);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {

            Log.d("nsdservicetag", "Resolve Succeeded. " + serviceInfo);

            if (serviceInfo.getServiceName().equals(SERVICE_NAME)) {
                Log.d("nsdservicetag", "Same IP.");
                return;
            }

            // Obtain port and IP
            hostPort = serviceInfo.getPort();
            hostAddress = serviceInfo.getHost();
            final String name = serviceInfo.getServiceName();
            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    TextView tv = new TextView(MainActivity.this);
                    tv.setText("Service Found : " + name);
                    LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
                    layout.addView(tv);
                    serv_Info s = new serv_Info(hostAddress.getHostAddress(), hostPort);
                    hashmap.put(name, s);

                }
            });

            Toast.makeText(MainActivity.this, "host address = " + hostAddress.getHostAddress(), Toast.LENGTH_SHORT).show();
        }
    };

    private class serv_Info {
        String Ip;
        int Port;

        public serv_Info(String ip, int port) {
            this.Ip = ip;
            this.Port = port;
        }
    }



public String GetStatus()
{

    JSONObject jsonObject=null;
   try
   {RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");

    String load = reader.readLine();

    String[] toks = load.split(" +");  // Split on one or more spaces

    long idle1 = Long.parseLong(toks[4]);
    long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
            + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

    try {
        Thread.sleep(360);
    } catch (Exception e) {
    }

    reader.seek(0);
    load = reader.readLine();
    reader.close();

    toks = load.split(" +");

    long idle2 = Long.parseLong(toks[4]);
    long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
            + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
    float cpu_usag = (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1)) * 100;

    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
    ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
    activityManager.getMemoryInfo(mi);
    double uedMegs = mi.availMem / 0x100000L;


    jsonObject = new JSONObject();
    jsonObject.put("status", "online");
    jsonObject.put("cpu", cpu_usag);
    jsonObject.put("memory", uedMegs);
       jsonObject.put("name",user);
      return jsonObject.toString();


} catch (IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "there is an exception", Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    return "";
    }

    class clientThread extends Thread
    {
        String Ip;
        int port;
        public clientThread(String ip,int p)
        {
            Ip=ip;
            port=p;
        }
        @Override
        public void run()
        {
            try {
                Socket socket = new Socket(Ip,port);
                DataOutputStream outputStream =new DataOutputStream(socket.getOutputStream());
                outputStream.writeUTF(user);
                while (true)
                {
                    final String Message=GetStatus();
                    outputStream.writeUTF(Message);
                    Thread.sleep(500);

                }
            } catch (final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,""+e,Toast.LENGTH_LONG).show();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
