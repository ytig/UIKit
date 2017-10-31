package com.vdian.sample.refresh.network;

/**
 * Created by zhangliang on 16/11/14.
 */
public class Data {
    private int id;

    public Data(int id) {
        this.id = id;
    }

    public Data(Data data) {
        this.id = data.id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Data) return id == ((Data) obj).id;
        return false;
    }
}
