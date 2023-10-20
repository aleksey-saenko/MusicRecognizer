package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.R
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun ApiTokenDialog(
    onConfirmClick: (String) -> Unit,
    onDismissClick: () -> Unit,
    initialToken: String
) {
    var token by rememberSaveable { mutableStateOf(initialToken) }
    AlertDialog(
        title = {
            Text(text = stringResource(StringsR.string.api_token))
        },
        confirmButton = {
            Button(onClick = { onConfirmClick(token) }) {
                Text(text = stringResource(StringsR.string.apply))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.cancel))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(StringsR.string.token_preference_dialog_message),
                )
                var passwordVisible by rememberSaveable { mutableStateOf(false) }
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    value = token,
                    onValueChange = { token = it },
                    label = { Text(stringResource(StringsR.string.audd_api_token)) },
                    trailingIcon = {
                        val iconPainter = painterResource(
                            if (passwordVisible) {
                                R.drawable.baseline_visibility_off_24
                            } else {
                                R.drawable.baseline_visibility_24
                            }
                        )
                        val iconDesc = stringResource(
                            if (passwordVisible) {
                                StringsR.string.hide_token
                            } else {
                                StringsR.string.show_token
                            }
                        )
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = iconPainter,
                                contentDescription = iconDesc
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    )
                )
            }
        },
        onDismissRequest = onDismissClick
    )
}