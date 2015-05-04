package byteshaft.com.recorder;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class SettingFragment extends Fragment implements CheckBox.OnCheckedChangeListener {
    
    private Helpers mHelpers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment, container, false);
        CheckBox checkbox = (CheckBox) view.findViewById(R.id.ch);
        checkbox.setOnCheckedChangeListener(this);
        mHelpers = new Helpers(getActivity().getApplicationContext());
        checkbox.setChecked(mHelpers.isCheckBoxEnabled());
        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {
            case R.id.ch:
                mHelpers.settingForCheckBox(isChecked);
                break;
        }
    }
}
