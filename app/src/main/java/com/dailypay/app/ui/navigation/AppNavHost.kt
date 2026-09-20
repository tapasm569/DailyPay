        // Multi-Role Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onAdminLoginSuccess = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onLenderLoginSuccess = { lenderId: String ->
                    navController.navigate(Screen.LenderHome.createRoute(lenderId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBorrowerLoginSuccess = { borrowerId: String ->
                    navController.navigate(Screen.BorrowerDashboard.createRoute(borrowerId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
