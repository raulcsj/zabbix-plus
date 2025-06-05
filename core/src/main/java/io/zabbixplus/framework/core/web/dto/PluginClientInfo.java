package io.zabbixplus.framework.core.web.dto;

import io.zabbixplus.framework.plugin.NavigationItem;

import java.util.List;
import java.util.Map;

public class PluginClientInfo {

    private String pluginId;
    private String pluginName; // Renamed from name
    private String vendor;
    private String version;
    private String description;
    private String assetsPath;
    private String entryComponent;
    private List<String> requiredPrivileges; // Assuming List<String>

    private Map<String, Object> uiMetadata; // Retained as is, might be redundant or for other specific UI hints
    private List<NavigationItem> navigationItems;

    public PluginClientInfo(String pluginId, String pluginName, String vendor, String version, String description,
                            String assetsPath, String entryComponent, List<String> requiredPrivileges,
                            Map<String, Object> uiMetadata, List<NavigationItem> navigationItems) {
        this.pluginId = pluginId;
        this.pluginName = pluginName;
        this.vendor = vendor;
        this.version = version;
        this.description = description;
        this.assetsPath = assetsPath;
        this.entryComponent = entryComponent;
        this.requiredPrivileges = requiredPrivileges;
        this.uiMetadata = uiMetadata;
        this.navigationItems = navigationItems;
    }

    // Getters
    public String getPluginId() {
        return pluginId;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getVendor() {
        return vendor;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getAssetsPath() {
        return assetsPath;
    }

    public String getEntryComponent() {
        return entryComponent;
    }

    public List<String> getRequiredPrivileges() {
        return requiredPrivileges;
    }

    public Map<String, Object> getUiMetadata() {
        return uiMetadata;
    }

    public List<NavigationItem> getNavigationItems() {
        return navigationItems;
    }

    // Optional: Setters if needed, but DTOs are often immutable or fields set via constructor
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAssetsPath(String assetsPath) {
        this.assetsPath = assetsPath;
    }

    public void setEntryComponent(String entryComponent) {
        this.entryComponent = entryComponent;
    }

    public void setRequiredPrivileges(List<String> requiredPrivileges) {
        this.requiredPrivileges = requiredPrivileges;
    }

    public void setUiMetadata(Map<String, Object> uiMetadata) {
        this.uiMetadata = uiMetadata;
    }

    public void setNavigationItems(List<NavigationItem> navigationItems) {
        this.navigationItems = navigationItems;
    }
}
