package com.example.evan.scout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.FileObserver;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.Comparator;

//class to handle all interactions with the listview of sent files
public class FileListAdapter extends ArrayAdapter<String> {
    private ListView fileList;
    private final MainActivity context;
    private FileObserver fileObserver;
    private String superName;
    public FileListAdapter(MainActivity context, ListView fileList,  String uuid, String superName) {
        super(context, android.R.layout.simple_list_item_1);
        this.context = context;
        this.fileList = fileList;
        this.superName = superName;
        initFileList(uuid);
    }
    //set up list view
    private void initFileList(final String uuid) {
        fileList.setAdapter(this);
        updateListView();
        //when you click on a file, it sends it
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String name = parent.getItemAtPosition(position).toString();
                //read data from file
                String text = Utils.readFile(context, name);
                if (text != null) {
                    String sendData;
                    try {
                        LocalTeamInMatchData previousData = (LocalTeamInMatchData)Utils.deserializeClass(text, LocalTeamInMatchData.class);
                        sendData = Utils.serializeClass(previousData.getFirebaseData());
                    } catch (Exception e) {
                        sendData = null;
                    }
                    Log.i("JSON before send", context.wrapJson(sendData));
                    if (sendData == null) {
                        Log.e("Json Error", "Failed to convert matchData to sendData");
                        Toast.makeText(context, "Error in send data", Toast.LENGTH_LONG).show();
                        return;
                    }
                    new ConnectThread(context, superName, uuid, new ConnectThread.ConnectThreadData(name, text, context.wrapJson(sendData))).start();
                }
            }
        });
        //if you click and hold on a file, give some options
        fileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(context)
                        //you can resend
                        .setTitle("File Options")
                        .setPositiveButton("Resend", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String name = parent.getItemAtPosition(position).toString();
                                //read data from file
                                String text = Utils.readFile(context, name);
                                if (text != null) {
                                    String sendData;
                                    try {
                                        LocalTeamInMatchData previousData = (LocalTeamInMatchData)Utils.deserializeClass(text, LocalTeamInMatchData.class);
                                        sendData = Utils.serializeClass(previousData.getFirebaseData());
                                    } catch (Exception e) {
                                        sendData = null;
                                    }
                                    Log.i("JSON before send", context.wrapJson(sendData));
                                    if (sendData == null) {
                                        Log.e("Json Error", "Failed to convert matchData to sendData");
                                        Toast.makeText(context, "Error in send data", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    new ConnectThread(context, superName, uuid, new ConnectThread.ConnectThreadData(name, text, context.wrapJson(sendData))).start();
                                }
                            }
                        })
                                //you can cancel
                        .setNeutralButton("Cancel", null)
                                //or you can edit
                        .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String name = parent.getItemAtPosition(position).toString();
                                //first read from file
                                String text = Utils.readFile(context, name);
                                if (text != null) {
                                    //next get team and matchnumber from filename
                                    int tmpTeam;
                                    int tmpMatch;
                                    try {
                                        tmpMatch = Integer.parseInt(name.replaceFirst("UNSENT_", "").split("_")[0].replaceAll("Q", ""));
                                        tmpTeam = Integer.parseInt(name.replaceFirst("UNSENT_", "").split("_")[1]);
                                    } catch (NumberFormatException nfe) {
                                        Log.e("File Error", "failed to parse data from file name");
                                        Toast.makeText(context, "Not a valid File", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    Log.i("JSON before edit", text);
                                    //call the onclick for the 'scout' button to move on to next activity, only when it changes it will keep data
                                    //(see startScout method)
                                    context.startScout(text, tmpMatch, tmpTeam);
                                }
                            }
                        })
                        .show();
                return true;
            }
        });



        //update list view when something is renamed or created
        fileObserver = new FileObserver(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData") {
            @Override
            public void onEvent(int event, String path) {
                if ((event == FileObserver.MOVED_TO) || (event == FileObserver.CREATE)) {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateListView();
                        }
                    });
                }
            }
        };
        fileObserver.startWatching();
    }
    public void updateListView() {
        File dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData/");
        if (!dir.mkdir()) {
            Log.i("File Info", "Failed to make Directory. Unimportant");
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        this.clear();
        for (File tmpFile : files) {
            this.add(tmpFile.getName());
        }
        //sort so unsent files are at the top, and then the rest come by match number
        this.sort(new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return -1*_compare(lhs, rhs);
            }
            public int _compare(String lhs, String rhs) {
                if (lhs.startsWith("UNSENT_")) {
                    if (rhs.startsWith("UNSENT_")) {
                        //both unsent, continue
                        rhs = rhs.replaceFirst("UNSENT_", "");
                        lhs = lhs.replaceFirst("UNSENT_", "");
                    } else {
                        //lhs greater, return
                        return 1;
                    }
                } else if (rhs.startsWith("UNSENT_")) {
                    //rhs greater, return
                    return -1;
                }
                int lhsNum;
                int rhsNum;
                try {
                    lhsNum = Integer.parseInt((lhs.split("_"))[0].replaceAll("Q", ""));
                    rhsNum = Integer.parseInt((rhs.split("_"))[0].replaceAll("Q", ""));
                } catch (NumberFormatException nfe) {
                    return 0;
                }
                if (lhsNum < rhsNum) {
                    //lhs greater, return
                    return -1;
                } else if (lhsNum == rhsNum) {
                    //equal, return
                    return 0;
                } else {
                    //rhs greater, return
                    return 1;
                }
            }
        });


        this.notifyDataSetChanged();
    }
    //filter list entries by string
    public void filterListView(String key) {
        key = key.toUpperCase();
        for (int i = 0; i < this.getCount();) {
            int tmpTeam;
            int tmpMatch;
            try {
                tmpTeam = Integer.parseInt((this.getItem(i).split("_"))[0].replaceAll("Q", ""));
                tmpMatch = Integer.parseInt((this.getItem(i).split("_"))[1]);
            } catch (NumberFormatException nfe) {
                tmpTeam = -1;
                tmpMatch = -1;
            }
            //we only want to keep the entry if it starts with the key, the key is the teamnumber in the entry, or the key is matchnumber in the entry
            if ((this.getItem(i).startsWith(key)) || (key.equals(Integer.toString(tmpMatch)))
                    || (key.equals(Integer.toString(tmpTeam)))) {
                i++;
            } else {
                this.remove(this.getItem(i));
            }
        }
        this.notifyDataSetChanged();
    }

    public void stopFileObserver() {
        fileObserver.stopWatching();
    }


    public void setSuperName(String superName) {
        this.superName = superName;
    }
}
