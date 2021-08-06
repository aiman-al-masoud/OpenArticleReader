package com.luxlunaris.noadpadlight.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.luxlunaris.noadpadlight.R;
import com.luxlunaris.noadpadlight.control.classes.Notebook;
import com.luxlunaris.noadpadlight.control.classes.ProxyNotebookListener;
import com.luxlunaris.noadpadlight.control.interfaces.NotebookListener;
import com.luxlunaris.noadpadlight.model.interfaces.Page;

import java.util.ArrayList;
import java.util.Random;

/**
 * This activity allows the user to access, delete, modify
 * Pages, presenting them on a PageFragment each.
 */
public class PagesActivity extends ColorActivity  implements NotebookListener {

    /**
     * The Notebook manages the pages.
     */
    Notebook notebook = Notebook.getInstance();

    /**
     * The layout that hosts the page fragments.
     */
    transient LinearLayout pagesLinLayout;

    /**
     * How many pages are loaded in a batch
     */
    final int PAGES_IN_A_BATCH = 10; //too small makes it impossible to reach the bottom

    /**
     * The page fragments that are on-screen
     */
    transient ArrayList<PageFragment> pageFragments;

    /**
     * Keeps track of changes happening to pages while
     * this activity is in the background, so that
     * onResume can know what fragments to add/remove/modify.
     */
    transient ProxyNotebookListener changes;

    /**
     * True if the user currently wants to see only page results to a query.
     */
    transient boolean isSearching = false;


    /**
     * On create.
     * @param savedInstanceState
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.Theme_AppCompat_Light_DarkActionBar);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pages);

        //get the lin layout that will hold the fragments
        pagesLinLayout = findViewById(R.id.pages_linear_layout);

        //initialize list to store fragments
        pageFragments = new ArrayList<>();

        //load first block of pages
        loadNextPagesBlock();

        //defines what the activity does when scrolling occurs
        ScrollView scrollView = findViewById(R.id.scroll_view_pages);
        scrollView.setOnScrollChangeListener(new ScrollHandler());

        //to keep track of changes while in the background
        changes = new ProxyNotebookListener();

        //start listening to notebook
        notebook.setListener(this);
    }


    /**
     * Used to add more pages when you scroll all the
     * way down.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    class ScrollHandler implements View.OnScrollChangeListener{

        @Override
        public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            //if can't scroll vertically anymore: bottom reached
            if(!v.canScrollVertically(1)){

                //load no new pages if the user is currently running a query
                if(isSearching){
                    return;
                }

                loadNextPagesBlock();
            }

        }
    }





    /**
     * Ensures that there is ONE and only ONE fragment for each Page in the fragments list.
     * @param page
     * @return
     */
    private PageFragment getFragment(Page page){

        //check if it's "equal" to an already existing one
        for(PageFragment pgFrag : pageFragments){
            if(pgFrag.getPage().getName().equals(page.getName())){
                return pgFrag;
            }
        }
        //create a new fragment
        return PageFragment.newInstance(page);
    }



    /**
     * Add a page fragment to the list
     * @param page
     */
    private void addPage(Page page, boolean top){

        //get the appropriate page fragment
        PageFragment pgFrag = getFragment(page);

        //the id of the future container of pgFrag
        int containerId = -1;


        if(!top){
            //add the new page fragment to the bottom of the list layout
            containerId = pagesLinLayout.getId();
        }else{
            //else add the new page fragment on top of all others
            FrameLayout child = new FrameLayout(pagesLinLayout.getContext());
            child.setId(new Random().nextInt(1000000000));
            pagesLinLayout.addView(child, 0);
            containerId = child.getId();
        }

        //add the fragment to the screen
        try{
            getSupportFragmentManager().beginTransaction().add(containerId,pgFrag,page.getName()).commit();
            getSupportFragmentManager().executePendingTransactions();
        }catch (Exception | Error  e){
            e.printStackTrace();
        }

        //add the page fragment to the fragment's list
        pageFragments.add(pgFrag);
    }



    /**
     * Loads an array of pages as page fragments
     */
    private void loadPages(Page[] pages){
        for(Page page : pages){
            addPage(page, false);
        }
    }

    /**
     * Loads the next block of page fragments
     */
    private void loadNextPagesBlock(){
       loadPages(notebook.getNext(PAGES_IN_A_BATCH));
    }

