package com.foodmaster.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.foodmaster.app.inventory.InventoryScreen
import com.foodmaster.app.scanner.ScannerScreen
import com.foodmaster.app.shopping.ShoppingScreen
import com.foodmaster.app.ui.FoodMasterTheme

private enum class Tab(val titleRes: Int, val tabRes: Int, val icon: ImageVector) {
    Inventory(R.string.title_inventory, R.string.tab_inventory, Icons.Filled.Inventory2),
    Scan(R.string.app_name, R.string.tab_scan, Icons.Filled.QrCodeScanner),
    Shopping(R.string.title_shopping, R.string.tab_shopping, Icons.Filled.ShoppingCart),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodMasterTheme {
                FoodMasterApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodMasterApp() {
    var tab by rememberSaveable { mutableStateOf(Tab.Scan) }

    Scaffold(
        topBar = {
            // The scanner is full-bleed camera, so it gets no top bar.
            if (tab != Tab.Scan) {
                CenterAlignedTopAppBar(title = { Text(stringResource(tab.titleRes)) })
            }
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { entry ->
                    NavigationBarItem(
                        selected = tab == entry,
                        onClick = { tab = entry },
                        icon = { Icon(entry.icon, contentDescription = null) },
                        label = { Text(stringResource(entry.tabRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (tab) {
            Tab.Inventory -> InventoryScreen(contentPadding = innerPadding)
            Tab.Scan -> ScannerScreen(
                onClose = { tab = Tab.Inventory },
                modifier = Modifier.padding(innerPadding),
            )
            Tab.Shopping -> ShoppingScreen(contentPadding = innerPadding)
        }
    }
}
