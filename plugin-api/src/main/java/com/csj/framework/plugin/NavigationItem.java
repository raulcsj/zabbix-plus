package com.csj.framework.plugin;

import java.io.Serializable; // Good practice for DTO-like objects

public class NavigationItem implements Serializable {
    private static final long serialVersionUID = 1L; // version for Serializable

    private final String name; // Display name for the navigation link
    private final String path; // The target path (e.g., "/ui/plugins/myplugin/home")
    private final String icon; // Optional: CSS class for an icon

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

    // Optional: equals and hashCode if these items are stored in sets or used in collections that require it.
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
