package com.example.chatapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.example.chatapp.model.SocketManager
import com.example.chatapp.ui.theme.ChatAppTheme
import io.socket.client.Socket

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SocketManager.initialize()
        val socket = SocketManager.getSocket()
        socket.connect()

        if(socket.isActive) {
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
            Log.e("Socket", "Is active")
        } else {
            Toast.makeText(this, "NOt Connected", Toast.LENGTH_SHORT).show()
            Log.e("Socket", "Is not active")
        }

        setContent {
            ChatAppTheme {
                SocketIOApp(socket)
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocketIOApp(socket: Socket) {
    var message by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(socket) {
        socket.on("message") { args ->
            if (args.isNotEmpty()) {
                val newMessage = args[0] as String
                chatMessages = chatMessages + newMessage
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Socket.IO Chat") })
        },
        content = { padding ->
            ChatAppContent(
                padding,
                chatMessages,
                message,
                {
                    message = it
                }, {
                    if (message.isNotBlank()) {
                        socket.emit("message", message)
                        message = ""
                    }
            })
        }
    )
}

@Composable
private fun ChatAppContent(
    padding: PaddingValues,
    chatMessages: List<String>,
    message: String,
    onMessageChange: (String) -> Unit = {},
    onSendMessage: () -> Unit
) {
    var message1 = message
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Top
        ) {
            chatMessages.forEach { msg ->
                Text(text = msg, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                StyledBasicTextField(
                    value = message1,
                    onValueChange = { onMessageChange(message1) },
                    modifier = Modifier
                )
            }

            Button(onClick = {
                 onSendMessage()
            }) {
                Text("Send")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatAppContentPreview() {
    ChatAppTheme {
        ChatAppContent(
            padding = PaddingValues(0.dp),
            chatMessages = listOf("Hello!", "How are you?"),
            message = "Test message",
            onMessageChange = {},
            onSendMessage = {}
        )
    }
}

@Composable
fun StyledBasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier // Accepting modifier as a parameter
) {
    val isFocused = remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .background(
                color = Color.White,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (isFocused.value) Color.Gray else Color.Black,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .onFocusChanged {
                isFocused.value = it.isFocused
            },
        textStyle = androidx.compose.ui.text.TextStyle(
            color = Color.Black,
            fontSize = androidx.compose.ui.unit.TextUnit.Unspecified
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty() && !isFocused.value) {
                    Text(
                        text = "Write message",
                        color = Color.Gray
                    )
                }
                innerTextField()
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun StyledBasicTextFieldPreview(
    @PreviewParameter(MessagePreviewProvider::class) message: String
) {
    StyledBasicTextField(
        value = message,
        onValueChange = {}
    )
}
class MessagePreviewProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf(
        "", // Empty input
        "Hello, world!", // Short input
        "This is a long message to test how the BasicTextField handles longer text inputs." // Long input
    )
    override val count: Int = values.count()
}