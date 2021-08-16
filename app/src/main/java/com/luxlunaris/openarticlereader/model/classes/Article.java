package com.luxlunaris.openarticlereader.model.classes;


import java.io.File;

public class Article extends ReadOnlyPage {


    EditablePage notesPage;

    public Article(String pathname) {
        super(pathname);
        notesPage = new EditablePage(pathname+File.separator+"notes");
    }

    @Override
    public void create(){
        super.create();
        notesPage.create();
    }

    public EditablePage getNotesPage(){
        return notesPage;
    }





}
