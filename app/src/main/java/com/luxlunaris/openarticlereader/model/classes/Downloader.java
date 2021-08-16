package com.luxlunaris.openarticlereader.model.classes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class Downloader {


    private WebUser listener;


    private ArrayList<Thread> activeThreads;


    public interface WebUser{

        /**
         * Should be synchronized/thread safe!
         * @param data
         */
        public void onDownloadReady(WebsiteData data);
    }

    public Downloader(){
        this.activeThreads = new ArrayList<>();
    }



    public void addListener(WebUser listener) {
        this.listener = listener;
    }


    public void download(String address) {
        DownloadThread d = new DownloadThread(address);
        activeThreads.add(d);
        d.start();
    }

    public void downloadAll(String homepage){
        DownloadAllThread t = new DownloadAllThread(homepage);
        t.start();
    }

    class DownloadAllThread extends Thread{

        String homepage;

        public DownloadAllThread(String homepage){
            this.homepage = homepage;
        }

        public void run(){

            Document doc = downloadDocument(homepage);
            Elements linkElems =  doc.select("a");

            for(Element e : linkElems){
                String link = e.absUrl("href");
                download(link);
            }
        }

    }





    private Document downloadDocument(String address) {
        try {
            return Jsoup.connect(address).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private Connection.Response downloadImage(String address) {
        try {
            return Jsoup.connect(address).ignoreContentType(true).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public void interruptAll() {

        for(Thread t : activeThreads) {
            t.stop();
        }
    }

    public void resumeAll() {
        new Thread(){
            public void run(){
                for(Thread t : activeThreads) {
                    t.start();
                }
            }
        }.start();
    }


    public void stopAll() {

        new Thread(){
            public void run(){
                for(Thread t : activeThreads) {
                    t.interrupt();
                }
            }
        }.start();

    }









    /**
     * Thread that downloads a single webpage and puts the data in a WebsiteData object.
     */
    class DownloadThread extends Thread{

        private String address;

        private Document doc;

        public DownloadThread(String address) {
            this.address = address;
        }


        public void run() {

            if(isInterrupted()){
                return;
            }

            //try downloading the document
            doc = downloadDocument(address);

            //stop if the document is null
            if(doc==null) {
                return;
            }

            ArrayList<Connection.Response> images = new ArrayList<>();

            //get the image elements
            Elements elements = doc.select("img");

            //for each element download the image
            for(Element e : elements) {

                //absolute URL on src
                String absoluteUrl = e.absUrl("src");

                //download the image
                Connection.Response img =  downloadImage(absoluteUrl);
                if(img!=null) {
                    images.add(img);
                }

            }


            WebsiteData data = new WebsiteData(doc, images);

            activeThreads.remove(this);

            listener.onDownloadReady(data);

        }

    }







}
