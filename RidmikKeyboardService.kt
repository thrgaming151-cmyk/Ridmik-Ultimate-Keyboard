package com.example.ridmikkeyboard

import android.inputmethodservice.InputMethodService
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import android.widget.TextView
import android.content.Intent
import android.speech.RecognizerIntent

class RidmikKeyboardService : InputMethodService() {

    private var keyboardView: View? = null
    private var initialX = 0f

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null)
        setupKeyboard()
        return keyboardView!!
    }

    private fun setupKeyboard() {
        val container = keyboardView?.findViewById<LinearLayout>(R.id.keyRowsContainer) ?: return
        container.removeAllViews()

        // Jatiya Layout Keys
        val row1 = arrayOf("১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "০")
        val row2 = arrayOf("ড়", "ঢ়", "অ", "ই", "উ", "ঋ", "এ", "ঐ", "ও", "ঔ")
        val row3 = arrayOf("ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ")
        val row4 = arrayOf("ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", "ন")
        val row5 = arrayOf("প", "ফ", "ব", "ভ", "ম", "য", "র", "ল", "শ", "ষ")
        val row6 = arrayOf("স", "হ", "ক্ষ", "জ্ঞ", "ড়", "ঢ়", "য়", "ৎ", "ং", "ঃ")
        
        val allRows = arrayOf(row1, row2, row3, row4, row5, row6)

        for (rowKeys in allRows) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            for (key in rowKeys) {
                val keyView = layoutInflater.inflate(R.layout.key_view, rowLayout, false) as TextView
                keyView.text = key
                keyView.setOnClickListener {
                    currentInputConnection?.commitText(key, 1)
                }
                rowLayout.addView(keyView)
            }
            container.addView(rowLayout)
        }

        // Action Row: Space, Cut, Copy, Paste, Voice, Delete
        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val cutKey = createActionButton("X Cut") { handleCut() }
        val copyKey = createActionButton("C Copy") { handleCopy() }
        val pasteKey = createActionButton("V Paste") { handlePaste() }
        val spaceKey = createActionButton("Space (Swipe Cursor)") { currentInputConnection?.commitText(" ", 1) }
        val voiceKey = createActionButton("🎤 Voice") { startVoiceInput() }
        val delKey = createActionButton("⌫ Delete") { 
            currentInputConnection?.deleteSurroundingText(1, 0) 
        }

        // Swipe on Spacebar for Cursor Control
        spaceKey.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> initialX = event.x
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - initialX
                    if (deltaX > 30) {
                        currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_RIGHT))
                        initialX = event.x
                    } else if (deltaX < -30) {
                        currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_LEFT))
                        initialX = event.x
                    }
                }
            }
            false
        }

        actionRow.addView(cutKey)
        actionRow.addView(copyKey)
        actionRow.addView(pasteKey)
        actionRow.addView(spaceKey)
        actionRow.addView(voiceKey)
        actionRow.addView(delKey)

        container.addView(actionRow)
    }

    private fun createActionButton(label: String, onClick: () -> Unit): TextView {
        val btn = layoutInflater.inflate(R.layout.key_view, null, false) as TextView
        btn.text = label
        btn.setOnClickListener { onClick() }
        return btn
    }

    // Cut Functionality
    private fun handleCut() {
        val ic = currentInputConnection ?: return
        val selectedText = ic.getSelectedText(0)
        if (!selectedText.isNull_or_Empty()) {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("text", selectedText)
            clipboard.setPrimaryClip(clip)
            ic.commitText("", 1)
        }
    }

    // Copy Functionality
    private fun handleCopy() {
        val ic = currentInputConnection ?: return
        val selectedText = ic.getSelectedText(0)
        if (!selectedText.isNull_or_Empty()) {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("text", selectedText)
            clipboard.setPrimaryClip(clip)
        }
    }

    // Paste Functionality
    private fun handlePaste() {
        val ic = currentInputConnection ?: return
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clipData = clipboard.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val textToPaste = clipData.getItemAt(0).coerceToText(this)
            ic.commitText(textToPaste, 1)
        }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Handle error
        }
    }
}
