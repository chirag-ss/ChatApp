package com.example.chatapp.model

import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketManager {

    private lateinit var socket: Socket

    fun initialize() {
        try {
            socket = IO.socket("http://10.0.2.2:3000") // Use 10.0.2.2 for local development
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun getSocket(): Socket = socket
}