package edu.nd.pmcburne.hwapp.one.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import edu.nd.pmcburne.hwapp.one.ui.theme.ChipGreen
import edu.nd.pmcburne.hwapp.one.ui.theme.PickupGreen

@Composable
fun BottomNavBar(
    currentTab: Tab,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = currentTab == Tab.Home,
            onClick = onHomeClick,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PickupGreen,
                selectedTextColor = PickupGreen,
                indicatorColor = ChipGreen,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            selected = currentTab == Tab.Profile,
            onClick = onProfileClick,
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PickupGreen,
                selectedTextColor = PickupGreen,
                indicatorColor = ChipGreen,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}

enum class Tab { Home, Profile }
