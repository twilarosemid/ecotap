package com.example.ecotap.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log

class NfcManager(private val activity: Activity) : NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private var onTagDiscoveredListener: ((String) -> Unit)? = null

    val isNfcAvailable: Boolean
        get() = nfcAdapter != null

    val isNfcEnabled: Boolean
        get() = nfcAdapter?.isEnabled == true

    fun setOnTagDiscoveredListener(listener: (String) -> Unit) {
        this.onTagDiscoveredListener = listener
    }

    fun enableReaderMode() {
        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

        val options = Bundle()
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

        try {
            nfcAdapter?.enableReaderMode(activity, this, flags, options)
        } catch (e: Exception) {
            Log.e("NfcManager", "Error enabling NFC Reader Mode: ${e.message}")
        }
    }

    fun disableReaderMode() {
        try {
            nfcAdapter?.disableReaderMode(activity)
        } catch (e: Exception) {
            Log.e("NfcManager", "Error disabling NFC Reader Mode: ${e.message}")
        }
    }

    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) return
        val tagIdBytes = tag.id ?: return
        val tagHex = tagIdBytes.joinToString("") { "%02X".format(it) }
        val formattedNfcId = "ECO-TAP-$tagHex"
        Log.d("NfcManager", "NFC Tag Discovered: $formattedNfcId")

        activity.runOnUiThread {
            onTagDiscoveredListener?.invoke(formattedNfcId)
        }
    }
}
