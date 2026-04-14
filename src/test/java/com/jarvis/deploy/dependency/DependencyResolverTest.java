package com.jarvis.deploy.dependency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DependencyResolverTest {

    private DependencyResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new DependencyResolver();
    }

    @Test
    void checkReturnsSuccessWhenNoDependenciesRegistered() {
        DependencyCheckResult result = resolver.check("service-a", Map.of());
        assertTrue(result.isSatisfied());
        assertTrue(result.getUnsatisfiedDependencies().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    @Test
    void checkSatisfiesHardDependencyWhenRequiredServiceIsDeployed() {
        resolver.register(new DeploymentDependency("service-a", "service-b", "1.0.0", DependencyType.HARD));
        DependencyCheckResult result = resolver.check("service-a", Map.of("service-b", "1.0.0"));
        assertTrue(result.isSatisfied());
    }

    @Test
    void checkFailsHardDependencyWhenRequiredServiceMissing() {
        resolver.register(new DeploymentDependency("service-a", "service-b", "1.0.0", DependencyType.HARD));
        DependencyCheckResult result = resolver.check("service-a", Map.of());
        assertFalse(result.isSatisfied());
        assertEquals(1, result.getUnsatisfiedDependencies().size());
        assertTrue(result.getUnsatisfiedDependencies().get(0).contains("service-b"));
    }

    @Test
    void checkAddsSoftDependencyWarningWhenMissing() {
        resolver.register(new DeploymentDependency("service-a", "service-c", "2.0.0", DependencyType.SOFT));
        DependencyCheckResult result = resolver.check("service-a", Map.of());
        assertTrue(result.isSatisfied());
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().get(0).contains("service-c"));
    }

    @Test
    void checkSatisfiesVersionConstraintWhenDeployedVersionIsHigherOrEqual() {
        resolver.register(new DeploymentDependency("service-a", "service-d", "1.2.0", DependencyType.VERSION_CONSTRAINT));
        DependencyCheckResult result = resolver.check("service-a", Map.of("service-d", "1.3.0"));
        assertTrue(result.isSatisfied());
    }

    @Test
    void checkFailsVersionConstraintWhenDeployedVersionIsLower() {
        resolver.register(new DeploymentDependency("service-a", "service-d", "2.0.0", DependencyType.VERSION_CONSTRAINT));
        DependencyCheckResult result = resolver.check("service-a", Map.of("service-d", "1.9.9"));
        assertFalse(result.isSatisfied());
        assertFalse(result.getUnsatisfiedDependencies().isEmpty());
    }

    @Test
    void checkHandlesMultipleDependenciesCorrectly() {
        resolver.register(new DeploymentDependency("service-a", "service-b", "1.0.0", DependencyType.HARD));
        resolver.register(new DeploymentDependency("service-a", "service-c", "1.0.0", DependencyType.SOFT));
        resolver.register(new DeploymentDependency("service-a", "service-d", "1.0.0", DependencyType.VERSION_CONSTRAINT));

        Map<String, String> deployed = new HashMap<>();
        deployed.put("service-b", "1.0.0");
        deployed.put("service-d", "1.0.0");
        // service-c not deployed (soft)

        DependencyCheckResult result = resolver.check("service-a", deployed);
        assertTrue(result.isSatisfied());
        assertTrue(result.hasWarnings());
    }

    @Test
    void getDependenciesReturnsRegisteredDependencies() {
        DeploymentDependency dep = new DeploymentDependency("service-a", "service-b", "1.0.0", DependencyType.HARD);
        resolver.register(dep);
        assertEquals(1, resolver.getDependencies("service-a").size());
        assertTrue(resolver.getDependencies("service-a").contains(dep));
    }

    @Test
    void clearRemovesAllDependencies() {
        resolver.register(new DeploymentDependency("service-a", "service-b", "1.0.0", DependencyType.HARD));
        resolver.clear();
        assertTrue(resolver.getDependencies("service-a").isEmpty());
    }
}
