package sample.formatUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sample.sharpImitation.BinaryReaderJ;

public class bd3Utils {

    private static File setkaFile;
    private static File tmpFile;

    private static int subdomainCount;
    private static int timeCount;
    private static int funcCount;
    private static float interval;
    private static List<String> funcNames = new ArrayList<>();
    private static String[] mass; //основной массив строк
    private static String[] pmass; //массив значений функции
    private static int cofms; //кол-во заполненных строк mass
    private static int[][] countmass; //массив размеров блоков
    private static int cofcms = 0; //кол-во заполненных строк countmass

    public static int getSubdomainCount() {
        return subdomainCount;
    }

    public static int getTimeCount() {
        return timeCount;
    }

    public static float getInterval() {
        return interval;
    }

    public static boolean setSetkaFile(File file) {
        try {
            setkaFile = file;
            analysBin();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public static List<String> getFuncNames() {
        return funcNames;
    }

    public static String getDescription() {
        StringBuilder description = new StringBuilder("Формат: ВК `Динамика 3` (.bd3)\n");
        description.append("Количество отсчетов по времени: " + timeCount + "\n");
        description.append("Количество подобластей: " + subdomainCount + "\n");
        description.append("Количество функций: " + funcCount + "\n");
        description.append("Функции:\n");
        for (int i = 0; i < funcCount; i++) {
            description.append(funcNames.get(i) + "; ");
        }
        return description.toString();
    }

    private static void analysBin() {
        BinaryReaderJ read = new BinaryReaderJ(setkaFile);
        int[] buffInt = new int[2];

        // Считывание параметров сетки
        buffInt[0] = read.readInt32();
        System.out.println(buffInt[0]);

        buffInt[1] = read.readInt32();
        System.out.println(buffInt[1]);

        // Считывание количества отсчётов времени и подобластей
        timeCount = read.readInt32();
        System.out.println(timeCount);
        subdomainCount = read.readInt32();
        System.out.println(subdomainCount);

        buffInt[0] = read.readInt32();
        System.out.println(buffInt[0]);
        buffInt[1] = read.readInt32();
        System.out.println(buffInt[1]);

        funcCount = read.readInt32();

        String[] functionIName = new String[funcCount];
        // Считывание имен функций
        funcNames = new ArrayList<>();
        for (int j = 0; j < funcCount; j++) {
            functionIName[j] = j + " " + String.valueOf(read.readChars(4));
            funcNames.add(functionIName[j]);
        }

        checkTime();
    }

    private static void binRead(int TTP, int[] submass, String funcName) {
        String graphPath = setkaFile.getParentFile().getAbsolutePath() + "/graphPath.txt"; // путь к графическому файлу
        BinaryReaderJ read = new BinaryReaderJ(setkaFile);
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(graphPath))) {

            long time;
            long[] buffInt = new long[4];
            char[] buffStr = new char[5];
            float[] buffFloat = new float[1];

            // Считывание размеров сетки
            buffInt[0] = read.readInt32();
            buffInt[1] = read.readInt32();

            // Считывание количества отсчётов времени и подобластей
            time = read.readInt32();
            int subdomain = read.readInt32();

            long[] facet = new long[subdomain]; // количество граней подобласти
            long[] block = new long[subdomain]; // количество блоков подобласти
            long[] nodeFuncDomain = new long[subdomain]; // количество узловых функций для подобласти
            long[] nodeFuncBlock = new long[subdomain]; // количество узловых функций для блока
            long[][] facetNode = new long[subdomain][]; // количество узлов j-ой грани в i-й подобласти
            long[][] blockNode = new long[subdomain][]; // количество узлов в j-блоке в i-й подобласти
            String[][] functionName = new String[subdomain][]; // названия функций

            // Считывание данных о i-й подобласти
            for (int i = 0; i < subdomain; i++) {
                facet[i] = read.readInt32();
                block[i] = read.readInt32();
                nodeFuncDomain[i] = read.readInt32();
                String[] functionIName = new String[(int) nodeFuncDomain[i]];

                // Считываем имя j-й функции в узлах i-й подобласти
                for (int j = 0; j < nodeFuncDomain[i]; j++) {
                    functionIName[j] = new String(read.readChars(4), 0, 4);
                }
                functionName[i] = functionIName;

                // Считывание числа функций в элементе грани
                buffInt[0] = read.readInt32();
                // Считывание числа функций в узлах блока
                nodeFuncBlock[i] = read.readInt32();

                // Считывание имен функций в узлах блока
                for (int j = 0; j < nodeFuncBlock[i]; j++) {
                    read.readChars(4);
                }

                // Считывание числа функции в элементе блока
                buffInt[0] = read.readInt32();

                // Считывание параметров фрагментации граней
                long[] facetINode = new long[2 * (int) facet[i]];
                for (int j = 0; j < facet[i]; j++) {
                    facetINode[2 * j] = read.readInt32();
                    facetINode[2 * j + 1] = read.readInt32();
                }
                facetNode[i] = facetINode;

                // Считывание параметров фрагментации блока
                long[] blockINode = new long[(int) block[i]];
                for (int j = 0; j < block[i]; j++) {
                    buffInt[0] = read.readInt32();
                    buffInt[1] = read.readInt32();
                    buffInt[2] = read.readInt32();
                    blockINode[j] = buffInt[0] * buffInt[1] * buffInt[2];
                }
                blockNode[i] = blockINode;
            }

            // Считывание данных для печати времени
            int printCountTime = 1;
            int[] printTime = new int[printCountTime];
            printTime[0] = TTP;

            // Считывание функции для печати
            int printCount = 1;
            int[] printIndex = new int[printCount];
            String variable = funcName;
            if (variable != null && !"".equals(variable) && !"...".equals(variable)) {
                printIndex = new int[printCount];
                printIndex[0] = Integer.parseInt(Character.toString(variable.charAt(0)));
            } else {
                printIndex[0] = 0;
            }


            int nameIndex = 0, timeIndex = 0;
            float viewTime;
            int u = -1;

            while (read.getAvaible() > 0) {
                u++;
                // Считывание информации для u-го времени
                viewTime = read.readFloat();
                System.out.println("viewTime: " + viewTime);
                System.out.println("u:" + u);

                // Просмотр подобласти
                for (int j = 0; j < subdomain; j++) {
                    float[][] node = new float[3 + printCount][];

                    // Просмотр грани
                    for (int k = 0; k < facet[j]; k++) {
                        int finalJ = j;
                        if (timeIndex < printCountTime && u == printTime[timeIndex] && Arrays.stream(submass).anyMatch(x -> x == finalJ)) {
                            System.out.println("Запись в файл подобласти: " + j + "; Разрешенные подобласти: " + Arrays.toString(submass));
                            writer.write(facetNode[j][2 * k + 1] + " " + (facetNode[j][2 * k] + " " + j) + " " + viewTime + System.lineSeparator());
                        }

                        // Считывание координаты j-х узлов грани
                        for (int m = 0; m < 3; m++) {
                            float[] nodeLine = new float[(int) (facetNode[j][2 * k] * facetNode[j][2 * k + 1])];
                            for (int n = 0; n < facetNode[j][2 * k] * facetNode[j][2 * k + 1]; n++)
                                nodeLine[n] = read.readFloat();
                            node[m] = nodeLine;
                        }

                        nameIndex = 0;

                        // Считывание значения функций в j-х узлах грани
                        for (int m = 0; m < nodeFuncDomain[j]; m++) {
                            float[] nodeLine = new float[(int) (facetNode[j][2 * k] * facetNode[j][2 * k + 1])];
                            for (int n = 0; n < facetNode[j][2 * k] * facetNode[j][2 * k + 1]; n++){
                                nodeLine[n] = read.readFloat();
                            }

                            if (nameIndex < printCount && m == printIndex[nameIndex]) {
                                node[3 + nameIndex] = nodeLine;
                                //System.out.println(m + " " + printIndex[nameIndex] + " " + Arrays.toString(nodeLine));
                                nameIndex++;
                            }
                        }

                        // Печать информации в графический файл
                        if (timeIndex < printCountTime && u == printTime[timeIndex] && Arrays.stream(submass).anyMatch(x -> x == finalJ)) {
                            for (int n = 0; n < facetNode[j][2 * k] * facetNode[j][2 * k + 1]; n++) {
                                for (int m = 0; m < 3 + printCount; m++) {
                                   // System.out.print(node[m][n] + " ");
                                    writer.write(String.format("%.6f ", node[m][n]));
                                }
                                //System.out.println("");
                                writer.write(System.lineSeparator());
                            }
                        }
                    }

                    // Информация о размере блоков
                    for (int k = 0; k < block[j]; k++) {
                        // Считывание координат j узлов блока подобласти
                        for (int n = 0; n < 3 * blockNode[j][k]; n++)
                            buffFloat[0] = read.readFloat();

                        // Считывание значения функций в j узлах блока подобласти
                        for (int n = 0; n < nodeFuncBlock[j] * blockNode[j][k]; n++)
                            buffFloat[0] = read.readFloat();
                    }
                }

                if ((timeIndex < printCountTime) && (u == printTime[timeIndex]))
                    timeIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        tmpFile = new File(Paths.get(graphPath).toUri());
    }

    public static void convert(int subdomainNumber, String funcName, int timeMoment)
    {
        System.out.println(timeMoment + " : " + funcName);
        int k = 0;
        if (subdomainNumber == -1)
            k = subdomainCount - 1;

        int[] submass = new int[k + 1];

        for (int i = 0; i < k + 1; i++) {
            if (subdomainNumber == -1 || subdomainNumber == i) {
                submass[i] = i;
            }
        }

        binRead(timeMoment, submass, funcName);

        try {
            int count = (int)Files.lines(tmpFile.toPath()).count();
            mass = new String[count];
            Arrays.fill(mass, "");
            pmass = new String[count];
            Arrays.fill(pmass, "");
            countmass = new int[count / 3][3];
            Read();
            writeVTK(setkaFile.getParentFile().getAbsolutePath()+"/"+setkaFile.getName().split("\\.")[0]+".vtk");
            System.out.println("end");
        } catch (IOException ioException) {
            System.out.print("Error: ");
            ioException.printStackTrace();
        }

    }

    private static void Read()
    {
        try (BufferedReader br = Files.newBufferedReader(tmpFile.toPath(), StandardCharsets.UTF_8))
        {
            int countblock = 0; //кол-во блоков
            int countspace = 0; //кол-во пробелов
            int i = 0;
            cofcms = 0;
            String tmp = br.readLine();
            while (tmp != null)
            {
                if (tmp.length() < 25)
                {
                    cofcms++;
                    String tmpcount = "";
                    for (int j = 0; j < tmp.length(); j++)
                    {
                        if (tmp.charAt(j) == ' ')
                        {
                            countmass[countblock][countspace] = Integer.valueOf(tmpcount);
                            countspace++;
                            tmpcount = "";
                        }
                        else
                            tmpcount += tmp.charAt(j);
                        if (countspace == 3)
                            break;
                    }
                    countblock++;
                    countspace = 0;
                }
                else
                {
                    for (int j = 0; j < tmp.length(); j++)
                    {
                        if (tmp.charAt(j) == ' ')
                            countspace++;
                        if (countspace < 3)
                            mass[i] += String.valueOf(tmp.charAt(j));
                        else
                            pmass[i] += String.valueOf(tmp.charAt(j));
                    }
                    countspace = 0;
                    i++;
                    cofms++;
                }
                tmp = br.readLine();
            }
        } catch (IOException ioException) {
            System.out.println("Error");
            ioException.printStackTrace();
        }
        System.out.println("End");
    }

    public static void writeVTK(String address) {
        int poc = mass.length - cofcms;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(address, false))) {
            writer.write("# vtk DataFile Version 3.0");
            writer.newLine();
            writer.write("vtk output");
            writer.newLine();
            writer.write("ASCII");
            writer.newLine();
            writer.write("DATASET UNSTRUCTURED_GRID");
            writer.newLine();
            writer.write("POINTS " + poc + " float \n");
            writer.newLine();
            for (int i = 0; i < poc; i++) {
                writer.write(mass[i].replace(',', '.'));
                writer.newLine();
            }
            writer.newLine();

            int nCells = 0; // n_cells - число ячеек
            for (int j = 0; j < cofcms; j++) {
                for (int i = 0; i <= countmass[j][1] * (countmass[j][0] - 1); i++) {
                    if (i % countmass[j][1] != 0) {
                        nCells++;
                    }
                }
            }
            int nList = nCells * 5; // n_list - число чисел
            writer.write("CELLS " + nCells + " " + nList);
            writer.newLine();

            int tmp = 0;
            for (int j = 0; j < cofcms; j++) {
                for (int i = 0; i <= countmass[j][1] * (countmass[j][0] - 1); i++) {
                    if (i % countmass[j][1] != 0) {
                        writer.write("4 " + (tmp + i - 1) + " " + (tmp + countmass[j][1] + i - 1) + " " + (tmp + countmass[j][1] + i) + " " + (tmp + i));
                        writer.newLine();
                    }
                }
                tmp += (countmass[j][0] * countmass[j][1]);
            }
            writer.newLine();

            writer.write("CELL_TYPES " + nCells);
            writer.newLine();
            for (int j = 0; j < nCells; j++) {
                writer.write("9");
                writer.newLine();
            }
            writer.write("POINT_DATA " + poc);
            writer.newLine();
            writer.write("SCALARS ip float 1");
            writer.newLine();
            writer.write("LOOKUP_TABLE ipName");
            writer.newLine();
            for (int j = 0; j < poc; j++) {
                writer.write(pmass[j].replace(',', '.'));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Костыль, ибо нет нормальной информации в файле о времени
    private static void checkTime() {
        BinaryReaderJ read = new BinaryReaderJ(setkaFile);

        long[] buffInt = new long[4];

        // Считывание размеров сетки
        read.readInt32();
        read.readInt32();

        // Считывание количества отсчётов времени и подобластей
        read.readInt32();
        int subdomain = read.readInt32();

        long[] facet = new long[subdomain]; // количество граней подобласти
        long[] block = new long[subdomain]; // количество блоков подобласти
        long[] nodeFuncDomain = new long[subdomain]; // количество узловых функций для подобласти
        long[] nodeFuncBlock = new long[subdomain]; // количество узловых функций для блока
        long[][] facetNode = new long[subdomain][]; // количество узлов j-ой грани в i-й подобласти
        long[][] blockNode = new long[subdomain][]; // количество узлов в j-блоке в i-й подобласти

        // Считывание данных о i-й подобласти
        for (int i = 0; i < subdomain; i++) {
            facet[i] = read.readInt32();
            block[i] = read.readInt32();
            nodeFuncDomain[i] = read.readInt32();

            // Считываем имя j-й функции в узлах i-й подобласти
            for (int j = 0; j < nodeFuncDomain[i]; j++) {
                read.readChars(4);
            };

            // Считывание числа функций в элементе грани
            read.readInt32();
            // Считывание числа функций в узлах блока
            nodeFuncBlock[i] = read.readInt32();

            // Считывание имен функций в узлах блока
            for (int j = 0; j < nodeFuncBlock[i]; j++) {
                read.readChars(4);
            }

            read.readInt32();
            // Считывание параметров фрагментации граней
            long[] facetINode = new long[2 * (int) facet[i]];
            for (int j = 0; j < facet[i]; j++) {
                facetINode[2 * j] = read.readInt32();
                facetINode[2 * j + 1] = read.readInt32();
            }
            facetNode[i] = facetINode;

            // Считывание параметров фрагментации блока
            long[] blockINode = new long[(int) block[i]];
            for (int j = 0; j < block[i]; j++) {
                buffInt[0] = read.readInt32();
                buffInt[1] = read.readInt32();
                buffInt[2] = read.readInt32();
                blockINode[j] = buffInt[0] * buffInt[1] * buffInt[2];
            }
            blockNode[i] = blockINode;
        }

        float viewTime;
        int u = -1;
        try {
            while (read.getAvaible() > 0) {
                u++;
                // Считывание информации для u-го времени
                viewTime = read.readFloat();
                if (interval == 0 && u != 0) {
                    interval = viewTime;
                }

                // Просмотр подобласти
                for (int j = 0; j < subdomain; j++) {

                    // Просмотр грани
                    for (int k = 0; k < facet[j]; k++) {
                        // Считывание координаты j-х узлов грани
                        for (int m = 0; m < 3; m++) {
                            for (int n = 0; n < facetNode[j][2 * k] * facetNode[j][2 * k + 1]; n++)
                                read.readFloat();
                        }

                        // Считывание значения функций в j-х узлах грани
                        for (int m = 0; m < nodeFuncDomain[j]; m++) {
                            for (int n = 0; n < facetNode[j][2 * k] * facetNode[j][2 * k + 1]; n++){
                                read.readFloat();
                            }

                        }

                    }

                    // Информация о размере блоков
                    for (int k = 0; k < block[j]; k++) {
                        // Считывание координат j узлов блока подобласти
                        for (int n = 0; n < 3 * blockNode[j][k]; n++)
                            read.readFloat();

                        // Считывание значения функций в j узлах блока подобласти
                        for (int n = 0; n < nodeFuncBlock[j] * blockNode[j][k]; n++)
                            read.readFloat();
                    }
                }

            }
            timeCount = u;
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }


    }



}
