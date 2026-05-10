# Preserve line numbers in crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Jsoup: parser uses internal factory patterns; suppress any missing-class warnings
# from optional javax.xml / org.w3c.dom dependencies it conditionally references
-dontwarn org.jsoup.**

# Timber: no trees are planted in release builds, so Timber calls are no-ops.
# R8 will eliminate them; no keep rules needed.
