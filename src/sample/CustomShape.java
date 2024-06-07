package sample;

import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableFloatArray;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CustomShape extends MeshView {

    public CustomShape(List<List<Double>> dots, List<List<Integer>> voxels) {

        setMesh(createCube(dots, voxels));
        PhongMaterial material = new PhongMaterial();
        //Diffuse Color
        material.setDiffuseColor(Color.LIGHTSLATEGRAY);
        //material.setSpecularPower(4);
        //Specular Color
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(60);
        setMaterial(material);

    }

    private Mesh createCube(List<List<Double>> dots, List<List<Integer>> voxels) {
        TriangleMesh m = new TriangleMesh();

        System.out.println(dots);

        // POINTS
//        for (int i = 0; i < 5; i++) {
//            m.getPoints().addAll(
//                    200*i + 0f, 0f, 0f,
//                    200*i + 0f, 0f, 200f,
//                    200*i + 0f, 200f, 200f,
//                    200*i + 0f, 200f, 0f,
//                    200*i + 200f, 0f, 0f,
//                    200*i + 200f, 0f, 200f,
//                    200*i + 200f, 200f, 200f,
//                    200*i + 200f, 200f, 0f
//            );
//        }
        for (int i = 0; i < dots.size(); i++) {
            m.getPoints().addAll((Float.parseFloat(String.valueOf(dots.get(i).get(0))))*100,
                    (Float.parseFloat(String.valueOf(dots.get(i).get(1))))*100,
                    (Float.parseFloat(String.valueOf(dots.get(i).get(2))))*100);
        }
        //m.getPoints().addAll(dots.toArray());

//

        // TEXTURES
        m.getTexCoords().addAll(1, 1, // idx t0
                1, 0, // idx t1
                0, 1, // idx t2
                0, 0);





        // FACES
        for (int i = 0; i < voxels.size(); i++) {
            m.getFaces().addAll(
                    voxels.get(i).get(3), 0, voxels.get(i).get(2), 1, voxels.get(i).get(0), 2,
                    voxels.get(i).get(2), 2, voxels.get(i).get(1), 3, voxels.get(i).get(0), 1,
                    voxels.get(i).get(6), 2, voxels.get(i).get(7), 1, voxels.get(i).get(4), 0,
                    voxels.get(i).get(4), 2, voxels.get(i).get(5), 3, voxels.get(i).get(6), 1,

                    voxels.get(i).get(4), 2, voxels.get(i).get(7), 1, voxels.get(i).get(0), 0,
                    voxels.get(i).get(7), 2, voxels.get(i).get(3), 3, voxels.get(i).get(0), 1,
                    voxels.get(i).get(5), 2, voxels.get(i).get(1), 1, voxels.get(i).get(2), 0,
                    voxels.get(i).get(6), 2, voxels.get(i).get(5), 3, voxels.get(i).get(2), 1,

                    voxels.get(i).get(5), 2, voxels.get(i).get(4), 1, voxels.get(i).get(0), 0,
                    voxels.get(i).get(1), 2, voxels.get(i).get(5), 3, voxels.get(i).get(0), 1,
                    voxels.get(i).get(3), 2, voxels.get(i).get(7), 1, voxels.get(i).get(6), 0,
                    voxels.get(i).get(3), 2, voxels.get(i).get(6), 3, voxels.get(i).get(2), 1
            );
            int[] var8 = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            m.getFaceSmoothingGroups().addAll(var8);
        }



        /*
        * voxels.get(i).get(0), 0, voxels.get(i).get(1), 0, voxels.get(i).get(2), 0,
                    voxels.get(i).get(2), 1, voxels.get(i).get(3), 1, voxels.get(i).get(0), 1,
                    voxels.get(i).get(4), 0, voxels.get(i).get(5), 0, voxels.get(i).get(6), 0,
                    voxels.get(i).get(6), 0, voxels.get(i).get(7), 0, voxels.get(i).get(4), 0,
                    voxels.get(i).get(4), 0, voxels.get(i).get(5), 0, voxels.get(i).get(1), 1,
                    voxels.get(i).get(1), 0, voxels.get(i).get(0), 0, voxels.get(i).get(4), 0,
                    voxels.get(i).get(4), 1, voxels.get(i).get(0), 0, voxels.get(i).get(7), 0,
                    voxels.get(i).get(7), 0, voxels.get(i).get(0), 0, voxels.get(i).get(3), 0,
                    voxels.get(i).get(3), 0, voxels.get(i).get(7), 0, voxels.get(i).get(6), 0,
                    voxels.get(i).get(3), 1, voxels.get(i).get(6), 0, voxels.get(i).get(2), 0,
                    voxels.get(i).get(2), 0, voxels.get(i).get(1), 0, voxels.get(i).get(5), 0,
                    voxels.get(i).get(2), 1, voxels.get(i).get(5), 0, voxels.get(i).get(6), 0*/

        return m;

    }

}
