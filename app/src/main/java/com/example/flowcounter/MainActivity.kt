package com.example.flowcounter

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlowCountScreen()
        }
    }
}

@Composable
fun FlowCountScreen(
    viewModel: FlowCountViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.errorFlow.collectLatest {
            Toast.makeText(context, R.string.toast_error_text, Toast.LENGTH_SHORT).show()
        }
    }

    val paddingBig = dimensionResource(R.dimen.padding_material_big)
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(paddingBig)
            .padding(top = paddingBig),
    ) {
        val (textField, textBox, button) = createRefs()

        var inputText by remember { mutableStateOf("") }

        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }

        TextField(
            value = inputText,
            onValueChange = {
                if (it.all { char -> char.isDigit() }) {
                    inputText = it
                }
            },
            label = { Text(stringResource(R.string.input_number_text)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),
            modifier = Modifier.fillMaxWidth()
                .constrainAs(textField) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .zIndex(1f)
                .focusRequester(focusRequester)
        )

        val displayText by viewModel.resultFlow.collectAsState()

        val scrollState = rememberScrollState()
        Box(modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(vertical = dimensionResource(R.dimen.padding_material_huge))
            .constrainAs(textBox) {
                top.linkTo(textField.bottom)
                bottom.linkTo(button.top)
            }
        ) {
            val textSize = dimensionResource(R.dimen.text_size).value.sp
            Text(
                text = displayText,
                fontSize = textSize,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }

        Button(
            onClick = { viewModel.onButtonClick(inputText) },
            modifier = Modifier.fillMaxWidth()
                .constrainAs(button) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Text(stringResource(R.string.calculate_text))
        }
    }
}