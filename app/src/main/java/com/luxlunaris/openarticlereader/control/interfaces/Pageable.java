package com.luxlunaris.openarticlereader.control.interfaces;
import com.luxlunaris.openarticlereader.model.interfaces.Page;


/**
 * Defines a "buffer/iterator" behavior: the implementation will have to
 * keep count of the returned pages, and, if called multiple times,
 * return the remaining (if any).
 */
public interface Pageable {
	
	public Page[] getNext(int amount);

}
