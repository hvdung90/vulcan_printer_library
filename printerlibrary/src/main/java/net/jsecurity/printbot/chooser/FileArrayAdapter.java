package net.jsecurity.printbot.chooser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

import net.jsecurity.printbot.R;
import net.jsecurity.printbot.model.GUIConstants;

public class FileArrayAdapter extends ArrayAdapter<Option> {
    private Context c;
    private int id;
    private List<Option> items;

    public FileArrayAdapter(Context context, int textViewResourceId, List<Option> objects) {
        super(context, textViewResourceId, objects);
        this.c = context;
        this.id = textViewResourceId;
        this.items = objects;
    }

    @Override // android.widget.ArrayAdapter
    public Option getItem(int i) {
        return this.items.get(i);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = ((LayoutInflater) this.c.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(this.id, (ViewGroup) null);
        }
        Option o = this.items.get(position);
        if (o != null) {
            ImageView imageView = (ImageView) v.findViewById(R.id.ImageView01);
            if (imageView != null) {
                String type = o.getType();
                if (FileChooser.TYPE_FOLDER.equals(type)) {
                    imageView.setImageResource(R.drawable.folder);
                } else if (GUIConstants.APPLICATION_PDF.equals(type)) {
                    imageView.setImageResource(R.drawable.pdf);
                } else if (type != null && type.startsWith("image/")) {
                    imageView.setImageResource(R.drawable.image);
                } else if (type == null || !type.startsWith("text/")) {
                    imageView.setImageResource(R.drawable.blank);
                } else {
                    imageView.setImageResource(R.drawable.text);
                }
            }
            TextView textView = (TextView) v.findViewById(R.id.TextView01);
            if (textView != null) {
                textView.setText(o.getName());
            }
        }
        return v;
    }
}
