package com.example.laundrytallyai.pages.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.laundrytallyai.R
import com.example.laundrytallyai.api.datastates.DataState
import com.example.laundrytallyai.navigation.Screen
import com.example.laundrytallyai.pages.auth.AuthUtils.LaundryLogoIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {

    val viewModel: AuthViewModel = hiltViewModel()

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        val registerState by viewModel.authState.collectAsState()
        val username = remember { mutableStateOf("") }
        val email = remember { mutableStateOf("") }
        val password = remember { mutableStateOf("") }
        val confirmPassword = remember { mutableStateOf("") }
        val passwordVisible = remember { mutableStateOf(false) }
        val confirmPasswordVisible = remember { mutableStateOf(false) }

        if(registerState is DataState.Success) {
            navController.navigate(Screen.Home.route)
        }

        Spacer(modifier = Modifier.height(8.dp))

        LaundryLogoIcon()

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = username.value,
            onValueChange = {username.value = it},
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email.value,
            onValueChange = {email.value = it},
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = AuthUtils.checkEmail(email.value) && email.value.isNotEmpty(),
            supportingText = {
                if (AuthUtils.checkEmail(email.value) && email.value.isNotEmpty()) {
                    Text(
                        text = "Invalid Email",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            })

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password.value,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { password.value = it },
            label = { Text("Password") },
            singleLine = true,
            placeholder = { Text("Password") },
            visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible.value)
                    ImageVector.vectorResource(id = R.drawable.eye)
                else ImageVector.vectorResource(id = R.drawable.eye_crossed)

                val description = if (passwordVisible.value) "Hide password" else "Show password"

                IconButton(onClick = {passwordVisible.value = !passwordVisible.value}){
                    Icon(imageVector  = image, description)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = confirmPassword.value,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { confirmPassword.value = it },
            label = { Text("Confirm Password") },
            singleLine = true,
            placeholder = { Text("Confirm Password") },
            visualTransformation = if (confirmPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (confirmPasswordVisible.value)
                    ImageVector.vectorResource(id = R.drawable.eye)
                else ImageVector.vectorResource(id = R.drawable.eye_crossed)

                val description = if (confirmPasswordVisible.value) "Hide password" else "Show password"

                IconButton(onClick = {confirmPasswordVisible.value = !confirmPasswordVisible.value}){
                    Icon(imageVector  = image, description)
                }
            },
            isError = password.value != confirmPassword.value,
            supportingText = {
                if (password.value != confirmPassword.value) {
                    Text(
                        text = "Passwords do not match",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (registerState is DataState.Error && (registerState as DataState.Error).message == "400") {
            Text(
                text = "Email is taken!",
                color = MaterialTheme.colorScheme.error,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { viewModel.register(
                email = email.value,
                password = password.value,
                username = username.value
            ) },
            modifier = Modifier.fillMaxWidth(),
            enabled = username.value.isNotEmpty() &&
                    email.value.isNotEmpty() &&
                    password.value.isNotEmpty() &&
                    confirmPassword.value.isNotEmpty() &&
                    password.value == confirmPassword.value &&
                    !AuthUtils.checkEmail(email.value)
        ) {
            Text(text = "Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ")
            Text(
                text = "Login Here",
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clickable { navController.navigate(Screen.Login.route) },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}