package com.example.androidtrlts.Fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.androidtrlts.R;

import mehdi.sakout.aboutpage.AboutPage;


public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mehdi.sakout.aboutpage.Element versionElement = new mehdi.sakout.aboutpage.Element();
        versionElement.setTitle("Version v2.0.0-alpha");

        mehdi.sakout.aboutpage.Element adsElement = new mehdi.sakout.aboutpage.Element();
        adsElement.setTitle("Advertise with us");


        View aboutPage = new AboutPage(getContext())
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher_round)
                .setDescription(getString(R.string.app_description))
                .addItem(versionElement)
                //.addItem(adsElement)
                .addGroup("Connect with us")
                .addEmail("lazymacs017@gmail.com")
                .create();

        return aboutPage;
    }
}
