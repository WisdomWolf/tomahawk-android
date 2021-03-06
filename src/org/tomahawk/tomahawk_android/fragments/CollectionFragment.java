/* == This file is part of Tomahawk Player - <http://tomahawk-player.org> ===
 *
 *   Copyright 2013, Enno Gottschalk <mrmaffen@googlemail.com>
 *
 *   Tomahawk is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Tomahawk is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Tomahawk. If not, see <http://www.gnu.org/licenses/>.
 */
package org.tomahawk.tomahawk_android.fragments;

import org.tomahawk.libtomahawk.collection.Collection;
import org.tomahawk.libtomahawk.collection.CollectionManager;
import org.tomahawk.tomahawk_android.R;
import org.tomahawk.tomahawk_android.TomahawkApp;
import org.tomahawk.tomahawk_android.adapters.TomahawkPagerAdapter;
import org.tomahawk.tomahawk_android.utils.FragmentUtils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link TomahawkListFragment} which shows a simple listview menu to the user, so that he can
 * choose between a {@link TracksFragment}, an {@link AlbumsFragment} and an {@link
 * ArtistsFragment}, which display the {@link org.tomahawk.libtomahawk.collection.UserCollection}'s
 * content to the user.
 */
public class CollectionFragment extends Fragment {

    private Collection mCollection;

    private class NavigationListener implements ActionBar.OnNavigationListener {

        private int mCurrentPosition = -1;

        public ArrayList<Collection> mCollections;

        public NavigationListener(ArrayList<Collection> collections) {
            mCollections = collections;
        }

        @Override
        public boolean onNavigationItemSelected(int position, long itemId) {
            if (mCurrentPosition < 0) {
                //we ignore the first call
                mCurrentPosition = position;
            } else if (position != mCurrentPosition) {
                mCurrentPosition = position;
                Bundle bundle = new Bundle();
                bundle.putString(CollectionManager.COLLECTION_ID,
                        mCollections.get(position).getId());
                FragmentUtils.replace(getActivity(), getActivity().getSupportFragmentManager(),
                        CollectionFragment.class, bundle);
                return true;
            }
            return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pagerfragment_layout, container, false);
    }

    /**
     * Called, when this {@link CollectionFragment}'s {@link View} has been created
     */
    @Override
    public void onResume() {
        super.onResume();

        if (getArguments() != null) {
            if (getArguments().containsKey(CollectionManager.COLLECTION_ID)) {
                mCollection = CollectionManager.getInstance()
                        .getCollection(getArguments().getString(CollectionManager.COLLECTION_ID));
            }
        }

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);
        ArrayList<CharSequence> collectionNames = new ArrayList<CharSequence>();
        int selectedSpinner = 0;
        ArrayList<Collection> collections = CollectionManager.getInstance().getCollections();
        for (int i = 0; i < collections.size(); i++) {
            Collection collection = collections.get(i);
            if (collection.getId().equals(TomahawkApp.PLUGINNAME_USERCOLLECTION)) {
                // Local collection should be always on top
                collections.add(0, collections.remove(i));
            } else if (collection.getId().equals(TomahawkApp.PLUGINNAME_HATCHET)) {
                // Don't show the hatchet collection
                collections.remove(i);
                i--;
            }
        }
        for (int i = 0; i < collections.size(); i++) {
            Collection collection = collections.get(i);
            collectionNames.add(collection.getName());
            if (collection.equals(mCollection)) {
                selectedSpinner = i;
            }
        }
        SpinnerAdapter spinnerAdapter = new ArrayAdapter<CharSequence>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, collectionNames);
        NavigationListener navigationListener = new NavigationListener(collections);
        actionBar.setListNavigationCallbacks(spinnerAdapter, navigationListener);
        actionBar.setSelectedNavigationItem(selectedSpinner);

        getActivity().setTitle(getString(R.string.usercollectionfragment_title_string));

        List<String> fragmentClassNames = new ArrayList<String>();
        if (mCollection.getId().equals(TomahawkApp.PLUGINNAME_USERCOLLECTION)) {
            fragmentClassNames.add(TracksFragment.class.getName());
        }
        fragmentClassNames.add(ArtistsFragment.class.getName());
        if (mCollection.getId().equals(TomahawkApp.PLUGINNAME_USERCOLLECTION)) {
            fragmentClassNames.add(AlbumsFragment.class.getName());
        }
        List<String> fragmentTitles = new ArrayList<String>();
        if (mCollection.getId().equals(TomahawkApp.PLUGINNAME_USERCOLLECTION)) {
            fragmentTitles.add(getString(R.string.tracksfragment_title_string));
        }
        fragmentTitles.add(getString(R.string.artistsfragment_title_string));
        if (mCollection.getId().equals(TomahawkApp.PLUGINNAME_USERCOLLECTION)) {
            fragmentTitles.add(getString(R.string.albumsfragment_title_string));
        }
        List<Bundle> fragmentBundles = new ArrayList<Bundle>();
        Bundle bundle = new Bundle();
        bundle.putString(CollectionManager.COLLECTION_ID, mCollection.getId());
        if (mCollection.getId().equals(TomahawkApp.PLUGINNAME_USERCOLLECTION)) {
            fragmentBundles.add(bundle);
            fragmentBundles.add(bundle);
        }
        fragmentBundles.add(bundle);
        TomahawkPagerAdapter adapter = new TomahawkPagerAdapter(getChildFragmentManager(),
                fragmentClassNames, fragmentTitles, fragmentBundles);
        ViewPager fragmentPager = (ViewPager) getActivity().findViewById(R.id.fragmentpager);
        fragmentPager.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
    }
}
