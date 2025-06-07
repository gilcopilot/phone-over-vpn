package com.example.openvpnsmshandler

import kotlinx.coroutines.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class HTTPRequest(
    @SerializedName("type") val type: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("message") val message: String = ""
)

class HTTPServer {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val gson = Gson()
    suspend fun start(port: Int, requestHandler: (HTTPRequest) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true
                while (isRunning) {
                    val clientSocket = serverSocket?.accept()
                    clientSocket?.let { socket ->
                        launch {
                            handleClient(socket, requestHandler)
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle server error
            }
        }
    }
    private suspend fun handleClient(socket: Socket, requestHandler: (HTTPRequest) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val writer = PrintWriter(socket.getOutputStream(), true)
                val requestLine = reader.readLine()
                val headers = mutableMapOf<String, String>()
                var line: String?
                while (reader.readLine().also { line = it } != null && line!!.isNotEmpty()) {
                    val parts = line!!.split(": ", limit = 2)
                    if (parts.size == 2) {
                        headers[parts[0]] = parts[1]
                    }
                }
                var body = ""
                if (requestLine.contains("POST")) {
                    val contentLength = headers["Content-Length"]?.toIntOrNull() ?: 0
                    if (contentLength > 0) {
                        val bodyChars = CharArray(contentLength)
                        reader.read(bodyChars)
                        body = String(bodyChars)
                    }
                }
                if (body.isNotEmpty()) {
                    try {
                        val request = gson.fromJson(body, HTTPRequest::class.java)
                        requestHandler(request)
                        writer.println("HTTP/1.1 200 OK")
                        writer.println("Content-Type: application/json")
                        writer.println("Connection: close")
                        writer.println()
                        writer.println("{\"status\":\"success\"}")
                    } catch (e: Exception) {
                        writer.println("HTTP/1.1 400 Bad Request")
                        writer.println("Content-Type: application/json")
                        writer.println("Connection: close")
                        writer.println()
                        writer.println("{\"status\":\"error\",\"message\":\"${e.message}\"}")
                    }
                } else {
                    writer.println("HTTP/1.1 405 Method Not Allowed")
                    writer.println("Connection: close")
                    writer.println()
                }
                socket.close()
            } catch (e: Exception) {
                // Handle client error
            }
        }
    }
    fun stop() {
        isRunning = false
        serverSocket?.close()
    }
}
