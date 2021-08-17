package com.luxlunaris.openarticlereader.control.classes;

import com.luxlunaris.openarticlereader.control.interfaces.NotebookListener;
import com.luxlunaris.openarticlereader.control.interfaces.PageListener;
import com.luxlunaris.openarticlereader.control.interfaces.Pageable;
import com.luxlunaris.openarticlereader.model.classes.Article;
import com.luxlunaris.openarticlereader.model.classes.Copier;
import com.luxlunaris.openarticlereader.model.classes.Downloader;
import com.luxlunaris.openarticlereader.model.classes.WebsiteData;
import com.luxlunaris.openarticlereader.model.classes.comparators.LastModifiedComparator;
import com.luxlunaris.openarticlereader.model.interfaces.Page;
import com.luxlunaris.openarticlereader.model.services.FileIO;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a facade controller that maintains a list of all of the user's pages,
 * and provides methods to create a new page, get a batch of pages of a specified size,
 * and more...
 *
 * It listens to all Pages, and it's listened to by a
 * NotebookListener.
 *
 *
 */
public class Notebook implements Pageable, PageListener, Downloader.WebUser {


	/**
	 * The instance of this Singleton class
	 */
	private static Notebook instance;

	/**
	 * The path to which all of the existing pages are stored
	 */
	private static final String PAGES_DIR = Paths.PAGES_DIR;

	/**
	 * The path to which deleted pages are stored (recycle bin)
	 */
	private static final String PAGES_RECYCLE_BIN = Paths.PAGES_RECYCLE_BIN;

	/**
	 * List of pages loaded in memory
	 */
	private static ArrayList<Page> pagesList;

	/**
	 * List of pages selected by the user
	 */
	private static ArrayList<Page> selectedPagesList;

	/**
	 * The list of pages in the recycle bin.
	 */
	private static ArrayList<Page> recycleBin;

	/**
	 * Current page index
	 */
	static int currentPage;

	/**
	 * Listens to this Notebook to receive updates on the status
	 * of the Pages therein.
	 */
	private static NotebookListener listener;

	/**
	 * The object that downloads and async-returns content from the web.
	 */
	private static Downloader downloader;


	private Notebook() {
		pagesList = new ArrayList<>();
		selectedPagesList = new ArrayList<>();
		recycleBin = new ArrayList<>();
		loadPages();
		loadRecycleBin();
		rewind();
		downloader = new Downloader();
		downloader.addListener(this);
	}

	/**
	 * Notebook is a Singleton
	 * @return
	 */
	public synchronized static Notebook getInstance() {
		return instance!=null? instance : (instance = new Notebook());
	}


	/**
	 * Called by a Page when it gets selected.
	 * Notebook adds it to the list of selected pages
	 * @param page
	 */
	@Override
	public void onSelected(Page page) {
		if(page.isSelected()){
			selectedPagesList.add(page);
		}else{
			selectedPagesList.remove(page);
		}
	}


	/**
	 * Returns an array of the selected pages
	 */
	public Page[] getSelected(){
		return selectedPagesList.toArray(new Page[0]);
	}


	/**
	 * When a page is deleted, it informs the Notebook
	 * @param page
	 */
	@Override
	public void onDeleted(Page page) {

		//add deleted page to recycle-bin
		putInRecycleBin(page);

		//remove the page from the "selected" list
		selectedPagesList.remove(page);

		//remove the page from the pages list
		pagesList.remove(page);

		//propagate the news about its deletion.
		try {
			listener.onDeleted(page);
		}catch (NullPointerException e){}

	}

	@Override
	public void onModified(Page page) {

		try{
			listener.onModified(page);
		}catch (NullPointerException e){
		}

		//re-sort the list of pages.
		//Collections.sort(pagesList, new LastModifiedComparator());

	}

	@Override
	public void onCreated(Page page) {

		//add page to
		addPage(page);

		try{
			listener.onCreated(page);
		}catch (NullPointerException e){
		}

	}


	/**
	 * Returns the next batch of pages
	 * @param amount
	 * @return
	 */
	@Override
	public Page[] getNext(int amount) {


		//calculating the amount of pages left to deliver
		amount = Math.min(amount, pagesList.size() -currentPage );

		List<Page> result = new ArrayList<>();

		try{
			result = pagesList.subList(currentPage, currentPage+amount);
			currentPage+=amount;
		}catch (Exception e){

		}

		return result.toArray(new Page[0]);

	}
	
	/**
	 * Get an array of pages by whitespace-separated keywords.
	 * @param query
	 * @return
	 */
	public void getByKeywords(String query) {

		Thread t = new Thread() {
			@Override
			public void run() {

				String[] keywords = query.split("\\s+");
				ArrayList<Page> result = new ArrayList<>(pagesList);

				for (Page page : pagesList) {
					if (page.contains(keywords)) {

						//as soon as you find a page that fits the keywords tell the
						//listener to display it.
						listener.onCreated(page);

					}
				}

			}
		};

		t.start();
	}


	/**
	 * Load pages to memory from their directory
	 */
	public void loadPages() {

		//create the pages dir if it doesn't exist yet
		File pagesDir = new File(PAGES_DIR);
		if(!pagesDir.exists()){
			pagesDir.mkdirs();
		}

		//list and load all of the folders in there
		for(File file : pagesDir.listFiles()) {
			Page page = new Article(file.getPath());
			addPage(page);
		}

		//sort the pages by time of creation
		Collections.sort(pagesList, new LastModifiedComparator());
	}


