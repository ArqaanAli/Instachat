package com.example.hp.instachat.Modules;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.google.firebase.FirebaseTooManyRequestsException;

class SectionPagerAdapter extends FragmentPagerAdapter {
    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position){
            case 0:

               ChatsFragment chatsFragment =new ChatsFragment();
               return chatsFragment;

            case 1:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;


            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;

        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){
        switch(position){
            case 0:
                return "CHATS";

            case 1:
                return "CALLS";

            case 2:
                return "FRIENDS";

            default:
                return null;
        }
    }
}
