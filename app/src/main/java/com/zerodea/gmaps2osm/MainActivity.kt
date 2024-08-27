package com.zerodea.gmaps2osm

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.zerodea.gmaps2osm.ui.theme.GMaps2OsmTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val hasLink = intent.action == "android.intent.action.VIEW"

        enableEdgeToEdge()
        setContent {
            GMaps2OsmTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (hasLink) {
                        LinkDeciphering(
                            intent.data.toString(),
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(24.dp)
                        )
                    } else {
                        InfoScreen(
                            onButtonClicked = { openSystemSettings() },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LinkDeciphering(
    url: String,
    modifier: Modifier = Modifier
) {
    var hasDecipheredLink by remember { mutableStateOf(false) }
    var decipheredLink by remember { mutableStateOf("") }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    webViewClient = (object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            url ?: return
                            if (!hasDecipheredLink && url.contains("@")) {
                                Log.d("g20", "url: $url")
                                val coordinates = url.dropWhile { it != '@' }
                                    .drop(1)
                                    .split(',')
                                    .take(2)
                                decipheredLink = "geo:${coordinates[0]},${coordinates[1]}"
                                val uri = Uri.parse(decipheredLink)
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        uri,
                                    )
                                )
                                hasDecipheredLink = true
                            }
                        }
                    })
                    @SuppressLint("SetJavaScriptEnabled")
                    settings.javaScriptEnabled = true
                }
            },
            update = {
                it.loadUrl(url)
            },
            modifier = Modifier.size(1.dp),
        )

        if (!hasDecipheredLink) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Deciphering the link might take a couple seconds",
                    textAlign = TextAlign.Center,
                )

                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                )
            }
        } else {
            Text("Link successfully deciphered: $decipheredLink")
        }
    }
}

@Composable
fun InfoScreen(
    onButtonClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "How to setup:",
        )

        Text(
            "1. Go to Settings > Apps > GMaps 2 Osm > or click on the button at the bottom of this screen",
        )

        Text(
            "2. Click \"Open By Default\"",
        )

        Text(
            "3. Enable \"Open supported links\"",
        )

        Text(
            "4. Click \"Add link\"",
        )

        Text(
            "5. select all the links and click \"Add\"",
        )

        Text(
            "Now when opening a Google Maps link the link will redirect to your OSM client app",
        )

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Note")
                }
                append(": The device")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(" must not ")
                }
                append("running Google Play Services. If it is disable it.\n")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("CAUTION")
                }
                append(": This might cause errors in other apps or in the system")
            },
        )

        Button(
            onClick = onButtonClicked,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("Open App Settings")
        }
    }
}

fun Context.openSystemSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }.let {
        startActivity(it)
    }
}
