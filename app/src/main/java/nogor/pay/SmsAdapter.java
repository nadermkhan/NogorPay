package nogor.pay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class SmsAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HashMap<String, String>> smsList;
    private LayoutInflater inflater;

    public SmsAdapter(Context context, ArrayList<HashMap<String, String>> smsList) {
        this.context = context;
        this.smsList = smsList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return smsList.size();
    }

    @Override
    public Object getItem(int position) {
        return smsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.sms_list_item, parent, false);
            holder = new ViewHolder();
            holder.senderTextView = convertView.findViewById(R.id.senderTextView);
            holder.messageTextView = convertView.findViewById(R.id.messageTextView);
            holder.timestampTextView = convertView.findViewById(R.id.timestampTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        HashMap<String, String> sms = smsList.get(position);

        holder.senderTextView.setText(sms.get("sender"));
        holder.messageTextView.setText(sms.get("message"));


        String timestamp = sms.get("timestamp");
        if (timestamp != null) {
            try {
                long time = Long.parseLong(timestamp) * 1000; // Convert to milliseconds
                Date date = new Date(time);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                holder.timestampTextView.setText(sdf.format(date));
            } catch (NumberFormatException e) {
                holder.timestampTextView.setText(timestamp);
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView senderTextView;
        TextView messageTextView;
        TextView timestampTextView;
    }
}