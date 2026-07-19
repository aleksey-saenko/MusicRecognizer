package com.mrsep.musicrecognizer.feature.preferences.presentation.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mrsep.musicrecognizer.core.ui.util.copyTextToClipboard
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

private val donationMethods = listOf(
    DonationMethod(
        paymentType = PaymentType.Bitcoin,
        address = "bc1qtxjs9h3yulfhxgpr6ppckv0f6pxgtlx8jat6hy",
        qrCodeAssetName = "btc-qr.png",
    ),
    DonationMethod(
        paymentType = PaymentType.Monero,
        address = "8BEKm3Dd7urgZs1rvYRGLQ8N6jQVkdhZ2VBg6TRdb5BDH87VUGPXThXTTKAM2aUWRmcGTnMX4tC29G5hnvr6mYv2EAxxCP5",
        qrCodeAssetName = "xmr-qr.png",
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DonationBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
) {
    var selectedQrCodeIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Text(
            text = stringResource(StringsR.string.about_pref_title_donation),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(StringsR.string.about_pref_donation_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(16.dp)
                .weight(1f, false),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            donationMethods.forEachIndexed { index, donationMethod ->
                DonationMethodCard(
                    donationMethod = donationMethod,
                    onShowQrCodeClicked = { selectedQrCodeIndex = index },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    selectedQrCodeIndex?.let { addressIndex ->
        val donationMethod = donationMethods[addressIndex]
        AlertDialog(
            onDismissRequest = { selectedQrCodeIndex = null },
            confirmButton = {
                TextButton(onClick = { selectedQrCodeIndex = null }) {
                    Text(text = stringResource(StringsR.string.close))
                }
            },
            title = {
                Text(
                    text = stringResource(donationMethod.paymentType.titleRes()),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "file:///android_asset/${donationMethod.qrCodeAssetName}",
                        contentDescription = stringResource(StringsR.string.qr_code),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .sizeIn(maxWidth = 200.dp, maxHeight = 200.dp)
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.small),
                    )
                }
            }
        )
    }
}


@Composable
private fun DonationMethodCard(
    modifier: Modifier = Modifier,
    donationMethod: DonationMethod,
    onShowQrCodeClicked: () -> Unit,
) {
    val context = LocalContext.current
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(donationMethod.paymentType.iconRes()),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(donationMethod.paymentType.titleRes()),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = donationMethod.address,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { context.copyTextToClipboard(donationMethod.address) }) {
                Icon(
                    painter = painterResource(UiR.drawable.outline_content_copy_24),
                    contentDescription = stringResource(StringsR.string.copy)
                )
            }
            IconButton(onClick = onShowQrCodeClicked) {
                Icon(
                    painter = painterResource(UiR.drawable.outline_qr_code_24),
                    contentDescription = stringResource(StringsR.string.qr_code)
                )
            }
        }
    }
}

private enum class PaymentType { Bitcoin, Monero }

private fun PaymentType.iconRes() = when (this) {
    PaymentType.Bitcoin -> UiR.drawable.outline_currency_bitcoin_24
    PaymentType.Monero -> UiR.drawable.outline_currency_monero_24
}

private fun PaymentType.titleRes() = when (this) {
    PaymentType.Bitcoin -> StringsR.string.bitcoin
    PaymentType.Monero -> StringsR.string.monero
}

private data class DonationMethod(
    val paymentType: PaymentType,
    val address: String,
    val qrCodeAssetName: String,
)
