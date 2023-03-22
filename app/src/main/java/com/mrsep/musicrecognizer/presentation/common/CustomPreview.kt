package com.mrsep.musicrecognizer.presentation.common

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    showSystemUi = true,
    device = "spec:id=reference_phone,shape=Normal,width=430,height=860,unit=dp,dpi=420",
    heightDp = 860,
    widthDp = 430,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark theme variant"
)
annotation class PreviewDeviceNight

@Preview(
    showSystemUi = true,
    device = "spec:id=reference_phone,shape=Normal,width=430,height=860,unit=dp,dpi=420",
    heightDp = 860,
    widthDp = 430,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Dark theme variant"
)
annotation class PreviewDeviceLight