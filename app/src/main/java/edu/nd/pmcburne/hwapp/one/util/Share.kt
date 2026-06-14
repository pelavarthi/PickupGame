package edu.nd.pmcburne.hwapp.one.util

import android.content.Context
import android.content.Intent
import edu.nd.pmcburne.hwapp.one.model.Game
import java.text.SimpleDateFormat
import java.util.Locale

object Share {
    fun shareText(ctx: Context, subject: String, body: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        val chooser = Intent.createChooser(send, "Share via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(chooser)
    }

    fun shareGame(ctx: Context, game: Game) {
        val fmt = SimpleDateFormat("EEE, MMM d 'at' h:mm a", Locale.getDefault())
        val when_ = fmt.format(game.dateTime.toDate())
        val body = buildString {
            append("Join my pickup ${game.sport} game!\n")
            append("$when_ at ${game.location}\n")
            append("${game.currentPlayers}/${game.maxPlayers} players signed up.\n")
            if (game.notes.isNotBlank()) append("\n${game.notes}\n")
            append("\nFind it in PickupGame.")
        }
        shareText(ctx, "Pickup ${game.sport} game", body)
    }

    fun shareProfile(ctx: Context, displayName: String, joined: Int, hosted: Int) {
        val body = "Check out my PickupGame profile! I'm $displayName — " +
            "joined $joined games, hosted $hosted. Find me on PickupGame."
        shareText(ctx, "My PickupGame profile", body)
    }
}
