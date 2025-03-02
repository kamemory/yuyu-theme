package com.iwagawake.yuyu.gradle.plugin

data class BaseThemeDefinition(
    val id: String,
    val name: String,
    val dark: Boolean,
    val group: String,
    val character: String,
    val author: String,
    val themeColor: String,
    val editorScheme: String,
    val colors: Map<String, Any>? = null,
    val foregroundColor: String? = null,
)