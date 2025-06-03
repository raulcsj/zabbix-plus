package io.zabbixplus.framework.core.web.dto;

import io.zabbixplus.framework.plugin.NavigationItem;

import java.util.List;
import java.util.Map;

public class PluginClientInfo {

    private String name;
    private Map<String, Object> uiMetadata;
    private List<NavigationItem> navigationItems;

    public PluginClientInfo(String name, Map<String, Object> uiMetadata, List<NavigationItem> navigationItems) {
        this.name = name;
        this.uiMetadata = uiMetadata;
        this.navigationItems = navigationItems;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getUiMetadata() {
        return uiMetadata;
    }

    public void setUiMetadata(Map<String, Object> uiMetadata) {
        this.uiMetadata = uiMetadata;
    }

    public List<NavigationItem> getNavigationItems() {
        return navigationItems;
    }

    public void setNavigationItems(List<NavigationItem> navigationItems) {
        this.navigationItems = navigationItems;
    }
}
