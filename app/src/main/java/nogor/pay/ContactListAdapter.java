package nogor.pay;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;

public class ContactListAdapter extends BaseAdapter {
    private Context context;
    private List<String> items;
    private LayoutInflater inflater;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public ContactListAdapter(Context context, List<String> items, OnDeleteClickListener deleteListener) {
        this.context = context;
        this.items = items;
        this.deleteListener = deleteListener;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.contact_list_item, parent, false);
            holder = new ViewHolder();
            holder.itemTextView = convertView.findViewById(R.id.itemTextView);
            holder.deleteButton = convertView.findViewById(R.id.deleteButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String item = items.get(position);
        holder.itemTextView.setText(item);

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(position);
                }
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView itemTextView;
        Button deleteButton;
    }
}