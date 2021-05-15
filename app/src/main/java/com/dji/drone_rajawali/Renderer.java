package com.dji.drone_rajawali;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.widget.Toast;

import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.ArcballCamera;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.RajawaliRenderer;

import java.util.Stack;

public class Renderer extends RajawaliRenderer {

    public Context context;
    private DirectionalLight directionalLight;
    private Vector3 xvect,yvect,zvect;
    private Object3D obj;
    private Line3D xline,yline,zline;

    public Renderer(Context context) {
        super(context);
        this.context = context;
        setFrameRate(60);
    }


    @Override
    protected void initScene() {
        //my drone

        ///
        xvect = new Vector3(1,0,0);
        yvect = new Vector3(0,1,0);
        zvect = new Vector3(0,0,1);
        ///
        DirectionalLight key = new DirectionalLight(-3,-4,-5);//set the position of
        //light
        key.setColor(1.0f, 1.0f, 1.0f);//color of light
        key.setPower(2);//power of light
        getCurrentScene().addLight(key);//add light to screen
        getCurrentScene().setBackgroundColor(Color.BLACK);//setBackgroundColor

        //load 3D obj
        LoaderOBJ loader = new LoaderOBJ(getContext().getResources(), mTextureManager, R.raw.drone_obj);

        try {
            //define material for it
            Material material1 = new Material();
            material1.enableLighting(true);
            material1.setDiffuseMethod(new DiffuseMethod.Lambert());
            material1.setColor(Color.BLACK);


            loader.parse();

            obj = loader.getParsedObject();

            obj.setScale(0.02f);//scale of obj
            obj.setPosition(0,0,0);//position of obj
            obj.setMaterial(material1);
            //obj.rotate(Vector3.Y,45);
            getCurrentScene().addChild(obj);//add obj to screen
            //x axis
            Stack<Vector3> points = new Stack<>();
            points.push(new Vector3(-10,0,0));
            points.push(new Vector3(5,0,0));
            points.push(new Vector3(4.75,0,-0.5));
            points.push(new Vector3(4.75,0,0.5));
            points.push(new Vector3(5,0,0));
             xline = new Line3D(points, 5, Color.RED);
            points.clear();
            //x axis
            //y axis
            points.push(new Vector3(0,-7,0));
            points.push(new Vector3(0,4,0));
            points.push(new Vector3(0,3.75,0.5));
            points.push(new Vector3(0,3.75,-0.5));
            points.push(new Vector3(0,4,0));
             yline = new Line3D(points, 5, Color.GREEN);
            points.clear();
            //y axis
            //z axis
            points.push(new Vector3(0,0,-5));
            points.push(new Vector3(0,0,5));
            points.push(new Vector3(0.5,0,4.75));
            points.push(new Vector3(-0.5,0,4.75));
            points.push(new Vector3(0,0,5));
             zline = new Line3D(points, 5, Color.BLUE);
            //z axis
            //imp
            Material material = new Material();
            xline.setMaterial(material);
            yline.setMaterial(material);
            zline.setMaterial(material);
            //imp
            //add axises to screen
            getCurrentScene().addChild(xline);
            getCurrentScene().addChild(yline);
            getCurrentScene().addChild(zline);


        } catch (ParsingException e) {
            e.printStackTrace();
        }
        getCurrentCamera().setPosition(10,6,10);//set position of camera
        getCurrentCamera().setLookAt(obj.getPosition());

        //ability of zoom and move obj with hand
        ArcballCamera arcball = new ArcballCamera(mContext, ((Activity)mContext).findViewById(R.id.rajawali));
        arcball.setTarget(obj); //your 3D Object

        arcball.setPosition(10,6,10); //optional

        getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), arcball);

    }

    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
    }
    public Object3D getObj()
    {
        return obj;
    }

    public void Rotate(float[] deltaRotationVector  ){//rotate obj based on x,y,z axis
        //Toast.makeText(this.context,"screen is "+mSceneInitialized,Toast.LENGTH_SHORT).show();

        if(mSceneInitialized){
            Matrix4 m4 = new Matrix4();
            Vector3 vx = new Vector3(deltaRotationVector[0],deltaRotationVector[3],
                    deltaRotationVector[6]);
            Vector3 vy = new Vector3(deltaRotationVector[1],deltaRotationVector[4],
                    deltaRotationVector[7]);
            Vector3 vz = new Vector3(deltaRotationVector[2],deltaRotationVector[5],
                    deltaRotationVector[8]);
            m4.setAll(vx,vy,vz,Vector3.ZERO);
            getObj().rotate(m4);
            //getCurrentCamera().rotate(m4);
            //getCurrentScene().getCamera().rotate(m4);


        }

        //setxyz(x,y,z);
        //rot = true;
    }
    public Vector3 getline(int axes_number)
    {
        if (axes_number == 0)
            return xvect ;
        else if(axes_number == 1)
            return yvect;
        else
            return zvect;
    }
    public void rotate_degree(double x,double y,double z)
    {
        getObj().rotate(getline(0),x);
        getObj().rotate(getline(1),y);
        getObj().rotate(getline(2),z);

    }
    public void Rotate_begin(Quaternion quat){


        if(mSceneInitialized) {

            getObj().setOrientation(quat);
        }
    }
    public void onTouchEvent(MotionEvent event){
    }

    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j){
    }
}
