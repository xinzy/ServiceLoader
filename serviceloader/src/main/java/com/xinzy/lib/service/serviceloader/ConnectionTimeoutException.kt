package com.xinzy.lib.service.serviceloader

import java.lang.RuntimeException

class ConnectionTimeoutException : RuntimeException {

    constructor()

    constructor(msg: String) : super(msg)

    constructor(msg: String, exception: Throwable) : super(msg, exception)
}