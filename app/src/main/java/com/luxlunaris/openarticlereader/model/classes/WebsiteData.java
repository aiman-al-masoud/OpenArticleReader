package com.luxlunaris.openarticlereader.model.classes;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.util.ArrayList;

public class WebsiteData {


    public Document doc;
    public ArrayList<Connection.Response> images;

    public WebsiteData(Document doc, ArrayList<Connection.Response> images) {
        this.doc = doc;
        this.images = images;
    }


}
