package com.willblaschko.android.alexa.interfaces;

import java.io.Serializable;

/**
 * @author wblaschko on 8/13/15.
 */
public abstract class AvsItem implements Serializable{
    String token;
    public AvsItem(String token){
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
