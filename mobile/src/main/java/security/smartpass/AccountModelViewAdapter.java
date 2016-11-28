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


    private Activity activity;
    private List data;
    private static LayoutInflater inflater=null;
    public Resources res;
    AccountModel tempValues=null;
    int i=0;

    public AccountModelViewAdapter(Activity a, List d, Resources resLocal) {

        activity = a;
        data=d;
        res = resLocal;

        inflater = ( LayoutInflater )activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

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

    public static class ViewHolder{

        public TextView appName;
        public TextView userName;
        public TextView appId;
        public TextView appUrl;
        public ImageView image;

    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;

        if(convertView==null){

            vi = inflater.inflate(R.layout.tabitem, null);

            holder = new ViewHolder();
            holder.appId = (TextView) vi.findViewById(R.id.appId);
            holder.appName = (TextView) vi.findViewById(R.id.appName);
            holder.userName = (TextView) vi.findViewById(R.id.userName);
            holder.image=(ImageView)vi.findViewById(R.id.image);

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
            tempValues=null;
            tempValues = (AccountModel) data.get( position );

            holder.appName.setText( tempValues.getAppName() );
            holder.appId.setText( tempValues.getAppId() );
            holder.userName.setText( tempValues.getUserName());
            holder.image.setImageResource(R.drawable.ic_money);

            vi.setOnClickListener(new OnItemClickListener( position ));
        }
        return vi;
    }
    @Override
    public void onClick(View v) {
        Log.v("CustomAdapter", "=====Row button clicked=====");
    }

    private class OnItemClickListener  implements View.OnClickListener {
        private int mPosition;

        OnItemClickListener(int position){
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {
            Main2Activity sct = (Main2Activity)activity;
            sct.onItemClick(mPosition);
        }
    }

}
