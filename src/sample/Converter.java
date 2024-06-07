package sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Converter {
    static int dotCount = 0;
    static int voxelCount = 0;
    static int dotInVoxelsCount = 0;
    static List<List<Double>> dots = new ArrayList<>();
    static List<List<Integer>> voxels= new ArrayList<>();
    static List<Integer> cellTypes = new ArrayList<>();

    public static List<List<Double>> delta = new ArrayList<>();
    static List<List<Double>> velocity = new ArrayList<>();
    static List<Double> def = new ArrayList<>();

    public static int getDotCount() {
        return dotCount;
    }

    public static int getVoxelCount() {
        return voxelCount;
    }

    public static List<List<Double>> getDots() {
        return dots;
    }

    public static List<List<Integer>> getVoxels() {
        return voxels;
    }


    public static void convert(File file, File fileData){
        dots.clear();
        voxels.clear();
        if(file.getName().contains(".k")){
            convertKFile(file);
        }else {
            convertCustomFile(file, fileData);
        }

    }

    private static void convertKFile(File file){
        try(BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)){
            dotCount = 0;
            voxelCount = 0;
            while (br.ready()){
                String currentString = br.readLine();
                if(currentString.contains("*ELEMENT_SOLID")){
                    currentString = br.readLine();
                    while(currentString.contains("id") || currentString.contains("ID") || currentString.trim().split("\\s+").length < 8){
                        currentString = br.readLine();
                    }
                    while(!currentString.contains("*") && !currentString.contains("$")){
                        voxels.add(Arrays.stream(currentString.trim().split("\\s+")).map(Integer::parseInt).map(v -> v-1).skip(2).collect(Collectors.toList()));
                        voxelCount++;
                        currentString = br.readLine();
                    }
                }
                if (currentString.contains("*NODE")){
                    currentString = br.readLine();
                    while(currentString.contains("id") || currentString.contains("ID")){
                        currentString = br.readLine();
                    }
                    while(!currentString.contains("*") && !currentString.contains("$")){
                        for (int i = 1; i < currentString.length(); i++) {
                            if(currentString.charAt(i) == '-' && currentString.charAt(i-1) != 'E' && currentString.charAt(i-1) != 'e'){
                                currentString = new StringBuilder(currentString).insert(i, " ").toString();
                                i++;
                            }
                        }
                        dots.add(Arrays.stream(currentString.trim().split("\\s+")).map(Double::parseDouble).skip(1).collect(Collectors.toList()));
                        while(dots.get(dotCount).size() < 3){
                            dots.get(dotCount).add(0.0);
                        }
                        dotCount++;
                        currentString = br.readLine();
                    }
                }
            }

        }catch (Exception ex){
            System.out.println("Error: " + ex.getMessage());
        }
        writeInFile(file);
    }

    private static void convertCustomFile(File file, File fileData){
        dotCount = 0;
        voxelCount = 0;
        dotInVoxelsCount = 0;
        try(BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)){
            dotCount = Integer.parseInt(br.readLine().replace(" ", ""));
            voxelCount = Integer.parseInt(br.readLine());
            for (int i = 0; i < dotCount; i++) {
                dots.add(Arrays.stream(br.readLine().trim().split("\\s+")).map(Double::parseDouble).collect(Collectors.toList()));
            }
            for (int i = 0; i < voxelCount; i++) {
                voxels.add(Arrays.stream(br.readLine().trim().split("\\s+")).map(Integer::parseInt).map(v -> v-1).collect(Collectors.toList()));
            }
        }catch (Exception ex){
            System.out.println("Error: " + ex.getMessage());
        }


        for (int i = 0; i < voxels.size(); i++) {
            List<Integer> curr = voxels.get(i);
            for (int j = 0; j < curr.size(); j++) {
                if (j == curr.size() - 1 || curr.get(j) == curr.get(j + 1)) {
                    cellTypes.add(j + 1);
                    break;
                }
            }
            for (int j = curr.size() - 1; j > 0; j--) {
                if (curr.indexOf(curr.get(j)) != j) {
                    curr.remove(j);
                }
            }
            dotInVoxelsCount += curr.size() + 1;
        }


        if (fileData != null) {
            convertFields(fileData);
        }

        writeInFile(file);

        if (fileData != null) {
            writeInFileWithDelta(file);
        }
    }

    private static void convertFields(File file){
        //System.out.println(file.length());
        try(BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)){
            System.out.println(br.readLine());
            while (!br.readLine().equals("перемещения")){}
            String currentLine = br.readLine();
            while (!currentLine.contains("скорости")) {
                delta.add(Arrays.stream(currentLine.trim().split("\\s+")).map(Double::parseDouble).collect(Collectors.toList()));
                currentLine = br.readLine();
            }
            currentLine = br.readLine();
            while (!currentLine.contains("пластические деформации")) {
                velocity.add(Arrays.stream(currentLine.trim().split("\\s+")).map(Double::parseDouble).collect(Collectors.toList()));
                currentLine = br.readLine();
            }
            while (currentLine != null) {
                currentLine = br.readLine();
                int index = 0;
                List<Double> strings = new ArrayList<>();
                while (index < currentLine.length()) {
                    strings.add(Double.parseDouble(currentLine.substring(index, Math.min(index + 8,currentLine.length()))));
                    index += 8;
                }
                double result = 0;
                for (int i = 0; i < 3; i++) {
                    result += Math.pow(strings.get(i), 2);
                }
                for (int i = 3; i < 6; i++) {
                    result += 2*Math.pow(strings.get(i), 2);
                }
                result *= (2.0 / 3.0);
                result = Math.sqrt(result);
                def.add(result);
            }
            //
        }catch (Exception ex){
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void writeInFile(File file){
        try(FileWriter fileWriter = new FileWriter(file.getParentFile().getAbsolutePath()+"/"+file.getName().split("\\.")[0]+".vtk", StandardCharsets.UTF_8)){
            fileWriter.write("# vtk DataFile Version 1.0 \n");
            fileWriter.write("Unstructured Grid Example \n");
            fileWriter.write("ASCII \n\n");
            fileWriter.write("DATASET UNSTRUCTURED_GRID \n");
            fileWriter.write("POINTS "+dotCount+" float \n");
            for (int i = 0; i < dotCount; i++) {
                for (int j = 0; j < 3; j++) {
                    fileWriter.write(dots.get(i).get(j).toString() + " ");
                }
                fileWriter.write("\n");
            }
            fileWriter.write("\nCELLS "+voxelCount+" "+(dotInVoxelsCount) + "\n");
            for (int i = 0; i < voxelCount; i++) {
                fileWriter.write((voxels.get(i).size()) + " "); //voxels.get(i).size() == 7 ? 8 :
                for (int j = 0; j < voxels.get(i).size(); j++) {
                    fileWriter.write(voxels.get(i).get(j).toString() + " ");
                }
                if (voxels.get(i).size() == 7) {
                   // fileWriter.write(voxels.get(i).get(0).toString());
                }
                fileWriter.write("\n");
            }

            fileWriter.write("\nCELL_TYPES " + voxelCount + "\n");
            for (int i = 0; i < voxelCount; i++) {
                if (voxels.get(i).size() == 8) {
                    fileWriter.write("12\n");
                } else if (voxels.get(i).size() == 6) {
                    fileWriter.write("13\n");  //13
                } else if (voxels.get(i).size() == 5) {
                    fileWriter.write("14\n");  //14
                } else if (voxels.get(i).size() == 4) {
                    fileWriter.write("10\n");  //10
                } else if (voxels.get(i).size() == 7) {
                    fileWriter.write("22\n");
                }
            }

            if (delta.size() > 0) {
                fileWriter.write("\n");
                fileWriter.write("POINT_DATA " + Integer.toString(dotCount) + "\n");
                fileWriter.write("VECTORS Movements float \n");
                for (int i = 0; i < dotCount; i++) {
                    for (int j = 0; j < 3; j++) {
                        fileWriter.write(String.format("%.5f", delta.get(i).get(j)).replace(',', '.') + " ");
                    }
                    fileWriter.write("\n");
                }

                fileWriter.write("VECTORS Velocity float \n");
                for (int i = 0; i < dotCount; i++) {
                    for (int j = 0; j < 3; j++) {
                        fileWriter.write(String.format("%.5f", velocity.get(i).get(j)).replace(',', '.') + " ");
                    }
                    fileWriter.write("\n");
                }

                fileWriter.write("\n");
                fileWriter.write("CELL_DATA " + Integer.toString(voxelCount) + "\n");
                fileWriter.write("SCALARS Deformations float 1\n");
                fileWriter.write("LOOKUP_TABLE default \n");
                for (int i = 0; i < voxelCount; i++) {
                    fileWriter.write(String.format("%.5f", def.get(i)).replace(',', '.') + " ");
                    fileWriter.write("\n");
                }
            }

        }catch (IOException ioException) {
            ioException.printStackTrace();
        }
        //dots.clear();
        //voxels.clear();
    }

    private static void writeInFileWithDelta(File file){
        try(FileWriter fileWriter = new FileWriter(file.getParentFile().getAbsolutePath()+"/"+file.getName().split("\\.")[0]+"Delta.vtk", StandardCharsets.UTF_8)){
            fileWriter.write("# vtk DataFile Version 1.0 \n");
            fileWriter.write("Unstructured Grid Example \n");
            fileWriter.write("ASCII \n\n");
            fileWriter.write("DATASET UNSTRUCTURED_GRID \n");
            fileWriter.write("POINTS "+dotCount+" float \n");
            for (int i = 0; i < dotCount; i++) {
                for (int j = 0; j < 3; j++) {
                    fileWriter.write(Double.toString(dots.get(i).get(j) + delta.get(i).get(j)) + " ");
                }
                fileWriter.write("\n");
            }
            fileWriter.write("\nCELLS "+voxelCount+" "+(dotInVoxelsCount) + "\n");
            for (int i = 0; i < voxelCount; i++) {
                fileWriter.write(voxels.get(i).size() + " ");
                for (int j = 0; j < voxels.get(i).size(); j++) {
                    fileWriter.write(voxels.get(i).get(j).toString() + " ");
                }
                fileWriter.write("\n");
            }

            fileWriter.write("\nCELL_TYPES "+voxelCount + "\n");
            for (int i = 0; i < voxelCount; i++) {
                fileWriter.write("12\n");
            }

            if (delta.size() > 0) {
                fileWriter.write("\n");
                fileWriter.write("POINT_DATA " + Integer.toString(dotCount) + "\n");
                fileWriter.write("VECTORS Movements float \n");
                for (int i = 0; i < dotCount; i++) {
                    for (int j = 0; j < 3; j++) {
                        fileWriter.write(String.format("%.5f", delta.get(i).get(j)).replace(',', '.') + " ");
                    }
                    fileWriter.write("\n");
                }

                fileWriter.write("VECTORS Velocity float \n");
                for (int i = 0; i < dotCount; i++) {
                    for (int j = 0; j < 3; j++) {
                        fileWriter.write(String.format("%.5f", velocity.get(i).get(j)).replace(',', '.') + " ");
                    }
                    fileWriter.write("\n");
                }

                fileWriter.write("\n");
                fileWriter.write("CELL_DATA " + Integer.toString(voxelCount) + "\n");
                fileWriter.write("SCALARS Deformations float 1\n");
                fileWriter.write("LOOKUP_TABLE default \n");
                for (int i = 0; i < voxelCount; i++) {
                    fileWriter.write(String.format("%.5f", def.get(i)).replace(',', '.') + " ");
                    fileWriter.write("\n");
                }
            }

        }catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }


}
