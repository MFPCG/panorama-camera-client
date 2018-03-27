package pers.liufushihai.panocamclient.fragment;

import java.util.HashMap;

/**
 * Date        : 2018/3/27
 * Author      : liufushihai
 * Description : Fragment工厂类，用于生产Fragment对象
 */

public class FragmentFactory {

    private static HashMap<Integer,BaseFragment> mBaseFragments
            = new HashMap<Integer,BaseFragment>();

    public static BaseFragment createFragment(int pos){
        BaseFragment baseFragment = mBaseFragments.get(pos);

        if(baseFragment == null){
            switch (pos){
                case 0:
                    baseFragment = new DevPicFragment();
                    break;
                case 1:
                    baseFragment = new SettingFragment();
                    break;
            }
            mBaseFragments.put(pos,baseFragment);
        }
        return baseFragment;
    }
}
