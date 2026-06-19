# TODO https://issuetracker.google.com/issues/488790212
-keep class * extends androidx.glance.appwidget.action.ActionCallback { <init>(); }
-keep public class * extends androidx.glance.appwidget.GlanceAppWidget { *; }