package planet.info.skyline.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import planet.info.skyline.ShowJobFiles_Fragment;


/**
 * Created by Belal on 2/3/2016.
 */
//Extending FragmentStatePagerAdapter
public class TabsPagerAdapter_ShowJobFiles extends FragmentStatePagerAdapter {

    //integer to count number of tabs
    int tabCount;
    ArrayList<HashMap<String,String>> list;

      String jobtxt_id;
    //Constructor to the class
    public TabsPagerAdapter_ShowJobFiles(FragmentManager fm, int tabCount, String jobtxt_id) {
        super(fm);
        //Initializing tab count
        this.tabCount= tabCount;
        this.jobtxt_id= jobtxt_id;

    }

    //Overriding method getItem
    @Override
    public Fragment getItem(int position) {
        //Returning the current tabs
       /* switch (position) {
            case 0:*/
                Fragment f=new ShowJobFiles_Fragment();
                Bundle b=new Bundle();
                b.putString("tab",position+"");
                b.putString("jobtxt_id",jobtxt_id);
                f.setArguments(b);
               return  f;

           /* case 1:
                Fragment dialog_SearchBy=new ShowJobFiles_Fragment();
                Bundle b1=new Bundle();
                b1.putString("tab",position+"");
                b1.putString("jobtxt_id",jobtxt_id);
                dialog_SearchBy.setArguments(b1);
                return  dialog_SearchBy;
            case 2:
                Fragment Dialog_No_JobsAvailable=new ShowJobFiles_Fragment();
                Bundle b2=new Bundle();
                b2.putString("tab",position+"");
                b2.putString("jobtxt_id",jobtxt_id);
                Dialog_No_JobsAvailable.setArguments(b2);
                return  Dialog_No_JobsAvailable;
            default:
                return null;*/
        //}
    }

    //Overriden method getCount to get the number of tabs
    @Override
    public int getCount() {
        return tabCount;
    }
}
