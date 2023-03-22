package com.mrsep.musicrecognizer.presentation.workshop.legacy

//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.DialogProperties
//import androidx.compose.ui.window.SecureFlagPolicy
//import com.mrsep.musicrecognizer.R

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun BaseMessageDialog(
//    onDismissClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    content: @Composable () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismissClick,
//        modifier = modifier,
//        properties = DialogProperties(
//            dismissOnBackPress = true,
//            dismissOnClickOutside = false,
//            securePolicy = SecureFlagPolicy.Inherit,
//            usePlatformDefaultWidth = true,
//            decorFitsSystemWindows = true
//        ),
//        content = content
//    )
//}
//
//@Composable
//private fun BadConnectionDialog8(
//    modifier: Modifier = Modifier,
//    onDismissClick: () -> Unit,
//    onNavigateToQueue: () -> Unit
//) {
//    BaseMessageDialog(
//        onDismissClick = onDismissClick,
//        modifier = modifier
//    ) {
//        Surface(
//            shape = MaterialTheme.shapes.extraLarge
//        ) {
//            Column(
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Icon(
//                    painter = painterResource(R.drawable.baseline_cloud_off_24),
//                    modifier = Modifier
//                        .padding(24.dp)
//                        .size(48.dp),
//                    contentDescription = null
//                )
//                Text(
//                    text = "No internet connection",
//                    style = MaterialTheme.typography.headlineSmall,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier
//                        .padding(horizontal = 24.dp)
//                        .padding(bottom = 16.dp)
//                )
//                Text(
//                    text = stringResource(R.string.internet_not_available),
//                    style = MaterialTheme.typography.bodyLarge,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.padding(horizontal = 24.dp)
//                )
//                Divider(modifier = Modifier.fillMaxWidth().padding(top = 24.dp))
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 12.dp)
//                        .padding(vertical = 16.dp)
//                ) {
//                    TextButton(
//                        onClick = onDismissClick
//                    ) {
//                        Text(text = "DISMISS")
//                    }
//                    TextButton(
//                        onClick = onNavigateToQueue
//                    ) {
//                        Text(text = "RECOGNITION QUEUE")
//                    }
//                }
//            }
//        }
//    }
//
//}