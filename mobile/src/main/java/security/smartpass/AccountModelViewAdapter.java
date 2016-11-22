package security.smartpass;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chuong on 11/1/2016.
 */

public class AccountModelViewAdapter extends BaseAdapter implements View.OnClickListener {


    /*********** Declare Used Variables *********/
    private Activity activity;
    private List data;
    private static LayoutInflater inflater=null;
    public Resources res;
    AccountModel tempValues=null;
    int i=0;

    /*************  CustomAdapter Constructor *****************/
    public AccountModelViewAdapter(Activity a, List d, Resources resLocal) {

        /********** Take passed values **********/
        activity = a;
        data=d;
        res = resLocal;

        /***********  Layout inflator to call external xml layout () ***********/
        inflater = ( LayoutInflater )activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    /******** What is the size of Passed Arraylist Size ************/
    public int getCount() {

        if(data.size()<=0)
            return 1;
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    /********* Create a holder Class to contain inflated xml file elements *********/
    public static class ViewHolder{

        public TextView appName;
        public TextView userName;
        public TextView appId;
        public TextView appUrl;
        public ImageView image;

    }

    /****** Depends upon data size called for each row , Create each ListView row *****/
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;

        if(convertView==null){

            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            vi = inflater.inflate(R.layout.tabitem, null);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.appId = (TextView) vi.findViewById(R.id.appId);
            holder.appName = (TextView) vi.findViewById(R.id.appName);
            holder.userName = (TextView) vi.findViewById(R.id.userName);
            holder.image=(ImageView)vi.findViewById(R.id.image);

            /************  Set holder with LayoutInflater ************/
            vi.setTag( holder );
        }
        else
            holder=(ViewHolder)vi.getTag();

        if(data.size()<=0)
        {
            holder.appName.setText("No Data");

        }
        else
        {
            /***** Get each Model object from Arraylist ********/
            tempValues=null;
            tempValues = (AccountModel) data.get( position );

            /************  Set Model values in Holder elements ***********/

            holder.appName.setText( tempValues.getAppName() );
            holder.appId.setText( tempValues.getAppId() );
            holder.userName.setText( tempValues.getUserName());
            holder.image.setImageResource(
                    res.getIdentifier(
                            "com.androidexample.customlistview:drawable/"+tempValues.getImage()
                            ,null,null));

            /******** Set Item Click Listner for LayoutInflater for each row *******/

            vi.setOnClickListener(new OnItemClickListener( position ));
        }
        return vi;
    }
    @Override
    public void onClick(View v) {
        Log.v("CustomAdapter", "=====Row button clicked=====");
    }

    /********* Called when Item click in ListView ************/
    private class OnItemClickListener  implements View.OnClickListener {
        private int mPosition;

        OnItemClickListener(int position){
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {
            Main2Activity sct = (Main2Activity)activity;
            /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/
            sct.onItemClick(mPosition);
        }
    }

}
