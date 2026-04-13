package com.jarvis.deploy.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentTagRegistryTest {

    private DeploymentTagRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DeploymentTagRegistry();
    }

    private DeploymentTag tag(String name, String env, String version, Map<String, String> labels) {
        return new DeploymentTag(name, env, version, labels);
    }

    @Test
    void registerAndFind_returnsTag() {
        DeploymentTag t = tag("release-1", "prod", "1.0.0", Map.of("team", "backend"));
        registry.register(t);
        Optional<DeploymentTag> found = registry.find("prod", "release-1");
        assertTrue(found.isPresent());
        assertEquals("1.0.0", found.get().getVersion());
    }

    @Test
    void find_missingTag_returnsEmpty() {
        assertTrue(registry.find("staging", "nonexistent").isEmpty());
    }

    @Test
    void register_nullTag_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> registry.register(null));
    }

    @Test
    void remove_existingTag_returnsTrueAndRemoves() {
        DeploymentTag t = tag("hotfix", "staging", "2.1.1", null);
        registry.register(t);
        assertTrue(registry.remove("staging", "hotfix"));
        assertTrue(registry.find("staging", "hotfix").isEmpty());
    }

    @Test
    void remove_nonExistingTag_returnsFalse() {
        assertFalse(registry.remove("prod", "ghost"));
    }

    @Test
    void findByEnvironment_returnsOnlyMatchingTags() {
        registry.register(tag("t1", "prod", "1.0", null));
        registry.register(tag("t2", "prod", "1.1", null));
        registry.register(tag("t3", "staging", "1.0", null));
        List<DeploymentTag> prodTags = registry.findByEnvironment("prod");
        assertEquals(2, prodTags.size());
        assertTrue(prodTags.stream().allMatch(t -> "prod".equals(t.getEnvironment())));
    }

    @Test
    void findByLabel_returnsMatchingTags() {
        registry.register(tag("a", "prod", "1.0", Map.of("owner", "alice")));
        registry.register(tag("b", "staging", "1.0", Map.of("owner", "alice")));
        registry.register(tag("c", "prod", "1.0", Map.of("owner", "bob")));
        List<DeploymentTag> aliceTags = registry.findByLabel("owner", "alice");
        assertEquals(2, aliceTags.size());
    }

    @Test
    void size_reflectsRegisteredCount() {
        assertEquals(0, registry.size());
        registry.register(tag("x", "dev", "0.1", null));
        assertEquals(1, registry.size());
    }

    @Test
    void clear_removesAllTags() {
        registry.register(tag("x", "dev", "0.1", null));
        registry.clear();
        assertEquals(0, registry.size());
    }

    @Test
    void tag_blankName_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentTag("", "prod", "1.0", null));
    }

    @Test
    void tag_hasLabel_worksCorrectly() {
        DeploymentTag t = tag("rel", "prod", "1.0", Map.of("critical", "true"));
        assertTrue(t.hasLabel("critical"));
        assertFalse(t.hasLabel("optional"));
        assertEquals("true", t.getLabel("critical"));
    }
}
