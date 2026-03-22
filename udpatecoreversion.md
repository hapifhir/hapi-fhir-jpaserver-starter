# org.hl7.fhir.core

matchbox depends on org.hl7.fhir.core. the project is available on [github](https://github.com/hapifhir/org.hl7.fhir.core) and does frequent releases https://github.com/hapifhir/org.hl7.fhir.core/releases which have to be integrated in matchbox.

matchbox has patched a few classes, that's why an update cannot be done just through referencing mvn.

the following steps need to be performed:

## 1. Sync the forked project and checkout the release tag

```
cd ../org.hl7.fhir.core/
git checkout master
git fetch upstream
git merge upstream/master
git push
```

Check out the latest release tag (e.g. `v6.9.1`):

```
git checkout -b oe_$(git describe --tags --abbrev=0)
cd ../matchbox
```

**Note:** If `git merge upstream/master` has conflicts, you can skip the merge and directly checkout the tag:
```
git fetch upstream --tags
git checkout -b oe_v6.9.1 v6.9.1
```

## 2. Update matchbox version references

Update `fhir.core.version` in the root `pom.xml` to the new version:

```xml
<fhir.core.version>6.9.1</fhir.core.version>
```

## 3. Run updatehapi.sh to copy fresh files

```
./updatehapi.sh
```

This copies files from `../org.hl7.fhir.core/` into `matchbox-engine/src/main/java/`. After this step, all matchbox patches are lost and need to be re-applied.

**Note:** If files listed in `updatehapi.sh` have been removed in the new core release (e.g. `Params.java` was removed in 6.9.1 due to picocli refactoring), the copy will fail for those files. Check if matchbox imported or used those files — if not, they can be safely ignored.

## 4. Re-apply matchbox patches

All matchbox-specific patches are marked with `matchbox patch` comments in the source code. Use `git diff` to review what `updatehapi.sh` overwrote, then manually re-apply each patch.

### Patched files and their patches

The following files have matchbox-specific patches (as of 6.9.1 update):

| File | Patch Description | Issue |
|------|------------------|-------|
| `BaseWorkerContext.java` | Locale handling (preserve locale across engine copies) | #425 |
| `BaseWorkerContext.java` | Duplicate resource handling (drop+re-register instead of throw) | #227, #452 |
| `BaseWorkerContext.java` | fhirVersioned URL stripping in fetchResourceWithExceptionByVersion | #265 |
| `StructureMapUtilities.java` | createType in processTarget uses services.createType() | #264 |
| `StructureMapUtilities.java` | CDA type matching: URL suffix comparison in matchesType | CDA support |
| `StructureMapUtilities.java` | DEF_GROUP_NAME handling in executeRule | 6.9.1 compat |
| `FHIRPathHostServices.java` | FML resolve() in Bundle | #359 |
| `Element.java` | Choice type handling in setProperty and convertToElement | FML suppor
t |
| `Property.java` | getName() path-based name, getChild() choice type matching | FML support |
| `XmlParser.java` | ST.r2b datatype handling (dot replacement in xsiType) | #439 |
| `BaseValidator.java` | ruleInv suppression uses invId instead of theMessage | Validation |
| `BundleValidator.java` | Profile-based validation of document bundle entries | #348 |
| `InstanceValidator.java` | Various patches (isNoService casing, resolveReferencesInBundle) | Multiple |
| `TestDataFactory.java` | 3-param lookup | #1942 |
| `ProfileUtilities.java` | findProfile | #424 |
| `ValidatorResourceFetcher.java` | Signature updates for VersionResolutionRules | 6.9.1 compat |
| `MatchboxEngine.java` | setValidatorFactory for InstanceValidator | 6.9.1 compat |
| `MatchboxService.java` | InstanceValidatorParameters for validate() | 6.9.1 compat |
| `HapiWorkerContext.java` | Add missing IWorkerContext 6.9.1 methods (VersionResolutionRules, expandVS, etc.) | 6.9.1 compat |

### Strategy for re-applying patches

1. **Before running updatehapi.sh**, save the current patched files or use `git stash` / `git diff` to capture the patches.
2. After running updatehapi.sh, use `git diff` to see all changes. The diff shows both removed patches AND new upstream changes.
3. For each patched file, re-apply ONLY the matchbox-specific changes (marked with `matchbox patch`). Do NOT revert upstream 6.9.1 changes.
4. Some patches may need adaptation if the surrounding code changed significantly.

## 5. Handle API changes

Common types of API changes between core versions:

- **New method parameters**: e.g. `VersionResolutionRules` parameter added to `fetchResource`, `resolveURL`, etc.
- **Method renames**: e.g. `IsNoService()` → `isNoService()` in 6.9.1
- **Signature changes**: e.g. `validate()` now takes `InstanceValidatorParameters` instead of `List<String>` profiles
- **New constants**: e.g. `DEF_GROUP_NAME` for anonymous group rules in StructureMap
- **Removed classes**: e.g. `Params.java` removed in 6.9.1 (picocli refactor)

Check the core release notes and changelog for breaking changes.

## 6. Build and test

```bash
# Compile first to catch API changes
cd matchbox-engine
mvn clean compile

# Fix any compilation errors, then run tests
mvn clean test

# Also compile the server
cd ../matchbox-server
mvn clean compile
```

### Common test failures after a core update

- **Changed error message IDs**: The validator may use different message keys (e.g. `Extension_EXTP_Context_Wrong` vs `Extension_EXTP_Context_Wrong_VER`). Check the `Messages.properties` in `org.hl7.fhir.utilities`.
- **Changed message text format**: Error messages may include additional information (e.g. version in extension URLs).
- **CDA type matching**: CDA StructureDefinitions may change their `type` field format (short name vs full URL). The `matchesType` method in StructureMapUtilities handles this.
- **Whitespace handling**: XML attribute whitespace may be preserved differently between versions.
- **New validator behavior**: The InstanceValidator may require explicit factory setup (e.g. `context.setValidatorFactory(new InstanceValidatorFactory())`).

## 7. Create PR

After all tests pass, create a branch and PR with the changes.
