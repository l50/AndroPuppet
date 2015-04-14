package info.teamgrinnich.andropuppet;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main Activity for ViewComponent for AndroPuppet
 * <p/>
 * Used to facilitate testing connectivity from the Android device to the cloud server and get the network
 * information about the Android device
 * <p/>
 * Based on code from here: https://developer.android.com/training/implementing-navigation/nav-drawer.html
 *
 * @author Jayson Grace ( jayson.e.grace @ gmail.com )
 * @version 1.0
 * @since 2014-04-07
 */
public class MainActivity extends Activity
{
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mSettingTitles;

    public static final String MENU_ITEM = "menu_item_number";
    /**
     * Used to run debug blocks which help move development along
     */
    public static boolean DEBUG = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mSettingTitles = getResources().getStringArray(R.array.settings_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mSettingTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        )
        {
            public void onDrawerClosed(View view)
            {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView)
            {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null)
        {
            selectItem(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            selectItem(position);
        }
    }

    /**
     * Get information about the network the android device is connected to
     *
     * @param context current context
     * @return all network information for the android device
     */
    private String[] getNetworkInformation(Context context)
    {
        String[] output = new String[4];

        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String ipAddress = Formatter.formatIpAddress(ip);
        output[0] = ipAddress;
        // Get DNS Server
        final DhcpInfo dhcp = wifiMgr.getDhcpInfo();
        output[1] = Formatter.formatIpAddress(dhcp.netmask);
        output[2] = Formatter.formatIpAddress(dhcp.gateway);
        output[3] = Formatter.formatIpAddress(dhcp.dns1);
        return output;
    }

    /**
     * Select one of the fragment items
     *
     * @param position position of the fragment on the menu
     */
    private void selectItem(int position)
    {
        Fragment fragment = null;
        Bundle args = new Bundle();

        switch (position)
        {
            case 0:
                fragment = new server_settings_Fragment();
                break;
            case 1:
                fragment = new client_settings_Fragment();
                args.putStringArray("netInfo", getNetworkInformation(this));
                fragment.setArguments(args);
                break;
        }
        // update the main content by replacing fragments
        args.putInt(MENU_ITEM, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mSettingTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title)
    {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Fragment for server settings menu option
     */
    public static class client_settings_Fragment extends Fragment
    {
        /**
         * Empty constructor required for fragment subclasses
         */
        public client_settings_Fragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.client_settings_layout, container, false);
            int i = getArguments().getInt(MENU_ITEM);
            String listItem = getResources().getStringArray(R.array.settings_array)[i];

            String[] networkInfo = this.getArguments().getStringArray("netInfo");
            String ip = networkInfo[0];
            TextView ipAddress = (TextView) rootView.findViewById(R.id.clientip);
            ipAddress.setText("IP Address: " + ip);

            String netMask = networkInfo[1];
            TextView netMaskAddr = (TextView) rootView.findViewById(R.id.clientnetmask);
            netMaskAddr.setText("Subnet Mask: " + netMask);

            String gateway = networkInfo[2];
            TextView gatewayAddr = (TextView) rootView.findViewById(R.id.clientgw);
            gatewayAddr.setText("Default Gateway: " + gateway);

            String dns = networkInfo[3];
            TextView dnsServer = (TextView) rootView.findViewById(R.id.clientdns);
            dnsServer.setText("DNS Server: " + dns);

            getActivity().setTitle(listItem);
            return rootView;
        }
    }

    /**
     * Validate IP address
     *
     * @param ip ip address to validate
     * @return whether or not the ip address is valid
     */
    private static boolean isValidIP(String ip)
    {
        Pattern validIp = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}" +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        Matcher matcher = validIp.matcher(ip);
        return matcher.matches();
    }

    /**
     * Validate Subnet Mask
     *
     * @param subnetMask subnet mask to validate
     * @return whether or not the subnet mask is valid
     */
    private static boolean isValidSubnet(String subnetMask)
    {
        Pattern validSubnet = Pattern.compile("^((128|192|224|240|248|252|254)\\.0\\.0\\.0)|" +
                "(255\\.(((0|128|192|224|240|248|252|254)\\.0\\.0)|(255\\.(((0|128|192|224" +
                "|240|248|252|254)\\.0)|255\\.(0|128|192|224|240|248|252|254)))))$");
        Matcher matcher = validSubnet.matcher(subnetMask);
        return matcher.matches();
    }

