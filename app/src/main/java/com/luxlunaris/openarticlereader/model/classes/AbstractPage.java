package com.luxlunaris.openarticlereader.model.classes;

import android.text.Html;
import android.util.Log;

import com.luxlunaris.openarticlereader.control.interfaces.PageListener;
import com.luxlunaris.openarticlereader.model.exceptions.WrongTagTypeException;
import com.luxlunaris.openarticlereader.model.interfaces.Metadata;
import com.luxlunaris.openarticlereader.model.interfaces.Page;
import com.luxlunaris.openarticlereader.model.services.FileIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public abstract class AbstractPage extends File implements Page {

    /**
     * manages this Page's stored metadata
     */
    protected Metadata metadata;

    /**
     * contains this Page's user-generated text
     */
    protected File textFile;

    /**
     * Directory that contains this Page's images.
     */
    protected File imageDir;

    /**
     * this Page's listeners (Notebook)
     */
    protected ArrayList<PageListener> listeners;

    /**
     * true if this Page is currently "selected"
     */
    protected boolean selected = false;

    /**
     * Data relative to the currently searched-for token.
     */
    Integer[] positionsOfToken;
    String currentToken;
    int posIndex = 0;

    /**
     * The tag that states whether this Page is editable or not.
     */
    public final String TAG_EDITABLE = "EDITABLE";



    //page tags
    public static final String SOURCE_URL = "";




    public AbstractPage(String pathname) {
        super(pathname);
        metadata = new MetadataFile(getPath()+File.separator+"metadata");
        textFile = new File(getPath()+File.separator+"text");
        imageDir = new File(getPath()+File.separator+"images");
        listeners = new ArrayList<>();
    }

    /**
     * Add a PageListener to this Page
     * @param listener
     */
    @Override
    public void addListener(PageListener listener) {
        Log.d("PAGE_GETS_LISTENER", listener+" started listening to: "+ this);
        listeners.add(listener);
    }

    /**
     * Is this Page's "selected" flag true?
     * @return
     */
    @Override
    public boolean isSelected() {
        return selected;
    }

    /**
     * Set this Page's "selected" flag
     * @param selected
     */
    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
        //notify the listener
        for(PageListener listener : listeners){
            listener.onSelected(this);
        }
    }

    /**
     * Sets the editable flag of this Page.
     * @param editable
     */
    public void setEditable(boolean editable){
        metadata.setTagValue(TAG_EDITABLE, editable+"");
    }

    /**
     * Checks the editable flag of this page.
     * @return
     */
    public boolean isEditable(){
        try {
            return metadata.getBoolean(TAG_EDITABLE);
        } catch (WrongTagTypeException e) {
            e.printStackTrace();
        }

        //editable by default.
        return true;
    }




    /**
     * get this Page's text from the text file.
     * (The raw text with all of the html tags).
     * @return
     */
    @Override
    public String getSource() {
        String text = FileIO.read(textFile.getPath());
        return text ==null? "" : text;
    }



    /**
     * Delete this page and all of its contents from disk
     * @return
     */
    @Override
    public boolean delete() {

        //notify the listeners that this got deleted
        for(PageListener listener : listeners){
            listener.onDeleted(this);
        }

        FileIO.deleteDirectory(this.getPath());

        //return del;
        return true;
    }

    /**
     * Create this Page on disk as a directory
     */
    @Override
    public void create() {


        mkdir();

        try {
            ((MetadataFile)metadata).createNewFile();
            textFile.createNewFile();
            imageDir.mkdir();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("CREATED_PAGE", "CREATED PAGE: "+getName()+" "+textFile.exists()+" "+imageDir.exists());

        //notify the listeners that this got created
        for(PageListener listener : listeners){
            listener.onCreated(this);
        }

    }


    /**
     * Get the text of this Page without any html tags.
     * @return
     */
    protected String getText(){
        return Html.fromHtml(getSource()).toString();
    }

    /**
     * How many times does a token appear in this Page's text?
     * @param token
     * @return
     */
    @Override
    public int numOfTokens(String token) {
        return getText().toUpperCase().split(token.toUpperCase()).length-1;
    }

    /**
     * Get this Page's name
     * @return
     */
    @Override
    public String getName(){
        return super.getName();
    }

    /**
     * Get this time this Page got last modified
     * @return
     */
    @Override
    public long getLastModifiedTime() {
        return textFile.lastModified();
    }

    /**
     * Set the token to be found in this Page
     * @param token
     */
    @Override
    public void setTokenToBeFound(String token){
        positionsOfToken = getTokensPositions(token);
        currentToken = token;
        posIndex = 0;
    }


    /**
     * Find all of the positions of a token in this Page
     * @param token
     * @return
     */
    private Integer[] getTokensPositions(String token) {

        //initialize list of positions
        ArrayList<Integer> positions = new ArrayList<Integer>();

        //convert token and text to upper case
        token = token.toUpperCase();

        //get the text (w/out tags, as displayed on screen) and convert it to upper case
        String text = getText().toUpperCase();

        //split the text by the token
        String[] parts = text.split(token);

        //first position
        positions.add(parts[0].length());

        //get the other positions
        for(int i =1; i<parts.length-1; i++){
            int lastPos = positions.get(positions.size()-1);
            int nextPos = lastPos+ token.length() +parts[i].length();
            positions.add(nextPos);
        }

        return positions.toArray(new Integer[0]);
    }


    /**
     *  Get the next position of the currently sought-after token
     * @return
     */
    @Override
    public int nextPosition() {

        //if no token, or no positions, return index = 0
        if(currentToken ==null || positionsOfToken.length==0){
            return 0;
        }


        if(posIndex+1 > positionsOfToken.length-1){
            return positionsOfToken[posIndex];
        }

        //return the due position, THEN increment the index
        return positionsOfToken[posIndex++];
    }

    /**
     *  Get the previous position of the currently sought-after token
     * @return
     */

    @Override
    public int previousPosition() {

        //if no token, or no positions, return index = 0
        if(currentToken ==null || positionsOfToken.length==0){
            return 0;
        }

        if(posIndex-1 < 0){
            return positionsOfToken[posIndex];
        }

        //return the due position, THEN increment the index
        return positionsOfToken[posIndex--];
    }

    /**
     * Set a "bookmark" within this page
     * @param pos
     */
    @Override
    public void savePosition(int pos) {
        metadata.setTagValue("LAST_POSITION", pos+"");
    }

    /**
     * Get this Page's "bookmark", aka last position visited.
     * @return
     */
    @Override
    public int getLastPosition() {
        String lastPosString = metadata.getString("LAST_POSITION")==null? "0" : metadata.getString("LAST_POSITION");
        return Integer.parseInt(lastPosString);
    }

    /**
     * Get a text-based preview of this Page.
     * (The first line).
     * @return
     */
    @Override
    public String getPreview() {
        return FileIO.readLine(textFile.getPath())+"\n";
    }


    /**
     * Checks if this page contains ALL of the provided keywords
     * (ANDed keywords)
     * @param keywords
     * @return
     */
    public boolean contains(String[] keywords){

        String text =  getText().toUpperCase();
        for(String keyword : keywords){
            if(!text.contains(keyword.toUpperCase())){
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean isInRecycleBin() {

        try {
            return metadata.getBoolean("IN_RECYCLE_BIN");
        } catch (WrongTagTypeException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void setInRecycleBin(boolean inRecycleBin) {
        metadata.setTagValue("IN_RECYCLE_BIN", inRecycleBin+"");
    }

    /**
     * Generate an image "tag" given its path.
     * @param path
     * @return
     */
    protected String generateImgTag(String path){
        //opening and closing tags
        String openImgTag = "<p><img src=\'";
        String closeImgTag = "\' /></p>";

        String element = openImgTag+path+closeImgTag;

        return element;
    }

    @Override
    public void setTag(String tag, String value) {
        metadata.setTagValue(tag, value);
    }

    @Override
    public String getStringTag(String tag) {
        return metadata.getString(tag);
    }

    @Override
    public int getIntTag(String tag) {
        try {
            return metadata.getInt(tag);
        } catch (WrongTagTypeException e) {
            e.printStackTrace();
        }

        //TODO: choose default value based on tag
        return 0;
    }

    @Override
    public boolean getBooleanTag(String tag) {
        try {
            metadata.getBoolean(tag);
        } catch (WrongTagTypeException e) {
            e.printStackTrace();
        }

        //TODO: choose default value based on tag
        return false;
    }

    @Override
    public double getFloatTag(String tag) {
        try {
            return metadata.getFloat(tag);
        } catch (WrongTagTypeException e) {
            e.printStackTrace();
        }

        //TODO: choose default value based on tag
        return 0;
    }

    @Override
    public long getLongTag(String tag) {
        try {
            return metadata.getLong(tag);
        } catch (WrongTagTypeException e) {
            e.printStackTrace();
        }

        //TODO: choose default value based on tag
        return 0;
    }






}
