package io.walti.api;

import io.walti.api.exceptions.WaltiApiException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONSerializer;

public class Target {

    public enum Status {
        ACTIVE,
        UNCHECKED,
        ARCHIVED,
    }

    public enum Ownership {
        UNKNOWN,
        QUEUED,
        CONFIRMED,
    }

    private Status status;
    private String name;
    private String description;
    private String label;
    private URL ownershipUrl;
    private Ownership ownership;
    private List<Plugin> plugins = new ArrayList<Plugin>();
    private Date createdAt;
    private Date updatedAt;

    /**
     * Get target status
     *
     * @return
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Get target's name
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get target's description
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get label
     *
     * @return
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get URL where ownership file is located
     *
     * @return
     */
    public URL getOwnershipUrl() {
        return ownershipUrl;
    }

    /**
     * Get ownership status
     *
     * @return
     */
    public Ownership getOwnership() {
        return ownership;
    }

    /**
     * Get plugin list available on this target
     *
     * @return
     */
    public List<Plugin> getPlugins() {
        return plugins;
    }

    /**
     * Get when this target is created
     *
     * @return
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Get when this target is last updated
     *
     * @return
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Fetch target info
     *
     * @param api
     * @param targetName
     * @return
     * @throws WaltiApiException
     */
    public static Target find(WaltiApi api, String targetName) throws WaltiApiException {
        String encodedTargetName = null;
        try {
            encodedTargetName = URLEncoder.encode(targetName, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new WaltiApiException(ex);
        }

        InputStream res = null;
        try {
            res = api.get("/v1/targets/" + encodedTargetName);
            if (api.getLastStatus() != 200) {
                throw new WaltiApiException();
            }
            String body = IOUtils.toString(res, Charset.forName("UTF-8"));
            return Target.createFromJSON((JSONObject) JSONSerializer.toJSON(body));
        } catch (IOException e) {
            throw new WaltiApiException(e);
        } finally {
            if (null != res) {
                IOUtils.closeQuietly(res);
            }
        }
    }

    /**
     * Get all targets
     *
     * @param api
     * @return
     * @throws WaltiApiException
     */
    public static List<Target> getAll(WaltiApi api) throws WaltiApiException {
        InputStream res = null;
        try {
            res = api.get("/v1/targets");
            String body = IOUtils.toString(res, Charset.forName("UTF-8"));
            JSONArray targetArr = (JSONArray)JSONSerializer.toJSON(body);
            List<Target> targets = new ArrayList<Target>();
            for (Object target : targetArr) {
                targets.add(Target.createFromJSON((JSONObject)target));
            }
            return targets;
        } catch (IOException e) {
            throw new WaltiApiException(e);
        } finally {
            if (null != res) {
                IOUtils.closeQuietly(res);
            }
        }
    }

    /**
     * Create instance from JSON object
     *
     * @param json
     * @return
     */
    public static Target createFromJSON(JSONObject json) throws WaltiApiException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        Target target = new Target();
        try {
            target.status = Status.valueOf(json.getString("status").toUpperCase());
            target.name = json.getString("name");
            target.description = json.getString("description");
            target.label = json.getString("label");
            target.ownershipUrl = new URL(json.getString("ownership_url"));
            target.ownership = Ownership.valueOf(json.getString("ownership").toUpperCase());
            target.createdAt = dateFormat.parse(json.getString("created_at").replaceAll("(?<=\\+\\d{1,2}):", ""));
            target.updatedAt = dateFormat.parse(json.getString("updated_at").replaceAll("(?<=\\+\\d{1,2}):", ""));

            JSONArray plugins = json.getJSONArray("plugins");
            for (Object plugin : plugins) {
                target.plugins.add(Plugin.createFromJSON((JSONObject)plugin));
            }
        } catch (ParseException e) {
            throw new WaltiApiException(e);
        } catch (MalformedURLException e) {
            throw new WaltiApiException(e);
        }
        return target;
    }

    /**
     * Get URL String where latest scan result page
     *
     * @param pluginName
     * @return
     */
    public String getResultURL(String pluginName) throws WaltiApiException {
        int logId = -1;
        for (Plugin plugin : plugins) {
            if (pluginName.equals(plugin.getName())) {
                Scan scan = plugin.getScan();
                if (scan == null) {
                    throw new WaltiApiException();
                }
                logId = scan.getId();
                break;
            }
        }
        return WaltiApi.getConsoleHost() + "/targets/" + getName() + "/plugins/" + pluginName + "/logs/" + logId;
    }
}
