package ar.edu.uade.piotask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ar.edu.uade.piotask.core.theme.PioTaskTheme
import ar.edu.uade.piotask.presentation.PioTaskApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PioTaskTheme {
                PioTaskApp()
            }
        }
    }
}
