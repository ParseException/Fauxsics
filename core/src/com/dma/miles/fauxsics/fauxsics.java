package com.dma.miles.fauxsics;

//---

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import java.util.ArrayList;
//---

public class fauxsics extends InputAdapter implements ApplicationListener {

    public Environment environment;
    public PerspectiveCamera cam;
    public CameraInputController camController;
    public ModelBatch modelBatch;
    public ArrayList<ModelInstance> instances;
    public ArrayList<ModelInstance> collide;

    private int selected = -1, selecting = -1;
    private Material selectionMaterial;
    private Material originalMaterial;

    @Override
    public void create() {

        instances = new ArrayList<ModelInstance>();
        collide = new ArrayList<ModelInstance>();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        modelBatch = new ModelBatch();

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, 10f, 10f);
        cam.lookAt(0, 0, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        Cube cube1 = new Cube( 1, 1, 1, 0, 0, 0, 0, 0, 0, 0);
        instances.add(cube1.getInstance());
        collide.add( cube1.getInstance() );

        Cube cube2 = new Cube( 1, 2, 1, 2, 0, 0, 0, 0, 45, 0);
        instances.add(cube2.getInstance());
        collide.add( cube2.getInstance() );

        Cube Floor = new Cube( 6, .5f, 6, 0, 0, 0, 0, 0, 0, 0);
        Gdx.input.setInputProcessor(camController);
        instances.add(Floor.getInstance());
        collide.add( Floor.getInstance() );

        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
    }

    @Override
    public void render() {

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        for (ModelInstance inst : instances) {

            Quaternion quat = inst.transform.getRotation(new Quaternion());

            Vector3 pos = inst.transform.getTranslation( new Vector3() );

            //inst.transform.set( quat.setEulerAngles( quat.getYaw() + 1, quat.getPitch(), quat.getRoll() ) );
            //inst.transform.setTranslation(pos);

            System.out.println("Yaw: " + quat.getYaw() + " Pitch: " + quat.getPitch() + " Roll: " + quat.getRoll());

            inst.calculateTransforms();
        }

        camController.update();

        modelBatch.begin(cam);

        int visibleCount = 0;
        for (final ModelInstance instance : instances) {
            if (isVisible(cam, instance)) {
                modelBatch.render(instance, environment);
                visibleCount++;
            }
        }

        modelBatch.render(instances, environment);

        modelBatch.end();
    }

    private Vector3 position = new Vector3();
    protected boolean isVisible(final Camera cam, final ModelInstance instance) {
        instance.transform.getTranslation(position);
        return cam.frustum.pointInFrustum(position);
    }

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        selecting = getObject(screenX, screenY);
        return selecting >= 0;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (selecting < 0)
            return false;
        if (selected == selecting) {
            Ray ray = cam.getPickRay(screenX, screenY);
            final float distance = -ray.origin.y / ray.direction.y;
            position.set(ray.direction).scl(distance).add(ray.origin);
            instances.get(selected).transform.setTranslation(position);
        }
        return true;
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if (selecting >= 0) {
            if (selecting == getObject(screenX, screenY))
                setSelected(selecting);
            selecting = -1;
            return true;
        }
        return false;
    }

    public void setSelected (int value) {
        if (selected == value) return;
        if (selected >= 0) {
            Material mat = instances.get(selected).materials.get(0);
            mat.clear();
            mat.set(originalMaterial);
        }
        selected = value;
        if (selected >= 0) {
            Material mat = instances.get(selected).materials.get(0);
            originalMaterial.clear();
            originalMaterial.set(mat);
            mat.clear();
            mat.set(selectionMaterial);
        }
    }

    public int getObject (int screenX, int screenY) {
        Ray ray = cam.getPickRay(screenX, screenY);

        int result = -1;
        float distance = -1;

        for (int i = 0; i < instances.size(); ++i) {
            final ModelInstance instance = instances.get(i);

            instance.transform.getTranslation(position);

            final float len = ray.direction.dot(position.x-ray.origin.x, position.y-ray.origin.y, position.z-ray.origin.z);
            if (len < 0f)
                continue;

            float dist2 = position.dst2(ray.origin.x+ray.direction.x*len, ray.origin.y+ray.direction.y*len, ray.origin.z+ray.direction.z*len);
            if (distance >= 0f && dist2 > distance)
                continue;

            if (dist2 <= 4) {
                result = i;
                distance = dist2;
            }
        }
        return result;
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
    }

    @Override
    public void resize(int width, int height) {

        cam.viewportHeight = height;
        cam.viewportWidth = width;

        cam.update();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}