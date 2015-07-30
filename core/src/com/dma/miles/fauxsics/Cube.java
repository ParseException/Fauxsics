package com.dma.miles.fauxsics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.util.Random;

import static java.lang.Math.abs;
import static java.lang.Math.floorMod;

public class Cube {

    ModelInstance instace;

    Cube(float length, float height, float width,
                                   float x, float y, float z,
                                   float scale,
                                   float yaw, float pitch, float roll) {

        Model model;

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox( length, height, width,
                new Material(ColorAttribute.createDiffuse(Color.WHITE)),
                Usage.Position | Usage.Normal);
        instace = new ModelInstance(model);
        instace.transform.scl(scale);
        instace.transform.setFromEulerAngles(yaw, pitch, roll);
        instace.transform.setTranslation(x, y, z);
        instace.calculateTransforms();
//        instace.transform.set(new Vector3(x, y, z), new Quaternion().setEulerAngles(yaw, pitch, roll), new Vector3(scale, scale, scale));
//        instace.calculateTransforms();
    }

    public ModelInstance getInstance() {
        return instace;
    }
}
