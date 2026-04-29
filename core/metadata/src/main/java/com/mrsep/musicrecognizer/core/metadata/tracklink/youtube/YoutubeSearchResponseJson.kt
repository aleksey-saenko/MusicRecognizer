package com.mrsep.musicrecognizer.core.metadata.tracklink.youtube

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class YoutubeSearchResponseJson(
    @SerialName("contents")
    val contents: YoutubeSearchContentsJson?,
) {
    @Serializable
    internal data class YoutubeSearchContentsJson(
        @SerialName("twoColumnSearchResultsRenderer")
        val twoColumnSearchResultsRenderer: TwoColumnSearchResultsRendererJson?,
    )

    @Serializable
    internal data class TwoColumnSearchResultsRendererJson(
        @SerialName("primaryContents")
        val primaryContents: PrimaryContentsJson?,
    )

    @Serializable
    internal data class PrimaryContentsJson(
        @SerialName("sectionListRenderer")
        val sectionListRenderer: SectionListRendererJson?,
    )

    @Serializable
    internal data class SectionListRendererJson(
        @SerialName("contents")
        val contents: List<SectionContentJson?>?,
    )

    @Serializable
    internal data class SectionContentJson(
        @SerialName("itemSectionRenderer")
        val itemSectionRenderer: ItemSectionRendererJson?,
    )

    @Serializable
    internal data class ItemSectionRendererJson(
        @SerialName("contents")
        val contents: List<ItemContentJson?>?,
    )

    @Serializable
    internal data class ItemContentJson(
        @SerialName("videoRenderer")
        val videoRenderer: VideoRendererJson?,
    )

    @Serializable
    internal data class VideoRendererJson(
        @SerialName("videoId")
        val videoId: String?,
        @SerialName("thumbnail")
        val thumbnail: ThumbnailContainerJson?,
        @SerialName("title")
        val title: TextBlockJson?,
        @SerialName("longBylineText")
        val longBylineText: TextBlockJson?,
        @SerialName("lengthText")
        val lengthText: TextBlockJson?,
    )
}

@Serializable
internal data class YoutubeMusicSearchResponseJson(
    @SerialName("contents")
    val contents: YoutubeMusicContentsJson?,
) {
    @Serializable
    internal data class YoutubeMusicContentsJson(
        @SerialName("tabbedSearchResultsRenderer")
        val tabbedSearchResultsRenderer: TabbedSearchResultsRendererJson?,
    )

    @Serializable
    internal data class TabbedSearchResultsRendererJson(
        @SerialName("tabs")
        val tabs: List<TabJson?>?,
    )

    @Serializable
    internal data class TabJson(
        @SerialName("tabRenderer")
        val tabRenderer: TabRendererJson?,
    )

    @Serializable
    internal data class TabRendererJson(
        @SerialName("content")
        val content: TabContentJson?,
    )

    @Serializable
    internal data class TabContentJson(
        @SerialName("sectionListRenderer")
        val sectionListRenderer: SectionListRendererJson?,
    )

    @Serializable
    internal data class SectionListRendererJson(
        @SerialName("contents")
        val contents: List<SectionContentJson?>?,
    )

    @Serializable
    internal data class SectionContentJson(
        @SerialName("musicShelfRenderer")
        val musicShelfRenderer: MusicShelfRendererJson?,
    )

    @Serializable
    internal data class MusicShelfRendererJson(
        @SerialName("contents")
        val contents: List<MusicShelfItemJson?>?,
    )

    @Serializable
    internal data class MusicShelfItemJson(
        @SerialName("musicResponsiveListItemRenderer")
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRendererJson?,
    )

    @Serializable
    internal data class MusicResponsiveListItemRendererJson(
        @SerialName("musicItemRendererDisplayPolicy")
        val musicItemRendererDisplayPolicy: String?,
        @SerialName("thumbnail")
        val thumbnail: MusicThumbnailContainerJson?,
        @SerialName("overlay")
        val overlay: MusicOverlayJson?,
        @SerialName("flexColumns")
        val flexColumns: List<MusicFlexColumnJson?>?,
        @SerialName("playlistItemData")
        val playlistItemData: PlaylistItemDataJson?,
    )

    @Serializable
    internal data class MusicThumbnailContainerJson(
        @SerialName("musicThumbnailRenderer")
        val musicThumbnailRenderer: MusicThumbnailRendererJson?,
    )

    @Serializable
    internal data class MusicThumbnailRendererJson(
        @SerialName("thumbnail")
        val thumbnail: ThumbnailContainerJson?,
    )

    @Serializable
    internal data class MusicOverlayJson(
        @SerialName("musicItemThumbnailOverlayRenderer")
        val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRendererJson?,
    )

    @Serializable
    internal data class MusicItemThumbnailOverlayRendererJson(
        @SerialName("content")
        val content: MusicOverlayContentJson?,
    )

    @Serializable
    internal data class MusicOverlayContentJson(
        @SerialName("musicPlayButtonRenderer")
        val musicPlayButtonRenderer: MusicPlayButtonRendererJson?,
    )

    @Serializable
    internal data class MusicPlayButtonRendererJson(
        @SerialName("playNavigationEndpoint")
        val playNavigationEndpoint: NavigationEndpointJson?,
    ) {
        @Serializable
        internal data class NavigationEndpointJson(
            @SerialName("watchEndpoint")
            val watchEndpoint: WatchEndpointJson?,
        )

        @Serializable
        internal data class WatchEndpointJson(
            @SerialName("videoId")
            val videoId: String?,
        )
    }

    @Serializable
    internal data class MusicFlexColumnJson(
        @SerialName("musicResponsiveListItemFlexColumnRenderer")
        val musicResponsiveListItemFlexColumnRenderer: MusicResponsiveListItemFlexColumnRendererJson?,
    )

    @Serializable
    internal data class MusicResponsiveListItemFlexColumnRendererJson(
        @SerialName("text")
        val text: TextBlockJson?,
    )

    @Serializable
    internal data class PlaylistItemDataJson(
        @SerialName("videoId")
        val videoId: String?,
    )
}

@Serializable
internal data class ThumbnailContainerJson(
    @SerialName("thumbnails")
    val thumbnails: List<ThumbnailJson?>?,
) {
    @Serializable
    internal data class ThumbnailJson(
        @SerialName("url")
        val url: String?,
        @SerialName("width")
        val width: Int?,
        @SerialName("height")
        val height: Int?,
    )
}

@Serializable
internal data class TextBlockJson(
    @SerialName("runs")
    val runs: List<TextRunJson?>?,
    @SerialName("simpleText")
    val simpleText: String?,
    @SerialName("accessibility")
    val accessibility: AccessibilityJson?,
) {
    @Serializable
    internal data class TextRunJson(
        @SerialName("text")
        val text: String?,
    )

    @Serializable
    internal data class AccessibilityJson(
        @SerialName("accessibilityData")
        val accessibilityData: AccessibilityDataJson?,
    )

    @Serializable
    internal data class AccessibilityDataJson(
        @SerialName("label")
        val label: String?,
    )
}
