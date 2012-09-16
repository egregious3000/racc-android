package net.example.raccoonclient;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

public class ForumListAdapter extends ArrayAdapter<Forum> {


    /*
    public Forum getByPosition(int position)
    {
        return _items.get(0); // position
    }*/
    
     private final String TAG = "CLA";
    ArrayList<Forum> _items = new ArrayList<Forum>();
    ArrayList<Forum> _filtered; 
    private Context _context;
    private Filter _filter;
    
    @SuppressWarnings("unchecked")
    public ForumListAdapter(Context context, int resource, ArrayList<Forum> objects) {    
        super(context, resource, objects);
        Log.e(TAG, "in here");
        try {
            _context = context;
            _filtered = (ArrayList<Forum>) objects.clone();
            _items = (ArrayList<Forum>) objects.clone();
        } catch (Exception e) {
            Log.e(TAG, "ctor exception:", e);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.item, null);
        }
        try {
            Forum f = _filtered.get(position);
            TextView tv = (TextView) v;
            tv.setText(f._name);
        } catch (Exception e) {
            Log.e(TAG, "getview got exception: ", e);
        }
        return v;
    }

    @Override
    public Filter getFilter()
    {
        if(_filter == null) {
            _filter = new ForumListFilter();
        }
        return _filter;
    }

    private class ForumListFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults result = new FilterResults();
            ArrayList<Forum> ret = new ArrayList<Forum>();
            ArrayList<Forum> lItems = new ArrayList<Forum>();
            synchronized (this) {
                lItems.addAll(_items);
            }  
            result.count = lItems.size();
            result.values = lItems;
            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                FilterResults results) {
            _filtered = (ArrayList<Forum>) results.values;
            notifyDataSetChanged();
            clear();
            try {
                for(int i = 0, l = _filtered.size(); i < l; i++)
                    add(_filtered.get(i));
            } catch (Exception e) {
                Log.e(TAG, "filtering problem, no results?", e);
            }
            notifyDataSetInvalidated();
        }
    }
}
