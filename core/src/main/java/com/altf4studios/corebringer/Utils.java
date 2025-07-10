package com.altf4studios.corebringer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Utils {

    //Looks for any file relative to your jar file
    public static FileHandle getClasspath(String filepath){
        return Gdx.files.classpath(filepath);
    }

    //getInternalPath can be used to retrieve asset folders, can be used for laoding objects from working directory
    public static FileHandle getInternalPath(String filepath){
        return Gdx.files.internal(filepath);
    }

    //Can be used for reading and writting
    public static FileHandle getLocalPath(String filepath){
        return Gdx.files.local(filepath);
    }

    //External: Best for storing large files as well as writing and reading them
    //When you do store files in your home's working directory that it is open to the user to edit these files
    public static FileHandle getExternalPath(String filepath){
        return Gdx.files.external(filepath);
    }
}
