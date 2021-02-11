package com.example.androidtrlts.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.androidtrlts.Activities.MainActivity;
import com.example.androidtrlts.Activities.TextEditorActivity;
import com.example.androidtrlts.Adapters.ItemAdapter;
import com.example.androidtrlts.Helpers.FileHelper;
import com.example.androidtrlts.Helpers.SessionHelper;
import com.example.androidtrlts.Model.Item;
import com.example.androidtrlts.R;
import com.example.androidtrlts.Utils.FileList;
import com.example.androidtrlts.Utils.Route;
import com.example.androidtrlts.Utils.Util;

import java.io.File;
import java.util.List;

public class BrowseFragment extends Fragment {
    private View mView;
    private List<Item> items;
    private FileList fileList;

    private ImageView backBtn;
    private TextView currentPathView;
    private ListView listView;
    private TextView empty;
    private SwipeRefreshLayout refreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.main_content, container, false);
        currentPathView = mView.findViewById(R.id.current_path);
        backBtn = mView.findViewById(R.id.back);
        empty = mView.findViewById(R.id.empty);
        listView = (ListView) mView.findViewById(R.id.listView);
        refreshLayout = mView.findViewById(R.id.refreshLayout);

        fileList = new FileList(getActivity(), listView, empty);

        // toolbar
        ((MainActivity)getActivity()).getSupportActionBar()
                .setTitle(fileList.getCurrentFolderName().replace(Route.ROOT, "Home"));
        // bread crumbs
        currentPathView.setText(fileList.getPathOriginRoot().replace(Route.ROOT, "Home"));

        // hide back button if the current directory is root and access outside root is not permissible
        if(!FileList.allowOutsideRootAccess &&
                Util.removeTrailingChar(FileList.currentDirPath, "/").equals(Route.getFullPath())){
            backBtn.setVisibility(View.GONE);
        }

        items = fileList.load(FileList.currentDirPath);
        FileList.sort(items, MainActivity.order, MainActivity.property, false);
        fileList.attach(items);

        if(FileList.action == Util.Action.CLOSE){
            back();
            FileList.action = Util.Action.OPEN;
        }

        backBtn.setOnClickListener(v -> {
            back();
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if(items.get(position).getType().toString().equals(ItemAdapter.Types.FOLDER.toString())){

                FileList.next(items.get(position).getName()); // change directory
                fileList.reloadListView(FileList.currentDirPath); // reload list view with new  items

                // toolbar
                ((MainActivity)getActivity()).getSupportActionBar().setTitle(fileList.getCurrentFolderName());
                // bread crumbs
                currentPathView.setText(fileList.getPathOriginRoot().replace(Route.ROOT, "Home"));

                FileList.allowDirAccess();
                backBtn.setVisibility(View.VISIBLE);
            }else{
                String name = FileList.currentDirPath+"/"+items.get(position).getName();
                File file = new File(name);
                String text = FileHelper.read(file);
                String title = FileHelper.getName(file);

                TextEditorActivity.filePath = file.toString();

                Intent intent = new Intent(getActivity(), TextEditorActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("extractedText", text);
                bundle.putString("title", title);
                intent.putExtras(bundle);
                getActivity().startActivity(intent);
            }

        });

        refreshLayout.setOnRefreshListener(() -> {
            fileList.reloadListView(FileList.currentDirPath); // reload list view
            refreshLayout.setRefreshing(false);
        });

        return mView;
    }

    public void back(){
        String target = Util.getCharsFromPrev(Util.removeTrailingChar(FileList.currentDirPath, "/"), "/");

        if(Util.removeTrailingChar(target, "/") .equals(Route.getFullPath()) && !FileList.allowOutsideRootAccess){
            backBtn.setVisibility(View.GONE);
        }

        if(FileList.dirAccess()){
            return;
        }

        FileList.previous();
        String targetParent = Util.getCharsFromPrev( Util.removeTrailingChar(FileList.currentDirPath, "/"),"/");

        ((MainActivity)getActivity()).getSupportActionBar()
                .setTitle(fileList.getCurrentFolderName().replace(Route.ROOT, "Home"));
        // bread crumbs
        currentPathView.setText(fileList.getPathOriginRoot().replace(Route.ROOT, "Home"));

        fileList.reloadListView(FileList.currentDirPath);

        if(items == null){
            FileList.StopDirAccess();
            backBtn.setVisibility(View.GONE);
        }

        if(targetParent.equals(FileList.SYMLINK)){
            FileList.StopDirAccess();
            backBtn.setVisibility(View.GONE);
        }
    }

}
