package com.powertrack.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.powertrack.mobile.ui.navigation.PowerTrackNavGraph
import com.powertrack.mobile.ui.theme.PowerTrackTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host de todo el árbol de Compose. No contiene lógica de
 * pantalla: solo aplica el tema y monta el NavGraph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PowerTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    PowerTrackNavGraph()
                }
            }
        }
    }
}
