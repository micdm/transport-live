package com.micdm.transportlive.donate;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;

import com.micdm.transportlive.misc.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DonateManager {

    public static interface OnLoadProductsListener {
        public void onLoadProducts(List<DonateProduct> products);
    }

    private static final int BILLING_API_VERSION = 3;
    private static final String PURCHASE_TYPE = "inapp";
    private static final String[] IDS = new String[] {"1", "2", "3", "4"};

    private final BillingServiceConnection connection = new BillingServiceConnection(new BillingServiceConnection.OnServiceReadyListener() {
        @Override
        public void onServiceReady() {
            consumeStalePurchases();
            loadProducts();
        }
    });
    private List<DonateProduct> products;

    private final Context context;
    private final OnLoadProductsListener listener;

    public DonateManager(Context context, OnLoadProductsListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void init() {
        context.bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), connection, Context.BIND_AUTO_CREATE);
    }

    public void deinit() {
        context.unbindService(connection);
    }

    private void consumeStalePurchases() {
        List<String> tokens = getStalePurchaseTokens();
        if (tokens != null) {
            for (String token: tokens) {
                consumePurchase(token);
            }
        }
    }

    private List<String> getStalePurchaseTokens() {
        try {
            Bundle result = connection.getService().getPurchases(BILLING_API_VERSION, context.getPackageName(), PURCHASE_TYPE, null);
            if (result.getInt("RESPONSE_CODE") != 0) {
                return null;
            }
            List<String> tokens = new ArrayList<String>();
            List<String> orders = result.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
            for (String order: orders) {
                tokens.add(new JSONObject(order).getString("purchaseToken"));
            }
            return tokens;
        } catch (RemoteException e) {
            return null;
        } catch (JSONException e) {
            return null;
        }
    }

    private void loadProducts() {
        AsyncTask<String[], Void, List<DonateProduct>> task = new AsyncTask<String[], Void, List<DonateProduct>>() {
            @Override
            protected List<DonateProduct> doInBackground(String[]... ids) {
                Bundle request = getLoadProductsRequestBundle(ids[0]);
                try {
                    Bundle result = connection.getService().getSkuDetails(BILLING_API_VERSION, context.getPackageName(), PURCHASE_TYPE, request);
                    if (result.getInt("RESPONSE_CODE") != 0) {
                        return null;
                    }
                    return getProducts(result.getStringArrayList("DETAILS_LIST"));
                } catch (RemoteException e) {
                    return null;
                } catch (JSONException e) {
                    return null;
                }
            }
            private Bundle getLoadProductsRequestBundle(String[] ids) {
                Bundle result = new Bundle();
                result.putStringArrayList("ITEM_ID_LIST", new ArrayList<String>(Arrays.asList(ids)));
                return result;
            }
            private List<DonateProduct> getProducts(List<String> datas) throws JSONException {
                List<DonateProduct> products = new ArrayList<DonateProduct>();
                for (String data: datas) {
                    JSONObject json = new JSONObject(data);
                    products.add(new DonateProduct(json.getString("productId"), json.getString("price"), getProductTitle(json.getString("title"))));
                }
                return products;
            }
            private String getProductTitle(String title) {
                return title.replace(String.format(" (%s)", Utils.getAppTitle(context)), "");
            }
            @Override
            protected void onPostExecute(List<DonateProduct> loaded) {
                products = loaded;
                listener.onLoadProducts(loaded);
            }
        };
        task.execute(IDS);
    }

    public PendingIntent getDonateIntent(DonateProduct product) {
        try {
            Bundle result = connection.getService().getBuyIntent(BILLING_API_VERSION, context.getPackageName(), product.getId(), PURCHASE_TYPE, "");
            if (result.getInt("RESPONSE_CODE") != 0) {
                return null;
            }
            return result.getParcelable("BUY_INTENT");
        } catch (RemoteException e) {
            return null;
        }
    }

    public void handleDonate(String data) {
        String token = getPurchaseToken(data);
        if (token != null) {
            consumePurchase(token);
        }
    }

    private String getPurchaseToken(String data) {
        try {
            JSONObject json = new JSONObject(data);
            return json.getString("purchaseToken");
        } catch (JSONException e) {
            return null;
        }
    }

    private void consumePurchase(String token) {
        AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... tokens) {
                try {
                    connection.getService().consumePurchase(BILLING_API_VERSION, context.getPackageName(), tokens[0]);
                } catch (RemoteException e) {

                }
                return null;
            }
        };
        task.execute(token);
    }
}
