package com.luxlunaris.openarticlereader.control.interfaces;

import com.luxlunaris.openarticlereader.model.interfaces.Page;

import java.io.Serializable;

/**
 * Listens to the Notebook for updates about the status
 * of created, deleted, modified Pages.
 */
public interface NotebookListener extends Serializable {

    public void onCreated(Page page);
    public void onDeleted(Page page);
    public void onModified(Page page);

}
