package com.example.bhelvisualizer.adapter;
// FragmentPagerAdapter.java
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.bhelvisualizer.ReportFragment;
import com.example.bhelvisualizer.ViewFilesFragment;

public class FragmentPagerAdapter extends FragmentStateAdapter {

    public FragmentPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ReportFragment();
            case 1:
                return new ViewFilesFragment();
            default:
                return new ReportFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Number of fragments
    }
}

