/*
 * This file is part of BaseProject - https://github.com/florianreuth/BaseProject
 * Copyright (C) 2024-2026 Florian Reuth <git@florianreuth.de>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.florianreuth.baseproject

import org.gradle.api.Project
import org.gradle.api.initialization.Settings

/**
 * Includes `<rootProject.name>-<name>` for every name, located in the `<name>` folder.
 *
 * @param names the subproject folder names
 */
fun Settings.setupViaSubprojects(vararg names: String) {
    names.forEach { name ->
        include("${rootProject.name}-$name")
        project(":${rootProject.name}-$name").projectDir = settingsDir.resolve(name)
    }
}

/**
 * Version, channel and changelog of a Modrinth / Hangar upload of a ViaVersion project.
 *
 * @property isRelease whether the project version has no pre-release suffix
 * @property isMainBranch whether the current Git branch is the main branch
 * @property version the project version, suffixed with the GitHub run number for non-releases
 * @property changelog a link to the GitHub releases for releases, otherwise [commitChangelog]
 * @property commitChangelog a link to and the message of the latest commit
 */
class ViaRelease(
    val isRelease: Boolean,
    val isMainBranch: Boolean,
    val version: String,
    val changelog: String,
    val commitChangelog: String
) {
    val modrinthVersionType: String
        get() = if (isRelease) "release" else if (isMainBranch) "beta" else "alpha"

    val hangarChannel: String
        get() = if (isRelease) "Release" else if (isMainBranch) "Snapshot" else "Alpha"
}

/**
 * Collects the release information from the project version and Git; the repository is `ViaVersion/<project_name>`.
 *
 * @param mainBranch the name of the branch releases are published from
 * @return the release information of the current build
 */
fun Project.viaRelease(mainBranch: String): ViaRelease {
    val baseVersion = version.toString()
    val isRelease = !baseVersion.contains('-')
    val repository = "https://github.com/ViaVersion/${property("project_name")}"
    val commitHash = latestCommitHash()
    val commitChangelog = "[$commitHash]($repository/commit/$commitHash) ${latestCommitMessage()}"

    return ViaRelease(
        isRelease = isRelease,
        isMainBranch = branchName() == mainBranch,
        version = if (isRelease) baseVersion else baseVersion + "+" + System.getenv("GITHUB_RUN_NUMBER"),
        changelog = if (isRelease) "See [GitHub]($repository) for release notes." else commitChangelog,
        commitChangelog = commitChangelog
    )
}
