package com.eastwood.tools.plugins.run

import com.eastwood.tools.plugins.run.extension.SingleRunConfig
import com.eastwood.tools.plugins.run.extension.SingleRunExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

class SingleRunBuildPlugin implements Plugin<Project> {

    Project project
    HashMap<String, SingleRunConfig> singleRunConfigs

    void apply(Project project) {
        this.project = project

        project.metaClass.singleRun { Closure closure ->
            SingleRunExtension singleRunExtension = new SingleRunExtension()
            ConfigureUtil.configure(closure, singleRunExtension)
            singleRunConfigs = singleRunExtension.getSingleRunConfigs()
        }

        project.apply from: 'singleRun.gradle'

        project.afterEvaluate {
            if (singleRunConfigs != null) {
                project.getAllprojects().each {
                    def currentProject = it
                    SingleRunConfig singleRunConfig = singleRunConfigs.get(currentProject.name)
                    if (singleRunConfig != null) {
                        currentProject.afterEvaluate {
                            for (int i = 0; i < singleRunConfig.modules.length; i++) {
                                currentProject.dependencies.add('api', currentProject.project(singleRunConfig.modules[i]))
                            }

                        }
                    }
                }
            }

        }
    }

}