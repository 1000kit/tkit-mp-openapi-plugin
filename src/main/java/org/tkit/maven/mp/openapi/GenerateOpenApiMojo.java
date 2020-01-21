/*
 * Copyright 2019 1000kit.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tkit.maven.mp.openapi;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.utils.io.DirectoryScanner;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Indexer;
import org.reflections.Reflections;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Generate the openAPI file.
 */
@Mojo(name = "generate",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresProject = true,
        aggregator = false,
        threadSafe = true)
public class GenerateOpenApiMojo extends AbstractMojo {

    /**
     * The classes directory.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private File classesDir;

    /**
     * The configuration file.
     */
    @Parameter(name = "configFile")
    private File configFile;

    /**
     * The configuration file ordinal number.
     */
    @Parameter(name = "configFileOrdinal", defaultValue = "200", required = true)
    private String configFileOrdinal;

    /**
     * The output file.
     */
    @Parameter(name = "outputFile", defaultValue = "${project.build.directory}/openapi.yaml", required = true)
    private File outputFile;

    /**
     * The format: YAML, JSON
     */
    @Parameter(name = "format", defaultValue = "YAML", required = true)
    private String format;

    /**
     * The verbose flag.
     */
    @Parameter(defaultValue = "false")
    private boolean verbose;

    /**
     * The micro-profile properties defined in the plugin configuration.
     */
    @Parameter(name = "properties")
    private Properties properties;

    /**
     * The micro-profile properties defined in the plugin configuration ordinal number.
     */
    @Parameter(name = "propertiesOrdinal", defaultValue = "201", required = true)
    private String propertiesOrdinal;

    /**
     * The mojo execution
     */
    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojoExecution;

    /**
     * {@inheritDoc }
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Indexer indexer = createIndexer();
            if (indexer == null) {
                return;
            }

            OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(createConfig(), indexer.complete());
            OpenAPI result = scanner.scan();

            OpenApiSerializer.Format f = OpenApiSerializer.Format.YAML;
            if (OpenApiSerializer.Format.JSON.name().equals(format)) {
                f = OpenApiSerializer.Format.JSON;
            }

            String output = OpenApiSerializer.serialize(result, f);
            if (verbose) {
                getLog().info("OpenApi:\n" + output);
            }
            getLog().info("OpenAPI file in format " + format + " created: " + outputFile);

            // create the target directory
            Files.createDirectories(outputFile.toPath().getParent());

            // write the openapi to the output file
            Files.write(outputFile.toPath(), output.getBytes(StandardCharsets.UTF_8));

        } catch (Exception ex) {
            throw new MojoExecutionException("Error execute the plugin ", ex);
        }
    }

    /**
     * Creates the micro-profile configuration for the openAPI
     *
     * @return the openAPI configuration.
     * @throws MojoExecutionException if the method fails.
     */
    private OpenApiConfig createConfig() throws MojoExecutionException {
        Config config = ConfigProvider.getConfig();
        SmallRyeConfig sc = (SmallRyeConfig) config;

        // config file
        if (configFile != null && configFile.exists() && configFile.isFile()) {
            Properties prop = new Properties();
            try (InputStream input = new FileInputStream(configFile)) {
                prop.load(input);
            } catch (Exception e) {
                throw new MojoExecutionException("Error loading the properties " + configFile, e);
            }
            prop.setProperty(ConfigSource.CONFIG_ORDINAL, configFileOrdinal);
            sc.addConfigSource(new PropertiesConfigSource(prop, configFile.getName()));
            getLog().info("Add the configuration source " + configFile);
        }

        // properties in the plugin configuration
        if (properties != null && !properties.isEmpty()) {
            properties.setProperty(ConfigSource.CONFIG_ORDINAL, propertiesOrdinal);
            String id = mojoExecution.getMojoDescriptor().getId() + " (" + mojoExecution.getExecutionId() + ")";
            sc.addConfigSource(new PropertiesConfigSource(properties, id));
            getLog().info("Add the configuration source from maven plugin: " + id);
        }

        return new OpenApiConfigImpl(config);
    }

    /**
     * Creates the indexer
     *
     * @return the indexer
     * @throws MojoExecutionException if the method fails.
     */
    private Indexer createIndexer() throws MojoExecutionException {

        if (!classesDir.exists()) {
            getLog().info("Directory does not exist! Directory: " + classesDir);
            return null;
        }

        final DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(classesDir);
        scanner.setIncludes("**/*.class");
        scanner.scan();
        final String[] files = scanner.getIncludedFiles();

        final Indexer indexer = new Indexer();
        for (final String file : files) {
            if (file.endsWith(".class")) {
                File tmp = new File(classesDir, file);
                if (verbose) {
                    getLog().info("File: " + tmp);
                }
                try (FileInputStream fis = new FileInputStream(tmp)) {
                    final ClassInfo info = indexer.index(fis);
                    if (verbose && info != null) {
                        getLog().info("Indexed " + info.name() + " (" + info.annotations().size() + " annotations)");
                    }
                } catch (final Exception e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }

            }
        }
        return indexer;
    }
}