    /**
     * Test connectivity from Android device to the cloud server
     *
     * @param ipAddress IP address for the cloud server
     * @return The result of attempting to connect to the server
     * @throws IOException If there is an issue during connectivity
     */
    private static String testConnection(String ipAddress) throws IOException
    {
        Pattern successfulSSH = Pattern.compile("Desktop");

        JSch jsch = new JSch();
        com.jcraft.jsch.Session session = null;
        String result = "";

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
            channel.setCommand("ls -lart");
            channel.connect(1000);
            java.lang.Thread.sleep(500);

            result = stream.toString();

            Matcher matcher = successfulSSH.matcher(result);
            if (matcher.find())
            {
                result = "Successfully connected!";
            }
        }
        catch (JSchException ex)
        {
            String s = ex.toString();
            System.out.println(s);
            result = "Invalid credentials!";
        }
        catch (InterruptedException ex)
        {
            String s = ex.toString();
            System.out.println(s);
            result = "Connection interrupted!";
        }
        finally
        {
            if (session != null)
                session.disconnect();
            return result;
        }
    }

    /**
     * Fragment for server settings menu option
     */
    public static class server_settings_Fragment extends Fragment
    {
        // Empty constructor required for fragment subclasses
        public server_settings_Fragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.server_settings_layout, container, false);
            int i = getArguments().getInt(MENU_ITEM);
            String listItem = getResources().getStringArray(R.array.settings_array)[i];

            final EditText ipText = (EditText) rootView.findViewById(R.id.edittextip);
            final EditText smText = (EditText) rootView.findViewById(R.id.edittextsm);
            final EditText gwText = (EditText) rootView.findViewById(R.id.edittextgw);
            final EditText dnsText = (EditText) rootView.findViewById(R.id.edittextdns);

            // Listener for test connection button
            rootView.findViewById(R.id.testServerConnectButton).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    final String ip = ipText.getText().toString();
                    final String sm = smText.getText().toString();
                    final String gw = gwText.getText().toString();
                    final String dns = dnsText.getText().toString();

                    if (DEBUG)
                    {
                        ipText.setText("192.168.1.145");
                        smText.setText("255.255.255.0");
                        gwText.setText("10.80.36.1");
                        dnsText.setText("10.80.36.1");
                    }
                    if (!isValidIP(ip))
                    {
                        ipText.setError("Invalid IP");
                    }
                    else if (!isValidSubnet(sm))
                    {
                        smText.setError("Invalid Subnet Mask");
                    }
                    else if (!isValidIP(gw))
                    {
                        gwText.setError("Invalid Gateway");
                    }
                    else if (!isValidIP(dns))
                    {
                        dnsText.setError("Invalid DNS Server");
                    }
                    else
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
                                        result = testConnection(ip);
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    return result;
                                }

                                protected void onPostExecute(String result)
                                {
                                    Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
                                }
                            }.execute("1");

                        }
                        catch (Exception e)
                        {
                            Toast toast = Toast.makeText(getActivity(), "Unable to connect to the target system!", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
            });
            // Listener for connect button
            rootView.findViewById(R.id.serverConnectButton).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Used to hold ip, which we will pass to the next activity
                    final String ip = ipText.getText().toString();
                    // Ensure an ip exists and is properly formatted
                    if (!isValidIP(ip))
                    {
                        ipText.setError("Invalid IP");
                    }
                    else
                    {
//                        Intent intent = new Intent(getActivity(), SelectMachineActivity.class);
                        Intent intent = new Intent(getActivity(), Dashboard.class);
                        intent.putExtra("cloudServerIP", ip);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.animator.animation1, R.animator.animation2);
                    }
                }
            });
            getActivity().setTitle(listItem);
            return rootView;
        }
    }
}