	/**
	 * Add a page to the list and start listening to it
	 * @param page
	 */
	private void addPage(Page page){

		//don't add the same page twice
		if(pagesList.contains(page)){
			return;
		}

		//start listening to the new page
		page.addListener(this);
		//add the page at beginning (list sorted newest first)
		pagesList.add(0, page);
	}


	/**
	 * Mark all Pages as selected
	 */
	public void selectAll(){
		selectedPagesList = new ArrayList<>(pagesList);
	}

	/**
	 * Mark all pages as unselected
	 */
	public void unselectAll(){
		selectedPagesList.clear();
	}

	/**
	 * Add a NotebookListener to this Notebook.
	 * @param listener
	 */
	public void setListener(NotebookListener listener){
		this.listener = listener;
	}

	/**
	 * The next batch of pages to deliver is reset to the initial one.
	 */
	public void rewind(){
		currentPage = 0;
	}

	/**
	 * Generate and return a zipped backup file that contains
	 * all of the pages' contents.
	 * @return
	 */
	public void generateBackupFile(BackupRequester listener){

		new Thread(){
			public void run(){
				File file = FileIO.zipDir(PAGES_DIR, Paths.PAGES_BACKUP_DIR);
				listener.onBackupReady(file);
			}
		}.start();
	}

	/**
	 * Implemented by objects that need to request a backup from Notebook.
	 */
	public interface BackupRequester{
		void onBackupReady(File backupFile);
	}

	/**
	 * Import pages from a zip file.
	 * @param sourcePath
	 */
	public void importPages(String sourcePath){
		File unzipped = FileIO.unzipDir(sourcePath, sourcePath+"unzipped");

		File pagesFolder = new File(unzipped.getPath()+File.separator+"pages");

		for(File file : pagesFolder.listFiles()){

			//copy each file from the unzipped file
			try {
				FileUtils.copyDirectory(file, new File(PAGES_DIR+File.separator+file.getName()));
				Page page = new Article(file.getPath());
				addPage(page);
				listener.onCreated(page);
			} catch (IOException e) {
				e.printStackTrace();
			}

			//Collections.sort(pagesList, new LastModifiedComparator());
		}
	}

	/**
	 * Put a page in the recycle bin:
	 * this creates a copy of the original page
	 * and places it in an alternate directory
	 *
	 * @param page
	 */
	private void putInRecycleBin(Page page){

		//if page is already in the recycle bin, remove it.
		//It means it's getting deleted forever.
		if(page.isInRecycleBin()){
			recycleBin.remove(page);
			return;
		}

		Article copy = new Article(PAGES_RECYCLE_BIN+File.separator+page.getName());
		copy.create();

		new Copier().copy(page, copy);
		copy.setInRecycleBin(true);

		recycleBin.add(copy);
		copy.addListener(this);
	}

	/**
	 * Remove a page from the recycle bin and put
	 * it back with the existing pages.
	 * @param page
	 */
	private void restoreFromRecycleBin(Page page){

		if(!page.isInRecycleBin()){
			return;
		}

		recycleBin.remove(page);

		Page restoredCopy = new Article(PAGES_DIR+File.separator+page.getName());
		restoredCopy.addListener(this);
		restoredCopy.create();

		new Copier().copy(page, restoredCopy);
		restoredCopy.setInRecycleBin(false);

		page.delete();

	}

	/**
	 * Permanently delete all of the pages in the recycle bin.
	 * And notify the listening UI that they got deleted.
	 */
	public void emptyRecycleBin(){
		for(Page page : getRecycleBin()){
			FileIO.deleteDirectory(((File)page).getPath() );
			listener.onDeleted(page);
		}
		recycleBin.clear();
	}

	/**
	 * Get the pages in the recycle bin.
	 * @return
	 */
	public Page[] getRecycleBin(){
		return recycleBin.toArray(new Page[0]);
	}

	/**
	 * Load the pages of the recycle bin from disk.
	 */
	private void loadRecycleBin(){

		File recycleBinDir = new File(PAGES_RECYCLE_BIN);

		if(! recycleBinDir.exists()){
			new File(PAGES_RECYCLE_BIN).mkdirs();
		}

		for(File file : recycleBinDir.listFiles()){
			Article page = new Article(file.getPath());
			recycleBin.add(page);
			page.addListener(this);
		}
	}


	/**
	 * Restore the selected pages from the recycle bin.
	 */
	public void restoreSelection(){
		for(Page page : getSelected()){
			restoreFromRecycleBin(page);
		}
		unselectAll();
	}


	public void download(String address){
		downloader.download(address);
	}

	public void downloadAll(String homepage){
		downloader.downloadAll(homepage);
	}

	public void pauseDownloads(){
		downloader.stopAll();
	}

	public void resumeDownloads(){
		downloader.resumeAll();
	}


	/**
	 * Called by a DownloadThread from the Downloader when a
	 * a download is ready.
	 * @param data
	 */
	@Override
	public void onDownloadReady(WebsiteData data) {

		synchronized (this){

			new Thread() {

				public void run() {

					Article page = new Article(PAGES_DIR+File.separator+System.currentTimeMillis());
					page.initContent(data);
					page.create();
					addPage(page);

					try {
						listener.onCreated(page);
					}catch (NullPointerException e){
						e.printStackTrace();
					}

				}

			}.start();

		}

	}




}

