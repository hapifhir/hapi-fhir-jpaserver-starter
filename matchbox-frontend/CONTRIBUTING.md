# Contributing to matchbox-formfiller

## Publish a new version to github pages

To publish a new version to github pages follow these steps:

```
git checkout master (can also be done via pull request)
yarn version
# enter the new version for example 0.3.1
git push && git push --tags
```

This will update the `package.json` of matchbox-formfiller and commit a git tag with the new version.
If a git tag on the master branch is pushed a build to publish the application to github pages is triggered.
