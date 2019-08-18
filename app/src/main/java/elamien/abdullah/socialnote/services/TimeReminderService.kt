package elamien.abdullah.socialnote.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import elamien.abdullah.socialnote.database.AppDatabase
import elamien.abdullah.socialnote.database.notes.Note
import elamien.abdullah.socialnote.receiver.NoteReminderReceiver
import elamien.abdullah.socialnote.utils.Constants
import java.util.*


class TimeReminderService : JobIntentService() {
	private var mDatabase : AppDatabase? = null

	fun enqueueReminderNotes(context : Context, intent : Intent) {
		enqueueWork(context, TimeReminderService::class.java, Constants.TIME_REMINDER_INTENT_JOB_ID, intent)
	}

	override fun onHandleWork(intent : Intent) {
		val action = intent.action
		if (action == Constants.RE_ADD_TIME_REMINDER_INTENT_ACTION) {
			addNotesToTheAlarmManager()
		}
	}

	private fun addNotesToTheAlarmManager() {
		mDatabase = AppDatabase.getDatabase(applicationContext)
		val noteReminderList = mDatabase?.notesDao()?.getTimeReminderNotes()
		noteReminderList?.forEach { note ->
			addNoteToAlarmManager(note)
		}
	}

	private fun addNoteToAlarmManager(note : Note) {
		if (note.timeReminder?.timeReminder!! >= Date().time) {
			val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
			val alarmIntent = Intent(applicationContext, NoteReminderReceiver::class.java).let { intent ->
				intent.action = Constants.NOTE_TIME_REMINDER_ACTION
				intent.putExtra(Constants.NOTE_INTENT_KEY, note.id)
				intent.putExtra(Constants.NOTE_NOTIFICATION_TEXT_INTENT_KEY, note.note)
				PendingIntent.getBroadcast(applicationContext,
						note.id?.toInt()!!,
						intent,
						PendingIntent.FLAG_UPDATE_CURRENT)
			}
			alarmManager.setExact(AlarmManager.RTC_WAKEUP, note.timeReminder?.timeReminder!!, alarmIntent)
		}
	}

	companion object {
		fun getTimeReminderService() = TimeReminderService()
	}
}