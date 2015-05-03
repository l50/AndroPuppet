package info.teamgrinnich.andropuppet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;


public class Machine extends Activity
{
    private String serverIPAddress = "";
    private String user = "";
    private String pass = "";
    private String machineName = "";
    private ProgressDialog pd;

    private String getCommand(String machine)
    {
        if (machine.equals("basicDev"))
            return "(cd /Users/l/programs/java/Android/AndroPuppet/server && ruby getIPAddress.rb basicDev)";
        else if (machine.equals("doomMachine"))
            return "(cd /Users/l/programs/java/Android/AndroPuppet/server && ruby getIPAddress.rb doomMachine)";
        else if (machine.equals("puppetDev"))
            return "(cd /Users/l/programs/java/Android/AndroPuppet/server && ruby getIPAddress.rb puppetDev)";
        else
            return "(cd /Users/l/programs/java/Android/AndroPuppet/server && ruby getIPAddress.rb railsDev)";
    }

    private String getMachineInfo(String ipAddress, String user, String pass, String machineName) throws IOException
    {
        JSch jsch = new JSch();
        com.jcraft.jsch.Session session = null;
        String result = "";
        String command;

        command = getCommand(machineName);
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
            result = "Connection interrupted - cannot access " + machineName + "!";
        }
        catch (InterruptedException ex)
        {
            String s = ex.toString();
            System.out.println(s);
            result = "Connection interrupted - cannot access " + machineName + "!";
        }
        finally
        {
            if (session != null)
                session.disconnect();
            return result;
        }
    }

    private String[] parseResult(String output)
    {
        String[] resultItems = output.split(",");
        return resultItems;
    }


    private String getDestroyCommand(String machine)
    {
        if (machine.equals("basicDev"))
            return "(cd /Users/l/programs/java/Android/AndroPuppet/server && vagrant destroy -f basicDev)";
        else if (machine.equals("doomMachine"))
            return "(cd /Users/l/programs/java/Android/AndroPuppet/server && vagrant destroy -f doomMachine)";
        else if (machine.equals("puppetDev"))
            return "(cd /Users/l/programs/java/Android/AndroPuppet/server && vagrant destroy -f puppetDev)";
        else
            return "(cd /Users/l/programs/java/Android/AndroPuppet/server && vagrant destroy -f railsDev)";
    }

    private void destroyMachine(String machine)
    {

        JSch jsch = new JSch();
        com.jcraft.jsch.Session session = null;
        String command;

        command = getDestroyCommand(machine);
        try
        {
            session = jsch.getSession(user, serverIPAddress, 22);
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
        }
        catch (JSchException ex)
        {
            String s = ex.toString();
            System.out.println(s);
        }
        finally
        {
            if (session != null)
                session.disconnect();
        }
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent();
        intent.putExtra("back", "back");
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine);
        Bundle extras = getIntent().getExtras();

        if (extras != null)
        {
            serverIPAddress = extras.getString("cloudServerIP");
            user = extras.getString("username");
            pass = extras.getString("password");
            machineName = extras.getString("machineName");
        }
        pd = ProgressDialog.show(this, "Please Wait...", "Populating information for " + machineName, false, true);
        pd.setCanceledOnTouchOutside(false);
        try
        {
            new AsyncTask<String, String, String>()
            {
                @Override
                protected String doInBackground(String... params)
                {
                    String result = null;
                    try
                    {
                        result = getMachineInfo(serverIPAddress, user, pass, machineName);
                        while (result.isEmpty())
                        {
                            result = getMachineInfo(serverIPAddress, user, pass, machineName);
                        }
                        if (!result.isEmpty())
                            pd.dismiss();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    return result;
                }

                protected void onPostExecute(final String result)
                {
                    String[] output = parseResult(result);
                    TextView ip = (TextView) findViewById(R.id.machineip);
                    ip.setText("IP Address: " + output[0]);
                    TextView status = (TextView) findViewById(R.id.machinestatus);
                    status.setText("Machine Status: " + output[1]);

                }
            }.execute("1");
        }
        catch (Exception e)
        {
            Toast toast = Toast.makeText(Machine.this, "Unable to connect to the target system!", Toast.LENGTH_SHORT);
            toast.show();
        }

        Button button = (Button) findViewById(R.id.destroymachine);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    new AsyncTask<String, String, String>()
                    {
                        @Override
                        protected String doInBackground(String... params)
                        {
                            String result = "";
                            try
                            {
                                destroyMachine(machineName);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            return result;
                        }

                        protected void onPostExecute(final String result)
                        {
                            finish();
                        }
                    }.execute("1");
                }
                catch (Exception e)
                {
                    Toast toast = Toast.makeText(Machine.this, "Unable to connect to the target system!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }
}
