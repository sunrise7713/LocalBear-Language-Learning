package com.rojdaurper.ingilizcemobil

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {

    // NavView referansını onStart içinde kullanabilmek için saklıyoruz
    private var bottomNavView: BottomNavigationView? = null

    protected fun setupCommonNavigation(navView: BottomNavigationView) {
        this.bottomNavView = navView

        // Tıklama olayını bir kez bağlamak yeterli
        navView.setOnItemSelectedListener { item ->
            val currentId = getCurrentId()
            if (item.itemId == currentId) {
                false
            } else {
                val intent = when (item.itemId) {
                    R.id.nav_explore -> Intent(this, MainActivity::class.java)
                    R.id.nav_saved -> Intent(this, FavoritesActivity::class.java)
                    R.id.nav_learn -> Intent(this, LearnActivity::class.java)
                    else -> null
                }

                // BaseActivity.kt içindeki intent?.let bloğunu şu şekilde güncelleyin:
                intent?.let {
                    it.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(it)

                    // Eski ve yeni yöntemleri beraber kullanarak animasyonu her cihazda sıfırlıyoruz
                    if (android.os.Build.VERSION.SDK_INT >= 34) {
                        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
                        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
                    } else {
                        @Suppress("DEPRECATION")
                        overridePendingTransition(0, 0)
                    }
                }
                true
            }
        }
    }

    // SAYFA HER ÖNE GELDİĞİNDE (Görünür olduğunda) ÇALIŞIR
    override fun onStart() {
        super.onStart()
        bottomNavView?.let { navView ->
            val currentId = getCurrentId()
            if (currentId != -1) {
                // Dinleyiciyi geçici olarak kapatıp doğru ikonu seçiyoruz
                navView.setOnItemSelectedListener(null)
                navView.selectedItemId = currentId
                // Dinleyiciyi tekrar kuruyoruz
                setupCommonNavigation(navView)
            }
        }
    }

    private fun getCurrentId(): Int {
        return when (this) {
            is MainActivity -> R.id.nav_explore
            is FavoritesActivity -> R.id.nav_saved
            is LearnActivity -> R.id.nav_learn
            else -> -1
        }
    }
}