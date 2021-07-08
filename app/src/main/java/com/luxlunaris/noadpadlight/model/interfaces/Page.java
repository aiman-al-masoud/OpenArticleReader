package com.luxlunaris.noadpadlight.model.interfaces;

import java.io.Serializable;

public interface Page extends Serializable {
	
	public String getText();
	
	public void setText(String text);

	public String getName();

	public long getCreationTime();

	public long getLastModifiedTime();

	public boolean delete();
	
	public void create();


	/**
	 * Get the next position of a token
	 * @param token
	 * @return
	 */
	public int nextPosition(String token);

	/**
	 * Get the previous position of a token.
	 * @param token
	 * @return
	 */
	public int previousPosition(String token);


	/**
	 * Get the next position of the current token
	 * @return
	 */
	public int nextPosition();


	/**
	 * Get the previous position of the current token
	 * @return
	 */
	public int previousPosition();


	public void savePosition(int pos);


	public int getLastPosition();

}
