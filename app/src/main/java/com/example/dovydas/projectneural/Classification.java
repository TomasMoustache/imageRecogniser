package com.example.dovydas.projectneural;

/**
 * Created by Dovydas on 2018.01.09.
 */
public class Classification {

    private float conf;

    private String label;

    Classification() {
        this.conf = -1.0F;
        this.label = null;
    }

    void update(float conf, String label) {
        this.conf = conf;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public float getConf() {
        return conf;
    }
}