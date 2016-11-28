/*
 * Sonar License Check Plugin
 * Copyright (C) 2013 Porsche Informatik
 * dev@sonar.codehaus.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.porscheinformatik.sonarqube.licensecheck;

import org.sonar.api.resources.Qualifiers;
import org.sonar.api.web.Page;
import org.sonar.api.web.ResourceQualifier;

/**
 * Configuration for licenses and dependencies.
 */
@ResourceQualifier({Qualifiers.PROJECT, Qualifiers.MODULE})
public class LicenseCheckExportPage implements Page
{
    @Override
    public String getId()
    {
        return "/licensecheck_export";
    }

    @Override
    public String getTitle()
    {
        return "License Check Export";
    }
}
