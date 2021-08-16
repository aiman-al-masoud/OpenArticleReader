package com.luxlunaris.openarticlereader.model.classes;

import com.luxlunaris.openarticlereader.control.interfaces.PageListener;
import com.luxlunaris.openarticlereader.model.services.FileIO;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ReadOnlyPage extends AbstractPage{

    transient Document doc;
    transient ArrayList<Connection.Response> images;

    public ReadOnlyPage(String pathname){
        super(pathname);
    }

    public void initContent(WebsiteData data){
        doc = data.doc;
        images = data.images;
    }


    @Override
    public void create() {
        super.create();

        if(doc==null || images==null){
            setEditable(false);
            return;
        }

        String content = doc.text();
        String buffer = "";
        for(String paragraph : content.split("\\. ")){
            buffer += "<p>"+paragraph+".</p>";
        }
        content = buffer;
        content = "<b>" + doc.title()+"</b>" +"<p>\n\n</p>"+ content ;
        setSource(content);

        for (Connection.Response img : images) {
            addImage(img);
        }

        setEditable(false);
    }

    protected void addImage(Connection.Response response){

        try{
            String pathname = imageDir.getPath() + File.separator + System.currentTimeMillis();
            FileOutputStream out = new FileOutputStream(new File(pathname));
            out.write(response.bodyAsBytes());
            out.flush();
            out.close();
            String imageTag = generateImgTag(pathname);
            String text = getSource();
            text+=imageTag;
            setSource(text);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    protected void setSource(String text){
        FileIO.write(textFile.getPath(), text);
    }

    @Override
    public File getImageDir() {
        return imageDir;
    }






}
