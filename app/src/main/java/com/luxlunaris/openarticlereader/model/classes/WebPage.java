package com.luxlunaris.openarticlereader.model.classes;

import com.luxlunaris.openarticlereader.control.interfaces.PageListener;
import com.luxlunaris.openarticlereader.model.interfaces.Page;
import com.luxlunaris.openarticlereader.model.services.FileIO;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorCompletionService;

public class WebPage  extends File implements Page {

    SinglePage webArticlePage;

    SinglePage notesPage;

    class ReadOnlyPageException extends Exception {
    }


    public WebPage(String pathname){
        super(pathname);
        webArticlePage = new SinglePage(getPath()+File.separator+"article_page");
        notesPage = new SinglePage(getPath()+File.separator+"notes_page");
    }

    public void initHtml(String htmlSource){
        webArticlePage.setText(htmlSource);
    }

    public void initImgs(ArrayList<String> paths){
        for(String path : paths){
            webArticlePage.addImage(path, 0);
        }
    }


    public String getNotesText(){
        return notesPage.getText();
    }


    public void setNotesText(String text){
        notesPage.setText(text);
    }



    @Override
    public String getText() {
        return webArticlePage.getText();
    }

    @Override
    public void setText(String text) {
        //throw new ReadOnlyPageException();
    }


    @Override
    public String getName() {
        return this.getName();
    }

    @Override
    public long getCreationTime() {
        return 0;
    }

    @Override
    public long getLastModifiedTime() {
        return notesPage.getLastModifiedTime();
    }

    @Override
    public boolean delete() {
        FileIO.deleteDirectory(this.getPath());
        return true;
    }

    @Override
    public void create() {
        this.mkdirs();
        webArticlePage.create();
        notesPage.create();
    }

    @Override
    public int numOfTokens(String token) {
        return 0;
    }

    @Override
    public void setTokenToBeFound(String token) {

    }

    @Override
    public int nextPosition() {
        return 0;
    }

    @Override
    public int previousPosition() {
        return 0;
    }

    @Override
    public void savePosition(int pos) {

    }

    @Override
    public int getLastPosition() {
        return 0;
    }

    @Override
    public void addListener(PageListener listener) {

    }

    @Override
    public String getPreview() {
        return webArticlePage.getPreview();
    }

    @Override
    public boolean contains(String[] keywords) {
        return false;
    }

    @Override
    public boolean isSelected() {
        return webArticlePage.isSelected();
    }

    @Override
    public void setSelected(boolean select) {
        webArticlePage.setSelected(select);
    }

    @Override
    public void addImage(String path, int pos) {
        webArticlePage.addImage(path, pos);
    }

    @Override
    public File getImageDir() {
        return webArticlePage.getImageDir();
    }

    @Override
    public void addHtmlTag(int pos, String tag) {

    }

    @Override
    public void removeHtmlTags(int pos) {

    }

    @Override
    public boolean isInRecycleBin() {
        return false;
    }

    @Override
    public void setInRecycleBin(boolean inReycleBin) {

    }






}
