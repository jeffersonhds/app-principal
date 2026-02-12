#!/bin/bash

# Script para consolidar todos os arquivos Kotlin em um único arquivo
# Use: ./consolidate_kotlin.sh ou bash consolidate_kotlin.sh

OUTPUT_FILE="codigo_consolidado.kt"
PROJECT_ROOT="."

# Remove arquivo anterior se existir
if [ -f "$OUTPUT_FILE" ]; then
    rm "$OUTPUT_FILE"
fi

# Cria cabeçalho
echo "// ============================================" >> "$OUTPUT_FILE"
echo "// CÓDIGO CONSOLIDADO DO APP" >> "$OUTPUT_FILE"
echo "// Gerado em: $(date)" >> "$OUTPUT_FILE"
echo "// ============================================" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Encontra todos os arquivos .kt e adiciona ao arquivo consolidado
find "$PROJECT_ROOT" -name "*.kt" -type f | sort | while read file; do
    # Pula arquivos de build
    if [[ "$file" == *"build/"* ]]; then
        continue
    fi
    
    echo "// ============================================" >> "$OUTPUT_FILE"
    echo "// Arquivo: $file" >> "$OUTPUT_FILE"
    echo "// ============================================" >> "$OUTPUT_FILE"
    cat "$file" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
    echo "" >> "$OUTPUT_FILE"
done

echo "✓ Código consolidado em: $OUTPUT_FILE"
echo "Total de linhas: $(wc -l < "$OUTPUT_FILE")"
