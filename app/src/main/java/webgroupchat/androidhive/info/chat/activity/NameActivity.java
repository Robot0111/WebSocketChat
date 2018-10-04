package webgroupchat.androidhive.info.chat.activity;


import android.support.v4.app.Fragment;

import webgroupchat.androidhive.info.chat.Fragment.NameFragment;
import webgroupchat.androidhive.info.chat.Single.SingleFragmentActivity;

public class NameActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() { return new NameFragment();}

}
