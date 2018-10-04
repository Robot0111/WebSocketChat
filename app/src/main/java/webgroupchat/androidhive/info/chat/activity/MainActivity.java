package webgroupchat.androidhive.info.chat.activity;

import android.support.v4.app.Fragment;

import webgroupchat.androidhive.info.chat.Fragment.MainFragment;
import webgroupchat.androidhive.info.chat.Single.SingleFragmentActivity;


public class MainActivity extends SingleFragmentActivity {

    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String NAME="webgroupchat.androidhive.info.chat.other.name";

    @Override
    protected Fragment createFragment() {
        String name = getIntent().getStringExtra(NAME);
        return  MainFragment.newInstance(name);
    }
}
