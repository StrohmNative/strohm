package dev.strohmnative.strohm

import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.*
import java.lang.RuntimeException
import java.nio.charset.Charset

class StatePersister internal constructor(val strohm: Strohm) {

    init {
        strohm.comms.registerHandlerFunction("persistState") { args ->
            this.persistStateHandler(args)
        }
    }

    private fun persistStateHandler(args: CommsHandlerArguments) {
        val state = args["state"] as? String
        state?.let { s ->
            try {
                val mainKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
                val tempFile = File(strohm.context.filesDir, "newState.enc")
                val encryptedFile = EncryptedFile.Builder(
                    tempFile,
                    strohm.context,
                    mainKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                ).build()
                val outputStream = encryptedFile.openFileOutput()
                val writer = OutputStreamWriter(outputStream, "UTF-8")
                writer.write(s)
                writer.close()

                val realStateFile = File(strohm.context.filesDir, "state.enc")
                if (realStateFile.exists()) { realStateFile.delete() }
                tempFile.renameTo(realStateFile)
            } catch (e: RuntimeException) {
                // TODO: handle exceptions
                Log.e("StatePersister", "failed to persist state", e)
            }
        }
    }
}
