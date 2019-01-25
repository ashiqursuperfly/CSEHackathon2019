package ashiqur.goriberfitbit.ui;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import ashiqur.goriberfitbit.R;
import ashiqur.goriberfitbit.utils.UiUtil;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView tvDebug;
    private TextView tvStepCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeXmlVariables();
    }

    private void initializeXmlVariables() {
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        tvDebug = findViewById(R.id.tv_debug);
        findViewById(R.id.btn_nav_drawer_toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }else drawer.openDrawer(GravityCompat.START);
            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        switchFragment(R.id.navdrawer_motion_detection);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        // Handle side navigation item clicks here.
        int id = item.getItemId();
        switchFragment(id);
        return true;
    }

    public void switchFragment(int id)
    {
        Fragment fragment = null;
        FragmentTransaction ft = null;
        switch (id)
        {
            case R.id.navdrawer_motion_detection:
                fragment = new MotionDetectionFragment();
                Bundle bundle = new Bundle();
                String myMessage = getIntent().getStringExtra("new user phone");
                bundle.putString("new user phone", myMessage );
                fragment.setArguments(bundle);
                ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.framelayout_content_main_activity, fragment,getSupportFragmentManager().getBackStackEntryCount()+"");
                ft.commit();
                ft.addToBackStack(null);
                break;
            case R.id.navdrawer_calc_heart_rate:
//                fragment=new CalcHeartRateFragment();
//                ft = getSupportFragmentManager().beginTransaction();
//                ft.replace(R.id.framelayout_content_main_activity, fragment,getSupportFragmentManager().getBackStackEntryCount()+"");
//                ft.commit();
                Intent i = new Intent(MainActivity.this, HeartRateMonitor.class);
                startActivity(i);

                //Toast.makeText(getApplicationContext(),"Premens",Toast.LENGTH_SHORT).show();
                break;
            case R.id.navdrawer_leaderboard:
                fragment = new LeaderboardFragment();
                ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.framelayout_content_main_activity, fragment,getSupportFragmentManager().getBackStackEntryCount()+"");
                ft.commit();

        }
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
    private int backPressCount=0;
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            if(getSupportFragmentManager().getBackStackEntryCount()<=2)
            {
                backPressCount++;
                if(backPressCount!=2)UiUtil.showToast(getApplicationContext(),"Press Back Again to Exit",Toast.LENGTH_SHORT);
                else finish();
            }
            else
            {
                backPressCount=0;
                //Log.wtf(TAG,getCurrentFragment().getClass().getName()+"");
                getSupportFragmentManager().popBackStackImmediate();

            }
        }

    }



}
