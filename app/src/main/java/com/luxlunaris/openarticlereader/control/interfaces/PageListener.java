package com.luxlunaris.openarticlereader.control.interfaces;


import com.luxlunaris.openarticlereader.model.interfaces.Page;

import java.io.Serializable;

/**
 * A PageListener is called by the Page itself whenever an event worthy of mention happens to it.
 *
 * (Many Pages, one PageListener)
 *
 */
public interface PageListener extends Serializable {

	public void onSelected(Page page);
	public void onDeleted(Page page);
	public void onModified(Page page);
	public void onCreated(Page page);

}
