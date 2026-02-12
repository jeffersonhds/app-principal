import os
import shutil

# --- CONFIGURAÇÕES DA OBRA ---
PASTA_ORIGEM = "./app/src/main/java" 
PASTA_DESTINO = "./REMESSA_PARA_IA"
IGNORAR_PASTAS = {"build", "generated", ".gradle"}
# -----------------------------

def copiar_arquivos_projeto():
    if os.path.exists(PASTA_DESTINO):
        shutil.rmtree(PASTA_DESTINO)
    os.makedirs(PASTA_DESTINO)
    
    print(f"🏗️  Iniciando a coleta de arquivos em: {PASTA_ORIGEM}")
    print(f"📂 Destino: {PASTA_DESTINO}\n")

    arquivos_copiados = 0

    for root, dirs, files in os.walk(PASTA_ORIGEM):
        dirs[:] = [d for d in dirs if d not in IGNORAR_PASTAS]

        for file in files:
            if file.endswith(".kt"): 
                caminho_origem = os.path.join(root, file)
                
                nome_final = file
                caminho_destino = os.path.join(PASTA_DESTINO, nome_final)
                
                contador = 1
                while os.path.exists(caminho_destino):
                    nome_sem_ext = os.path.splitext(file)[0]
                    nome_final = f"{nome_sem_ext}_{contador}.kt"
                    caminho_destino = os.path.join(PASTA_DESTINO, nome_final)
                    contador += 1

                shutil.copy2(caminho_origem, caminho_destino)
                print(f"✅ Copiado: {nome_final}")
                arquivos_copiados += 1

    print(f"\n🚀 SUCESSO! {arquivos_copiados} arquivos prontos na pasta '{PASTA_DESTINO}'.")

if __name__ == "__main__":
    copiar_arquivos_projeto()
