/**
 * Copyright 2018 The original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.option.apt;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import io.dekorate.WithSession;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.doc.Description;
import io.dekorate.option.annotation.GeneratorOptions;
import io.dekorate.option.config.GeneratorConfigBuilder;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.utils.Strings;

@Description("Processing generator options, which are used for customizing the generation process")
@SupportedAnnotationTypes({
  "io.dekorate.annotation.Dekorate",
  "io.dekorate.kubernetes.annotation.KubernetesApplication",
  "io.dekorate.openshift.annotation.OpenshiftApplication",
  "io.dekorate.knative.annotation.KnativeApplication",
  "io.dekorate.option.annotation.GeneratorOptions"
})
public class GeneratorOptionsProcessor extends AbstractAnnotationProcessor implements WithSession {

  private static final String INPUT_DIR = "dekorate.input.dir";
  private static final String OUTPUT_DIR = "dekorate.output.dir";

  private static final String FALLBACK_INPUT_DIR = "META-INF/fabric8";
  private static final String FALLBACK_OUTPUT_DIR = "META-INF/fabric8";

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      getSession().close();
      return true;
    }

    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        GeneratorOptions options = mainClass.getAnnotation(GeneratorOptions.class);
        if (options == null) {
          continue;
        }

        configurePaths(options.inputPath(), options.outputPath());
        return false;
       }
      configurePaths(FALLBACK_INPUT_DIR, FALLBACK_OUTPUT_DIR);
    }
    return false;
  }

  private void configurePaths(String defaultInputPath, String defaultOutputPath) {
    final String inputPath = System.getProperty(INPUT_DIR, defaultInputPath);
    final String outputPath =  Optional.ofNullable(System.getProperty(OUTPUT_DIR))
      .map(path -> {
        resolve(path).mkdirs();
        return path;
      }).orElse(defaultOutputPath);
    if (isPathValid(inputPath)) {
      applyToProject(p -> p.withDekorateInputDir(inputPath));
      getSession().configurators().add(new ConfigurationSupplier<>(new GeneratorConfigBuilder()));
    }
    if (isPathValid(outputPath)) {
      applyToProject(p -> p.withDekorateOutputDir(outputPath));
    }
  }

  private boolean isPathValid(String path) {
    return Strings.isNotNullOrEmpty(path) && resolve(path).exists();
  }

  private File resolve(String unixPath) {
    return new File(getProject().getBuildInfo().getClassOutputDir().toFile(), unixPath.replace('/', File.separatorChar));
  }

}
