package com.jarvis.deploy.version;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Resolves and validates deployment version strings.
 * Supports semver, snapshot, and timestamp-based versions.
 */
public class VersionResolver {

    private static final Pattern SEMVER = Pattern.compile("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.]+)?$");
    private static final Pattern SNAPSHOT = Pattern.compile("^\\d+\\.\\d+\\.\\d+-SNAPSHOT$");
    private static final Pattern TIMESTAMP = Pattern.compile("^\\d{8}\\.\\d{6}$");

    public VersionResolutionResult resolve(String raw) {
        if (raw == null || raw.isBlank()) {
            return VersionResolutionResult.failure("Version string must not be blank");
        }
        String version = raw.trim();
        VersionType type = detectType(version);
        if (type == VersionType.UNKNOWN) {
            return VersionResolutionResult.failure("Unrecognized version format: " + version);
        }
        return VersionResolutionResult.success(version, type);
    }

    public boolean isStable(String version) {
        if (version == null) return false;
        return SEMVER.matcher(version.trim()).matches() && !SNAPSHOT.matcher(version.trim()).matches();
    }

    public Optional<String> normalize(String version) {
        if (version == null) return Optional.empty();
        String v = version.trim().replaceAll("^v", "");
        if (SEMVER.matcher(v).matches() || TIMESTAMP.matcher(v).matches()) {
            return Optional.of(v);
        }
        return Optional.empty();
    }

    private VersionType detectType(String version) {
        if (SNAPSHOT.matcher(version).matches()) return VersionType.SNAPSHOT;
        if (SEMVER.matcher(version).matches()) return VersionType.SEMVER;
        if (TIMESTAMP.matcher(version).matches()) return VersionType.TIMESTAMP;
        return VersionType.UNKNOWN;
    }
}
