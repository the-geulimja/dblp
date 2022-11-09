package org.dblp

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.doublereceive.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.dblp.db.initDbConnection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

fun main(): Unit = runBlocking {
    /**
     * This cron job implementation follows the following link.
     * @link: https://stackoverflow.com/questions/59516599/how-to-run-cron-jobs-in-kotlin-ktor
     *
     * TODO: Inspect performance difference between this implementation and using an actual library.
     * @link: https://github.com/justwrote/kjob
     * */
    
    var hasBeenInitialised = false
    val now = ZonedDateTime.now().plusSeconds(1)
    
    launch {

        if (!hasBeenInitialised) {

            // No matter the time of installation, the first check will always be at 9:00 next day.
            val tomorrowMorningAtNine = now
                .plusDays(1)
                .withHour(9)
                .minusMinutes(now.minute.toLong())

            delay(tomorrowMorningAtNine.toInstant().toEpochMilli() - now.toInstant().toEpochMilli())

            checkRegisteredIssueStatus()

            hasBeenInitialised = true

        }

        while (hasBeenInitialised) {

            // Daily check at 9:00.
            delay(24 * 60 * 60 * 1000)
            checkRegisteredIssueStatus()

        }

    }

    embeddedServer(Netty, port = 8080, watchPaths = listOf("classes")) {
        install(DoubleReceive)
        initDbConnection()
        configureRouting()
    }.start(wait = true)
}

val config: Config by lazy { ConfigFactory.load() }

val log: Logger = LoggerFactory.getLogger("ApplicationKt")