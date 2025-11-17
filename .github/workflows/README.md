# GitHub Actions Workflows

This directory contains GitHub Actions workflows for building and releasing Spectrum.

## Workflows

### 1. Build (`build.yml`)
- **Triggers**: Push to main/master/develop, pull requests, manual trigger
- **What it does**:
  - Builds both Debug and Release configurations
  - Runs on Ubuntu (Linux is sufficient for Android builds)
  - Uploads all artifacts (AARs and APKs)
  - Runs unit tests
- **Matrix builds**: Debug and Release

### 2. CI (`ci.yml`)
- **Triggers**: Pull requests only
- **What it does**:
  - Quick Debug build only (faster feedback)
  - Runs lint checks
  - Uploads lint reports
- **Purpose**: Fast validation for PRs

### 3. Release (`release.yml`)
- **Triggers**: Git tags starting with 'v*' or manual trigger
- **What it does**:
  - Builds Release configuration only
  - Creates GitHub Release with artifacts
  - Uploads all release AARs and APK
- **Usage**: 
  - Tag a commit: `git tag v1.0.0 && git push --tags`
  - Or manually trigger with version input

## Configuration

All workflows use:
- **JDK 17** (required for Android Gradle Plugin 8.x)
- **NDK 28.2.13676358** (for native code compilation)
- **Kotlin 2.2.0** (latest stable)
- **Gradle caching** for faster builds
- **Latest GitHub Actions** (v4) for all actions to avoid deprecation warnings

## Build Status Badges

Add these to your main README.md:

```markdown
[![Build](https://github.com/mayankst/spectrum/actions/workflows/build.yml/badge.svg)](https://github.com/mayankst/spectrum/actions/workflows/build.yml)
[![CI](https://github.com/mayankst/spectrum/actions/workflows/ci.yml/badge.svg)](https://github.com/mayankst/spectrum/actions/workflows/ci.yml)
```

## Secrets Required

No secrets are required for building. The workflows use the default `GITHUB_TOKEN` for creating releases.

## Customization

To customize the workflows:

1. **Change NDK version**: Update the version in the "Install NDK" step
2. **Change build variants**: Modify the matrix strategy or build commands
3. **Add signing**: Add signing configuration and secrets for release APKs
4. **Add deployment**: Add steps to deploy to Maven Central, Google Play, etc.

## Troubleshooting

If builds fail:

1. **Out of Memory**: The workflows use the JVM args from `gradle.properties` which sets `-Xmx4096m`
2. **NDK not found**: Ensure the NDK version matches what's in `build.gradle`
3. **Gradle wrapper**: The workflows use `./gradlew` - ensure it's committed to the repo
4. **Native libraries**: The workflows always prepare native libraries before building
