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

	/**
	 * Get the Page's name
	 * @return
	 */
	String getName();

	/**
	 * Get the time the Page was created
	 * @return
	 */
	long getCreationTime();

	/**
	 * Get the last time the Page was modified
	 * @return
	 */
	long getLastModifiedTime();

	/**
	 * Delete the Page
	 * @return
	 */
	boolean delete();

	/**
	 * Create the Page
	 */
	void create();

	/**
	 * get the number of tokens
	 * @param token
	 * @return
	 */
	int numOfTokens(String token);

	/**
	 * Set the token to be found.
	 * @param token
	 */
	public void setTokenToBeFound(String token);

	/**
	 * Get the next position of the current token
	 * @return
	 */
	int nextPosition();

	/**
	 * Get the previous position of the current token
	 * @return
	 */
	int previousPosition();

	/**
	 * Save the current position
	 * @param pos
	 */
	void savePosition(int pos);

	/**
	 * Get the last-saved (last visited) position
	 * @return
	 */
	int getLastPosition();

	/**
	 * Add a PageListener to this Page
	 * @param listener
	 */
	void addListener(PageListener listener);

	/**
	 * Get a text based preview of this Page
	 * @return
	 */
	String getPreview();

	/**
	 *  Checks if this page contains ALL of the provided keywords
	 * 	(ANDed keywords)
	 * @param keywords
	 * @return
	 */
	boolean contains(String[] keywords);

	/**
	 * Is this Page currently selected?
	 * @return
	 */
	boolean isSelected();

	/**
	 * Set this Page as selected.
	 * @param select
	 */
	void setSelected(boolean select);

	boolean isInRecycleBin();

	void setInRecycleBin(boolean inReycleBin);

	boolean isEditable();

	void setEditable(boolean editable);
	/**
	 * Get the Page's html source
	 * @return
	 */
	String getSource();
	/**
	 * Returns the image directory of this Page.
	 * @return
	 */
	File getImageDir();






}
