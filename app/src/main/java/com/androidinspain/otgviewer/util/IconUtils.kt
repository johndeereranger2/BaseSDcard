/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.androidinspain.otgviewer.util

import com.androidinspain.otgviewer.R
import java.util.*

object IconUtils {
    private val sMimeIcons: HashMap<Any?, Any?> =
        HashMap<Any?, Any?>()

    private fun add(mimeType: String, resId: Int) {
        if (sMimeIcons.put(mimeType, resId) != null) {
            throw RuntimeException("$mimeType already registered!")
        }
    }

    fun loadMimeIcon(mimeType: String?): Int {

        // Look for exact match first
        val resId = sMimeIcons[mimeType]
        if (resId != null) {
            return resId as Int
        }
        if (mimeType == null) {
            // TODO: generic icon?
            return R.drawable.ic_doc_generic
        }

        // Otherwise look for partial match
        val typeOnly = mimeType.split("/".toRegex()).toTypedArray()[0]
        return if ("audio" == typeOnly) {
            R.drawable.ic_doc_audio
        } else if ("image" == typeOnly) {
            R.drawable.ic_doc_image
        } else if ("text" == typeOnly) {
            R.drawable.ic_doc_text
        } else if ("video" == typeOnly) {
            R.drawable.ic_doc_video
        } else {
            R.drawable.ic_doc_generic
        }
    }

    init {
        var icon: Int

        // Package
        icon = R.drawable.ic_doc_apk
        add(
            "application/vnd.android.package-archive",
            icon
        )

        // Audio
        icon = R.drawable.ic_doc_audio
        add("application/ogg", icon)
        add("application/x-flac", icon)

        // Certificate
        icon = R.drawable.ic_doc_certificate
        add("application/pgp-keys", icon)
        add("application/pgp-signature", icon)
        add("application/x-pkcs12", icon)
        add("application/x-pkcs7-certreqresp", icon)
        add("application/x-pkcs7-crl", icon)
        add("application/x-x509-ca-cert", icon)
        add("application/x-x509-user-cert", icon)
        add("application/x-pkcs7-certificates", icon)
        add("application/x-pkcs7-mime", icon)
        add("application/x-pkcs7-signature", icon)

        // Source code
        icon = R.drawable.ic_doc_codes
        add("application/rdf+xml", icon)
        add("application/rss+xml", icon)
        add("application/x-object", icon)
        add("application/xhtml+xml", icon)
        add("text/css", icon)
        add("text/html", icon)
        add("text/xml", icon)
        add("text/x-c++hdr", icon)
        add("text/x-c++src", icon)
        add("text/x-chdr", icon)
        add("text/x-csrc", icon)
        add("text/x-dsrc", icon)
        add("text/x-csh", icon)
        add("text/x-haskell", icon)
        add("text/x-java", icon)
        add("text/x-literate-haskell", icon)
        add("text/x-pascal", icon)
        add("text/x-tcl", icon)
        add("text/x-tex", icon)
        add("application/x-latex", icon)
        add("application/x-texinfo", icon)
        add("application/atom+xml", icon)
        add("application/ecmascript", icon)
        add("application/json", icon)
        add("application/javascript", icon)
        add("application/xml", icon)
        add("text/javascript", icon)
        add("application/x-javascript", icon)

        // Compressed
        icon = R.drawable.ic_doc_compressed
        add("application/mac-binhex40", icon)
        add("application/rar", icon)
        add("application/zip", icon)
        add("application/x-apple-diskimage", icon)
        add("application/x-debian-package", icon)
        add("application/x-gtar", icon)
        add("application/x-iso9660-image", icon)
        add("application/x-lha", icon)
        add("application/x-lzh", icon)
        add("application/x-lzx", icon)
        add("application/x-stuffit", icon)
        add("application/x-tar", icon)
        add("application/x-webarchive", icon)
        add("application/x-webarchive-xml", icon)
        add("application/gzip", icon)
        add("application/x-7z-compressed", icon)
        add("application/x-deb", icon)
        add("application/x-rar-compressed", icon)

        // Contact
        icon = R.drawable.ic_doc_contact
        add("text/x-vcard", icon)
        add("text/vcard", icon)

        // Event
        icon = R.drawable.ic_doc_event
        add("text/calendar", icon)
        add("text/x-vcalendar", icon)

        // Font
        icon = R.drawable.ic_doc_font
        add("application/x-font", icon)
        add("application/font-woff", icon)
        add("application/x-font-woff", icon)
        add("application/x-font-ttf", icon)

        // Image
        icon = R.drawable.ic_doc_image
        add(
            "application/vnd.oasis.opendocument.graphics",
            icon
        )
        add(
            "application/vnd.oasis.opendocument.graphics-template",
            icon
        )
        add(
            "application/vnd.oasis.opendocument.image",
            icon
        )
        add("application/vnd.stardivision.draw", icon)
        add("application/vnd.sun.xml.draw", icon)
        add(
            "application/vnd.sun.xml.draw.template",
            icon
        )

        // PDF
        icon = R.drawable.ic_doc_pdf
        add("application/pdf", icon)

        // Presentation
        icon = R.drawable.ic_doc_presentation
        add(
            "application/vnd.stardivision.impress",
            icon
        )
        add("application/vnd.sun.xml.impress", icon)
        add(
            "application/vnd.sun.xml.impress.template",
            icon
        )
        add("application/x-kpresenter", icon)
        add(
            "application/vnd.oasis.opendocument.presentation",
            icon
        )

        // Spreadsheet
        icon = R.drawable.ic_doc_spreadsheet
        add(
            "application/vnd.oasis.opendocument.spreadsheet",
            icon
        )
        add(
            "application/vnd.oasis.opendocument.spreadsheet-template",
            icon
        )
        add("application/vnd.stardivision.calc", icon)
        add("application/vnd.sun.xml.calc", icon)
        add(
            "application/vnd.sun.xml.calc.template",
            icon
        )
        add("application/x-kspread", icon)

        // Text
        icon = R.drawable.ic_doc_text
        add(
            "application/vnd.oasis.opendocument.text",
            icon
        )
        add(
            "application/vnd.oasis.opendocument.text-master",
            icon
        )
        add(
            "application/vnd.oasis.opendocument.text-template",
            icon
        )
        add(
            "application/vnd.oasis.opendocument.text-web",
            icon
        )
        add("application/vnd.stardivision.writer", icon)
        add(
            "application/vnd.stardivision.writer-global",
            icon
        )
        add("application/vnd.sun.xml.writer", icon)
        add(
            "application/vnd.sun.xml.writer.global",
            icon
        )
        add(
            "application/vnd.sun.xml.writer.template",
            icon
        )
        add("application/x-abiword", icon)
        add("application/x-kword", icon)

        // Video
        icon = R.drawable.ic_doc_video
        add("application/x-quicktimeplayer", icon)
        add("application/x-shockwave-flash", icon)

        // Word
        icon = R.drawable.ic_doc_word
        add("application/msword", icon)
        add(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            icon
        )
        add(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
            icon
        )

        // Excel
        icon = R.drawable.ic_doc_excel
        add("application/vnd.ms-excel", icon)
        add(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            icon
        )
        add(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
            icon
        )

        // Powerpoint
        icon = R.drawable.ic_doc_powerpoint
        add("application/vnd.ms-powerpoint", icon)
        add(
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            icon
        )
        add(
            "application/vnd.openxmlformats-officedocument.presentationml.template",
            icon
        )
        add(
            "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
            icon
        )
    }
}