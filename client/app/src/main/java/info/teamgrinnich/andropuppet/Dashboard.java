package info.teamgrinnich.andropuppet;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dashboard extends Activity
{
    private Button buildMachine;
    private ListView lv;
    private ArrayList<String> machineList;
    private ArrayAdapter<String> adapter;
    private String ipAddress = "";

    private HashMap parseOutput(String machines)
    {
        HashMap listOfMachines = new HashMap();

        Pattern stringCuration = Pattern.compile("(.*running|not\\screated virtualbox\\))\\s"
        );
        Matcher stringCurationMatch = stringCuration.matcher(machines);
        ArrayList<String> machineCurationList = new ArrayList<String>();

        while (stringCurationMatch.find())
        {
            machineCurationList.add(stringCurationMatch.group(1));
        }

        Pattern status = Pattern.compile("(\\w+)\\s+(\\w+)");
        for (String machine : machineCurationList)
        {
            Matcher statusMatch = status.matcher(machine);
            if (statusMatch.find())
                listOfMachines.put(statusMatch.group(1), statusMatch.group(2));
        }
        return listOfMachines;
    }

    /**
     * Facilitate building a machine based on a cloud server
     *
     * @param ipAddress The ip address of the cloud server to build the machine on
     * @return The result of trying to build the machine
     * @throws java.io.IOException If there is an issue during connectivity
     */
    private HashMap queryMachine(String ipAddress) throws IOException
    {
        JSch jsch = new JSch();
        com.jcraft.jsch.Session session = null;
        String result = "";
        String command = "(cd /Users/l/programs/java/Android/AndroPuppet/server && vagrant status)";
        try
        {
            session = jsch.getSession("user", ipAddress, 22);
            session.setPassword("pass");

            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);
            session.connect();

            // SSH Channel
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            channel.setOutputStream(stream);

            // Execute command
            channel.setCommand(command);
            channel.connect(1000);
            // Need to increase to this point to allow for the result to come in
            java.lang.Thread.sleep(6000);

            result = stream.toString();
        }
        catch (JSchException ex)
        {
            String s = ex.toString();
            System.out.println(s);
        }
        catch (InterruptedException ex)
        {
            String s = ex.toString();
            System.out.println(s);
        }
        finally
        {
            HashMap machineInfo = parseOutput(result);

            if (session != null)
                session.disconnect();
            return machineInfo;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        buildMachine = (Button) findViewById(R.id.button1);
        lv = (ListView) findViewById(R.id.listView1);
        machineList = new ArrayList<String>();

        adapter = new ArrayAdapter<String>(this,
                R.layout.customlayout, machineList);
        lv.setAdapter(adapter);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            ipAddress = extras.getString("cloudServerIP");
        }
        Toast status2 = Toast.makeText(Dashboard.this, "Populating Systems, please wait!", Toast.LENGTH_LONG);
        status2.show();

        try
        {
            new AsyncTask<String, String, HashMap>()
            {
                @Override
                protected HashMap doInBackground(String... params)
                {
                    HashMap result = null;
                    try
                    {
                        result = queryMachine(ipAddress);
                        if (result.isEmpty())
                        {
                            result = queryMachine(ipAddress);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    return result;
                }

                protected void onPostExecute(final HashMap result)
                {
                    for (Object key : result.keySet())
                    {
                        if (result.get(key).toString().contains("running"))
                        {
                            machineList.add(key.toString());
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }.execute("1");
        }
        catch (Exception e)
        {
            Toast toast = Toast.makeText(Dashboard.this, "Unable to connect to the target system!", Toast.LENGTH_SHORT);
            toast.show();
        }

        buildMachine.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(v.getContext(), SelectMachineActivity.class);
                intent.putExtra("cloudServerIP", ipAddress);
                startActivity(intent);
                overridePendingTransition(R.animator.animation1, R.animator.animation2);
            }
        });
    }

}
