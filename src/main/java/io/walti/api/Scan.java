package io.walti.api;

import io.walti.api.exceptions.WaltiApiException;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Scan {

    final static public String STATUS_COLOR_GREEN = "green";
    final static public String STATUS_COLOR_ORANGE = "orange";
    final static public String STATUS_COLOR_RED = "red";
    final static public String STATUS_COLOR_GREY = "grey";

    private int id;
    private String message;
    private String status;
    private String statusColor;
    private int resultStatus;
    private Date createdAt;
    private Date updatedAt;

    public static final int RESULT_OK = 200;

    public enum QueueResult {
        UNDEFINED,
        SUCCESS,
        SKIPPED,
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public int getResultStatus() {
        return resultStatus;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Queue specified scan
     *
     * @param api
     * @param targetName
     * @param pluginName
     * @return
     */
    public static QueueResult queue(WaltiApi api, String targetName, String pluginName) throws WaltiApiException {
        String encodedTargetName = null;
        String encodedPluginName = null;
        try {
            encodedTargetName = URLEncoder.encode(targetName, "UTF-8");
            encodedPluginName = URLEncoder.encode(pluginName, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new WaltiApiException(ex);
        }

        InputStream res = null;
        try {
            res = api.post("/v1/targets/" + encodedTargetName + "/plugins/" + encodedPluginName + "/scans");
        } finally {
            if (null != res) {
                IOUtils.closeQuietly(res);
            }
        }

        switch (api.getLastStatus()) {
            case 402:
                return QueueResult.SKIPPED;
            case 201:
                return QueueResult.SUCCESS;
            default:
                throw new WaltiApiException("Unexpected status code on calling scan queueing API status:" + api.getLastStatus());
        }
    }

    /**
     * Create instance from JSON object
     *
     * @param json
     * @return
     */
    public static Scan createFromJSON(JSONObject json) throws WaltiApiException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        Scan scan = new Scan();
        scan.id = json.getInt("id");
        scan.message = json.getString("message");
        scan.status = json.getString("status");
        scan.statusColor = json.getString("status_color");
        scan.resultStatus = json.getInt("result_status");
        try {
            scan.createdAt = dateFormat.parse(json.getString("created_at").replaceAll("(?<=\\+\\d{1,2}):", ""));
            scan.updatedAt = dateFormat.parse(json.getString("updated_at").replaceAll("(?<=\\+\\d{1,2}):", ""));
        } catch (ParseException e) {
            throw new WaltiApiException(e);
        }
        return scan;
    }
}
