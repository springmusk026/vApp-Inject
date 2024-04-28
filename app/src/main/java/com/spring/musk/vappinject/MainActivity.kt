package com.spring.musk.vappinject

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fvbox.lib.FCore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.spring.musk.vappinject.databinding.ActivityMainBinding
import com.spring.musk.vappinject.utils.vAppU

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissionsAndProceed()

    }

    private fun checkPermissionsAndProceed() {
        val filePermissionGranted = isFilePermissionGranted()
        val installPermissionGranted = isInstallPermissionGranted()

        if (filePermissionGranted && installPermissionGranted) {
            // Proceed with your logic after both permissions are granted
            proceedAfterPermissions()
        } else {
            if (!installPermissionGranted) {
                requestInstallPermission()
            }
            if (!filePermissionGranted) {
                requestFilePermission()
            }
        }
    }

    private fun isInstallPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            packageManager.canRequestPackageInstalls()
        } else {
            true // Permissions are not required for older versions
        }
    }

    private fun isFilePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, INSTALL_PERMISSION_REQUEST_CODE)
        }
    }

    private fun requestFilePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            FILE_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FILE_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // Check permissions again after file permission is granted
            checkPermissionsAndProceed()
        }
    }

    private fun StartApp(){
        binding.launchgame.visibility = View.VISIBLE
        binding.launchgame.setOnClickListener {
            FCore.get().launchApk(PACKAGE_NAME, USER_ID)
        }
    }
    private fun proceedAfterPermissions() {
        if (isPackageInstalledWithSameArch(PACKAGE_NAME)) {
            if (!FCore.get().isInstalled(PACKAGE_NAME, USER_ID)) {
                FCore.get().installPackageAsUser(PACKAGE_NAME, USER_ID)
            }
            if(vAppU.isinternalobb(this@MainActivity, PACKAGE_NAME)){
                return StartApp()
            }
            vAppU.copyobb(PACKAGE_NAME, this@MainActivity) {
                showDialog(
                    title = "Successful",
                    message = "Setup Successful, Restart app to continue.",
                    dismissOnClick = false
                )
            }
        } else {
            showDialog(
                title = "Error",
                message = "Please install the $PACKAGE_NAME first."
            )
        }
    }
    private fun isPackageInstalledWithSameArch(packageName: String): Boolean {
        return try {
            val targetAppInfo = packageManager.getApplicationInfo(packageName, 0)
            val currentAppInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_SHARED_LIBRARY_FILES)

            val targetAppAbi = getAbiFromNativeLibraryDir(targetAppInfo.nativeLibraryDir)
            val currentAppAbi = getAbiFromNativeLibraryDir(currentAppInfo.nativeLibraryDir)

            targetAppAbi == currentAppAbi
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun getAbiFromNativeLibraryDir(nativeLibraryDir: String): String {
        val supportedAbis = Build.SUPPORTED_ABIS
        for (abi in supportedAbis) {
            if (nativeLibraryDir.contains(abi)) {
                return abi
            }
        }
        return ""
    }

    private fun showDialog(
        title: String? = null,
        message: String? = null,
        dismissOnClick: Boolean = true
    ) {
        MaterialAlertDialogBuilder(this).apply {
            title?.let {
                setTitle(it)
            }
            message?.let { setMessage(it) }
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                if (dismissOnClick) {
                    finish()
                }
            }
            setCancelable(false)
                .show()
        }
    }

    companion object {
        private const val FILE_PERMISSION_REQUEST_CODE = 1002
        private const val INSTALL_PERMISSION_REQUEST_CODE = 1001
        val USER_ID = 0
        val PACKAGE_NAME = "com.dts.freefireth"
    }
}
