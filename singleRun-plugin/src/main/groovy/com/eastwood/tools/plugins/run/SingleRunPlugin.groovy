package com.eastwood.tools.plugins.run

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

class SingleRunPlugin implements Plugin<Object> {

    void apply(Object object) {
        Plugin plugin = null
        if (object instanceof Project) {
            plugin = new SingleRunBuildPlugin();
        } else if (object instanceof Settings) {
            plugin = new SingleRunSettingsPlugin()
        }

        if (plugin != null) {
            plugin.apply(object)
        }
    }

}