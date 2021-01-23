package model;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;


import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

public class Data2 {
    public static boolean writeToJSON(ArrayList<Kategorija> list,String path){
        try {
            Writer writer=new FileWriter(path);
            Gson gson = new Gson();
            gson.toJson(list,writer);
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<Kategorija> readFromJson(String path){
        if(!new File(path).exists()){
            return new ArrayList<>();
        }
        try {
            JsonReader reader=new JsonReader(new FileReader(path));
            Gson gson = new Gson();
            return gson.fromJson(reader,new TypeToken<ArrayList<Kategorija>>(){}.getType());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static String readCss(){
        try {
            return new String(Files.readAllBytes(new File("public/css/style.css").toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String readJS(){
        try {

            return new String(Files.readAllBytes(new File("public/js/jquery-3.3.1.min.js").toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
