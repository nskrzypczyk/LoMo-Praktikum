package com.example.praktikum1.statistics;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.praktikum1.Utils;

import java.util.ArrayList;
import java.util.List;

public class TraitTable {

    private TableLayout table;

    private TableRow titleRow;
    private TextView title;

    private List<String> labels;
    private List<TextView> valueTextViews = new ArrayList<>();

    public TraitTable(Context context, ViewGroup parent, String titleContent) {

        labels = new ArrayList<>();
        labels.add(Utils.TYPE_FLP_HIGH);
        labels.add(Utils.TYPE_FLP_LOW);
        labels.add(Utils.TYPE_LM_GPS);

        table = new TableLayout(context);
        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableParams.topMargin = 20;
        table.setLayoutParams(tableParams);

        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

        // Titel basteln
        titleRow = new TableRow(context);
        titleRow.setLayoutParams(tableRowParams);
        title = new TextView(context);
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setText(titleContent);
        titleRow.addView(title);
        table.addView(titleRow);

        // Für jeden TYPE (flp_high, ...) eine Zeile rendern
        for(String label : labels) {
            TableRow tr = new TableRow(context);
            titleRow.setLayoutParams(tableRowParams);

            TextView tv = new TextView(context);
            tv.setText(label);
            tr.addView(tv);

            TextView valueView = new TextView(context);
            valueView.setText("Kein Wert");
            valueTextViews.add(valueView);
            tr.addView(valueView);

            table.addView(tr);
        }

        parent.addView(table);
    }

    public void setContent(String[] content) {
        for(int i = 0; i < valueTextViews.size(); i++) {
            valueTextViews.get(i).setText(content[i]);
        }
    }
}
