# Code Cleanup Checklist

The following files and configurations have been identified as extraneous, unused, or temporary artifacts that should be removed from the project.

## Temporary/Generated Files
- [ ] **Heap Dump:** `java_pid279217.hprof` - Large memory dump file, likely from a crash or profile.
- [ ] **Kotlin Error Logs:** `.kotlin/errors/errors-1766342322671.log` - Temporary error log.
- [ ] **Gradle Cache/State:** `.gradle/` directory - Contains local build cache and state.
- [ ] **IDE Configuration/Cache:** `.idea/` directory - Contains local IDE settings and caches.
- [ ] **Local Properties:** `local.properties` - Contains local SDK paths, should not be shared.
- [ ] **Tool Settings:** `.claude/settings.local.json` - Local user settings for external tool.

## Configuration Updates
- [ ] **Update .gitignore:** The current `.gitignore` is too sparse. It should be updated to exclude the files listed above (standard Android ignore pattern).
