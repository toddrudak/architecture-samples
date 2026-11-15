package com.example.android.architecture.blueprints.todoapp.addedittask

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.android.architecture.blueprints.todoapp.R

@Composable
fun SaveTaskExtendedButton(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(Icons.Filled.Done, stringResource(id = R.string.cd_save_task)) },
        text = { Text("Save task") }
    )
}
