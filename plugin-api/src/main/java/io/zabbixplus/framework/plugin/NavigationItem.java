package io.zabbixplus.framework.plugin; // Updated package

import java.io.Serializable;

public class NavigationItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String path;
    private final String icon;

    public NavigationItem(String name, String path) {
        this(name, path, null);
    }

    public NavigationItem(String name, String path, String icon) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Navigation item name cannot be null or empty.");
        }
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Navigation item path cannot be null or empty.");
        }
        this.name = name;
        this.path = path;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NavigationItem that = (NavigationItem) o;
        return name.equals(that.name) && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }
}
