package com.dji.drone_rajawali;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.rajawali3d.math.Quaternion;
import org.rajawali3d.postprocessing.passes.BlurPass;
import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.RajawaliSurfaceView;
import org.rajawali3d.surface.RajawaliTextureView;

import static androidx.vectordrawable.graphics.drawable.PathInterpolatorCompat.EPSILON;
import static java.lang.Math.sqrt;


// y >> z
// x >> y
// z >> x

public class MainActivity extends AppCompatActivity {

    ProgressBar bar;

    Renderer renderer;
    int flag = 0;

    double pwm_x,pwm_y,pwm_z;//1500 to 2500

    double prev_Vx,prev_Vz;
    float prev_x_1=0,prev_y_1=0,prev_x_2=0,prev_y_2=0,flag_first_1=1,flag_first_2=1;
    float d_new_y=0,d_prev_y=0;
    TextView t1,t11,t111,t2,t22,t222,t1_right,t11_right,t111_right,t2_right,t22_right,t222_right;//for roll , pitch , yaw
    //control sensor
    SensorManager sensorManager;
    Sensor sensor;
    //control sensor
    private static final float NS2S = 1.0f / 1000000000.0f;///
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    private OnScreenJoystick joystick_left,joystick_right;//define joysticks
    public RajawaliSurfaceView rajawaliSurface;//define suface for 3D shape
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //gyroscope
        PackageManager packageManager = getPackageManager();
        boolean gyroExists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        if (gyroExists)//if phone has gyroscope
        {
            //establish sensor manager for controlling sensor
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            flag =1;//flag for phones with gyroscope > some functions under condition of having
            // flag=1 will run
        }
        else
        {
            Toast.makeText(this,"This phone doesn't have Gyroscope",Toast.LENGTH_SHORT).show();
        }

        //gyroscope


        //font
        Typeface typeface = ResourcesCompat.getFont(getBaseContext(), R.font.no54);
        //font
        //build ui
        RelativeLayout relParent = new RelativeLayout(this);
        RelativeLayout.LayoutParams relParentParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        relParent.setLayoutParams(relParentParam);

        rajawaliSurface = new RajawaliSurfaceView(this);//view for 3D shape , object of
        //Rajawali class
        renderer = new Renderer(this);//define renderer class
        rajawaliSurface.setSurfaceRenderer(renderer);//define renderer for 3D view class



        //get height and width of screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        //get height and width of screen

        Toast.makeText(this,"width is "+width,Toast.LENGTH_SHORT).show();

        RelativeLayout.LayoutParams rajparam = new RelativeLayout.LayoutParams(width/2, RelativeLayout.LayoutParams.MATCH_PARENT);
        rajawaliSurface.setRenderMode(IRajawaliSurface.RENDERMODE_WHEN_DIRTY);
        rajawaliSurface.setFrameRate(60.0);
        rajawaliSurface.setLayoutParams(rajparam);

        rajawaliSurface.setId(R.id.rajawali);





