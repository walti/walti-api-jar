package io.walti.api;

import io.walti.api.exceptions.WaltiApiException;
import net.sf.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Plugin {

    public enum Schedule {
        DAY,
        WEEK,
        MONTH,
        OFF
    }

    private String name;
    private Scan scan;
    private Schedule schedule;
    private boolean queued;
    private Date queuedAt;

    /**
     * Get plugin name
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get last scan result of this plugin
     *
     * @return
     */
    public Scan getScan() {
        return scan;
    }

    /**
     * Get schedule of this plugins
     *
     * @return
     */
    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * Whether this plugin is being queued
     *
     * @return
     */
    public boolean isQueued() {
        return queued;
    }

    /**
     * When this plugin is queued
     *
     * @return
     */
    public Date getQueuedAt() {
        return queuedAt;
    }

    /**
     * Create instance from JSON object
     *
     * @param json
     * @return
     */
    public static Plugin createFromJSON(JSONObject json) throws WaltiApiException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        Plugin plugin = new Plugin();
        plugin.name = json.getString("name");
        plugin.schedule = Schedule.valueOf(json.getString("schedule").toUpperCase());
        JSONObject scan = json.optJSONObject("scan");
        if (scan != null) {
            plugin.scan = Scan.createFromJSON(scan);
        }
        plugin.queued = json.getBoolean("queued");
        if (plugin.isQueued()) {
            try {
                plugin.queuedAt = dateFormat.parse(json.getString("queued_at").replaceAll("(?<=\\+\\d{1,2}):", ""));
            } catch (ParseException e) {
                throw new WaltiApiException(e);
            }
        }
        return plugin;
    }
}
