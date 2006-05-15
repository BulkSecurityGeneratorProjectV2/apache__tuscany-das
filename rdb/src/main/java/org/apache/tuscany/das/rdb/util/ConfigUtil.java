/**
 *
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tuscany.das.rdb.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tuscany.das.rdb.config.Config;
import org.apache.tuscany.das.rdb.config.ConfigFactory;
import org.apache.tuscany.sdo.util.SDOUtil;

import commonj.sdo.helper.XMLHelper;

/**
 * Config util provides config-related utilities such as loading a Config
 * instance from an InputStream
 * 
 */
public class ConfigUtil {

    public static Config loadConfig(InputStream configStream) {

        if (configStream == null)
            throw new Error(
                    "Cannot load configuration from a null InputStream. Possibly caused by an incorrect config xml file name");

        SDOUtil.registerStaticTypes(ConfigFactory.class);
        XMLHelper helper = XMLHelper.INSTANCE;

        try {
            return (Config) helper.load(configStream).getRootObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
