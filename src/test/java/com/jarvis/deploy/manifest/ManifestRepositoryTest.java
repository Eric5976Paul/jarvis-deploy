package com.jarvis.deploy.manifest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ManifestRepositoryTest {

    private ManifestRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ManifestRepository();
    }

    private DeploymentManifest buildManifest(String id, String env, String artifact, String version) {
        return new DeploymentManifest(id, env, artifact, version, Map.of("key", "val"), "ci-bot");
    }

    @Test
    void saveAndFindById_shouldReturnManifest() {
        DeploymentManifest manifest = buildManifest("m-001", "prod", "my-app", "1.0.0");
        repository.save(manifest);

        Optional<DeploymentManifest> found = repository.findById("m-001");
        assertTrue(found.isPresent());
        assertEquals("prod", found.get().getEnvironment());
    }

    @Test
    void findById_unknownId_shouldReturnEmpty() {
        assertTrue(repository.findById("nonexistent").isEmpty());
    }

    @Test
    void findById_nullId_shouldReturnEmpty() {
        assertTrue(repository.findById(null).isEmpty());
    }

    @Test
    void findByEnvironment_shouldReturnMatchingManifests() {
        repository.save(buildManifest("m-001", "staging", "app-a", "1.0"));
        repository.save(buildManifest("m-002", "staging", "app-b", "2.0"));
        repository.save(buildManifest("m-003", "prod", "app-a", "1.0"));

        List<DeploymentManifest> stagingManifests = repository.findByEnvironment("staging");
        assertEquals(2, stagingManifests.size());
        assertTrue(stagingManifests.stream().allMatch(m -> "staging".equals(m.getEnvironment())));
    }

    @Test
    void findByStatus_shouldReturnOnlySealedManifests() {
        DeploymentManifest draft = buildManifest("m-001", "dev", "app", "1.0");
        DeploymentManifest sealed = buildManifest("m-002", "prod", "app", "2.0");
        sealed.seal();

        repository.save(draft);
        repository.save(sealed);

        List<DeploymentManifest> sealedList = repository.findByStatus(ManifestStatus.SEALED);
        assertEquals(1, sealedList.size());
        assertEquals("m-002", sealedList.get(0).getManifestId());
    }

    @Test
    void delete_shouldRemoveManifest() {
        repository.save(buildManifest("m-001", "dev", "app", "1.0"));
        assertTrue(repository.delete("m-001"));
        assertFalse(repository.findById("m-001").isPresent());
    }

    @Test
    void delete_nonExistentId_shouldReturnFalse() {
        assertFalse(repository.delete("ghost-id"));
    }

    @Test
    void count_shouldReflectCurrentSize() {
        assertEquals(0, repository.count());
        repository.save(buildManifest("m-001", "dev", "app", "1.0"));
        assertEquals(1, repository.count());
        repository.clear();
        assertEquals(0, repository.count());
    }

    @Test
    void save_nullManifest_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void sealManifest_thenSealAgain_shouldThrow() {
        DeploymentManifest manifest = buildManifest("m-001", "prod", "app", "1.0");
        manifest.seal();
        assertThrows(IllegalStateException.class, manifest::seal);
    }
}
