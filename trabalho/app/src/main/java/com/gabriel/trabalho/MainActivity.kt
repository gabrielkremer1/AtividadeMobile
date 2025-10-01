// Imports necessários para Jetpack Compose e ViewModel
package com.gabriel.trabalho

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel // <-- Resolve 'viewModel'
import java.util.Locale
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBar



// --- 1. MODEL (Produto) ---
/**
 * Data class representando um Produto.
 */
data class Produto(
    val id: Int,
    val nome: String,
    val preco: Double,
    val categoria: String
)

// --- 2. VIEWMODEL (ProdutoViewModel) ---
/**
 * ViewModel que gerencia o estado da lista de produtos em memória.
 * Requisito: Usa mutableStateOf para o estado.
 */
class ProdutoViewModel : ViewModel() {

    // Estado observável pela UI usando delegação 'by'.
    var listaProdutos by mutableStateOf(listOf<Produto>())
        private set

    private var idProduto = 1 // Gerador de ID simples

    /**
     * Adiciona um novo produto se as validações forem atendidas.
     */
    fun adicionarProduto(nome: String, precoStr: String, categoria: String): Boolean {
        // Tenta converter o preço, aceitando vírgula ou ponto
        val preco = precoStr.replace(',', '.').toDoubleOrNull()

        // Validação: nome/categoria não vazios, preço numérico e > 0
        if (nome.isBlank() || categoria.isBlank() || preco == null || preco <= 0) {
            return false // Falha na validação
        }

        val novoProduto = Produto(
            id = idProduto++,
            nome = nome.trim(),
            preco = preco,
            categoria = categoria.trim()
        )

        // Atualização da lista de forma imutável
        listaProdutos = listaProdutos + novoProduto
        return true // Sucesso
    }

    /**
     * Deleta um produto pelo seu ID.
     */
    fun deletarProduto(id: Int) {
        // Atualiza a lista filtrando para remover o item com o ID correspondente
        listaProdutos = listaProdutos.filter { it.id != id }
    }
}


// --- 3. UI (COMPOSABLES) ---

/**
 * Composable que representa a tela principal de Cadastro de Produtos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCadastroProdutos(vm: ProdutoViewModel = viewModel()) {

    // Estado local para os campos do formulário
    var nome by rememberSaveable { mutableStateOf("") }
    var precoStr by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Lógica de Validação do Formulário
    val precoDouble = precoStr.replace(',', '.').toDoubleOrNull()
    val isFormValid = remember(nome, precoDouble, categoria) {
        nome.isNotBlank() && categoria.isNotBlank() && precoDouble != null && precoDouble > 0
    }

    // Ação de Cadastro
    val onAddProduct = {
        // UI dispara a ação no VM
        val sucesso = vm.adicionarProduto(nome, precoStr, categoria)

        if (sucesso) {
            // Requisito: Limpar campos e remover foco após adicionar
            nome = ""
            precoStr = ""
            categoria = ""
            focusManager.clearFocus()
            ExibirToast(context, "Produto Cadastrado!")
        } else {
            ExibirToast(context, "Erro: Preencha todos os campos corretamente.")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastro de Produtos - MVVM") },
                colors = TopAppBarDefaults.topAppBarColors( // <-- Resolve 'topAppBarColors'
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Formulário de Cadastro ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Campo Nome
                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = it },
                        label = { Text("Nome do Produto") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo Preço
                    OutlinedTextField(
                        value = precoStr,
                        onValueChange = { newValue ->
                            precoStr = newValue.filter { it.isDigit() || it == '.' || it == ',' }
                        },
                        label = { Text("Preço (numérico > 0)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal, // <-- Resolve 'NumberDecimal'
                            imeAction = ImeAction.Next
                        ),
                        isError = precoStr.isNotBlank() && (precoDouble == null || precoDouble <= 0),
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo Categoria
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = { categoria = it },
                        label = { Text("Categoria") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (isFormValid) onAddProduct()
                        })
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Botão Adicionar Produto
                    Button(
                        onClick = onAddProduct,
                        // Requisito obrigatório: Habilitado só quando o formulário estiver válido
                        enabled = isFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = if (isFormValid) "Adicionar Produto" else "Preencha Todos os Campos",
                            fontWeight = FontWeight.Bold,
                            color = if (isFormValid) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- Lista de Produtos ---
            Text(
                text = "Produtos Cadastrados (${vm.listaProdutos.size})",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )
            Divider()

            if (vm.listaProdutos.isEmpty()) {
                Text(
                    "A lista está vazia.",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vm.listaProdutos, key = { it.id }) { produto ->
                        ProdutoItem(produto = produto,
                            onDelete = { vm.deletarProduto(produto.id) })
                    }
                }
            }
        }
    }
}

/**
 * Composable para exibir um item de produto na lista.
 * Inclui o botão de exclusão (Deletar).
 */
@Composable
fun ProdutoItem(produto: Produto, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = produto.nome,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
                Text(
                    text = "ID: ${produto.id} | Cat.: ${produto.categoria}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    // Formatação de preço (melhoria de UX)
                    text = "Preço: R$ ${String.format(Locale.getDefault(), "%,.2f", produto.preco)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            // Botão Deletar (Requisito)
            FilledTonalButton(
                onClick = onDelete,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Deletar Produto")
            }
        }
    }
}

/**
 * Função utilitária para exibir Toast.
 */
fun ExibirToast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

// --- CLASSE PRINCIPAL DA ATIVIDADE ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Definição de um esquema de cores básico
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF6200EE),
                    primaryContainer = Color(0xFFBB86FC),
                    secondary = Color(0xFF03DAC5),
                    error = Color(0xFFB00020),
                    background = Color.White,
                    surface = Color.White,
                )
            ) {
                AppCadastroProdutos()
            }
        }
    }
}
