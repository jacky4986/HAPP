package com.hypodiabetic.happ.integration;

import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.hypodiabetic.happ.MainActivity;
import com.hypodiabetic.happ.Objects.Integration;
import com.hypodiabetic.happ.R;
import com.hypodiabetic.happ.integration.Objects.ObjectToSync;
import com.hypodiabetic.happ.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Tim on 06/02/2016.
 */
public class InsulinIntegrationAppNotification {

    public Snackbar check(final View v){
        List<Integration> recentlyUpdated = Integration.getUpdatedInLastMins(1,"insulin_integration_app");

        if (recentlyUpdated.size() > 0) {

            Boolean error = false;
            String snackbarMsg="";
            final ArrayList<HashMap<String, String>> detailList = new ArrayList<>();

            for (Integration integration : recentlyUpdated) {
                ObjectToSync integrationWithDetails = new ObjectToSync(integration);
                HashMap<String, String> detailListItem = new HashMap<String, String>();

                if (integrationWithDetails.state.equals("delete_me")) {
                    integration.delete();
                } else {

                    if(integrationWithDetails.state.equals("error")) error = true;

                    switch (integrationWithDetails.happ_object_type){
                        case "bolus_delivery":
                            detailListItem.put("value", tools.formatDisplayInsulin(integrationWithDetails.value1, 2));
                            detailListItem.put("summary", integrationWithDetails.value3);
                            snackbarMsg += integrationWithDetails.state.toUpperCase() + ": " + tools.formatDisplayInsulin(integrationWithDetails.value1,2) + " " + integrationWithDetails.value3 + "\n";
                            break;

                        case "temp_basal":
                            detailListItem.put("value", tools.formatDisplayBasal(integrationWithDetails.value1, true));
                            detailListItem.put("summary", "(" + integrationWithDetails.value2 + "%) " + integrationWithDetails.value3 + "mins");
                            snackbarMsg += integrationWithDetails.state.toUpperCase() + ": " + tools.formatDisplayBasal(integrationWithDetails.value1, false) + " (" + integrationWithDetails.value2 + "%) " + integrationWithDetails.value3 + "mins\n";
                            break;
                    }
                    detailListItem.put("happObjectType", integrationWithDetails.happ_object_type);
                    detailListItem.put("state", integrationWithDetails.state.toUpperCase());
                    detailListItem.put("details", integrationWithDetails.details);
                    detailList.add(detailListItem);
                }
            }


            int snackbarLength = Snackbar.LENGTH_LONG;
            if (error) snackbarLength = Snackbar.LENGTH_INDEFINITE;

            Snackbar snackbar = Snackbar.make(v, snackbarMsg, snackbarLength);
            View snackbarView = snackbar.getView();
            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            //textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,10F);
            textView.setMaxLines(recentlyUpdated.size());
            snackbar.setAction("DETAILS", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog dialog = new Dialog(view.getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.integration_dialog);
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(true);

                    ListView list = (ListView) dialog.findViewById(R.id.integrationList);
                    mySimpleAdapter adapter = new mySimpleAdapter(MainActivity.getInstace(), detailList, R.layout.integration_list_layout_insulin_summary,
                            new String[]{"value", "summary", "state", "details", "happObjectType"},
                            new int[]{R.id.insulinSummaryAmount, R.id.insulinSummarySummary, R.id.insulinSummaryState, R.id.insulinSummaryDetails, R.id.insulinSummaryHappObjectType});
                    list.setAdapter(adapter);

                    dialog.show();
                }
            });

            return snackbar;
        } else {
            return null;
        }
    }

    public class mySimpleAdapter extends SimpleAdapter {
        private Context mContext;

        public mySimpleAdapter(Context context, List<HashMap<String, String>> items, int resource, String[] from, int[] to) {
            super(context, items, resource, from, to);
            this.mContext = context;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            ImageView imageView = (ImageView) view.findViewById(R.id.insulinSummaryStateImage);
            TextView textView = (TextView) view.findViewById(R.id.insulinSummaryState);
            switch (textView.getText().toString().toLowerCase()) {
                case "sent":
                    imageView.setBackgroundResource(R.drawable.arrow_right_bold_circle);
                    break;
                case "received":
                    imageView.setBackgroundResource(R.drawable.information);
                    break;
                case "delayed":
                    imageView.setBackgroundResource(R.drawable.clock);
                    break;
                case "delivered":
                case "set":
                case "canceled":
                    imageView.setBackgroundResource(R.drawable.checkbox_marked_circle);
                    break;
                case "error":
                    imageView.setBackgroundResource(R.drawable.alert_circle);
                    break;
                default:
                    imageView.setBackgroundResource(R.drawable.alert_circle);
                    break;
            }

            TextView happObject = (TextView) view.findViewById(R.id.insulinSummaryHappObjectType);
            TextView value = (TextView) view.findViewById(R.id.insulinSummaryAmount);
            switch (happObject.getText().toString()){
                case "bolus_delivery":
                    value.setBackgroundResource(R.drawable.insulin_treatment_round);
                    value.setMaxLines(1);
                    break;
                case "temp_basal":
                    value.setBackgroundResource(R.drawable.insulin_basal_square);
                    value.setMaxLines(2);
                    break;
            }
            return view;
        }
    }
}
