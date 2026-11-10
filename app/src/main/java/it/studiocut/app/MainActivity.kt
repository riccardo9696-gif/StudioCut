package it.studiocut.app

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

private val Bg = Color(0xFF101116)
private val Panel = Color(0xFF1B1D27)
private val Accent = Color(0xFFA58BFF)
private val Muted = Color(0xFFA6A9B8)

class MainActivity : ComponentActivity() {
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StudioCutApp() }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun StudioCutApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val images = remember { mutableStateListOf<Uri>() }
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var duration by remember { mutableIntStateOf(4) }
    var format by remember { mutableStateOf("Verticale 9:16") }
    var exporting by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var status by remember { mutableStateOf("Crea un progetto importando le tue foto.") }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(40)
    ) { selected ->
        selected.forEach { uri ->
            try { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            if (!images.contains(uri)) images.add(uri)
        }
        status = if (selected.isEmpty()) "Nessuna foto selezionata." else "${images.size} elementi nella timeline."
    }

    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            audioUri = uri
            status = "Traccia audio aggiunta."
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            secondary = Color(0xFF5DE0D0),
            background = Bg,
            surface = Panel,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Scaffold(containerColor = Bg) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 14.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("STUDIOCUT", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("VIDEO CREATION STUDIO", fontSize = 10.sp, color = Muted, letterSpacing = 2.sp)
                    }
                    Surface(color = Color(0xFF30294A), shape = RoundedCornerShape(50)) {
                        Text("FREE · OFFLINE", Modifier.padding(horizontal = 11.dp, vertical = 7.dp), color = Color(0xFFE2D9FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VideoLibrary, null, tint = Accent)
                            Spacer(Modifier.width(8.dp))
                            Text("Il tuo progetto", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Spacer(Modifier.weight(1f))
                            Text("${images.size} foto", color = Muted, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Box(
                            Modifier.fillMaxWidth().height(150.dp).background(Color(0xFF090A0F), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Movie, null, tint = Accent, modifier = Modifier.size(38.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(if (images.isEmpty()) "La tua anteprima" else "Progetto pronto per l'esportazione", color = Muted, fontSize = 13.sp)
                                Text("MP4 · H.264", color = Color(0xFF5DE0D0), fontSize = 11.sp)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Accent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(7.dp))
                            Text("Importa fotografie", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(13.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoLibrary, null, tint = Accent)
                            Spacer(Modifier.width(8.dp))
                            Text("Timeline", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            Text("Riordina le foto", color = Muted, fontSize = 11.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        if (images.isEmpty()) {
                            Text("Le immagini appariranno qui. Puoi selezionarne fino a 40.", color = Muted, fontSize = 12.sp)
                        } else {
                            LazyColumn(Modifier.heightIn(max = 210.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                itemsIndexed(images, key = { _, uri -> uri.toString() }) { index, uri ->
                                    Row(
                                        Modifier.fillMaxWidth().background(Color(0xFF252734), RoundedCornerShape(10.dp)).padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${index + 1}", color = Accent, fontWeight = FontWeight.Bold, modifier = Modifier.width(25.dp))
                                        Text(uri.lastPathSegment?.takeLast(22) ?: "Foto", Modifier.weight(1f), fontSize = 11.sp, color = Color.White, maxLines = 1)
                                        IconButton(onClick = { if (index > 0) { val item = images.removeAt(index); images.add(index - 1, item) } }, enabled = index > 0) {
                                            Icon(Icons.Default.ArrowUpward, null, tint = Muted)
                                        }
                                        IconButton(onClick = { if (index < images.lastIndex) { val item = images.removeAt(index); images.add(index + 1, item) } }, enabled = index < images.lastIndex) {
                                            Icon(Icons.Default.ArrowDownward, null, tint = Muted)
                                        }
                                        IconButton(onClick = { images.removeAt(index) }) {
                                            Icon(Icons.Default.Delete, null, tint = Color(0xFFFF7B86))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(13.dp)) {
                        Text("Impostazioni di esportazione", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text("Formato", color = Muted, fontSize = 11.sp)
                                var expanded by remember { mutableStateOf(false) }
                                Box {
                                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(format, fontSize = 11.sp) }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        listOf("Verticale 9:16", "Orizzontale 16:9", "Quadrato 1:1").forEach { option ->
                                            DropdownMenuItem(text = { Text(option) }, onClick = { format = option; expanded = false })
                                        }
                                    }
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                Text("Durata foto", color = Muted, fontSize = 11.sp)
                                var expanded by remember { mutableStateOf(false) }
                                Box {
                                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text("$duration secondi", fontSize = 11.sp) }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        listOf(2, 3, 4, 5, 6, 8).forEach { value ->
                                            DropdownMenuItem(text = { Text("$value secondi") }, onClick = { duration = value; expanded = false })
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { audioPicker.launch(arrayOf("audio/*")) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.MusicNote, null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (audioUri == null) "Aggiungi musica dal telefono" else "Cambia traccia musicale")
                        }
                        if (audioUri != null) Text("Musica selezionata", color = Color(0xFF5DE0D0), fontSize = 11.sp)
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (images.isEmpty()) status = "Aggiungi almeno una foto prima di esportare."
                                else {
                                    exporting = true
                                    progress = 0f
                                    status = "Preparazione esportazione MP4…"
                                    scope.launch {
                                        try {
                                            exportProject(context, images.toList(), audioUri, duration, format) { p ->
                                                progress = p
                                                status = "Esportazione: ${(p * 100).toInt()}%"
                                            }
                                            status = "Video salvato in Galleria > Movies/StudioCut"
                                            Toast.makeText(context, "Video MP4 creato", Toast.LENGTH_LONG).show()
                                        } catch (e: Exception) {
                                            status = "Esportazione non riuscita: ${e.message ?: "errore"}"
                                        } finally { exporting = false }
                                    }
                                }
                            },
                            enabled = !exporting && images.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth().height(49.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Accent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (exporting) "Esportazione in corso…" else "ESPORTA VIDEO MP4", fontWeight = FontWeight.ExtraBold)
                        }
                        if (exporting) {
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = Accent)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(status, color = Muted, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Elaborazione locale · Nessun account · Nessun credito · Nessun abbonamento", color = Muted, fontSize = 10.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
            }
        }
    }
}

@OptIn(UnstableApi::class)
private suspend fun exportProject(
    context: Context,
    imageUris: List<Uri>,
    audioUri: Uri?,
    secondsPerImage: Int,
    outputFormat: String,
    onProgress: (Float) -> Unit
): Uri = withContext(Dispatchers.Main) {
    val temp = File(context.cacheDir, "studiocut_${System.currentTimeMillis()}.mp4")
    val editedImages = imageUris.map { uri ->
        val mediaItem = androidx.media3.common.MediaItem.Builder()
            .setUri(uri)
            .setImageDurationMs(secondsPerImage * 1000L)
            .build()
        androidx.media3.transformer.EditedMediaItem.Builder(mediaItem).setFrameRate(30).build()
    }
    val videoSequence = androidx.media3.transformer.EditedMediaItemSequence.withVideoFrom(editedImages)
    val sequences = mutableListOf(videoSequence)
    if (audioUri != null) {
        val audioItem = androidx.media3.transformer.EditedMediaItem.Builder(
            androidx.media3.common.MediaItem.fromUri(audioUri)
        ).build()
        val audioSequence = androidx.media3.transformer.EditedMediaItemSequence
            .withAudioFrom(listOf(audioItem)).buildUpon().setIsLooping(true).build()
        sequences.add(audioSequence)
    }
    val aspectRatio = when (outputFormat) {
        "Verticale 9:16" -> 9f / 16f
        "Quadrato 1:1" -> 1f
        else -> 16f / 9f
    }
    val presentation = androidx.media3.effect.Presentation.createForAspectRatio(
        aspectRatio,
        androidx.media3.effect.Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP
    )
    val composition = androidx.media3.transformer.Composition.Builder(sequences)
        .setEffects(androidx.media3.transformer.Effects(emptyList(), listOf(presentation)))
        .build()
    val finished = kotlinx.coroutines.CompletableDeferred<Result<Unit>>()
    val listener = object : androidx.media3.transformer.Transformer.Listener {
        override fun onCompleted(composition: androidx.media3.transformer.Composition, exportResult: androidx.media3.transformer.ExportResult) {
            finished.complete(Result.success(Unit))
        }
        override fun onError(composition: androidx.media3.transformer.Composition, exportResult: androidx.media3.transformer.ExportResult, exportException: androidx.media3.transformer.ExportException) {
            finished.complete(Result.failure(exportException))
        }
    }
    val exporter = androidx.media3.transformer.Transformer.Builder(context)
        .setVideoMimeType(androidx.media3.common.MimeTypes.VIDEO_H264)
        .setAudioMimeType(androidx.media3.common.MimeTypes.AUDIO_AAC)
        .addListener(listener)
        .build()
    exporter.start(composition, temp.absolutePath)
    val progressHolder = androidx.media3.transformer.ProgressHolder()
    while (!finished.isCompleted) {
        if (exporter.getProgress(progressHolder) == androidx.media3.transformer.Transformer.PROGRESS_STATE_AVAILABLE) {
            onProgress(progressHolder.progress / 100f)
        }
        kotlinx.coroutines.delay(400)
    }
    finished.await().getOrThrow()
    val values = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, "StudioCut_${System.currentTimeMillis()}.mp4")
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/StudioCut")
        put(MediaStore.Video.Media.IS_PENDING, 1)
    }
    val resolver = context.contentResolver
    val outputUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        ?: throw IllegalStateException("Impossibile creare il file nella Galleria.")
    try {
        resolver.openOutputStream(outputUri)?.use { out -> FileInputStream(temp).use { input -> input.copyTo(out) } }
            ?: throw IllegalStateException("Impossibile scrivere il video.")
        resolver.update(outputUri, ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }, null, null)
        temp.delete()
        outputUri
    } catch (e: Exception) {
        resolver.delete(outputUri, null, null)
        temp.delete()
        throw e
    }
}
