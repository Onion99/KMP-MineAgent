package com.onion.model.rule

import kotlinx.serialization.Serializable

/**
 * 书籍列表规则
 */
@Serializable
data class BookListRule (
    var bookList: String? = "",
    var name: String? = "",
    var author: String? = "",
    var intro: String? = "",
    var kind: String? = "",
    var lastChapter: String? = "",
    var updateTime: String? = "",
    var bookUrl: String? = "",
    var coverUrl: String? = "",
    var wordCount: String? = "",
)