package info.teamgrinnich.andropuppet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
    private String user = "";
    private String pass = "";
    private ProgressDialog pd;
    private boolean resume = true;


    private HashMap parseOutput(String machines)
    {
        HashMap listOfMachines = new HashMap();

        if (!machines.isEmpty())
        {
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
        }
        else
            listOfMachines.put("Run again", "Run again");
        return listOfMachines;
    }

    /**
     * Facilitate building a machine based on a cloud server
     *
     * @param ipAddress The ip address of the cloud server to build the machine on
     * @return The result of trying to build the machine
     * @throws java.io.IOException If there is an issue during connectivity
     */
    private HashMap queryMachine(String ipAddress, String user, String pass) throws IOException
    {
        JSch jsch = new JSch();
        com.jcraft.jsch.Session session = null;
        String result = "";
        String command = "(cd /Users/l/programs/java/Android/AndroPuppet/server && vagrant status)";
        try
        {
            session = jsch.getSession(user, ipAddress, 22);
            session.setPassword(pass);

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
            // 4000 seems like a (mostly) optimal amount of time to send the ssh request for info
            java.lang.Thread.sleep(4000);

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

    public void updateMachines()
    {
        final GetMachines getMachines = new GetMachines();

        try
        {
            getMachines.execute("1");
        }
        catch (Exception e)
        {
            Toast toast = Toast.makeText(Dashboard.this, "Unable to connect to the target system!", Toast.LENGTH_SHORT);
            toast.show();
        }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String machineName = lv.getItemAtPosition(position).toString();
                if (!machineName.contains("No Machines Detected"))
                {
                    getMachines.cancel(true);
                    Intent intent = new Intent(getApplicationContext(), Machine.class);
                    intent.putExtra("cloudServerIP", ipAddress);
                    intent.putExtra("username", user);
                    intent.putExtra("password", pass);
                    intent.putExtra("machineName", machineName);
//                    startActivity(intent);
                    startActivityForResult(intent, 1);
                }
            }
        });
        buildMachine.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (machineList.size() < 4)
                {
                    getMachines.cancel(true);
                    Intent intent = new Intent(v.getContext(), SelectMachineActivity.class);
                    intent.putExtra("cloudServerIP", ipAddress);
                    intent.putExtra("username", user);
                    intent.putExtra("password", pass);
                    startActivityForResult(intent, 1);
//                    startActivity(intent);
                    overridePendingTransition(R.animator.animation1, R.animator.animation2);
                }
                else
                {
                    Toast toast = Toast.makeText(Dashboard.this, "You can only have 4 machines, delete a machine and try again!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        buildMachine = (Button) findViewById(R.id.button1);
        lv = (ListView) findViewById(R.id.listView1);
        machineList = new ArrayList<String>();

        adapter = new ArrayAdapter<String>(this,
                R.layout.customlayout, machineList);
        lv.setAdapter(adapter);

//        pd = ProgressDialog.show(this, "Please Wait...", "Populating Systems", false, true);
//        pd.setCanceledOnTouchOutside(false);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            ipAddress = extras.getString("cloudServerIP");
            user = extras.getString("username");
            pass = extras.getString("password");
        }

//        updateMachines();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (resume)
        {
            pd = ProgressDialog.show(this, "Please Wait...", "Updating Systems", false, true);
            pd.setCanceledOnTouchOutside(false);
            updateMachines();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                String returnedInfo = data.getStringExtra("back");
                if (returnedInfo.contains("back"))
                {
                    resume = false;
                }
            }
            else
                resume = true;
        }
    }

    private class GetMachines extends AsyncTask<String, String, HashMap>
    {
        @Override
        protected HashMap doInBackground(String... params)
        {
            HashMap result = null;
            try
            {
                for (int i = 0; i < 10; i++)
                {
                    if (isCancelled()) break;
                    result = queryMachine(ipAddress, user, pass);
                    // experimenting with this as a possible way to deal with the problems
                    if (i > 1 && result != null) break;
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                while (result.containsKey("Run again"))
                {
                    result = queryMachine(ipAddress, user, pass);
                }

                if (result.isEmpty())
                {
                    result.put("No Machines Detected", "No Machines Detected");
                    pd.dismiss();
                }
                else
                    pd.dismiss();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(final HashMap result)
        {
            machineList.clear();
            for (Object key : result.keySet())
            {
                if (result.get(key).toString().contains("running") || result.get(key).toString().contains("No"))
                {
                    // Check if the machineList already contains the machine, if not add it
                    if (!machineList.contains(key.toString()))
                    {
                        machineList.add(key.toString());
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }
    }
}