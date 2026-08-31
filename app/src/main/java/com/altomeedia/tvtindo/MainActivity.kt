package com.altomeedia.tvtindo

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.altomeedia.tvtindo.databinding.ActivityMainBinding
import com.altomeedia.tvtindo.data.datastore.SettingsPreferences
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsPreferences: SettingsPreferences

    private var pkgSlot1 = "com.netflix.ninja"
    private var pkgSlot2 = "com.google.android.youtube.tv"
    private var pkgSlot3 = "com.unitv.player"

    // 1. PENDENGAR SISTEM: Mendeteksi saat Anda memasang/menghapus aplikasi
    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Segera memperbarui daftar aplikasi
            carregarGradeDeApps()
        }
    }

    private val pickBannerImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                lifecycleScope.launch {
                    settingsPreferences.saveBannerUris(uri.toString())
                    safeSetImageUri(binding.imgBanner, uri)
                    Toast.makeText(this@MainActivity, "Banner diperbarui!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsPreferences = SettingsPreferences(this)

        setupFocusAnimations()
        carregarConfiguracoesSalvas()
        configurarCliques()
        carregarGradeDeApps()

        // Mendaftarkan pendengar untuk memantau pemasangan dan penghapusan
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        registerReceiver(packageReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Membersihkan pendengar untuk menghemat memori saat menutup aplikasi
        try {
            unregisterReceiver(packageReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupFocusAnimations() {
        binding.cardBanner.setupTVFocusAnimation()
        binding.cardApp1.setupTVFocusAnimation()
        binding.cardApp2.setupTVFocusAnimation()
        binding.cardApp3.setupTVFocusAnimation()
        binding.cardSettings.setupTVFocusAnimation()
        binding.cardStore.setupTVFocusAnimation()

        binding.btnWifi.setupTVFocusAnimation()
        binding.btnCast.setupTVFocusAnimation()
        binding.btnBluetooth.setupTVFocusAnimation()

        binding.cardApp1.requestFocus()
    }

    private fun carregarConfiguracoesSalvas() {
        lifecycleScope.launch {
            settingsPreferences.shortcut1Flow.collect {
                if (!it.isNullOrEmpty()) pkgSlot1 = it
                atualizarVisualCard(pkgSlot1, binding.iconApp1)
            }
        }
        lifecycleScope.launch {
            settingsPreferences.shortcut2Flow.collect {
                if (!it.isNullOrEmpty()) pkgSlot2 = it
                atualizarVisualCard(pkgSlot2, binding.iconApp2)
            }
        }
        lifecycleScope.launch {
            settingsPreferences.shortcut3Flow.collect {
                if (!it.isNullOrEmpty()) pkgSlot3 = it
                atualizarVisualCard(pkgSlot3, binding.iconApp3)
            }
        }
        lifecycleScope.launch {
            settingsPreferences.bannerUrisFlow.collect { uriStr ->
                if (!uriStr.isNullOrEmpty()) {
                    try {
                        safeSetImageUri(binding.imgBanner, Uri.parse(uriStr))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    // 2. FUNGSI AMAN: Mencegah crash bila TV Box tidak memiliki pintasan langsung
    private fun abrirConfiguracaoSegura(action: String, fallbackAction: String = Settings.ACTION_SETTINGS) {
        try {
            startActivity(Intent(action))
        } catch (e: Exception) {
            try {
                // Jika gagal, arahkan ke pengaturan umum TV
                startActivity(Intent(fallbackAction))
            } catch (ex: Exception) {
                Toast.makeText(this, "Menu pengaturan tidak ditemukan.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarCliques() {
        // Sekarang tombol-tombol memakai fungsi aman anti-crash
        binding.btnWifi.setOnClickListener { abrirConfiguracaoSegura(Settings.ACTION_WIFI_SETTINGS) }
        binding.btnBluetooth.setOnClickListener { abrirConfiguracaoSegura(Settings.ACTION_BLUETOOTH_SETTINGS) }
        binding.btnCast.setOnClickListener { abrirConfiguracaoSegura(Settings.ACTION_DISPLAY_SETTINGS) } // Pengaturan Tampilan biasanya mencakup transmisi di TV Box

        binding.cardBanner.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            pickBannerImageLauncher.launch(intent)
        }

        binding.cardApp1.setOnClickListener { abrirApp(pkgSlot1) }
        binding.cardApp1.setOnLongClickListener { mostrarSeletor(1); true }

        binding.cardApp2.setOnClickListener { abrirApp(pkgSlot2) }
        binding.cardApp2.setOnLongClickListener { mostrarSeletor(2); true }

        binding.cardApp3.setOnClickListener { abrirApp(pkgSlot3) }
        binding.cardApp3.setOnLongClickListener { mostrarSeletor(3); true }

        binding.cardSettings.setOnClickListener { abrirConfiguracaoSegura(Settings.ACTION_SETTINGS) }
        binding.cardStore.setOnClickListener { abrirApp("com.android.vending") }
    }

    private fun mostrarSeletor(slot: Int) {
        mostrarSeletorDeApps("Pilih Aplikasi untuk Kartu $slot") { novoPacote ->
            lifecycleScope.launch { settingsPreferences.saveShortcut(slot, novoPacote) }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {

            if (event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                val focusedView = currentFocus
                if (focusedView == binding.cardApp1 ||
                    focusedView == binding.cardApp2 ||
                    focusedView == binding.cardApp3 ||
                    focusedView == binding.cardSettings ||
                    focusedView == binding.cardStore) {

                    binding.mainScrollView.smoothScrollTo(0, 1080)

                    binding.appsGridView.postDelayed({
                        binding.appsGridView.getChildAt(0)?.requestFocus()
                    }, 50)

                    return true
                }
            }

            else if (event.keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                val focusedView = currentFocus
                if (focusedView != null && focusedView.parent == binding.appsGridView) {
                    val pos = binding.appsGridView.getPositionForView(focusedView)
                    if (pos in 0..4) {
                        binding.mainScrollView.smoothScrollTo(0, 0)
                        binding.cardApp1.requestFocus()
                        return true
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onBackPressed() {
        if (currentFocus?.parent == binding.appsGridView) {
            binding.mainScrollView.smoothScrollTo(0, 0)
            binding.cardApp1.requestFocus()
        } else {
            super.onBackPressed()
        }
    }

    private fun safeSetImageUri(imageView: ImageView, uri: Uri) {
        try {
            imageView.setImageURI(uri)
        } catch (e: Throwable) {
            e.printStackTrace()
            imageView.setImageResource(R.drawable.bg_wallpaper)
        }
    }

    private fun carregarGradeDeApps() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, 0).sortedBy { it.loadLabel(pm).toString() }

        val appList = resolveInfos.map { resolveInfo ->
            AppItem(
                label = resolveInfo.loadLabel(pm).toString(),
                packageName = resolveInfo.activityInfo.packageName,
                icon = resolveInfo.loadIcon(pm)
            )
        }
        binding.appsGridView.adapter = AppAdapter(appList)
    }

    data class AppItem(val label: String, val packageName: String, val icon: Drawable)

    inner class AppAdapter(private val apps: List<AppItem>) : BaseAdapter() {
        private val inflater = LayoutInflater.from(this@MainActivity)
        override fun getCount(): Int = apps.size
        override fun getItem(position: Int): Any = apps[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: inflater.inflate(R.layout.item_app, parent, false)
            val app = apps[position]

            val imgIcon = view.findViewById<ImageView>(R.id.imgAppIcon)
            val txtName = view.findViewById<TextView>(R.id.txtAppName)

            imgIcon.setImageDrawable(app.icon)
            txtName.text = app.label

            view.setupTVFocusAnimation()

            view.setOnClickListener {
                abrirApp(app.packageName)
            }

            // 3. MENU OPSI: Tahan tombol OK untuk menghapus
            view.setOnLongClickListener {
                val opcoes = arrayOf("Buka aplikasi", "Hapus")
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(app.label)
                    .setItems(opcoes) { _, which ->
                        if (which == 0) {
                            abrirApp(app.packageName)
                        } else if (which == 1) {
                            val uri = Uri.fromParts("package", app.packageName, null)
                            val intent = Intent(Intent.ACTION_DELETE, uri)
                            startActivity(intent)
                        }
                    }
                    .show()
                true
            }

            return view
        }
    }

    private fun mostrarSeletorDeApps(titulo: String, onAppSelected: (String) -> Unit) {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, 0).sortedBy { it.loadLabel(pm).toString() }

        val nomesApps = resolveInfos.map { it.loadLabel(pm).toString() }.toTypedArray()
        val pacotesApps = resolveInfos.map { it.activityInfo.packageName }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setItems(nomesApps) { _, which -> onAppSelected(pacotesApps[which]) }
            .show()
    }

    private fun abrirApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Aplikasi tidak terpasang di TV ini.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun atualizarVisualCard(packageName: String, imageView: ImageView) {
        try {
            val icon = packageManager.getApplicationIcon(packageName)
            imageView.setImageDrawable(icon)
        } catch (e: Exception) {
            imageView.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }
}

fun View.setupTVFocusAnimation() {
    this.isFocusable = true

    this.setOnFocusChangeListener { view, hasFocus ->
        if (hasFocus) {
            view.animate().scaleX(1.08f).scaleY(1.08f).translationZ(20f).setDuration(150).withLayer().start()
            view.setBackgroundResource(R.drawable.bg_card_focused)
        } else {
            view.animate().scaleX(1.0f).scaleY(1.0f).translationZ(0f).setDuration(150).withLayer().start()
            view.setBackgroundResource(R.drawable.bg_card_normal)
        }
    }
}