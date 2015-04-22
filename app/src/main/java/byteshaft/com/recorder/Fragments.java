package byteshaft.com.recorder;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public  class Fragments extends ListFragment {

    static int fragmentValue;
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static Fragments newInstance(int sectionNumber , int value) {
        Fragments fragment = new Fragments();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        fragmentValue = value;
        return fragment;
    }

    public Fragments() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = null;
        if (fragmentValue == 0) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
            setListAdapter(MainActivity.modeAdapter);
        } else if (fragmentValue ==1) {
            rootView = inflater.inflate(R.layout.fragment, container, false);
        }
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}


