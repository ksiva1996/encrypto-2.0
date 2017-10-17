package com.leagueofshadows.encrypto;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FilePicker extends AppCompatActivity implements Com{

    public final static String EXTRA_FILE_PATH = "file_path";
    private final static String DEFAULT_INITIAL_DIRECTORY = Util.sdcard;

    RecyclerView recyclerView;
    protected File directory;
    protected ArrayList<File> files;
    protected FileAdapter adapter;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(DEFAULT_INITIAL_DIRECTORY);
        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView)findViewById(R.id.list);
        LinearLayoutManager lnm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lnm);
        files = new ArrayList<>();
        adapter = new FileAdapter(files,this);
        recyclerView.setAdapter(adapter);
        directory = new File(DEFAULT_INITIAL_DIRECTORY);
        refresh(directory);
    }


    @Override
    public void refresh(File file) {
        files.clear();
        directory = file;
        toolbar.setTitle(file.getAbsolutePath());
        File[] list = file.listFiles();
        if(list!=null && list.length!=0) {
            for (File f : list) {
                if (!f.isHidden())
                    files.add(f);
            }
            Collections.sort(files, new FileComp());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void result(File file) {
        Intent i = new Intent(this,MainActivity.class);
        i.putExtra(EXTRA_FILE_PATH,file.getAbsolutePath());
        setResult(RESULT_OK,i);
        finish();
    }

    @Override
    public void onBackPressed() {
        if(directory.getAbsolutePath().equals(DEFAULT_INITIAL_DIRECTORY))
        {
            super.onBackPressed();
        }
        else
        {
            File file = directory.getParentFile();
            refresh(file);
        }
    }

    private class FileComp implements Comparator<File> {

        @Override
        public int compare(File o1, File o2) {
            if(o1.isFile()&&o2.isFile())
                return o1.getName().compareTo(o2.getName());
            if(!o1.isFile()&&!o2.isFile())
                return o1.getName().compareTo(o2.getName());
            else if(!o1.isFile())
                return -1;
            else
                return 1;

        }
        @Override
        public boolean equals(Object obj) {
            return false;
        }
    }
}

class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder>
{
    private ArrayList<File> files;
    private Context context;

    FileAdapter(ArrayList<File> files, Context context)
    {
        this.files=files;
        this.context=context;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        LinearLayout container;
        ImageView image;
        ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.file_picker_text);
            container = (LinearLayout)v.findViewById(R.id.container);
            image = (ImageView)v.findViewById(R.id.file_picker_image);
        }
    }


    @Override
    public FileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileAdapter.ViewHolder holder, int position) {
        final File file = files.get(position);
        if(file.isFile())
            holder.image.setImageResource(R.drawable.ic_insert_drive_file_black_24dp);
        else
            holder.image.setImageResource(R.drawable.ic_folder_black_24dp);
        holder.textView.setText(file.getName());
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Com com = (Com) context;
                if(file.isFile())
                    com.result(file);
                else {
                    com.refresh(file);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }
}