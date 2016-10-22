package com.mateyinc.marko.matey.activity.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.internet.SessionManager;

/**
 * Created by Sarma on 8/27/2016.
 */
public class MenuFragment extends Fragment {

    // Items are dynamically added to list, tracking their ids based on their list position
    private static final int PROFILE_ITEM_ID = 0;
    private static final int INTERESTS_ITEM_ID = 1;
    private static final int GROUPS_ITEM_ID = 2;
    private static final int FEEDBACK_ITEM_ID = 3;
    private static final int LOGOUT_ITEM_ID = 4;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * NotificationsFragment callback for when an item has been selected.
         */
        public void onItemSelected();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MenuFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notif_msg, container, false);
        ListView listView = (ListView) rootView;

        String[] resArray = getResources().getStringArray(R.array.menu_items);
//        List<String> myResArrayList = Arrays.asList(resArray);
//        List<String> myResMutableList = new ArrayList<String>(resArray);

        // TODO - set custom adapters
        listView.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, resArray));
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case PROFILE_ITEM_ID: {
                        Intent i = new Intent(MenuFragment.this.getContext(), ProfileActivity.class);
                        startActivity(i);
                        break;
                    }
//                    case INTERESTS_ITEM_ID: {
//                        Intent i = new Intent(MenuFragment.this.getContext(), InterestsActivity.class);
//                        startActivity(i);
//                        break;
//                    }
                    case GROUPS_ITEM_ID: {
                        Intent i = new Intent(MenuFragment.this.getContext(), GroupsActivity.class);
                        startActivity(i);
                        break;
                    }
//                    case FEEDBACK_ITEM_ID: {
//                        Intent i = new Intent(MenuFragment.this.getContext(), FeedbackActivity.class);
//                        startActivity(i);
//                        break;
//                    }
                    case LOGOUT_ITEM_ID: {
                        SessionManager.logout((HomeActivity)MenuFragment.this.getActivity(), ((HomeActivity)MenuFragment.this.getActivity()).getSecurePreferences());
                        break;
                    }
                    default:
                        return;
                }
            }
        });
        return rootView;
    }
}
