package com.eastwood.tools.plugins.run

import com.eastwood.tools.plugins.run.extension.SingleRunConfig
import com.eastwood.tools.plugins.run.extension.SingleRunExtension
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.util.ConfigureUtil

class SingleRunSettingsPlugin implements Plugin<Settings> {

    private static String BUILD_GRADLE_TEMPLATE_PATH = "/build.tmpl"
    private static String JUMPER_ACTIVITY_TEMPLATE_PATH = "/SingleRunActivity.tmpl"
    private static String ANDROID_MANIFEST_TEMPLATE_PATH = "/AndroidManifest.tmpl"

    Settings settings
    File projectDir

    void apply(Settings settings) {
        this.settings = settings
        projectDir = settings.rootProject.projectDir

        settings.metaClass.singleRun { Closure closure ->
            SingleRunExtension jumperExtension = new SingleRunExtension()
            ConfigureUtil.configure(closure, jumperExtension)

            def runDir = new File(projectDir, 'run')
            if (jumperExtension.runDir != null) {
                runDir = new File(projectDir, jumperExtension.runDir)
            }

            settings.gradle.startParameter.taskNames.each {
                if (it == 'clean') {
                    boolean result = runDir.deleteDir()
                    if (!result) {
                        throw new RuntimeException("unable to delete dir " + runDir.absolute)
                    }
                }
            }

            if (!runDir.exists()) {
                runDir.mkdir()
            }

            HashMap<String, SingleRunConfig> singleRunConfigs = jumperExtension.getSingleRunConfigs()
            singleRunConfigs.each {
                SingleRunConfig singleRunConfig = it.value
                def moduleName = it.key
                def moduleDir = new File(runDir, moduleName)
                if (!moduleDir.exists()) {
                    moduleDir.mkdir()
                }

                def buildFile = new File(moduleDir, 'build.gradle')
                if (!buildFile.exists()) {
                    InputStream is = SingleRunSettingsPlugin.class.getResourceAsStream(BUILD_GRADLE_TEMPLATE_PATH);
                    Scanner scanner = new Scanner(is)
                    String template = scanner.useDelimiter("\\A").next();
                    String fileContent = template.replace("%APPLICATION_ID%", "run." + moduleName)
                    buildFile.setText(fileContent)
                }

                def androidManifestFile = new File(moduleDir, 'src/AndroidManifest.xml')
                if (!androidManifestFile.exists()) {
                    androidManifestFile.getParentFile().mkdirs()
                    InputStream is = SingleRunSettingsPlugin.class.getResourceAsStream(ANDROID_MANIFEST_TEMPLATE_PATH);
                    Scanner scanner = new Scanner(is)
                    String template = scanner.useDelimiter("\\A").next()
                    String applicationReplaceHolder = ""
                    if (singleRunConfig.applicationName != null) {
                        applicationReplaceHolder = "android:name=\"${singleRunConfig.applicationName}\"\ntools:replace=\"android:name\""
                    }
                    String fileContent = template.replace("%APPLICATION_NAME%", applicationReplaceHolder)
                    androidManifestFile.setText(fileContent)
                }

                def singleRunActivityFile = new File(moduleDir, '/src/java/run/SingleRunActivity.java')
                if (!singleRunActivityFile.exists()) {
                    singleRunActivityFile.getParentFile().mkdirs()

                    InputStream is = SingleRunSettingsPlugin.class.getResourceAsStream(JUMPER_ACTIVITY_TEMPLATE_PATH);
                    Scanner scanner = new Scanner(is)
                    String template = scanner.useDelimiter("\\A").next()

                    String bundle = ''
                    singleRunConfig.bundle.each {
                        if (it.value instanceof String) {
                            bundle += '        intent.putExtra("' + it.key + '", "' + it.value + '");\n'
                        } else if (it.value instanceof Float) {
                            bundle += '        intent.putExtra("' + it.key + '", ' + it.value + 'f);\n'
                        } else if (it.value instanceof Long) {
                            bundle += '        intent.putExtra("' + it.key + '", ' + it.value + 'L);\n'
                        } else if (it.value instanceof Double) {
                            bundle += '        intent.putExtra("' + it.key + '", ' + it.value + 'd);\n'
                        } else {
                            bundle += '        intent.putExtra("' + it.key + '", ' + it.value + ');\n'
                        }
                    }

                    String fileContent = template
                            .replace("%TARGET%", singleRunConfig.target)
                            .replace("%BUNDLE%", bundle)

                    singleRunActivityFile.setText(fileContent)
                }

                def stringsFile = new File(moduleDir, '/src/res/values/strings.xml')
                if (!stringsFile.exists()) {
                    stringsFile.getParentFile().mkdirs()
                    String fileContent = "<resources>\n    <string name=\"app_name\">" + moduleName + "</string>\n</resources>"

                    stringsFile.setText(fileContent)
                }

                settings.include(":${moduleName}")
                settings.project(":${moduleName}").setProjectDir(new File(runDir, moduleName))
            }
        }

        settings.apply from: 'singleRun.gradle'
    }
}