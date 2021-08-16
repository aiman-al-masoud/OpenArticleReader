package com.luxlunaris.openarticlereader.model.interfaces;

import com.luxlunaris.openarticlereader.control.interfaces.PageListener;

import java.io.File;
import java.io.Serializable;

/**
 * Page is the storage unit of the Notebook.
 *
 * The user can create several Pages, each Page
 * can contain text, other files, and the relative metadata.
 *
 */
public interface Page extends Serializable {

	//////
	/**
	 * Get the Page's name
	 * @return
	 */
	public String getName();


	//////
	/**
	 * Get the time the Page was created
	 * @return
	 */
	public long getCreationTime();


	/**
	 * Get the last time the Page was modified
	 * @return
	 */
	public long getLastModifiedTime();

	///////////
	/**
	 * Delete the Page
	 * @return
	 */
	public boolean delete();

	/////////////
	/**
	 * Create the Page
	 */
	public void create();


	///////////////
	/**
	 * get the number of tokens
	 * @param token
	 * @return
	 */
	public int numOfTokens(String token);


	//////////////
	/**
	 * Set the token to be found.
	 * @param token
	 */
	public void setTokenToBeFound(String token);


	////////////
	/**
	 * Get the next position of the current token
	 * @return
	 */
	public int nextPosition();


	/////////////
	/**
	 * Get the previous position of the current token
	 * @return
	 */
	public int previousPosition();


	//////////
	/**
	 * Save the current position
	 * @param pos
	 */
	public void savePosition(int pos);


	////////////
	/**
	 * Get the last-saved (last visited) position
	 * @return
	 */
	public int getLastPosition();

	////////////
	/**
	 * Add a PageListener to this Page
	 * @param listener
	 */
	public void addListener(PageListener listener);

	//////////
	/**
	 * Get a text based preview of this Page
	 * @return
	 */
	public String getPreview();

	/////////
	/**
	 *  Checks if this page contains ALL of the provided keywords
	 * 	(ANDed keywords)
	 * @param keywords
	 * @return
	 */
	public boolean contains(String[] keywords);


	//////////
	/**
	 * Is this Page currently selected?
	 * @return
	 */
	public boolean isSelected();


	//////////
	/**
	 * Set this Page as selected.
	 * @param select
	 */
	public void setSelected(boolean select);

	public boolean isInRecycleBin();

	////
	public void setInRecycleBin(boolean inReycleBin);

	///////
	public boolean isEditable();

	//////////
	public void setEditable(boolean editable);

	/**
	 * Get the Page's html source
	 * @return
	 */
	public String getSource();

	/**
	 * Returns the image directory of this Page.
	 * @return
	 */
	public File getImageDir();

}
