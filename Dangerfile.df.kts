// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
import systems.danger.kotlin.*

danger(args) {

  val allSourceFiles = git.modifiedFiles + git.createdFiles
  val changelogChanged = allSourceFiles.any { it.contains("CHANGELOG.md") }
  val sourceChanges = allSourceFiles.firstOrNull { it.contains("src") }
  val testChanges = allSourceFiles.firstOrNull { it.contains("test") }

  onGitHub {
    val isTrivial = pullRequest.title.contains("#trivial")

    message("This PR has been checked by Danger")

    // Changelog
    if (!isTrivial && !changelogChanged) {
      error(
        "any changes to library code should be reflected in the Changelog.\n\n" +
          "Please add your change there and adhere to the " +
          "[Changelog Guidelines](https://github.com/Moya/contributors/blob/master/Changelog%20Guidelines.md).",
      )
    }

    // Testing
    if (sourceChanges != null && testChanges == null) {
      fail("any changes to library code should have accompanied tests. Please add tests to cover your changes.")
    }

    // Big PR Check
    if ((pullRequest.additions ?: 0) - (pullRequest.deletions ?: 0) > 300) {
      warn("Big PR, try to keep changes smaller if you can")
    }

    // Work in progress check
    if (pullRequest.title.contains("WIP", false)) {
      warn("PR is classed as Work in Progress")
    }

    if (git.linesOfCode > 500) {
      warn("This PR is original Xbox Huge! Consider breaking into smaller PRs")
    }
  }
}
