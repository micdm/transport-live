package com.micdm.transportlive.donate;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;
import com.micdm.transportlive.misc.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoadItemsTask extends AsyncTask<String[], Void, List<DonateItem>> {

    public static interface OnLoadItemsListener {
        public void onLoadItems(List<DonateItem> items);
    }

    private final Context context;
    private final IInAppBillingService service;
    private final OnLoadItemsListener listener;

    public LoadItemsTask(Context context, IInAppBillingService service, OnLoadItemsListener listener) {
        this.context = context;
        this.service = service;
        this.listener = listener;
    }

    @Override
    protected List<DonateItem> doInBackground(String[]... ids) {
        Bundle request = getLoadItemsRequestBundle(ids[0]);
        try {
            Bundle result = service.getSkuDetails(DonateManager.BILLING_API_VERSION, context.getPackageName(), DonateManager.PURCHASE_TYPE, request);
            if (result.getInt("RESPONSE_CODE") != 0) {
                return null;
            }
            return parseResult(result.getStringArrayList("DETAILS_LIST"));
        } catch (RemoteException e) {
            return null;
        } catch (JSONException e) {
            return null;
        }
    }

    private Bundle getLoadItemsRequestBundle(String[] ids) {
        Bundle result = new Bundle();
        result.putStringArrayList("ITEM_ID_LIST", new ArrayList<String>(Arrays.asList(ids)));
        return result;
    }

    private List<DonateItem> parseResult(List<String> datas) throws JSONException {
        List<DonateItem> items = new ArrayList<DonateItem>();
        for (String data: datas) {
            JSONObject json = new JSONObject(data);
            items.add(new DonateItem(json.getString("productId"), json.getString("price"), getItemTitle(json.getString("title"))));
        }
        return items;
    }

    private String getItemTitle(String title) {
        return title.replace(String.format(" (%s)", Utils.getAppTitle(context)), "");
    }

    @Override
    protected void onPostExecute(List<DonateItem> items) {
        listener.onLoadItems(items);
    }
}
