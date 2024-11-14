# Technical details about the handling of NPM packages

## matchbox-engine

When used by itself, the _matchbox-engine_ requires a dedicated directory in the filesystem to store the installed 
packages NPM files.
This directory can be configured with the `MatchboxEngineBuilder.setPackageCacheMode(FilesystemPackageCacheMode)`  
method:

| Value   | Directory location on Windows | Directory location on other OS |
|---------|-------------------------------|--------------------------------|
| USER    | _$user.home_/.fhir/packages   | _$user.home_/.fhir/packages    |
| SYSTEM  | ProgramData\.fhir\packages    | /var/lib/.fhir/packages        |
| TESTING | c:\temp\.fhir\packages        | _$tmp_/.fhir/packages          |

See HAPI's `FilesystemPackageCacheManager` class for more details about these values.

A custom directory location can also be used with the `MatchboxEngineBuilder.setPackageCachePath(String)` method.

## matchbox-server

When initiated by _matchbox-server_, the _matchbox-engine_ is configured with our `IgLoaderFromJpaPackageCache` IG 
loader.
Because of that, the resources and packages are stored in the database instead of the filesystem (including all 
resources, and the NPM file content itself).

This allows serving the original NPM file in the `ImplementationGuiderPackageInterceptor` class.

Then, the loading of IGs in `matchbox-engine`s is controlled by the server:

1. When using `MatchboxEngineSupport.getMatchboxEngine()`, the IG loading is controller by the `canonical` parameter 
   and the `CliContext.ig` property.
2. Otherwise, specific calls to `MatchboxEngine.loadPackage()` or `MatchboxEngine.getIgLoader().loadIg()` can be used 
   to load a specific IG.

In all cases, the IGs are loaded from the database, so make sure they are installed there first.
