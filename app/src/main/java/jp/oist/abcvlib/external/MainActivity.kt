package jp.oist.abcvlib.external

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import jp.oist.abcvlib.core.inputs.PublisherManager
import jp.oist.abcvlib.core.inputs.phone.OrientationData
import jp.oist.abcvlib.core.inputs.phone.OrientationDataSubscriber
import jp.oist.abcvlib.external.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity(), OrientationDataSubscriber {
    private val publisherManager = PublisherManager()
    private var pitchDegrees by mutableStateOf<Double?>(null)
    private var lastUiUpdateTimestamp = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        OrientationData.Builder(this, publisherManager)
            .build()
            .addSubscriber(this)
        publisherManager.initializePublishers()
        publisherManager.startPublishers()

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OrientationScreen(
                        pitchDegrees = pitchDegrees,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onOrientationUpdate(
        timestamp: Long,
        thetaRad: Double,
        angularVelocityRad: Double
    ) {
        if (timestamp <= 0 || timestamp - lastUiUpdateTimestamp < UI_UPDATE_INTERVAL_NANOS) {
            return
        }

        val updatedPitchDegrees = OrientationData.getThetaDeg(thetaRad)
        if (!updatedPitchDegrees.isFinite()) {
            return
        }

        lastUiUpdateTimestamp = timestamp
        runOnUiThread {
            pitchDegrees = updatedPitchDegrees
        }
    }

    override fun onResume() {
        super.onResume()
        publisherManager.resumePublishers()
    }

    override fun onPause() {
        publisherManager.pausePublishers()
        super.onPause()
    }

    override fun onDestroy() {
        publisherManager.stopPublishers()
        super.onDestroy()
    }

    private companion object {
        const val UI_UPDATE_INTERVAL_NANOS = 100_000_000L
    }
}

@Composable
private fun OrientationScreen(
    pitchDegrees: Double?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.tilt_phone))
        Text(
            text = pitchDegrees?.let {
                stringResource(R.string.pitch_degrees, it)
            } ?: stringResource(R.string.waiting_for_orientation)
        )
    }
}
