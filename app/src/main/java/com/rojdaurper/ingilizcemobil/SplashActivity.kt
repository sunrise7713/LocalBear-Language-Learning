package com.rojdaurper.ingilizcemobil

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Splash tasarımını (layout) ekrana basar
        setContentView(R.layout.activity_splash)

        // 2000 milisaniye (2 saniye) bekleme süresi
        Handler(Looper.getMainLooper()).postDelayed({

            // HEDEF DEĞİŞTİRİLDİ: Uygulama artık Login'e değil, MainActivity'ye gidiyor
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Splash ekranını kapatıyoruz ki geri tuşuyla buraya dönülmesin
            finish()

        }, 2000)
    }
}