package io.zabbixplus.framework.core.controller;

import io.zabbixplus.framework.core.plugin.PluginService;
import io.zabbixplus.framework.core.web.dto.PluginClientInfo;
import io.zabbixplus.framework.plugin.Plugin;
import io.zabbixplus.framework.plugin.UiPlugin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ui")
public class PluginUiController {

    private final PluginService pluginService;

    public PluginUiController(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @GetMapping("/plugin-metadata")
    public List<PluginClientInfo> getPluginUiMetadata() {
        return pluginService.getLoadedPlugins().values().stream()
                .filter(UiPlugin.class::isInstance)
                .map(UiPlugin.class::cast)
                .map(uiPlugin -> new PluginClientInfo(
                        uiPlugin.getPluginName(),
                        uiPlugin.getUiMetadata(),
                        uiPlugin.getNavigationItems()
                ))
                .collect(Collectors.toList());
    }
}
