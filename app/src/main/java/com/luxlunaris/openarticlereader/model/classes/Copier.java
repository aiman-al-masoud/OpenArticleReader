package com.luxlunaris.openarticlereader.model.classes;

import com.luxlunaris.openarticlereader.model.interfaces.Page;
import com.luxlunaris.openarticlereader.model.services.FileIO;

import java.io.File;

/**
 * Combines old pages into a single new one.
 */
public class Copier {

    public Copier() {

    }

    /**
     * Copies the contents of a page unto a new one.
     * @param original
     * @param blankPage
     */
    public void copy(Page original, Page blankPage) {

        //get the original's html source
        String source = original.getSource();

        //migrate images from old pages to blank page.

        //this is the path in the html that will replace the older ones.
        String blankPagesPath = ((File) blankPage).getPath();
        //due to slight differences across devices in initial part of path, just replace the part from "com.luxlunaris..." onwards
        int start = blankPagesPath.indexOf("com");
        blankPagesPath = blankPagesPath.substring(start);


        //these are the paths to be replaced
        String oldPathToBeReplaced = ((File) original).getPath();
        //due to slight differences across devices in initial part of path, just replace the part from "com.luxlunaris..." onwards
        start = oldPathToBeReplaced.indexOf("com");
        oldPathToBeReplaced = oldPathToBeReplaced.substring(start);

        //replace all instances of old path w/ path of new blank page
        source = source.replaceAll(oldPathToBeReplaced, blankPagesPath);

        //copy the actual image files to the new blank page's directory
        File[] imageFiles = original.getImageDir().listFiles();
        FileIO.copyFilesToDirectory(imageFiles, blankPage.getImageDir().getPath());

        Article page = (Article) blankPage;
        FileIO.write(page.textFile.getPath(), source);

        FileIO.copyDirectory(((Article) original).notesPage.getPath(), page.notesPage.getPath());
    }



}
