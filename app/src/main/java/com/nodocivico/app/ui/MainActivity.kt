package com.nodocivico.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.nodocivico.app.R
import com.nodocivico.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    // Fragments que son destinos "top-level" (sin flecha de retroceso)
    private val topLevelDestinations = setOf(
        R.id.homeFragment,
        R.id.reportListFragment,
        R.id.syncStatusFragment,
        R.id.profileFragment,
        R.id.splashFragment,
        R.id.loginFragment,
        R.id.registerFragment
    )

    // Fragments donde el BottomNav NO debe mostrarse
    private val hideBottomNavDestinations = setOf(
        R.id.splashFragment,
        R.id.loginFragment,
        R.id.registerFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(topLevelDestinations)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)

        // Mostrar/ocultar BottomNav según el destino
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in hideBottomNavDestinations) {
                binding.bottomNav.hide()
                supportActionBar?.hide()
            } else {
                binding.bottomNav.show()
                supportActionBar?.show()
            }
        }

        // Manejar navegación desde notificación a reporte específico
        intent?.getLongExtra("navigate_to_report", -1L)?.takeIf { it != -1L }?.let { reportId ->
            val args = Bundle().apply { putLong("reportId", reportId) }
            navController.navigate(R.id.reportDetailFragment, args)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Extensiones privadas para animar el BottomNav
    private fun com.google.android.material.bottomnavigation.BottomNavigationView.show() {
        animate().translationY(0f).setDuration(200).start()
        visibility = android.view.View.VISIBLE
    }

    private fun com.google.android.material.bottomnavigation.BottomNavigationView.hide() {
        animate().translationY(height.toFloat()).setDuration(200).start()
    }
}
