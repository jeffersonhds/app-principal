package com.jefferson.antenas.ui.componets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.theme.*

// ✅ BARRA DE BUSCA COMPACTA (estilo marketplace)
@Composable
fun StoreSearchBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Buscar produtos...", color = TextSecondary, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SignalOrange) },
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardGradientStart,
            unfocusedContainerColor = CardGradientStart,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = SignalOrange,
            unfocusedBorderColor = Color.Transparent
        ),
        shape = RoundedCornerShape(26.dp),
        singleLine = true
    )
}

// ✅ HEADER DA LOJA COM BUSCA (mantido para compatibilidade)
@Composable
fun StoreHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MidnightBlueStart)
            .padding(16.dp)
    ) {
        Text(
            text = "Loja Completa",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Buscar produtos...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SignalOrange) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardGradientStart,
                unfocusedContainerColor = CardGradientStart,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = SignalOrange,
                unfocusedBorderColor = TextSecondary
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

// ✅ FILTROS HORIZONTAIS — chips com scroll edge-to-edge
data class FilterOption(
    val id: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null
)

@Composable
fun HorizontalFilters(
    filters: List<FilterOption>,
    selectedFilter: String?,
    onFilterSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { onFilterSelected(null) },
                label = { Text("Todos", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SignalOrange,
                    selectedLabelColor = MidnightBlueStart,
                    containerColor = CardGradientStart,
                    labelColor = TextSecondary
                )
            )
        }
        items(filters) { filter ->
            val isSelected = selectedFilter == filter.id
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(if (isSelected) null else filter.id) },
                label = { Text(filter.label, fontSize = 12.sp) },
                leadingIcon = filter.icon?.let { icon ->
                    { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SignalOrange,
                    selectedLabelColor = MidnightBlueStart,
                    containerColor = CardGradientStart,
                    labelColor = TextSecondary
                )
            )
        }
    }
}

// ✅ OPÇÕES DE ORDENAÇÃO
data class SortOption(
    val id: String,
    val label: String
)

// ✅ LINHA COMPACTA: contagem de resultados + dropdown de ordenação
@Composable
fun SortAndResultsRow(
    filteredCount: Int,
    sortOptions: List<SortOption>,
    selectedSort: String,
    onSortSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$filteredCount produto${if (filteredCount != 1) "s" else ""}",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalOrange),
                border = androidx.compose.foundation.BorderStroke(1.dp, SignalOrange),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.Sort, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    sortOptions.find { it.id == selectedSort }?.label ?: "Ordenar",
                    fontSize = 11.sp
                )
                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(14.dp))
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(CardGradientStart)
            ) {
                sortOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                option.label,
                                color = if (option.id == selectedSort) SignalOrange else TextPrimary,
                                fontWeight = if (option.id == selectedSort) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSortSelected(option.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ✅ DROPDOWN COMPLETO (mantido para compatibilidade)
@Composable
fun SortDropdown(
    sortOptions: List<SortOption>,
    selectedSort: String,
    onSortSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.padding(horizontal = 16.dp)) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(40.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalOrange),
            border = androidx.compose.foundation.BorderStroke(1.dp, SignalOrange),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Sort, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Ordenar por: ${sortOptions.find { it.id == selectedSort }?.label ?: "Padrão"}",
                fontSize = 12.sp,
                maxLines = 1
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f).background(CardGradientStart)
        ) {
            sortOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option.label,
                            color = if (option.id == selectedSort) SignalOrange else TextPrimary,
                            fontWeight = if (option.id == selectedSort) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = { onSortSelected(option.id); expanded = false }
                )
            }
        }
    }
}

// ✅ INDICADOR DE RESULTADOS (mantido para compatibilidade)
@Composable
fun ResultsInfo(
    totalProducts: Int,
    filteredProducts: Int,
    modifier: Modifier = Modifier
) {
    if (totalProducts == 0) return
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Mostrando $filteredProducts de $totalProducts produtos",
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

// ✅ EMPTY STATE
@Composable
fun EmptyStoreState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingBag,
            contentDescription = null,
            tint = SignalOrange,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Nenhum Produto Encontrado", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            "Tente ajustar seus filtros ou faça uma nova busca",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}