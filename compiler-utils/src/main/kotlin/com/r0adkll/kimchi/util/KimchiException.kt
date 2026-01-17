// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode

public class KimchiException(
  message: String,
  public val node: KSNode? = null,
  cause: Throwable? = null,
) : Exception(message, cause) {

  override fun toString(): String {
    return "KimchiException: $message\n${nodeDetails()}"
  }

  private fun nodeDetails(): String {
    if (node != null) {
      return """
        KSNode: ${node::class.simpleName}
        ${
        when (node) {
          is KSClassDeclaration -> "Class: ${node.qualifiedName?.asString()}"
          is KSFunctionDeclaration -> "Function: ${node.qualifiedName?.asString()}"
          else -> "Unknown: $node"
        }
      }
        File: ${node.safeContainingFile()?.filePath}
      """.trimIndent()
    }
    return ""
  }
}

private fun KSNode.safeContainingFile(): KSFile? = try {
  containingFile
} catch (e: Exception) {
  null
}
