package dev.strohmnative.strohm

import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

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
                val mainKey = MasterKey.Builder(
                    strohm.context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                    // MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
                val stateFile = File(strohm.context.filesDir, "state.enc")
                stateFile.delete()
                val encryptedFile = EncryptedFile.Builder(
                    strohm.context,
                    stateFile,
                    mainKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                ).build()
                val outputStream = encryptedFile.openFileOutput()
                val writer = OutputStreamWriter(outputStream, "UTF-8")
                writer.write(s)
                writer.flush()
                writer.close()
                outputStream.flush()
                outputStream.close()
            } catch (e: IOException) {
                // TODO: handle exceptions
                Log.e("StatePersister", "failed to persist state", e)
            }
        }
    }

    fun loadState(): String? {
        try {
            val mainKey = MasterKey.Builder(
                strohm.context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
                //MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val stateFile = File(strohm.context.filesDir, "state.enc")
            val encryptedFile = EncryptedFile.Builder(
                strohm.context,
                stateFile,
                mainKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            val inputStream = encryptedFile.openFileInput()
            val reader = InputStreamReader(inputStream, "UTF-8")
            val state = reader.readText()
            reader.close()
            return state
        } catch (e: IOException) {
            // TODO: handle exceptions
            Log.e("StatePersister", "failed to load state", e)
            return null
        }
    }
}
