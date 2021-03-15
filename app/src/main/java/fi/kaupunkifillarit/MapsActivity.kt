package fi.kaupunkifillarit

import androidx.appcompat.app.AppCompatActivity
import fi.kaupunkifillarit.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}