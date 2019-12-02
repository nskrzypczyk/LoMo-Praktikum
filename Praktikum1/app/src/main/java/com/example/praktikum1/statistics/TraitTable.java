package com.example.praktikum1.statistics;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.praktikum1.Utils;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class TraitTable {

    private TableLayout table;

    private TableRow titleRow;
    private TextView title;

    private List<String> labels;
    private List<TextView> valueTextViews = new ArrayList<>();

    @Getter @Setter
    private String suffix = "m";

    public TraitTable(Context context, ViewGroup parent, String titleContent) {

        labels = new ArrayList<>();
        labels.add(Utils.TYPE_FLP_HIGH);
        labels.add(Utils.TYPE_FLP_LOW);
        labels.add(Utils.TYPE_LM_GPS);

        table = new TableLayout(context);
        table.setColumnStretchable(1, true);
        LinearLayout.LayoutParams tableParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tableParams.topMargin = 30;
        table.setLayoutParams(tableParams);

        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

        // Ist für das TextView, welches den Title anzeigt. Dieses bekommt eine colpsan von 2
        TableRow.LayoutParams tableTitleParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        tableTitleParams.span = 2;

        // Titel basteln
        titleRow = new TableRow(context);
        titleRow.setLayoutParams(tableRowParams);
        title = new TextView(context);
        title.setAllCaps(true);
        title.setTypeface(null, Typeface.BOLD);
        title.setText(titleContent);
        title.setLayoutParams(tableTitleParams);
        titleRow.addView(title);
        table.addView(titleRow);

        // Für jeden TYPE (flp_high, ...) eine Zeile rendern
        for(String label : labels) {
            TableRow tr = new TableRow(context);
            tr.setLayoutParams(tableRowParams);

            TableRow.LayoutParams labelLayout = new TableRow.LayoutParams();
            labelLayout.rightMargin = 30;

            TextView tv = new TextView(context);
            tv.setText(label);
            tv.setLayoutParams(labelLayout);
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
            if(!content[i].isEmpty()) {
                valueTextViews.get(i).append(suffix);
            }
        }
    }
}
