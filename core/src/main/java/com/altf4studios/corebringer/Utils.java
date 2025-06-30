package com.altf4studios.corebringer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Utils {

    //Looks for the file related to the assets and anything related to the jar file
    public static FileHandle getInternalPath(String filepath){
        return Gdx.files.internal(filepath);
    }

    //Local
    public static FileHandle getLocalPath(String filepath){
        return Gdx.files.local(filepath);
    }

    //External: Best for storing large files
    public static FileHandle getExternalPath(String filepath){
        return Gdx.files.external(filepath);
    }
}
