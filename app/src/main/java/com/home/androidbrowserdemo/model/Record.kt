package com.home.androidbrowserdemo.model

class Record {

    var title: String? = null
    var url: String? = null
    var time: Long = 0

    constructor() {
        this.title = null
        this.url = null
        this.time = 0L
    }

    constructor(title: String, url: String, time: Long) {
        this.title = title
        this.url = url
        this.time = time
    }
}
