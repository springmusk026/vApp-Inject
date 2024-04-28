package com.spring.musk.vappinject.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class vAppU {


    companion object {


        fun haveFilePermission(activity: Activity) {
            val filePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    activity,
                    filePermission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(activity, arrayOf(filePermission), 1002)
            }
        }

        fun isinternalobb(context: Context, packagename: String): Boolean {
            val internaldatadir = context.dataDir.absolutePath
            val obbpath = File("${internaldatadir}/storage/emulated/0/Android/obb/$packagename")
            return runBlocking {
                if (!obbpath.exists()) {
                    obbpath.mkdirs()
                }

                val thisisobb = obbpath.listFiles { file ->
                    file.isFile && file.name.endsWith(".obb", ignoreCase = true)
                }

                thisisobb?.forEach { obb ->
                    return@runBlocking true
                }
                return@runBlocking false
            }
        }

        public fun isexternalobb(packagename: String): Boolean {
            val obbpathsource = File("/storage/emulated/0/Android/obb/$packagename")
            return runBlocking {
                if (obbpathsource != null) {
                    if (!obbpathsource.exists()) {
                        obbpathsource.mkdirs()
                    }
                }
                val thisisobb2 = obbpathsource.listFiles { file ->
                    file.isFile && file.name.endsWith(".obb", ignoreCase = true)
                }
                thisisobb2?.forEach { obb ->
                    return@runBlocking true
                }
                return@runBlocking false
            }
        }

        public fun copyobb(
            packagename: String,
            context: Context,
            onFailed: () -> Unit
        ) {
            runBlocking {
                val internal = isinternalobb(context, packagename)
                val external = isexternalobb(packagename)
                if (external) {
                    if (internal) {
                        return@runBlocking
                    } else {
                        val sourceobbdir = File("/storage/emulated/0/Android/obb/$packagename")
                        val internaldatadir = context.dataDir.absolutePath
                        val destinationpath =
                            File("${internaldatadir}/storage/emulated/0/Android/obb/$packagename")
                        val getobb = sourceobbdir.listFiles { file ->
                            file.isFile && file.name.endsWith(".obb", ignoreCase = true)
                        }
                        var obbname: File? = null
                        getobb?.forEach {
                            obbname = it
                        }
                        if (!destinationpath.exists()) {
                            destinationpath.mkdirs()
                        }
                        val destFile = obbname?.name?.let {
                            File(destinationpath, it)
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val input = FileInputStream(obbname)
                                val output = FileOutputStream(destFile)
                                val buffer = ByteArray(1024)
                                var lenth: Int
                                while (input.read(buffer).also {
                                        lenth = it
                                    } > 0) {
                                    output.write(buffer, 0, lenth)
                                }
                                input.close()
                                output.close()
                            } catch (err: IOException) {
                                err.printStackTrace()
                            }
                        }
                    }
                } else {
                    onFailed()
                }
            }
        }
    }
}