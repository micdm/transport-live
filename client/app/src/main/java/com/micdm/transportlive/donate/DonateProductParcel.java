package com.micdm.transportlive.donate;

import android.os.Parcel;
import android.os.Parcelable;

public class DonateProductParcel implements Parcelable {

    public static final Creator<DonateProduct> CREATOR = new Creator<DonateProduct>() {

        public DonateProduct createFromParcel(Parcel in) {
            String id = in.readString();
            String price = in.readString();
            String title = in.readString();
            return new DonateProduct(id, price, title);
        }

        public DonateProduct[] newArray(int size) {
            return new DonateProduct[size];
        }
    };

    private final DonateProduct product;

    public DonateProductParcel(DonateProduct product) {
        this.product = product;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(product.getId());
        dest.writeString(product.getPrice());
        dest.writeString(product.getTitle());
    }

    public DonateProduct getProduct() {
        return product;
    }
}
