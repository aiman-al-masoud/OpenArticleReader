package com.luxlunaris.openarticlereader.model.classes;

import android.util.Log;

import com.luxlunaris.openarticlereader.control.interfaces.PageListener;
import com.luxlunaris.openarticlereader.model.classes.AbstractPage;
import com.luxlunaris.openarticlereader.model.services.FileIO;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditablePage extends AbstractPage {


    public EditablePage(String pathname) {
        super(pathname);
    }


    /**
     * Add an image to this Page.
     * @param path
     */
    public void addImage(String path, int pos) {

        //convert the position in the rendered text to line number
        int lineNum = getLine(pos);

        //get the line number to paragraph number
        int parNum = lineToParagraph(lineNum);
        //(add the image as a new paragraph after the one selected, hence: +1)
        parNum+=1;

        //prepare a new file in this Page's imgDir
        File imageCopy = new File(imageDir.getPath()+File.separator+System.currentTimeMillis());

        //copy provided image to this Page's imgDir
        FileIO.copyFile(path, imageCopy.getPath());

        //create the image element in html
        String imgElement = generateImgTag(imageCopy.getPath());

        //get the paragraphs of this page
        String[] pars = getParagraphs();

        //convert the paragraphs array to a mutable list
        List<String> parsList = new ArrayList<>(Arrays.asList(pars));

        //add a new image-paragraph at the specified position.
        parsList.add(parNum, "<p>"+imgElement+"</p>");

        //recompose the html source from the paragraphs' list.
        String newHtml = "";
        for(String par : parsList){
            newHtml+=par;
        }

        //save the new html source
        setSource(newHtml);
    }


    /**
     * Get the html source as a list of paragraphs.
     * @return
     */
    protected String[] getParagraphs(){
        //split the html source by end of paragraph tags
        String[] pars = getSource().split("</p>");

        //remove the last empty "paragraph"
        pars = Arrays.copyOf(pars, pars.length-1);

        //adjust each paragraph
        for(int i =0; i<pars.length; i++){
            pars[i] = pars[i].replaceAll("\n", "");
            pars[i] = pars[i]+" </p>";
        }

        return pars;
    }

    /**
     * From the position in the rendered text, determine
     * the line.
     * @param pos
     * @return
     */
    private int getLine(int pos){

        String text = getText();

        //if length
        if(text.length()==0){
            return 0;
        }

        String upTillPos;
        upTillPos = text.substring(0, Math.min(pos, text.length()-1));

        int newLines = upTillPos.split("\n").length;

        return newLines;
    }

    /**
     * Given a line number find the paragraph containing it.
     * @param lineNum
     * @return
     */
    private int lineToParagraph(int lineNum){

        //get the paragraphs in this Page
        String[] pars = getParagraphs();

        //get the number of lines in each paragraph
        int[] numLinesPerPar =  new int[pars.length];
        for(int i =0; i<pars.length; i++){
            numLinesPerPar[i] = pars[i].split("<br>").length;
            if(numLinesPerPar[i]==1){
                numLinesPerPar[i] = 2;
            }
        }

        //test log how many lines in each paragraph
        for(int i =0; i<numLinesPerPar.length; i++){
            Log.d("LINE_NUM", "par "+i+" has: "+numLinesPerPar[i]+" lines");
        }

        //convert the lineNum to a paragraph num
        int accumulLines = 0;
        for(int i =0; i<numLinesPerPar.length; i++){
            accumulLines += numLinesPerPar[i];
            if(lineNum <= accumulLines){
                Log.d("LINE_NUM", "line "+lineNum+ " is in paragraph: "+i);
                return i;
            }
        }

        return numLinesPerPar.length-1;
    }

    @Override
    public File getImageDir() {
        return imageDir;
    }


    /**
     * Surround some text with an html tag and save.
     * (Works on entire paragraphs.)
     * @param pos
     * @param tag
     */
    public void addHtmlTag(int pos, String tag){

        int lineNum = getLine(pos);
        Log.d("LINE_NUM", lineNum+"");

        //get the paragraphs
        String[] pars = getParagraphs();

        //convert the line number to the paragraph number.
        lineNum = lineToParagraph(lineNum);

        //make the tags from the inner part
        String startTag = "<"+tag+">";
        String endTag = "</"+tag+">";

        //apply the html tag
        pars[lineNum] = startTag+pars[lineNum]+endTag;

        //re-build the html source from the single paragraphs.
        String newHtml = "";
        for(String par : pars){
            newHtml+=par;
        }

        //save it.
        setSource(newHtml);

    }

    /**
     * Remove all html tags from a paragraph.
     * @param pos
     */

    public void removeHtmlTags(int pos){

        int lineNum = getLine(pos);

        //get the paragraphs
        String[] pars = getParagraphs();

        //convert the line number to the paragraph number.
        lineNum = lineToParagraph(lineNum);

        String modifiedPar = pars[lineNum];

        //remove all tags other than the paragraph tag. (sort of)
        modifiedPar = modifiedPar.replaceAll("<[abcefghijklmnoqrstuvwxyz]>", "").replaceAll("</[abcefghijklmnoqrstuvwxyz]>", "");

        //replace the paragraph
        pars[lineNum] = modifiedPar;

        //re-build the html source from the single paragraphs.
        String newHtml = "";
        for(String par : pars){
            newHtml+=par;
        }

        //save it.
        setSource(newHtml);
    }

    /**
     * Save new/edited text to the text file.
     * @param text
     */

    public synchronized void setSource(String text) {

        //if this page isn't editable throw an illegal state exception
        if(isInRecycleBin()){
            throw new IllegalStateException("Tried editing page in recycle bin!");
        }

        FileIO.write(textFile.getPath(), text);

        //notify the listeners
        for(PageListener listener : listeners){
            listener.onModified(this);
        }

        //delete any non-used image files.
        checkDeleteImages();
    }


    /**
     * Checks if there are any images that
     * don't have a corresponding tag in the
     * html source and deletes them from the
     * imageDir.
     */
    private void checkDeleteImages(){

        //get the html source
        String text = getSource();

        //for each image...
        for(File imgFile : imageDir.listFiles()){

            Log.d("IMAGE_DEL", imgFile.getName());

            String nameOfImage = imgFile.getName();

            //if the name of the image is not in the html source, the image file is useless
            if(!text.contains(nameOfImage)){
                imgFile.delete();
                Log.d("IMAGE_DEL", imgFile.getName() + "no longer in use, deleted!");
            }

        }

    }







}