    /**
     * Removes all of currently displayed pages (without deleting the pages)
     */
    private void removeAllPages(){

        //remove all fragments
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            removeFragment(fragment);
        }

        //make sure they're all removed
        getSupportFragmentManager().executePendingTransactions();

        //forget about all fragments
        pageFragments.clear();

        //tell the notebook that you want to start over cycling through pages.
        notebook.rewind();
    }


    /**
     * Removes a fragment without deleting its page
     * @param page
     */
    private void removeFragment(Page page){
        PageFragment frag = getFragment(page);
        pageFragments.remove(frag);
        removeFragment(frag);
    }


    /**
     * Removes a fragment.
     * @param fragment
     */
    private void removeFragment(Fragment fragment){
        try{
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }catch (Exception | Error e){
            e.printStackTrace();
        }
    }


    /**
     * Create the toolbar menu for this activity
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //inflate the menu's layout xml
        getMenuInflater().inflate(R.menu.pages_activity_toolbar, menu);

        //make a search view for queries on articles
        SearchView searchView = (SearchView)menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                isSearching = true;
                removeAllPages();
                notebook.getByKeywords(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Perform an action from the toolbar menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){

            case R.id.go_to_settings:

                //start the settings activity
                Intent goToSetIntent = new Intent(this, SettingsActivity.class);
                startActivity(goToSetIntent);
                break;
            case R.id.new_page:
                Intent intent = new Intent(this, ReaderActivity.class);
                intent.putExtra(ReaderActivity.PAGE_EXTRA,notebook.newPage());
                startActivity(intent);
                break;
            case R.id.edit:
                EditMenu editMenu = new EditMenu(this, findViewById(R.id.edit));
                editMenu.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * This menu appears on the Pages activity and
     * allows the user to do stuff with multiple
     * pages after having selected them.
     */
    class EditMenu extends PopupMenu{

        public EditMenu(Context context, View anchor) {
            super(context, anchor);
            getMenuInflater().inflate(R.menu.edit_menu, this.getMenu());
            setOnMenuItemClickListener(new EditMenuHandler());
        }

    }

    /**
     * This is the EditMenu's brain.
     */
    class EditMenuHandler implements PopupMenu.OnMenuItemClickListener{

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.delete:

                    for(Page page : notebook.getSelected()){
                        //delete the page (the fragment will automatically be removed too through the callback method "onDeleted")
                        page.delete();
                    }
                    break;
                case R.id.compact:
                    notebook.compactSelection();
                    break;

            }

            return true;
        }
    }


    /**
     * Action when the back button is pressed
     */
    @Override
    public void onBackPressed() {

        isSearching = false;

        //on back pressed add all pages to this activity
        if(notebook.getPagesNum() > pageFragments.size()){
            removeAllPages();
            loadNextPagesBlock();
        }
    }



    /**
     * Called by Notebook when a new page gets created.
     * @param page
     */
    @Override
    public void onCreated(Page page) {

        //if in foreground, just add the page fragment.
        if(isInForeground()) {

            //run on ui thread if onCreated gets called from a different thread.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addPage(page, true);
                }
            });

            return;
        }


        //else you're in background, stash in changes
        changes.onCreated(page);
    }

    /**
     * Called by notebook when a page gets deleted.
     * @param page
     */
    @Override
    public void onDeleted(Page page) {

        //if in foreground simply remove the page's fragment.
        if(isInForeground()){
            removeFragment(page);
        }

        //else you're in background, stash in changes
        changes.onDeleted(page);
    }

    /**
     * Called by notebook when a page gets modified.
     * @param page
     */
    @Override
    public void onModified(Page page) {

        //if in foreground simply remove and add again
        if(isInForeground()){
            removeFragment(page);
            addPage(page, true);
        }

        //else you're in background, stash in changes
        changes.onModified(page);
    }


    /**
     * On resume, this activity checks if there have been
     * changes to the Pages displayed, and eventually
     * updates the list.
     */
    @Override
    protected void onResume() {
        super.onResume();

        //put the modified pages back on top
        for(Page page : changes.popJustModified()){
            removeFragment(page);
            addPage(page, true);
        }

        //get the pages that were created while this activity was in the
        //background and add the appropriate fragments
        for(Page page : changes.popJustCreated()){
            addPage(page, true);
        }

        //get the pages that were deleted while this activity was in the
        //background and remove the relative fragments
        for(Page page : changes.popJustDeleted()){
            removeFragment(page);
        }

    }















}