package io.jenkins.plugins.security.scan.service.scan.srm;

import static org.junit.jupiter.api.Assertions.*;

import hudson.EnvVars;
import hudson.model.TaskListener;
import io.jenkins.plugins.security.scan.global.ApplicationConstants;
import io.jenkins.plugins.security.scan.input.blackducksca.BlackDuckSCA;
import io.jenkins.plugins.security.scan.input.coverity.Coverity;
import io.jenkins.plugins.security.scan.input.srm.SRM;
import io.jenkins.plugins.security.scan.service.scan.blackducksca.BlackDuckSCAParametersService;
import io.jenkins.plugins.security.scan.service.scan.coverity.CoverityParametersService;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SRMParametersServiceTest {

    private SRMParametersService srmParametersService;
    private BlackDuckSCAParametersService blackDuckSCAParametersService;
    private CoverityParametersService coverityParametersService;
    private final TaskListener listenerMock = Mockito.mock(TaskListener.class);
    private final EnvVars envVarsMock = Mockito.mock(EnvVars.class);
    private final String TEST_SRM_SERVER_URL = "https://fake.srm-server.url";
    private final String TEST_SRM_API_KEY_TOKEN = "fakeSrmAPIKey";
    private final String TEST_SRM_PROJECT_NAME = "fake-srm-project-name";
    private final String TEST_SRM_PROJECT_ID = "fake-srm-project-id";
    private final String TEST_SRM_ASSESSMENT_TYPES = "SCA";
    private final String TEST_SRM_BRANCH_NAME = "test-branch";
    private final String TEST_SRM_BRANCH_PARENT_NAME = "test-parent-branch";
    private final String TEST_BLACKDUCKSCA_ARGS = "--detect.diagnostic=true";
    private final String TEST_BLACKDUCKSCA_CONFIG_FILE_PATH = "DIR/CONFIG/application.properties";
    private final String TEST_COVERITY_CLEAN_COMMAND = "mvn clean";
    private final String TEST_COVERITY_BUILD_COMMAND = "mvn clean install";
    private final String TEST_COVERITY_ARGS = "-o capture.build.clean-command=\"mvn clean\" -- mvn clean install";
    private final String TEST_COVERITY_CONFIG_FILE_PATH = "DIR/CONFIG/coverity.yml";
    private final String TEST_DETECT_EXECUTION_PATH = "/fake/path/bd";
    private final String TEST_COVERITY_EXECUTION_PATH = "/fake/path/cov";

    @BeforeEach
    void setUp() {
        srmParametersService = new SRMParametersService(listenerMock, envVarsMock);
        Mockito.when(listenerMock.getLogger()).thenReturn(Mockito.mock(PrintStream.class));
    }

    @Test
    void invalidScanParametersTest() {
        Map<String, Object> srmParameters = new HashMap<>();

        assertFalse(srmParametersService.hasAllMandatorySrmParams(srmParameters));

        srmParameters.put(ApplicationConstants.SRM_URL_KEY, TEST_SRM_SERVER_URL);
        srmParameters.put(ApplicationConstants.SRM_APIKEY_KEY, TEST_SRM_API_KEY_TOKEN);

        assertFalse(srmParametersService.hasAllMandatorySrmParams(srmParameters));
    }

    @Test
    void validScanParametersTest() {
        Map<String, Object> srmParameters = new HashMap<>();

        srmParameters.put(ApplicationConstants.SRM_URL_KEY, TEST_SRM_SERVER_URL);
        srmParameters.put(ApplicationConstants.SRM_APIKEY_KEY, TEST_SRM_API_KEY_TOKEN);
        srmParameters.put(ApplicationConstants.SRM_PROJECT_NAME_KEY, TEST_SRM_PROJECT_NAME);
        srmParameters.put(ApplicationConstants.SRM_PROJECT_ID_KEY, TEST_SRM_PROJECT_ID);
        srmParameters.put(ApplicationConstants.SRM_ASSESSMENT_TYPES_KEY, TEST_SRM_ASSESSMENT_TYPES);
        srmParameters.put(ApplicationConstants.SRM_BRANCH_NAME_KEY, TEST_SRM_BRANCH_NAME);
        srmParameters.put(ApplicationConstants.SRM_BRANCH_PARENT_KEY, TEST_SRM_BRANCH_PARENT_NAME);

        assertTrue(srmParametersService.hasAllMandatorySrmParams(srmParameters));
    }

    @Test
    void prepareScanInputForBridgeForPolaris_SCA_SAST_ArbitraryParamsTest() {
        Map<String, Object> srmParameters = new HashMap<>();

        srmParameters.put(ApplicationConstants.SRM_URL_KEY, TEST_SRM_SERVER_URL);
        srmParameters.put(ApplicationConstants.SRM_APIKEY_KEY, TEST_SRM_API_KEY_TOKEN);
        srmParameters.put(ApplicationConstants.SRM_PROJECT_NAME_KEY, TEST_SRM_PROJECT_NAME);
        srmParameters.put(ApplicationConstants.SRM_PROJECT_ID_KEY, TEST_SRM_PROJECT_ID);
        srmParameters.put(ApplicationConstants.SRM_ASSESSMENT_TYPES_KEY, TEST_SRM_ASSESSMENT_TYPES);
        srmParameters.put(ApplicationConstants.SRM_BRANCH_NAME_KEY, TEST_SRM_BRANCH_NAME);
        srmParameters.put(ApplicationConstants.SRM_BRANCH_PARENT_KEY, TEST_SRM_BRANCH_PARENT_NAME);
        srmParameters.put(ApplicationConstants.DETECT_SEARCH_DEPTH_KEY, 2);
        srmParameters.put(ApplicationConstants.DETECT_CONFIG_PATH_KEY, TEST_BLACKDUCKSCA_CONFIG_FILE_PATH);
        srmParameters.put(ApplicationConstants.DETECT_ARGS_KEY, TEST_BLACKDUCKSCA_ARGS);
        srmParameters.put(ApplicationConstants.COVERITY_BUILD_COMMAND_KEY, TEST_COVERITY_BUILD_COMMAND);
        srmParameters.put(ApplicationConstants.COVERITY_CLEAN_COMMAND_KEY, TEST_COVERITY_CLEAN_COMMAND);
        srmParameters.put(ApplicationConstants.COVERITY_CONFIG_PATH_KEY, TEST_COVERITY_CONFIG_FILE_PATH);
        srmParameters.put(ApplicationConstants.COVERITY_ARGS_KEY, TEST_COVERITY_ARGS);
        srmParameters.put(ApplicationConstants.SRM_SCA_DETECT_EXECUTION_PATH_KEY, TEST_DETECT_EXECUTION_PATH);
        srmParameters.put(ApplicationConstants.SRM_SAST_EXECUTION_PATH_KEY, TEST_COVERITY_EXECUTION_PATH);

        blackDuckSCAParametersService = new BlackDuckSCAParametersService(listenerMock, envVarsMock);
        coverityParametersService = new CoverityParametersService(listenerMock, envVarsMock);

        SRM srm = srmParametersService.prepareSrmObjectForBridge(srmParameters);
        BlackDuckSCA blackDuckSCA = blackDuckSCAParametersService.prepareBlackDuckSCAObjectForBridge(srmParameters);
        Coverity coverity = coverityParametersService.prepareCoverityObjectForBridge(srmParameters);

        assertEquals(srm.getUrl(), TEST_SRM_SERVER_URL);
        assertEquals(srm.getApikey(), TEST_SRM_API_KEY_TOKEN);
        assertEquals(srm.getProject().getName(), TEST_SRM_PROJECT_NAME);
        assertEquals(srm.getProject().getId(), TEST_SRM_PROJECT_ID);
        assertEquals(srm.getBranch().getName(), TEST_SRM_BRANCH_NAME);
        assertEquals(srm.getBranch().getParent(), TEST_SRM_BRANCH_PARENT_NAME);
        assertEquals(srm.getAssessmentTypes().getTypes(), List.of(TEST_SRM_ASSESSMENT_TYPES));
        assertEquals(2, blackDuckSCA.getSearch().getDepth());
        assertEquals(
                TEST_BLACKDUCKSCA_CONFIG_FILE_PATH, blackDuckSCA.getConfig().getPath());
        assertEquals(TEST_BLACKDUCKSCA_ARGS, blackDuckSCA.getArgs());
        assertEquals(coverity.getBuild().getCommand(), TEST_COVERITY_BUILD_COMMAND);
        assertEquals(coverity.getClean().getCommand(), TEST_COVERITY_CLEAN_COMMAND);
        assertEquals(coverity.getConfig().getPath(), TEST_COVERITY_CONFIG_FILE_PATH);
        assertEquals(coverity.getArgs(), TEST_COVERITY_ARGS);
    }
}
