package com.tonyyang.typtt.ui

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tonyyang.typtt.ui.article.ArticleScreen
import com.tonyyang.typtt.ui.article.ArticleViewModel
import com.tonyyang.typtt.ui.board.BoardScreen
import com.tonyyang.typtt.ui.board.BoardViewModel
import com.tonyyang.typtt.ui.hotboard.HotBoardScreen
import com.tonyyang.typtt.ui.hotboard.HotBoardViewModel

const val ROUTE_HOTBOARD = "hotboard"
const val ROUTE_BOARD = "board?name={name}&boardTitle={boardTitle}&url={url}"
const val ROUTE_ARTICLE = "article?articleTitle={articleTitle}&articleUrl={articleUrl}"

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ROUTE_HOTBOARD,
        modifier = modifier
    ) {
        composable(ROUTE_HOTBOARD) {
            val viewModel: HotBoardViewModel = hiltViewModel()
            HotBoardScreen(
                viewModel = viewModel,
                onItemClick = { hotBoard ->
                    navController.navigate(
                        "board" +
                            "?name=${Uri.encode(hotBoard.name)}" +
                            "&boardTitle=${Uri.encode(hotBoard.title)}" +
                            "&url=${Uri.encode(hotBoard.url)}"
                    )
                }
            )
        }

        composable(
            route = ROUTE_BOARD,
            arguments = listOf(
                navArgument("name") { type = NavType.StringType; defaultValue = "" },
                navArgument("boardTitle") { type = NavType.StringType; defaultValue = "" },
                navArgument("url") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url").orEmpty()
            val viewModel: BoardViewModel = hiltViewModel()
            BoardScreen(
                viewModel = viewModel,
                boardUrl = url,
                onItemClick = { articles ->
                    navController.navigate(
                        "article" +
                            "?articleTitle=${Uri.encode(articles.title)}" +
                            "&articleUrl=${Uri.encode(articles.url)}"
                    )
                }
            )
        }

        composable(
            route = ROUTE_ARTICLE,
            arguments = listOf(
                navArgument("articleTitle") { type = NavType.StringType; defaultValue = "" },
                navArgument("articleUrl") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("articleUrl").orEmpty()
            val viewModel: ArticleViewModel = hiltViewModel()
            ArticleScreen(
                viewModel = viewModel,
                articleUrl = url,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
