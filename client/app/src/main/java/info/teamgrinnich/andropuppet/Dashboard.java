package info.teamgrinnich.andropuppet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class Dashboard extends Activity
{

    private Button buildMachine;
    private ListView lv;
    private ArrayList<String> machineList;
    private ArrayAdapter<String> adapter;
    private EditText et;
    private String ipAddress = "";

    private ArrayList<String> getMachineList(ArrayList<String> listOfMachines)
    {
        // Ssh to machine
        listOfMachines.add(ipAddress);
        return listOfMachines;
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

        ArrayList<String> machines = new ArrayList<String>();
        machines = getMachineList(machines);

        machineList.add(machines.toString());
        adapter.notifyDataSetChanged();

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