        //gridlayout
        //border
        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.BLACK); //white background
        border.setStroke(1, Color.GRAY); //black border with full opacity

        //border
        //left gridlayout
        GridLayout gridLayout = new GridLayout(this);


        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(width/4, RelativeLayout.LayoutParams.MATCH_PARENT);
        param.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);

        gridLayout.setLayoutParams(param);
        gridLayout.setColumnCount(1);
        gridLayout.setRowCount(4);



        ///linear 1
        GridLayout.LayoutParams layoutParams1 = new GridLayout.LayoutParams();

        layoutParams1.height = 0;
        layoutParams1.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams1.width = GridLayout.LayoutParams.MATCH_PARENT;
        layoutParams1.columnSpec = GridLayout.spec(0, 1, 1);
        layoutParams1.rowSpec = GridLayout.spec(0, 1, 1);

        LinearLayout l1 = new LinearLayout(this);
        l1.setBackground(border);



        l1.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams l1_pr = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
        l1_pr.weight =  0.5f;
        l1_pr.setMarginStart(10);
        l1_pr.gravity = Gravity.CENTER_VERTICAL;


        t1 = new TextView(this);
        t1.setGravity(Gravity.CENTER_VERTICAL);
        t1.setText("Battery : ");
        t1.setTextColor(Color.YELLOW);
        t1.setTypeface(typeface);
        t1.setTextSize(18);

        l1.addView(t1,l1_pr);

        LinearLayout.LayoutParams l2_pr = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
        l2_pr.weight =  0.5f;
        l2_pr.setMarginEnd(10);
        l2_pr.gravity = Gravity.CENTER_VERTICAL;

        bar = new ProgressBar(MainActivity.this,null,android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setScaleY(5f);
        bar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.WHITE));


        bar.setProgress(50);//battery
        bar.setPadding(0,0,10,0);
        bar.setForegroundGravity(Gravity.CENTER_VERTICAL);
        l1.addView(bar,l2_pr);


        gridLayout.addView(l1,layoutParams1);
        ///linear 1

        ///linear 2
        GridLayout.LayoutParams layoutParams11 = new GridLayout.LayoutParams();
        layoutParams11.height = 0;
        layoutParams11.width = GridLayout.LayoutParams.MATCH_PARENT;
        layoutParams11.columnSpec = GridLayout.spec(0, 1, 1);
        layoutParams11.rowSpec = GridLayout.spec(1, 1, 1);

        LinearLayout l11 = new LinearLayout(this);
        l11.setOrientation(LinearLayout.HORIZONTAL);
        l11.setBackground(border);


        LinearLayout.LayoutParams l11_pr = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
        l11_pr.weight =  0.5f;
        l11_pr.setMarginStart(10);
        l11_pr.gravity = Gravity.CENTER_VERTICAL;

         t11 = new TextView(this);
        t11.setGravity(Gravity.CENTER_VERTICAL);
        t11.setText("pwm_x:");
        t11.setTextColor(Color.YELLOW);
        t11.setTypeface(typeface);
        t11.setTextSize(18);

         t22 = new TextView(this);
        t22.setGravity(Gravity.CENTER_VERTICAL);
        t22.setText("Not Available");
        t22.setTextColor(Color.YELLOW);
        t22.setTypeface(typeface);
        t22.setTextSize(18);

        l11.addView(t11,l11_pr);
        l11.addView(t22,l11_pr);

        gridLayout.addView(l11,layoutParams11);
        ///linear 2
        ///linear 3
        GridLayout.LayoutParams layoutParams111 = new GridLayout.LayoutParams();
        layoutParams111.height = 0;
        layoutParams111.width = GridLayout.LayoutParams.MATCH_PARENT;
        layoutParams111.columnSpec = GridLayout.spec(0, 1, 1);
        layoutParams111.rowSpec = GridLayout.spec(2, 1, 1);

        LinearLayout l111 = new LinearLayout(this);
        l111.setOrientation(LinearLayout.HORIZONTAL);
        l111.setBackground(border);



        LinearLayout.LayoutParams l111_pr = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
        l111_pr.weight =  0.5f;
        l111_pr.setMarginStart(10);
        l111_pr.gravity = Gravity.CENTER_VERTICAL;


         t111 = new TextView(this);
        t111.setGravity(Gravity.CENTER_VERTICAL);
        t111.setText("pwm_z:");
        t111.setTextColor(Color.YELLOW);
        t111.setTypeface(typeface);
        t111.setTextSize(18);

         t222 = new TextView(this);
        t222.setGravity(Gravity.CENTER_VERTICAL);
        t222.setText("Not Available");
        t222.setTextColor(Color.YELLOW);
        t222.setTypeface(typeface);
        t222.setTextSize(18);

        l111.addView(t111,l111_pr);
        l111.addView(t222,l111_pr);

        gridLayout.addView(l111,layoutParams111);
        ///linear 3
        //joystick
        GridLayout.LayoutParams layoutParams4 = new GridLayout.LayoutParams();
        layoutParams4.height = 0;
        layoutParams4.width = GridLayout.LayoutParams.MATCH_PARENT;
        layoutParams4.columnSpec = GridLayout.spec(0, 1, 1);
        layoutParams4.rowSpec = GridLayout.spec(3, 1, 2);

        /*LinearLayout l4 = new LinearLayout(this);
        l4.setOrientation(LinearLayout.HORIZONTAL);
        l4.setBackground(border);
*/
        /*LinearLayout.LayoutParams l4_pr = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
        l4_pr.weight =  0.5f;*/

         joystick_left = new OnScreenJoystick(this);
         joystick_left.setJoystickListener(joystick_left_listener);


        /*l4.addView(joystick_left,l4_pr);
        l4.addView(joystick_right,l4_pr);*/

        gridLayout.addView(joystick_left,layoutParams4);
        //joystick
        //left gridlayout
        //right gridlayout
        // gridlayout right
        GridLayout gridLayout_right = new GridLayout(this);


        RelativeLayout.LayoutParams param_right = new RelativeLayout.LayoutParams(width/4, RelativeLayout.LayoutParams.MATCH_PARENT);
        param_right.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
        gridLayout.setLayoutParams(param_right);
        gridLayout.setColumnCount(1);
        gridLayout.setRowCount(4);

        ///linear 1
        GridLayout.LayoutParams layoutParams1_right = new GridLayout.LayoutParams();
        layoutParams1_right .height = 0;
        layoutParams1_right .width = GridLayout.LayoutParams.MATCH_PARENT;
        layoutParams1_right .columnSpec = GridLayout.spec(0, 1, 1);
        layoutParams1_right .rowSpec = GridLayout.spec(0, 1, 1);

        LinearLayout l1_right  = new LinearLayout(this);
        l1_right .setOrientation(LinearLayout.HORIZONTAL);
        l1_right .setBackground(border);


        LinearLayout.LayoutParams l1_pr_right  = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
        l1_pr_right .weight =  0.5f;
        l1_pr_right .setMarginStart(10);
        l1_pr_right .gravity = Gravity.CENTER_VERTICAL;

        t1_right  = new TextView(this);
        t1_right .setGravity(Gravity.CENTER_VERTICAL);
        t1_right .setText("variable:");
        t1_right.setTextColor(Color.YELLOW);
        t1_right.setTypeface(typeface);
        t1_right.setTextSize(18);

        t2_right  = new TextView(this);
        t2_right .setGravity(Gravity.CENTER_VERTICAL);
        t2_right .setText("Not Available");
        t2_right.setTextColor(Color.YELLOW);
        t2_right.setTypeface(typeface);
        t2_right.setTextSize(18);


        l1_right.addView(t1_right ,l1_pr_right);
        l1_right.addView(t2_right ,l1_pr_right);

        gridLayout_right.addView(l1_right ,layoutParams1_right );
        ///linear 1
        ///linear 2
        GridLayout.LayoutParams layoutParams11_right = new GridLayout.LayoutParams();
        layoutParams11_right .height = 0;
        layoutParams11_right .width = GridLayout.LayoutParams.MATCH_PARENT;
        layoutParams11_right .columnSpec = GridLayout.spec(0, 1, 1);
        layoutParams11_right .rowSpec = GridLayout.spec(1, 1, 1);

        LinearLayout l11_right  = new LinearLayout(this);
        l11_right .setOrientation(LinearLayout.HORIZONTAL);
        l11_right .setBackground(border);


        LinearLayout.LayoutParams l11_pr_right  = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
        l11_pr_right .weight =  0.5f;
        l11_pr_right .setMarginStart(10);
        l11_pr_right .gravity = Gravity.CENTER_VERTICAL;

        t11_right  = new TextView(this);
        t11_right .setGravity(Gravity.CENTER_VERTICAL);
        t11_right .setText("variable:");
        t11_right.setTextColor(Color.YELLOW);
        t11_right.setTypeface(typeface);
        t11_right.setTextSize(18);


        t22_right  = new TextView(this);
        t22_right .setGravity(Gravity.CENTER_VERTICAL);
        t22_right .setText("Not Available");
        t22_right.setTextColor(Color.YELLOW);
        t22_right.setTypeface(typeface);
        t22_right.setTextSize(18);


        l11_right.addView(t11_right ,l11_pr_right);
        l11_right.addView(t22_right ,l11_pr_right);

        gridLayout_right.addView(l11_right ,layoutParams11_right );
        ///linear 2
        ///linear 3
        GridLayout.LayoutParams layoutParams111_right  = new GridLayout.LayoutParams();
        layoutParams111_right .height = 0;
        layoutParams111_right .width = GridLayout.LayoutParams.MATCH_PARENT;
        layoutParams111_right .columnSpec = GridLayout.spec(0, 1, 1);
        layoutParams111_right .rowSpec = GridLayout.spec(2, 1, 1);

        LinearLayout l111_right  = new LinearLayout(this);
        l111_right .setOrientation(LinearLayout.HORIZONTAL);
        l111_right .setBackground(border);



        LinearLayout.LayoutParams l111_pr_right  = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
        l111_pr_right .weight =  0.5f;
        l111_pr_right .setMarginStart(10);
        l111_pr_right .gravity = Gravity.CENTER_VERTICAL;


        t111_right  = new TextView(this);
        t111_right .setGravity(Gravity.CENTER_VERTICAL);
        t111_right .setText("variable:");
        t111_right.setTextColor(Color.YELLOW);
        t111_right.setTypeface(typeface);
        t111_right.setTextSize(18);


        t222_right  = new TextView(this);
        t222_right .setGravity(Gravity.CENTER_VERTICAL);
        t222_right .setText("Not Available");
        t222_right.setTextColor(Color.YELLOW);
        t222_right.setTypeface(typeface);
        t222_right.setTextSize(18);


        l111_right .addView(t111_right ,l111_pr_right );
        l111_right .addView(t222_right ,l111_pr_right );

        gridLayout_right.addView(l111_right ,layoutParams111_right );
        ///linear 3
        //joystick right
        GridLayout.LayoutParams layoutParams4_right = new GridLayout.LayoutParams();
        layoutParams4_right.height = 0;
        layoutParams4_right.width = GridLayout.LayoutParams.MATCH_PARENT;
        layoutParams4_right.columnSpec = GridLayout.spec(0, 1, 1);
        layoutParams4_right.rowSpec = GridLayout.spec(3, 1, 2);



        joystick_right = new OnScreenJoystick(this);
        joystick_right.setJoystickListener(joystick_right_listener);

        /*l4.addView(joystick_left,l4_pr);
        l4.addView(joystick_right,l4_pr);*/

        gridLayout_right.addView(joystick_right,layoutParams4_right);
        //joystick right
        // gridlayout right
        //right gridlayout
        relParent.addView(gridLayout,param);//left grid

        gridLayout.setId(View.generateViewId());
        rajparam.addRule(RelativeLayout.RIGHT_OF,gridLayout.getId());
        relParent.addView(rajawaliSurface,rajparam);//3D object

        param_right.addRule(RelativeLayout.RIGHT_OF,rajawaliSurface.getId());
        relParent.addView(gridLayout_right,param_right);//right grid
        //grid
        setContentView(relParent,relParentParam);


    }

    public void onResume() {//for sensor
        super.onResume();
        if(flag == 1){//when phone has gyroscope
            sensorManager.registerListener(gyroListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        }
    }

    public void onStop() {
        super.onStop();
        if(flag == 1)//when phone has gyroscope
            sensorManager.unregisterListener(gyroListener);
    }
    public OnScreenJoystickListener joystick_left_listener = new OnScreenJoystickListener()
    {

        @Override
        public void onTouch(final MotionEvent pEvent,OnScreenJoystick joystick, float pX, float pY) {
            if (pEvent.getAction() == MotionEvent.ACTION_UP) {//when joystick is clicked
                Quaternion q = new Quaternion(Math.sqrt(2)/2,0,Math.sqrt(2)/2,0);

                renderer.Rotate_begin(q);

            }else {


                    d_new_y = joystick_left.distance_base(pEvent.getX());

                    int flag = 1;
                    if(d_new_y < d_prev_y)
                    {
                        flag = -1 ;
                    }
                    d_prev_y = d_new_y;

                    double Vy =  flag * joystick.getStrength() * Math.abs(Math.cos(Math.toRadians(joystick.getAngle())));//yaw
                    // Calculate the angular speed of the sample
                    Log.d("----pX :",String.valueOf(Math.round(pX * 100.0) / 100.0));

                    Log.d("----cos of angle is :",String.valueOf(Math.cos(Math.toRadians(joystick.getAngle()))));

                    ///////////////////////////////////////////////////////////////////
                    float strength = Float.parseFloat(String.valueOf(joystick.getStrength()));

                    final float dT = (float) (strength * 0.05);
                    // Axis of the rotation sample, not normalized yet.
                    float axisX = 0;
                    float axisZ = 0;
                    float axisY = (float) Vy;


                    // Calculate the angular speed of the sample
                    float omegaMagnitude_joy_left = (float) sqrt((float) (axisX * axisX + axisY * axisY + axisZ * axisZ));

                    // Normalize the rotation vector if it's big enough to get the axis
                    if (omegaMagnitude_joy_left > EPSILON) {
                        axisX /= omegaMagnitude_joy_left;
                    }

                    // Integrate around this axis with the angular speed by the time step
                    // in order to get a delta rotation from this sample over the time step
                    // We will convert this axis-angle representation of the delta rotation
                    // into a quaternion before turning it into the rotation matrix.
                    float thetaOverTwo = omegaMagnitude_joy_left * dT / 2.0f;
                    float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
                    float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
                    deltaRotationVector[0] = sinThetaOverTwo * axisX;
                    deltaRotationVector[1] = sinThetaOverTwo * axisY;
                    deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                    deltaRotationVector[3] = cosThetaOverTwo;


                    //timestamp_j_left = (float)(strength*0.5);
                    float[] deltaRotationMatrix = new float[9];
                    SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
                    renderer.Rotate(deltaRotationMatrix);

                    //send pwm data
                    /// range 1500 - 2000 - 2500
                    // if cos or sin was negative > 1500 to 2000 . otherwise 2000 to 2500
                    if(Math.cos(Math.toRadians(joystick.getAngle()))<0)
                    {
                        pwm_y = 2000 - (joystick.getStrength() * Math.abs(Math.cos(Math.toRadians(joystick.getAngle()))) * 500);
                    }
                    else
                    {
                        pwm_y = 2000 + (joystick.getStrength() * Math.abs(Math.cos(Math.toRadians(joystick.getAngle()))) * 500);
                    }

                }


        }
    };

    public OnScreenJoystickListener joystick_right_listener = new OnScreenJoystickListener()
    {

        @Override
        public void onTouch(final MotionEvent pEvent,OnScreenJoystick joystick, float pX, float pY) {
            if (pEvent.getAction() == MotionEvent.ACTION_UP) {//when joystick is clicked
                Quaternion q = new Quaternion(Math.sqrt(2)/2,0,Math.sqrt(2)/2,0);

                renderer.Rotate_begin(q);
                prev_Vx = 0;
                prev_Vz = 0;

            }else {
                if(flag_first_2 == 1 )
                {
                    prev_x_2 = (float)(Math.round(pX * 100.0) / 100.0);
                    prev_y_2 = (float)(Math.round(pY * 100.0) / 100.0);
                    flag_first_2 = 0;
                }
                else if(prev_y_2 != (float)(Math.round(pY * 100.0) / 100.0)
                    && prev_x_2 != (float)(Math.round(pX * 100.0) / 100.0)) {

                    prev_x_2 = (float)(Math.round(pX * 100.0) / 100.0);
                    prev_y_2 = (float)(Math.round(pY * 100.0) / 100.0);


                    /// angle that joystick takes and we want angles between -45 and 45
                    double Vz = (45)  * joystick.getStrength() * (Math.cos(Math.toRadians(joystick.getAngle())));//roll
                    double Vx = (45)  * joystick.getStrength() * (Math.sin(Math.toRadians(joystick.getAngle())));//pitch
                    ///

                    double final_Vz = 0,final_Vx = 0;

                    final_Vz = Vz - prev_Vz;//it indicates angle the obj should rotate >> roll
                    prev_Vz = Vz;

                    final_Vx = Vx - prev_Vx;//it indicates angle the obj should rotate >> pitch
                    prev_Vx = Vx;

                    // Calculate the strength of the sample
                    //Log.d("----strength is :", String.valueOf(joystick.getStrength()));
                    ///////////////////////////////////////////////////////////////////

                    renderer.rotate_degree(final_Vx,0,final_Vz);

                    //send pwm data
                    /// range 1500 - 2000 - 2500
                    // if cos or sin was negative > 1500 to 2000 . otherwise 2000 to 2500
                    if(Math.cos(Math.toRadians(joystick.getAngle()))<0)
                    {
                        pwm_z = 2000 - (joystick.getStrength() * Math.abs(Math.cos(Math.toRadians(joystick.getAngle()))) * 500);
                    }
                    else
                    {
                        pwm_z = 2000 + (joystick.getStrength() * Math.abs(Math.cos(Math.toRadians(joystick.getAngle()))) * 500);
                    }
                    t222.setText(String.valueOf((Math.round(pwm_z * 100.0) / 100.0)));
                    if(Math.sin(Math.toRadians(joystick.getAngle()))<0)
                    {
                        pwm_x = 2000 - (joystick.getStrength() * Math.abs(Math.sin(Math.toRadians(joystick.getAngle()))) * 500);
                    }
                    else
                    {
                        pwm_x = 2000 + (joystick.getStrength()* Math.abs(Math.sin(Math.toRadians(joystick.getAngle())))  * 500);
                    }
                    t22.setText(String.valueOf((Math.round(pwm_x * 100.0) / 100.0)));
                    //send pwm data
                }
            }
            ///////////////////////////////////////////////////////////////////
        }
    };


    public SensorEventListener gyroListener = new SensorEventListener() {

        public void onAccuracyChanged(Sensor sensor, int acc) {
        }

        public void onSensorChanged(SensorEvent event) {

            if(flag == 1) {//when phone has gyroscope

                // This time step's delta rotation to be multiplied by the current rotation
                // after computing it from the gyro sample data.
                if (timestamp != 0) {
                    final float dT = (event.timestamp - timestamp) * NS2S;
                    // Axis of the rotation sample, not normalized yet.
                    float axisX = event.values[1];
                    float axisY = event.values[0];
                    float axisZ = event.values[2];


                    // Calculate the angular speed of the sample
                    float omegaMagnitude = (float) sqrt((float)(axisX*axisX + axisY*axisY + axisZ*axisZ));

                    // Normalize the rotation vector if it's big enough to get the axis
                    if (omegaMagnitude > EPSILON) {
                        axisX /= omegaMagnitude;
                        axisY /= omegaMagnitude;
                        axisZ /= omegaMagnitude;
                    }

                    // Integrate around this axis with the angular speed by the time step
                    // in order to get a delta rotation from this sample over the time step
                    // We will convert this axis-angle representation of the delta rotation
                    // into a quaternion before turning it into the rotation matrix.
                    float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                    float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
                    float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
                    deltaRotationVector[0] = sinThetaOverTwo * axisX;
                    deltaRotationVector[1] = sinThetaOverTwo * axisY;
                    deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                    deltaRotationVector[3] = cosThetaOverTwo;

                }

                timestamp = event.timestamp;
                float[] deltaRotationMatrix = new float[9];
                SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
                renderer.Rotate(deltaRotationMatrix);
                // User code should concatenate the delta rotation we computed with the current
                // rotation in order to get the updated rotation.
                // rotationCurrent = rotationCurrent * deltaRotationMatrix;

                ///new
            }
        }
    };
}