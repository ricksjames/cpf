/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.pentaho.ClassLoaderResolver;

/**
 *
 * @author pdpi
 */
public class CpfProperties extends Properties {

    private static final long serialVersionUID = 1L;
    private static CpfProperties instance;
    private static final Log logger = LogFactory.getLog(CpfProperties.class);
    private static String PROPERTIES_FILE = "config.properties";

    private CpfProperties(IContentAccessFactory accessor) {
      loadSettings(accessor);
    }

    public static CpfProperties getInstance() {
      if (instance == null) {
          instance = new CpfProperties(PluginEnvironment.repository());
      }
      return instance;
    }

    private boolean loadProperties(IReadAccess location, String fileName) throws IOException {
      if (location.fileExists(fileName)) {
        loadAndClose(location.getFileInputStream(fileName));
        return true;
      }
      return false;
    }

    private void loadSettings(IContentAccessFactory accessor) {
        try {

          // 1) a config.properties inside the jar
          // this one should always exist
          IReadAccess inJar = new ClassLoaderResolver(getClass());
          if (!loadProperties(inJar, PROPERTIES_FILE)) {
            logger.warn("No CPF base settings.");
          }

          // 2) a config.properties in repository:cpf/config.properties
          // factory not so good for this one
          IReadAccess inRepositoryCpf = accessor.getPluginRepositoryReader("../cpf");//XXX
          if (!loadProperties(inRepositoryCpf, PROPERTIES_FILE) && logger.isDebugEnabled()) {
            logger.debug("No global CPF settings.");//downgraded to debug
          }

          //3) in system/<plugin>/config.properties
          IReadAccess inSystem = PluginEnvironment.repository().getPluginSystemReader("");
          if (!loadProperties(inSystem, PROPERTIES_FILE) && logger.isDebugEnabled()) {
            logger.debug("No plugin-specific CPF settings.");//downgraded to debug
          }

        }
        catch (IOException ioe) {
          logger.error("Failed to read CPF settings", ioe);
        }
    }

    public boolean getBooleanProperty(String property, boolean defaultValue) {
        String propertyValue = getProperty(property, null);
        if (!StringUtils.isEmpty(propertyValue)) {
            return Boolean.parseBoolean(propertyValue);
        }
        return defaultValue;
    }

    public int getIntProperty(String property, int defaultValue) {
        String propertyValue = getProperty(property, null);
        if (!StringUtils.isEmpty(propertyValue)) {
            try {
                return Integer.parseInt(propertyValue);
            } catch (NumberFormatException e) {
                logger.error("getIntProperty: " + property + " is not a valid int value.");
            }
        }
        return defaultValue;
    }

    public long getLongProperty(String property, long defaultValue) {
        String propertyValue = getProperty(property, null);
        if (!StringUtils.isEmpty(propertyValue)) {
            try {
                return Long.parseLong(propertyValue);
            } catch (NumberFormatException e) {
                logger.error("getLongProperty: " + property + " is not a valid long value.");
            }
        }
        return defaultValue;
    }

    private void loadAndClose(InputStream input) throws IOException {
        try {
            load(input);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

}