/*
 * synopsys-security-scan-plugin
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.jenkins.scan.global;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.TaskListener;
import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

public class Utility {

    public static String getDirectorySeparator(FilePath workspace, TaskListener listener) {
        String os = getAgentOs(workspace, listener);

        if (os != null && os.contains("win")) {
            return "\\";
        }  else {
            return "/";
        }
    }

    public static String getAgentOs(FilePath workspace, TaskListener listener) {
        String os =  null;
        LoggerWrapper logger = new LoggerWrapper(listener);

        if (workspace.isRemote()) {
            try {
                os = workspace.act(new OsNameTask());
            } catch (IOException | InterruptedException e) {
                logger.error("An exception occurred while fetching the OS information for the agent node: " + e.getMessage());
            }
        } else {
            os = System.getProperty("os.name").toLowerCase();
        }

        return os;
    }


    public static void removeFile(String filePath, FilePath workspace, TaskListener listener) {
        LoggerWrapper logger = new LoggerWrapper(listener);
        try {
            FilePath file = new FilePath(workspace.getChannel(), filePath);
            file = file.absolutize();

            if (file.exists()) {
                file.delete();
            }
        } catch (IOException | InterruptedException e) {
            logger.error("An exception occurred while deleting file: " + e.getMessage());
        }
    }

    public static boolean isStringNullOrBlank(String str) {
        return str == null || str.isBlank() || str.equals("null");
    }

    public static HttpURLConnection getHttpURLConnection(URL url, EnvVars envVars, LoggerWrapper logger) {
        try {
            Proxy proxy = getProxy(url, envVars, logger);
            return (HttpURLConnection) url.openConnection(proxy);
        } catch (IOException e) {
            logger.error("An exception occurred while getting HttpURLConnection: " + e.getMessage());
        }

        return null;
    }

    public static Proxy getProxy(URL url, EnvVars envVars, LoggerWrapper logger) {
        try {
            String httpsProxy = getEnvOrSystemProxyDetails(ApplicationConstants.HTTPS_PROXY, envVars);
            if (!isStringNullOrBlank(httpsProxy)) {
                URL httpsProxyURL = new URL(httpsProxy);
                setDefaultProxyAuthenticator(httpsProxyURL.getUserInfo());

                return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpsProxyURL.getHost(), httpsProxyURL.getPort()));
            }

            String httpProxy = getEnvOrSystemProxyDetails(ApplicationConstants.HTTP_PROXY, envVars);
            if (!isStringNullOrBlank(httpProxy)) {
                URL httpProxyURL = new URL(httpProxy);
                setDefaultProxyAuthenticator(httpProxyURL.getUserInfo());

                return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxyURL.getHost(), httpProxyURL.getPort()));
            }
            
            if (!isStringNullOrBlank(httpsProxy) || !isStringNullOrBlank(httpProxy)) {
                String noProxy = getEnvOrSystemProxyDetails(ApplicationConstants.NO_PROXY, envVars);
                if (!isStringNullOrBlank(noProxy)) {
                    List<String> noProxyList = List.of(noProxy.split(","));
                    if (noProxyList.contains(url.toString())) {
                        return Proxy.NO_PROXY;
                    }
                }
            }
        } catch (MalformedURLException e) {
            logger.error("An exception occurred while getting proxy: " + e.getMessage());
        }

        return Proxy.NO_PROXY;
    }

    public static String getEnvOrSystemProxyDetails(String proxyType, EnvVars envVars) {
        String proxyDetails = envVars.get(proxyType);
        if (isStringNullOrBlank(proxyDetails)) {
            proxyDetails = envVars.get(proxyType.toLowerCase());
        }
        if (isStringNullOrBlank(proxyDetails)) {
            proxyDetails = System.getenv(proxyType);
        }
        if (isStringNullOrBlank(proxyDetails)) {
            proxyDetails = System.getenv(proxyType.toLowerCase());
        }

        return proxyDetails;
    }

    public static void setDefaultProxyAuthenticator(String userInfo) {
        if (!isStringNullOrBlank(userInfo)) {
            String[] userInfoArray = userInfo.split(":");
            if (userInfoArray.length == 2) {
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(userInfoArray[0], userInfoArray[1].toCharArray());
                    }
                });
            }
        }
    }

}
