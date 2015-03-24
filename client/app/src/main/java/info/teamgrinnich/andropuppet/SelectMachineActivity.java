package info.teamgrinnich.andropuppet;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Activity to select machine template to build for AndroPuppet
 *
 * @author Jayson Grace ( jayson.e.grace @ gmail.com )
 * @version 1.0
 * @since 2014-03-24
 */
public class SelectMachineActivity extends Activity
{
    private RadioGroup radioGroupId;
    private RadioButton radioButton;
    private Button button;

    /**
     * Used to run debug blocks which help move development along
     */
    public static boolean DEBUG = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_machine);

        addButtonListener();
    }

    private static String getCommand(String machine)
    {
        if (machine == "Basic Developer Machine")
            return "bash buildDev.sh";
        else if (machine == "Penetration Testing Machine")
            return "bash buildPen.sh";
        else return "buildDev.sh";
    }

    private static String buildMachine(String machine, String ipAddress) throws IOException
    {
        JSch jsch = new JSch();
        com.jcraft.jsch.Session session = null;
        String result = "";
        String command;

        command = getCommand(machine);

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
            channel.setCommand("bash buildDev.sh");
            channel.connect(1000);
            java.lang.Thread.sleep(500);

            result = stream.toString();
            System.out.println(result);

            result = "Building " + machine + "- feel free to SSH in to check progress!";
        }
        catch (JSchException ex)
        {
            String s = ex.toString();
            System.out.println(s);
            result = "Invalid credentials - failed to build " + machine + "!";
        }
        catch (InterruptedException ex)
        {
            String s = ex.toString();
            System.out.println(s);
            result = "Connection interrupted - failed to build " + machine + "!";
        }
        finally
        {
            if (session != null)
                session.disconnect();
            return result;
        }
    }

    public void addButtonListener()
    {

        radioGroupId = (RadioGroup) findViewById(R.id.machineButtonGroup);

        button = (Button) findViewById(R.id.build_button);

        button.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {

                // get the selected radio button from the group
                int selectedOption = radioGroupId.getCheckedRadioButtonId();

                // find the radio button by the previously returned id
                radioButton = (RadioButton) findViewById(selectedOption);

                try
                {
                    new AsyncTask<String, String, String>()
                    {
                        @Override
                        protected String doInBackground(String... params)
                        {
                            String result = "";
                            // Change this
                            String ipAddress = "10.80.36.116";

                            try
                            {
                                result = buildMachine(radioButton.getText().toString(), ipAddress);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            return result;
                        }

                        protected void onPostExecute(String result)
                        {
                            Toast.makeText(SelectMachineActivity.this, result, Toast.LENGTH_SHORT).show();
                        }
                    }.execute("1");

                }
                catch (Exception e)
                {
                    Toast toast = Toast.makeText(SelectMachineActivity.this, "Unable to connect to the target system!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

        });

    }
}
