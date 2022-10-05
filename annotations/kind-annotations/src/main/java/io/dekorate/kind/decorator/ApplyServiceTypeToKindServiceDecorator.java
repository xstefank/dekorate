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
 * 
**/

package io.dekorate.kind.decorator;

import io.dekorate.kind.config.KindConfig;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ServiceSpecFluent;

public class ApplyServiceTypeToKindServiceDecorator extends NamedResourceDecorator<ServiceSpecFluent> {

  private final KindConfig config;

  public ApplyServiceTypeToKindServiceDecorator(String name, KindConfig config) {
    super(name);
    this.config = config;
  }

  @Override
  public void andThenVisit(ServiceSpecFluent spec, ObjectMeta resourceMeta) {
    spec.withType(config.getServiceType() != null ? config.getServiceType().name() : "NodePort");
  }
}
