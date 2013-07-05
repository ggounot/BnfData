package eu.gounot.bnfdata.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import eu.gounot.bnfdata.data.Author;

public class WorkAdapter extends ArrayAdapter<Author.Work> {

    private Activity mActivity;
    private Author.Work mWorks[];

    public WorkAdapter(Activity activity, Author.Work works[]) {
        super(activity, android.R.layout.simple_list_item_2, android.R.id.text1, works);
        mActivity = activity;
        mWorks = works;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = mActivity.getLayoutInflater();
            rowView = inflater.inflate(android.R.layout.simple_list_item_2, null);
        }

        TextView titleTextView = (TextView) rowView.findViewById(android.R.id.text1);
        TextView descriptionTextView = (TextView) rowView.findViewById(android.R.id.text2);
        Author.Work work = mWorks[position];

        titleTextView.setText(work.getTitle());
        descriptionTextView.setText(work.getDescription());
        rowView.setTag(work.getArkName());

        return rowView;
    }
}
