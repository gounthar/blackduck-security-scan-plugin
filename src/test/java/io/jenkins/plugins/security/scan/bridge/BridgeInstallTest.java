package io.jenkins.plugins.security.scan.bridge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.FilePath;
import hudson.model.TaskListener;
import io.jenkins.plugins.security.scan.exception.PluginExceptionHandler;
import io.jenkins.plugins.security.scan.global.ApplicationConstants;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class BridgeInstallTest {
    private final TaskListener listenerMock = Mockito.mock(TaskListener.class);
    private FilePath bridgeInstallationPath;
    private BridgeInstall bridgeInstall;

    @BeforeEach
    public void setup() {
        bridgeInstallationPath = new FilePath(
                new File(getHomeDirectory() + File.separator + ApplicationConstants.DEFAULT_DIRECTORY_NAME));
        Mockito.when(listenerMock.getLogger()).thenReturn(Mockito.mock(PrintStream.class));
        bridgeInstall = new BridgeInstall(bridgeInstallationPath, listenerMock);
    }

    @Test
    void installBridgeCLITest() {
        FilePath sourceBridge;
        String os = System.getProperty("os.name").toLowerCase();
        FilePath destinationBridge = bridgeInstallationPath.child("demo-bridge.zip");

        if (os.contains("win")) {
            sourceBridge = new FilePath(new File("src\\test\\resources\\demo-bridge.zip"));
        } else {
            sourceBridge = new FilePath(new File("src/test/resources/demo-bridge.zip"));
        }

        try {
            sourceBridge.copyTo(destinationBridge);
            bridgeInstall.installBridgeCLI(getFullZipPath(), bridgeInstallationPath, "TEST_DIR");

            assertFalse(destinationBridge.exists());
            assertTrue(bridgeInstallationPath.child("demo-bridge-extensions").isDirectory());
            assertTrue(bridgeInstallationPath.child("demo-bridge-versions.txt").exists());
            assertTrue(bridgeInstallationPath.child("demo-bridge-LICENSE.txt").exists());

            cleanupBridgeInstallationPath(bridgeInstallationPath);
        } catch (IOException | InterruptedException | PluginExceptionHandler e) {
            System.out.println("Exception occurred during testing for installBridgeCLI method. " + e.getMessage());
        }
    }

    public String getHomeDirectory() {
        return System.getProperty("user.home");
    }

    public FilePath getFullZipPath() {
        FilePath bridgeZipPath;
        if (getHomeDirectory().contains("\\")) {
            bridgeZipPath = new FilePath(
                    new File(bridgeInstallationPath.getRemote().concat("\\").concat("demo-bridge.zip")));
        } else {
            bridgeZipPath = new FilePath(
                    new File(bridgeInstallationPath.getRemote().concat("/").concat("demo-bridge.zip")));
        }
        return bridgeZipPath;
    }

    public void cleanupBridgeInstallationPath(FilePath bridgeInstallationPath) {
        try {
            FilePath versionsFile = bridgeInstallationPath.child("demo-bridge-versions.txt");
            if (versionsFile.exists()) {
                versionsFile.delete();
            }

            FilePath licenseFile = bridgeInstallationPath.child("demo-bridge-LICENSE.txt");
            if (licenseFile.exists()) {
                licenseFile.delete();
            }

            FilePath extensionsDirectory = bridgeInstallationPath.child("demo-bridge-extensions");
            if (extensionsDirectory.isDirectory()) {
                extensionsDirectory.deleteRecursive();
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error while cleaning up bridgeInstallationPath: " + e.getMessage());
        }
    }
}
