package io.walti.api;


import io.walti.api.exceptions.WaltiApiException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class WaltiApi {

    private static final String CONSOLE_HOST = "https://console.walti.io";
    private static final String API_HOST = "https://api.walti.io";
    private static final String USER_AGENT = "Walti Jenkins Plugin";

    private final String key;
    private final String secret;
    private int lastStatus = 0;

    /**
     * constructor
     *
     * @param key
     * @param secret
     */
    private WaltiApi(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }

    /**
     * Creates a default instance of the given class
     *
     * @param key API key
     * @param secret API secret
     * @return WaltiApi
     */
    public static WaltiApi createInstance(String key, String secret) {
        return new WaltiApi(key, secret);
    }

    /**
     * Get console host root URL
     *
     * @return
     */
    public static String getConsoleHost() {
        return CONSOLE_HOST;
    }

    /**
     * Get status code of last API request
     *
     * @return
     */
    public int getLastStatus() {
        return lastStatus;
    }

    /**
     * Publish POST request
     * @param path
     * @return
     * @throws WaltiApiException
     */
    public InputStream post(String path) throws WaltiApiException {
        return post(path, new HashMap<String, String>());
    }

    /**
     * Publish POST request with parameters as request body
     *
     * @param path
     * @param params
     * @return
     * @throws WaltiApiException
     */
    public InputStream post(String path, Map<String, String> params) throws WaltiApiException {
        URL url = null;
        HttpURLConnection con = null;
        try {
            url = new URL(API_HOST + path);
            con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Api-Key", key);
            con.setRequestProperty("Api-Secret", secret);
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // TOOD make request body if params is not empty
            return request(con);
        } catch (MalformedURLException e) {
            throw new WaltiApiException(e);
        } catch (UnsupportedEncodingException e) {
            throw new WaltiApiException(e);
        } catch (ProtocolException e) {
            throw new WaltiApiException(e);
        } catch (IOException e) {
            throw new WaltiApiException(e);
        }
    }

    /**
     * Publish GET request with parameters as request body
     *
     * @param path
     * @return
     * @throws WaltiApiException
     */
    public InputStream get(String path) throws WaltiApiException {
        URL url = null;
        HttpURLConnection con = null;
        try {
            url = new URL(API_HOST + path);
            con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Api-Key", key);
            con.setRequestProperty("Api-Secret", secret);
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept", "application/json");
            return request(con);
        } catch (MalformedURLException e) {
            throw new WaltiApiException(e);
        } catch (UnsupportedEncodingException e) {
            throw new WaltiApiException(e);
        } catch (ProtocolException e) {
            throw new WaltiApiException(e);
        } catch (IOException e) {
            throw new WaltiApiException(e);
        }
    }

    /**
     * Publish request
     *
     * @param con
     * @return
     * @throws WaltiApiException
     */
    private InputStream request(HttpURLConnection con) throws WaltiApiException {
        try {
            con.connect();
            lastStatus = con.getResponseCode();
            return con.getInputStream();
        } catch (IOException e) {
            try {
                lastStatus = con.getResponseCode();
                InputStream es = con.getErrorStream();
                return es;
            } catch (IOException e1) {
                throw new WaltiApiException(e1);
            }
        }
    }

    /**
     *
     * @return
     * @throws WaltiApiException
     */
    public boolean isValidCredentials() throws WaltiApiException {
        get("/v1/me");
        switch (getLastStatus()) {
            case 200:
                return true;
            case 404:
                return false;
            default:
                throw new WaltiApiException();
        }
    }
}
